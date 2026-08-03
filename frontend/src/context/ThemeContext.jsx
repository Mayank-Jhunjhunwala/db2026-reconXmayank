// TICKET-ADV124 — ThemeProvider: context flips data-theme; CSS owns colours.
import React, { createContext, useContext, useState } from 'react';

const ThemeContext = createContext({ theme: 'light', toggle: () => {} });

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(() => localStorage.getItem('reconx-theme') || 'light');

  React.useEffect(() => {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem('reconx-theme', theme);
  }, [theme]);

  const toggle = () => {
    setTheme((prev) => (prev === 'light' ? 'dark' : 'light'));
  };

  return (
    <ThemeContext.Provider value={{ theme, toggle }}>
      {children}
    </ThemeContext.Provider>
  );
}

export const useTheme = () => useContext(ThemeContext);
