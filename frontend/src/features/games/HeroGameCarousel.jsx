import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowUpRight, ChevronLeft, ChevronRight } from 'lucide-react';
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

  const currentGame = slides[selectedIndex] ?? slides[0];

  if (!currentGame) return null;

  return (
    <section className="relative size-full" aria-label="Juegos destacados" aria-roledescription="carrusel"
      onFocusCapture={() => setFocusWithin(true)}
      onBlurCapture={(event) => {
        if (!event.currentTarget.contains(event.relatedTarget)) setFocusWithin(false);
      }}>
      <div className="h-[260px] min-w-0 overflow-hidden cursor-grab sm:h-[340px] lg:h-[360px] [&.is-wheel-dragging]:cursor-grabbing" ref={emblaRef}>
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
                <Link draggable={false} tabIndex={index === selectedIndex ? 0 : -1} className="block size-full select-none overflow-hidden text-[var(--paper)] no-underline bg-[var(--ink)] focus-visible:outline-white focus-visible:outline-offset-[-4px]" to={`/games/${game.id}`} aria-label={`Abrir ${game.title}`}>
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

      <div className="grid gap-3 border-t border-border bg-popover p-4 sm:p-5">
        <div className="flex flex-wrap items-start justify-between gap-x-4 gap-y-1">
          <h2 className="min-w-0 break-words text-xl leading-7 font-semibold">{currentGame.title}</h2>
          <div className="flex flex-wrap items-center" role="group" aria-label="Elegir juego destacado">
            {slides.map((game, index) => (
              <button
                key={game.id}
                type="button"
                aria-label={`Ver juego ${index + 1}: ${game.title}`}
                aria-current={index === selectedIndex ? 'true' : undefined}
                disabled={!emblaApi}
                onClick={() => emblaApi?.scrollTo(index)}
                className="flex size-11 items-center justify-center rounded-sm"
              >
                <span aria-hidden="true" className={`h-1 w-7 rounded-sm ${index === selectedIndex ? 'bg-foreground/70' : 'bg-foreground/20'}`} />
              </button>
            ))}
          </div>
        </div>
        <div className="flex flex-wrap items-center justify-between gap-3">
          <Link className="inline-flex min-h-11 items-center gap-2 text-sm font-medium text-primary underline underline-offset-4" to={`/games/${currentGame.id}`} aria-label={`Abrir ficha de ${currentGame.title}`}>
            Abrir ficha <ArrowUpRight className="size-[18px]" aria-hidden="true" />
          </Link>
          <div className="flex gap-2" aria-label="Controles del carrusel">
            <Button variant="outline" size="icon" type="button" onClick={() => emblaApi?.scrollPrev()} disabled={!emblaApi || slides.length < 2} aria-label="Juego anterior"><ChevronLeft aria-hidden="true" /></Button>
            <Button variant="outline" size="icon" type="button" onClick={() => emblaApi?.scrollNext()} disabled={!emblaApi || slides.length < 2} aria-label="Juego siguiente"><ChevronRight aria-hidden="true" /></Button>
          </div>
        </div>
      </div>
    </section>
  );
}
