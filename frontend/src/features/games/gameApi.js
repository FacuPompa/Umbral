import { getJson, sendJson } from '../../lib/apiClient';

export function fetchGames() {
  return getJson('/api/games', 'No se pudo cargar el catálogo.');
}

export function fetchCheckpoints(gameId) {
  return getJson(`/api/games/${gameId}/checkpoints`, 'No se pudieron cargar los checkpoints.');
}

export function submitCheckpointSuggestion(gameId, label, position) {
  return sendJson(
    `/api/games/${gameId}/checkpoint-suggestions`,
    'POST',
    { label, position },
    'No se pudo enviar la propuesta de checkpoint.',
  );
}

export function fetchGameProgress() {
  return getJson('/api/me/game-progress', 'No se pudo cargar el progreso.');
}

export function fetchCurrentUserLibrary() {
  return getJson('/api/me/library', 'No se pudo cargar tu biblioteca.');
}

export function fetchPublicProfile(handle) {
  return getJson(`/api/users/${encodeURIComponent(handle)}`, 'No encontramos ese perfil.');
}

export function searchCatalogAndUsers(query) {
  return getJson(`/api/search?query=${encodeURIComponent(query)}`, 'No se pudo completar la búsqueda.');
}

export function addGameToCurrentUserLibrary(gameId, status = 'WANT_TO_PLAY') {
  return sendJson('/api/me/library', 'POST', { gameId, status }, 'No se pudo agregar el juego a tu biblioteca.');
}

export function updateCurrentUserLibraryGameStatus(gameId, status) {
  return sendJson(`/api/me/library/${gameId}/status`, 'PATCH', { status }, 'No se pudo actualizar el estado del juego.');
}

export function updateCurrentUserLibraryGameFavorite(gameId, favorite) {
  return sendJson(`/api/me/library/${gameId}/favorite`, 'PATCH', { favorite }, 'No se pudo actualizar el favorito.');
}

export function removeGameFromCurrentUserLibrary(gameId) {
  return sendJson(`/api/me/library/${gameId}`, 'DELETE', {}, 'No se pudo quitar el juego de tu biblioteca.');
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

export function searchExternalGames(query) {
  return getJson(`/api/game-suggestions/search?query=${encodeURIComponent(query)}`, 'No se pudo buscar juegos ahora.');
}

export function submitGameSuggestion(rawgGameId) {
  return sendJson('/api/game-suggestions', 'POST', { rawgGameId }, 'No se pudo enviar la sugerencia.');
}

export function fetchPendingGameSuggestions() {
  return getJson('/api/moderation/game-suggestions', 'No se pudieron cargar las sugerencias pendientes.');
}

export function approveGameSuggestion(suggestionId, safeDescription) {
  return sendJson(
    `/api/moderation/game-suggestions/${suggestionId}/approve`,
    'POST',
    { safeDescription },
    'No se pudo aprobar la sugerencia.',
  );
}

export function rejectGameSuggestion(suggestionId) {
  return sendJson(
    `/api/moderation/game-suggestions/${suggestionId}/reject`,
    'POST',
    {},
    'No se pudo rechazar la sugerencia.',
  );
}

export function fetchPendingCheckpointSuggestions() {
  return getJson('/api/moderation/checkpoint-suggestions', 'No se pudieron cargar los checkpoints pendientes.');
}

export function approveCheckpointSuggestion(suggestionId) {
  return sendJson(
    `/api/moderation/checkpoint-suggestions/${suggestionId}/approve`,
    'POST',
    {},
    'No se pudo aprobar el checkpoint.',
  );
}

export function rejectCheckpointSuggestion(suggestionId) {
  return sendJson(
    `/api/moderation/checkpoint-suggestions/${suggestionId}/reject`,
    'POST',
    {},
    'No se pudo rechazar el checkpoint.',
  );
}
