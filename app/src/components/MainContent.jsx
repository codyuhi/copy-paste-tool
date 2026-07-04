import React from 'react';

const MainContent = ({ 
  sections, 
  searchQuery, 
  onSearchChange, 
  onCopy, 
  onContextMenu, 
  onOpenModal,
  onDeleteSection,
  onSetActiveSection
}) => {
  
  // Filtering logic
  const query = searchQuery.toLowerCase().trim();
  
  const filteredSections = sections.map((sec, secIdx) => {
    // If query is empty, return all buttons
    if (!query) {
      return { ...sec, originalIndex: secIdx, matchedButtons: sec.sectionButtons };
    }
    
    // Filter buttons that match name or paste value
    const matchedButtons = sec.sectionButtons.filter(btn => 
      btn.buttonName.toLowerCase().includes(query) || 
      btn.pasteValue.toLowerCase().includes(query)
    );
    
    // Check if section name matches the query
    const sectionNameMatches = sec.sectionName.toLowerCase().includes(query);
    
    // We keep the section if the section name matches OR it has matching buttons
    if (sectionNameMatches || matchedButtons.length > 0) {
      return {
        ...sec,
        originalIndex: secIdx,
        // If section name matches but no buttons match, show all buttons in it. Otherwise, show only matched buttons.
        matchedButtons: matchedButtons.length > 0 ? matchedButtons : sec.sectionButtons
      };
    }
    
    return null;
  }).filter(Boolean);

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
            <p style={{ fontSize: '14px' }}>Click "Create Section" in the sidebar to start organizing your copy-paste keys.</p>
          </div>
        ) : filteredSections.length === 0 ? (
          <div className="empty-state">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ opacity: 0.5 }}>
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <h3>No matching buttons found</h3>
            <p style={{ fontSize: '14px' }}>Try refining your search terms.</p>
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
                  <h3 className="section-name-title">{sec.sectionName}</h3>
                  <div className="section-actions">
                    <button 
                      className="btn-sec-action delete"
                      onClick={() => onDeleteSection(sec.originalIndex)}
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
                      // Find the actual button index in the original array
                      const originalBtnIdx = sec.sectionButtons.findIndex(
                        b => b.buttonName === btn.buttonName && b.pasteValue === btn.pasteValue
                      );
                      
                      return (
                        <button
                          key={btnIdx}
                          className="copy-btn"
                          onClick={() => onCopy(btn.buttonName, btn.pasteValue)}
                          onContextMenu={(e) => onContextMenu(e, 'button', { 
                            sectionIdx: sec.originalIndex, 
                            buttonIdx: originalBtnIdx !== -1 ? originalBtnIdx : btnIdx 
                          })}
                          title={btn.pasteValue}
                        >
                          {btn.buttonName}
                        </button>
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
