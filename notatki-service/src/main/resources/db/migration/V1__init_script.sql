CREATE TABLE note (
    note_id serial PRIMARY KEY,
	user_id VARCHAR(50) NOT NULL,
	note TEXT NOT NULL,
	created_on TIMESTAMP NOT NULL,
	modified_on TIMESTAMP
);