import React, { useState, useEffect, useRef, useCallback } from 'react';
import Sidebar from './components/Sidebar';
import MainContent from './components/MainContent';
import FavoritesPane from './components/FavoritesPane';
import ContextMenu from './components/ContextMenu';
import Modals from './components/Modals';
import AuthModal from './components/AuthModal';
import { loadSections, saveSections, loadFavorites, saveFavorites } from './utils/storage';
import { getAuthToken, getStoredUser, apiGetMe, apiFetchUserData, apiSaveUserData, apiLogout } from './utils/api';
import ThemeToggle from './components/ThemeToggle';

function App() {
  // App States
  const [sections, setSections] = useState([]);
  const [favorites, setFavorites] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  
  // Auth & Cloud Sync States
  const [user, setUser] = useState(() => getStoredUser());
  const [syncStatus, setSyncStatus] = useState('idle'); // 'idle' | 'saving' | 'saved' | 'error'
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const syncTimeoutRef = useRef(null);

  // Mobile Tab State: 'snippets' | 'sections' | 'favorites'
  const [mobileTab, setMobileTab] = useState('snippets');

  // Modal / Selection States
  const [activeModal, setActiveModal] = useState(null); // 'section-add' | 'button-add' | 'button-edit' | 'settings' | 'import' | 'export' | 'reorder-sections'
  const [activeSectionIdx, setActiveSectionIdx] = useState(null);
  const [activeButtonCoords, setActiveButtonCoords] = useState(null); // { sectionIdx, buttonIdx }
  
  // Custom Context Menu State
  const [contextMenu, setContextMenu] = useState({
    x: 0,
    y: 0,
    visible: false,
    type: null, // 'button' | 'favorite'
    data: null // coords or index
  });

  // Toast Notification State
  const [toast, setToast] = useState({ visible: false, message: '' });
  const toastTimeoutRef = useRef(null);

  // Active Section navigation tracker (highlight sidebar item based on scroll/click)
  const [activeSectionId, setActiveSectionId] = useState('top');

  // Helper to persist locally and sync to server if authenticated
  const saveAndSync = useCallback(async (newSections, newFavorites) => {
    saveSections(newSections);
    saveFavorites(newFavorites);

    if (getAuthToken()) {
      setSyncStatus('saving');
      try {
        await apiSaveUserData(newSections, newFavorites);
        setSyncStatus('saved');
        if (syncTimeoutRef.current) clearTimeout(syncTimeoutRef.current);
        syncTimeoutRef.current = setTimeout(() => setSyncStatus('idle'), 2500);
      } catch (err) {
        console.error('Failed to sync to server:', err);
        setSyncStatus('error');
      }
    }
  }, []);

  // Load initial state and verify session
  useEffect(() => {
    setSections(loadSections());
    setFavorites(loadFavorites());

    const initAuthAndSync = async () => {
      const token = getAuthToken();
      if (!token) return;

      try {
        const currentUser = await apiGetMe();
        if (currentUser) {
          setUser(currentUser);
          const data = await apiFetchUserData();
          if (data && (data.sections?.length > 0 || data.favorites?.length > 0)) {
            setSections(data.sections || []);
            setFavorites(data.favorites || []);
            saveSections(data.sections || []);
            saveFavorites(data.favorites || []);
          } else {
            // If account is new/empty, migrate current local snippets to server
            const localSec = loadSections();
            const localFav = loadFavorites();
            if (localSec.length > 0 || localFav.length > 0) {
              await apiSaveUserData(localSec, localFav);
            }
          }
        } else {
          setUser(null);
        }
      } catch (err) {
        console.warn('Initial session sync failed:', err);
      }
    };

    initAuthAndSync();
  }, []);

  // Auto-sync when returning to tab / switching devices
  useEffect(() => {
    const handleVisibilitySync = async () => {
      if (!getAuthToken()) return;
      try {
        const data = await apiFetchUserData();
        if (data?.sections) {
          setSections(data.sections);
          setFavorites(data.favorites || []);
          saveSections(data.sections);
          saveFavorites(data.favorites || []);
        }
      } catch (err) {
        console.warn('Background sync error:', err);
      }
    };

    window.addEventListener('focus', handleVisibilitySync);
    const onVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        handleVisibilitySync();
      }
    };
    document.addEventListener('visibilitychange', onVisibilityChange);

    return () => {
      window.removeEventListener('focus', handleVisibilitySync);
      document.removeEventListener('visibilitychange', onVisibilityChange);
      if (syncTimeoutRef.current) clearTimeout(syncTimeoutRef.current);
    };
  }, []);

  // Display Toast Notification
  const showToast = (message) => {
    if (toastTimeoutRef.current) {
      clearTimeout(toastTimeoutRef.current);
    }
    setToast({ visible: true, message });
    toastTimeoutRef.current = setTimeout(() => {
      setToast({ visible: false, message: '' });
    }, 3000);
  };

  // Cleanup timeout on unmount
  useEffect(() => {
    return () => {
      if (toastTimeoutRef.current) clearTimeout(toastTimeoutRef.current);
    };
  }, []);

  // Handle Clipboard Copy
  const handleCopy = async (name, value) => {
    try {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        await navigator.clipboard.writeText(value);
      } else {
        // Fallback
        const textarea = document.createElement('textarea');
        textarea.value = value;
        textarea.style.position = 'fixed';
        textarea.style.opacity = '0';
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
      }
      showToast(`Copied "${name}" text data to clipboard!`);
    } catch (err) {
      console.error('Failed to copy to clipboard:', err);
      showToast('Failed to copy text.');
    }
  };

  // Section CRUD Operations
  const handleAddSection = (name) => {
    const updated = [...sections, { sectionName: name, sectionButtons: [] }];
    setSections(updated);
    saveAndSync(updated, favorites);
    showToast(`Created section "${name}"`);
  };

  const handleDeleteSection = (index) => {
    const sec = sections[index];
    if (confirm(`Are you sure you want to delete the section "${sec.sectionName}" and all of its buttons?`)) {
      const updated = sections.filter((_, idx) => idx !== index);
      setSections(updated);

      // Splicing elements can shift indices. Adjust favorites pointing to subsequent indexes
      const filteredFavs = favorites.filter(fav => fav.sectionId !== index);
      const adjustedFavs = filteredFavs.map(fav => {
        if (fav.sectionId > index) {
          return { ...fav, sectionId: fav.sectionId - 1 };
        }
        return fav;
      });
      setFavorites(adjustedFavs);
      saveAndSync(updated, adjustedFavs);

      showToast(`Deleted section "${sec.sectionName}"`);
    }
  };

  // Button CRUD Operations
  const handleAddButton = (secIdx, buttonName, pasteValue) => {
    const updated = sections.map((sec, idx) => {
      if (idx === secIdx) {
        return {
          ...sec,
          sectionButtons: [...sec.sectionButtons, { buttonName, pasteValue }]
        };
      }
      return sec;
    });
    setSections(updated);
    saveAndSync(updated, favorites);
    showToast(`Created button "${buttonName}"`);
  };

  const handleEditButton = (secIdx, btnIdx, buttonName, pasteValue) => {
    const updated = sections.map((sec, idx) => {
      if (idx === secIdx) {
        const updatedButtons = sec.sectionButtons.map((btn, bIdx) => {
          if (bIdx === btnIdx) {
            return { buttonName, pasteValue };
          }
          return btn;
        });
        return { ...sec, sectionButtons: updatedButtons };
      }
      return sec;
    });
    setSections(updated);

    // Sync edited fields inside Favorites
    const updatedFavs = favorites.map(fav => {
      if (fav.sectionId === secIdx && fav.buttonId === btnIdx) {
        return { ...fav, buttonName, pasteValue };
      }
      return fav;
    });
    setFavorites(updatedFavs);
    saveAndSync(updated, updatedFavs);

    showToast(`Updated button "${buttonName}"`);
  };

  const handleDeleteButton = ({ sectionIdx, buttonIdx }) => {
    const btn = sections[sectionIdx]?.sectionButtons[buttonIdx];
    if (!btn) return;

    if (confirm(`Are you sure you want to delete the button "${btn.buttonName}"?`)) {
      const updated = sections.map((sec, idx) => {
        if (idx === sectionIdx) {
          const updatedButtons = sec.sectionButtons.filter((_, bIdx) => bIdx !== buttonIdx);
          return { ...sec, sectionButtons: updatedButtons };
        }
        return sec;
      });
      setSections(updated);

      // Clean up favorites and adjust indices of shifted buttons
      const filteredFavs = favorites.filter(fav => !(fav.sectionId === sectionIdx && fav.buttonId === buttonIdx));
      const adjustedFavs = filteredFavs.map(fav => {
        if (fav.sectionId === sectionIdx && fav.buttonId > buttonIdx) {
          return { ...fav, buttonId: fav.buttonId - 1 };
        }
        return fav;
      });
      setFavorites(adjustedFavs);
      saveAndSync(updated, adjustedFavs);

      showToast(`Deleted button "${btn.buttonName}"`);
    }
  };

  // Favorites Operations
  const handleAddFavorite = ({ sectionIdx, buttonIdx }) => {
    const btn = sections[sectionIdx]?.sectionButtons[buttonIdx];
    if (!btn) return;

    const isAlreadyFav = favorites.some(fav => fav.sectionId === sectionIdx && fav.buttonId === buttonIdx);
    if (isAlreadyFav) {
      showToast(`"${btn.buttonName}" is already favorited!`);
      return;
    }

    const newFav = {
      ...btn,
      sectionId: sectionIdx,
      buttonId: buttonIdx
    };

    const updated = [...favorites, newFav];
    setFavorites(updated);
    saveAndSync(sections, updated);
    showToast(`Added "${btn.buttonName}" to Favorites!`);
  };

  const handleRemoveFavorite = (favIdx) => {
    const fav = favorites[favIdx];
    if (!fav) return;
    const updated = favorites.filter((_, idx) => idx !== favIdx);
    setFavorites(updated);
    saveAndSync(sections, updated);
    showToast(`Removed "${fav.buttonName}" from Favorites`);
  };

  // Reorder Sections
  const handleReorderSections = (newSections) => {
    setSections(newSections);

    // Sync favorite mappings to new section positions
    const adjustedFavs = favorites.map(fav => {
      const oldSectionName = sections[fav.sectionId]?.sectionName;
      const newIdx = newSections.findIndex(sec => sec.sectionName === oldSectionName);
      if (newIdx !== -1) {
        return { ...fav, sectionId: newIdx };
      }
      return fav;
    });
    setFavorites(adjustedFavs);
    saveAndSync(newSections, adjustedFavs);

    showToast('Sections reordered successfully!');
  };

  // Import JSON Data
  const handleImportData = (importedSections) => {
    const updated = [...sections, ...importedSections];
    setSections(updated);
    saveAndSync(updated, favorites);
    showToast(`Successfully imported ${importedSections.length} sections!`);
  };

  // Clear all data
  const handleClearAll = () => {
    setSections([]);
    setFavorites([]);
    saveAndSync([], []);
    showToast('All sections and favorites deleted.');
  };

  // Auth Success Handler
  const handleAuthSuccess = async (newUser, initialData) => {
    setUser(newUser);
    if (initialData?.initialSections && initialData.initialSections.length > 0) {
      setSections(initialData.initialSections);
      setFavorites(initialData.initialFavorites || []);
      saveSections(initialData.initialSections);
      saveFavorites(initialData.initialFavorites || []);
    } else {
      try {
        const data = await apiFetchUserData();
        if (data?.sections) {
          setSections(data.sections);
          setFavorites(data.favorites || []);
          saveSections(data.sections);
          saveFavorites(data.favorites || []);
        }
      } catch (err) {
        console.error('Failed to load user data on login:', err);
      }
    }
  };

  // Sign out Handler
  const handleLogout = async () => {
    await apiLogout();
    setUser(null);
    setSyncStatus('idle');
    showToast('Signed out successfully');
  };

  // Scroll to section element smoothly
  const handleScrollToSection = (idx) => {
    setMobileTab('snippets');
    setTimeout(() => {
      const el = document.getElementById(`section-${idx}`);
      if (el) {
        el.scrollIntoView({ behavior: 'smooth' });
        setActiveSectionId(`section-${idx}`);
      }
    }, 50);
  };

  // Context Menu Trigger Hook
  const handleContextMenuTrigger = (e, type, data) => {
    if (e && e.preventDefault) e.preventDefault();
    setContextMenu({
      x: e?.clientX ?? (window.innerWidth / 2),
      y: e?.clientY ?? (window.innerHeight / 2),
      visible: true,
      type,
      data
    });
  };

  // Route Custom Dialog Callbacks
  const handleContextMenuAction = (action, data) => {
    if (action === 'favorite') {
      handleAddFavorite(data);
    } else if (action === 'edit') {
      setActiveButtonCoords(data);
      setActiveModal('button-edit');
    } else if (action === 'delete') {
      handleDeleteButton(data);
    } else if (action === 'remove-favorite') {
      handleRemoveFavorite(data);
    }
  };

  // Modals settings transition route
  const handleSettingsNav = (sectionsData, mode) => {
    if (mode === 'import') {
      setActiveModal('import');
    } else if (mode === 'export') {
      setActiveModal('export');
    } else {
      setActiveModal('reorder-sections');
    }
  };

  return (
    <div className={`app-container tab-${mobileTab}`}>
      {/* Header Banner */}
      <header className="app-header">
        <div className="header-left">
          <button 
            className="settings-btn" 
            onClick={() => setActiveModal('settings')}
            title="Open Menu"
            aria-label="Open Settings Menu"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="3"></circle>
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
            </svg>
          </button>
          <span className="brand-title">Chat Agent Tool</span>
        </div>
        
        <span className="brand-company">company</span>
        
        <div className="header-right">
          {user && (
            <div className={`sync-indicator ${syncStatus}`} title="Cloud Sync Status">
              {syncStatus === 'saving' && (
                <>
                  <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67"></path>
                  </svg>
                  <span>Saving...</span>
                </>
              )}
              {syncStatus === 'saved' && (
                <>
                  <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                  <span>Saved</span>
                </>
              )}
              {syncStatus === 'error' && (
                <>
                  <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                  </svg>
                  <span>Offline</span>
                </>
              )}
              {syncStatus === 'idle' && (
                <>
                  <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M17.5 19H9a7 7 0 1 1 6.71-9h1.79a4.5 4.5 0 1 1 0 9Z"></path>
                  </svg>
                  <span>Synced</span>
                </>
              )}
            </div>
          )}

          {user ? (
            <>
              <div className="user-badge" title={`Signed in as ${user.username}`}>
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
                <span>{user.username}</span>
              </div>
              <button 
                className="btn-auth-logout" 
                onClick={handleLogout}
                title="Sign Out"
                aria-label="Sign Out"
              >
                Sign Out
              </button>
            </>
          ) : (
            <button 
              className="btn-auth-signin" 
              onClick={() => setIsAuthModalOpen(true)}
              title="Sign In / Register"
              aria-label="Sign In"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path>
                <polyline points="10 17 15 12 10 7"></polyline>
                <line x1="15" y1="12" x2="3" y2="12"></line>
              </svg>
              <span>Sign In</span>
            </button>
          )}

          <span className="brand-version">Version 2.0.0</span>
          <ThemeToggle />
        </div>
      </header>

      {/* Navigation Sidebar */}
      <Sidebar 
        sections={sections}
        onOpenModal={setActiveModal}
        activeSectionId={activeSectionId}
        onScrollToSection={handleScrollToSection}
        onNavigateMobile={() => setMobileTab('snippets')}
      />

      {/* Central Content Panel */}
      <MainContent 
        sections={sections}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        onCopy={handleCopy}
        onContextMenu={handleContextMenuTrigger}
        onOpenModal={setActiveModal}
        onDeleteSection={handleDeleteSection}
        onSetActiveSection={setActiveSectionIdx}
        onNavigateToSections={() => setMobileTab('sections')}
      />

      {/* Favorites Sidebar */}
      <FavoritesPane 
        favorites={favorites}
        onCopy={handleCopy}
        onContextMenu={handleContextMenuTrigger}
        onRemoveFavorite={handleRemoveFavorite}
      />

      {/* Mobile Bottom Navigation Bar (Visible on screens < 1024px) */}
      <nav className="mobile-bottom-nav" aria-label="Mobile Navigation">
        <button
          className={`mobile-nav-tab ${mobileTab === 'snippets' ? 'active' : ''}`}
          onClick={() => setMobileTab('snippets')}
          type="button"
          aria-label="Snippets"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect width="14" height="14" x="8" y="8" rx="2" ry="2"></rect>
            <path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2"></path>
          </svg>
          <span>Snippets</span>
        </button>

        <button
          className={`mobile-nav-tab ${mobileTab === 'sections' ? 'active' : ''}`}
          onClick={() => setMobileTab('sections')}
          type="button"
          aria-label="Sections"
        >
          <div className="mobile-tab-icon-wrap">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M4 22V4c0-.5.2-1 .6-1.4C5 2.2 5.5 2 6 2h8l6 6v14c0 .5-.2 1-.6 1.4-.4.4-.9.6-1.4.6H6c-.5 0-1-.2-1.4-.6-.4-.4-.6-.9-.6-1.4Z"></path>
              <path d="M14 2v6h6"></path>
            </svg>
            {sections.length > 0 && <span className="mobile-tab-counter">{sections.length}</span>}
          </div>
          <span>Sections</span>
        </button>

        <button
          className={`mobile-nav-tab ${mobileTab === 'favorites' ? 'active' : ''}`}
          onClick={() => setMobileTab('favorites')}
          type="button"
          aria-label="Favorites"
        >
          <div className="mobile-tab-icon-wrap">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
            </svg>
            {favorites.length > 0 && <span className="mobile-tab-counter">{favorites.length}</span>}
          </div>
          <span>Favorites</span>
        </button>

        <button
          className="mobile-nav-tab"
          onClick={() => setActiveModal('settings')}
          type="button"
          aria-label="Menu"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="12" cy="12" r="3"></circle>
            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
          </svg>
          <span>Menu</span>
        </button>
      </nav>

      {/* Custom Right-Click Context Menu / Mobile Action Sheet */}
      <ContextMenu 
        x={contextMenu.x}
        y={contextMenu.y}
        visible={contextMenu.visible}
        type={contextMenu.type}
        data={contextMenu.data}
        onClose={() => setContextMenu({ ...contextMenu, visible: false })}
        onAction={handleContextMenuAction}
      />

      {/* Modals Management Layer */}
      <Modals 
        activeModal={activeModal}
        onClose={() => setActiveModal(null)}
        sections={sections}
        onAddSection={activeSectionIdx === null && activeModal === 'settings' ? handleSettingsNav : handleAddSection}
        onAddButton={handleAddButton}
        onEditButton={handleEditButton}
        activeSectionIdx={activeSectionIdx}
        activeButtonCoords={activeButtonCoords}
        onReorderSections={handleReorderSections}
        onImportData={handleImportData}
        onClearAll={handleClearAll}
        user={user}
        onOpenAuth={() => setIsAuthModalOpen(true)}
        onLogout={handleLogout}
      />

      {/* User Authentication & Registration Modal */}
      <AuthModal 
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
        onSuccess={handleAuthSuccess}
        localSections={sections}
        localFavorites={favorites}
        showToast={showToast}
      />

      {/* Floating Snackbar Toast */}
      <div className={`toast ${toast.visible ? 'show' : ''}`}>
        {toast.message}
      </div>
    </div>
  );
}

export default App;
