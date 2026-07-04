import React, { useState, useEffect, useRef } from 'react';
import Sidebar from './components/Sidebar';
import MainContent from './components/MainContent';
import FavoritesPane from './components/FavoritesPane';
import ContextMenu from './components/ContextMenu';
import Modals from './components/Modals';
import { loadSections, saveSections, loadFavorites, saveFavorites } from './utils/storage';

function App() {
  // App States
  const [sections, setSections] = useState([]);
  const [favorites, setFavorites] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  
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

  // Load initial state
  useEffect(() => {
    setSections(loadSections());
    setFavorites(loadFavorites());
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
    saveSections(updated);
    showToast(`Created section "${name}"`);
  };

  const handleDeleteSection = (index) => {
    const sec = sections[index];
    if (confirm(`Are you sure you want to delete the section "${sec.sectionName}" and all of its buttons?`)) {
      const updated = sections.filter((_, idx) => idx !== index);
      setSections(updated);
      saveSections(updated);

      // Splicing elements can shift indices. Adjust favorites pointing to subsequent indexes
      const filteredFavs = favorites.filter(fav => fav.sectionId !== index);
      const adjustedFavs = filteredFavs.map(fav => {
        if (fav.sectionId > index) {
          return { ...fav, sectionId: fav.sectionId - 1 };
        }
        return fav;
      });
      setFavorites(adjustedFavs);
      saveFavorites(adjustedFavs);

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
    saveSections(updated);
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
    saveSections(updated);

    // Sync edited fields inside Favorites
    const updatedFavs = favorites.map(fav => {
      if (fav.sectionId === secIdx && fav.buttonId === btnIdx) {
        return { ...fav, buttonName, pasteValue };
      }
      return fav;
    });
    setFavorites(updatedFavs);
    saveFavorites(updatedFavs);

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
      saveSections(updated);

      // Clean up favorites and adjust indices of shifted buttons
      const filteredFavs = favorites.filter(fav => !(fav.sectionId === sectionIdx && fav.buttonId === buttonIdx));
      const adjustedFavs = filteredFavs.map(fav => {
        if (fav.sectionId === sectionIdx && fav.buttonId > buttonIdx) {
          return { ...fav, buttonId: fav.buttonId - 1 };
        }
        return fav;
      });
      setFavorites(adjustedFavs);
      saveFavorites(adjustedFavs);

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
    saveFavorites(updated);
    showToast(`Added "${btn.buttonName}" to Favorites!`);
  };

  const handleRemoveFavorite = (favIdx) => {
    const fav = favorites[favIdx];
    if (!fav) return;
    const updated = favorites.filter((_, idx) => idx !== favIdx);
    setFavorites(updated);
    saveFavorites(updated);
    showToast(`Removed "${fav.buttonName}" from Favorites`);
  };

  // Reorder Sections
  const handleReorderSections = (newSections) => {
    // If we transition to reorder modal, we save the new list
    setSections(newSections);
    saveSections(newSections);

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
    saveFavorites(adjustedFavs);

    showToast('Sections reordered successfully!');
  };

  // Import JSON Data
  const handleImportData = (importedSections) => {
    const updated = [...sections, ...importedSections];
    setSections(updated);
    saveSections(updated);
    showToast(`Successfully imported ${importedSections.length} sections!`);
  };

  // Clear all data
  const handleClearAll = () => {
    setSections([]);
    setFavorites([]);
    saveSections([]);
    saveFavorites([]);
    showToast('All sections and favorites deleted.');
  };

  // Scroll to section element smoothly
  const handleScrollToSection = (idx) => {
    const el = document.getElementById(`section-${idx}`);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' });
      setActiveSectionId(`section-${idx}`);
    }
  };

  // Context Menu Trigger Hook
  const handleContextMenuTrigger = (e, type, data) => {
    e.preventDefault();
    setContextMenu({
      x: e.clientX,
      y: e.clientY,
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
    <div className="app-container">
      {/* Header Banner */}
      <header className="app-header">
        <div className="header-left">
          <button 
            className="settings-btn" 
            onClick={() => setActiveModal('settings')}
            title="Open Menu"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="3"></circle>
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
            </svg>
          </button>
          <span className="brand-title">Chat Agent Tool</span>
        </div>
        
        <span className="brand-company">company</span>
        
        <span className="brand-version">Version 2.0.0</span>
      </header>

      {/* Navigation Sidebar */}
      <Sidebar 
        sections={sections}
        onOpenModal={setActiveModal}
        activeSectionId={activeSectionId}
        onScrollToSection={handleScrollToSection}
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
      />

      {/* Favorites Sidebar */}
      <FavoritesPane 
        favorites={favorites}
        onCopy={handleCopy}
        onContextMenu={handleContextMenuTrigger}
      />

      {/* Custom Right-Click Context Menu Overlay */}
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
      />

      {/* Floating Snackbar Toast */}
      <div className={`toast ${toast.visible ? 'show' : ''}`}>
        {toast.message}
      </div>
    </div>
  );
}

export default App;
