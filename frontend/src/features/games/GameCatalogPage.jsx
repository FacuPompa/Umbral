import { useEffect, useState } from 'react';
import { fetchGames } from './gameApi';

export default function GameCatalogPage() {
  const [games, setGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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
        </article>
      ))}
    </section>
  );
}