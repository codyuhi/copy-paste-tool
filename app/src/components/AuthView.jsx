import React, { useState } from 'react';
import { apiLogin, apiRegister } from '../utils/api';
import ThemeToggle from './ThemeToggle';

const AuthView = ({
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
    <div className="auth-gate-page">
      <header className="app-header">
        <div className="header-left">
          <span className="brand-title">Chat Agent Tool</span>
        </div>
        <div className="header-right">
          <span className="brand-version">Version 2.0.1</span>
          <ThemeToggle />
        </div>
      </header>

      <main className="auth-gate-main">
        <div className="auth-gate-card">
          <div className="auth-gate-header">
            <div className="auth-gate-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <rect width="18" height="11" x="3" y="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
            </div>
            <h2 className="auth-gate-title">
              {tab === 'login' ? 'Sign In to Your Account' : 'Create an Account'}
            </h2>
            <p className="auth-gate-subtitle">
              {tab === 'login'
                ? 'Sign in to access your synchronized snippets across all devices.'
                : 'Create an account to store and access your snippets from anywhere.'}
            </p>
          </div>

          {/* Tab Navigation */}
          <div className="auth-gate-tabs">
            <button
              type="button"
              className={`auth-gate-tab ${tab === 'login' ? 'active' : ''}`}
              onClick={() => switchTab('login')}
            >
              Sign In
            </button>
            <button
              type="button"
              className={`auth-gate-tab ${tab === 'register' ? 'active' : ''}`}
              onClick={() => switchTab('register')}
            >
              Create Account
            </button>
          </div>

          {error && (
            <div className="auth-gate-error" role="alert">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="auth-gate-form">
            <div className="form-group">
              <label className="form-label" htmlFor="gate-username">Username</label>
              <input
                id="gate-username"
                className="form-input"
                type="text"
                placeholder="Enter your username..."
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoCapitalize="none"
                autoCorrect="off"
                required
                autoFocus
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="gate-password">Password</label>
              <input
                id="gate-password"
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
                <label className="form-label" htmlFor="gate-confirm-password">Confirm Password</label>
                <input
                  id="gate-confirm-password"
                  className="form-input"
                  type="password"
                  placeholder="Re-enter your password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                />
              </div>
            )}

            {tab === 'register' && localSections.length > 0 && (
              <label className="auth-gate-migrate-opt">
                <input
                  type="checkbox"
                  checked={includeLocalData}
                  onChange={(e) => setIncludeLocalData(e.target.checked)}
                />
                <span>Save existing {localSections.length} local section(s) to new account</span>
              </label>
            )}

            <button
              type="submit"
              className="btn btn-primary auth-gate-submit"
              disabled={loading}
            >
              {loading ? (
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}>
                  <svg className="spin" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67"></path>
                  </svg>
                  Please wait...
                </span>
              ) : (
                tab === 'login' ? 'Sign In' : 'Create Account'
              )}
            </button>
          </form>
        </div>
      </main>
    </div>
  );
};

export default AuthView;
