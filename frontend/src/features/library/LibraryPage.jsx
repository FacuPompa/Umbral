import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import LoadingIndicator from '../../components/LoadingIndicator';
import ContentLoading from '@/components/ContentLoading';
import { useAuth } from '../auth/useAuth';
import {
  fetchCurrentUserLibrary,
  removeGameFromCurrentUserLibrary,
  updateCurrentUserLibraryGameFavorite,
  updateCurrentUserLibraryGameStatus,
} from '../games/gameApi';
import { Star } from 'lucide-react';
import { LazyMotion, m, useReducedMotion } from 'motion/react';
import ConfirmDialog from '@/components/ConfirmDialog';
import PageHeading from '@/components/PageHeading';
import LibraryGameRow from './LibraryGameRow';
import StatusMessage from '@/components/StatusMessage';
import { Button } from '@/components/ui/button';
import { libraryStatusOptions } from './libraryLabels';

const loadLayoutFeatures = () => import('@/lib/motionLayoutFeatures').then((module) => module.default);

const filters = [
  { value: 'ALL', label: 'Todos' },
  ...libraryStatusOptions,
];

export default function LibraryPage() {
  const { user, loading: loadingUser } = useAuth();
  const location = useLocation();
  const mainRef = useRef(null);
  const reduced = useReducedMotion();
  const [pendingRemoval, setPendingRemoval] = useState(null);
  const [removeError, setRemoveError] = useState(null);
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
    return <main className="grid min-h-[65vh] place-items-center text-center text-muted-foreground" id="main-content"><LoadingIndicator label="Recuperando sesión" /></main>;
  }

  if (!user) {
    return <Navigate replace state={{ from: location }} to="/login" />;
  }

  async function changeStatus(gameId, status) {
    if (workingGameId !== null) return;
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
    if (workingGameId !== null) return;
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

  async function removeGame() {
    if (!pendingRemoval || workingGameId !== null) return;
    const game = pendingRemoval;
    setWorkingGameId(game.gameId);
    setRemoveError(null);
    try {
      await removeGameFromCurrentUserLibrary(game.gameId);
      setLibrary((currentLibrary) => currentLibrary.filter((item) => item.gameId !== game.gameId));
      setPendingRemoval(null);
    } catch (requestError) {
      setRemoveError(requestError.message);
    } finally {
      setWorkingGameId(null);
    }
  }

  return (
    <main className="grid w-full max-w-[980px] gap-8 py-8 md:gap-10 md:py-12" id="main-content" ref={mainRef} tabIndex={-1}>
      <PageHeading title="Mi biblioteca" description="Guardá los juegos que querés jugar y mantené tu colección personal al día.">
        <Button asChild variant="outline"><Link to="/#catalogo">Explorar catálogo</Link></Button>
      </PageHeading>

      {!loading && library.length > 0 && (
        <section className="grid gap-3" aria-label="Filtros de biblioteca">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <fieldset className="min-w-0 w-full sm:w-auto">
              <legend className="sr-only">Filtrar por estado</legend>
              <div className="grid grid-cols-2 gap-1 rounded-md border border-border bg-surface-subtle p-1 sm:flex">
                {filters.map((filter) => {
                  const count = library.filter((game) => (
                    (filter.value === 'ALL' || game.status === filter.value)
                    && (!favoritesOnly || game.favorite)
                  )).length;
                  return (
                    <label key={filter.value} className="relative min-w-0 cursor-pointer">
                      <input className="peer sr-only" type="radio" name="library-filter" value={filter.value}
                        checked={activeFilter === filter.value} onChange={() => setActiveFilter(filter.value)} />
                      <span className="flex min-h-11 items-center justify-between gap-3 rounded-sm border border-transparent px-3 text-sm text-muted-foreground transition-colors hover:text-foreground peer-checked:border-border peer-checked:bg-background peer-checked:text-foreground peer-focus-visible:outline-2 peer-focus-visible:outline-offset-2 peer-focus-visible:outline-ring">
                        {filter.label}<span className="tabular-nums" aria-hidden="true">{count}</span>
                      </span>
                    </label>
                  );
                })}
              </div>
            </fieldset>
            <Button variant="outline" type="button" aria-pressed={favoritesOnly}
              className={favoritesOnly ? 'border-primary text-primary' : ''}
              onClick={() => setFavoritesOnly((current) => !current)}>
              <Star aria-hidden="true" fill={favoritesOnly ? 'currentColor' : 'none'} />Solo favoritos
            </Button>
          </div>
          <p className="text-sm text-muted-foreground" role="status" aria-atomic="true">
            {visibleGames.length} {visibleGames.length === 1 ? 'juego' : 'juegos'}
            {activeFilter !== 'ALL' || favoritesOnly ? ` de ${library.length} en tu biblioteca` : ' en tu biblioteca'}
          </p>
        </section>
      )}

      {loading && <ContentLoading label="Cargando biblioteca" />}
      {error && <StatusMessage kind="error">{error}</StatusMessage>}
      {!loading && !error && library.length === 0 && (
        <section className="grid justify-items-start gap-4 py-4">
          <h2 className="text-2xl leading-[30px] font-semibold tracking-normal">Tu biblioteca está vacía</h2>
          <p className="max-w-[600px] text-base leading-6 text-muted-foreground">Agregá un juego desde su ficha o guardá tu primer progreso para verlo acá.</p>
          <Button asChild><Link to="/#catalogo">Explorar juegos</Link></Button>
        </section>
      )}
      {!loading && library.length > 0 && visibleGames.length === 0 && (
        <section className="grid justify-items-start gap-3 py-4">
          <h2 className="text-xl font-semibold">No hay juegos con estos filtros</h2>
          <p className="text-muted-foreground">Probá otro estado o mostrá toda tu biblioteca.</p>
          <Button variant="outline" type="button" onClick={() => { setActiveFilter('ALL'); setFavoritesOnly(false); }}>Limpiar filtros</Button>
        </section>
      )}
      {!loading && visibleGames.length > 0 && (
        <LazyMotion features={loadLayoutFeatures}>
          <ol className="border-t border-border">
            {visibleGames.map((game) => (
              <m.li key={game.gameId} initial={false} layout={reduced ? false : 'position'} transition={{ layout: { duration: 0.16, ease: [0.2, 0, 0, 1] } }}>
                <LibraryGameRow game={game} disabled={workingGameId !== null} busy={workingGameId === game.gameId}
                  onStatusChange={(status) => changeStatus(game.gameId, status)}
                  onFavoriteChange={() => changeFavorite(game)}
                  onRemove={() => { setRemoveError(null); setPendingRemoval(game); }} />
              </m.li>
            ))}
          </ol>
        </LazyMotion>
      )}
      <ConfirmDialog open={Boolean(pendingRemoval)} onOpenChange={(open) => { if (!open) setPendingRemoval(null); }}
        title="¿Quitar este juego?" description={<>Vas a quitar <strong>{pendingRemoval?.gameTitle}</strong> de tu biblioteca. Se conservan tu progreso y tus publicaciones.</>}
        confirmLabel="Quitar de mi biblioteca" pendingLabel="Quitando juego" destructive
        busy={workingGameId !== null} error={removeError} onConfirm={removeGame} fallbackFocusRef={mainRef} />
    </main>
  );
}
