// API client for authentication and persistent cloud synchronization

const API_BASE = '/api';

export const getAuthToken = () => {
  try {
    return localStorage.getItem('cpt_token');
  } catch {
    return null;
  }
};

export const setAuthToken = (token) => {
  try {
    if (token) {
      localStorage.setItem('cpt_token', token);
    } else {
      localStorage.removeItem('cpt_token');
    }
  } catch (err) {
    console.error('Failed to set auth token:', err);
  }
};

export const getStoredUser = () => {
  try {
    const raw = localStorage.getItem('cpt_user');
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
};

export const setStoredUser = (user) => {
  try {
    if (user) {
      localStorage.setItem('cpt_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('cpt_user');
    }
  } catch (err) {
    console.error('Failed to store user:', err);
  }
};

export const clearAuth = () => {
  setAuthToken(null);
  setStoredUser(null);
};

const authHeaders = () => {
  const token = getAuthToken();
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
};

export const apiRegister = async ({ username, password, initialSections = [], initialFavorites = [] }) => {
  const res = await fetch(`${API_BASE}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username,
      password,
      initialSections,
      initialFavorites
    })
  });

  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.error || 'Failed to create account');
  }

  setAuthToken(data.token);
  setStoredUser(data.user);
  return data;
};

export const apiLogin = async ({ username, password }) => {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });

  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.error || 'Invalid credentials');
  }

  setAuthToken(data.token);
  setStoredUser(data.user);
  return data;
};

export const apiLogout = async () => {
  try {
    await fetch(`${API_BASE}/auth/logout`, {
      method: 'POST',
      headers: authHeaders()
    });
  } catch {
    // Ignore network error on logout
  } finally {
    clearAuth();
  }
};

export const apiGetMe = async () => {
  const token = getAuthToken();
  if (!token) return null;

  const res = await fetch(`${API_BASE}/auth/me`, {
    headers: authHeaders()
  });

  if (!res.ok) {
    clearAuth();
    return null;
  }

  const data = await res.json();
  setStoredUser(data.user);
  return data.user;
};

export const apiFetchUserData = async () => {
  const res = await fetch(`${API_BASE}/data`, {
    headers: authHeaders()
  });

  if (!res.ok) {
    if (res.status === 401) {
      clearAuth();
    }
    throw new Error('Failed to fetch user data');
  }

  return await res.json();
};

export const apiSaveUserData = async (sections, favorites) => {
  const token = getAuthToken();
  if (!token) return null;

  const res = await fetch(`${API_BASE}/data`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify({ sections, favorites })
  });

  if (!res.ok) {
    if (res.status === 401) {
      clearAuth();
    }
    throw new Error('Failed to save data to server');
  }

  return await res.json();
};
