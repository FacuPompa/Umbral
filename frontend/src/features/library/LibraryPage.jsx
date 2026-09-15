import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import LoadingIndicator from '../../components/LoadingIndicator';
import { useAuth } from '../auth/useAuth';
import {
  fetchCurrentUserLibrary,
  removeGameFromCurrentUserLibrary,
  updateCurrentUserLibraryGameFavorite,
  updateCurrentUserLibraryGameStatus,
} from '../games/gameApi';
import { Star, Trash2 } from 'lucide-react';
import { LazyMotion, m, useReducedMotion } from 'motion/react';
import ConfirmDialog from '@/components/ConfirmDialog';
import PageHeading from '@/components/PageHeading';
import GameRow from '@/components/GameRow';
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
    return <main className="page-state" id="main-content"><LoadingIndicator label="Recuperando sesión" /></main>;
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

      <div className="flex flex-wrap items-center justify-between gap-4 border-b border-border pb-4">
        <div className="flex flex-wrap gap-2" role="group" aria-label="Filtrar por estado">
          {filters.map((filter) => (
            <Button aria-pressed={activeFilter === filter.value}
              variant={activeFilter === filter.value ? 'primary' : 'ghost'} key={filter.value}
              onClick={() => setActiveFilter(filter.value)} type="button">{filter.label}</Button>
          ))}
        </div>
        <label className="inline-flex min-h-11 items-center gap-3 text-sm">
          <input className="size-4 accent-primary" checked={favoritesOnly} onChange={(event) => setFavoritesOnly(event.target.checked)} type="checkbox" />
          Solo favoritos
        </label>
      </div>

      {loading && <LoadingIndicator label="Cargando biblioteca" showLabel />}
      {error && <StatusMessage kind="error">{error}</StatusMessage>}
      {!loading && !error && library.length === 0 && (
        <section className="grid justify-items-start gap-4 py-4">
          <h2 className="text-2xl leading-[30px] font-semibold tracking-normal">Tu biblioteca está vacía</h2>
          <p className="max-w-[600px] text-base leading-6 text-muted-foreground">Agregá un juego desde su ficha o guardá tu primer progreso para verlo acá.</p>
          <Button asChild><Link to="/#catalogo">Explorar juegos</Link></Button>
        </section>
      )}
      {!loading && library.length > 0 && visibleGames.length === 0 && <StatusMessage>No hay juegos que coincidan con este filtro.</StatusMessage>}
      {!loading && visibleGames.length > 0 && (
        <LazyMotion features={loadLayoutFeatures}>
          <ol className="border-t border-border">
            {visibleGames.map((game) => (
              <m.li key={game.gameId} initial={false} layout={reduced ? false : 'position'} transition={{ layout: { duration: 0.16, ease: [0.2, 0, 0, 1] } }}>
                <GameRow gameId={game.gameId} title={game.gameTitle} coverImageUrl={game.coverImageUrl}
                  description={game.checkpointLabel ? `Avance: ${game.checkpointLabel}` : 'Todavía no marcaste un checkpoint.'}>
                  <div className="grid gap-3 sm:w-60">
                    <label className="grid gap-2 text-sm font-medium">
                      <span id={`library-status-label-${game.gameId}`}>Estado</span>
                      <select className="min-h-11 w-full rounded-md border border-input bg-popover px-3 text-base font-normal text-foreground"
                        aria-labelledby={`library-status-label-${game.gameId}`}
                        disabled={workingGameId !== null} onChange={(event) => changeStatus(game.gameId, event.target.value)} value={game.status}>
                        {libraryStatusOptions.map((status) => <option key={status.value} value={status.value}>{status.label}</option>)}
                      </select>
                    </label>
                    <div className="flex flex-wrap items-center gap-2">
                      <Button variant="ghost" className="px-2" aria-pressed={game.favorite} aria-label={`Favorito: ${game.gameTitle}`}
                        disabled={workingGameId !== null} onClick={() => changeFavorite(game)} type="button">
                        <Star aria-hidden="true" fill={game.favorite ? 'currentColor' : 'none'} className={game.favorite ? 'text-primary' : ''} />Favorito
                      </Button>
                      <Button variant="destructive" className="px-2" disabled={workingGameId !== null}
                        onClick={() => { setRemoveError(null); setPendingRemoval(game); }} type="button" aria-label={`Quitar ${game.gameTitle}`}>
                        <Trash2 aria-hidden="true" />Quitar
                      </Button>
                    </div>
                    {workingGameId === game.gameId && <LoadingIndicator label="Actualizando juego" showLabel />}
                  </div>
                </GameRow>
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
