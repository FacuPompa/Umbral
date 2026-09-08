export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

let csrfToken;
const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '');

function apiUrl(path) {
  return `${apiBaseUrl}${path}`;
}

function defaultMessageForStatus(status) {
  if (status === 400) return 'Revisá los datos que ingresaste.';
  if (status === 401) return 'Necesitás iniciar sesión para continuar.';
  if (status === 403) return 'No se pudo validar la sesión. Probá de nuevo.';
  if (status === 409) return 'Ese email o nombre de usuario ya está en uso.';
  return 'No se pudo completar la solicitud.';
}

async function errorFrom(response, fallbackMessage) {
  let message = fallbackMessage ?? defaultMessageForStatus(response.status);

  try {
    const body = await response.json();
    if (typeof body.message === 'string' && body.message.trim()) {
      message = body.message;
    }
  } catch {
    // Las respuestas de error actuales no siempre incluyen un cuerpo JSON.
  }

  return new ApiError(message, response.status);
}

export async function ensureCsrfToken() {
  if (csrfToken) return csrfToken;

  const response = await fetch(apiUrl('/api/auth/csrf'), {
    credentials: 'include',
  });

  if (!response.ok) {
    throw await errorFrom(response, 'No se pudo preparar una sesión segura.');
  }

  csrfToken = await response.json();
  return csrfToken;
}

export async function apiRequest(path, options = {}) {
  const method = (options.method ?? 'GET').toUpperCase();
  const headers = new Headers(options.headers);

  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    const csrf = await ensureCsrfToken();
    headers.set(csrf.headerName, csrf.token);
  }

  return fetch(apiUrl(path), {
    ...options,
    method,
    headers,
    credentials: 'include',
  });
}

export async function getJson(path, fallbackMessage) {
  const response = await apiRequest(path);

  if (!response.ok) {
    throw await errorFrom(response, fallbackMessage);
  }

  return response.json();
}

export async function sendJson(path, method, body, fallbackMessage) {
  const response = await apiRequest(path, {
    method,
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  });

  if (!response.ok) {
    throw await errorFrom(response, fallbackMessage);
  }

  if (response.status === 204) return null;
  return response.json();
}
