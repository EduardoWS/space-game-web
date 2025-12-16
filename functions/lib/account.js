"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.deleteAccount = void 0;
const functions = require("firebase-functions");
const admin = require("firebase-admin");
/**
 * Callable function to delete the authenticated user's account and all associated data.
 * This ensures atomic cleanup of:
 * - User Profile (users/{uid})
 * - Username Reservation (usernames/{username})
 * - Global Scores (scores where uid == uid)
 * - Firebase Auth Account
 */
exports.deleteAccount = functions.https.onCall(async (data, context) => {
    // 1. Ensure User is Authenticated
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "The function must be called while authenticated.");
    }
    const uid = context.auth.uid;
    const db = admin.firestore();
    try {
        // 2. Fetch User Profile to get the Username
        const userDocRef = db.collection("users").doc(uid);
        const userDoc = await userDocRef.get();
        let username = null;
        if (userDoc.exists) {
            username = userDoc.data()?.username;
        }
        // 3. Begin Batch Operation for Firestore Data
        const batch = db.batch();
        // Delete User Profile
        batch.delete(userDocRef);
        // Delete Username Reservation (if it exists)
        if (username) {
            const usernameRef = db.collection("usernames").doc(username);
            batch.delete(usernameRef);
        }
        // Delete User's Scores (Query for all scores by this UID)
        const scoresQuery = await db.collection("scores").where("uid", "==", uid).get();
        scoresQuery.forEach((doc) => {
            batch.delete(doc.ref);
        });
        // Commit the batch
        await batch.commit();
        // 4. Delete the Firebase Auth User
        await admin.auth().deleteUser(uid);
        console.log(`Successfully deleted account for user ${uid} (username: ${username})`);
        return { success: true };
    }
    catch (error) {
        console.error("Error deleting account:", error);
        throw new functions.https.HttpsError("internal", "Failed to delete account. Please try again later.");
    }
});
//# sourceMappingURL=account.js.map