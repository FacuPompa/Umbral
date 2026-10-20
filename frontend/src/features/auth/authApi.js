import { restoreAuthentication, authenticate, endAuthentication, sendJson } from '../../lib/apiClient';

export function fetchCurrentUser() {
  return restoreAuthentication();
}

export function registerUser({ handle, email, password }) {
  return sendJson('/api/auth/register', 'POST', { handle, email, password }, 'No se pudo crear tu cuenta.');
}

export function loginUser({ handle, password }) {
  return authenticate({ handle, password });
}

export function logoutUser() {
  return endAuthentication();
}

export function changeUserPassword(currentPassword, newPassword) {
  return sendJson('/api/me/password', 'POST', { currentPassword, newPassword },
    'No se pudo cambiar la contraseña. Revisá los datos y probá nuevamente.');
}
