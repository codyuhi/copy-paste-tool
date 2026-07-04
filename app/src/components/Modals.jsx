import React, { useState, useEffect } from 'react';

const Modals = ({
  activeModal,
  onClose,
  sections,
  onAddSection,
  onAddButton,
  onEditButton,
  activeSectionIdx,
  activeButtonCoords,
  onReorderSections,
  onImportData,
  onClearAll
}) => {
  if (!activeModal) return null;

  // Local Form States
  const [sectionName, setSectionName] = useState('');
  const [buttonName, setButtonName] = useState('');
  const [pasteValue, setPasteValue] = useState('');
  const [tempSections, setTempSections] = useState([]);
  const [exportSelections, setExportSelections] = useState({});
  const [allExportSelected, setAllExportSelected] = useState(false);

  // Initialize form fields when modal opens
  useEffect(() => {
    if (activeModal === 'section-add') {
      setSectionName('');
    } else if (activeModal === 'button-add') {
      setButtonName('');
      setPasteValue('');
    } else if (activeModal === 'button-edit' && activeButtonCoords) {
      const { sectionIdx, buttonIdx } = activeButtonCoords;
      const btn = sections[sectionIdx]?.sectionButtons[buttonIdx];
      if (btn) {
        setButtonName(btn.buttonName);
        setPasteValue(btn.pasteValue);
      }
    } else if (activeModal === 'reorder-sections') {
      setTempSections([...sections]);
    } else if (activeModal === 'export') {
      const initialSelections = {};
      sections.forEach((_, idx) => {
        initialSelections[idx] = false;
      });
      setExportSelections(initialSelections);
      setAllExportSelected(false);
    }
  }, [activeModal, sections, activeButtonCoords]);

  // Form Submit Handlers
  const handleAddSectionSubmit = (e) => {
    e.preventDefault();
    if (!sectionName.trim()) return;
    onAddSection(sectionName.trim());
    onClose();
  };

  const handleAddButtonSubmit = (e) => {
    e.preventDefault();
    if (!buttonName.trim() || !pasteValue.trim()) return;
    onAddButton(activeSectionIdx, buttonName.trim(), pasteValue);
    onClose();
  };

  const handleEditButtonSubmit = (e) => {
    e.preventDefault();
    if (!buttonName.trim() || !pasteValue.trim()) return;
    onEditButton(activeButtonCoords.sectionIdx, activeButtonCoords.buttonIdx, buttonName.trim(), pasteValue);
    onClose();
  };

  // Reorder Handlers
  const moveSection = (index, direction) => {
    const newIndex = direction === 'up' ? index - 1 : index + 1;
    if (newIndex < 0 || newIndex >= tempSections.length) return;
    const updated = [...tempSections];
    const temp = updated[index];
    updated[index] = updated[newIndex];
    updated[newIndex] = temp;
    setTempSections(updated);
  };

  const handleReorderSubmit = () => {
    onReorderSections(tempSections);
    onClose();
  };

  // Import Handler
  const handleImportFile = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    
    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const parsed = JSON.parse(event.target.result);
        if (!Array.isArray(parsed)) throw new Error('Data is not a JSON array');
        
        const imported = parsed.map(item => {
          // Compatibility with legacy nested-stringified format
          const sec = typeof item === 'string' ? JSON.parse(item) : item;
          if (!sec.sectionName) throw new Error('Missing sectionName key');
          return {
            sectionName: sec.sectionName,
            sectionButtons: Array.isArray(sec.sectionButtons) ? sec.sectionButtons.map(b => ({
              buttonName: b.buttonName || 'Unnamed',
              pasteValue: b.pasteValue || ''
            })) : []
          };
        });

        onImportData(imported);
        onClose();
      } catch (err) {
        alert('File upload failed. Please try again with a valid JSON file. Error: ' + err.message);
      }
    };
    reader.readAsText(file);
  };

  // Export Handlers
  const handleExportToggleAll = () => {
    const nextVal = !allExportSelected;
    setAllExportSelected(nextVal);
    const updated = {};
    sections.forEach((_, idx) => {
      updated[idx] = nextVal;
    });
    setExportSelections(updated);
  };

  const handleExportToggleItem = (idx) => {
    const updated = { ...exportSelections, [idx]: !exportSelections[idx] };
    setExportSelections(updated);
    
    // Update "select all" state
    const allSelected = sections.every((_, i) => updated[i]);
    setAllExportSelected(allSelected);
  };

  const handleExportSubmit = () => {
    const selectedIndexes = Object.keys(exportSelections).filter(idx => exportSelections[idx]);
    if (selectedIndexes.length === 0) {
      alert('Please select at least one section to export.');
      return;
    }

    const exportData = selectedIndexes.map(idx => {
      const sec = sections[idx];
      // Keep legacy format structure (array of stringified section objects)
      return JSON.stringify({
        sectionName: sec.sectionName,
        sectionButtons: sec.sectionButtons
      });
    });

    const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'chat_sections_export.json';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    onClose();
  };

  return (
    <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', zIndex: 100 }}>
      {/* Backdrop */}
      <div 
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          backgroundColor: 'rgba(5, 8, 16, 0.8)',
          backdropFilter: 'blur(8px)',
          WebkitBackdropFilter: 'blur(8px)'
        }}
        onClick={onClose}
      />

      {/* Modal Dialog Body */}
      <dialog open style={{ display: 'flex' }}>
        <div className="modal-header">
          <h3 className="modal-title">
            {activeModal === 'section-add' && 'Create Section'}
            {activeModal === 'button-add' && 'Add New Button'}
            {activeModal === 'button-edit' && 'Edit Button'}
            {activeModal === 'settings' && 'Menu Settings'}
            {activeModal === 'import' && 'Import JSON Data'}
            {activeModal === 'export' && 'Export Sections'}
            {activeModal === 'reorder-sections' && 'Reorder Sections'}
          </h3>
          <button className="modal-close" onClick={onClose}>
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        {/* 1. Add Section Form */}
        {activeModal === 'section-add' && (
          <form onSubmit={handleAddSectionSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="sec-name">Section Name</label>
              <input
                id="sec-name"
                className="form-input"
                type="text"
                placeholder="Enter section name..."
                value={sectionName}
                onChange={(e) => setSectionName(e.target.value)}
                autoFocus
                required
              />
            </div>
            <div className="modal-actions">
              <button type="button" className="btn btn-cancel" onClick={onClose}>Cancel</button>
              <button type="submit" className="btn btn-primary">Submit</button>
            </div>
          </form>
        )}

        {/* 2. Add Button Form */}
        {activeModal === 'button-add' && (
          <form onSubmit={handleAddButtonSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="btn-name">Button Name</label>
              <input
                id="btn-name"
                className="form-input"
                type="text"
                placeholder="Enter button name..."
                value={buttonName}
                onChange={(e) => setButtonName(e.target.value)}
                autoFocus
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="btn-paste">Paste Value</label>
              <textarea
                id="btn-paste"
                className="form-textarea"
                placeholder="Enter copy paste content..."
                value={pasteValue}
                onChange={(e) => setPasteValue(e.target.value)}
                required
              />
            </div>
            <div className="modal-actions">
              <button type="button" className="btn btn-cancel" onClick={onClose}>Cancel</button>
              <button type="submit" className="btn btn-primary">Submit</button>
            </div>
          </form>
        )}

        {/* 3. Edit Button Form */}
        {activeModal === 'button-edit' && (
          <form onSubmit={handleEditButtonSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="edit-btn-name">Button Name</label>
              <input
                id="edit-btn-name"
                className="form-input"
                type="text"
                value={buttonName}
                onChange={(e) => setButtonName(e.target.value)}
                autoFocus
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="edit-btn-paste">Paste Value</label>
              <textarea
                id="edit-btn-paste"
                className="form-textarea"
                value={pasteValue}
                onChange={(e) => setPasteValue(e.target.value)}
                required
              />
            </div>
            <div className="modal-actions">
              <button type="button" className="btn btn-cancel" onClick={onClose}>Cancel</button>
              <button type="submit" className="btn btn-primary">Save Changes</button>
            </div>
          </form>
        )}

        {/* 4. Settings Dashboard */}
        {activeModal === 'settings' && (
          <div className="settings-menu">
            <div className="settings-section">
              <div className="settings-sec-title">Reorder Controls</div>
              <button 
                className="btn btn-cancel" 
                style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
                onClick={() => onReorderSections(sections) /* triggers view switch */}
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <polyline points="4 17 10 11 4 5"></polyline>
                  <polyline points="12 19 20 19"></polyline>
                </svg>
                Manage Sections Order
              </button>
            </div>
            
            <div className="settings-section">
              <div className="settings-sec-title">Import & Export</div>
              <div className="settings-actions">
                <button 
                  className="btn btn-cancel" 
                  style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
                  onClick={() => onAddSection(null, 'import')} // proxy to transition import
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                    <polyline points="17 8 12 3 7 8"></polyline>
                    <line x1="12" y1="3" x2="12" y2="15"></line>
                  </svg>
                  Import
                </button>
                <button 
                  className="btn btn-cancel" 
                  style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
                  onClick={() => onAddSection(null, 'export')} // proxy to transition export
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                    <polyline points="7 10 12 15 17 10"></polyline>
                    <line x1="12" y1="15" x2="12" y2="3"></line>
                  </svg>
                  Export
                </button>
              </div>
            </div>

            <div className="settings-section" style={{ borderLeft: '3px solid var(--danger-color)' }}>
              <div className="settings-sec-title" style={{ color: 'var(--danger-color)' }}>Danger Zone</div>
              <button 
                className="btn btn-danger" 
                style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
                onClick={() => {
                  if (confirm('THIS WILL DELETE ALL OF YOUR SECTIONS AND THEIR BUTTONS!\nAre you sure you want to delete all data?')) {
                    onClearAll();
                    onClose();
                  }
                }}
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
                Delete All Data
              </button>
            </div>
          </div>
        )}

        {/* 5. Import Modal */}
        {activeModal === 'import' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
              Please select a previously exported `.json` file to restore your sections and buttons.
            </p>
            <div className="form-group" style={{ padding: '16px', border: '1px dashed var(--border-color)', borderRadius: '10px', textAlign: 'center' }}>
              <input
                id="file-import"
                type="file"
                accept=".json"
                onChange={handleImportFile}
                style={{ cursor: 'pointer', width: '100%' }}
              />
            </div>
            <div className="modal-actions">
              <button className="btn btn-cancel" onClick={onClose}>Cancel</button>
            </div>
          </div>
        )}

        {/* 6. Export Modal */}
        {activeModal === 'export' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {sections.length === 0 ? (
              <p style={{ textAlign: 'center', color: 'var(--text-secondary)' }}>No sections available to export.</p>
            ) : (
              <>
                <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
                  Select the sections you want to download as a JSON package:
                </p>
                
                <label className="export-item" style={{ fontWeight: '600', paddingBottom: '6px', borderBottom: '1px solid var(--border-color)' }}>
                  <input
                    type="checkbox"
                    checked={allExportSelected}
                    onChange={handleExportToggleAll}
                  />
                  Select All Sections
                </label>

                <div className="export-list">
                  {sections.map((sec, idx) => (
                    <label key={idx} className="export-item">
                      <input
                        type="checkbox"
                        checked={exportSelections[idx] || false}
                        onChange={() => handleExportToggleItem(idx)}
                      />
                      {sec.sectionName}
                    </label>
                  ))}
                </div>
              </>
            )}
            <div className="modal-actions">
              <button className="btn btn-cancel" onClick={onClose}>Cancel</button>
              {sections.length > 0 && (
                <button className="btn btn-primary" onClick={handleExportSubmit}>Confirm & Export</button>
              )}
            </div>
          </div>
        )}

        {/* 7. Reorder Sections Modal */}
        {activeModal === 'reorder-sections' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {tempSections.length === 0 ? (
              <p style={{ textAlign: 'center', color: 'var(--text-secondary)' }}>No sections created to reorder.</p>
            ) : (
              <>
                <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
                  Shift the order of sections using the Up and Down arrows:
                </p>
                <div className="reorder-list">
                  {tempSections.map((sec, idx) => (
                    <div key={idx} className="reorder-item">
                      <div className="reorder-name">{sec.sectionName}</div>
                      <div className="reorder-controls">
                        <button
                          type="button"
                          className="reorder-btn"
                          disabled={idx === 0}
                          onClick={() => moveSection(idx, 'up')}
                          title="Move Up"
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                            <polyline points="18 15 12 9 6 15"></polyline>
                          </svg>
                        </button>
                        <button
                          type="button"
                          className="reorder-btn"
                          disabled={idx === tempSections.length - 1}
                          onClick={() => moveSection(idx, 'down')}
                          title="Move Down"
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                            <polyline points="6 9 12 15 18 9"></polyline>
                          </svg>
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </>
            )}
            <div className="modal-actions">
              <button className="btn btn-cancel" onClick={onClose}>Cancel</button>
              {tempSections.length > 0 && (
                <button className="btn btn-primary" onClick={handleReorderSubmit}>Save Order</button>
              )}
            </div>
          </div>
        )}
      </dialog>
    </div>
  );
};

export default Modals;
