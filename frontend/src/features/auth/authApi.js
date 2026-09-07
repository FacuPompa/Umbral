import { getJson, sendJson } from '../../lib/apiClient';

export function fetchCurrentUser() {
  return getJson('/api/auth/me', 'No se pudo recuperar tu sesión.');
}

export function registerUser({ handle, email, password }) {
  return sendJson('/api/auth/register', 'POST', { handle, email, password }, 'No se pudo crear tu cuenta.');
}

export function loginUser({ handle, password }) {
  return sendJson('/api/auth/login', 'POST', { handle, password }, 'El usuario o la contraseña no son correctos.');
}

export function logoutUser() {
  return sendJson('/api/auth/logout', 'POST', {}, 'No se pudo cerrar la sesión.');
}
