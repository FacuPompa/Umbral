import { Link } from 'react-router-dom';
import { Star } from 'lucide-react';
import GameArtwork from '@/components/GameArtwork';
import { Button } from '@/components/ui/button';
import { getGameArtwork } from '../games/gameArtwork';

export default function ProfileFavorites({ games, personal = false }) {
  return (
    <section className="grid gap-6" aria-labelledby="profile-favorites-title">
      <div className="grid gap-2">
        <h2 id="profile-favorites-title" className="text-2xl font-semibold leading-[30px]">Juegos favoritos</h2>
        <p className="text-base leading-6 text-muted-foreground">
          {personal ? 'Los juegos que elegiste destacar también aparecen en tu perfil público.' : 'Una selección de los juegos que esta persona eligió destacar.'}
        </p>
      </div>
      {games.length > 0 ? (
        <ol aria-label="Juegos favoritos" className="grid grid-cols-2 gap-x-4 gap-y-6 sm:grid-cols-3 sm:gap-6">
          {games.map((game) => (
            <li key={game.gameId} className="min-w-0">
              <Link to={`/games/${game.gameId}`} className="group grid gap-3 rounded-md text-foreground">
                <GameArtwork src={game.coverImageUrl ?? getGameArtwork(game.gameTitle)} title={game.gameTitle} className="aspect-[3/4] w-full" />
                <h3 className="break-words text-base font-semibold leading-6 group-hover:text-primary sm:text-lg">{game.gameTitle}</h3>
              </Link>
            </li>
          ))}
        </ol>
      ) : (
        <div className="flex items-start gap-4 bg-surface-subtle p-5 sm:p-6">
          <Star aria-hidden="true" className="mt-1 size-5 shrink-0 text-muted-foreground" />
          <div className="grid justify-items-start gap-3">
            <p className="font-medium">{personal ? 'Todavía no elegiste favoritos' : 'Todavía no hay favoritos para mostrar'}</p>
            <p className="text-sm leading-6 text-muted-foreground">
              {personal ? 'Marcá la estrella de un juego en tu biblioteca para destacarlo acá.' : 'Cuando esta persona marque juegos como favoritos, sus portadas aparecerán acá.'}
            </p>
            {personal && <Button asChild variant="outline"><Link to="/me/library">Elegir favoritos</Link></Button>}
          </div>
        </div>
      )}
    </section>
  );
}
