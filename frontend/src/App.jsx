import { Navigate, Route, Routes } from 'react-router-dom';
import AppShell from './layout/AppShell';
import HomePage from './features/home/HomePage';
import GameDetailPage from './features/games/GameDetailPage';

export default function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<HomePage />} />
        <Route path="games/:gameId" element={<GameDetailPage />} />
        <Route path="*" element={<Navigate replace to="/" />} />
      </Route>
    </Routes>
  );
}
