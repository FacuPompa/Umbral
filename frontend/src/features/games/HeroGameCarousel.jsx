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
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [focusWithin, setFocusWithin] = useState(false);

  useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
    const updatePreference = (event) => setPrefersReducedMotion(event.matches);

    mediaQuery.addEventListener('change', updatePreference);
    return () => mediaQuery.removeEventListener('change', updatePreference);
  }, []);

  const plugins = useMemo(
    () => [
      WheelGesturesPlugin({ forceWheelAxis: 'x' }),
      Autoplay({
        active: !prefersReducedMotion && !focusWithin,
        delay: 5000,
        stopOnInteraction: false,
        stopOnMouseEnter: true,
      }),
    ],
    [prefersReducedMotion, focusWithin],
  );
  const [emblaRef, emblaApi] = useEmblaCarousel(
    { align: 'start', containScroll: 'trimSnaps', loop: true, duration: prefersReducedMotion ? 0 : 25 },
    plugins,
  );

  useEffect(() => {
    if (!emblaApi) return;
    const updateSelection = () => setSelectedIndex(emblaApi.selectedScrollSnap());
    updateSelection();
    emblaApi.on('select', updateSelection).on('reInit', updateSelection);
    return () => {
      emblaApi.off('select', updateSelection).off('reInit', updateSelection);
    };
  }, [emblaApi]);

  return (
    <section className="relative size-full" aria-label="Juegos destacados" aria-roledescription="carrusel"
      onFocusCapture={() => setFocusWithin(true)}
      onBlurCapture={(event) => {
        if (!event.currentTarget.contains(event.relatedTarget)) setFocusWithin(false);
      }}>
      <div className="size-full min-w-0 overflow-hidden cursor-grab [&.is-wheel-dragging]:cursor-grabbing" ref={emblaRef}>
        <ol className="flex h-full [touch-action:pan-y_pinch-zoom]">
          {slides.map((game, index) => (
            <li
              className="min-w-0 flex-[0_0_100%]"
              key={`game-${game.id}`}
              aria-label={`${index + 1} de ${slides.length}`}
              aria-roledescription="slide"
              aria-hidden={index !== selectedIndex}
            >
              <article className="h-full">
                <Link tabIndex={index === selectedIndex ? 0 : -1} className="block size-full overflow-hidden text-[var(--paper)] no-underline bg-[var(--ink)] focus-visible:outline-white focus-visible:outline-offset-[-4px]" to={`/games/${game.id}`} aria-label={`Abrir ${game.title}`}>
                  {game.artwork ? (
                    <img className="size-full object-cover" src={game.artwork} alt="" draggable={false} />
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
