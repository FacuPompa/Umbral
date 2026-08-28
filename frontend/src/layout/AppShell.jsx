import { useEffect, useState } from 'react';
import { Link, Outlet } from 'react-router-dom';

function getInitialTheme() {
  const savedTheme = window.localStorage.getItem('umbral-theme');

  if (savedTheme === 'light' || savedTheme === 'dark') {
    return savedTheme;
  }

  return window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
}

export default function AppShell() {
  const [theme, setTheme] = useState(getInitialTheme);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    window.localStorage.setItem('umbral-theme', theme);
  }, [theme]);

  function toggleTheme() {
    setTheme((currentTheme) => (currentTheme === 'dark' ? 'light' : 'dark'));
  }

  const isDarkTheme = theme === 'dark';

  return (
    <div className="app-shell">
      <header className="site-header">
        <Link className="site-brand" to="/">Umbral</Link>

        <nav className="site-nav" aria-label="Navegación principal">
          <Link to="/#catalogo">Catálogo</Link>
          <button
            className="theme-toggle"
            type="button"
            aria-label={isDarkTheme ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro'}
            aria-pressed={!isDarkTheme}
            onClick={toggleTheme}
          >
            <span aria-hidden="true" className="theme-toggle-track">
              <span className="theme-toggle-thumb" />
            </span>
            <span>{isDarkTheme ? 'Claro' : 'Oscuro'}</span>
          </button>
          <button className="future-access future-access-login" disabled type="button">
            Iniciar sesión
            <span>Próximamente</span>
          </button>
          <button className="future-access future-access-register" disabled type="button">
            Crear cuenta
            <span>Próximamente</span>
          </button>
        </nav>
      </header>

      <Outlet />
    </div>
  );
}
