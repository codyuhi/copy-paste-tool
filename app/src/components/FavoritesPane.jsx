import React from 'react';

const FavoritesPane = ({ favorites, onCopy, onContextMenu }) => {
  return (
    <aside className="app-favorites">
      <div className="favorites-title">Favorites Pane</div>
      
      <hr style={{ borderColor: 'var(--border-color)', borderStyle: 'solid', borderWidth: '0.5px 0 0 0', margin: '0' }} />

      {favorites.length === 0 ? (
        <div className="favorites-empty">
          No Favorites Added Yet
          <br /><br />
          <span style={{ fontSize: '11px', opacity: 0.8 }}>
            (Right-Click any button in the main panel to add it as a favorite)
          </span>
        </div>
      ) : (
        <div className="favorites-list">
          {favorites.map((fav, idx) => (
            <button
              key={idx}
              className="fav-item"
              onClick={() => onCopy(fav.buttonName, fav.pasteValue)}
              onContextMenu={(e) => onContextMenu(e, 'favorite', idx)}
              title={fav.pasteValue}
            >
              <div className="fav-item-name" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="#eab308" stroke="#eab308" strokeWidth="2">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                </svg>
                {fav.buttonName}
              </div>
              <div className="fav-item-preview">{fav.pasteValue}</div>
            </button>
          ))}
        </div>
      )}
    </aside>
  );
};

export default FavoritesPane;
