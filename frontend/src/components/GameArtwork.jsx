import { useState } from 'react';
import { cn } from '@/lib/utils';
import { getGameInitials } from '@/features/games/gameArtwork';

export default function GameArtwork({ src, title, className }) {
  const [loadedSource, setLoadedSource] = useState(null);
  const [failedSource, setFailedSource] = useState(null);
  const showImage = src && failedSource !== src;
  return (
    <span className={cn('relative flex shrink-0 items-center justify-center overflow-hidden rounded-md border border-border bg-muted text-foreground', className)}>
      {showImage ? (
        <>
          {loadedSource !== src && <span className="absolute inset-0 bg-muted" aria-hidden="true" />}
          <img src={src} alt="" loading="lazy" decoding="async"
            className="relative size-full object-cover" onLoad={() => setLoadedSource(src)} onError={() => setFailedSource(src)} />
        </>
      ) : <span aria-hidden="true" className="text-lg font-semibold">{getGameInitials(title)}</span>}
    </span>
  );
}
