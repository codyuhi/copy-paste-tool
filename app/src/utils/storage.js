export const loadSections = () => {
  try {
    const data = localStorage.getItem('allSections');
    if (!data) return [];
    const parsed = JSON.parse(data);
    return Array.isArray(parsed) ? parsed : [];
  } catch (err) {
    console.error('Failed to parse allSections from localStorage:', err);
    return [];
  }
};

export const saveSections = (sections) => {
  try {
    localStorage.setItem('allSections', JSON.stringify(sections));
  } catch (err) {
    console.error('Failed to save allSections to localStorage:', err);
  }
};

export const loadFavorites = () => {
  try {
    const data = localStorage.getItem('favoriteButtons');
    if (!data) return [];
    const parsed = JSON.parse(data);
    return Array.isArray(parsed) ? parsed : [];
  } catch (err) {
    console.error('Failed to parse favoriteButtons from localStorage:', err);
    return [];
  }
};

export const saveFavorites = (favorites) => {
  try {
    localStorage.setItem('favoriteButtons', JSON.stringify(favorites));
  } catch (err) {
    console.error('Failed to save favoriteButtons to localStorage:', err);
  }
};
