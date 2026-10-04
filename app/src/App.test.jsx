import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import App from './App';

describe('Chat Agent Tool - Integration Tests', () => {
  const mockUser = { id: 'user-1', username: 'cody' };

  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem('cpt_auth_token', 'mock-token');
    localStorage.setItem('cpt_user', JSON.stringify(mockUser));
    vi.clearAllMocks();
  });

  it('renders application title and empty states initially', () => {
    render(<App />);
    expect(screen.getByText('Chat Agent Tool')).toBeInTheDocument();
    expect(screen.getByText('No Sections Created Yet')).toBeInTheDocument();
    expect(screen.getByText('No Favorites Added Yet')).toBeInTheDocument();
  });

  it('creates a new section successfully', async () => {
    render(<App />);

    // Click "Create Section" button in sidebar
    const createSecBtn = screen.getByText('Create Section');
    fireEvent.click(createSecBtn);

    // Verify modal is open and fill out form
    const input = screen.getByLabelText('Section Name');
    fireEvent.change(input, { target: { value: 'Billing Support' } });
    
    // Submit the form
    const submitBtn = screen.getByText('Submit');
    fireEvent.click(submitBtn);

    // Verify section appears in main container and sidebar
    await waitFor(() => {
      // Main container heading
      expect(screen.getByRole('heading', { name: 'Billing Support' })).toBeInTheDocument();
      // Sidebar link
      expect(screen.getByRole('link', { name: 'Billing Support' })).toBeInTheDocument();
    });
  });

  it('adds a new button to a section', async () => {
    render(<App />);

    // Create a section first
    fireEvent.click(screen.getByText('Create Section'));
    fireEvent.change(screen.getByLabelText('Section Name'), { target: { value: 'Greetings' } });
    fireEvent.click(screen.getByText('Submit'));

    // Verify section exists and click "Add Button" inside section
    const addButton = screen.getByText('Add Button');
    fireEvent.click(addButton);

    // Fill in button details
    fireEvent.change(screen.getByLabelText('Button Name'), { target: { value: 'Welcome Message' } });
    fireEvent.change(screen.getByLabelText('Paste Value'), { target: { value: 'Hello! Thank you for contacting customer support.' } });
    
    // Submit button form
    fireEvent.click(screen.getByText('Submit'));

    // Verify the copy button appears in the main container grid
    await waitFor(() => {
      expect(screen.getByText('Welcome Message')).toBeInTheDocument();
    });
  });

  it('copies text and shows toast notification on button click', async () => {
    render(<App />);

    // Create section
    fireEvent.click(screen.getByText('Create Section'));
    fireEvent.change(screen.getByLabelText('Section Name'), { target: { value: 'General' } });
    fireEvent.click(screen.getByText('Submit'));

    // Create copy button
    fireEvent.click(screen.getByText('Add Button'));
    fireEvent.change(screen.getByLabelText('Button Name'), { target: { value: 'FAQ Link' } });
    fireEvent.change(screen.getByLabelText('Paste Value'), { target: { value: 'https://example.com/faq' } });
    fireEvent.click(screen.getByText('Submit'));

    // Wait for button to render and click it
    const copyBtn = await screen.findByText('FAQ Link');
    fireEvent.click(copyBtn);

    // Verify clipboard API was called with matching value and toast is shown
    await waitFor(() => {
      expect(navigator.clipboard.writeText).toHaveBeenCalledWith('https://example.com/faq');
      expect(screen.getByText('Copied "FAQ Link" text data to clipboard!')).toBeInTheDocument();
    });
  });

  it('filters buttons and sections using search input', async () => {
    render(<App />);

    // Create first section and button
    fireEvent.click(screen.getByText('Create Section'));
    fireEvent.change(screen.getByLabelText('Section Name'), { target: { value: 'Tech Help' } });
    fireEvent.click(screen.getByText('Submit'));

    fireEvent.click(screen.getByText('Add Button'));
    fireEvent.change(screen.getByLabelText('Button Name'), { target: { value: 'Reset Wifi' } });
    fireEvent.change(screen.getByLabelText('Paste Value'), { target: { value: 'Unplug router for 30s' } });
    fireEvent.click(screen.getByText('Submit'));

    // Create second section and button
    fireEvent.click(screen.getByText('Create Section'));
    fireEvent.change(screen.getByLabelText('Section Name'), { target: { value: 'Sales' } });
    fireEvent.click(screen.getByText('Submit'));

    // Need to specify the specific "Add Button" since there are multiple now
    const addBtns = screen.getAllByText('Add Button');
    fireEvent.click(addBtns[1]); // second section's add button
    fireEvent.change(screen.getByLabelText('Button Name'), { target: { value: 'Pricing Quote' } });
    fireEvent.change(screen.getByLabelText('Paste Value'), { target: { value: 'Enterprise: $99/mo' } });
    fireEvent.click(screen.getByText('Submit'));

    // Ensure both are on screen
    expect(await screen.findByText('Reset Wifi')).toBeInTheDocument();
    expect(screen.getByText('Pricing Quote')).toBeInTheDocument();

    // Type query into search bar
    const searchInput = screen.getByPlaceholderText('Search buttons or paste values...');
    fireEvent.change(searchInput, { target: { value: 'Wifi' } });

    // Verify Wifi button is present, but pricing quote button is filtered out
    expect(screen.getByText('Reset Wifi')).toBeInTheDocument();
    expect(screen.queryByText('Pricing Quote')).not.toBeInTheDocument();

    // Verify typing something that has no matches shows empty search state
    fireEvent.change(searchInput, { target: { value: 'invalid-match-query' } });
    expect(screen.queryByText('Reset Wifi')).not.toBeInTheDocument();
    expect(screen.getByText('No matching buttons found')).toBeInTheDocument();
  });

  it('favorites a button and displays it in the Favorites panel', async () => {
    render(<App />);

    // Create section
    fireEvent.click(screen.getByText('Create Section'));
    fireEvent.change(screen.getByLabelText('Section Name'), { target: { value: 'Quick Links' } });
    fireEvent.click(screen.getByText('Submit'));

    // Create copy button
    fireEvent.click(screen.getByText('Add Button'));
    fireEvent.change(screen.getByLabelText('Button Name'), { target: { value: 'Home Page' } });
    fireEvent.change(screen.getByLabelText('Paste Value'), { target: { value: 'https://home.com' } });
    fireEvent.click(screen.getByText('Submit'));

    // Trigger right click context menu on the button
    const copyBtn = await screen.findByText('Home Page');
    fireEvent.contextMenu(copyBtn);

    // Verify context menu is visible and click "Favorite"
    const favoriteOption = screen.getByText('Favorite');
    expect(favoriteOption).toBeInTheDocument();
    fireEvent.click(favoriteOption);

    // Verify the favorite item is rendered in the Favorites pane
    expect(screen.getByText('Home Page', { selector: '.fav-item-name' })).toBeInTheDocument();
  });

  it('switches mobile navigation tabs seamlessly', async () => {
    const { container } = render(<App />);

    // Bottom nav is present
    expect(screen.getByRole('navigation', { name: 'Mobile Navigation' })).toBeInTheDocument();
    const appContainer = container.querySelector('.app-container');
    expect(appContainer).toHaveClass('tab-snippets');

    // Switch to sections tab
    fireEvent.click(screen.getByRole('button', { name: 'Sections' }));
    expect(appContainer).toHaveClass('tab-sections');

    // Switch to favorites tab
    fireEvent.click(screen.getByRole('button', { name: 'Favorites' }));
    expect(appContainer).toHaveClass('tab-favorites');

    // Switch back to snippets tab
    fireEvent.click(screen.getByRole('button', { name: 'Snippets' }));
    expect(appContainer).toHaveClass('tab-snippets');
  });

  it('opens snippet options menu via touch more button and removes favorite with direct action', async () => {
    render(<App />);

    // Create section and button
    fireEvent.click(screen.getByText('Create Section'));
    fireEvent.change(screen.getByLabelText('Section Name'), { target: { value: 'Customer Service' } });
    fireEvent.click(screen.getByText('Submit'));

    fireEvent.click(screen.getByText('Add Button'));
    fireEvent.change(screen.getByLabelText('Button Name'), { target: { value: 'Refund Policy' } });
    fireEvent.change(screen.getByLabelText('Paste Value'), { target: { value: 'Refunds within 30 days.' } });
    fireEvent.click(screen.getByText('Submit'));

    // Open options via snippet more button (3-dots)
    const moreBtn = screen.getByRole('button', { name: 'Options for Refund Policy' });
    expect(moreBtn).toBeInTheDocument();
    fireEvent.click(moreBtn);

    // Favorite via context menu
    const favOption = screen.getByText('Favorite');
    fireEvent.click(favOption);

    // Verify it is in favorites pane
    expect(screen.getByText('Refund Policy', { selector: '.fav-item-name' })).toBeInTheDocument();

    // Now remove favorite directly using the touch remove button
    const removeBtn = screen.getByRole('button', { name: 'Remove Refund Policy from favorites' });
    expect(removeBtn).toBeInTheDocument();
    fireEvent.click(removeBtn);

    // Confirm it's removed and empty state shows
    await waitFor(() => {
      expect(screen.queryByText('Refund Policy', { selector: '.fav-item-name' })).not.toBeInTheDocument();
      expect(screen.getByText('No Favorites Added Yet')).toBeInTheDocument();
    });
  });

  it('renders authentication gate when unauthenticated and prevents access', () => {
    localStorage.clear();
    render(<App />);

    expect(screen.getByText('Sign In to Your Account')).toBeInTheDocument();
    expect(screen.getByText('Sign in to access your synchronized snippets across all devices.')).toBeInTheDocument();
    expect(screen.getByLabelText('Username')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.queryByText('Create Section')).not.toBeInTheDocument();
  });

  it('switches between Sign In and Create Account on the auth gate', () => {
    localStorage.clear();
    render(<App />);

    // Click Create Account tab
    const createAccountTab = screen.getByRole('button', { name: 'Create Account' });
    fireEvent.click(createAccountTab);

    expect(screen.getByText('Create an Account')).toBeInTheDocument();
    expect(screen.getByLabelText('Confirm Password')).toBeInTheDocument();
  });

  it('supports login from auth gate, displays user badge, and loads cloud data', async () => {
    localStorage.clear();
    global.fetch = vi.fn().mockImplementation((url) => {
      if (url.includes('/api/auth/login')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve({
            token: 'mock-jwt-token-xyz',
            user: { id: 'user-1', username: 'cody' }
          })
        });
      }
      if (url.includes('/api/data')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve({
            sections: [
              {
                sectionName: 'Cloud Section',
                sectionButtons: [{ buttonName: 'Cloud Item', pasteValue: 'Synced!' }]
              }
            ],
            favorites: []
          })
        });
      }
      return Promise.resolve({ ok: true, json: () => Promise.resolve({}) });
    });

    render(<App />);

    // Fill credentials
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'cody' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password123' } });

    // Submit form
    const form = screen.getByLabelText('Username').closest('form');
    fireEvent.submit(form);

    // Verify user badge appears and cloud section is rendered
    await waitFor(() => {
      expect(screen.getByText('cody')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Sign Out' })).toBeInTheDocument();
      expect(screen.getByText('Cloud Item')).toBeInTheDocument();
    });
  });

  it('supports signing out and returning to the auth gate', async () => {
    global.fetch = vi.fn().mockImplementation((url) => {
      if (url.includes('/api/auth/logout')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve({ success: true })
        });
      }
      return Promise.resolve({ ok: true, json: () => Promise.resolve({}) });
    });

    render(<App />);

    // Expect logged-in header with Sign Out button
    const signOutBtn = screen.getByRole('button', { name: 'Sign Out' });
    expect(signOutBtn).toBeInTheDocument();
    fireEvent.click(signOutBtn);

    // Should return to auth gate
    await waitFor(() => {
      expect(screen.getByText('Sign In to Your Account')).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: 'Sign Out' })).not.toBeInTheDocument();
    });
  });

  it('preserves drafted button input when window refocuses or background sync occurs', async () => {
    sessionStorage.clear();
    global.fetch = vi.fn().mockImplementation((url) => {
      if (url.includes('/api/data')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve({
            sections: [
              {
                sectionName: 'Work Tools',
                sectionButtons: []
              }
            ],
            favorites: []
          })
        });
      }
      return Promise.resolve({ ok: true, json: () => Promise.resolve({}) });
    });

    render(<App />);

    // Create section
    fireEvent.click(screen.getByText('Create Section'));
    fireEvent.change(screen.getByLabelText('Section Name'), { target: { value: 'Work Tools' } });
    fireEvent.click(screen.getByText('Submit'));

    // Open Add Button modal
    fireEvent.click(screen.getByText('Add Button'));

    // Fill in button draft
    const nameInput = screen.getByLabelText('Button Name');
    const pasteInput = screen.getByLabelText('Paste Value');
    fireEvent.change(nameInput, { target: { value: 'API Access Token' } });
    fireEvent.change(pasteInput, { target: { value: 'bearer-token-12345' } });

    expect(nameInput.value).toBe('API Access Token');
    expect(pasteInput.value).toBe('bearer-token-12345');

    // Simulate leaving and returning to window (window focus / visibilitychange)
    window.dispatchEvent(new Event('focus'));
    document.dispatchEvent(new Event('visibilitychange'));

    // Verify draft input was NOT wiped out
    expect(nameInput.value).toBe('API Access Token');
    expect(pasteInput.value).toBe('bearer-token-12345');
  });
});
