import React, { useState } from "react";
import { signInWithEmailAndPassword, GoogleAuthProvider, signInWithPopup, getAdditionalUserInfo } from "firebase/auth";
import { auth, db } from "../firebase";
import { doc, getDoc, setDoc } from "firebase/firestore";
import { Link, useNavigate } from "react-router-dom";

const Login: React.FC = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  // Google Signup State
  const [isGoogleSignup, setIsGoogleSignup] = useState(false);
  const [username, setUsername] = useState("");
  const [usernameError, setUsernameError] = useState("");
  const [googleUser, setGoogleUser] = useState<any>(null);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    try {
      await signInWithEmailAndPassword(auth, email, password);
      navigate("/dashboard");
    } catch (err: any) {
      setError("Invalid credentials. Please retry.");
      console.error(err);
    }
  };

  const handleGoogleLogin = async () => {
    try {
      const provider = new GoogleAuthProvider();
      const result = await signInWithPopup(auth, provider);
      const user = result.user;

      const additionalInfo = getAdditionalUserInfo(result);
      // Validar se é user novo. Se não for, assume que já tem cadastro e redireciona.
      // Isso evita o "flash" da tela de username para usuários antigos.
      if (!additionalInfo?.isNewUser) {
        navigate("/dashboard");
        return;
      }

      // Se for novo (ou se isNewUser falhar/null), verifica o documento apenas para garantir.
      const userDocRef = doc(db, "users", user.uid);
      const userDocSnap = await getDoc(userDocRef);

      if (userDocSnap.exists() && userDocSnap.data()?.username) {
        navigate("/dashboard");
      } else {
        // User authenticated but no profile (or incomplete) -> prompt for username
        setGoogleUser(user);
        setIsGoogleSignup(true);
      }
    } catch (err: any) {
      setError("Google authentication failed.");
      console.error(err);
    }
  };

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

  const completeGoogleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    if (usernameError || !username) {
      setError("Please define a valid username");
      return;
    }

    try {
      // 1. Reserve Username & Create Profile
      await setDoc(doc(db, "usernames", username), { uid: googleUser.uid });
      await setDoc(doc(db, "users", googleUser.uid), {
        username: username,
        email: googleUser.email,
        uid: googleUser.uid,
        createdAt: new Date().toISOString()
      });

      navigate("/dashboard");
    } catch (err: any) {
      setError("Failed to create profile: " + err.message);
    }
  };

  if (isGoogleSignup) {
    return (
      <div className="login-container glass-panel">
        <div className="auth-header">
          <h1>IDENTIFICATION</h1>
          <p>Set your callsign, Commander</p>
        </div>

        <form onSubmit={completeGoogleSignup}>
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
              autoFocus
            />
            {usernameError && <div className="error-msg" style={{ color: usernameError === 'Checking...' ? 'var(--primary)' : 'var(--danger)' }}>{usernameError}</div>}
          </div>

          {error && <div className="error-msg">{error}</div>}

          <button type="submit" className="btn btn-primary" style={{ marginTop: '10px' }}>
            CONFIRM IDENTITY
          </button>
        </form>
      </div>
    );
  }

  return (
    <div className="login-container glass-panel">
      <div className="auth-header">
        <h1>ACCESS PORTAL</h1>
        <p>Identify yourself, Commander</p>
      </div>

      <form onSubmit={handleLogin} id="login-view">
        <div className="form-group">
          <label>Identifier (Email)</label>
          <input
            className="input-field"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="commander@fleet.com"
            required
          />
        </div>

        <div className="form-group">
          <label>Passcode</label>
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

        {error && <div className="error-msg">{error}</div>}

        <button type="submit" className="btn btn-primary">INITIALIZE LINK</button>
      </form>

      <div className="divider">OR USE FEDERATION ID</div>

      <button className="btn btn-google" onClick={handleGoogleLogin}>
        <svg style={{ marginRight: '10px' }} xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 48 48">
          <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z" />
          <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z" />
          <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z" />
          <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z" />
          <path fill="none" d="M0 0h48v48H0z" />
        </svg>
        Continue with Google
      </button>

      <div className="auth-footer">
        New recruit? <Link to="/register">Enlist Now</Link>
      </div>
    </div>
  );
};

export default Login;

