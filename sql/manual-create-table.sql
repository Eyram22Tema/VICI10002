-- Optional reference SQL.
-- You do NOT need to run this for the beginner setup because
-- spring.jpa.hibernate.ddl-auto=update creates/updates the table automatically.

CREATE TABLE IF NOT EXISTS jibble_attendance_records (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    jibble_entry_id VARCHAR(150) NOT NULL UNIQUE,
    person_id VARCHAR(150),
    person_name VARCHAR(300),
    entry_type VARCHAR(50),
    entry_time TIMESTAMPTZ,
    belongs_to_date DATE,
    note TEXT,
    activity_id VARCHAR(150),
    project_id VARCHAR(150),
    location_id VARCHAR(150),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    raw_payload TEXT,
    last_synced_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_attendance_person_id
    ON jibble_attendance_records(person_id);

CREATE INDEX IF NOT EXISTS idx_attendance_date
    ON jibble_attendance_records(belongs_to_date);

CREATE INDEX IF NOT EXISTS idx_attendance_time
    ON jibble_attendance_records(entry_time);
