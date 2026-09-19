import { useEffect, useState } from 'react';
import BackendWakeupNotice from '../../components/BackendWakeupNotice';
import LoadingIndicator from '../../components/LoadingIndicator';
import HeroGameCarousel from '../games/HeroGameCarousel';
import { fetchGames } from '../games/gameApi';
import CatalogSection from '../games/CatalogSection';
import { Button } from '@/components/ui/button';
import SpoilerBoundaryDemo from './SpoilerBoundaryDemo';
import ConversationFormats from './ConversationFormats';

export default function HomePage() {
  const [games, setGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isSlowToLoad, setIsSlowToLoad] = useState(false);
  const [requestVersion, setRequestVersion] = useState(0);

  useEffect(() => {
    let isActive = true;
    let retryTimer;
    const wakeupTimer = window.setTimeout(() => {
      if (isActive) setIsSlowToLoad(true);
    }, 1500);

    async function loadCatalog() {
      try {
        setLoading(true);
        setError(null);
        setIsSlowToLoad(false);
        const catalog = await fetchGames();
        if (isActive) setGames(catalog);
      } catch (requestError) {
        if (isActive) {
          setError(requestError);
          retryTimer = window.setTimeout(() => setRequestVersion((version) => version + 1), 5000);
        }
      } finally {
        window.clearTimeout(wakeupTimer);
        if (isActive) setLoading(false);
      }
    }

    loadCatalog();
    return () => {
      isActive = false;
      window.clearTimeout(wakeupTimer);
      window.clearTimeout(retryTimer);
    };
  }, [requestVersion]);

  function retryCatalog() {
    setRequestVersion((version) => version + 1);
  }

  return (
    <main id="main-content">
      <section className="page-container grid items-center gap-8 py-10 lg:grid-cols-[minmax(0,1.1fr)_minmax(0,1fr)] lg:gap-12 lg:py-16" aria-labelledby="hero-title">
        <div className="grid justify-items-start gap-6">
          <h1 id="hero-title" className="max-w-[620px] text-4xl leading-10 font-[650] tracking-[-0.025em] md:text-[52px] md:leading-[56px]">Preguntá, compartí y seguí jugando sin adelantarte la historia.</h1>
          <p className="max-w-[560px] text-lg leading-7 text-muted-foreground">Marcá hasta dónde llegaste. Umbral te muestra las conversaciones de ese tramo y de los anteriores, para que puedas pedir ayuda sin conocer lo que viene después.</p>
          <div className="flex flex-wrap items-center gap-3">
            <Button asChild><a href="#catalogo">Explorar juegos</a></Button>
            <Button asChild variant="ghost"><a href="#como-funciona">Cómo funciona</a></Button>
          </div>
        </div>
        <div className="min-w-0 overflow-hidden rounded-lg bg-muted">
          {loading && !isSlowToLoad && <div className="grid min-h-[400px] place-items-center"><LoadingIndicator label="Cargando catálogo" showLabel /></div>}
          {(isSlowToLoad || error) && <div className="grid min-h-[400px] place-items-center p-6"><BackendWakeupNotice onRetry={retryCatalog} retrying={loading} /></div>}
          {!loading && !error && (games.length > 0 ? <HeroGameCarousel games={games} /> : <p className="grid min-h-[400px] place-items-center p-6 text-base text-muted-foreground">El catálogo todavía no tiene juegos.</p>)}
        </div>
      </section>

      <SpoilerBoundaryDemo games={games} loading={loading} error={error} />

      <ConversationFormats />

      <div className="page-container">
        <CatalogSection games={games} loading={loading} error={error} onRetry={retryCatalog} />
      </div>
      <footer className="bg-surface-subtle py-8 text-sm">
        <div className="page-container flex flex-wrap items-center justify-between gap-4">
        <strong className="font-semibold">Umbral</strong>
        <p className="text-sm leading-5 text-muted-foreground">Conversaciones sin adelantarte la historia. Datos e imágenes de <a className="underline underline-offset-4" href="https://rawg.io/" rel="noreferrer" target="_blank">RAWG</a>.</p>
        </div>
      </footer>
    </main>
  );
}
