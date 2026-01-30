CREATE DATABASE champions;
USE champions;
CREATE TABLE futbol (
    id INT PRIMARY KEY,
    equipo_local VARCHAR(100) NOT NULL,
    equipo_visitante VARCHAR(100) NOT NULL,
    fecha_partido DATE NOT NULL,
    estadio VARCHAR(150) NOT NULL,
    goles_local INT NOT NULL,
    goles_visitante INT NOT NULL,
    partido_jugado BOOLEAN NOT NULL
);

SHOW tables;
DESCRIBE futbol;

SELECT COUNT(*) FROM futbol;
SELECT * FROM futbol;

USE champions;
TRUNCATE TABLE futbol;

















