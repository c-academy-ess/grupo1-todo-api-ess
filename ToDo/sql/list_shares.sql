CREATE TABLE list_shares (
    list_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (list_id, user_id),
    FOREIGN KEY (list_id) REFERENCES lists(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
