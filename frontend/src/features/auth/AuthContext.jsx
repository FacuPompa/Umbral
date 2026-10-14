import { useEffect, useMemo, useState } from 'react';
import { ApiError } from '../../lib/apiClient';
import { fetchCurrentUser, loginUser, logoutUser, registerUser } from './authApi';
import { AuthContext } from './authContext';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    let expired = false;

    function handleSessionExpired() {
      expired = true;
      setUser(null);
    }
    window.addEventListener('umbral:session-expired', handleSessionExpired);

    async function restoreSession() {
      try {
        const currentUser = await fetchCurrentUser();
        if (active && !expired) setUser(currentUser);
      } catch (error) {
        if (!(error instanceof ApiError) || error.status !== 401) {
          console.error('No se pudo restaurar la sesión.', error);
        }
        if (active) setUser(null);
      } finally {
        if (active) setLoading(false);
      }
    }

    restoreSession();
    return () => {
      active = false;
      window.removeEventListener('umbral:session-expired', handleSessionExpired);
    };
  }, []);

  const value = useMemo(() => ({
    user,
    loading,
    async register(credentials) {
      await registerUser(credentials);
      const authenticatedUser = await loginUser(credentials);
      setUser(authenticatedUser);
      return authenticatedUser;
    },
    async login(credentials) {
      const authenticatedUser = await loginUser(credentials);
      setUser(authenticatedUser);
      return authenticatedUser;
    },
    async logout() {
      await logoutUser();
      setUser(null);
    },
  }), [loading, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
