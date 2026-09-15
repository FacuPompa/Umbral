import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import LoadingIndicator from '../../components/LoadingIndicator';
import { fetchPublicProfile } from '../games/gameApi';
import ProfileIdentity from '@/components/ProfileIdentity';
import LibrarySummary from '@/components/LibrarySummary';
import StatusMessage from '@/components/StatusMessage';
import GameRow from '@/components/GameRow';
import PageHeading from '@/components/PageHeading';
import { Button } from '@/components/ui/button';

export default function PublicProfilePage() {
  const { handle } = useParams();
  return <PublicProfile key={handle} handle={handle} />;
}

function PublicProfile({ handle }) {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let active = true;

    async function loadProfile() {
      setLoading(true);
      setError(null);
      try {
        const response = await fetchPublicProfile(handle);
        if (active) setProfile(response);
      } catch (requestError) {
        if (active) setError(requestError.message);
      } finally {
        if (active) setLoading(false);
      }
    }

    loadProfile();
    return () => { active = false; };
  }, [handle]);

  if (loading) return <main className="py-12" id="main-content"><LoadingIndicator label="Cargando perfil público" showLabel /></main>;
  if (error || !profile) return (
    <main className="grid max-w-[840px] gap-6 py-12" id="main-content">
      <PageHeading title="Perfil no disponible" />
      <StatusMessage kind="error">{error ?? 'No pudimos cargar este perfil.'}</StatusMessage>
      <div><Button asChild variant="outline"><Link to="/#catalogo">Volver al catálogo</Link></Button></div>
    </main>
  );

  return (
    <main className="grid w-full max-w-[840px] gap-8 py-8 md:gap-10 md:py-12" id="main-content">
      <ProfileIdentity handle={profile.handle} description="Su selección de juegos en Umbral. El progreso y los estados individuales de su biblioteca son privados." />
      <section className="border-y border-border py-6" aria-label="Resumen público de biblioteca">
        <LibrarySummary total={profile.libraryCount} completed={profile.completedCount} favorites={profile.favoriteCount} />
      </section>
      <section className="grid gap-5" aria-labelledby="public-profile-favorites-title">
        <h2 className="text-2xl leading-[30px] font-semibold tracking-normal" id="public-profile-favorites-title">Juegos favoritos</h2>
        {profile.favoriteGames.length === 0 ? <StatusMessage>Todavía no marcó juegos favoritos.</StatusMessage> : (
          <ol className="border-t border-border">
            {profile.favoriteGames.map((game) => (
              <li key={game.gameId}><GameRow gameId={game.gameId} title={game.gameTitle} coverImageUrl={game.coverImageUrl} /></li>
            ))}
          </ol>
        )}
      </section>
    </main>
  );
}
