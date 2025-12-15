import React, { useState } from "react";
import { createUserWithEmailAndPassword } from "firebase/auth";
import { auth, db } from "../firebase";
import { doc, getDoc, setDoc } from "firebase/firestore";
import { Link, useNavigate } from "react-router-dom";

const Register: React.FC = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [username, setUsername] = useState("");
  const [error, setError] = useState("");
  const [usernameError, setUsernameError] = useState("");
  const [passwordVisible, setPasswordVisible] = useState(false);
  const navigate = useNavigate();

  const handleUsernameChange = async (val: string) => {
    const capsVal = val.toUpperCase();
    setUsername(capsVal);

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

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }
    if (usernameError) {
      setError("Please fix username errors");
      return;
    }

    setError("");
    try {
      // 1. Create Auth
      const userCredential = await createUserWithEmailAndPassword(auth, email, password);
      const user = userCredential.user;

      // 2. Reserve Username & Create Profile
      await setDoc(doc(db, "usernames", username), { uid: user.uid });
      await setDoc(doc(db, "users", user.uid), {
        username: username,
        email: email,
        highScore: 0,
        createdAt: new Date().toISOString()
      });

      navigate("/dashboard");
    } catch (err: any) {
      setError(err.message);
    }
  };

  return (
    <div className="login-container glass-panel">

      <div className="auth-header">
        <h1>NEW COMMISSION</h1>
        <p>Register your service record</p>
      </div>

      <form onSubmit={handleRegister}>
        <div className="form-group">
          <label>Handle (Unique)</label>
          <input
            className="input-field"
            type="text"
            value={username}
            onChange={(e) => handleUsernameChange(e.target.value)}
            placeholder="COMMANDER"
            maxLength={15}
            required
          />
          {usernameError && <div className="error-msg" style={{ color: usernameError === 'Checking...' ? 'var(--primary)' : 'var(--danger)' }}>{usernameError}</div>}
        </div>

        <div className="form-group">
          <label>Communication ID (Email)</label>
          <input
            className="input-field"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="pilot@fleet.com"
            required
          />
        </div>

        <div className="form-group">
          <label>Security Clearance (Password)</label>
          <div className="password-container">
            <input
              className="input-field"
              type={passwordVisible ? "text" : "password"}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <div className="password-toggle" onClick={() => setPasswordVisible(!passwordVisible)}>
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                {passwordVisible ? (
                  <path d="M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 3.12 0 5.91-1.23 7.97-3.21l1.15 1.15 1.71-1.71L3.71 2.56 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z" />
                ) : (
                  <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z" />
                )}
              </svg>
            </div>
          </div>
        </div>

        <div className="form-group">
          <label>Confirm Clearance</label>
          <input
            className="input-field"
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
          />
        </div>

        {error && <div className="error-msg">{error}</div>}

        <button type="submit" className="btn btn-primary" style={{ marginTop: '10px' }}>COMPLETE REGISTRATION</button>
      </form>

      <div className="auth-footer">
        Already enlisted? <Link to="/">Access Portal</Link>
      </div>
    </div>
  );
};

export default Register;
