import { useState } from 'react';
import { Link } from 'react-router-dom';
import { MoreHorizontal, Star, Trash2 } from 'lucide-react';
import GameArtwork from '@/components/GameArtwork';
import LoadingIndicator from '@/components/LoadingIndicator';
import { Button } from '@/components/ui/button';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';
import { getGameArtwork } from '../games/gameArtwork';
import { libraryStatusOptions } from './libraryLabels';

export default function LibraryGameRow({ game, disabled, busy, onStatusChange, onFavoriteChange, onRemove }) {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <article aria-label={game.gameTitle} className="grid min-w-0 grid-cols-[80px_minmax(0,1fr)] gap-4 border-b border-border py-6 sm:grid-cols-[112px_minmax(0,1fr)] sm:gap-6">
      <Link to={`/games/${game.gameId}`} aria-label={`Ver ${game.gameTitle}`} className="row-span-2 self-start rounded-md">
        <GameArtwork src={game.coverImageUrl ?? getGameArtwork(game.gameTitle)} title={game.gameTitle} className="h-28 w-20 sm:h-40 sm:w-28" />
      </Link>
      <div className="flex min-w-0 items-start justify-between gap-2">
        <h2 className="min-w-0 break-words text-xl font-semibold leading-7">
          <Link to={`/games/${game.gameId}`} className="rounded-sm hover:text-primary">{game.gameTitle}</Link>
        </h2>
        <DropdownMenu open={menuOpen} onOpenChange={setMenuOpen}>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" className="shrink-0 px-3" aria-label={`Opciones de ${game.gameTitle}`} disabled={disabled}>
              <MoreHorizontal aria-hidden="true" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent open={menuOpen}>
            <DropdownMenuItem disabled={disabled} onSelect={onRemove}>
              <Trash2 aria-hidden="true" />Quitar de mi biblioteca
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
      <div className="min-w-0 self-start border-l-2 border-primary pl-3">
        <p className="text-sm text-muted-foreground">
          {game.checkpointPosition != null ? `Checkpoint ${game.checkpointPosition}` : 'Sin avance guardado'}
        </p>
        <p className="mt-1 break-words text-sm leading-6">
          {game.checkpointLabel ?? 'Marcá tu progreso desde la ficha del juego.'}
        </p>
        {game.checkpointLabel && <p className="mt-1 text-sm text-muted-foreground">Tu lectura llega hasta acá.</p>}
      </div>
      <div className="col-span-2 flex flex-wrap items-end gap-3 sm:col-span-1 sm:col-start-2">
        <label className="grid min-w-0 flex-1 gap-2 text-sm font-medium sm:max-w-56">
          Estado
          <select className="min-h-11 w-full rounded-md border border-input bg-popover px-3 text-base font-normal text-foreground"
            disabled={disabled} onChange={(event) => onStatusChange(event.target.value)} value={game.status}>
            {libraryStatusOptions.map((status) => <option key={status.value} value={status.value}>{status.label}</option>)}
          </select>
        </label>
        <Button variant="ghost" aria-pressed={game.favorite} aria-label={`Favorito: ${game.gameTitle}`}
          disabled={disabled} onClick={onFavoriteChange} type="button">
          <Star aria-hidden="true" fill={game.favorite ? 'currentColor' : 'none'} className={game.favorite ? 'text-primary' : ''} />Favorito
        </Button>
        {busy && <LoadingIndicator label="Actualizando juego" showLabel />}
      </div>
    </article>
  );
}
