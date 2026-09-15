import GameRow from '@/components/GameRow';
import LoadingIndicator from '@/components/LoadingIndicator';
import StatusMessage from '@/components/StatusMessage';
import { Button } from '@/components/ui/button';

export default function CatalogSection({ games, loading, error, onRetry }) {
  return (
    <section className="grid scroll-mt-6 gap-6 border-t border-border py-12 md:py-16" id="catalogo" aria-labelledby="catalog-title">
      <header className="flex flex-wrap items-end justify-between gap-4">
        <div className="grid gap-3">
          <h2 className="text-2xl leading-[30px] font-semibold tracking-normal" id="catalog-title">Juegos en Umbral</h2>
          <p className="text-base leading-6 text-muted-foreground">Elegí un juego para guardar tu progreso y sumarte a su conversación.</p>
        </div>
        {!loading && !error && <span className="text-sm text-muted-foreground">{games.length} {games.length === 1 ? 'juego disponible' : 'juegos disponibles'}</span>}
      </header>
      {loading && <LoadingIndicator label="Cargando juegos" showLabel />}
      {error && <StatusMessage kind="error">No pudimos conectar con el catálogo todavía. <Button variant="ghost" type="button" onClick={onRetry}>Reintentar catálogo</Button></StatusMessage>}
      {!loading && !error && games.length === 0 && <StatusMessage>Todavía no hay juegos disponibles.</StatusMessage>}
      {!loading && !error && games.length > 0 && <ol className="border-t border-border">
        {games.map((game) => <li key={game.id}><GameRow gameId={game.id} title={game.title} description={game.description} coverImageUrl={game.coverImageUrl} /></li>)}
      </ol>}
    </section>
  );
}
