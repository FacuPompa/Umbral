import { useEffect, useState } from 'react';
import { Link, Outlet } from 'react-router-dom';
import { useAuth } from '../features/auth/useAuth';

function SunIcon() {
  return (
    <svg aria-hidden="true" fill="none" height="18" viewBox="0 0 24 24" width="18">
      <circle cx="12" cy="12" r="4" stroke="currentColor" strokeWidth="1.8" />
      <path d="M12 2.5v2M12 19.5v2M21.5 12h-2M4.5 12h-2M18.7 5.3l-1.4 1.4M6.7 17.3l-1.4 1.4M18.7 18.7l-1.4-1.4M6.7 6.7 5.3 5.3" stroke="currentColor" strokeLinecap="round" strokeWidth="1.8" />
    </svg>
  );
}

function MoonIcon() {
  return (
    <svg aria-hidden="true" fill="none" height="18" viewBox="0 0 24 24" width="18">
      <path d="M20.5 15.2A8.5 8.5 0 0 1 8.8 3.5 8.5 8.5 0 1 0 20.5 15.2Z" stroke="currentColor" strokeLinejoin="round" strokeWidth="1.8" />
    </svg>
  );
}

function MenuIcon({ open }) {
  return open ? (
    <svg aria-hidden="true" fill="none" height="20" viewBox="0 0 24 24" width="20">
      <path d="m6 6 12 12M18 6 6 18" stroke="currentColor" strokeLinecap="round" strokeWidth="1.8" />
    </svg>
  ) : (
    <svg aria-hidden="true" fill="none" height="20" viewBox="0 0 24 24" width="20">
      <path d="M4 7h16M4 12h16M4 17h16" stroke="currentColor" strokeLinecap="round" strokeWidth="1.8" />
    </svg>
  );
}

function getInitialTheme() {
  const savedTheme = window.localStorage.getItem('umbral-theme');

  if (savedTheme === 'light' || savedTheme === 'dark') {
    return savedTheme;
  }

  return window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
}

export default function AppShell() {
  const [theme, setTheme] = useState(getInitialTheme);
  const [menuOpen, setMenuOpen] = useState(false);
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
  const closeMenu = () => setMenuOpen(false);

  return (
    <div className="app-shell">
      <a className="skip-link" href="#main-content">Saltar al contenido</a>
      <header className="site-header">
        <Link aria-label="Umbral" className="site-brand" to="/">
          <img alt="" src="/favicon.svg" />
        </Link>

        <nav className={menuOpen ? 'site-nav is-open' : 'site-nav'} id="site-navigation" aria-label="Navegación principal">
          <a href="/#catalogo" onClick={closeMenu}>Catálogo</a>
          <a href="/#como-funciona" onClick={closeMenu}>Cómo funciona</a>
          {!loading && user && <Link className="site-nav-suggestion" onClick={closeMenu} to="/suggestions/new">Sugerir juego</Link>}
          {!loading && user?.role === 'MODERATOR' && <Link className="site-nav-moderation" onClick={closeMenu} to="/moderation/games">Moderación</Link>}
          {!loading && !user && (
            <div className="account-actions">
              <Link className="future-access future-access-login" onClick={closeMenu} to="/login">Iniciar sesión</Link>
              <Link className="future-access future-access-register" onClick={closeMenu} to="/register">Crear cuenta</Link>
            </div>
          )}
          {!loading && user && (
            <div className="account-actions">
              <Link aria-label={`Abrir el perfil de ${user.handle}`} className="account-profile-link" onClick={closeMenu} to="/me">
                <span aria-hidden="true" className="account-avatar">{user.handle.slice(0, 1).toUpperCase()}</span>
                <span className="account-handle">{user.handle}</span>
              </Link>
              <button className="future-access future-access-login" onClick={handleLogout} type="button">Cerrar sesión</button>
            </div>
          )}
        </nav>
        <div className="header-quick-actions">
          <button
            className="icon-button theme-toggle"
            type="button"
            aria-label={isDarkTheme ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro'}
            title={isDarkTheme ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro'}
            onClick={toggleTheme}
          >
            {isDarkTheme ? <SunIcon /> : <MoonIcon />}
          </button>
          <button
            aria-controls="site-navigation"
            aria-expanded={menuOpen}
            aria-label={menuOpen ? 'Cerrar menú' : 'Abrir menú'}
            className="icon-button menu-toggle"
            onClick={() => setMenuOpen((open) => !open)}
            type="button"
          >
            <MenuIcon open={menuOpen} />
          </button>
        </div>
      </header>

      <Outlet />
    </div>
  );
}
