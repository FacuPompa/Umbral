import { Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './features/auth/AuthContext';
import AuthPage from './features/auth/AuthPage';
import AppShell from './layout/AppShell';
import HomePage from './features/home/HomePage';
import GameDetailPage from './features/games/GameDetailPage';

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route element={<AppShell />}>
          <Route index element={<HomePage />} />
          <Route path="login" element={<AuthPage mode="login" />} />
          <Route path="register" element={<AuthPage mode="register" />} />
          <Route path="games/:gameId" element={<GameDetailPage />} />
          <Route path="*" element={<Navigate replace to="/" />} />
        </Route>
      </Routes>
    </AuthProvider>
  );
}
