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
import { getGameInitials } from './gameArtwork';

export default function GameModerationPage() {
  const { user, loading: loadingUser } = useAuth();
  const [suggestions, setSuggestions] = useState([]);
  const [checkpointSuggestions, setCheckpointSuggestions] = useState([]);
  const [descriptions, setDescriptions] = useState({});
  const [loading, setLoading] = useState(true);
  const [workingId, setWorkingId] = useState(null);
  const [workingCheckpointId, setWorkingCheckpointId] = useState(null);
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

  async function reviewCheckpointSuggestion(suggestion, action) {
    setWorkingCheckpointId(suggestion.id);
    setError(null);

    try {
      if (action === 'approve') {
        await approveCheckpointSuggestion(suggestion.id);
      } else {
        await rejectCheckpointSuggestion(suggestion.id);
      }
      setCheckpointSuggestions((currentSuggestions) => currentSuggestions.filter((item) => item.id !== suggestion.id));
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setWorkingCheckpointId(null);
    }
  }

  return (
    <main className="page-main moderation-page" id="main-content">
      <header className="page-heading">
        <h1>Moderación</h1>
        <p>
          Los juegos y los checkpoints se revisan antes de entrar al catálogo y de
          habilitar conversaciones.
        </p>
      </header>

      {loading && <div className="inline-loader"><LoadingIndicator label="Cargando sugerencias pendientes" /></div>}
      {error && <p className="status-message status-message-error" role="alert">{error}</p>}
      {!loading && !error && suggestions.length === 0 && checkpointSuggestions.length === 0 && (
        <p className="status-message">No hay propuestas pendientes de revisión.</p>
      )}
      {!loading && suggestions.length > 0 && (
        <section className="suggestion-results-wrap" aria-labelledby="game-suggestions-title">
          <h2 className="moderation-section-title" id="game-suggestions-title">Juegos pendientes</h2>
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
        </section>
      )}
      {!loading && checkpointSuggestions.length > 0 && (
        <section className="suggestion-results-wrap" aria-labelledby="checkpoint-suggestions-title">
          <h2 className="moderation-section-title" id="checkpoint-suggestions-title">Checkpoints pendientes</h2>
          <ol className="moderation-list">
            {checkpointSuggestions.map((suggestion) => {
              const isWorking = workingCheckpointId === suggestion.id;

              return (
                <li key={suggestion.id}>
                  <article className="moderation-item">
                    <div className="suggestion-result-artwork checkpoint-suggestion-marker" aria-hidden="true">
                      {String(suggestion.position).padStart(2, '0')}
                    </div>
                    <div className="moderation-copy">
                      <h2>{suggestion.label}</h2>
                      <p>{suggestion.gameTitle} · posición {suggestion.position} · sugerido por @{suggestion.suggestedByHandle}</p>
                      <div className="moderation-actions">
                        <button
                          className="button-primary"
                          disabled={isWorking}
                          onClick={() => reviewCheckpointSuggestion(suggestion, 'approve')}
                          type="button"
                        >
                          {isWorking ? <LoadingIndicator label="Revisando checkpoint" /> : 'Aprobar checkpoint'}
                        </button>
                        <button
                          className="button-secondary"
                          disabled={isWorking}
                          onClick={() => reviewCheckpointSuggestion(suggestion, 'reject')}
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
        </section>
      )}
    </main>
  );
}
