import { test, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import { clearClientSession, restoreAuthentication, authenticate, endAuthentication, apiRequest, ApiError } from './apiClient.js';

globalThis.window = new EventTarget();
const calls = [];
const json = (body, status = 200) => new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } });
beforeEach(() => { clearClientSession(); calls.length = 0; });
const csrf = () => json({ token: 'csrf', headerName: 'X-XSRF-TOKEN' });
const tokens = (accessToken) => json({ accessToken, user: { handle: 'test' } });

test('restores access with cookie/CSRF but sends only a memory Bearer to the API', async () => {
  globalThis.fetch = async (path, options) => {
    calls.push({ path, options });
    if (path.endsWith('/csrf')) return csrf();
    if (path.endsWith('/refresh')) return tokens('access-one');
    return json({ ok: true });
  };
  assert.equal((await restoreAuthentication()).handle, 'test');
  await apiRequest('/api/me/library');
  const refresh = calls.find((c) => c.path.endsWith('/refresh'));
  assert.equal(refresh.options.credentials, 'include');
  assert.equal(refresh.options.headers.get('X-XSRF-TOKEN'), 'csrf');
  const api = calls.at(-1);
  assert.equal(api.options.credentials, 'omit');
  assert.equal(api.options.headers.get('Authorization'), 'Bearer access-one');
});

test('coalesces concurrent 401 requests into one rotation and retries each once', async () => {
  let refreshCount = 0;
  globalThis.fetch = async (path, options) => {
    if (path.endsWith('/csrf')) return csrf();
    if (path.endsWith('/login')) return tokens('old');
    if (path.endsWith('/refresh')) {
      refreshCount++;
      await new Promise((resolve) => setTimeout(resolve, 10));
      return tokens('new');
    }
    return options.headers.get('Authorization') === 'Bearer old' ? new Response(null, { status: 401 }) : json({});
  };
  await authenticate({ handle: 'test', password: 'not-logged' });
  const results = await Promise.all([apiRequest('/api/me/library'), apiRequest('/api/auth/me')]);
  assert.ok(results.every((r) => r.ok));
  assert.equal(refreshCount, 1);
});

test('does not renew anonymous public requests or retry denied permissions', async () => {
  globalThis.fetch = async (path) => { calls.push(path); return new Response(null, { status: 401 }); };
  assert.equal((await apiRequest('/api/games')).status, 401);
  assert.deepEqual(calls, ['/api/games']);
});

test('rejected refresh clears memory and notifies React instead of looping', async () => {
  let expired = 0;
  const listener = () => expired++;
  window.addEventListener('umbral:session-expired', listener);
  globalThis.fetch = async (path) => {
    calls.push(path);
    return path.endsWith('/csrf') ? csrf() : new Response(null, { status: 401 });
  };
  await assert.rejects(restoreAuthentication(), (error) => error instanceof ApiError && error.status === 401);
  assert.equal(expired, 1);
  assert.equal(calls.filter((p) => p.endsWith('/refresh')).length, 1);
  window.removeEventListener('umbral:session-expired', listener);
});

test('renews stale CSRF once and never sends the password as Bearer credentials', async () => {
  let attempts = 0;
  globalThis.fetch = async (path, options) => {
    calls.push({ path, options });
    if (path.endsWith('/csrf')) return csrf();
    assert.equal(options.headers.get('Authorization'), null);
    return attempts++ === 0 ? new Response(null, { status: 403 }) : tokens('login-token');
  };
  await authenticate({ handle: 'test', password: 'a-password' });
  assert.equal(attempts, 2);
});

test('logout waits for in-flight refresh and prevents a late token resurrection', async () => {
  globalThis.fetch = async (path, options) => {
    calls.push({ path, options });
    if (path.endsWith('/csrf')) return csrf();
    if (path.endsWith('/refresh')) {
      await new Promise((resolve) => setTimeout(resolve, 10));
      return tokens('rotated');
    }
    return new Response(null, { status: 204 });
  };
  const restoring = restoreAuthentication();
  const logout = endAuthentication();
  await Promise.all([restoring, logout]);
  await apiRequest('/api/games');
  assert.equal(calls.at(-1).options.headers.get('Authorization'), null);
  assert.ok(calls.findIndex((c) => c.path.endsWith('/logout')) > calls.findIndex((c) => c.path.endsWith('/refresh')));
});
