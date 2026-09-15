import { useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import LoadingIndicator from '../../components/LoadingIndicator';
import { useAuth } from '../auth/useAuth';
import { searchExternalGames, submitGameSuggestion } from './gameApi';
import { Search, Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Field, Input } from '@/components/ui/field';
import StatusMessage from '@/components/StatusMessage';
import GameArtwork from '@/components/GameArtwork';

function formatReleaseDate(releaseDate) {
  if (!releaseDate) return 'Fecha de lanzamiento no informada';

  return new Intl.DateTimeFormat('es-AR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(new Date(`${releaseDate}T12:00:00`));
}

export default function GameSuggestionPage() {
  const { user, loading: loadingUser } = useAuth();
  const location = useLocation();
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [hasSearched, setHasSearched] = useState(false);
  const [searching, setSearching] = useState(false);
  const [submittingId, setSubmittingId] = useState(null);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);

  if (loadingUser) {
    return <main className="py-12" id="main-content"><LoadingIndicator label="Recuperando sesión" showLabel /></main>;
  }

  if (!user) {
    return <Navigate replace state={{ from: location }} to="/login" />;
  }

  async function handleSearch(event) {
    event.preventDefault();
    if (searching || submittingId !== null || query.trim().length < 2) return;
    setSearching(true);
    setError(null);
    setNotice(null);

    try {
      setResults(await searchExternalGames(query.trim()));
      setHasSearched(true);
    } catch (requestError) {
      setError(requestError.message);
      setResults([]);
    } finally {
      setSearching(false);
    }
  }

  async function handleSuggestion(rawgGameId) {
    if (submittingId !== null) return;
    setSubmittingId(rawgGameId);
    setError(null);
    setNotice(null);

    try {
      const suggestion = await submitGameSuggestion(rawgGameId);
      setNotice(`“${suggestion.title}” quedó enviado para revisión.`);
      setResults((currentResults) => currentResults.filter((result) => result.rawgGameId !== rawgGameId));
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSubmittingId(null);
    }
  }

  return (
    <main className="grid w-full max-w-[840px] gap-8 py-8 md:gap-10 md:py-12" id="main-content">
      <header className="grid gap-3">
        <h1 className="text-[28px] leading-[34px] font-semibold tracking-[-0.02em] md:text-4xl md:leading-[42px]">Sumá un juego</h1>
        <p className="max-w-[660px] text-base leading-6 text-muted-foreground">
          Buscá una edición concreta y enviala a revisión. Si se aprueba, va a aparecer
          en el catálogo; sus checkpoints se proponen y revisan por separado.
        </p>
      </header>

      <form className="border-t border-border pt-8" onSubmit={handleSearch} aria-busy={searching}>
        <Field id="game-search-query" label="Nombre del juego" hint="Buscamos ediciones en RAWG, no solo los juegos que ya están en Umbral.">
          <div className="grid gap-3 sm:grid-cols-[minmax(0,1fr)_auto]">
            <Input id="game-search-query" maxLength={100} minLength={2}
              aria-describedby="game-search-query-hint" onChange={(event) => setQuery(event.target.value)}
              placeholder="Por ejemplo: Persona 3 Reload" required value={query} />
            <Button disabled={searching || submittingId !== null || query.trim().length < 2} type="submit">
              {searching ? <LoadingIndicator label="Buscando juegos" showLabel /> : <><Search aria-hidden="true" />Buscar</>}
            </Button>
          </div>
        </Field>
      </form>

      {notice && <StatusMessage kind="success">{notice}</StatusMessage>}
      {error && <StatusMessage kind="error">{error}</StatusMessage>}
      {!searching && hasSearched && results.length === 0 && !notice && !error && (
        <StatusMessage>No encontramos ediciones con ese nombre. Probá con otro título.</StatusMessage>
      )}
      {!searching && results.length > 0 && (
        <section aria-labelledby="suggestion-results-title" className="grid gap-4">
          <h2 className="text-2xl leading-[30px] font-semibold tracking-normal" id="suggestion-results-title">Ediciones encontradas</h2>
          <ol className="border-t border-border">
            {results.map((result) => (
              <li key={result.rawgGameId} className="border-b border-border py-5">
                <article className="grid grid-cols-[64px_minmax(0,1fr)] items-center gap-x-4 gap-y-3 sm:grid-cols-[80px_minmax(0,1fr)_auto] sm:gap-6">
                  <GameArtwork title={result.title} src={result.coverImageUrl} className="row-span-2 h-[88px] w-16 sm:row-span-1 sm:h-[104px] sm:w-20" />
                  <div className="min-w-0">
                    <h3 className="break-words text-lg leading-6 font-semibold">{result.title}</h3>
                    <p className="mt-2 text-sm leading-5 text-muted-foreground">{formatReleaseDate(result.releaseDate)}</p>
                  </div>
                  <Button variant="outline" className="col-start-2 justify-self-start sm:col-start-auto"
                    disabled={submittingId !== null} onClick={() => handleSuggestion(result.rawgGameId)} type="button"
                    aria-label={submittingId === result.rawgGameId ? 'Enviando sugerencia' : `Sugerir ${result.title}`}>
                    {submittingId === result.rawgGameId ? <LoadingIndicator label="Enviando sugerencia" showLabel /> : <><Plus aria-hidden="true" />Sugerir</>}
                  </Button>
                </article>
              </li>
            ))}
          </ol>
          <p className="text-sm leading-5 text-muted-foreground">Datos e imágenes de <a className="underline underline-offset-4 hover:text-foreground" href="https://rawg.io/" rel="noreferrer" target="_blank">RAWG</a>.</p>
        </section>
      )}
    </main>
  );
}
