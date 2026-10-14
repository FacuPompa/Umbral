import { getJson, sendJson } from '../../lib/apiClient';

export function fetchModeratedUsers({ query = '', status = '', page = 0 } = {}) {
  const params = new URLSearchParams({ query, page, size: 10 });
  if (status) params.set('suspended', status === 'suspended' ? 'true' : 'false');
  return getJson(`/api/moderation/users?${params}`, 'No se pudieron cargar los usuarios.');
}

export function suspendUser(userId, reason) {
  return sendJson(`/api/moderation/users/${userId}/suspend`, 'POST', { reason },
    'No se pudo suspender la cuenta.');
}

export function reactivateUser(userId) {
  return sendJson(`/api/moderation/users/${userId}/reactivate`, 'POST', {},
    'No se pudo reactivar la cuenta.');
}
