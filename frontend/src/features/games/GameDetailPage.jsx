import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
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
          fetchGameProgress(),
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
  }, [numericGameId]);

  useEffect(() => {
    if (!game) return;

    loadCheckpoints(game.id);
    loadJournalEntries(game.id);
  }, [game]);

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

  async function selectCheckpoint(checkpoint) {
    setSavingProgress(true);
    setSavingProgressError(null);
    try {
      const savedProgress = await updateGameProgress(game.id, checkpoint.id);
      setProgress(savedProgress);
      setEntryCheckpointId(String(savedProgress.checkpointId));
      await loadJournalEntries(game.id);
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

  if (loading) return <div className="page-state">Cargando juego...</div>;
  if (error) return <div className="page-state page-state-error">{error}</div>;
  if (!game) {
    return (
      <div className="page-state">
        <p>Ese juego no existe en el catálogo.</p>
        <Link className="detail-back" to="/">Volver al inicio <span aria-hidden="true">→</span></Link>
      </div>
    );
  }

  const availableCheckpoints = progress
    ? checkpoints.filter((checkpoint) => checkpoint.position <= progress.position)
    : [];

  return (
    <div className="page-main detail-page">
      <Link className="detail-back" to="/">← Volver al catálogo</Link>

      <section className="game-hero">
        <div className="game-hero-copy">
          <p className="eyebrow">Juego</p>
          <h1>{game.title}</h1>
          <p>{game.description}</p>
        </div>
        <aside className="progress-summary" aria-label="Resumen de tu avance">
          <p className="eyebrow">Tu avance guardado</p>
          <strong>{progress ? progress.checkpointLabel : 'Todavía no elegiste un tramo'}</strong>
          <p>{progress ? 'Esto define las conversaciones que podés leer y los tramos sobre los que podés publicar.' : 'Elegí el último tramo que jugaste para desbloquear conversaciones seguras.'}</p>
        </aside>
      </section>

      <div className="detail-workspace">
        <section className="progress-panel" aria-labelledby="checkpoint-title">
          <header className="panel-heading">
            <p className="eyebrow">Tu progreso</p>
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
                      onClick={() => selectCheckpoint(checkpoint)}
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
              <p className="eyebrow">Nueva entrada</p>
              <h2 id="entry-title">Compartí lo que ya conocés</h2>
            </header>
            {!progress && <p className="status-message">Marcá tu avance antes de publicar una entrada.</p>}
            {progress && !loadingCheckpoints && !checkpointsError && (
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
            {savingEntryError && <p className="status-message status-message-error">{savingEntryError}</p>}
          </section>

          <section className="feed-panel" aria-labelledby="journal-title">
            <header className="panel-heading">
              <p className="eyebrow">Bitácora segura</p>
              <h2 id="journal-title">Conversaciones que ya podés leer</h2>
            </header>
            {loadingJournalEntries && <p className="status-message">Cargando entradas...</p>}
            {journalEntriesError && <p className="status-message status-message-error">{journalEntriesError}</p>}
            {!loadingJournalEntries && !journalEntriesError && journalEntries.length === 0 && (
              <p className="empty-feed">Por ahora no hay nada que podamos mostrarte sin spoilearte. Volvé cuando avances un poco más.</p>
            )}
            {!loadingJournalEntries && !journalEntriesError && journalEntries.length > 0 && (
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
    </div>
  );
}
