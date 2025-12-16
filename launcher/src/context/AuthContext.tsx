import React, { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { auth, db } from "../firebase";
import { onAuthStateChanged, type User } from "firebase/auth";
import { doc, onSnapshot, type DocumentData, type DocumentSnapshot, type FirestoreError } from "firebase/firestore";

interface AuthContextType {
  currentUser: User | null;
  userData: DocumentData | null;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [userData, setUserData] = useState<DocumentData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let unsubscribeSnapshot: (() => void) | undefined;

    const unsubscribeAuth = onAuthStateChanged(auth, (user) => {
      setCurrentUser(user);

      // Clean up previous snapshot listener
      if (unsubscribeSnapshot) {
        unsubscribeSnapshot();
        unsubscribeSnapshot = undefined;
      }

      if (user) {
        setLoading(true);
        // Subscribe to user profile using onSnapshot for real-time updates
        const docRef = doc(db, "users", user.uid);
        unsubscribeSnapshot = onSnapshot(docRef, (docSnap: DocumentSnapshot<DocumentData>) => {
          if (docSnap.exists()) {
            setUserData(docSnap.data());
          } else {
            setUserData(null);
          }
          // Only set loading to false AFTER we have the profile data
          setLoading(false);
        }, (error: FirestoreError) => {
          console.error("Error listening to user data", error);
          setUserData(null);
          setLoading(false);
        });
      } else {
        setUserData(null);
        setLoading(false);
      }
    });

    return () => {
      unsubscribeAuth();
      if (unsubscribeSnapshot) {
        unsubscribeSnapshot();
      }
    };
  }, []);

  const value = {
    currentUser,
    userData,
    loading
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
