import { useEffect, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';
import {
  createJournalEntry,
  createJournalReply,
  fetchCheckpoints,
  fetchGameProgress,
  fetchGames,
  fetchJournalEntries,
  fetchJournalReplies,
  updateGameProgress,
} from './gameApi';
import { getGameArtwork, getGameInitials } from './gameArtwork';

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

export default function GameDetailPage() {
  const { gameId } = useParams();
  const location = useLocation();
  const { user, loading: loadingUser } = useAuth();
  const [game, setGame] = useState(null);
  const [progress, setProgress] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [checkpoints, setCheckpoints] = useState([]);
  const [loadingCheckpoints, setLoadingCheckpoints] = useState(false);
  const [checkpointsError, setCheckpointsError] = useState(null);
  const [savingProgress, setSavingProgress] = useState(false);
  const [savingProgressError, setSavingProgressError] = useState(null);
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
      setCheckpoints([]);
      setJournalEntries([]);
      setOpenRepliesEntryId(null);
      setRepliesByEntryId({});

      if (!Number.isInteger(numericGameId) || numericGameId <= 0) {
        setLoading(false);
        return;
      }

      try {
        const [gamesFromApi, progressFromApi] = await Promise.all([
          fetchGames(),
          user ? fetchGameProgress() : Promise.resolve([]),
        ]);
        const requestedGame = gamesFromApi.find((catalogGame) => catalogGame.id === numericGameId);

        if (!requestedGame) return;

        const savedProgress = progressFromApi.find((gameProgress) => gameProgress.gameId === numericGameId) ?? null;
        setGame(requestedGame);
        setProgress(savedProgress);
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
      setCheckpoints(await fetchCheckpoints(id));
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
      await loadJournalEntries(game.id);
      setPendingCheckpoint(null);
    } catch (requestError) {
      setSavingProgressError(requestError.message);
    } finally {
      setSavingProgress(false);
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

  if (loading || loadingUser) return <main className="page-state" id="main-content">Cargando juego...</main>;
  if (error) return <main className="page-state page-state-error" id="main-content">{error}</main>;
  if (!game) {
    return (
      <main className="page-state" id="main-content">
        <p>Ese juego no existe en el catálogo.</p>
        <Link className="detail-back" to="/">Volver al inicio <span aria-hidden="true">→</span></Link>
      </main>
    );
  }

  const availableCheckpoints = progress
    ? checkpoints.filter((checkpoint) => checkpoint.position <= progress.position)
    : [];
  const artwork = getGameArtwork(game.title);

  return (
    <main className="page-main detail-page" id="main-content">
      <Link className="detail-back" to="/">← Volver al catálogo</Link>

      <section className="game-hero">
        <div className="game-detail-artwork">
          {artwork ? (
            <img src={artwork} alt={`Arte de ${game.title}`} />
          ) : (
            <span aria-hidden="true">{getGameInitials(game.title)}</span>
          )}
        </div>
        <div className="game-hero-copy">
          <h1>{game.title}</h1>
          <p>{game.description}</p>
        </div>
        <aside className="progress-summary" aria-label="Resumen de tu avance">
          <dl>
            <dt>Tu avance guardado</dt>
            <dd>{user ? (progress ? progress.checkpointLabel : 'Todavía no elegiste un tramo') : 'Iniciá sesión para guardar avance'}</dd>
          </dl>
          <p>{user && progress ? 'Esto define las conversaciones que podés leer y los tramos sobre los que podés publicar.' : 'Tu avance personal define las conversaciones que Umbral puede mostrarte sin spoilers.'}</p>
        </aside>
      </section>

      <div className="detail-workspace">
        <section className="progress-panel" aria-labelledby="checkpoint-title">
          <header className="panel-heading">
            <h2 id="checkpoint-title">¿Hasta dónde llegaste?</h2>
            <p>Marcá el último tramo que alcanzaste. Podés actualizarlo cuando avances.</p>
          </header>
          {loadingCheckpoints && <p className="status-message">Cargando índice...</p>}
          {checkpointsError && <p className="status-message status-message-error">{checkpointsError}</p>}
          {!loadingCheckpoints && !checkpointsError && (
            <ol className="checkpoint-list">
              {checkpoints.map((checkpoint) => {
                const isCurrentCheckpoint = progress?.checkpointId === checkpoint.id;
                return (
                  <li key={checkpoint.id}>
                    <button
                      className={`checkpoint-button ${isCurrentCheckpoint ? 'checkpoint-button-current' : ''}`}
                      disabled={savingProgress}
                      onClick={() => requestCheckpointChange(checkpoint)}
                      type="button"
                    >
                      <span className="checkpoint-position">{String(checkpoint.position).padStart(2, '0')}</span>
                      <span className="checkpoint-label">{checkpoint.label}</span>
                      <span className="checkpoint-state">
                        {isCurrentCheckpoint && (savingProgress ? 'Guardando' : 'Actual')}
                      </span>
                    </button>
                  </li>
                );
              })}
            </ol>
          )}
          {savingProgressError && <p className="status-message status-message-error">{savingProgressError}</p>}
        </section>

        <div className="conversation-column">
          <section className="compose-panel" aria-labelledby="entry-title">
            <header className="panel-heading">
              <h2 id="entry-title">Compartí lo que ya conocés</h2>
              <p>Publicá dentro del límite que marca tu progreso.</p>
            </header>
            {!user && (
              <div className="account-callout">
                <p>Para publicar una reflexión, duda, teoría o reseña necesitás una cuenta y un avance guardado.</p>
                <button className="button-primary" onClick={() => setAccountPrompt('publicar una entrada')} type="button">Crear cuenta para publicar</button>
              </div>
            )}
            {user && !progress && <p className="status-message">Marcá tu avance antes de publicar una entrada.</p>}
            {user && progress && !loadingCheckpoints && !checkpointsError && (
              <form className="entry-form" onSubmit={createEntry}>
                <label>
                  Esta entrada habla hasta
                  <select value={entryCheckpointId} onChange={(event) => setEntryCheckpointId(event.target.value)}>
                    {availableCheckpoints.map((checkpoint) => <option key={checkpoint.id} value={checkpoint.id}>{checkpoint.label}</option>)}
                  </select>
                </label>
                <label>
                  Tipo de publicación
                  <select value={entryType} onChange={(event) => setEntryType(event.target.value)}>
                    <option value="">Elegí una opción</option>
                    <option value="REFLECTION">Reflexión</option>
                    <option value="QUESTION">Duda</option>
                    <option value="THEORY">Teoría</option>
                    <option value="REVIEW">Reseña</option>
                  </select>
                </label>
                <label>
                  Tu entrada
                  <textarea value={entryContent} maxLength={5000} onChange={(event) => setEntryContent(event.target.value)} placeholder="Compartí lo que te dejó este tramo..." />
                </label>
                <div className="form-footer">
                  <p>Solo podés publicar sobre checkpoints que ya alcanzaste.</p>
                  <button className="button-primary" disabled={savingEntry || !entryCheckpointId || !entryType || !entryContent.trim()} type="submit">
                    {savingEntry ? 'Publicando...' : 'Publicar'}
                  </button>
                </div>
              </form>
            )}
            {user && savingEntryError && <p className="status-message status-message-error">{savingEntryError}</p>}
          </section>

          <section className="feed-panel" aria-labelledby="journal-title">
            <header className="panel-heading">
              <h2 id="journal-title">Conversaciones que ya podés leer</h2>
              <p>El servidor filtra cada entrada antes de enviarla.</p>
            </header>
            {!user && (
              <div className="account-callout">
                <p>Las conversaciones se filtran según el tramo que cada persona alcanzó. Creá una cuenta para guardar el tuyo y ver solo lo que ya conocés.</p>
                <button className="button-secondary" onClick={() => setAccountPrompt('leer conversaciones seguras')} type="button">Crear cuenta</button>
              </div>
            )}
            {user && loadingJournalEntries && <p className="status-message">Cargando entradas...</p>}
            {user && journalEntriesError && <p className="status-message status-message-error">{journalEntriesError}</p>}
            {user && !loadingJournalEntries && !journalEntriesError && journalEntries.length === 0 && (
              <p className="empty-feed">Por ahora no hay nada que podamos mostrarte sin spoilearte. Volvé cuando avances un poco más.</p>
            )}
            {user && !loadingJournalEntries && !journalEntriesError && journalEntries.length > 0 && (
              <ol className="journal-list">
                {journalEntries.map((entry) => {
                  const isRepliesOpen = openRepliesEntryId === entry.id;
                  const replies = repliesByEntryId[entry.id] ?? [];
                  const isLoadingReplies = loadingRepliesEntryId === entry.id;

                  return (
                    <li key={entry.id} className="journal-entry">
                      <p className="journal-entry-meta">
                        @{entry.authorHandle} · {entry.checkpointLabel} ·{' '}
                        <span className="entry-type">{entryTypeLabels[entry.type] ?? entry.type}</span>{' '}
                        · {formatEntryDate(entry.createdAt)}
                      </p>
                      <p>{entry.content}</p>
                      <button
                        className="reply-toggle"
                        type="button"
                        aria-expanded={isRepliesOpen}
                        onClick={() => toggleReplies(entry.id)}
                      >
                        {isRepliesOpen ? 'Ocultar respuestas' : 'Ver respuestas'}
                      </button>

                      {isRepliesOpen && (
                        <div className="reply-thread">
                          {isLoadingReplies && <p className="reply-status">Cargando respuestas...</p>}
                          {repliesError && <p className="reply-status status-message-error">{repliesError}</p>}
                          {!isLoadingReplies && !repliesError && replies.length === 0 && (
                            <p className="reply-status">Todavía no hay respuestas. Podés abrir la conversación.</p>
                          )}
                          {!isLoadingReplies && !repliesError && replies.length > 0 && (
                            <ol className="reply-list">
                              {replies.map((reply) => (
                                <li key={reply.id} className="journal-reply">
                                  <p className="journal-entry-meta">
                                    @{reply.authorHandle} · {formatEntryDate(reply.createdAt)}
                                  </p>
                                  <p>{reply.content}</p>
                                </li>
                              ))}
                            </ol>
                          )}
                          {!isLoadingReplies && !repliesError && (
                            <form className="reply-form" onSubmit={(event) => createReply(event, entry.id)}>
                              <label>
                                Tu respuesta
                                <textarea
                                  value={replyContent}
                                  maxLength={5000}
                                  onChange={(event) => setReplyContent(event.target.value)}
                                  placeholder="Sumate a la conversación sin adelantar nada..."
                                />
                              </label>
                              <div className="reply-form-footer">
                                <p>Esta respuesta pertenece al mismo tramo que la entrada.</p>
                                <button className="button-primary" disabled={savingReply || !replyContent.trim()} type="submit">
                                  {savingReply ? 'Publicando...' : 'Responder'}
                                </button>
                              </div>
                            </form>
                          )}
                          {savingReplyError && <p className="reply-status status-message-error">{savingReplyError}</p>}
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

      {pendingCheckpoint && (
        <div aria-labelledby="confirm-progress-title" aria-modal="true" className="dialog-backdrop" role="dialog">
          <section className="dialog-panel">
            <h2 id="confirm-progress-title">¿Guardar este avance?</h2>
            <p>Vas a marcar <strong>{pendingCheckpoint.label}</strong> como el último tramo que alcanzaste. Umbral ajustará las conversaciones disponibles a partir de este punto.</p>
            {savingProgressError && <p className="form-error" role="alert">{savingProgressError}</p>}
            <div className="dialog-actions">
              <button className="button-secondary" disabled={savingProgress} onClick={() => setPendingCheckpoint(null)} type="button">Cancelar</button>
              <button className="button-primary" disabled={savingProgress} onClick={confirmCheckpointChange} type="button">
                {savingProgress ? 'Guardando...' : 'Guardar avance'}
              </button>
            </div>
          </section>
        </div>
      )}

      {accountPrompt && (
        <div aria-labelledby="account-prompt-title" aria-modal="true" className="dialog-backdrop" role="dialog">
          <section className="dialog-panel">
            <h2 id="account-prompt-title">Creá una cuenta para {accountPrompt}</h2>
            <p>Con una cuenta podés guardar tu punto del juego y Umbral muestra solo las conversaciones que ya son seguras para vos.</p>
            <div className="dialog-actions">
              <button className="button-secondary" onClick={() => setAccountPrompt(null)} type="button">Seguir mirando</button>
              <Link className="button-primary" state={{ from: location }} to="/register">Crear cuenta</Link>
            </div>
          </section>
        </div>
      )}
    </main>
  );
}
