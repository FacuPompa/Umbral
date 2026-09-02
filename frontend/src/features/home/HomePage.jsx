import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import HeroGameCarousel from '../games/HeroGameCarousel';
import { fetchGames } from '../games/gameApi';
import { getGameArtwork, getGameInitials } from '../games/gameArtwork';

const conversationTypes = [
  ['Dudas', 'Pedí una mano en el punto exacto donde estás jugando.'],
  ['Reflexiones', 'Contá qué te dejó una escena o un tramo de la historia.'],
  ['Teorías', 'Conectá pistas con otras personas que llegaron hasta ahí.'],
  ['Reseñas', 'Compartí una mirada completa cuando termines el juego.'],
];

export default function HomePage() {
  const [games, setGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function loadCatalog() {
      try {
        setGames(await fetchGames());
      } catch (requestError) {
        setError(requestError.message);
      } finally {
        setLoading(false);
      }
    }

    loadCatalog();
  }, []);

  return (
    <main className="landing-page">
      <section className="landing-hero" aria-labelledby="hero-title">
        <div className="landing-hero-copy">
          <h1 id="hero-title">Hablá de juegos sin adelantarte la historia.</h1>
          <p>
            Umbral es una comunidad para preguntar, responder y compartir lo que te dejó
            un juego. Marcá hasta dónde llegaste y el resto queda fuera de vista.
          </p>
          <div className="hero-actions">
            <a className="button button-primary-link" href="#catalogo">Explorar juegos</a>
            <a className="text-action" href="#como-funciona">Cómo funciona</a>
          </div>
        </div>

        <div className="landing-hero-carousel">
          {loading && <div className="hero-carousel-placeholder">Cargando catálogo...</div>}
          {error && <div className="hero-carousel-placeholder">El catálogo no está disponible.</div>}
          {!loading && !error && <HeroGameCarousel games={games} />}
        </div>
      </section>

      <section className="landing-section" id="como-funciona" aria-labelledby="safe-title">
        <header className="section-heading">
          <h2 id="safe-title">Tu progreso decide qué aparece</h2>
          <p>No dependemos solo de que alguien recuerde escribir “spoiler” en el título.</p>
        </header>
        <ol className="safety-steps">
          <li>
            <span>01</span>
            <div>
              <h3>Elegís un juego</h3>
              <p>Entrás al detalle y ves los tramos narrativos disponibles.</p>
            </div>
          </li>
          <li>
            <span>02</span>
            <div>
              <h3>Guardás tu avance</h3>
              <p>Indicás el último checkpoint que alcanzaste en tu partida.</p>
            </div>
          </li>
          <li>
            <span>03</span>
            <div>
              <h3>Umbral filtra el resto</h3>
              <p>El servidor entrega solamente publicaciones que ya son seguras para vos.</p>
            </div>
          </li>
        </ol>
      </section>

      <section className="community-section" aria-labelledby="community-title">
        <div className="community-intro">
          <h2 id="community-title">Un foro, no una lista de puntajes</h2>
          <p>
            Las reseñas tienen lugar, pero el centro son las conversaciones: preguntar,
            responder y pensar una historia con otras personas.
          </p>
        </div>
        <ol className="conversation-list">
          {conversationTypes.map(([title, description], index) => (
            <li key={title}>
              <span>{String(index + 1).padStart(2, '0')}</span>
              <h3>{title}</h3>
              <p>{description}</p>
            </li>
          ))}
        </ol>
      </section>

      <section className="catalog-section" id="catalogo" aria-labelledby="catalog-title">
        <header className="section-heading section-heading-row">
          <div>
            <h2 id="catalog-title">Juegos en Umbral</h2>
            <p>Abrí un juego para registrar tu progreso y entrar a su conversación.</p>
          </div>
          <span>{games.length} {games.length === 1 ? 'juego disponible' : 'juegos disponibles'}</span>
        </header>

        {loading && <p className="catalog-message">Cargando juegos...</p>}
        {error && <p className="catalog-message catalog-message-error">{error}</p>}
        {!loading && !error && games.length === 0 && <p className="catalog-message">Todavía no hay juegos disponibles.</p>}
        {!loading && !error && games.length > 0 && (
          <ol className="catalog-list">
            {games.map((game) => {
              const artwork = getGameArtwork(game.title);

              return (
                <li key={game.id}>
                  <article className="catalog-list-item">
                    <Link className="catalog-list-artwork" to={`/games/${game.id}`} aria-label={`Abrir ${game.title}`}>
                      {artwork ? <img src={artwork} alt={`Arte de ${game.title}`} /> : <span>{getGameInitials(game.title)}</span>}
                    </Link>
                    <div className="catalog-list-copy">
                      <h3>{game.title}</h3>
                      <p>{game.description}</p>
                    </div>
                    <Link className="text-action catalog-list-action" to={`/games/${game.id}`}>Ver juego</Link>
                  </article>
                </li>
              );
            })}
          </ol>
        )}
      </section>

      <footer className="site-footer">
        <strong>Umbral</strong>
        <p>Conversaciones sobre juegos narrativos, sin adelantarte nada.</p>
      </footer>
    </main>
  );
}
