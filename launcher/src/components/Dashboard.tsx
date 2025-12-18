import React, { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { auth, db } from "../firebase";
import { signOut, sendPasswordResetEmail, sendEmailVerification } from "firebase/auth";
import { doc, getDoc, setDoc } from "firebase/firestore";
import { getFunctions, httpsCallable } from "firebase/functions";
import v1Data from "../data/v1.json";

interface ReleaseNoteSection {
  title?: string;
  content?: string;
  list?: string[];
}

interface ReleaseNote {
  version: string;
  date: string;
  title: string;
  sections: ReleaseNoteSection[];
}

type Tab = 'home' | 'account';

const Dashboard: React.FC = () => {
  const { userData, currentUser } = useAuth();
  const [activeTab, setActiveTab] = useState<Tab>('home');
  const [selectedNote, setSelectedNote] = useState<ReleaseNote | null>(null);

  // Load release notes from the current version JSON file
  const visibleNotes: ReleaseNote[] = v1Data;
  const [isResending, setIsResending] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const handleLaunch = () => {
    // Redirect to game
    window.location.href = "/game/index.html";
  };

  const handleLogout = () => {
    signOut(auth);
  };

  const handleResetPassword = async () => {
    if (!userData?.email) return;
    try {
      await sendPasswordResetEmail(auth, userData.email);
      alert(`Password reset email sent to ${userData.email}`);
    } catch (error: any) {
      alert("Error: " + error.message);
    }
  };

  const handleResendVerification = async () => {
    if (!currentUser) return;
    setIsResending(true);
    try {
      await sendEmailVerification(currentUser);
      alert(`Verification email sent to ${currentUser.email}. Please check your inbox.`);
    } catch (error: any) {
      alert("Error sending email: " + error.message);
    } finally {
      setIsResending(false);
    }
  };

  const handleDeleteAccount = async () => {
    if (!currentUser) return;
    const confirmDelete = window.confirm(
      "WARNING: This will PERMANENTLY delete your account, high scores, and username reservation. This action cannot be undone. Are you sure?"
    );

    if (confirmDelete) {
      setIsDeleting(true);
      try {
        const functions = getFunctions();
        const deleteAccountFunction = httpsCallable(functions, 'deleteAccount');

        await deleteAccountFunction();

        // After successful deletion, Firebase Auth might automatically detect it, 
        // but explicit sign out ensures client state is cleared.
        await signOut(auth);

      } catch (error: any) {
        console.error(error);
        alert("Error deleting account: " + error.message);
        setIsDeleting(false);
      }
    }
  };

  /* -------------------------------------------------------------
     COMPLETE PROFILE LOGIC (For Google Sign-ins without Username)
     ------------------------------------------------------------- */
  const [newUsername, setNewUsername] = useState("");
  const [usernameError, setUsernameError] = useState("");
  const [setupError, setSetupError] = useState("");

  const handleUsernameChange = async (val: string) => {
    const capsVal = val.toUpperCase();
    setNewUsername(capsVal);

    if (capsVal.length < 3) {
      setUsernameError("Min 3 chars");
      return;
    }
    if (!/^[A-Z0-9_]+$/.test(capsVal)) {
      setUsernameError("Alphanumeric only");
      return;
    }

    // Check availability
    setUsernameError("Checking...");
    try {
      const docRef = doc(db, "usernames", capsVal);
      const docSnap = await getDoc(docRef);
      if (docSnap.exists()) {
        setUsernameError("Username taken!");
      } else {
        setUsernameError(""); // Available
      }
    } catch {
      setUsernameError("Error checking username");
    }
  };

  const completeSetup = async (e: React.FormEvent) => {
    e.preventDefault();
    if (usernameError || !newUsername) {
      setSetupError("Please define a valid username");
      return;
    }
    if (!currentUser) return;

    try {
      // 1. Reserve Username & Create/Update Profile
      await setDoc(doc(db, "usernames", newUsername), { uid: currentUser.uid });

      // Merge with existing data (e.g. email) but ensure username is set
      await setDoc(doc(db, "users", currentUser.uid), {
        username: newUsername,
        email: currentUser.email,
        uid: currentUser.uid,
        createdAt: new Date().toISOString()
      }, { merge: true });

    } catch (err: any) {
      setSetupError("Failed to update profile: " + err.message);
    }
  };

  // IF NO USERNAME, SHOW SETUP SCREEN
  if (currentUser && (!userData || !userData.username)) {
    // Prevent flash during deletion
    if (isDeleting) {
      return (
        <div className="login-container glass-panel" style={{ maxWidth: '400px', margin: '100px auto', textAlign: 'center' }}>
          <h2>CLOSING COMMISSION...</h2>
        </div>
      );
    }

    // Prevent flash during email registration (waiting for firestore write)
    // If user has 'password' provider, they must have registered via form which sets username.
    // If creation was < 10 seconds ago, we are likely just waiting for the doc to sync.
    const isPasswordUser = currentUser.providerData.some(p => p.providerId === 'password');
    const isNew = currentUser.metadata.creationTime && (new Date().getTime() - new Date(currentUser.metadata.creationTime).getTime() < 10000);

    if (isPasswordUser && isNew) {
      return (
        <div className="login-container glass-panel" style={{ maxWidth: '400px', margin: '100px auto', textAlign: 'center' }}>
          <h2>INITIALIZING SERVICE RECORD...</h2>
        </div>
      );
    }

    return (
      <div className="login-container glass-panel" style={{ maxWidth: '400px', margin: '100px auto' }}>
        <div className="auth-header">
          <h1>IDENTIFICATION</h1>
          <p>Set your callsign, Commander</p>
        </div>

        <form onSubmit={completeSetup}>
          <div className="form-group">
            <label>Handle (Unique)</label>
            <input
              className="input-field"
              type="text"
              value={newUsername}
              onChange={(e) => handleUsernameChange(e.target.value)}
              placeholder="COMMANDER"
              maxLength={15}
              required
              autoFocus
            />
            {usernameError && <div className="error-msg" style={{ color: usernameError === 'Checking...' ? 'var(--primary)' : 'var(--danger)' }}>{usernameError}</div>}
          </div>

          {setupError && <div className="error-msg">{setupError}</div>}

          <button type="submit" className="btn btn-primary" style={{ marginTop: '10px' }}>
            CONFIRM IDENTITY
          </button>

          <div className="auth-footer" style={{ marginTop: '20px' }}>
            <span onClick={handleLogout} style={{ cursor: 'pointer', color: 'var(--danger)', fontSize: '0.9em' }}>Cancel / Logout</span>
          </div>
        </form>
      </div>
    );
  }

  /* -------------------------------------------------------------
     MAIN DASHBOARD UI
     ------------------------------------------------------------- */
  return (
    <div className="dashboard-layout">
      {/* SIDEBAR NAVIGATION */}
      <div className="sidebar">
        <div className="sidebar-header">
          <img src="/img/spaceship.png" alt="App Logo" style={{ width: '60px', marginBottom: '10px' }} />
        </div>

        <div className="sidebar-menu">
          <div
            className={`nav-item ${activeTab === 'home' ? 'active' : ''}`}
            onClick={() => setActiveTab('home')}
          >
            <svg viewBox="0 0 24 24" width="18" height="18"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z" /></svg>
            LAUNCHER
          </div>

          <div
            className={`nav-item ${activeTab === 'account' ? 'active' : ''}`}
            onClick={() => setActiveTab('account')}
          >
            <svg viewBox="0 0 24 24" width="18" height="18"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z" /></svg>
            ACCOUNT
          </div>

          <div className="nav-item nav-item-bottom" onClick={handleLogout}>
            <svg viewBox="0 0 24 24" width="18" height="18"><path d="M10.09 15.59L11.5 17l5-5-5-5-1.41 1.41L12.67 11H3v2h9.67l-2.58 2.59zM19 3H5c-1.11 0-2 .9-2 2v4h2V5h14v14H5v-4H3v4c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z" /></svg>
            LOGOUT
          </div>
        </div>
      </div>

      {/* CONTENT AREA */}
      <div className="content-area">

        {/* HOME VIEW */}
        {activeTab === 'home' && (
          <div className="view-home">
            <div className="main-stage">
              <h1 className="game-title-large">SPACE<br />GAME</h1>
              <div className="game-subtitle">v1.2</div>

              <div className="launch-btn-container">
                <button onClick={handleLaunch} className="btn-launch">
                  LAUNCH
                </button>
              </div>
            </div>

            <div className="side-panel">
              <div className="panel-header">TRANSMISSIONS</div>
              <ul className="notes-list">
                {visibleNotes.map((note) => (
                  <li
                    key={note.version}
                    className="note-item"
                    onClick={() => setSelectedNote(note)}
                    style={{ cursor: 'pointer' }}
                  >
                    <span className="note-date">{note.date}</span>
                    <span className="note-title" style={{ color: 'var(--primary)', textDecoration: 'underline' }}>
                      {note.title}
                    </span>
                  </li>
                ))}
              </ul>
            </div>

            {/* MODAL */}
            {selectedNote && (
              <div className="modal-overlay" onClick={() => setSelectedNote(null)}>
                <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                  <div className="modal-header">
                    <h2>MISSION BRIEFING: v{selectedNote.version}</h2>
                    <button className="close-modal-btn" onClick={() => setSelectedNote(null)}>×</button>
                  </div>
                  <div className="modal-body">
                    {selectedNote.sections.map((section, idx) => (
                      <div key={idx} style={{ marginBottom: '15px' }}>
                        {section.title && <h3>{section.title}</h3>}

                        {section.content && (
                          <p dangerouslySetInnerHTML={{ __html: section.content }} />
                        )}

                        {section.list && (
                          <ul>
                            {section.list.map((item, i) => (
                              <li key={i} dangerouslySetInnerHTML={{ __html: item }} />
                            ))}
                          </ul>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* ACCOUNT VIEW */}
        {activeTab === 'account' && (
          <div className="view-account">
            <h2 className="section-title">COMMANDER PROFILE</h2>

            {!currentUser?.emailVerified && (
              <div className="warning-banner" style={{
                background: 'rgba(234, 67, 53, 0.2)',
                border: '1px solid #ea4335',
                padding: '15px',
                borderRadius: '8px',
                marginBottom: '20px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
              }}>
                <div style={{ display: 'flex', alignItems: 'center' }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="var(--danger)" style={{ marginRight: '10px' }}>
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
                  </svg>
                  <span>
                    <strong>Unverified Communication Link.</strong><br />
                    Please verify your email to secure your account.
                  </span>
                </div>
                <button
                  className="btn btn-secondary"
                  onClick={handleResendVerification}
                  disabled={isResending}
                  style={{ padding: '8px 15px', fontSize: '0.8em' }}
                >
                  {isResending ? "SENDING..." : "RESEND EMAIL"}
                </button>
              </div>
            )}

            <div className="account-grid">
              <div className="info-card">
                <span className="info-label">Handle</span>
                <p className="info-value">{userData?.username || "UNKNOWN"}</p>
              </div>

              <div className="info-card">
                <span className="info-label">Identifier</span>
                <p className="info-value">{userData?.email || "..."}</p>
              </div>
            </div>

            <div className="account-actions">
              <button className="btn btn-secondary" onClick={handleResetPassword}>RESET PASSWORD</button>
              <button className="btn btn-danger-outline" onClick={handleDeleteAccount}>DELETE ACCOUNT</button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
