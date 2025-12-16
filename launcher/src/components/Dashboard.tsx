import React, { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { auth, db } from "../firebase";
import { signOut, sendPasswordResetEmail, deleteUser } from "firebase/auth";
import { doc, deleteDoc, getDoc, setDoc } from "firebase/firestore";

type Tab = 'home' | 'account';

const Dashboard: React.FC = () => {
  const { userData, currentUser } = useAuth();
  const [activeTab, setActiveTab] = useState<Tab>('home');
  const [selectedNote, setSelectedNote] = useState<boolean>(false);

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

  const handleDeleteAccount = async () => {
    if (!currentUser) return;
    const confirmDelete = window.confirm(
      "WARNING: This will permanently delete your account and all progress. Are you sure?"
    );

    if (confirmDelete) {
      try {
        const uid = currentUser.uid;
        // Optional: Delete user data from Firestore
        if (userData?.username) {
          try { await deleteDoc(doc(db, "usernames", userData.username)); } catch (e) { }
          try { await deleteDoc(doc(db, "users", uid)); } catch (e) { }
        }

        await deleteUser(currentUser);
        // Auth state change will redirect to login
      } catch (error: any) {
        alert("Error deleting account (You may need to re-login first if it's been a while): " + error.message);
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
              <div className="game-subtitle">Mission Control v1.0</div>

              <div className="launch-btn-container">
                <button onClick={handleLaunch} className="btn-launch">
                  LAUNCH
                </button>
              </div>
            </div>

            <div className="side-panel">
              <div className="panel-header">TRANSMISSIONS</div>
              <ul className="notes-list">
                <li className="note-item" onClick={() => setSelectedNote(true)} style={{ cursor: 'pointer' }}>
                  <span className="note-date">2025-12-15</span>
                  <span className="note-title" style={{ color: 'var(--primary)', textDecoration: 'underline' }}>Initial Release v1.0</span>
                </li>
              </ul>
            </div>

            {/* MODAL */}
            {selectedNote && (
              <div className="modal-overlay" onClick={() => setSelectedNote(false)}>
                <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                  <div className="modal-header">
                    <h2>MISSION BRIEFING: v1.0</h2>
                    <button className="close-modal-btn" onClick={() => setSelectedNote(false)}>×</button>
                  </div>
                  <div className="modal-body">
                    <h3>Dark Zone Warning</h3>
                    <p>Be advised: Long-range sensors will fail in Dark Waves. Visibility will be reduced to a narrow cone. Rely on visual confirmation for hostility engagement.</p>

                    <h3>Flight Mechanics</h3>
                    <p>The new engine core consumes <strong>ENERGY</strong> for high-g maneuvering and weapons fire. Monitor your reserves; a depleted ship is a dead ship.</p>

                    <h3>Hostile Intelligence</h3>
                    <p>Scanner data indicates new alien behavioral patterns:</p>
                    <ul>
                      <li>Erratic evasion maneuvers detected</li>
                      <li>Swarm tactics observed in higher waves</li>
                      <li>Increased aggression from Hunter-class entities</li>
                    </ul>

                    <h3>System Status</h3>
                    <ul>
                      <li>Global Leaderboards: <strong>ONLINE</strong></li>
                      <li>Secure Account Database: <strong>ACTIVE</strong></li>
                      <li>Web-GL Rendering Core: <strong>OPTIMIZED</strong></li>
                    </ul>
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

            <div className="account-grid">
              <div className="info-card">
                <span className="info-label">Handle</span>
                <p className="info-value">{userData?.username || "UNKNOWN"}</p>
              </div>

              <div className="info-card">
                <span className="info-label">Identifier</span>
                <p className="info-value">{userData?.email || "..."}</p>
              </div>

              <div className="info-card">
                <span className="info-label">Service Record</span>
                <p className="info-value">Active</p>
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
