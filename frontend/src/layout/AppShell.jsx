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
      <a className="skip-link" href="#main-content">Saltar al contenido</a>
      <header className="site-header">
        <Link className="site-brand" to="/">UMBRAL/</Link>

        <nav className="site-nav" aria-label="Navegación principal">
          <a href="/#catalogo">Catálogo</a>
          <a href="/#como-funciona">Cómo funciona</a>
          <button
            className="theme-toggle"
            type="button"
            aria-label={isDarkTheme ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro'}
            onClick={toggleTheme}
          >
            <span aria-hidden="true" className="theme-toggle-track">
              <span className="theme-toggle-thumb" />
            </span>
            <span>{isDarkTheme ? 'Claro' : 'Oscuro'}</span>
          </button>
          <button className="future-access future-access-login" disabled title="Próximamente" type="button">Iniciar sesión</button>
          <button className="future-access future-access-register" disabled title="Próximamente" type="button">Crear cuenta</button>
        </nav>
      </header>

      <Outlet />
    </div>
  );
}
