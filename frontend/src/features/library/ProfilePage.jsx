import { useEffect, useState } from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import ProfileIdentity from '@/components/ProfileIdentity';
import LibrarySummary from '@/components/LibrarySummary';
import StatusMessage from '@/components/StatusMessage';
import { Button } from '@/components/ui/button';
import LoadingIndicator from '../../components/LoadingIndicator';
import ContentLoading from '@/components/ContentLoading';
import { useAuth } from '../auth/useAuth';
import { fetchCurrentUserLibrary } from '../games/gameApi';
import ProfileFavorites from './ProfileFavorites';

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
    return <main className="grid min-h-[65vh] place-items-center text-center text-muted-foreground" id="main-content"><LoadingIndicator label="Recuperando sesión" /></main>;
  }

  if (!user) {
    return <Navigate replace state={{ from: location }} to="/login" />;
  }

  const completedCount = library.filter((game) => game.status === 'COMPLETED').length;
  const favoriteCount = library.filter((game) => game.favorite).length;

  return (
    <main className="grid w-full max-w-[840px] gap-8 py-8 md:gap-10 md:py-12" id="main-content">
      <ProfileIdentity handle={user.handle} description="Tu espacio para seguir juegos y volver a las conversaciones que ya podés leer.">
        <Button asChild><Link to="/me/library">Ver mi biblioteca</Link></Button>
        <Button asChild variant="outline"><Link to={`/users/${encodeURIComponent(user.handle)}`}>Ver perfil público</Link></Button>
      </ProfileIdentity>
      {loading && <ContentLoading label="Cargando perfil" variant="profile" />}
      {error && <StatusMessage kind="error">{error}</StatusMessage>}
      {!loading && !error && (
        <>
          <section aria-label="Resumen de tu biblioteca"><LibrarySummary total={library.length} completed={completedCount} favorites={favoriteCount} /></section>
          <ProfileFavorites games={library.filter((game) => game.favorite)} personal />
        </>
      )}
    </main>
  );
}
