import { useEffect, useState } from 'react';
import { fetchGames } from '../games/gameApi';
import GameShelf from '../games/GameShelf';
import LoadingIndicator from '@/components/LoadingIndicator';
import StatusMessage from '@/components/StatusMessage';
import { Button } from '@/components/ui/button';

export default function SearchDiscovery() {
  const [games, setGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    let active = true;
    fetchGames()
      .then((items) => { if (active) setGames(items.slice(0, 3)); })
      .catch(() => { if (active) setError(true); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [attempt]);

  return (
    <section className="grid min-w-0 gap-5" aria-labelledby="discovery-title">
      <header className="grid gap-2">
        <h2 id="discovery-title" className="text-xl leading-7 font-semibold">Explorá el catálogo</h2>
        <p className="text-sm leading-5 text-muted-foreground">Podés empezar por un juego o buscar a alguien por su nombre de usuario.</p>
      </header>
      {loading ? <LoadingIndicator label="Cargando juegos para explorar" showLabel /> : error ? (
        <div className="grid justify-items-start gap-3">
          <StatusMessage kind="error">No pudimos cargar los juegos para explorar. Podés seguir usando el buscador.</StatusMessage>
          <Button variant="outline" onClick={() => { setLoading(true); setError(false); setAttempt((value) => value + 1); }}>Reintentar catálogo</Button>
        </div>
      ) : games.length ? <GameShelf games={games} /> : <StatusMessage>Todavía no hay juegos en el catálogo. Podés buscar perfiles de la comunidad.</StatusMessage>}
    </section>
  );
}
