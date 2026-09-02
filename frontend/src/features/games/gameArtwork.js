import persona5RoyalArtwork from '../../assets/games/persona-5-royal.webp';

const artworkByTitle = {
  'Persona 5 Royal': persona5RoyalArtwork,
};

export function getGameArtwork(title) {
  return artworkByTitle[title] ?? null;
}

export function getGameInitials(title) {
  return title
    .split(' ')
    .filter(Boolean)
    .slice(0, 3)
    .map((word) => word[0])
    .join('')
    .toUpperCase();
}
