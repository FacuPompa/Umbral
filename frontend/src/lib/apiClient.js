export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

// Access tokens live only in this module's memory. The refresh credential is HttpOnly.
let accessToken;
let csrfToken;
let csrfRequest;
let refreshRequest;
let generation = 0;
const apiBaseUrl = (import.meta.env?.VITE_API_BASE_URL ?? '').replace(/\/$/, '');
const apiUrl = (path) => `${apiBaseUrl}${path}`;
const withAuthLock = (work) => navigator.locks ? navigator.locks.request('umbral-refresh', work) : work();

async function errorFrom(response, fallbackMessage) {
  let message = fallbackMessage ?? (response.status === 401 ? 'Necesitás iniciar sesión para continuar.' : 'No se pudo completar la solicitud.');
  try {
    const body = await response.json();
    if (typeof body.message === 'string' && body.message.trim()) message = body.message;
  } catch { /* Some security responses intentionally have no body. */ }
  return new ApiError(message, response.status);
}

export function clearClientSession() {
  generation++;
  accessToken = undefined;
  csrfToken = undefined;
  window.dispatchEvent(new Event('umbral:session-expired'));
}

export async function ensureCsrfToken() {
  if (csrfToken) return csrfToken;
  if (!csrfRequest) {
    const version = generation;
    csrfRequest = fetch(apiUrl('/api/auth/csrf'), { credentials: 'include', cache: 'no-store' })
      .then(async (response) => {
        if (!response.ok) throw await errorFrom(response, 'No se pudo preparar una sesión segura.');
        const token = await response.json();
        if (version === generation) csrfToken = token;
        return token;
      }).finally(() => { csrfRequest = undefined; });
  }
  return csrfRequest;
}

async function cookieRequest(path, options = {}) {
  const method = (options.method ?? 'POST').toUpperCase();
  const attempt = async () => {
    const csrf = await ensureCsrfToken();
    const headers = new Headers(options.headers);
    headers.set(csrf.headerName, csrf.token);
    return fetch(apiUrl(path), { ...options, method, headers, credentials: 'include', cache: 'no-store' });
  };
  let response = await attempt();
  if (response.status === 403) {
    csrfToken = undefined;
    response = await attempt();
  }
  return response;
}

// Web Locks serialize rotations across same-origin tabs; the promise coalesces requests in one tab.
export async function restoreAuthentication() {
  if (!refreshRequest) {
    const version = generation;
    const refresh = async () => {
      const response = await cookieRequest('/api/auth/refresh');
      if (!response.ok) {
        if (response.status === 401 && version === generation) clearClientSession();
        throw await errorFrom(response, 'No se pudo renovar tu acceso.');
      }
      const body = await response.json();
      if (version !== generation) throw new ApiError('El acceso cambió durante la solicitud.', 401);
      accessToken = body.accessToken;
      return body.user;
    };
    refreshRequest = withAuthLock(refresh)
      .finally(() => { refreshRequest = undefined; });
  }
  return refreshRequest;
}

export async function authenticate(credentials) {
  if (refreshRequest) await refreshRequest.catch(() => {});
  return withAuthLock(async () => {
    const response = await cookieRequest('/api/auth/login', {
      headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(credentials),
    });
    if (!response.ok) throw await errorFrom(response, 'El usuario o la contraseña no son correctos.');
    const body = await response.json();
    generation++;
    accessToken = body.accessToken;
    csrfToken = undefined;
    return body.user;
  });
}

export async function endAuthentication() {
  // Wait for a rotation before revoking its latest cookie so logout cannot resurrect access.
  if (refreshRequest) await refreshRequest.catch(() => {});
  return withAuthLock(async () => {
    const response = await cookieRequest('/api/auth/logout');
    if (!response.ok) throw await errorFrom(response, 'No se pudo cerrar la sesión.');
    clearClientSession();
  });
}

export async function apiRequest(path, options = {}) {
  if (path.startsWith('/api/auth/') && !['GET', 'HEAD'].includes((options.method ?? 'GET').toUpperCase())) {
    return cookieRequest(path, options);
  }
  const request = () => {
    const headers = new Headers(options.headers);
    if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`);
    return fetch(apiUrl(path), { ...options, headers, credentials: 'omit' });
  };
  const sentToken = accessToken;
  let response = await request();
  // Public anonymous requests must not trigger a refresh.
  if (response.status === 401 && sentToken) {
    if (accessToken === sentToken) await restoreAuthentication();
    response = await request();
    if (response.status === 401) clearClientSession();
  }
  return response;
}

export async function getJson(path, fallbackMessage) {
  const response = await apiRequest(path);
  if (!response.ok) throw await errorFrom(response, fallbackMessage);
  return response.json();
}

export async function sendJson(path, method, body, fallbackMessage) {
  const response = await apiRequest(path, {
    method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body),
  });
  if (!response.ok) throw await errorFrom(response, fallbackMessage);
  return response.status === 204 ? null : response.json();
}
