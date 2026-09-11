export const libraryStatusLabels = {
  WANT_TO_PLAY: 'Quiero jugar',
  PLAYING: 'Jugando',
  COMPLETED: 'Terminado',
};

export const libraryStatusOptions = Object.entries(libraryStatusLabels).map(([value, label]) => ({ value, label }));
