import React from 'react';

const FavoritesPane = ({ favorites, onCopy, onContextMenu, onRemoveFavorite }) => {
  return (
    <aside className="app-favorites">
      <div className="favorites-header-row">
        <div className="favorites-title">Favorites Pane</div>
        {favorites.length > 0 && (
          <span className="favorites-count-badge">{favorites.length}</span>
        )}
      </div>
      
      <hr style={{ borderColor: 'var(--border-color)', borderStyle: 'solid', borderWidth: '0.5px 0 0 0', margin: '0' }} />

      {favorites.length === 0 ? (
        <div className="favorites-empty">
          <svg xmlns="http://www.w3.org/2000/svg" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" style={{ opacity: 0.4, margin: '0 auto 12px' }}>
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
          </svg>
          <div style={{ fontWeight: 600, marginBottom: '6px' }}>No Favorites Added Yet</div>
          <span style={{ fontSize: '12px', opacity: 0.75, lineHeight: 1.5 }}>
            Tap the options menu (⋯) or right-click any snippet in the main panel to add it here for 1-tap access.
          </span>
        </div>
      ) : (
        <div className="favorites-list">
          {favorites.map((fav, idx) => (
            <div key={idx} className="fav-item-row">
              <button
                className="fav-item"
                onClick={() => onCopy(fav.buttonName, fav.pasteValue)}
                onContextMenu={(e) => onContextMenu(e, 'favorite', idx)}
                title={fav.pasteValue}
              >
                <div className="fav-item-name" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="#eab308" stroke="#eab308" strokeWidth="2" style={{ flexShrink: 0 }}>
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                  </svg>
                  {fav.buttonName}
                </div>
                <div className="fav-item-preview">{fav.pasteValue}</div>
              </button>
              <button
                type="button"
                className="fav-item-remove-btn"
                onClick={(e) => {
                  e.stopPropagation();
                  if (onRemoveFavorite) {
                    onRemoveFavorite(idx);
                  } else {
                    onContextMenu(e, 'favorite', idx);
                  }
                }}
                aria-label={`Remove ${fav.buttonName} from favorites`}
                title="Remove favorite"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </button>
            </div>
          ))}
        </div>
      )}
    </aside>
  );
};

export default FavoritesPane;
