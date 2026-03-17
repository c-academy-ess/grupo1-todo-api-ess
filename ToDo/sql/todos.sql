CREATE TABLE todos (
    id SERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    list_id INTEGER NOT NULL,
    FOREIGN KEY (list_id) REFERENCES lists(id)
);