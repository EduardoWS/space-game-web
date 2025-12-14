const express = require('express');
const cors = require('cors');
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');

// Initialize Express
const app = express();
app.use(cors());
app.use(express.json());

// Initialize Firebase
// Instructions:
// 1. Create a Firebase Project
// 2. Generate a Service Account Key (JSON file)
// 3. For Render deployment: Create an Env Var GOOGLE_APPLICATION_CREDENTIALS_JSON with the content of the JSON file
// 4. For Local dev: Set GOOGLE_APPLICATION_CREDENTIALS to the path of your json file
if (process.env.GOOGLE_APPLICATION_CREDENTIALS_JSON) {
    try {
        const serviceAccount = JSON.parse(process.env.GOOGLE_APPLICATION_CREDENTIALS_JSON);
        initializeApp({
            credential: cert(serviceAccount)
        });
    } catch (e) {
        console.error("Error parsing GOOGLE_APPLICATION_CREDENTIALS_JSON", e);
    }
} else {
    // Falls back to GOOGLE_APPLICATION_CREDENTIALS env var or default machine identity
    initializeApp(); 
}

const db = getFirestore();
const scoresCollection = db.collection('scores');

// GET Top 10 Scores
app.get('/api/scores', async (req, res) => {
    try {
        const snapshot = await scoresCollection
            .orderBy('score', 'desc')
            .limit(10)
            .get();
        
        const scores = [];
        snapshot.forEach(doc => {
            scores.push(doc.data());
        });
        
        res.json(scores);
    } catch (error) {
        console.error(error);
        res.status(500).send('Error retrieving scores');
    }
});

// POST New Score
app.post('/api/scores', async (req, res) => {
    const { playerName, score } = req.body;
    
    if (!playerName || score === undefined) {
        return res.status(400).send('Invalid data, playerName and score required');
    }

    try {
        await scoresCollection.add({
            playerName,
            score: Number(score),
            timestamp: new Date()
        });
        res.status(201).send('Score saved');
    } catch (error) {
        console.error(error);
        res.status(500).send('Error saving score');
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
