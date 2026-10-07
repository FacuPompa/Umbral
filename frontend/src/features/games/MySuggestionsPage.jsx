import { useEffect, useState } from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';
import { fetchMySuggestions } from './gameApi';
import LoadingIndicator from '@/components/LoadingIndicator';
import StatusMessage from '@/components/StatusMessage';
import GameArtwork from '@/components/GameArtwork';
import { Button } from '@/components/ui/button';
import { Field } from '@/components/ui/field';

const statuses = { PENDING: 'Pendiente', APPROVED: 'Aprobada', REJECTED: 'Rechazada' };
const dateFormatter = new Intl.DateTimeFormat('es-AR', { dateStyle: 'medium' });

export default function MySuggestionsPage() {
  const { user, loading: loadingUser } = useAuth();
  const location = useLocation();
  const [kind, setKind] = useState(() => new URLSearchParams(location.search).get('type') === 'checkpoints' ? 'checkpoints' : 'games');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [revision, setRevision] = useState(0);
  const [result, setResult] = useState(null);
  const key = JSON.stringify([user?.handle, kind, status, page, revision]);
  const activeResult = result?.key === key ? result : null;

  useEffect(() => {
    let cancelled = false;
    if (!user || loadingUser) return;
    fetchMySuggestions(kind, { status, page })
      .then((response) => { if (!cancelled) setResult({ ...response, key }); })
      .catch((error) => { if (!cancelled) setResult({ key, error: error.message }); });
    return () => { cancelled = true; };
  }, [user, loadingUser, kind, status, page, revision, key]);

  if (loadingUser) return <main id="main-content" className="py-12"><LoadingIndicator label="Recuperando sesión" showLabel /></main>;
  if (!user) return <Navigate replace state={{ from: location }} to="/login" />;

  return (
    <main id="main-content" className="grid w-full max-w-[840px] gap-8 py-8 md:py-12">
      <header className="grid gap-3">
        <h1 className="text-[28px] leading-[34px] font-semibold tracking-[-0.02em] md:text-4xl">Mis propuestas</h1>
        <p className="text-base leading-6 text-muted-foreground">Consultá el estado de los juegos y checkpoints que enviaste a revisión. Solo vos podés ver este historial.</p>
        <Button asChild variant="outline" className="justify-self-start"><Link to="/suggestions/new">Sugerir un juego</Link></Button>
      </header>

      <div className="grid gap-4 sm:grid-cols-2">
        <Field id="suggestion-kind" label="Tipo de propuesta">
          <select id="suggestion-kind" className="min-h-11 rounded-md border border-input bg-popover px-3 text-foreground focus-visible:outline-2 focus-visible:outline-ring" value={kind}
            onChange={(event) => { setKind(event.target.value); setPage(0); }}>
            <option value="games">Juegos</option><option value="checkpoints">Checkpoints</option>
          </select>
        </Field>
        <Field id="suggestion-status" label="Estado">
          <select id="suggestion-status" className="min-h-11 rounded-md border border-input bg-popover px-3 text-foreground focus-visible:outline-2 focus-visible:outline-ring" value={status}
            onChange={(event) => { setStatus(event.target.value); setPage(0); }}>
            <option value="">Todos los estados</option>
            {Object.entries(statuses).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
          </select>
        </Field>
      </div>

      <section aria-label="Historial de propuestas" aria-busy={!activeResult} className="grid gap-4">
        {!activeResult && <LoadingIndicator label="Cargando propuestas" showLabel />}
        {activeResult?.error && <><StatusMessage kind="error">{activeResult.error}</StatusMessage><Button variant="outline" className="justify-self-start" onClick={() => setRevision((value) => value + 1)}>Reintentar</Button></>}
        {activeResult?.items?.length === 0 && <StatusMessage>{status ? 'No tenés propuestas con este estado.' : 'Todavía no enviaste propuestas de este tipo.'} Los checkpoints se proponen desde el detalle de cada juego.</StatusMessage>}
        {Boolean(activeResult?.items?.length) && (
          <ol className="border-t border-border">
            {activeResult.items.map((suggestion) => (
              <li key={suggestion.id} className="flex items-start gap-4 border-b border-border py-5">
                {kind === 'games' && <GameArtwork title={suggestion.title} src={suggestion.coverImageUrl} className="h-20 w-16 shrink-0" />}
                <div className="grid min-w-0 flex-1 gap-2">
                  <h2 className="break-words text-lg font-semibold">{kind === 'games' ? suggestion.title : suggestion.label}</h2>
                  {kind === 'checkpoints' && <p className="text-sm text-muted-foreground"><Link className="underline underline-offset-4 hover:text-foreground" to={`/games/${suggestion.gameId}`}>{suggestion.gameTitle}</Link> · Posición {suggestion.position}</p>}
                  <p className="text-sm text-muted-foreground"><time dateTime={suggestion.createdAt}>Enviada el {dateFormatter.format(new Date(suggestion.createdAt))}</time></p>
                  <p className="text-sm font-medium">{statuses[suggestion.status]}</p>
                </div>
              </li>
            ))}
          </ol>
        )}
        <p className="text-sm leading-5 text-muted-foreground">Las propuestas pendientes no modifican el catálogo. Moderación debe aprobarlas antes de publicarlas.</p>
        <nav aria-label="Páginas del historial" className="flex flex-wrap items-center gap-3">
          <Button variant="outline" disabled={page === 0} onClick={() => setPage((value) => value - 1)}>Anterior</Button>
          <span className="text-sm text-muted-foreground" aria-live="polite">Página {page + 1}</span>
          <Button variant="outline" disabled={!activeResult?.hasNext} onClick={() => setPage((value) => value + 1)}>Siguiente</Button>
        </nav>
      </section>
    </main>
  );
}
