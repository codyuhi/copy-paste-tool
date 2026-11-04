/**
 * Utility functions for variable template placeholders: {{var_name}}
 */

const PLACEHOLDER_REGEX = /\{\{\s*([a-zA-Z0-9_\-\.]+)\s*\}\}/g;
const TEMPLATE_MEMORY_KEY = 'cpt_template_vars';

/**
 * Extracts unique placeholder variable names from a text string.
 * e.g. "Hello {{name}}, your ticket {{id}} is updated. Contact {{name}}."
 * returns ['name', 'id']
 */
export function extractPlaceholders(text) {
  if (!text || typeof text !== 'string') return [];
  const regex = new RegExp(PLACEHOLDER_REGEX.source, 'g');
  const matches = [];
  let match;
  while ((match = regex.exec(text)) !== null) {
    const varName = match[1].trim();
    if (varName && !matches.includes(varName)) {
      matches.push(varName);
    }
  }
  return matches;
}

/**
 * Checks whether a text contains one or more placeholders.
 */
export function hasPlaceholders(text) {
  if (!text || typeof text !== 'string') return false;
  const regex = new RegExp(PLACEHOLDER_REGEX.source, 'g');
  return regex.test(text);
}

/**
 * Interpolates variables into a template string.
 * If a variable is provided in the `variables` map, its value is used.
 * Otherwise, the placeholder itself is retained (or empty if fallbackToEmpty=true).
 */
export function interpolateTemplate(text, variables = {}, fallbackToEmpty = false) {
  if (!text || typeof text !== 'string') return '';
  const regex = new RegExp(PLACEHOLDER_REGEX.source, 'g');
  return text.replace(regex, (match, rawVarName) => {
    const varName = rawVarName.trim();
    if (variables[varName] !== undefined && variables[varName] !== null && variables[varName] !== '') {
      return variables[varName];
    }
    return fallbackToEmpty ? '' : match;
  });
}

/**
 * Retrieves the stored variable memory from localStorage.
 */
export function getTemplateMemory() {
  try {
    const raw = localStorage.getItem(TEMPLATE_MEMORY_KEY);
    return raw ? JSON.parse(raw) : {};
  } catch (e) {
    console.warn('Failed to parse template memory:', e);
    return {};
  }
}

/**
 * Updates stored variable memory in localStorage.
 */
export function saveTemplateMemory(newValues = {}) {
  try {
    const current = getTemplateMemory();
    const updated = { ...current, ...newValues };
    localStorage.setItem(TEMPLATE_MEMORY_KEY, JSON.stringify(updated));
    return updated;
  } catch (e) {
    console.warn('Failed to save template memory:', e);
    return newValues;
  }
}

/**
 * Formats a variable name for clean UI label presentation.
 * e.g. 'customer_name' -> 'Customer Name', 'ticket-id' -> 'Ticket Id'
 */
export function formatVariableLabel(varName) {
  if (!varName) return '';
  return varName
    .replace(/[_-]+/g, ' ')
    .replace(/\b\w/g, (char) => char.toUpperCase());
}
