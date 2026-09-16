import { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import LoadingIndicator from '../../components/LoadingIndicator';
import { useAuth } from '../auth/useAuth';
import {
  approveCheckpointSuggestion,
  approveGameSuggestion,
  fetchPendingCheckpointSuggestions,
  fetchPendingGameSuggestions,
  rejectCheckpointSuggestion,
  rejectGameSuggestion,
} from './gameApi';
import GameArtwork from '../../components/GameArtwork';
import PageHeading from '../../components/PageHeading';
import StatusMessage from '../../components/StatusMessage';
import { Button } from '../../components/ui/button';
import { Field, Textarea } from '../../components/ui/field';

export default function GameModerationPage() {
  const { user, loading: loadingUser } = useAuth();
  const [suggestions, setSuggestions] = useState([]);
  const [checkpointSuggestions, setCheckpointSuggestions] = useState([]);
  const [descriptions, setDescriptions] = useState({});
  const [loading, setLoading] = useState(true);
  const [reviews, setReviews] = useState({});
  const [reloadKey, setReloadKey] = useState(0);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!user || user.role !== 'MODERATOR') return;

    let active = true;

    async function loadSuggestions() {
      setLoading(true);
      setError(null);
      try {
        const [pendingGameSuggestions, pendingCheckpointSuggestions] = await Promise.all([
          fetchPendingGameSuggestions(),
          fetchPendingCheckpointSuggestions(),
        ]);
        if (active) {
          setSuggestions(pendingGameSuggestions);
          setCheckpointSuggestions(pendingCheckpointSuggestions);
        }
      } catch (requestError) {
        if (active) setError(requestError.message);
      } finally {
        if (active) setLoading(false);
      }
    }

    loadSuggestions();
    return () => {
      active = false;
    };
  }, [user, reloadKey]);

  if (loadingUser) {
    return <main className="grid min-h-[65vh] place-items-center text-center text-muted-foreground" id="main-content"><LoadingIndicator label="Recuperando sesión" /></main>;
  }

  if (!user || user.role !== 'MODERATOR') {
    return <Navigate replace to="/" />;
  }

  async function reviewSuggestion(suggestion, action) {
    const key = `game-${suggestion.id}`;
    setReviews((current) => ({ ...current, [key]: { action } }));

    try {
      if (action === 'approve') {
        await approveGameSuggestion(suggestion.id, descriptions[suggestion.id] ?? '');
      } else {
        await rejectGameSuggestion(suggestion.id);
      }
      setSuggestions((currentSuggestions) => currentSuggestions.filter((item) => item.id !== suggestion.id));
    } catch (requestError) {
      setReviews((current) => ({ ...current, [key]: { error: requestError.message } }));
    }
  }

  async function reviewCheckpointSuggestion(suggestion, action) {
    const key = `checkpoint-${suggestion.id}`;
    setReviews((current) => ({ ...current, [key]: { action } }));

    try {
      if (action === 'approve') {
        await approveCheckpointSuggestion(suggestion.id);
      } else {
        await rejectCheckpointSuggestion(suggestion.id);
      }
      setCheckpointSuggestions((currentSuggestions) => currentSuggestions.filter((item) => item.id !== suggestion.id));
    } catch (requestError) {
      setReviews((current) => ({ ...current, [key]: { error: requestError.message } }));
    }
  }

  return (
    <main className="mx-auto grid w-full max-w-[900px] gap-8 py-8 md:gap-12 md:py-12" id="main-content">
      <PageHeading title="Moderación" description="Revisá los juegos y checkpoints propuestos antes de incorporarlos al catálogo y habilitar conversaciones." />
      {loading && <LoadingIndicator label="Cargando sugerencias pendientes" />}
      {error && (
        <div className="grid justify-items-start gap-3">
          <StatusMessage kind="error">{error}</StatusMessage>
          <Button variant="outline" onClick={() => setReloadKey((key) => key + 1)}>Reintentar carga</Button>
        </div>
      )}
      {!loading && !error && (
        <>
          <section aria-labelledby="game-suggestions-title" className="grid gap-4">
            <h2 className="text-2xl font-semibold leading-[30px]" id="game-suggestions-title">Juegos pendientes</h2>
            {suggestions.length === 0 ? <StatusMessage>No hay juegos pendientes de revisión.</StatusMessage> : (
              <ol className="divide-y divide-border border-y border-border">
                {suggestions.map((suggestion) => {
                  const review = reviews[`game-${suggestion.id}`] ?? {};
                  const isWorking = Boolean(review.action);
                  const description = descriptions[suggestion.id] ?? '';
                  const fieldId = `description-${suggestion.id}`;
                  return (
                    <li key={suggestion.id} className="py-6">
                      <article aria-labelledby={`game-title-${suggestion.id}`} className="grid min-w-0 gap-4 sm:grid-cols-[88px_minmax(0,1fr)] sm:gap-6">
                        <GameArtwork src={suggestion.coverImageUrl} title={suggestion.title} className="h-24 w-16 sm:h-28 sm:w-[88px]" />
                        <div className="grid min-w-0 gap-4">
                          <div className="grid gap-1">
                            <h3 id={`game-title-${suggestion.id}`} className="break-words text-xl font-semibold leading-7">{suggestion.title}</h3>
                            <p className="break-words text-sm leading-5 text-muted-foreground">Sugerido por @{suggestion.suggestedByHandle}</p>
                          </div>
                          <Field id={fieldId} label="Descripción segura para el catálogo" hint="Presentá el juego sin adelantar la historia. Hasta 255 caracteres.">
                            <Textarea id={fieldId} aria-describedby={`${fieldId}-hint`} maxLength={255} disabled={isWorking}
                              value={description} onChange={(event) => setDescriptions((current) => ({ ...current, [suggestion.id]: event.target.value }))} />
                          </Field>
                          {review.error && <StatusMessage kind="error">{review.error}</StatusMessage>}
                          <div className="flex flex-wrap gap-3">
                            <Button disabled={isWorking || !description.trim()} onClick={() => reviewSuggestion(suggestion, 'approve')}>
                              {review.action === 'approve' ? <LoadingIndicator label="Aprobando juego" /> : 'Aprobar y publicar'}
                            </Button>
                            <Button variant="outline" disabled={isWorking} onClick={() => reviewSuggestion(suggestion, 'reject')}>
                              {review.action === 'reject' ? <LoadingIndicator label="Rechazando juego" /> : 'Rechazar'}
                            </Button>
                          </div>
                        </div>
                      </article>
                    </li>
                  );
                })}
              </ol>
            )}
            {suggestions.length > 0 && <p className="text-sm leading-5 text-muted-foreground">Datos e imágenes de <a className="underline underline-offset-4" href="https://rawg.io/" rel="noreferrer" target="_blank">RAWG</a>.</p>}
          </section>
          <section aria-labelledby="checkpoint-suggestions-title" className="grid gap-4">
            <h2 className="text-2xl font-semibold leading-[30px]" id="checkpoint-suggestions-title">Checkpoints pendientes</h2>
            {checkpointSuggestions.length === 0 ? <StatusMessage>No hay checkpoints pendientes de revisión.</StatusMessage> : (
              <ol className="divide-y divide-border border-y border-border">
                {checkpointSuggestions.map((suggestion) => {
                  const review = reviews[`checkpoint-${suggestion.id}`] ?? {};
                  const isWorking = Boolean(review.action);
                  return (
                    <li key={suggestion.id} className="py-6">
                      <article aria-labelledby={`checkpoint-title-${suggestion.id}`} className="grid min-w-0 gap-4">
                        <div className="grid gap-1">
                          <h3 id={`checkpoint-title-${suggestion.id}`} className="break-words text-xl font-semibold leading-7">{suggestion.label}</h3>
                          <p className="break-words text-base leading-6">{suggestion.gameTitle} · posición {suggestion.position}</p>
                          <p className="break-words text-sm leading-5 text-muted-foreground">Sugerido por @{suggestion.suggestedByHandle}</p>
                        </div>
                        {review.error && <StatusMessage kind="error">{review.error}</StatusMessage>}
                        <div className="flex flex-wrap gap-3">
                          <Button disabled={isWorking} onClick={() => reviewCheckpointSuggestion(suggestion, 'approve')}>
                            {review.action === 'approve' ? <LoadingIndicator label="Aprobando checkpoint" /> : 'Aprobar checkpoint'}
                          </Button>
                          <Button variant="outline" disabled={isWorking} onClick={() => reviewCheckpointSuggestion(suggestion, 'reject')}>
                            {review.action === 'reject' ? <LoadingIndicator label="Rechazando checkpoint" /> : 'Rechazar'}
                          </Button>
                        </div>
                      </article>
                    </li>
                  );
                })}
              </ol>
            )}
          </section>
        </>
      )}
    </main>
  );
}
