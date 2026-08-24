INSERT INTO games (title, description)
VALUES (
    'Persona 5 Royal',
    'Persona 5 Royal es un juego de rol japonés donde eres un estudiante de secundaria en Tokio que vive una doble vida. De día estudias y haces amigos; de noche te conviertes en un ladrón fantasma que roba los malos deseos de las mentes corruptas.'
)
ON CONFLICT (title) DO NOTHING;

INSERT INTO checkpoints (game_id, label, position)
SELECT
    game.id,
    checkpoint_data.label,
    checkpoint_data.position
FROM games AS game
CROSS JOIN (
    VALUES
        ('Inicio de la historia', 1),
        ('Palacio de Kamoshida', 2),
        ('Palacio de Madarame', 3),
        ('Palacio de Kaneshiro', 4),
        ('Palacio de Futaba', 5),
        ('Palacio de Okumura', 6),
        ('Palacio de Niijima', 7),
        ('Palacio de Shido', 8),
        ('Tercer semestre', 9),
        ('Final de la historia', 10)
) AS checkpoint_data(label, position)
WHERE game.title = 'Persona 5 Royal'
ON CONFLICT (game_id, position) DO NOTHING;

INSERT INTO app_users (id, handle)
VALUES (1, 'umbral-demo')
ON CONFLICT DO NOTHING;

SELECT setval(
    pg_get_serial_sequence('app_users', 'id'),
    (SELECT MAX(id) FROM app_users),
    true
);