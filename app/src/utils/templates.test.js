import { describe, it, expect, beforeEach } from 'vitest';
import {
  extractPlaceholders,
  hasPlaceholders,
  interpolateTemplate,
  getTemplateMemory,
  saveTemplateMemory,
  formatVariableLabel
} from './templates';

describe('templates utility', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  describe('extractPlaceholders', () => {
    it('returns empty array when text has no placeholders', () => {
      expect(extractPlaceholders('Plain text without any variables')).toEqual([]);
      expect(extractPlaceholders('')).toEqual([]);
      expect(extractPlaceholders(null)).toEqual([]);
    });

    it('extracts single and multiple placeholders', () => {
      expect(extractPlaceholders('Hello {{name}}!')).toEqual(['name']);
      expect(extractPlaceholders('Hello {{ name }}, ticket {{ ticket_id }} in {{env}}.'))
        .toEqual(['name', 'ticket_id', 'env']);
    });

    it('deduplicates repeating placeholders', () => {
      expect(extractPlaceholders('{{user}} said hello to {{user}} at {{time}}.'))
        .toEqual(['user', 'time']);
    });

    it('supports hyphens and dots in variable names', () => {
      expect(extractPlaceholders('Deploy {{app-name}} to {{cluster.domain}}'))
        .toEqual(['app-name', 'cluster.domain']);
    });
  });

  describe('hasPlaceholders', () => {
    it('detects presence of placeholders correctly', () => {
      expect(hasPlaceholders('Static string')).toBe(false);
      expect(hasPlaceholders('Static with single {brace}')).toBe(false);
      expect(hasPlaceholders('Template with {{var}}')).toBe(true);
      expect(hasPlaceholders('Template with {{ spaced_var }}')).toBe(true);
    });
  });

  describe('interpolateTemplate', () => {
    it('interpolates variables accurately', () => {
      const template = 'Hello {{name}}, your balance is {{amount}}.';
      const variables = { name: 'Cody', amount: '$42.00' };
      expect(interpolateTemplate(template, variables)).toBe('Hello Cody, your balance is $42.00.');
    });

    it('replaces all occurrences of the same variable', () => {
      const template = '{{name}} is awesome! Thanks {{name}}!';
      expect(interpolateTemplate(template, { name: 'Lena' })).toBe('Lena is awesome! Thanks Lena!');
    });

    it('retains placeholder if variable value is not provided', () => {
      const template = 'ID: {{id}}, User: {{user}}';
      expect(interpolateTemplate(template, { id: '123' })).toBe('ID: 123, User: {{user}}');
    });

    it('clears missing placeholders if fallbackToEmpty is true', () => {
      const template = 'ID: {{id}}, User: {{user}}';
      expect(interpolateTemplate(template, { id: '123' }, true)).toBe('ID: 123, User: ');
    });
  });

  describe('getTemplateMemory and saveTemplateMemory', () => {
    it('persists and loads template memory in localStorage', () => {
      expect(getTemplateMemory()).toEqual({});
      saveTemplateMemory({ name: 'Cody', namespace: 'default' });
      expect(getTemplateMemory()).toEqual({ name: 'Cody', namespace: 'default' });
      saveTemplateMemory({ namespace: 'kube-system', cluster: 'minipc' });
      expect(getTemplateMemory()).toEqual({
        name: 'Cody',
        namespace: 'kube-system',
        cluster: 'minipc'
      });
    });
  });

  describe('formatVariableLabel', () => {
    it('formats snake_case and kebab-case into Title Case', () => {
      expect(formatVariableLabel('customer_name')).toBe('Customer Name');
      expect(formatVariableLabel('incident-id')).toBe('Incident Id');
      expect(formatVariableLabel('name')).toBe('Name');
    });
  });
});
