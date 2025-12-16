import * as admin from "firebase-admin";

// Initialize Firebase Admin globally once
admin.initializeApp();

// Export Cloud Functions from separate modules
export * from "./leaderboard";
export * from "./account";
