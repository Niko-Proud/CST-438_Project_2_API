-- Ah, SQLite.. my old friend and fickle lover.

--Users table: Has a unique username.

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    is_admin INTEGER NOT NULL DEFAULT 0
        CHECK (is_admin IN (0, 1))
);

-- Registry items table: Has a foreign key to users where if the
-- user gets deleted, this field will be deleted as well.
CREATE TABLE IF NOT EXISTS registry_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    product TEXT NOT NULL CHECK (length(product) BETWEEN 1 AND 200),
    batch TEXT CHECK (batch IS NULL OR length(batch) <= 100),

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Recalled foods table: Has a foreign key to
-- registry_items where if the registry item
-- gets deleted, this field will become null.
CREATE TABLE IF NOT EXISTS recalled_foods (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    registry_item_id INTEGER,
    product TEXT NOT NULL,
    contaminant TEXT NOT NULL,
    recall_date TEXT NOT NULL,
    manu_date TEXT NOT NULL,
    batch TEXT,

    FOREIGN KEY (registry_item_id)
    REFERENCES registry_items(id) ON DELETE SET NULL
);
