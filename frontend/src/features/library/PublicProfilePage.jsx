import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import ContentLoading from '@/components/ContentLoading';
import { fetchPublicProfile } from '../games/gameApi';
import ProfileIdentity from '@/components/ProfileIdentity';
import LibrarySummary from '@/components/LibrarySummary';
import StatusMessage from '@/components/StatusMessage';
import ProfileFavorites from './ProfileFavorites';
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

  if (loading) return <main className="w-full max-w-[840px] py-8 md:py-12" id="main-content"><ContentLoading label="Cargando perfil público" variant="profile" showIdentity /></main>;
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
      <section aria-label="Resumen público de biblioteca">
        <LibrarySummary total={profile.libraryCount} completed={profile.completedCount} favorites={profile.favoriteCount} />
      </section>
      <ProfileFavorites games={profile.favoriteGames} />
    </main>
  );
}
