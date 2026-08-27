import { useEffect, useState } from 'react';
import {
  createJournalEntry,
  fetchCheckpoints,
  fetchGameProgress,
  fetchGames,
  fetchJournalEntries,
  updateGameProgress,
} from './gameApi';

export default function GameCatalogPage() {
  const [games, setGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [checkpoints, setCheckpoints] = useState([]);
  const [loadingCheckpoints, setLoadingCheckpoints] = useState(false);
  const [checkpointsError, setCheckpointsError] = useState(null);
  const [selectedGameId, setSelectedGameId] = useState(null);
  const [progressByGameId, setProgressByGameId] = useState({});
  const [savingProgress, setSavingProgress] = useState(false);
  const [savingProgressError, setSavingProgressError] = useState(null);
  const [journalEntries, setJournalEntries] = useState([]);
  const [loadingJournalEntries, setLoadingJournalEntries] = useState(false);
  const [journalEntriesError, setJournalEntriesError] = useState(null);
  const [entryCheckpointId, setEntryCheckpointId] = useState('');
  const [entryContent, setEntryContent] = useState('');
  const [savingEntry, setSavingEntry] = useState(false);
  const [savingEntryError, setSavingEntryError] = useState(null);

  useEffect(() => {
    async function loadInitialData() {
      try {
        const [gamesFromApi, progressFromApi] = await Promise.all([
          fetchGames(),
          fetchGameProgress(),
        ]);
        setGames(gamesFromApi);
        const progressMap = {};
        for (const progress of progressFromApi) {
          progressMap[progress.gameId] = progress;
        }
        setProgressByGameId(progressMap);
      } catch (requestError) {
        setError(requestError.message);
      } finally {
        setLoading(false);
      }
    }

    loadInitialData();
  }, []);

  async function loadCheckpoints(gameId) {
    setLoadingCheckpoints(true);
    setCheckpointsError(null);
    setCheckpoints([]);
    try {
      setCheckpoints(await fetchCheckpoints(gameId));
    } catch (requestError) {
      setCheckpointsError(requestError.message);
    } finally {
      setLoadingCheckpoints(false);
    }
  }

  async function loadJournalEntries(gameId) {
    setLoadingJournalEntries(true);
    setJournalEntriesError(null);
    setJournalEntries([]);
    try {
      setJournalEntries(await fetchJournalEntries(gameId));
    } catch (requestError) {
      setJournalEntriesError(requestError.message);
    } finally {
      setLoadingJournalEntries(false);
    }
  }

  function loadGameDetails(gameId) {
    const savedProgress = progressByGameId[gameId];
    setSelectedGameId(gameId);
    setEntryCheckpointId(savedProgress ? String(savedProgress.checkpointId) : '');
    setEntryContent('');
    setSavingEntryError(null);
    loadCheckpoints(gameId);
    loadJournalEntries(gameId);
  }

  async function selectCheckpoint(gameId, checkpoint) {
    setSavingProgress(true);
    setSavingProgressError(null);
    try {
      const savedProgress = await updateGameProgress(gameId, checkpoint.id);
      setProgressByGameId((currentProgress) => ({
        ...currentProgress,
        [savedProgress.gameId]: savedProgress,
      }));
      setEntryCheckpointId(String(savedProgress.checkpointId));
      if (selectedGameId === gameId) {
        await loadJournalEntries(gameId);
      }
    } catch (requestError) {
      setSavingProgressError(requestError.message);
    } finally {
      setSavingProgress(false);
    }
  }

  async function createEntry(event, gameId) {
    event.preventDefault();
    setSavingEntry(true);
    setSavingEntryError(null);
    try {
      await createJournalEntry(Number(entryCheckpointId), entryContent);
      setEntryContent('');
      await loadJournalEntries(gameId);
    } catch (requestError) {
      setSavingEntryError(requestError.message);
    } finally {
      setSavingEntry(false);
    }
  }

  if (loading) return <main className="page-state">Cargando catálogo...</main>;
  if (error) return <main className="page-state page-state-error">{error}</main>;

  const selectedGame = games.find((game) => game.id === selectedGameId);
  const savedProgress = selectedGame ? progressByGameId[selectedGame.id] : null;
  const availableCheckpoints = savedProgress
    ? checkpoints.filter((checkpoint) => checkpoint.position <= savedProgress.position)
    : [];

  return (
    <main className="app-shell">
      <header className="app-header">
        <div>
          <p className="eyebrow">Umbral</p>
          <h1>Bitácora sin spoilers</h1>
        </div>
        <p className="app-subtitle">Conversaciones según tu avance.</p>
      </header>

      <div className="workspace">
        <aside className="catalog-panel panel">
          <div className="section-heading">
            <p className="eyebrow">Catálogo</p>
            <h2>Elegí un juego</h2>
          </div>
          <ul className="game-list">
            {games.map((game) => {
              const gameProgress = progressByGameId[game.id];
              const isActive = game.id === selectedGameId;
              return (
                <li key={game.id}>
                  <button
                    className={`game-card ${isActive ? 'game-card-active' : ''}`}
                    onClick={() => loadGameDetails(game.id)}
                    type="button"
                  >
                    <span className="game-card-title">{game.title}</span>
                    <span className="game-card-description">{game.description}</span>
                    <span className="game-card-progress">
                      {gameProgress ? `Vas por: ${gameProgress.checkpointLabel}` : 'Todavía no elegiste tu avance'}
                    </span>
                  </button>
                </li>
              );
            })}
          </ul>
        </aside>

        <section className="detail-panel">
          {!selectedGame && (
            <section className="empty-selection panel">
              <p className="eyebrow">Tu espacio de juego</p>
              <h2>Elegí un juego para empezar</h2>
              <p>Consultá tu progreso, publicá sin spoilear y leé solamente lo que ya podés conocer.</p>
            </section>
          )}

          {selectedGame && (
            <>
              <section className="game-introduction panel">
                <p className="eyebrow">Juego seleccionado</p>
                <h2>{selectedGame.title}</h2>
                <p>{selectedGame.description}</p>
              </section>

              <section className="progress-panel panel">
                <div className="section-heading section-heading-row">
                  <div>
                    <p className="eyebrow">Tu avance</p>
                    <h2>Elegí hasta dónde llegaste</h2>
                  </div>
                  {savedProgress && <p className="progress-summary">Vas por <strong>{savedProgress.checkpointLabel}</strong></p>}
                </div>
                {loadingCheckpoints && <p className="status-message">Cargando checkpoints...</p>}
                {checkpointsError && <p className="status-message status-message-error">{checkpointsError}</p>}
                {!loadingCheckpoints && !checkpointsError && (
                  <ul className="checkpoint-list">
                    {checkpoints.map((checkpoint) => {
                      const isCurrentCheckpoint = savedProgress?.checkpointId === checkpoint.id;
                      return (
                        <li key={checkpoint.id}>
                          <button
                            className={`checkpoint-button ${isCurrentCheckpoint ? 'checkpoint-button-current' : ''}`}
                            disabled={savingProgress}
                            onClick={() => selectCheckpoint(selectedGame.id, checkpoint)}
                            type="button"
                          >
                            <span className="checkpoint-position">{String(checkpoint.position).padStart(2, '0')}</span>
                            <span>{checkpoint.label}</span>
                          </button>
                        </li>
                      );
                    })}
                  </ul>
                )}
                {savingProgress && <p className="status-message">Guardando progreso...</p>}
                {savingProgressError && <p className="status-message status-message-error">{savingProgressError}</p>}
              </section>

              <section className="composer-panel panel">
                <div className="section-heading">
                  <p className="eyebrow">Tu bitácora</p>
                  <h2>Publicá una entrada</h2>
                </div>
                {!savedProgress && <p className="status-message">Elegí tu progreso antes de publicar una entrada.</p>}
                {savedProgress && !loadingCheckpoints && !checkpointsError && (
                  <form className="entry-form" onSubmit={(event) => createEntry(event, selectedGame.id)}>
                    <label>
                      Esta entrada habla hasta
                      <select value={entryCheckpointId} onChange={(event) => setEntryCheckpointId(event.target.value)}>
                        {availableCheckpoints.map((checkpoint) => <option key={checkpoint.id} value={checkpoint.id}>{checkpoint.label}</option>)}
                      </select>
                    </label>
                    <label>
                      Tu entrada
                      <textarea value={entryContent} maxLength={5000} onChange={(event) => setEntryContent(event.target.value)} placeholder="Compartí lo que te dejó este tramo..." />
                    </label>
                    <div className="form-footer">
                      <p>Solo podés publicar sobre checkpoints que ya alcanzaste.</p>
                      <button className="button-primary" disabled={savingEntry || !entryCheckpointId || !entryContent.trim()} type="submit">
                        {savingEntry ? 'Publicando...' : 'Publicar'}
                      </button>
                    </div>
                  </form>
                )}
                {savingEntryError && <p className="status-message status-message-error">{savingEntryError}</p>}
              </section>

              <section className="journal-panel panel">
                <div className="section-heading">
                  <p className="eyebrow">Lectura segura</p>
                  <h2>Bitácora</h2>
                </div>
                {loadingJournalEntries && <p className="status-message">Cargando bitácora...</p>}
                {journalEntriesError && <p className="status-message status-message-error">{journalEntriesError}</p>}
                {!loadingJournalEntries && !journalEntriesError && journalEntries.length === 0 && (
                  <p className="empty-feed">Por ahora no hay nada que podamos mostrarte sin spoilearte. Volvé cuando avances un poco más.</p>
                )}
                {!loadingJournalEntries && !journalEntriesError && journalEntries.length > 0 && (
                  <ul className="journal-list">
                    {journalEntries.map((entry) => (
                      <li key={entry.id} className="journal-entry">
                        <p className="journal-entry-meta">{entry.authorHandle} · {entry.checkpointLabel}</p>
                        <p>{entry.content}</p>
                      </li>
                    ))}
                  </ul>
                )}
              </section>
            </>
          )}
        </section>
      </div>
    </main>
  );
}
