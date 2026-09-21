import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';
import { m, useReducedMotion } from 'motion/react';
import PageHeading from '@/components/PageHeading';
import SearchForm from '@/components/SearchForm';
import GameRow from '@/components/GameRow';
import InitialAvatar from '@/components/InitialAvatar';
import StatusMessage from '@/components/StatusMessage';
import { searchCatalogAndUsers } from '../games/gameApi';
import SearchDiscovery from './SearchDiscovery';

export default function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const urlQuery = searchParams.get('query') ?? '';
  // El historial reconstruye campo y resultados desde la URL, sin datos previos.
  return <SearchContent key={urlQuery} urlQuery={urlQuery} onSearch={(query) => setSearchParams({ query })} />;
}

function SearchContent({ urlQuery, onSearch }) {
  const normalized = urlQuery.trim();
  const searched = normalized.length >= 2;
  const [query, setQuery] = useState(urlQuery);
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(searched);
  const [error, setError] = useState(null);
  const [attempt, setAttempt] = useState(0);
  const reduced = useReducedMotion();

  useEffect(() => {
    if (!searched) return;
    let active = true;
    async function loadResults() {
      setLoading(true);
      setError(null);
      try {
        const response = await searchCatalogAndUsers(normalized);
        if (active) setResults(response);
      } catch (requestError) {
        if (active) setError(requestError.message);
      } finally {
        if (active) setLoading(false);
      }
    }
    loadResults();
    return () => { active = false; };
  }, [normalized, searched, attempt]);

  function submitSearch(event) {
    event.preventDefault();
    const value = query.trim();
    if (value.length < 2 || loading) return;
    if (value === normalized) setAttempt((current) => current + 1);
    else onSearch(value);
  }

  const hasResults = results && (results.games.length > 0 || results.users.length > 0);
  return (
    <main className="grid w-full max-w-[840px] gap-8 py-8 md:gap-10 md:py-12" id="main-content">
      <PageHeading title="Buscar" description="Encontrá juegos del catálogo y perfiles de la comunidad." />
      <SearchForm query={query} onQueryChange={setQuery} onSubmit={submitSearch} loading={loading}
        label="Título de juego o nombre de usuario" hint="Escribí al menos dos caracteres. Mostramos hasta cinco resultados de cada grupo." />
      {error && <StatusMessage kind="error">{error}</StatusMessage>}
      {!searched && <SearchDiscovery />}
      {!loading && !error && searched && !hasResults && <StatusMessage>No encontramos juegos ni personas con “{normalized}”.</StatusMessage>}
      {!loading && !error && hasResults && (
        <m.div className="grid gap-10" initial={{ opacity: reduced ? 1 : 0 }} animate={{ opacity: 1 }} transition={{ duration: reduced ? 0 : 0.12 }}>
          {results.games.length > 0 && (
            <section className="grid gap-5" aria-labelledby="search-games-title">
              <h2 className="text-2xl leading-[30px] font-semibold tracking-normal" id="search-games-title">Juegos</h2>
              <ol className="border-t border-border">
                {results.games.map((game) => <li key={game.id}><GameRow gameId={game.id} title={game.title} description={game.description} coverImageUrl={game.coverImageUrl} /></li>)}
              </ol>
            </section>
          )}
          {results.users.length > 0 && (
            <section className="grid gap-5" aria-labelledby="search-users-title">
              <h2 className="text-2xl leading-[30px] font-semibold tracking-normal" id="search-users-title">Personas</h2>
              <ol className="border-t border-border">
                {results.users.map((user) => (
                  <li key={user.handle} className="border-b border-border">
                    <Link className="flex min-h-20 items-center gap-4 rounded-md py-4 text-foreground no-underline hover:text-primary" to={`/users/${encodeURIComponent(user.handle)}`}>
                      <InitialAvatar handle={user.handle} className="size-10" />
                      <span className="min-w-0 flex-1 break-all text-base font-medium">{user.handle}</span>
                      <ArrowRight aria-hidden="true" className="size-[18px] shrink-0" />
                    </Link>
                  </li>
                ))}
              </ol>
            </section>
          )}
        </m.div>
      )}
    </main>
  );
}
