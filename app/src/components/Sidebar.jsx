import React from 'react';

const Sidebar = ({ sections, onOpenModal, activeSectionId, onScrollToSection, onNavigateMobile }) => {
  return (
    <aside className="app-sidebar">
      <div className="sidebar-header-row">
        <div className="sidebar-title">Navigation Pane</div>
        {sections.length > 0 && (
          <span className="sidebar-badge">{sections.length}</span>
        )}
      </div>
      
      <button 
        className="create-sec-btn"
        onClick={() => onOpenModal('section-add')}
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        Create Section
      </button>

      <hr style={{ borderColor: 'var(--border-color)', borderStyle: 'solid', borderWidth: '0.5px 0 0 0', margin: '8px 0' }} />

      <div className="nav-links">
        <a 
          href="#top" 
          className="nav-link"
          onClick={(e) => {
            e.preventDefault();
            const scrollable = document.querySelector('.content-scrollable');
            if (scrollable) {
              scrollable.scrollTo({ top: 0, behavior: 'smooth' });
            } else {
              window.scrollTo({ top: 0, behavior: 'smooth' });
            }
            if (onNavigateMobile) onNavigateMobile();
          }}
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ marginRight: '8px', flexShrink: 0 }}>
            <path d="m18 15-6-6-6 6"/>
          </svg>
          Top
        </a>
        
        {sections.map((sec, idx) => (
          <a
            key={idx}
            href={`#section-${idx}`}
            className={`nav-link ${activeSectionId === `section-${idx}` ? 'active' : ''}`}
            onClick={(e) => {
              e.preventDefault();
              onScrollToSection(idx);
              if (onNavigateMobile) onNavigateMobile();
            }}
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ marginRight: '8px', opacity: 0.7, flexShrink: 0 }}>
              <path d="M4 22V4c0-.5.2-1 .6-1.4C5 2.2 5.5 2 6 2h8l6 6v14c0 .5-.2 1-.6 1.4-.4.4-.9.6-1.4.6H6c-.5 0-1-.2-1.4-.6-.4-.4-.6-.9-.6-1.4Z"></path>
              <path d="M14 2v6h6"></path>
            </svg>
            <span className="nav-link-name">{sec.sectionName}</span>
            {sec.sectionButtons && sec.sectionButtons.length > 0 && (
              <span className="nav-count-pill">{sec.sectionButtons.length}</span>
            )}
          </a>
        ))}
      </div>
    </aside>
  );
};

export default Sidebar;
