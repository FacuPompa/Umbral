import { Link } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';
import GameArtwork from './GameArtwork';
import { getGameArtwork } from '@/features/games/gameArtwork';

export default function GameRow({ gameId, title, coverImageUrl, description, children }) {
  return (
    <article className="grid items-center gap-4 border-b border-border py-5 sm:grid-cols-[minmax(0,1fr)_auto] sm:gap-6">
      <Link to={`/games/${gameId}`} className="group flex min-w-0 items-center gap-4 rounded-md text-foreground no-underline">
        <GameArtwork src={coverImageUrl ?? getGameArtwork(title)} title={title} className="h-20 w-16 sm:h-24 sm:w-20" />
        <div className="grid min-w-0 flex-1 gap-2">
          <h3 className="break-words text-lg leading-6 font-semibold group-hover:text-primary">{title}</h3>
          {description && <p className="text-sm leading-5 text-muted-foreground">{description}</p>}
        </div>
        {!children && <ArrowRight aria-hidden="true" className="size-[18px] shrink-0 text-muted-foreground group-hover:text-primary" />}
      </Link>
      {children}
    </article>
  );
}
