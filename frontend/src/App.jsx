import { Navigate, Route, Routes } from 'react-router-dom';
import { lazy, Suspense } from 'react';
import LoadingIndicator from './components/LoadingIndicator';
import { AuthProvider } from './features/auth/AuthContext';
import AuthPage from './features/auth/AuthPage';
import AppShell from './layout/AppShell';
import HomePage from './features/home/HomePage';
import GameSuggestionPage from './features/games/GameSuggestionPage';
import ProfilePage from './features/library/ProfilePage';
import LibraryPage from './features/library/LibraryPage';
import PublicProfilePage from './features/library/PublicProfilePage';
import SearchPage from './features/search/SearchPage';

const GameModerationPage = lazy(() => import('./features/games/GameModerationPage'));
const GameDetailPage = lazy(() => import('./features/games/GameDetailPage'));

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route element={<AppShell />}>
          <Route index element={<HomePage />} />
          <Route path="login" element={<AuthPage mode="login" />} />
          <Route path="register" element={<AuthPage mode="register" />} />
          <Route path="games/:gameId" element={<Suspense fallback={<main id="main-content" className="py-12"><LoadingIndicator label="Cargando juego" showLabel /></main>}><GameDetailPage /></Suspense>} />
          <Route path="suggestions/new" element={<GameSuggestionPage />} />
          <Route path="moderation/games" element={<Suspense fallback={<main id="main-content" className="py-12"><LoadingIndicator label="Cargando moderación" showLabel /></main>}><GameModerationPage /></Suspense>} />
          <Route path="me" element={<ProfilePage />} />
          <Route path="me/library" element={<LibraryPage />} />
          <Route path="users/:handle" element={<PublicProfilePage />} />
          <Route path="search" element={<SearchPage />} />
          <Route path="*" element={<Navigate replace to="/" />} />
        </Route>
      </Routes>
    </AuthProvider>
  );
}
