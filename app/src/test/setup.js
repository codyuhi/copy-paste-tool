import '@testing-library/jest-dom';
import { vi } from 'vitest';

// Explicit mock for localStorage to prevent issues in Node environments
const localStorageMock = (() => {
  let store = {};
  return {
    getItem: (key) => store[key] || null,
    setItem: (key, value) => {
      store[key] = String(value);
    },
    removeItem: (key) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
    key: (index) => Object.keys(store)[index] || null,
    get length() {
      return Object.keys(store).length;
    }
  };
})();

Object.defineProperty(globalThis, 'localStorage', {
  value: localStorageMock,
  writable: true
});

// Mock navigator.clipboard API
Object.defineProperty(navigator, 'clipboard', {
  value: {
    writeText: vi.fn().mockImplementation(() => Promise.resolve()),
  },
  writable: true,
});

// Mock window.confirm
window.confirm = vi.fn().mockImplementation(() => true);

// Mock window.alert
window.alert = vi.fn();
