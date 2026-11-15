import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import App from './App';

const regularData = {
  sections: [{ sectionName: 'Public Section', sectionButtons: [{ buttonName: 'Hello', pasteValue: 'hi' }] }],
  favorites: []
};
const lihimData = {
  sections: [{ sectionName: 'Hidden Section', sectionButtons: [{ buttonName: 'Secret', pasteValue: 'shh' }] }],
  favorites: [{ buttonName: 'Secret', pasteValue: 'shh', sectionId: 0, buttonId: 0 }]
};

const json = (body, status = 200) => Promise.resolve(new Response(JSON.stringify(body), { status }));

function mockServer(user) {
  const calls = [];
  global.fetch = vi.fn((url, opts = {}) => {
    calls.push({ url, method: opts.method || 'GET', body: opts.body });
    if (url === '/api/auth/me') return json({ user });
    if (url === '/api/data') return json(regularData);
    if (url === '/api/lihim/data') {
      if (!user.isAdmin) return Promise.resolve(new Response('404 page not found', { status: 404 }));
      return opts.method === 'PUT' ? json({ success: true }) : json(lihimData);
    }
    return json({});
  });
  return calls;
}

const tapTitle = (n) => {
  const title = screen.getByTestId('brand-title');
  for (let i = 0; i < n; i++) fireEvent.click(title);
};

describe('Lihim view', () => {
  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem('cpt_token', 'tok');
  });
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('opens only lihim content after 5 title taps by an admin, and exits back', async () => {
    const user = { id: 'u1', username: 'boss', isAdmin: true };
    localStorage.setItem('cpt_user', JSON.stringify(user));
    mockServer(user);
    render(<App />);
    await screen.findByRole('heading', { name: 'Public Section' });

    tapTitle(4);
    expect(screen.queryByText('Hidden Section')).not.toBeInTheDocument();
    tapTitle(1);

    await screen.findByRole('heading', { name: 'Hidden Section' });
    expect(screen.queryByText('Public Section')).not.toBeInTheDocument();
    expect(screen.getByTestId('brand-title')).toHaveTextContent('Lihim');

    // Lihim content never lands in localStorage.
    expect(JSON.stringify({ ...localStorage })).not.toContain('Hidden Section');

    fireEvent.click(screen.getByLabelText('Exit Lihim'));
    await screen.findByRole('heading', { name: 'Public Section' });
    expect(screen.queryByText('Hidden Section')).not.toBeInTheDocument();
  });

  it('saves lihim edits to the lihim endpoint only', async () => {
    const user = { id: 'u1', username: 'boss', isAdmin: true };
    localStorage.setItem('cpt_user', JSON.stringify(user));
    const calls = mockServer(user);
    render(<App />);
    await screen.findByRole('heading', { name: 'Public Section' });
    tapTitle(5);
    await screen.findByRole('heading', { name: 'Hidden Section' });

    const before = calls.length;
    fireEvent.click(screen.getByText('Create Section'));
    fireEvent.change(screen.getByLabelText('Section Name'), { target: { value: 'Second Hidden' } });
    fireEvent.click(screen.getByText('Submit'));

    await waitFor(() => {
      const puts = calls.slice(before).filter((c) => c.method === 'PUT');
      expect(puts).toHaveLength(1);
      expect(puts[0].url).toBe('/api/lihim/data');
      expect(puts[0].body).toContain('Second Hidden');
    });
    expect(JSON.stringify({ ...localStorage })).not.toContain('Second Hidden');
  });

  it('does nothing for non-admin users', async () => {
    const user = { id: 'u2', username: 'regular' };
    localStorage.setItem('cpt_user', JSON.stringify(user));
    const calls = mockServer(user);
    render(<App />);
    await screen.findByRole('heading', { name: 'Public Section' });
    tapTitle(6);
    await new Promise((r) => setTimeout(r, 20));
    expect(calls.some((c) => c.url === '/api/lihim/data')).toBe(false);
    expect(screen.getByTestId('brand-title')).toHaveTextContent('Chat Agent Tool');
  });
});
