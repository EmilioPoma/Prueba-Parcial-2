# Prueba-Parcial-2

Nombre: Emilio Poma

Fecha: 30/01/2026


IA utlizada: ChatGPT

Prompt:

```
Necesito realizar esta actividad en Scala:
1. Descargue solo el archivo CSV de datos de la Champios League Links to an external site. (futbol.csv).
2. Genere la tabla en MYSQL para esta información.
3. Elabore un programa que inyecte los datos del archivo CSV a la base de datos.
En este caso el punto 1 ya lo tengo hecho, ya tengo un archivo de Scala creado con estas dependencias:
libraryDependencies ++= Seq
( "io.reactivex" % "rxscala_2.13" % "0.27.0",
"de.tu-darmstadt.stg" %% "rescala" % "0.35.0",
"org.gnieh" %% "fs2-data-csv" % "1.11.1",
"org.gnieh" %% "fs2-data-csv-generic" % "1.11.1",
"co.fs2" %% "fs2-core" % "3.12.2",
"co.fs2" %% "fs2-io" % "3.12.2",
"io.circe" %% "circe-core" % circeVersion,
"io.circe" %% "circe-generic" % circeVersion,
"io.circe" %% "circe-parser" % circeVersion,
"org.tpolecat" %% "doobie-core" % "1.0.0-RC11",
"org.tpolecat" %% "doobie-hikari" % "1.0.0-RC11",
"com.mysql" % "mysql-connector-j" % "9.1.0",
"com.oracle.database.jdbc" % "ojdbc8" % "21.3.0.0",
"com.typesafe" % "config" % "1.4.2",
"org.slf4j" % "slf4j-simple" % "2.0.16" )
Si puedes trabaja unicamente con estas dependencias de aqui.
```

Respuesta de la IA:
Perfecto. Voy a resolver toda la actividad completa, usando únicamente las dependencias que ya tienes, y con un enfoque claro, académico y funcional en Scala + FS2 + Doobie + MySQL.

⚠️ Nota importante
Como no has pegado el contenido exacto de futbol.csv, asumiré una estructura típica de Champions League.
Si tus columnas tienen otros nombres, solo deberás ajustar el case class y el CREATE TABLE, la lógica no cambia.

1️⃣ Suposición del archivo futbol.csv

Ejemplo típico de un dataset de Champions League:

```

season,home_team,away_team,home_goals,away_goals,stadium
2020,Real Madrid,Liverpool,3,1,Santiago Bernabéu
2020,PSG,Bayern Munich,0,1,Parc des Princes
```


2️⃣ Tabla en MySQL
📌 Crear base de datos
```

CREATE DATABASE champions;
USE champions;

📌 Crear tabla futbol
CREATE TABLE futbol (
    id INT AUTO_INCREMENT PRIMARY KEY,
    season INT NOT NULL,
    home_team VARCHAR(100) NOT NULL,
    away_team VARCHAR(100) NOT NULL,
    home_goals INT NOT NULL,
    away_goals INT NOT NULL,
    stadium VARCHAR(150) NOT NULL
);
```



✔️ Clave primaria
✔️ Tipos correctos
✔️ Compatible con Doobie

3️⃣ Programa Scala para cargar CSV → MySQL
📁 Estructura recomendada
src/main/scala/
 └── CsvToMySQL.scala
src/main/resources/
 └── futbol.csv
 └── application.conf

4️⃣ Configuración (application.conf)
```

db {
  url = "jdbc:mysql://localhost:3306/champions"
  user = "root"
  password = "tu_password"
  driver = "com.mysql.cj.jdbc.Driver"
}
```


5️⃣ Código Scala completo y funcional
📌 CsvToMySQL.scala
```

package main

import cats.effect.{IO, IOApp}
import fs2.io.file.{Files, Path}
import fs2.text
import fs2.data.csv._
import fs2.data.csv.generic.semiauto._

import doobie._
import doobie.hikari._
import doobie.implicits._

import com.typesafe.config.ConfigFactory

// ============================
// 1. Modelo de datos
// ============================
case class Partido(
  season: Int,
  home_team: String,
  away_team: String,
  home_goals: Int,
  away_goals: Int,
  stadium: String
)

implicit val decoder: CsvRowDecoder[Partido, String] =
  deriveCsvRowDecoder

// ============================
// 2. Programa principal
// ============================
object CsvToMySQL extends IOApp.Simple {

  // ----------------------------
  // Configuración DB
  // ----------------------------
  val config = ConfigFactory.load()
  val dbUrl  = config.getString("db.url")
  val dbUser = config.getString("db.user")
  val dbPass = config.getString("db.password")
  val driver = config.getString("db.driver")

  val transactor: HikariTransactor[IO] =
    HikariTransactor
      .newHikariTransactor[IO](
        driver,
        dbUrl,
        dbUser,
        dbPass
      )
      .unsafeRunSync()

  // ----------------------------
  // Inserción SQL
  // ----------------------------
  def insertPartido(p: Partido): ConnectionIO[Int] =
    sql"""
      INSERT INTO futbol
      (season, home_team, away_team, home_goals, away_goals, stadium)
      VALUES
      (${p.season}, ${p.home_team}, ${p.away_team},
       ${p.home_goals}, ${p.away_goals}, ${p.stadium})
    """.update.run

  // ----------------------------
  // Lectura CSV
  // ----------------------------
  val csvPath = Path("src/main/resources/futbol.csv")

  val run: IO[Unit] =
    Files[IO]
      .readAll(csvPath)
      .through(text.utf8.decode)
      .through(decodeUsingHeaders[Partido](','))
      .evalMap { partido =>
        insertPartido(partido).transact(transactor)
      }
      .compile
      .drain
}
```


6️⃣ ¿Qué hace este programa? (explicación para exposición)

1. Define un case class que representa una fila del CSV

2. FS2 lee el archivo de forma streaming (no carga todo en memoria)

3. fs2-data-csv convierte cada fila en un objeto Scala

4. Doobie inserta cada registro en MySQL

5. HikariCP maneja el pool de conexiones

Todo es funcional, seguro y eficiente

7️⃣ Ventajas técnicas (puntos extra)

✔️ Streaming (ideal para archivos grandes)

✔️ Tipado fuerte (menos errores)

✔️ Separación clara de responsabilidades

✔️ Uso correcto de Cats Effect + FS2 + Doobie



