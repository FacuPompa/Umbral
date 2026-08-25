import { useEffect, useState } from 'react';
import {
  fetchCheckpoints,
  fetchGameProgress,
  fetchGames,
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
    setSelectedGameId(gameId);

    try {
      const checkpointsFromApi = await fetchCheckpoints(gameId);
      setCheckpoints(checkpointsFromApi);
    } catch (requestError) {
      setCheckpointsError(requestError.message);
    } finally {
      setLoadingCheckpoints(false);
    }
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
    } catch (requestError) {
      setSavingProgressError(requestError.message);
    } finally {
      setSavingProgress(false);
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

        return (
          <article key={game.id}>
            <h3>{game.title}</h3>
            <p>{game.description}</p>

            <button onClick={() => loadCheckpoints(game.id)}>
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
              </section>
            )}
          </article>
        );
      })}
    </section>
  );
}