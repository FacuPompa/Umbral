import { useEffect, useMemo, useState } from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import LoadingIndicator from '../../components/LoadingIndicator';
import { useAuth } from '../auth/useAuth';
import {
  fetchCurrentUserLibrary,
  removeGameFromCurrentUserLibrary,
  updateCurrentUserLibraryGameFavorite,
  updateCurrentUserLibraryGameStatus,
} from '../games/gameApi';
import { getGameArtwork, getGameInitials } from '../games/gameArtwork';
import { libraryStatusOptions } from './libraryLabels';

const filters = [
  { value: 'ALL', label: 'Todos' },
  ...libraryStatusOptions,
];

export default function LibraryPage() {
  const { user, loading: loadingUser } = useAuth();
  const location = useLocation();
  const [library, setLibrary] = useState([]);
  const [activeFilter, setActiveFilter] = useState('ALL');
  const [favoritesOnly, setFavoritesOnly] = useState(false);
  const [loading, setLoading] = useState(true);
  const [workingGameId, setWorkingGameId] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!user) return;

    let active = true;
    async function loadLibrary() {
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

    loadLibrary();
    return () => { active = false; };
  }, [user]);

  const visibleGames = useMemo(() => library.filter((game) => {
    const matchesStatus = activeFilter === 'ALL' || game.status === activeFilter;
    return matchesStatus && (!favoritesOnly || game.favorite);
  }), [activeFilter, favoritesOnly, library]);

  if (loadingUser) {
    return <main className="page-state" id="main-content"><LoadingIndicator label="Recuperando sesión" /></main>;
  }

  if (!user) {
    return <Navigate replace state={{ from: location }} to="/login" />;
  }

  async function changeStatus(gameId, status) {
    setWorkingGameId(gameId);
    setError(null);
    try {
      const updatedGame = await updateCurrentUserLibraryGameStatus(gameId, status);
      setLibrary((currentLibrary) => currentLibrary.map((game) => game.gameId === gameId ? updatedGame : game));
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setWorkingGameId(null);
    }
  }

  async function changeFavorite(game) {
    setWorkingGameId(game.gameId);
    setError(null);
    try {
      const updatedGame = await updateCurrentUserLibraryGameFavorite(game.gameId, !game.favorite);
      setLibrary((currentLibrary) => currentLibrary.map((item) => item.gameId === game.gameId ? updatedGame : item));
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setWorkingGameId(null);
    }
  }

  async function removeGame(game) {
    if (!window.confirm(`¿Quitar ${game.gameTitle} de tu biblioteca? Tu progreso no se va a borrar.`)) return;

    setWorkingGameId(game.gameId);
    setError(null);
    try {
      await removeGameFromCurrentUserLibrary(game.gameId);
      setLibrary((currentLibrary) => currentLibrary.filter((item) => item.gameId !== game.gameId));
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setWorkingGameId(null);
    }
  }

  return (
    <main className="page-main library-page" id="main-content">
      <header className="page-heading library-heading">
        <div>
          <h1>Mi biblioteca</h1>
          <p>Guardá los juegos que querés jugar y mantené tu colección personal al día.</p>
        </div>
        <Link className="text-action" to="/#catalogo">Explorar catálogo</Link>
      </header>

      <div className="library-filters" aria-label="Filtros de biblioteca">
        <div className="library-filter-list" role="group" aria-label="Filtrar por estado">
          {filters.map((filter) => (
            <button
              aria-pressed={activeFilter === filter.value}
              className={activeFilter === filter.value ? 'library-filter is-active' : 'library-filter'}
              key={filter.value}
              onClick={() => setActiveFilter(filter.value)}
              type="button"
            >
              {filter.label}
            </button>
          ))}
        </div>
        <label className="favorites-filter">
          <input checked={favoritesOnly} onChange={(event) => setFavoritesOnly(event.target.checked)} type="checkbox" />
          Solo favoritos
        </label>
      </div>

      {loading && <div className="inline-loader"><LoadingIndicator label="Cargando biblioteca" /></div>}
      {error && <p className="status-message status-message-error" role="alert">{error}</p>}
      {!loading && !error && library.length === 0 && (
        <section className="library-empty-state">
          <h2>Tu biblioteca está vacía</h2>
          <p>Cuando marques un progreso, el juego aparecerá acá. También vas a poder organizarlo por estado y favoritos.</p>
          <Link className="button-primary" to="/#catalogo">Explorar juegos</Link>
        </section>
      )}
      {!loading && !error && library.length > 0 && visibleGames.length === 0 && (
        <p className="status-message">No hay juegos que coincidan con este filtro.</p>
      )}
      {!loading && !error && visibleGames.length > 0 && (
        <ol className="library-list">
          {visibleGames.map((game) => {
            const artwork = game.coverImageUrl ?? getGameArtwork(game.gameTitle);
            const isWorking = workingGameId === game.gameId;

            return (
              <li key={game.gameId}>
                <article className="library-item">
                  <Link aria-label={`Abrir ${game.gameTitle}`} className="library-artwork" to={`/games/${game.gameId}`}>
                    {artwork ? <img alt={`Arte de ${game.gameTitle}`} src={artwork} /> : <span aria-hidden="true">{getGameInitials(game.gameTitle)}</span>}
                  </Link>
                  <div className="library-copy">
                    <div className="library-title-row">
                      <h2>{game.gameTitle}</h2>
                      {game.favorite && <span className="library-favorite-label">Favorito</span>}
                    </div>
                    <p>{game.checkpointLabel ? `Avance: ${game.checkpointLabel}` : 'Todavía no marcaste un checkpoint.'}</p>
                  </div>
                  <div className="library-controls">
                    <label>
                      Estado
                      <select
                        disabled={isWorking}
                        onChange={(event) => changeStatus(game.gameId, event.target.value)}
                        value={game.status}
                      >
                        {libraryStatusOptions.map((status) => <option key={status.value} value={status.value}>{status.label}</option>)}
                      </select>
                    </label>
                    <div className="library-actions">
                      <button className="library-action" disabled={isWorking} onClick={() => changeFavorite(game)} type="button">
                        {game.favorite ? 'Quitar favorito' : 'Marcar favorito'}
                      </button>
                      <button className="library-remove-button" disabled={isWorking} onClick={() => removeGame(game)} type="button">Quitar</button>
                    </div>
                  </div>
                </article>
              </li>
            );
          })}
        </ol>
      )}
    </main>
  );
}
