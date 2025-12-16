import React from 'react';

const MobileWarning: React.FC = () => {
  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      width: '100vw',
      height: '100vh',
      backgroundColor: 'var(--bg-dark)', // Solid background to cover everything
      backgroundImage: 'radial-gradient(ellipse at bottom, #1b2735 0%, #090a0f 100%)',
      zIndex: 9999,
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'center',
      alignItems: 'center',
      padding: '40px',
      textAlign: 'center',
      boxSizing: 'border-box'
    }}>
      <div style={{
        padding: '40px',
        border: '1px solid rgba(0, 240, 255, 0.2)',
        borderRadius: '16px',
        background: 'rgba(21, 23, 37, 0.8)',
        backdropFilter: 'blur(10px)',
        boxShadow: '0 0 40px rgba(0, 0, 0, 0.5)',
        maxWidth: '500px'
      }}>
        <h1 style={{
          fontFamily: 'var(--font-display)',
          color: 'var(--danger)',
          fontSize: '24px',
          marginBottom: '20px',
          textTransform: 'uppercase',
          letterSpacing: '2px',
          textShadow: '0 0 10px rgba(255, 42, 42, 0.4)'
        }}>
          Device Not Supported
        </h1>
        <p style={{
          color: 'var(--text-main)',
          fontSize: '16px',
          lineHeight: '1.6',
          marginBottom: '30px',
          opacity: 0.9
        }}>
          The Space Game is designed for PC and requires a specific screen resolution to function correctly.
        </p>
        <p style={{
          color: 'var(--text-muted)',
          fontSize: '14px'
        }}>
          Please access this URL from a desktop or laptop computer.
        </p>
      </div>
    </div>
  );
};

export default MobileWarning;
