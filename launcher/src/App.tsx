import { useState, useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import MobileWarning from './components/MobileWarning';
import { useAuth } from './context/AuthContext';

function App() {
  const { currentUser } = useAuth();
  const [isMobile, setIsMobile] = useState(window.innerWidth < 1024);

  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth < 1024);
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  if (isMobile) {
    return <MobileWarning />;
  }

  return (
    <div>
      <Routes>
        <Route path="/" element={!currentUser ? <Login /> : <Navigate to="/dashboard" />} />
        <Route path="/register" element={!currentUser ? <Register /> : <Navigate to="/dashboard" />} />
        <Route path="/dashboard" element={currentUser ? <Dashboard /> : <Navigate to="/" />} />
      </Routes>
    </div>
  );
}

export default App;
