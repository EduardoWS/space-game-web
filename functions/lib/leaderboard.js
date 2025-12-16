"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.maintainLeaderboard = void 0;
const functions = require("firebase-functions");
const admin = require("firebase-admin");
const SCORES_COLLECTION = "scores";
const MAX_SCORES = 10;
/**
 * Triggered by any change (create, update, delete) to the 'scores' collection.
 * Maintains strictly the top 10 scores by deleting the lowest ones if the count exceeds 10.
 */
exports.maintainLeaderboard = functions.firestore
    .document("scores/{scoreId}")
    .onWrite(async (change, context) => {
    // Get the Firestore instance (initialized in index.ts, or verify usage here)
    const db = admin.firestore();
    // If the document was deleted, we usually don't need to do anything.
    if (!change.after.exists) {
        return null;
    }
    try {
        // Get all scores ordered by score (descending)
        const snapshot = await db.collection(SCORES_COLLECTION)
            .orderBy("score", "desc")
            .get();
        if (snapshot.size <= MAX_SCORES) {
            return null; // Within limits
        }
        // Identify documents to delete (all documents from index MAX_SCORES onwards)
        const batch = db.batch();
        let deleteCount = 0;
        const docs = snapshot.docs;
        for (let i = MAX_SCORES; i < docs.length; i++) {
            batch.delete(docs[i].ref);
            deleteCount++;
        }
        if (deleteCount > 0) {
            await batch.commit();
            functions.logger.log(`Maintained leaderboard: Deleted ${deleteCount} excess scores.`);
        }
    }
    catch (error) {
        functions.logger.error("Error maintaining leaderboard:", error);
    }
    return null;
});
//# sourceMappingURL=leaderboard.js.map