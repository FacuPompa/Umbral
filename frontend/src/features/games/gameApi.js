const GAMES_URL = '/api/games';


export async function fetchGames() {
  const response = await fetch(GAMES_URL);

  if (!response.ok) {
    throw new Error('No se pudo cargar el catálogo.');
  }

  return response.json();
}