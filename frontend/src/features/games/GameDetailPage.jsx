import { useEffect, useRef, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';
import {
  createJournalEntry,
  createJournalReply,
  addGameToCurrentUserLibrary,
  fetchCheckpoints,
  fetchCurrentUserLibrary,
  fetchGameProgress,
  fetchGames,
  fetchJournalEntries,
  fetchJournalReplies,
  submitCheckpointSuggestion,
  updateGameProgress,
} from './gameApi';
import { getGameArtwork } from './gameArtwork';
import { ArrowLeft, Check, MessageCircle, Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input, Textarea } from '@/components/ui/field';
import GameArtwork from '@/components/GameArtwork';
import StatusMessage from '@/components/StatusMessage';
import ConfirmDialog from '@/components/ConfirmDialog';
import AccountPrompt from '@/components/AccountPrompt';
import LoadingIndicator from '../../components/LoadingIndicator';

function formatEntryDate(createdAt) {
  return new Intl.DateTimeFormat('es-AR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(new Date(createdAt));
}

const entryTypeLabels = {
  REFLECTION: 'Reflexión',
  QUESTION: 'Duda',
  THEORY: 'Teoría',
  REVIEW: 'Reseña',
};

const libraryStatusLabels = {
  WANT_TO_PLAY: 'Quiero jugar',
  PLAYING: 'Jugando',
  COMPLETED: 'Terminado',
};

export default function GameDetailPage() {
  const { gameId } = useParams();
  const mainRef = useRef(null);
  const location = useLocation();
  const { user, loading: loadingUser } = useAuth();
  const [game, setGame] = useState(null);
  const [progress, setProgress] = useState(null);
  const [libraryEntry, setLibraryEntry] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [checkpoints, setCheckpoints] = useState([]);
  const [loadingCheckpoints, setLoadingCheckpoints] = useState(false);
  const [checkpointsError, setCheckpointsError] = useState(null);
  const [savingProgress, setSavingProgress] = useState(false);
  const [savingProgressError, setSavingProgressError] = useState(null);
  const [savingLibrary, setSavingLibrary] = useState(false);
  const [libraryError, setLibraryError] = useState(null);
  const [checkpointSuggestionLabel, setCheckpointSuggestionLabel] = useState('');
  const [checkpointSuggestionPosition, setCheckpointSuggestionPosition] = useState('');
  const [savingCheckpointSuggestion, setSavingCheckpointSuggestion] = useState(false);
  const [checkpointSuggestionError, setCheckpointSuggestionError] = useState(null);
  const [checkpointSuggestionNotice, setCheckpointSuggestionNotice] = useState(null);
  const [journalEntries, setJournalEntries] = useState([]);
  const [loadingJournalEntries, setLoadingJournalEntries] = useState(false);
  const [journalEntriesError, setJournalEntriesError] = useState(null);
  const [entryCheckpointId, setEntryCheckpointId] = useState('');
  const [entryType, setEntryType] = useState('');
  const [entryContent, setEntryContent] = useState('');
  const [savingEntry, setSavingEntry] = useState(false);
  const [savingEntryError, setSavingEntryError] = useState(null);
  const [openRepliesEntryId, setOpenRepliesEntryId] = useState(null);
  const [repliesByEntryId, setRepliesByEntryId] = useState({});
  const [loadingRepliesEntryId, setLoadingRepliesEntryId] = useState(null);
  const [repliesError, setRepliesError] = useState(null);
  const [replyContent, setReplyContent] = useState('');
  const [savingReply, setSavingReply] = useState(false);
  const [savingReplyError, setSavingReplyError] = useState(null);
  const [pendingCheckpoint, setPendingCheckpoint] = useState(null);
  const [accountPrompt, setAccountPrompt] = useState(null);

  const numericGameId = Number(gameId);

  useEffect(() => {
    async function loadGame() {
      setLoading(true);
      setError(null);
      setGame(null);
      setProgress(null);
      setLibraryEntry(null);
      setCheckpoints([]);
      setJournalEntries([]);
      setOpenRepliesEntryId(null);
      setRepliesByEntryId({});

      if (!Number.isInteger(numericGameId) || numericGameId <= 0) {
        setLoading(false);
        return;
      }

      try {
        const [gamesFromApi, progressFromApi, libraryFromApi] = await Promise.all([
          fetchGames(),
          user ? fetchGameProgress() : Promise.resolve([]),
          user ? fetchCurrentUserLibrary() : Promise.resolve([]),
        ]);
        const requestedGame = gamesFromApi.find((catalogGame) => catalogGame.id === numericGameId);

        if (!requestedGame) return;

        const savedProgress = progressFromApi.find((gameProgress) => gameProgress.gameId === numericGameId) ?? null;
        setGame(requestedGame);
        setProgress(savedProgress);
        setLibraryEntry(libraryFromApi.find((entry) => entry.gameId === numericGameId) ?? null);
        setEntryCheckpointId(savedProgress ? String(savedProgress.checkpointId) : '');
      } catch (requestError) {
        setError(requestError.message);
      } finally {
        setLoading(false);
      }
    }

    loadGame();
  }, [numericGameId, user]);

  useEffect(() => {
    if (!game) return;

    loadCheckpoints(game.id);

    if (user) {
      loadJournalEntries(game.id);
    }
  }, [game, user]);

  async function loadCheckpoints(id) {
    setLoadingCheckpoints(true);
    setCheckpointsError(null);
    try {
      const checkpointsFromApi = await fetchCheckpoints(id);
      setCheckpoints(checkpointsFromApi);
      setCheckpointSuggestionPosition(String(
        Math.max(0, ...checkpointsFromApi.map((checkpoint) => checkpoint.position)) + 1,
      ));
    } catch (requestError) {
      setCheckpointsError(requestError.message);
    } finally {
      setLoadingCheckpoints(false);
    }
  }

  async function loadJournalEntries(id) {
    setLoadingJournalEntries(true);
    setJournalEntriesError(null);
    setOpenRepliesEntryId(null);
    setRepliesByEntryId({});
    setRepliesError(null);
    setReplyContent('');
    setSavingReplyError(null);
    try {
      setJournalEntries(await fetchJournalEntries(id));
    } catch (requestError) {
      setJournalEntriesError(requestError.message);
    } finally {
      setLoadingJournalEntries(false);
    }
  }

  function requestCheckpointChange(checkpoint) {
    if (!user) {
      setAccountPrompt('guardar tu avance');
      return;
    }

    if (progress?.checkpointId === checkpoint.id) return;
    setSavingProgressError(null);
    setPendingCheckpoint(checkpoint);
  }

  async function confirmCheckpointChange() {
    if (!pendingCheckpoint) return;

    setSavingProgress(true);
    setSavingProgressError(null);
    try {
      const savedProgress = await updateGameProgress(game.id, pendingCheckpoint.id);
      setProgress(savedProgress);
      setEntryCheckpointId(String(savedProgress.checkpointId));
      const library = await fetchCurrentUserLibrary();
      setLibraryEntry(library.find((entry) => entry.gameId === game.id) ?? null);
      await loadJournalEntries(game.id);
      setPendingCheckpoint(null);
    } catch (requestError) {
      setSavingProgressError(requestError.message);
    } finally {
      setSavingProgress(false);
    }
  }

  async function addGameToLibrary() {
    if (!user) {
      setAccountPrompt('guardar este juego en tu biblioteca');
      return;
    }

    setSavingLibrary(true);
    setLibraryError(null);
    try {
      setLibraryEntry(await addGameToCurrentUserLibrary(game.id));
    } catch (requestError) {
      setLibraryError(requestError.message);
    } finally {
      setSavingLibrary(false);
    }
  }

  async function createCheckpointSuggestion(event) {
    event.preventDefault();
    setSavingCheckpointSuggestion(true);
    setCheckpointSuggestionError(null);
    setCheckpointSuggestionNotice(null);

    try {
      const suggestion = await submitCheckpointSuggestion(
        game.id,
        checkpointSuggestionLabel.trim(),
        Number(checkpointSuggestionPosition),
      );
      setCheckpointSuggestionLabel('');
      setCheckpointSuggestionNotice(`“${suggestion.label}” quedó enviado para revisión.`);
    } catch (requestError) {
      setCheckpointSuggestionError(requestError.message);
    } finally {
      setSavingCheckpointSuggestion(false);
    }
  }

  async function createEntry(event) {
    event.preventDefault();
    setSavingEntry(true);
    setSavingEntryError(null);
    try {
      await createJournalEntry(Number(entryCheckpointId), entryType, entryContent);
      setEntryType('');
      setEntryContent('');
      await loadJournalEntries(game.id);
    } catch (requestError) {
      setSavingEntryError(requestError.message);
    } finally {
      setSavingEntry(false);
    }
  }

  async function toggleReplies(entryId) {
    if (openRepliesEntryId === entryId) {
      setOpenRepliesEntryId(null);
      setRepliesError(null);
      setSavingReplyError(null);
      return;
    }

    setOpenRepliesEntryId(entryId);
    setRepliesError(null);
    setSavingReplyError(null);
    setReplyContent('');

    if (Object.hasOwn(repliesByEntryId, entryId)) return;

    setLoadingRepliesEntryId(entryId);
    try {
      const replies = await fetchJournalReplies(entryId);
      setRepliesByEntryId((currentReplies) => ({
        ...currentReplies,
        [entryId]: replies,
      }));
    } catch (requestError) {
      setRepliesError(requestError.message);
    } finally {
      setLoadingRepliesEntryId(null);
    }
  }

  async function createReply(event, entryId) {
    event.preventDefault();
    setSavingReply(true);
    setSavingReplyError(null);

    try {
      const savedReply = await createJournalReply(entryId, replyContent);
      setRepliesByEntryId((currentReplies) => ({
        ...currentReplies,
        [entryId]: [...(currentReplies[entryId] ?? []), savedReply],
      }));
      setReplyContent('');
    } catch (requestError) {
      setSavingReplyError(requestError.message);
    } finally {
      setSavingReply(false);
    }
  }

  if (loading || loadingUser) return <main className="grid min-h-[50vh] content-center justify-items-start gap-4 py-12" id="main-content"><LoadingIndicator label="Cargando juego" showLabel /></main>;
  if (error) return <main className="py-12 text-destructive" id="main-content">{error}</main>;
  if (!game) {
    return (
      <main className="grid min-h-[50vh] content-center justify-items-start gap-4 py-12" id="main-content">
        <p>Ese juego no existe en el catálogo.</p>
        <Link className="inline-flex min-h-11 items-center text-base underline underline-offset-4" to="/">Volver al inicio <span aria-hidden="true">→</span></Link>
      </main>
    );
  }

  const availableCheckpoints = progress
    ? checkpoints.filter((checkpoint) => checkpoint.position <= progress.position)
    : [];
  const artwork = game.coverImageUrl ?? getGameArtwork(game.title);

  return (
    <main className="grid gap-8 py-8 md:gap-10 md:py-12" id="main-content" ref={mainRef} tabIndex={-1}>
      <Button asChild variant="ghost" className="w-fit px-0"><Link to="/#catalogo"><ArrowLeft aria-hidden="true" />Volver al catálogo</Link></Button>

      <section className="grid grid-cols-[96px_minmax(0,1fr)] items-start gap-x-4 gap-y-6 border-b border-border pb-8 sm:grid-cols-[208px_minmax(0,1fr)] sm:gap-x-8 lg:grid-cols-[240px_minmax(0,1fr)] lg:gap-x-12" aria-labelledby="game-title">
        <GameArtwork src={artwork} title={game.title} className="aspect-[4/5] w-24 sm:row-span-3 sm:w-52 lg:w-60" />
        <h1 id="game-title" className="min-w-0 self-center break-words text-[28px] leading-[34px] font-semibold tracking-[-0.02em] sm:self-end sm:text-4xl sm:leading-[42px]">{game.title}</h1>
        <p className="col-span-2 max-w-[720px] break-words text-base leading-7 text-muted-foreground sm:col-span-1">{game.description}</p>
        <div className="col-span-2 grid min-w-0 gap-4 sm:col-span-1">
          <div className="flex flex-wrap items-center gap-x-8 gap-y-4 border-t border-border pt-5">
            {user && libraryEntry ? (
              <>
                <dl className="grid gap-1">
                  <dt className="text-sm text-muted-foreground">En tu biblioteca</dt>
                  <dd className="font-medium">{libraryStatusLabels[libraryEntry.status] ?? 'Sin estado'}</dd>
                </dl>
                {libraryEntry.favorite && <span className="text-sm font-medium">Favorito</span>}
                <Button asChild variant="outline"><Link to="/me/library">Ver biblioteca</Link></Button>
              </>
            ) : (
              <Button variant="outline" disabled={savingLibrary} onClick={addGameToLibrary} type="button">
                {savingLibrary ? <LoadingIndicator label="Guardando en biblioteca" showLabel /> : <><Plus aria-hidden="true" />Guardar para jugar</>}
              </Button>
            )}
          </div>
          {libraryError && <StatusMessage kind="error">{libraryError}</StatusMessage>}
        </div>
      </section>

      <div className="grid items-start gap-10 lg:grid-cols-[280px_minmax(0,720px)] lg:gap-12">
        <section className="grid min-w-0 gap-6" aria-labelledby="checkpoint-title">
          <header className="grid gap-3 [&_h2]:text-2xl [&_h2]:leading-[30px] [&_h2]:font-semibold [&_h2]:tracking-normal [&_p]:text-base [&_p]:leading-6 [&_p]:text-muted-foreground">
            <h2 id="checkpoint-title">¿Hasta dónde llegaste?</h2>
            <p>Marcá el último tramo que alcanzaste. Podés actualizarlo cuando avances.</p>
          </header>
          <dl className="grid gap-2 border-l-2 border-primary pl-4" aria-label="Resumen de tu avance">
            <dt className="text-sm leading-5 text-muted-foreground">Tu avance guardado</dt>
            <dd className="break-words text-base leading-6 font-medium">{user ? (progress ? progress.checkpointLabel : 'Todavía no elegiste un tramo') : 'Iniciá sesión para guardar avance'}</dd>
          </dl>
          {loadingCheckpoints && <LoadingIndicator label="Cargando checkpoints" showLabel />}
          {checkpointsError && <StatusMessage kind="error">{checkpointsError}</StatusMessage>}
          {!loadingCheckpoints && !checkpointsError && (
            <ol className="border-t border-border">
              {checkpoints.map((checkpoint) => {
                const isCurrentCheckpoint = progress?.checkpointId === checkpoint.id;
                return (
                  <li key={checkpoint.id}>
                    <button
                      className={`grid w-full grid-cols-[24px_minmax(0,1fr)] items-center gap-x-3 gap-y-1 border-b border-l-2 border-border px-3 py-3 text-left text-base leading-6 hover:bg-accent disabled:opacity-60 ${isCurrentCheckpoint ? 'border-l-primary bg-accent text-foreground' : 'border-l-transparent text-muted-foreground'}`}
                      aria-current={isCurrentCheckpoint ? 'step' : undefined}
                      disabled={savingProgress}
                      onClick={() => requestCheckpointChange(checkpoint)}
                      type="button"
                    >
                      <span className="text-sm leading-5 text-muted-foreground">{String(checkpoint.position).padStart(2, '0')}</span>
                      <span className="min-w-0 break-words">{checkpoint.label}</span>
                      <span className="col-start-2 flex items-center gap-1 text-sm leading-5 text-primary">
                        {isCurrentCheckpoint && (savingProgress ? <LoadingIndicator label="Guardando avance" showLabel /> : <><Check aria-hidden="true" className="size-4" />Actual</>)}
                      </span>
                    </button>
                  </li>
                );
              })}
            </ol>
          )}
          {user && !loadingCheckpoints && !checkpointsError && (
            <form className="grid gap-4 border-t border-border pt-6 [&_h3]:text-base [&_h3]:font-semibold [&_p]:text-sm [&_p]:leading-5 [&_p]:text-muted-foreground" onSubmit={createCheckpointSuggestion}>
              <h3>¿Falta un checkpoint?</h3>
              <p>Proponelo para que una persona moderadora pueda revisarlo antes de habilitarlo.</p>
              <label className="grid gap-2 text-base leading-6 font-medium">
                Nombre del tramo
                <Input
                  maxLength="255"
                  onChange={(event) => setCheckpointSuggestionLabel(event.target.value)}
                  placeholder="Nombre del checkpoint"
                  required
                  value={checkpointSuggestionLabel}
                />
              </label>
              <label className="grid gap-2 text-base leading-6 font-medium">
                Posición en la historia
                <Input
                  min="1"
                  onChange={(event) => setCheckpointSuggestionPosition(event.target.value)}
                  required
                  type="number"
                  value={checkpointSuggestionPosition}
                />
              </label>
              <Button
                variant="outline"
                disabled={savingCheckpointSuggestion || !checkpointSuggestionLabel.trim() || !checkpointSuggestionPosition}
                type="submit"
              >
                {savingCheckpointSuggestion ? <LoadingIndicator label="Enviando checkpoint" showLabel /> : 'Enviar propuesta'}
              </Button>
              {checkpointSuggestionNotice && <StatusMessage kind="success">{checkpointSuggestionNotice}</StatusMessage>}
              {checkpointSuggestionError && <StatusMessage kind="error">{checkpointSuggestionError}</StatusMessage>}
            </form>
          )}
        </section>

        <div className="grid min-w-0 gap-12">
          <section className="grid gap-6" aria-labelledby="entry-title">
            <header className="grid gap-3 [&_h2]:text-2xl [&_h2]:leading-[30px] [&_h2]:font-semibold [&_h2]:tracking-normal [&_p]:text-base [&_p]:leading-6 [&_p]:text-muted-foreground">
              <h2 id="entry-title">Compartí lo que ya conocés</h2>
              <p>Publicá dentro del límite que marca tu progreso.</p>
            </header>
            {!user && (
              <div className="grid justify-items-start gap-4">
                <p>Para publicar una reflexión, duda, teoría o reseña necesitás una cuenta y un avance guardado.</p>
                <Button onClick={() => setAccountPrompt('publicar una entrada')} type="button">Crear cuenta para publicar</Button>
              </div>
            )}
            {user && !progress && <StatusMessage>Marcá tu avance antes de publicar una entrada.</StatusMessage>}
            {user && progress && !loadingCheckpoints && !checkpointsError && (
              <form className="grid gap-5" onSubmit={createEntry}>
                <label className="grid gap-2 text-base leading-6 font-medium">
                  <span id="entry-checkpoint-label">Esta entrada habla hasta</span>
                  <select aria-labelledby="entry-checkpoint-label" className="min-h-11 w-full min-w-0 rounded-md border border-input bg-popover px-3 text-base font-normal text-foreground" value={entryCheckpointId} onChange={(event) => setEntryCheckpointId(event.target.value)}>
                    {availableCheckpoints.map((checkpoint) => <option key={checkpoint.id} value={checkpoint.id}>{checkpoint.label}</option>)}
                  </select>
                </label>
                <label className="grid gap-2 text-base leading-6 font-medium">
                  <span id="entry-type-label">Tipo de publicación</span>
                  <select aria-labelledby="entry-type-label" className="min-h-11 w-full min-w-0 rounded-md border border-input bg-popover px-3 text-base font-normal text-foreground" value={entryType} onChange={(event) => setEntryType(event.target.value)}>
                    <option value="">Elegí una opción</option>
                    <option value="REFLECTION">Reflexión</option>
                    <option value="QUESTION">Duda</option>
                    <option value="THEORY">Teoría</option>
                    <option value="REVIEW">Reseña</option>
                  </select>
                </label>
                <label className="grid gap-2 text-base leading-6 font-medium">
                  Tu entrada
                  <Textarea value={entryContent} maxLength={5000} onChange={(event) => setEntryContent(event.target.value)} placeholder="Compartí lo que te dejó este tramo..." />
                </label>
                <div className="flex flex-col items-start gap-4 sm:flex-row sm:items-center sm:justify-between [&_p]:max-w-[400px] [&_p]:text-sm [&_p]:leading-5 [&_p]:text-muted-foreground">
                  <p>Solo podés publicar sobre checkpoints que ya alcanzaste.</p>
                  <Button  disabled={savingEntry || !entryCheckpointId || !entryType || !entryContent.trim()} type="submit">
                    {savingEntry ? <LoadingIndicator label="Publicando entrada" showLabel /> : 'Publicar'}
                  </Button>
                </div>
              </form>
            )}
            {user && savingEntryError && <StatusMessage kind="error">{savingEntryError}</StatusMessage>}
          </section>

          <section className="grid gap-6 border-t border-border pt-8" aria-labelledby="journal-title">
            <header className="grid gap-3 [&_h2]:text-2xl [&_h2]:leading-[30px] [&_h2]:font-semibold [&_h2]:tracking-normal [&_p]:text-base [&_p]:leading-6 [&_p]:text-muted-foreground">
              <h2 id="journal-title">Conversaciones que ya podés leer</h2>
              <p>Conversaciones de tu tramo y de los anteriores.</p>
            </header>
            {!user && (
              <div className="grid justify-items-start gap-4">
                <p>Las conversaciones se filtran según el tramo que cada persona alcanzó. Creá una cuenta para guardar el tuyo y ver solo lo que ya conocés.</p>
                <Button variant="outline" onClick={() => setAccountPrompt('leer conversaciones seguras')} type="button">Crear cuenta</Button>
              </div>
            )}
            {user && loadingJournalEntries && <LoadingIndicator label="Cargando entradas" showLabel />}
            {user && journalEntriesError && <StatusMessage kind="error">{journalEntriesError}</StatusMessage>}
            {user && !loadingJournalEntries && !journalEntriesError && journalEntries.length === 0 && (
              <StatusMessage>Por ahora no hay nada que podamos mostrarte sin spoilearte. Volvé cuando avances un poco más.</StatusMessage>
            )}
            {user && !loadingJournalEntries && !journalEntriesError && journalEntries.length > 0 && (
              <ol className="border-t border-border">
                {journalEntries.map((entry) => {
                  const isRepliesOpen = openRepliesEntryId === entry.id;
                  const replies = repliesByEntryId[entry.id] ?? [];
                  const isLoadingReplies = loadingRepliesEntryId === entry.id;

                  return (
                    <li key={entry.id} className="grid min-w-0 gap-4 border-b border-border py-6">
                      <p className="break-words text-sm leading-5 text-muted-foreground">
                        @{entry.authorHandle} · {entry.checkpointLabel} ·{' '}
                        <span className="font-medium text-foreground">{entryTypeLabels[entry.type] ?? entry.type}</span>{' '}
                        · {formatEntryDate(entry.createdAt)}
                      </p>
                      <p className="whitespace-pre-wrap break-words text-[17px] leading-[27px] text-foreground sm:text-lg sm:leading-[29px]">{entry.content}</p>
                      <Button
                        variant="ghost" className="w-fit px-0"
                        type="button"
                        aria-expanded={isRepliesOpen}
                        onClick={() => toggleReplies(entry.id)}
                      >
                        <MessageCircle aria-hidden="true" />{isRepliesOpen ? 'Ocultar respuestas' : 'Ver respuestas'}
                      </Button>

                      {isRepliesOpen && (
                        <div className="grid min-w-0 gap-5 border-l border-border pl-3 sm:pl-4">
                          {isLoadingReplies && <p className="text-sm leading-5 text-muted-foreground"><LoadingIndicator label="Cargando respuestas" showLabel /></p>}
                          {repliesError && <StatusMessage kind="error">{repliesError}</StatusMessage>}
                          {!isLoadingReplies && !repliesError && replies.length === 0 && (
                            <p className="text-sm leading-5 text-muted-foreground">Todavía no hay respuestas. Podés abrir la conversación.</p>
                          )}
                          {!isLoadingReplies && !repliesError && replies.length > 0 && (
                            <ol className="grid gap-5">
                              {replies.map((reply) => (
                                <li key={reply.id} className="grid min-w-0 gap-3 border-b border-border pb-5 last:border-0">
                                  <p className="break-words text-sm leading-5 text-muted-foreground">
                                    @{reply.authorHandle} · {formatEntryDate(reply.createdAt)}
                                  </p>
                                  <p className="whitespace-pre-wrap break-words text-[17px] leading-[27px] text-foreground sm:text-lg sm:leading-[29px]">{reply.content}</p>
                                </li>
                              ))}
                            </ol>
                          )}
                          {!isLoadingReplies && !repliesError && (
                            <form className="grid gap-4 border-t border-border pt-5" onSubmit={(event) => createReply(event, entry.id)}>
                              <label className="grid gap-2 text-base leading-6 font-medium">
                                Tu respuesta
                                <Textarea
                                  value={replyContent}
                                  maxLength={5000}
                                  onChange={(event) => setReplyContent(event.target.value)}
                                  placeholder="Sumate a la conversación sin adelantar nada..."
                                />
                              </label>
                              <div className="flex flex-col items-start gap-4 sm:flex-row sm:items-center sm:justify-between [&_p]:max-w-[400px] [&_p]:text-sm [&_p]:leading-5 [&_p]:text-muted-foreground">
                                <p>Esta respuesta pertenece al mismo tramo que la entrada.</p>
                                <Button  disabled={savingReply || !replyContent.trim()} type="submit">
                                  {savingReply ? <LoadingIndicator label="Publicando respuesta" showLabel /> : 'Responder'}
                                </Button>
                              </div>
                            </form>
                          )}
                          {savingReplyError && <StatusMessage kind="error">{savingReplyError}</StatusMessage>}
                        </div>
                      )}
                    </li>
                  );
                })}
              </ol>
            )}
          </section>
        </div>
      </div>

      <ConfirmDialog open={Boolean(pendingCheckpoint)} onOpenChange={(open) => { if (!open) setPendingCheckpoint(null); }}
        title="¿Guardar este avance?"
        description={<>Vas a marcar <strong>{pendingCheckpoint?.label}</strong> como el último tramo que alcanzaste. Umbral ajustará las conversaciones disponibles a partir de este punto.</>}
        confirmLabel="Guardar avance" pendingLabel="Guardando avance" busy={savingProgress}
        error={savingProgressError} onConfirm={confirmCheckpointChange} fallbackFocusRef={mainRef} />
      <AccountPrompt action={accountPrompt} onClose={() => setAccountPrompt(null)} from={location} fallbackFocusRef={mainRef} />
    </main>
  );
}
