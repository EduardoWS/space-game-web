import os
import json
from typing import List, Optional
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import firebase_admin
from firebase_admin import credentials, firestore

# Initialize FastAPI
app = FastAPI(title="Space Game API")

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allow all origins for now (adjust for production)
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize Firebase
# Try to get credentials from environment variable (Render) or local file
cred = None
if os.getenv("FIREBASE_CREDENTIALS"):
    # If the JSON content is stored in an env variable
    cred_json = json.loads(os.getenv("FIREBASE_CREDENTIALS"))
    cred = credentials.Certificate(cred_json)
elif os.getenv("GOOGLE_APPLICATION_CREDENTIALS"):
    # Standard Google Auth way
    cred = credentials.Certificate(os.getenv("GOOGLE_APPLICATION_CREDENTIALS"))
else:
    # Fallback/Dev check (You might want to place a serviceAccountKey.json in backend/ for local dev)
    try:
        cred = credentials.Certificate("serviceAccountKey.json")
    except:
        print("Warning: No Firebase credentials found. Firestore will not work.")

if cred:
    firebase_admin.initialize_app(cred)
    db = firestore.client()
else:
    db = None

class ScoreEntry(BaseModel):
    playerName: str
    score: int

class ScoreResponse(BaseModel):
    playerName: str
    score: int

@app.get("/")
def read_root():
    return {"status": "online", "message": "Space Game Backend is Running"}

@app.get("/scores", response_model=List[ScoreResponse])
def get_scores():
    if not db:
        raise HTTPException(status_code=503, detail="Database not initialized")
    
    try:
        # Get top 10 scores
        scores_ref = db.collection('scores')
        query = scores_ref.order_by('score', direction=firestore.Query.DESCENDING).limit(10)
        results = query.stream()
        
        scores = []
        for doc in results:
            data = doc.to_dict()
            scores.append(ScoreResponse(playerName=data.get('playerName', 'Unknown'), score=data.get('score', 0)))
            
        return scores
    except Exception as e:
        print(f"Error fetching scores: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/scores")
def add_score(entry: ScoreEntry):
    if not db:
        raise HTTPException(status_code=503, detail="Database not initialized")
    
    try:
        scores_ref = db.collection('scores')
        
        # Add the new score
        scores_ref.add(entry.dict())
        
        # Maintenance: Keep only top 10 (optional, but requested by user)
        # We query top 11 to see if we need to remove one.
        # Note: In a high-concurrency environment, this might be race-condition prone, 
        # but for a simple game it's fine.
        
        # Re-query all (or logic to count) - Efficient way for Firestore is to just let them pile up 
        # or run a scheduled function. But user asked to "guardar os 10". 
        # We will try to delete the lowest if count > 10.
        
        # Maintenance: Keep only top 10
        # We fetch top 11 to check if we have more than 10
        all_scores_query = scores_ref.order_by('score', direction=firestore.Query.DESCENDING)
        # We need to fetch enough to find the 11th item. 
        # Ideally, we don't want to fetch ALL if there are millions, but for now we assume small volume 
        # or we just fetch top 11.
        top_11_query = scores_ref.order_by('score', direction=firestore.Query.DESCENDING).limit(11)
        top_11 = list(top_11_query.stream())
        
        if len(top_11) > 10:
            # The 11th item and anything after (if we queried more) should be deleted.
            # Since we just added one, we might have 11 items now.
            # We delete the one at index 10 (the 11th item)
            doc_to_delete = top_11[10]
            doc_to_delete.reference.delete()
            
            # If for some reason there were more (e.g. manual insertion), we could loop and delete, 
            # but standard flow adds 1 and deletes 1.
            print(f"Deleted overflow score: {doc_to_delete.id}")
            
        return {"message": "Score saved successfully"}
    except Exception as e:
        print(f"Error saving score: {e}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    # Bind to 0.0.0.0 for Render support
    port = int(os.environ.get("PORT", 8000))
    uvicorn.run("main:app", host="0.0.0.0", port=port, reload=True)
