import { useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import LoadingIndicator from '../../components/LoadingIndicator';
import { useAuth } from '../auth/useAuth';
import { searchExternalGames, submitGameSuggestion } from './gameApi';
import { getGameInitials } from './gameArtwork';

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
  const [searching, setSearching] = useState(false);
  const [submittingId, setSubmittingId] = useState(null);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);

  if (loadingUser) {
    return <main className="page-state" id="main-content"><LoadingIndicator label="Recuperando sesión" /></main>;
  }

  if (!user) {
    return <Navigate replace state={{ from: location }} to="/login" />;
  }

  async function handleSearch(event) {
    event.preventDefault();
    setSearching(true);
    setError(null);
    setNotice(null);

    try {
      setResults(await searchExternalGames(query));
    } catch (requestError) {
      setError(requestError.message);
      setResults([]);
    } finally {
      setSearching(false);
    }
  }

  async function handleSuggestion(rawgGameId) {
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
    <main className="page-main suggestion-page" id="main-content">
      <header className="page-heading">
        <h1>Sumá un juego</h1>
        <p>
          Buscá una edición concreta. Si se aprueba, va a quedar disponible para que
          después podamos cargar sus checkpoints de forma segura.
        </p>
      </header>

      <form className="game-search-form" onSubmit={handleSearch}>
        <label htmlFor="game-search-query">Nombre del juego</label>
        <div>
          <input
            id="game-search-query"
            maxLength="100"
            minLength="2"
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Por ejemplo: Persona 3 Reload"
            required
            value={query}
          />
          <button className="button-primary" disabled={searching || query.trim().length < 2} type="submit">
            {searching ? <LoadingIndicator label="Buscando juegos" /> : 'Buscar'}
          </button>
        </div>
      </form>

      {notice && <p className="status-message" role="status">{notice}</p>}
      {error && <p className="status-message status-message-error" role="alert">{error}</p>}
      {searching && <div className="inline-loader"><LoadingIndicator label="Buscando juegos" /></div>}
      {!searching && results.length > 0 && (
        <div className="suggestion-results-wrap">
          <ol className="suggestion-results">
            {results.map((result) => (
              <li key={result.rawgGameId}>
                <article className="suggestion-result">
                  <div className="suggestion-result-artwork">
                    {result.coverImageUrl ? (
                      <img alt={`Portada de ${result.title}`} src={result.coverImageUrl} />
                    ) : (
                      <span aria-hidden="true">{getGameInitials(result.title)}</span>
                    )}
                  </div>
                  <div className="suggestion-result-copy">
                    <h2>{result.title}</h2>
                    <p>{formatReleaseDate(result.releaseDate)}</p>
                  </div>
                  <button
                    className="button-secondary"
                    disabled={submittingId === result.rawgGameId}
                    onClick={() => handleSuggestion(result.rawgGameId)}
                    type="button"
                  >
                    {submittingId === result.rawgGameId ? <LoadingIndicator label="Enviando sugerencia" /> : 'Sugerir'}
                  </button>
                </article>
              </li>
            ))}
          </ol>
          <p className="external-attribution">Datos e imágenes de <a href="https://rawg.io/" rel="noreferrer" target="_blank">RAWG</a>.</p>
        </div>
      )}
    </main>
  );
}
