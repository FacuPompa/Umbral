import { useEffect, useState } from 'react';
import { fetchCheckpoints, fetchGames } from './gameApi';

export default function GameCatalogPage() {
  const [games, setGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [checkpoints, setCheckpoints] = useState([]);
  const [loadingCheckpoints, setLoadingCheckpoints] = useState(false);
  const [checkpointsError, setCheckpointsError] = useState(null);
  const [selectedGameId, setSelectedGameId] = useState(null);
  const [selectedCheckpoint, setSelectedCheckpoint] = useState(null);

  useEffect(() => {
    async function loadGames() {
      try {
        const gamesFromApi = await fetchGames();
        setGames(gamesFromApi);
      } catch (requestError) {
        setError(requestError.message);
      } finally {
        setLoading(false);
      }
    }

    loadGames();
  }, []);

  async function loadCheckpoints(gameId) {
    setLoadingCheckpoints(true);
    setCheckpointsError(null);
    setSelectedGameId(gameId);
    setSelectedCheckpoint(null);

    try {
      const checkpointsFromApi = await fetchCheckpoints(gameId);
      setCheckpoints(checkpointsFromApi);
    } catch (requestError) {
      setCheckpointsError(requestError.message);
    } finally {
      setLoadingCheckpoints(false);
    }
  }

  function selectCheckpoint(checkpoint) {
    setSelectedCheckpoint(checkpoint);
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

      {games.map((game) => (
        <article key={game.id}>
          <h3>{game.title}</h3>
          <p>{game.description}</p>

          <button onClick={() => loadCheckpoints(game.id)}>
            Ver checkpoints
          </button>

          {selectedGameId === game.id && (
            <section>
              <h4>Elegí tu progreso</h4>

              {loadingCheckpoints && <p>Cargando checkpoints...</p>}

              {checkpointsError && <p>{checkpointsError}</p>}

              {!loadingCheckpoints && !checkpointsError && (
                <ul>
                  {checkpoints.map((checkpoint) => (
                    <li key={checkpoint.id}>
                      <button onClick={() => selectCheckpoint(checkpoint)}>
                        {checkpoint.label}
                      </button>
                    </li>
                  ))}
                </ul>
              )}

              {selectedCheckpoint && (
                <p>
                  Progreso seleccionado: {selectedCheckpoint.label}
                </p>
              )}
            </section>
          )}
        </article>
      ))}
    </section>
  );
}