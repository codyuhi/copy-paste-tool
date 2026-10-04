import React, { useState } from 'react';
import { apiLogin, apiRegister } from '../utils/api';

const AuthModal = ({
  isOpen,
  onClose,
  onSuccess,
  localSections = [],
  localFavorites = [],
  showToast
}) => {
  const [tab, setTab] = useState('login'); // 'login' | 'register'
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [includeLocalData, setIncludeLocalData] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const trimmedUser = username.trim();
    if (!trimmedUser) {
      setError('Please enter a username');
      return;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    if (tab === 'register' && password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    setLoading(true);
    try {
      if (tab === 'login') {
        const res = await apiLogin({ username: trimmedUser, password });
        showToast?.(`Welcome back, ${res.user.username}!`);
        onSuccess(res.user);
        onClose();
      } else {
        const initialSections = includeLocalData ? localSections : [];
        const initialFavorites = includeLocalData ? localFavorites : [];
        const res = await apiRegister({
          username: trimmedUser,
          password,
          initialSections,
          initialFavorites
        });
        showToast?.(`Account created! Welcome, ${res.user.username}`);
        onSuccess(res.user, { initialSections, initialFavorites });
        onClose();
      }
    } catch (err) {
      setError(err.message || 'Authentication failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const switchTab = (newTab) => {
    setTab(newTab);
    setError('');
  };

  return (
    <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', zIndex: 120 }}>
      {/* Backdrop */}
      <div
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          backgroundColor: 'var(--overlay)',
          backdropFilter: 'blur(8px)',
          WebkitBackdropFilter: 'blur(8px)'
        }}
        onClick={onClose}
      />

      {/* Modal Dialog */}
      <dialog
        open
        style={{
          position: 'relative',
          display: 'flex',
          flexDirection: 'column',
          maxWidth: '440px',
          width: '90%',
          margin: 'auto',
          backgroundColor: 'var(--bg-secondary)',
          border: '1px solid var(--border-color)',
          borderRadius: '16px',
          boxShadow: 'var(--shadow-elevated)',
          padding: '24px',
          zIndex: 121,
          top: '50%',
          transform: 'translateY(-50%)'
        }}
      >
        <div className="modal-header" style={{ marginBottom: '16px' }}>
          <h3 className="modal-title" style={{ fontFamily: 'var(--font-title)', fontSize: '20px' }}>
            {tab === 'login' ? 'Sign In to Your Account' : 'Create an Account'}
          </h3>
          <button className="modal-close" onClick={onClose} aria-label="Close modal">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        {/* Tab switchers */}
        <div
          style={{
            display: 'flex',
            backgroundColor: 'var(--bg-tertiary)',
            padding: '4px',
            borderRadius: '10px',
            marginBottom: '20px',
            gap: '4px'
          }}
        >
          <button
            type="button"
            className="auth-tab-btn"
            style={{
              flex: 1,
              padding: '8px 12px',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer',
              fontWeight: 600,
              fontSize: '14px',
              backgroundColor: tab === 'login' ? 'var(--bg-field)' : 'transparent',
              color: tab === 'login' ? 'var(--text-emphasis)' : 'var(--text-secondary)',
              boxShadow: tab === 'login' ? '0 1px 3px rgba(0,0,0,0.1)' : 'none',
              transition: 'all 0.15s ease'
            }}
            onClick={() => switchTab('login')}
          >
            Sign In
          </button>
          <button
            type="button"
            className="auth-tab-btn"
            style={{
              flex: 1,
              padding: '8px 12px',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer',
              fontWeight: 600,
              fontSize: '14px',
              backgroundColor: tab === 'register' ? 'var(--bg-field)' : 'transparent',
              color: tab === 'register' ? 'var(--text-emphasis)' : 'var(--text-secondary)',
              boxShadow: tab === 'register' ? '0 1px 3px rgba(0,0,0,0.1)' : 'none',
              transition: 'all 0.15s ease'
            }}
            onClick={() => switchTab('register')}
          >
            Create Account
          </button>
        </div>

        <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '16px', lineHeight: 1.4 }}>
          {tab === 'login'
            ? 'Sign in to access and sync your copy-paste snippets across all your devices.'
            : 'Register a personal account to store your snippets persistently and access them anywhere.'}
        </p>

        {error && (
          <div
            style={{
              padding: '10px 14px',
              backgroundColor: 'rgba(199, 55, 15, 0.12)',
              border: '1px solid var(--danger-color)',
              color: 'var(--danger-color)',
              borderRadius: '8px',
              fontSize: '13px',
              marginBottom: '16px'
            }}
          >
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div className="form-group">
            <label className="form-label" htmlFor="auth-username">Username</label>
            <input
              id="auth-username"
              className="form-input"
              type="text"
              placeholder="e.g. cody"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoCapitalize="none"
              autoCorrect="off"
              required
              autoFocus
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="auth-password">Password</label>
            <input
              id="auth-password"
              className="form-input"
              type="password"
              placeholder="At least 6 characters"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          {tab === 'register' && (
            <div className="form-group">
              <label className="form-label" htmlFor="auth-confirm-password">Confirm Password</label>
              <input
                id="auth-confirm-password"
                className="form-input"
                type="password"
                placeholder="Re-enter password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
            </div>
          )}

          {tab === 'register' && localSections.length > 0 && (
            <label
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                fontSize: '13px',
                color: 'var(--text-secondary)',
                cursor: 'pointer',
                marginTop: '4px'
              }}
            >
              <input
                type="checkbox"
                checked={includeLocalData}
                onChange={(e) => setIncludeLocalData(e.target.checked)}
                style={{ width: '16px', height: '16px', accentColor: 'var(--accent-color)' }}
              />
              Save existing {localSections.length} local section(s) to new account
            </label>
          )}

          <div className="modal-actions" style={{ marginTop: '8px' }}>
            <button
              type="button"
              className="btn btn-cancel"
              onClick={onClose}
              disabled={loading}
            >
              Continue as Guest
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
              style={{ minWidth: '120px' }}
            >
              {loading ? 'Please wait...' : tab === 'login' ? 'Sign In' : 'Create Account'}
            </button>
          </div>
        </form>
      </dialog>
    </div>
  );
};

export default AuthModal;
