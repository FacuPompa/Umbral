import { useEffect, useState } from 'react';
import { Link, Outlet } from 'react-router-dom';
import { useAuth } from '../features/auth/useAuth';

function getInitialTheme() {
  const savedTheme = window.localStorage.getItem('umbral-theme');

  if (savedTheme === 'light' || savedTheme === 'dark') {
    return savedTheme;
  }

  return window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
}

export default function AppShell() {
  const [theme, setTheme] = useState(getInitialTheme);
  const { user, loading, logout } = useAuth();

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    window.localStorage.setItem('umbral-theme', theme);
  }, [theme]);

  function toggleTheme() {
    setTheme((currentTheme) => (currentTheme === 'dark' ? 'light' : 'dark'));
  }

  async function handleLogout() {
    try {
      await logout();
    } catch (error) {
      console.error('No se pudo cerrar la sesión.', error);
    }
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
          {!loading && user && <Link className="site-nav-suggestion" to="/suggestions/new">Sugerir juego</Link>}
          {!loading && user?.role === 'MODERATOR' && <Link className="site-nav-moderation" to="/moderation/games">Moderación</Link>}
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
          {!loading && !user && (
            <div className="account-actions">
              <Link className="future-access future-access-login" to="/login">Iniciar sesión</Link>
              <Link className="future-access future-access-register" to="/register">Crear cuenta</Link>
            </div>
          )}
          {!loading && user && (
            <div className="account-actions">
              <span className="account-handle">{user.handle}</span>
              <button className="future-access future-access-login" onClick={handleLogout} type="button">Cerrar sesión</button>
            </div>
          )}
        </nav>
      </header>

      <Outlet />
    </div>
  );
}
