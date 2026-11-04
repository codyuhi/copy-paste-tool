import React, { useState, useEffect, useRef } from 'react';
import { interpolateTemplate, getTemplateMemory, saveTemplateMemory, formatVariableLabel } from '../utils/templates';

const TemplateModal = ({
  isOpen,
  onClose,
  templateData, // { name, pasteValue, placeholders }
  onConfirm
}) => {
  const [variables, setVariables] = useState({});
  const firstInputRef = useRef(null);

  useEffect(() => {
    if (isOpen && templateData && templateData.placeholders) {
      const memory = getTemplateMemory();
      const initial = {};
      templateData.placeholders.forEach((p) => {
        initial[p] = memory[p] || '';
      });
      setVariables(initial);

      // Focus first input
      setTimeout(() => {
        firstInputRef.current?.focus();
        firstInputRef.current?.select();
      }, 50);
    }
  }, [isOpen, templateData]);

  if (!isOpen || !templateData) return null;

  const handleChange = (placeholder, val) => {
    setVariables((prev) => ({
      ...prev,
      [placeholder]: val
    }));
  };

  const handleSubmit = (e) => {
    if (e) e.preventDefault();
    saveTemplateMemory(variables);
    const finalResult = interpolateTemplate(templateData.pasteValue, variables, false);
    onConfirm(finalResult);
  };

  const previewText = interpolateTemplate(templateData.pasteValue, variables, false);

  return (
    <div className="modal-backdrop show" onClick={onClose} role="dialog" aria-modal="true">
      <div 
        className="modal-content template-modal" 
        onClick={(e) => e.stopPropagation()}
        style={{ maxWidth: '540px' }}
      >
        <div className="modal-header">
          <div className="modal-title-wrap">
            <h3 className="modal-title">Fill Template</h3>
            <span className="template-snippet-name">{templateData.name}</span>
          </div>
          <button 
            type="button" 
            className="btn-modal-close" 
            onClick={onClose}
            aria-label="Close template prompt"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            <p className="template-instructions">
              Enter values for each placeholder. Values are remembered for quick reuse.
            </p>

            <div className="template-inputs-grid">
              {templateData.placeholders.map((placeholder, idx) => {
                const inputId = `template-var-${placeholder}`;
                return (
                  <div key={placeholder} className="form-group template-var-group">
                    <label htmlFor={inputId} className="form-label" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span>{formatVariableLabel(placeholder)}</span>
                      <code className="var-token-pill">{`{{${placeholder}}}`}</code>
                    </label>
                    <input
                      ref={idx === 0 ? firstInputRef : null}
                      id={inputId}
                      type="text"
                      className="form-input"
                      value={variables[placeholder] || ''}
                      onChange={(e) => handleChange(placeholder, e.target.value)}
                      placeholder={`Enter ${placeholder}...`}
                    />
                  </div>
                );
              })}
            </div>

            <div className="template-preview-container">
              <div className="template-preview-header">
                <span>Result Preview</span>
              </div>
              <pre className="template-preview-box">{previewText}</pre>
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-cancel" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="btn btn-submit">
              <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" style={{ marginRight: '6px' }}>
                <rect width="14" height="14" x="8" y="8" rx="2" ry="2"></rect>
                <path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2"></path>
              </svg>
              Copy to Clipboard
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default TemplateModal;
