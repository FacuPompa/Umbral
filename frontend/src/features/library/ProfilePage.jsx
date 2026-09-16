import { useEffect, useState } from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import ProfileIdentity from '@/components/ProfileIdentity';
import LibrarySummary from '@/components/LibrarySummary';
import StatusMessage from '@/components/StatusMessage';
import { Button } from '@/components/ui/button';
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
    return <main className="grid min-h-[65vh] place-items-center text-center text-muted-foreground" id="main-content"><LoadingIndicator label="Recuperando sesión" /></main>;
  }

  if (!user) {
    return <Navigate replace state={{ from: location }} to="/login" />;
  }

  const completedCount = library.filter((game) => game.status === 'COMPLETED').length;
  const favoriteCount = library.filter((game) => game.favorite).length;

  return (
    <main className="grid w-full max-w-[840px] gap-8 py-8 md:gap-10 md:py-12" id="main-content">
      <ProfileIdentity handle={user.handle} description="Tu espacio para seguir juegos y volver a las conversaciones que ya podés leer." />
      <div><Button asChild variant="outline"><Link to={`/users/${encodeURIComponent(user.handle)}`}>Ver perfil público</Link></Button></div>
      {loading && <LoadingIndicator label="Cargando perfil" showLabel />}
      {error && <StatusMessage kind="error">{error}</StatusMessage>}
      {!loading && !error && (
        <section className="grid justify-items-start gap-6 border-t border-border pt-8" aria-labelledby="profile-library-title">
          <div className="grid gap-3">
            <h2 className="text-2xl leading-[30px] font-semibold tracking-normal" id="profile-library-title">Tu biblioteca</h2>
            <p className="text-base leading-6 text-muted-foreground">Organizá los juegos que querés jugar, los que estás siguiendo y los que terminaste.</p>
          </div>
          <LibrarySummary total={library.length} completed={completedCount} favorites={favoriteCount} />
          <Button asChild><Link to="/me/library">Ver mi biblioteca</Link></Button>
        </section>
      )}
    </main>
  );
}
