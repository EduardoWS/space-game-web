import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";

const firebaseConfig = {
  apiKey: "AIzaSyDvraBN9ZbLDS8rGpOHTpNlS56rDKkrBp0",
  authDomain: "space-game-web.firebaseapp.com",
  projectId: "space-game-web",
  storageBucket: "space-game-web.firebasestorage.app",
  messagingSenderId: "558062429284",
  appId: "1:558062429284:web:51c0748573ec10359cab71"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
export default app;
