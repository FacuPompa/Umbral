import { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import LoadingIndicator from '../../components/LoadingIndicator';
import { useAuth } from '../auth/useAuth';
import { approveGameSuggestion, fetchPendingGameSuggestions, rejectGameSuggestion } from './gameApi';
import { getGameInitials } from './gameArtwork';

export default function GameModerationPage() {
  const { user, loading: loadingUser } = useAuth();
  const [suggestions, setSuggestions] = useState([]);
  const [descriptions, setDescriptions] = useState({});
  const [loading, setLoading] = useState(true);
  const [workingId, setWorkingId] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!user || user.role !== 'MODERATOR') return;

    let active = true;

    async function loadSuggestions() {
      setLoading(true);
      setError(null);
      try {
        const pendingSuggestions = await fetchPendingGameSuggestions();
        if (active) setSuggestions(pendingSuggestions);
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
  }, [user]);

  if (loadingUser) {
    return <main className="page-state" id="main-content"><LoadingIndicator label="Recuperando sesión" /></main>;
  }

  if (!user || user.role !== 'MODERATOR') {
    return <Navigate replace to="/" />;
  }

  async function reviewSuggestion(suggestion, action) {
    setWorkingId(suggestion.id);
    setError(null);

    try {
      if (action === 'approve') {
        await approveGameSuggestion(suggestion.id, descriptions[suggestion.id] ?? '');
      } else {
        await rejectGameSuggestion(suggestion.id);
      }
      setSuggestions((currentSuggestions) => currentSuggestions.filter((item) => item.id !== suggestion.id));
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setWorkingId(null);
    }
  }

  return (
    <main className="page-main moderation-page" id="main-content">
      <header className="page-heading">
        <h1>Moderación de juegos</h1>
        <p>
          Al aprobar, el juego entra al catálogo local. Escribí una descripción breve y
          segura: los checkpoints se cargarán en un paso separado.
        </p>
      </header>

      {loading && <div className="inline-loader"><LoadingIndicator label="Cargando sugerencias pendientes" /></div>}
      {error && <p className="status-message status-message-error" role="alert">{error}</p>}
      {!loading && !error && suggestions.length === 0 && (
        <p className="status-message">No hay juegos pendientes de revisión.</p>
      )}
      {!loading && suggestions.length > 0 && (
        <div className="suggestion-results-wrap">
          <ol className="moderation-list">
            {suggestions.map((suggestion) => {
              const isWorking = workingId === suggestion.id;
              const description = descriptions[suggestion.id] ?? '';

              return (
                <li key={suggestion.id}>
                  <article className="moderation-item">
                    <div className="suggestion-result-artwork">
                      {suggestion.coverImageUrl ? (
                        <img alt={`Portada de ${suggestion.title}`} src={suggestion.coverImageUrl} />
                      ) : (
                        <span aria-hidden="true">{getGameInitials(suggestion.title)}</span>
                      )}
                    </div>
                    <div className="moderation-copy">
                      <h2>{suggestion.title}</h2>
                      <p>Sugerido por @{suggestion.suggestedByHandle}</p>
                      <label>
                        Descripción segura para el catálogo
                        <textarea
                          maxLength="255"
                          onChange={(event) => setDescriptions((current) => ({
                            ...current,
                            [suggestion.id]: event.target.value,
                          }))}
                          placeholder="Presentá el juego sin adelantar elementos de la historia."
                          value={description}
                        />
                      </label>
                      <div className="moderation-actions">
                        <button
                          className="button-primary"
                          disabled={isWorking || !description.trim()}
                          onClick={() => reviewSuggestion(suggestion, 'approve')}
                          type="button"
                        >
                          {isWorking ? <LoadingIndicator label="Revisando sugerencia" /> : 'Aprobar y publicar'}
                        </button>
                        <button
                          className="button-secondary"
                          disabled={isWorking}
                          onClick={() => reviewSuggestion(suggestion, 'reject')}
                          type="button"
                        >
                          Rechazar
                        </button>
                      </div>
                    </div>
                  </article>
                </li>
              );
            })}
          </ol>
          <p className="external-attribution">Datos e imágenes de <a href="https://rawg.io/" rel="noreferrer" target="_blank">RAWG</a>.</p>
        </div>
      )}
    </main>
  );
}
