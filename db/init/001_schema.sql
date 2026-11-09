CREATE SCHEMA app;
CREATE TABLE app.space (
    space_id VARCHAR(10) PRIMARY KEY,
    building VARCHAR(20) NOT NULL,
    space_name VARCHAR(120) NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity > 0)
);
CREATE TABLE app.report (
    report_id VARCHAR(10) PRIMARY KEY,
    space_id VARCHAR(10) NOT NULL REFERENCES app.space(space_id),
    reported_at TIMESTAMP NOT NULL,
    occupancy_ratio NUMERIC(4,3) NOT NULL CHECK (occupancy_ratio BETWEEN 0 AND 1),
    noise_db NUMERIC(5,2) NOT NULL CHECK (noise_db BETWEEN 0 AND 120)
);
INSERT INTO app.space (space_id, building, space_name, capacity) VALUES
    ('A101', 'A', 'Biblioteca norte', 30),
    ('A102', 'A', 'Sala silenciosa', 20),
    ('B201', 'B', 'Aula de estudio', 15),
    ('C101', 'C', 'Zona de estudio', 40);
INSERT INTO app.report
    (report_id, space_id, reported_at, occupancy_ratio, noise_db) VALUES
    ('R001', 'A101', '2026-10-20 09:00:00', 0.700, 35),
    ('R002', 'A101', '2026-10-20 09:10:00', 0.800, 50),
    ('R003', 'A102', '2026-10-20 09:05:00', 0.200, 20),
    ('R004', 'B201', '2026-10-20 09:07:00', 0.600, 15),
    ('R005', 'C101', '2026-10-20 09:15:00', 0.900, 70),
    ('R006', 'B201', '2026-10-20 09:18:00', 0.500, 10),
    ('R007', 'A102', '2026-10-20 09:12:00', 0.250, 22);
CREATE INDEX report_space_latest_idx
    ON app.report (space_id, reported_at DESC, report_id DESC);
CREATE VIEW app.vw_student_space_status AS
SELECT space_id, building, space_name, capacity, occupancy_ratio, noise_db,
       reported_at,
       GREATEST(capacity - ROUND(capacity * occupancy_ratio)::INTEGER, 0)
           AS available_places
FROM (
    SELECT s.space_id, s.building, s.space_name, s.capacity,
           r.occupancy_ratio, r.noise_db, r.reported_at,
           ROW_NUMBER() OVER (
               PARTITION BY s.space_id
               ORDER BY r.reported_at DESC, r.report_id DESC
           ) AS position
    FROM app.space AS s
    INNER JOIN app.report AS r ON r.space_id = s.space_id
) AS latest
WHERE position = 1;
CREATE ROLE aula_reader LOGIN PASSWORD 'aula_reader_local';
GRANT CONNECT ON DATABASE aula_viva TO aula_reader;
GRANT USAGE ON SCHEMA app TO aula_reader;
GRANT SELECT ON app.vw_student_space_status TO aula_reader;
