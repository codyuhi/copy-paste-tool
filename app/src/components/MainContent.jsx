import React, { useState, useRef } from 'react';

const MainContent = ({ 
  sections, 
  searchQuery, 
  onSearchChange, 
  onCopy, 
  onContextMenu, 
  onOpenModal,
  onDeleteSection,
  onSetActiveSection,
  onNavigateToSections
}) => {
  const [copiedKey, setCopiedKey] = useState(null);
  const touchTimerRef = useRef(null);
  const touchMovedRef = useRef(false);

  // Filtering logic
  const query = searchQuery.toLowerCase().trim();
  
  const filteredSections = sections.map((sec, secIdx) => {
    if (!query) {
      return { ...sec, originalIndex: secIdx, matchedButtons: sec.sectionButtons };
    }
    
    const matchedButtons = sec.sectionButtons.filter(btn => 
      btn.buttonName.toLowerCase().includes(query) || 
      btn.pasteValue.toLowerCase().includes(query)
    );
    
    const sectionNameMatches = sec.sectionName.toLowerCase().includes(query);
    
    if (sectionNameMatches || matchedButtons.length > 0) {
      return {
        ...sec,
        originalIndex: secIdx,
        matchedButtons: matchedButtons.length > 0 ? matchedButtons : sec.sectionButtons
      };
    }
    
    return null;
  }).filter(Boolean);

  const handleCopyClick = (btn, key) => {
    onCopy(btn.buttonName, btn.pasteValue);
    setCopiedKey(key);
    setTimeout(() => {
      setCopiedKey((prev) => (prev === key ? null : prev));
    }, 1400);
  };

  const handleTouchStart = (e, coords) => {
    touchMovedRef.current = false;
    touchTimerRef.current = setTimeout(() => {
      if (!touchMovedRef.current) {
        // Trigger context menu on long press
        const touch = e.touches[0];
        onContextMenu(
          {
            preventDefault: () => {},
            clientX: touch ? touch.clientX : 100,
            clientY: touch ? touch.clientY : 100
          },
          'button',
          coords
        );
      }
    }, 550);
  };

  const handleTouchMove = () => {
    touchMovedRef.current = true;
    if (touchTimerRef.current) clearTimeout(touchTimerRef.current);
  };

  const handleTouchEnd = () => {
    if (touchTimerRef.current) {
      clearTimeout(touchTimerRef.current);
      touchTimerRef.current = null;
    }
  };

  return (
    <main className="app-main">
      <div className="search-wrapper">
        <div className="search-container">
          <svg className="search-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
            <circle cx="11" cy="11" r="8"></circle>
            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
          </svg>
          <input
            type="text"
            className="search-input"
            placeholder="Search buttons or paste values..."
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
          />
          {searchQuery && (
            <button 
              type="button" 
              className="search-clear-btn" 
              onClick={() => onSearchChange('')}
              aria-label="Clear search input"
              title="Clear search"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          )}
        </div>
      </div>

      <div className="content-scrollable">
        {sections.length === 0 ? (
          <div className="empty-state">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ opacity: 0.5 }}>
              <path d="M4 22V4c0-.5.2-1 .6-1.4C5 2.2 5.5 2 6 2h8l6 6v14c0 .5-.2 1-.6 1.4-.4.4-.9.6-1.4.6H6c-.5 0-1-.2-1.4-.6-.4-.4-.6-.9-.6-1.4Z"></path>
              <path d="M14 2v6h6"></path>
              <line x1="9" y1="15" x2="15" y2="15"></line>
              <line x1="9" y1="11" x2="15" y2="11"/>
            </svg>
            <h3>No Sections Created Yet</h3>
            <p style={{ fontSize: '14px', maxWidth: '360px', textAlign: 'center', lineHeight: 1.5 }}>
              Click "Create Section" in the navigation pane to start organizing your copy-paste keys.
            </p>
          </div>
        ) : filteredSections.length === 0 ? (
          <div className="empty-state">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ opacity: 0.5 }}>
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <h3>No matching buttons found</h3>
            <p style={{ fontSize: '14px' }}>Try refining your search terms.</p>
            <button 
              type="button"
              className="btn btn-cancel"
              onClick={() => onSearchChange('')}
              style={{ marginTop: '8px' }}
            >
              Clear Search Filter
            </button>
          </div>
        ) : (
          <div className="section-list">
            {filteredSections.map((sec) => (
              <section 
                key={sec.originalIndex} 
                id={`section-${sec.originalIndex}`}
                className="section-card"
              >
                <div className="section-header">
                  <div className="section-title-wrap">
                    <h3 className="section-name-title">{sec.sectionName}</h3>
                    {sec.matchedButtons && (
                      <span className="section-count-tag">{sec.matchedButtons.length}</span>
                    )}
                  </div>
                  <div className="section-actions">
                    <button 
                      className="btn-sec-action delete"
                      onClick={() => onDeleteSection(sec.originalIndex)}
                      title={`Delete section "${sec.sectionName}"`}
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                        <polyline points="3 6 5 6 21 6"></polyline>
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                      </svg>
                      Delete Section
                    </button>
                    <button 
                      className="btn-sec-action add"
                      onClick={() => {
                        onSetActiveSection(sec.originalIndex);
                        onOpenModal('button-add');
                      }}
                      title={`Add button to "${sec.sectionName}"`}
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                        <line x1="12" y1="5" x2="12" y2="19"></line>
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                      </svg>
                      Add Button
                    </button>
                  </div>
                </div>

                <div className="button-grid">
                  {sec.matchedButtons.length === 0 ? (
                    <div className="no-buttons-placeholder">
                      No buttons created for this section yet
                    </div>
                  ) : (
                    sec.matchedButtons.map((btn, btnIdx) => {
                      const originalBtnIdx = sec.sectionButtons.findIndex(
                        b => b.buttonName === btn.buttonName && b.pasteValue === btn.pasteValue
                      );
                      const coords = { 
                        sectionIdx: sec.originalIndex, 
                        buttonIdx: originalBtnIdx !== -1 ? originalBtnIdx : btnIdx 
                      };
                      const key = `${sec.originalIndex}-${coords.buttonIdx}`;
                      const isCopied = copiedKey === key;
                      
                      return (
                        <div key={btnIdx} className={`snippet-card ${isCopied ? 'copied' : ''}`}>
                          <button
                            className="copy-btn"
                            onClick={() => handleCopyClick(btn, key)}
                            onContextMenu={(e) => onContextMenu(e, 'button', coords)}
                            onTouchStart={(e) => handleTouchStart(e, coords)}
                            onTouchMove={handleTouchMove}
                            onTouchEnd={handleTouchEnd}
                            onTouchCancel={handleTouchEnd}
                            title={btn.pasteValue}
                          >
                            {btn.buttonName}
                          </button>
                          <button
                            type="button"
                            className="snippet-more-btn"
                            onClick={(e) => {
                              e.stopPropagation();
                              const rect = e.currentTarget.getBoundingClientRect();
                              onContextMenu(
                                {
                                  preventDefault: () => {},
                                  clientX: rect.left,
                                  clientY: rect.bottom + 4
                                },
                                'button',
                                coords
                              );
                            }}
                            title={`Options for ${btn.buttonName}`}
                            aria-label={`Options for ${btn.buttonName}`}
                          >
                            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                              <circle cx="12" cy="5" r="2"></circle>
                              <circle cx="12" cy="12" r="2"></circle>
                              <circle cx="12" cy="19" r="2"></circle>
                            </svg>
                          </button>
                          {isCopied && <div className="copied-toast-chip">Copied! ✓</div>}
                        </div>
                      );
                    })
                  )}
                </div>
              </section>
            ))}
          </div>
        )}
      </div>
    </main>
  );
};

export default MainContent;
