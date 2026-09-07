import { getJson, sendJson } from '../../lib/apiClient';

export function fetchGames() {
  return getJson('/api/games', 'No se pudo cargar el catálogo.');
}

export function fetchCheckpoints(gameId) {
  return getJson(`/api/games/${gameId}/checkpoints`, 'No se pudieron cargar los checkpoints.');
}

export function fetchGameProgress() {
  return getJson('/api/me/game-progress', 'No se pudo cargar el progreso.');
}

export function updateGameProgress(gameId, checkpointId) {
  return sendJson(`/api/me/games/${gameId}/progress`, 'PUT', { checkpointId }, 'No se pudo guardar el progreso.');
}

export function fetchJournalEntries(gameId) {
  return getJson(`/api/games/${gameId}/journal-entries`, 'No se pudo cargar la bitácora.');
}

export function createJournalEntry(checkpointId, type, content) {
  return sendJson('/api/me/journal-entries', 'POST', { checkpointId, type, content }, 'No se pudo publicar la entrada.');
}

export function fetchJournalReplies(entryId) {
  return getJson(`/api/journal-entries/${entryId}/replies`, 'No se pudieron cargar las respuestas.');
}

export function createJournalReply(entryId, content) {
  return sendJson(`/api/me/journal-entries/${entryId}/replies`, 'POST', { content }, 'No se pudo publicar la respuesta.');
}
