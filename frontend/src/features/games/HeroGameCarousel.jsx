import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { Button } from '@/components/ui/button';
import useEmblaCarousel from 'embla-carousel-react';
import Autoplay from 'embla-carousel-autoplay';
import { WheelGesturesPlugin } from 'embla-carousel-wheel-gestures';
import { getGameArtwork, getGameInitials } from './gameArtwork';

export default function HeroGameCarousel({ games }) {
  const slides = games.map((game) => ({
    ...game,
    artwork: game.coverImageUrl ?? getGameArtwork(game.title),
  }));
  const [prefersReducedMotion, setPrefersReducedMotion] = useState(() => (
    window.matchMedia('(prefers-reduced-motion: reduce)').matches
  ));

  useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
    const updatePreference = (event) => setPrefersReducedMotion(event.matches);

    mediaQuery.addEventListener('change', updatePreference);
    return () => mediaQuery.removeEventListener('change', updatePreference);
  }, []);

  const plugins = useMemo(
    () => [
      WheelGesturesPlugin(),
      ...(!prefersReducedMotion ? [Autoplay({ delay: 5000, stopOnInteraction: false })] : []),
    ],
    [prefersReducedMotion],
  );
  const [emblaRef, emblaApi] = useEmblaCarousel(
    { align: 'start', containScroll: 'trimSnaps', loop: true },
    plugins,
  );

  return (
    <section className="relative size-full" aria-label="Juegos destacados">
      <div className="size-full min-w-0 overflow-hidden cursor-grab [&.is-wheel-dragging]:cursor-grabbing" ref={emblaRef}>
        <ol className="flex h-full [touch-action:pan-y_pinch-zoom]">
          {slides.map((game, index) => (
            <li
              className="min-w-0 flex-[0_0_100%]"
              key={`game-${game.id}`}
              aria-label={`${index + 1} de ${slides.length}`}
              aria-roledescription="slide"
            >
              <article className="h-full">
                <Link className="block size-full overflow-hidden text-[var(--paper)] no-underline bg-[var(--ink)]" to={`/games/${game.id}`} aria-label={`Abrir ${game.title}`}>
                  {game.artwork ? (
                    <img className="size-full object-cover" src={game.artwork} alt={`Arte de ${game.title}`} />
                  ) : (
                    <span className="grid size-full place-items-center text-5xl font-semibold">{getGameInitials(game.title)}</span>
                  )}
                </Link>
              </article>
            </li>
          ))}
        </ol>
      </div>

      <div className="absolute right-3 bottom-3 flex gap-2" aria-label="Controles del carrusel">
        <Button variant="outline" size="icon" className="border-white/40 bg-[var(--ink)] text-[var(--paper)] hover:bg-[#2b2d28] focus-visible:outline-white" type="button" onClick={() => emblaApi?.scrollPrev()} disabled={!emblaApi || slides.length < 2} aria-label="Juego anterior"><ChevronLeft aria-hidden="true" /></Button>
        <Button variant="outline" size="icon" className="border-white/40 bg-[var(--ink)] text-[var(--paper)] hover:bg-[#2b2d28] focus-visible:outline-white" type="button" onClick={() => emblaApi?.scrollNext()} disabled={!emblaApi || slides.length < 2} aria-label="Juego siguiente"><ChevronRight aria-hidden="true" /></Button>
      </div>
    </section>
  );
}
