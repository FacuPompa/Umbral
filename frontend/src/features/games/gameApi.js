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

export async function fetchJournalEntries(gameId) {
  const response = await fetch(`/api/games/${gameId}/journal-entries`);

  if (!response.ok) {
    throw new Error('No se pudo cargar la bitácora.');
  }

  return response.json();
}

export async function createJournalEntry(checkpointId, type, content) {
  const response = await fetch('/api/me/journal-entries', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ checkpointId, type, content }),
  });

  if (!response.ok) {
    throw new Error('No se pudo publicar la entrada.');
  }

  return response.json();
}

export async function fetchJournalReplies(entryId) {
  const response = await fetch(`/api/journal-entries/${entryId}/replies`);

  if (!response.ok) {
    throw new Error('No se pudieron cargar las respuestas.');
  }

  return response.json();
}

export async function createJournalReply(entryId, content) {
  const response = await fetch(`/api/me/journal-entries/${entryId}/replies`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ content }),
  });

  if (!response.ok) {
    throw new Error('No se pudo publicar la respuesta.');
  }

  return response.json();
}
