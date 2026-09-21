import { Link } from 'react-router-dom';
import { ArrowUpRight } from 'lucide-react';
import GameArtwork from '@/components/GameArtwork';
import { getGameArtwork } from './gameArtwork';

export default function GameShelf({ games }) {
  return (
    <div className="min-w-0">
      {games.length > 1 && <p className="mb-3 text-sm text-muted-foreground md:hidden">Deslizá para explorar los juegos.</p>}
      <ol aria-label="Juegos del catálogo" className={`grid grid-flow-col ${games.length === 1 ? 'auto-cols-[100%]' : 'auto-cols-[85%]'} snap-x snap-mandatory gap-5 overflow-x-auto px-1 pt-1 pb-4 md:grid-flow-row md:auto-cols-auto md:grid-cols-3 md:gap-6 md:overflow-visible`}>
        {games.map((game) => (
          <li key={game.id} className="min-w-0 snap-start">
            <Link to={`/games/${game.id}`} className="group grid gap-4 rounded-md text-foreground no-underline">
              <GameArtwork src={game.coverImageUrl ?? getGameArtwork(game.title)} title={game.title} className="aspect-[4/3] w-full" />
              <div className="flex items-start justify-between gap-3">
                <h3 className="min-w-0 break-words text-xl leading-7 font-semibold group-hover:text-primary">{game.title}</h3>
                <ArrowUpRight aria-hidden="true" className="mt-1 size-[18px] shrink-0 text-muted-foreground group-hover:text-primary" />
              </div>
              {game.description && <p className="line-clamp-2 text-sm leading-5 text-muted-foreground">{game.description}</p>}
            </Link>
          </li>
        ))}
      </ol>
    </div>
  );
}
