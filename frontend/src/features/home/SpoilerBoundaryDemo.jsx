import { useEffect, useState } from 'react';
import { LockKeyhole } from 'lucide-react';
import { fetchCheckpoints } from '../games/gameApi';
import LoadingIndicator from '@/components/LoadingIndicator';
import StatusMessage from '@/components/StatusMessage';
import { Button } from '@/components/ui/button';

function CheckpointPreview({ gameId }) {
  const [checkpoints, setCheckpoints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [attempt, setAttempt] = useState(0);
  const [selectedIndex, setSelectedIndex] = useState(0);

  useEffect(() => {
    let active = true;
    fetchCheckpoints(gameId)
      .then((items) => {
        if (!active) return;
        setCheckpoints([...items].sort((a, b) => a.position - b.position));
        setSelectedIndex(0);
      })
      .catch(() => { if (active) setError(true); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [gameId, attempt]);

  if (loading) return <div className="py-12"><LoadingIndicator label="Cargando tramos del juego" showLabel /></div>;
  if (error) return <div className="grid justify-items-start gap-4 py-6"><StatusMessage kind="error">No pudimos cargar los tramos para la demostración.</StatusMessage><Button variant="outline" onClick={() => { setLoading(true); setError(false); setAttempt((value) => value + 1); }}>Reintentar</Button></div>;
  if (!checkpoints.length) return <StatusMessage>Este juego todavía no tiene tramos aprobados. Probá con otro del catálogo.</StatusMessage>;

  const current = checkpoints[selectedIndex];
  return (
    <div className="grid gap-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <span className="text-sm text-muted-foreground">Probá mover tu límite de lectura</span>
        <div className="flex gap-2">
          <Button variant="outline" disabled={selectedIndex === 0} onClick={() => setSelectedIndex((value) => value - 1)}>Retroceder</Button>
          <Button variant="outline" disabled={selectedIndex === checkpoints.length - 1} onClick={() => setSelectedIndex((value) => value + 1)}>Avanzar un tramo</Button>
        </div>
      </div>
      <div aria-live="polite" aria-atomic="true">
        <ol className="border-l border-border pl-5">
          {checkpoints.slice(0, selectedIndex + 1).map((checkpoint) => (
            <li key={checkpoint.id} className="flex items-baseline gap-4 py-3">
              <span className="text-sm tabular-nums text-muted-foreground">{checkpoint.position}</span>
              <span className={checkpoint.id === current.id ? 'font-semibold' : 'text-muted-foreground'}>{checkpoint.label}</span>
            </li>
          ))}
        </ol>
        <div className="my-4 border-t-2 border-primary pt-4">
          <p className="font-semibold text-primary">Hasta acá podés leer</p>
          <p className="mt-2 text-sm leading-5 text-muted-foreground">Conversaciones de «{current.label}» y de los tramos anteriores.</p>
        </div>
        {selectedIndex < checkpoints.length - 1 ? (
          <div className="flex items-start gap-3 bg-background p-4">
            <LockKeyhole className="mt-0.5 size-5 shrink-0 text-muted-foreground" aria-hidden="true" />
            <div><p className="font-medium">Lo que sigue, todavía oculto</p><p className="mt-1 text-sm leading-5 text-muted-foreground">Las conversaciones posteriores quedan fuera de tu lectura hasta que avances.</p></div>
          </div>
        ) : <p className="text-sm leading-5 text-muted-foreground">Alcanzaste el último tramo disponible en Umbral para este juego.</p>}
      </div>
    </div>
  );
}

export default function SpoilerBoundaryDemo({ games, loading, error }) {
  const [gameId, setGameId] = useState('');
  const selectedGame = games.find((game) => String(game.id) === gameId) ?? games[0];
  return (
    <section className="bg-surface-subtle py-12 md:py-16" id="como-funciona" aria-labelledby="safe-title">
      <div className="page-container grid gap-8 md:grid-cols-[minmax(0,1fr)_minmax(0,1.4fr)] md:gap-12">
        <header className="grid content-start gap-4">
          <h2 className="text-2xl leading-[30px] font-semibold" id="safe-title">Tu progreso decide qué aparece</h2>
          <p className="max-w-[440px] leading-6 text-muted-foreground">Elegí un juego y avanzá por sus tramos para ver dónde queda la frontera de lectura.</p>
          <p className="max-w-[440px] text-sm leading-5 text-muted-foreground">Es una demostración: no cambia tu progreso ni muestra publicaciones. En tu partida, vos decidís hasta dónde llegaste.</p>
        </header>
        <div className="min-w-0">
          {loading ? <LoadingIndicator label="Cargando juegos para la demostración" showLabel /> : error ? <StatusMessage>La demostración estará disponible cuando se cargue el catálogo.</StatusMessage> : !selectedGame ? <StatusMessage>La demostración estará disponible cuando haya juegos en el catálogo.</StatusMessage> : (
            <div className="grid gap-6">
              <div className="grid gap-2">
                <label htmlFor="demo-game" className="font-medium">Juego para probar</label>
                <select id="demo-game" value={selectedGame.id} onChange={(event) => setGameId(event.target.value)} className="min-h-11 w-full min-w-0 rounded-md border border-input bg-popover px-3 py-2 text-foreground">
                  {games.map((game) => <option value={game.id} key={game.id}>{game.title}</option>)}
                </select>
              </div>
              <CheckpointPreview key={selectedGame.id} gameId={selectedGame.id} />
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
