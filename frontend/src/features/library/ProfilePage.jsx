import { useEffect, useState } from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import LoadingIndicator from '../../components/LoadingIndicator';
import { useAuth } from '../auth/useAuth';
import { fetchCurrentUserLibrary } from '../games/gameApi';

export default function ProfilePage() {
  const { user, loading: loadingUser } = useAuth();
  const location = useLocation();
  const [library, setLibrary] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!user) return;

    let active = true;
    async function loadProfile() {
      setLoading(true);
      setError(null);
      try {
        const games = await fetchCurrentUserLibrary();
        if (active) setLibrary(games);
      } catch (requestError) {
        if (active) setError(requestError.message);
      } finally {
        if (active) setLoading(false);
      }
    }

    loadProfile();
    return () => { active = false; };
  }, [user]);

  if (loadingUser) {
    return <main className="page-state" id="main-content"><LoadingIndicator label="Recuperando sesión" /></main>;
  }

  if (!user) {
    return <Navigate replace state={{ from: location }} to="/login" />;
  }

  const completedCount = library.filter((game) => game.status === 'COMPLETED').length;
  const favoriteCount = library.filter((game) => game.favorite).length;

  return (
    <main className="page-main profile-page" id="main-content">
      <header className="profile-header">
        <div aria-hidden="true" className="profile-avatar">{user.handle.slice(0, 1).toUpperCase()}</div>
        <div className="profile-header-copy">
          <h1>{user.handle}</h1>
          <p>Tu espacio para seguir juegos y volver a las conversaciones que ya podés leer.</p>
        </div>
      </header>

      {loading && <div className="inline-loader"><LoadingIndicator label="Cargando perfil" /></div>}
      {error && <p className="status-message status-message-error" role="alert">{error}</p>}
      {!loading && !error && (
        <section className="profile-overview" aria-labelledby="profile-library-title">
          <div>
            <h2 id="profile-library-title">Tu biblioteca</h2>
            <p>Organizá los juegos que querés jugar, los que estás siguiendo y los que terminaste.</p>
          </div>
          <dl className="profile-stats">
            <div><dt>En biblioteca</dt><dd>{library.length}</dd></div>
            <div><dt>Favoritos</dt><dd>{favoriteCount}</dd></div>
            <div><dt>Terminados</dt><dd>{completedCount}</dd></div>
          </dl>
          <Link className="button-primary" to="/me/library">Ver mi biblioteca</Link>
        </section>
      )}
    </main>
  );
}
