import React, { useEffect, useRef, useState } from 'react';

const ContextMenu = ({ x, y, visible, type, data, onClose, onAction }) => {
  const menuRef = useRef(null);
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(typeof window !== 'undefined' && window.innerWidth < 640);
    };
    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  useEffect(() => {
    const handleOutsideClick = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        onClose();
      }
    };

    if (visible) {
      document.addEventListener('click', handleOutsideClick);
      document.addEventListener('contextmenu', handleOutsideClick);
    }

    return () => {
      document.removeEventListener('click', handleOutsideClick);
      document.removeEventListener('contextmenu', handleOutsideClick);
    };
  }, [visible, onClose]);

  if (!visible) return null;

  // Reposition to prevent menu overflow on desktop
  const windowWidth = typeof window !== 'undefined' ? window.innerWidth : 1024;
  const windowHeight = typeof window !== 'undefined' ? window.innerHeight : 768;
  const adjustedX = Math.min(Math.max(12, x), windowWidth - 190);
  const adjustedY = Math.min(Math.max(12, y), windowHeight - 160);

  return (
    <>
      {isMobile && (
        <div 
          className="context-backdrop" 
          onClick={onClose}
          aria-hidden="true"
        />
      )}
      <div 
        ref={menuRef}
        className={`custom-context-menu ${isMobile ? 'mobile-sheet' : ''}`}
        style={!isMobile ? { left: `${adjustedX}px`, top: `${adjustedY}px` } : undefined}
      >
        {isMobile && (
          <div className="mobile-sheet-header">
            <div className="mobile-sheet-drag-handle"></div>
            <div className="mobile-sheet-title">
              {type === 'button' ? 'Snippet Actions' : 'Favorite Actions'}
            </div>
          </div>
        )}

        {type === 'button' && (
          <>
            <button 
              type="button"
              className="context-item"
              onClick={() => {
                onAction('favorite', data);
                onClose();
              }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
              </svg>
              Favorite
            </button>
            
            <button 
              type="button"
              className="context-item"
              onClick={() => {
                onAction('edit', data);
                onClose();
              }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 20h9"></path>
                <path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"></path>
              </svg>
              Edit
            </button>
            
            <hr style={{ borderColor: 'var(--border-color)', borderStyle: 'solid', borderWidth: '0.5px 0 0 0', margin: '4px 0' }} />
            
            <button 
              type="button"
              className="context-item danger"
              onClick={() => {
                onAction('delete', data);
                onClose();
              }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              </svg>
              Delete
            </button>
          </>
        )}

        {type === 'favorite' && (
          <button 
            type="button"
            className="context-item danger"
            onClick={() => {
              onAction('remove-favorite', data);
              onClose();
            }}
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
            Remove Favorite
          </button>
        )}

        {isMobile && (
          <button 
            type="button"
            className="context-item cancel-btn"
            onClick={onClose}
          >
            Cancel
          </button>
        )}
      </div>
    </>
  );
};

export default ContextMenu;
