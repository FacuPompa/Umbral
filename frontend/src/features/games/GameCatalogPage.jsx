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
      const checkpointsFromApi = await fetchCheckpoints(gameId);
      setCheckpoints(checkpointsFromApi);
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
      const entriesFromApi = await fetchJournalEntries(gameId);
      setJournalEntries(entriesFromApi);
    } catch (requestError) {
      setJournalEntriesError(requestError.message);
    } finally {
      setLoadingJournalEntries(false);
    }
  }

  function loadGameDetails(gameId) {
    const savedProgress = progressByGameId[gameId];

    setSelectedGameId(gameId);
    setEntryCheckpointId(
      savedProgress ? String(savedProgress.checkpointId) : ''
    );
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
      await createJournalEntry(
        Number(entryCheckpointId),
        entryContent
      );

      setEntryContent('');
      await loadJournalEntries(gameId);
    } catch (requestError) {
      setSavingEntryError(requestError.message);
    } finally {
      setSavingEntry(false);
    }
  }

  if (loading) {
    return <p>Cargando catálogo...</p>;
  }

  if (error) {
    return <p>{error}</p>;
  }

  return (
    <section>
      <h1>Umbral</h1>
      <h2>Catálogo</h2>

      {games.map((game) => {
        const savedProgress = progressByGameId[game.id];
        const availableCheckpoints = savedProgress
          ? checkpoints.filter(
              (checkpoint) => checkpoint.position <= savedProgress.position
            )
          : [];

        return (
          <article key={game.id}>
            <h3>{game.title}</h3>
            <p>{game.description}</p>

            <button onClick={() => loadGameDetails(game.id)}>
              Ver checkpoints
            </button>

            {savedProgress && (
              <p>Progreso guardado: {savedProgress.checkpointLabel}</p>
            )}

            {selectedGameId === game.id && (
              <section>
                <h4>Elegí tu progreso</h4>

                {loadingCheckpoints && <p>Cargando checkpoints...</p>}

                {checkpointsError && <p>{checkpointsError}</p>}

                {!loadingCheckpoints && !checkpointsError && (
                  <ul>
                    {checkpoints.map((checkpoint) => (
                      <li key={checkpoint.id}>
                        <button
                          disabled={savingProgress}
                          onClick={() => selectCheckpoint(game.id, checkpoint)}
                        >
                          {checkpoint.label}
                        </button>
                      </li>
                    ))}
                  </ul>
                )}

                {savingProgress && <p>Guardando progreso...</p>}

                {savingProgressError && <p>{savingProgressError}</p>}

                <section>
                  <h4>Publicá en la bitácora</h4>

                  {!savedProgress && (
                    <p>Elegí tu progreso antes de publicar una entrada.</p>
                  )}

                  {savedProgress &&
                    !loadingCheckpoints &&
                    !checkpointsError && (
                      <form onSubmit={(event) => createEntry(event, game.id)}>
                        <label>
                          Hasta qué checkpoint habla tu entrada
                          <select
                            value={entryCheckpointId}
                            onChange={(event) =>
                              setEntryCheckpointId(event.target.value)
                            }
                          >
                            {availableCheckpoints.map((checkpoint) => (
                              <option
                                key={checkpoint.id}
                                value={checkpoint.id}
                              >
                                {checkpoint.label}
                              </option>
                            ))}
                          </select>
                        </label>

                        <label>
                          Tu entrada
                          <textarea
                            value={entryContent}
                            maxLength={5000}
                            onChange={(event) =>
                              setEntryContent(event.target.value)
                            }
                          />
                        </label>

                        <button
                          disabled={
                            savingEntry ||
                            !entryCheckpointId ||
                            !entryContent.trim()
                          }
                          type="submit"
                        >
                          Publicar
                        </button>
                      </form>
                    )}

                  {savingEntry && <p>Publicando entrada...</p>}

                  {savingEntryError && <p>{savingEntryError}</p>}
                </section>

                <section>
                  <h4>Bitácora sin spoilers</h4>

                  {loadingJournalEntries && <p>Cargando bitácora...</p>}

                  {journalEntriesError && <p>{journalEntriesError}</p>}

                  {!loadingJournalEntries &&
                    !journalEntriesError &&
                    journalEntries.length === 0 && (
                      <p>
                        Por ahora no hay nada que podamos mostrarte sin
                        spoilearte. Volvé cuando avances un poco más.
                      </p>
                    )}

                  {!loadingJournalEntries &&
                    !journalEntriesError &&
                    journalEntries.length > 0 && (
                      <ul>
                        {journalEntries.map((entry) => (
                          <li key={entry.id}>
                            <article>
                              <p>
                                {entry.authorHandle} · {entry.checkpointLabel}
                              </p>
                              <p>{entry.content}</p>
                            </article>
                          </li>
                        ))}
                      </ul>
                    )}
                </section>
              </section>
            )}
          </article>
        );
      })}
    </section>
  );
}