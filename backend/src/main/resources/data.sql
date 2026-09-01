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
INSERT INTO app_users (handle)
VALUES ('umbral-author-demo')
ON CONFLICT (handle) DO NOTHING;

INSERT INTO user_game_progress (user_id, game_id, checkpoint_id)
SELECT
    app_user.id,
    game.id,
    checkpoint.id
FROM app_users AS app_user
JOIN games AS game
    ON game.title = 'Persona 5 Royal'
JOIN checkpoints AS checkpoint
    ON checkpoint.game_id = game.id
    AND checkpoint.position = 8
WHERE app_user.handle = 'umbral-author-demo'
ON CONFLICT (user_id, game_id) DO UPDATE
SET checkpoint_id = EXCLUDED.checkpoint_id;

INSERT INTO journal_entries (author_id, checkpoint_id, type, content, created_at)
SELECT
    app_user.id,
    checkpoint.id,
    entry_data.type,
    entry_data.content,
    entry_data.created_at
FROM app_users AS app_user
JOIN games AS game
    ON game.title = 'Persona 5 Royal'
JOIN (
    VALUES
        (
            3,
            'REFLECTION',
            'Me gustó mucho cómo cambia la dinámica del grupo después de este tramo.',
            TIMESTAMPTZ '2026-08-20 15:00:00+00'
        ),
        (
            7,
            'THEORY',
            'Cada vez tengo más ganas de sentarme a jugar una tarde entera. Este tramo viene con un ritmo tremendo.',
            TIMESTAMPTZ '2026-08-21 18:30:00+00'
        )
) AS entry_data(checkpoint_position, type, content, created_at)
    ON TRUE
JOIN checkpoints AS checkpoint
    ON checkpoint.game_id = game.id
    AND checkpoint.position = entry_data.checkpoint_position
WHERE app_user.handle = 'umbral-author-demo'
  AND NOT EXISTS (
      SELECT 1
      FROM journal_entries AS existing_entry
      WHERE existing_entry.author_id = app_user.id
        AND existing_entry.checkpoint_id = checkpoint.id
        AND existing_entry.content = entry_data.content
  );

  INSERT INTO journal_entry_replies (
      journal_entry_id,
      author_id,
      content,
      created_at
  )
  SELECT
      entry.id,
      reply_author.id,
      'Me quedó la misma sensación. A partir de acá el grupo empieza a sentirse mucho más unido.',
      TIMESTAMPTZ '2026-08-22 14:00:00+00'
  FROM journal_entries AS entry
  JOIN app_users AS entry_author
      ON entry_author.id = entry.author_id
  JOIN app_users AS reply_author
      ON reply_author.handle = 'umbral-author-demo'
  JOIN checkpoints AS checkpoint
      ON checkpoint.id = entry.checkpoint_id
  JOIN games AS game
      ON game.id = checkpoint.game_id
  WHERE game.title = 'Persona 5 Royal'
    AND checkpoint.position = 3
    AND entry_author.handle = 'umbral-author-demo'
    AND entry.content = 'Me gustó mucho cómo cambia la dinámica del grupo después de este tramo.'
    AND NOT EXISTS (
        SELECT 1
        FROM journal_entry_replies AS existing_reply
        WHERE existing_reply.journal_entry_id = entry.id
          AND existing_reply.author_id = reply_author.id
          AND existing_reply.content = 'Me quedó la misma sensación. A partir de acá el grupo empieza a sentirse mucho más unido.'
    );