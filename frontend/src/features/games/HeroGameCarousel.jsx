import { useMemo } from 'react';
import { Link } from 'react-router-dom';
import useEmblaCarousel from 'embla-carousel-react';
import Autoplay from 'embla-carousel-autoplay';
import { WheelGesturesPlugin } from 'embla-carousel-wheel-gestures';
import { catalogPreviewSlides } from './catalogPreviewSlides';
import { getGameArtwork, getGameInitials } from './gameArtwork';

export default function HeroGameCarousel({ games }) {
  const slides = [
    ...games.map((game) => ({ ...game, artwork: getGameArtwork(game.title), isPreview: false })),
    ...catalogPreviewSlides.map((slide) => ({ ...slide, isPreview: true })),
  ];
  const plugins = useMemo(
    () => [
      WheelGesturesPlugin(),
      Autoplay({ delay: 5000, stopOnInteraction: false }),
    ],
    [],
  );
  const [emblaRef, emblaApi] = useEmblaCarousel(
    { align: 'start', containScroll: 'trimSnaps', loop: true },
    plugins,
  );

  return (
    <section className="hero-carousel" aria-label="Juegos destacados">
      <div className="hero-carousel-viewport" ref={emblaRef}>
        <ol className="hero-carousel-track">
          {slides.map((game, index) => (
            <li
              className="hero-carousel-slide"
              key={`${game.isPreview ? 'preview' : 'game'}-${game.id}`}
              aria-label={`${index + 1} de ${slides.length}`}
              aria-roledescription="slide"
            >
              <article className="hero-carousel-card">
                {game.isPreview ? (
                  <div className="hero-carousel-image">
                    <img src={game.artwork} alt="Portada ficticia de un próximo juego" />
                  </div>
                ) : (
                  <Link className="hero-carousel-image" to={`/games/${game.id}`} aria-label={`Abrir ${game.title}`}>
                    {game.artwork ? (
                      <img src={game.artwork} alt={`Arte de ${game.title}`} />
                    ) : (
                      <span>{getGameInitials(game.title)}</span>
                    )}
                  </Link>
                )}
              </article>
            </li>
          ))}
        </ol>
      </div>

      <div className="hero-carousel-controls" aria-label="Controles del carrusel">
        <button type="button" onClick={() => emblaApi?.scrollPrev()} disabled={!emblaApi} aria-label="Juego anterior">←</button>
        <button type="button" onClick={() => emblaApi?.scrollNext()} disabled={!emblaApi} aria-label="Juego siguiente">→</button>
      </div>
    </section>
  );
}
