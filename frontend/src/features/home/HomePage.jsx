import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchGames } from '../games/gameApi';

const conversationTypes = [
  ['Reflexiones', 'Para compartir qué te dejó un tramo sin tener que calificarlo.'],
  ['Dudas', 'Para pedir una mano sin recibir respuestas de más.'],
  ['Teorías', 'Para conectar pistas con otras personas que llegaron hasta ahí.'],
  ['Reseñas', 'Para mirar una experiencia completa cuando ya estés listo.'],
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
          <p className="eyebrow">Comunidad sin spoilers</p>
          <h1 id="hero-title">Conversá sobre un juego sin enterarte de lo que sigue.</h1>
          <p className="hero-description">
            En Umbral cada conversación tiene un límite narrativo. Marcás hasta dónde jugaste y te mostramos solo lo que ya podés conocer.
          </p>
          <div className="hero-actions">
            <a className="button button-accent" href="#catalogo">Explorar catálogo</a>
            <a className="text-action" href="#como-funciona">Ver cómo funciona <span aria-hidden="true">↓</span></a>
          </div>
        </div>

        <aside className="mechanism-example" aria-label="Ejemplo conceptual de cómo funciona el filtro de spoilers">
          <p className="eyebrow">El límite lo pone tu partida</p>
          <div className="mechanism-flow">
            <p>Tu progreso</p>
            <strong>Palacio de Madarame</strong>
            <span aria-hidden="true">↓</span>
            <p>determina</p>
            <strong>Lo que podés leer</strong>
            <small>Publicaciones hasta ese checkpoint</small>
          </div>
        </aside>
      </section>

      <section className="landing-section explanation-section" id="como-funciona" aria-labelledby="spoiler-title">
        <header className="section-intro">
          <p className="eyebrow">Cómo evita spoilers</p>
          <h2 id="spoiler-title">El límite lo pone tu partida.</h2>
        </header>
        <div className="section-content">
          <p className="section-lead">
            No confiamos solo en una etiqueta. Umbral compara el punto de la historia de cada publicación con el avance que registraste antes de mostrarla.
          </p>
          <ol className="steps-list">
            <li><span>01</span>Elegí un juego.</li>
            <li><span>02</span>Marcá el último tramo que jugaste.</li>
            <li><span>03</span>Leé o participá solo desde ahí.</li>
          </ol>
        </div>
      </section>

      <section className="landing-section conversation-section" aria-labelledby="conversation-title">
        <header className="section-intro">
          <p className="eyebrow">Para hablar de juegos</p>
          <h2 id="conversation-title">No todo es una reseña.</h2>
        </header>
        <div className="conversation-types">
          {conversationTypes.map(([title, description]) => (
            <article key={title}>
              <h3>{title}</h3>
              <p>{description}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="catalog-section" id="catalogo" aria-labelledby="catalog-title">
        <header className="catalog-heading">
          <div>
            <p className="eyebrow">Catálogo inicial</p>
            <h2 id="catalog-title">Elegí dónde cruzar el umbral.</h2>
          </div>
          <p>Los juegos disponibles se cargan desde Umbral. Al abrir uno, podés indicar tu avance y entrar a su conversación segura.</p>
        </header>

        {loading && <p className="catalog-message">Cargando juegos...</p>}
        {error && <p className="catalog-message catalog-message-error">{error}</p>}
        {!loading && !error && games.length === 0 && <p className="catalog-message">Todavía no hay juegos disponibles.</p>}
        {!loading && !error && games.length > 0 && (
          <ol className="catalog-list">
            {games.map((game, index) => (
              <li key={game.id} className="catalog-row">
                <span className="catalog-index">{String(index + 1).padStart(2, '0')}</span>
                <div>
                  <h3>{game.title}</h3>
                  <p>{game.description}</p>
                </div>
                <Link className="row-action" to={`/games/${game.id}`}>Ver juego <span aria-hidden="true">→</span></Link>
              </li>
            ))}
          </ol>
        )}
      </section>

      <section className="landing-closing" aria-labelledby="closing-title">
        <div>
          <p className="eyebrow">Una comunidad en formación</p>
          <h2 id="closing-title">Una historia compartida también puede cuidarse.</h2>
        </div>
        <div>
          <a className="button button-accent" href="#catalogo">Explorar juegos</a>
          <p>Las cuentas y perfiles llegarán más adelante. Por ahora, podés conocer cómo funciona Umbral desde cada juego.</p>
        </div>
      </section>

      <footer className="site-footer">
        <p>Umbral · conversaciones sobre juegos narrativos, sin adelantarte nada.</p>
      </footer>
    </main>
  );
}
