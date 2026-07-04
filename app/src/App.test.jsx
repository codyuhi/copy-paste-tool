import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import App from './App';

describe('Chat Agent Tool - Integration Tests', () => {
  beforeEach(() => {
    localStorage.clear();
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
    const favoritesPanel = screen.getByText('Favorites Pane').closest('aside');
    expect(screen.getByText('Home Page', { selector: '.fav-item-name' })).toBeInTheDocument();
  });
});
