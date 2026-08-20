INSERT INTO games (title, description)
SELECT
    'Persona 5 Royal',
    'Persona 5 Royal es un juego de rol japonés donde eres un estudiante de secundaria en Tokio que vive una doble vida. De día estudias y haces amigos; de noche te conviertes en un ladrón fantasma que roba los malos deseos de las mentes corruptas.'
WHERE NOT EXISTS (
    SELECT 1
    FROM games
    WHERE title = 'Persona 5 Royal'
);
