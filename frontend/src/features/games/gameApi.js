const GAMES_URL = '/api/games';


export async function fetchGames() {
  const response = await fetch(GAMES_URL);

  if (!response.ok) {
    throw new Error('No se pudo cargar el catálogo.');
  }

  return response.json();
}

export async function fetchCheckpoints(gameId) {
  const response = await fetch(`/api/games/${gameId}/checkpoints`);

  if (!response.ok) {
    throw new Error('No se pudieron cargar los checkpoints.');
  }

  return response.json();
}

  export async function fetchGameProgress() {
  const response = await fetch('/api/me/game-progress');

  if (!response.ok) {
    throw new Error('No se pudo cargar el progreso.');
  }

  return response.json();
}

export async function updateGameProgress(gameId, checkpointId) {
  const response = await fetch(`/api/me/games/${gameId}/progress`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ checkpointId }),
  });

  if (!response.ok) {
    throw new Error('No se pudo guardar el progreso.');
  }

  return response.json();
}
