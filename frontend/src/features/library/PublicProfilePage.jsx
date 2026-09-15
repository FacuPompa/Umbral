import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import LoadingIndicator from '../../components/LoadingIndicator';
import { fetchPublicProfile } from '../games/gameApi';
import { getGameArtwork, getGameInitials } from '../games/gameArtwork';

export default function PublicProfilePage() {
  const { handle } = useParams();
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

  if (loading) {
    return <main className="page-state" id="main-content"><LoadingIndicator label="Cargando perfil público" /></main>;
  }

  if (error || !profile) {
    return (
      <main className="page-main public-profile-page" id="main-content">
        <section className="library-empty-state">
          <h1>Perfil no disponible</h1>
          <p>{error ?? 'No pudimos cargar este perfil.'}</p>
          <Link className="button-secondary" to="/#catalogo">Volver al catálogo</Link>
        </section>
      </main>
    );
  }

  return (
    <main className="page-main public-profile-page" id="main-content">
      <header className="profile-header">
        <div aria-hidden="true" className="profile-avatar">{profile.handle.slice(0, 1).toUpperCase()}</div>
        <div className="profile-header-copy">
          <p className="profile-kicker">Perfil público</p>
          <h1>{profile.handle}</h1>
          <p>Una vista compartida de su relación con los juegos en Umbral.</p>
        </div>
      </header>

      <section className="profile-overview" aria-labelledby="public-profile-summary-title">
        <div>
          <h2 id="public-profile-summary-title">Su biblioteca</h2>
          <p>Los estados de cada juego y el progreso narrativo siguen siendo personales.</p>
        </div>
        <dl className="profile-stats">
          <div><dt>En biblioteca</dt><dd>{profile.libraryCount}</dd></div>
          <div><dt>Favoritos</dt><dd>{profile.favoriteCount}</dd></div>
          <div><dt>Terminados</dt><dd>{profile.completedCount}</dd></div>
        </dl>
      </section>

      <section className="profile-favorites" aria-labelledby="public-profile-favorites-title">
        <div className="section-heading-row">
          <div>
            <p className="section-eyebrow">Selección personal</p>
            <h2 id="public-profile-favorites-title">Juegos favoritos</h2>
          </div>
          <span>{profile.favoriteCount} {profile.favoriteCount === 1 ? 'juego' : 'juegos'}</span>
        </div>

        {profile.favoriteGames.length === 0 ? (
          <p className="status-message">Todavía no marcó juegos favoritos.</p>
        ) : (
          <ol className="public-favorite-list">
            {profile.favoriteGames.map((game) => {
              const artwork = game.coverImageUrl ?? getGameArtwork(game.gameTitle);
              return (
                <li key={game.gameId}>
                  <Link className="public-favorite-item" to={`/games/${game.gameId}`}>
                    <span className="public-favorite-artwork">
                      {artwork ? <img alt={`Arte de ${game.gameTitle}`} src={artwork} /> : <span aria-hidden="true">{getGameInitials(game.gameTitle)}</span>}
                    </span>
                    <span>{game.gameTitle}</span>
                    <span aria-hidden="true">→</span>
                  </Link>
                </li>
              );
            })}
          </ol>
        )}
      </section>
    </main>
  );
}
