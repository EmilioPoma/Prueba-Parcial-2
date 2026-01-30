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
import scala.concurrent.ExecutionContext

// ===============================
// MODELO
// ===============================
case class Partido(
                    ID: Int,
                    Equipo_Local: String,
                    Equipo_Visitante: String,
                    Fecha_Partido: String,
                    Estadio: String,
                    Goles_Local: Int,
                    Goles_Visitante: Int,
                    Partido_Jugado: Boolean
                  )

implicit val decoder: CsvRowDecoder[Partido, String] =
  deriveCsvRowDecoder

// ===============================
// PROGRAMA PRINCIPAL
// ===============================
object CSVaMYSQL extends IOApp.Simple {

  // Configuración
  val config = ConfigFactory.load()
  val url = config.getString("db.url")
  val user = config.getString("db.user")
  val pass = config.getString("db.password")
  val driver = config.getString("db.driver")

  implicit val ec: ExecutionContext =
    ExecutionContext.global

  val transactorResource =
    HikariTransactor.newHikariTransactor[IO](
      driver,
      url,
      user,
      pass,
      ec
    )


  // Inserción
  def insertPartido(p: Partido): ConnectionIO[Int] =
    sql"""
      INSERT INTO futbol
      (id, equipo_local, equipo_visitante, fecha_partido,
       estadio, goles_local, goles_visitante, partido_jugado)
      VALUES
      (${p.ID}, ${p.Equipo_Local}, ${p.Equipo_Visitante},
       ${p.Fecha_Partido}, ${p.Estadio},
       ${p.Goles_Local}, ${p.Goles_Visitante}, ${p.Partido_Jugado})
    """.update.run

  // Lectura CSV
  val csvPath = Path("src/main/resources/data/futbol.csv")

  val run: IO[Unit] =
    transactorResource.use { xa =>
      Files[IO]
        .readAll(csvPath)
        .through(text.utf8.decode)
        .through(decodeUsingHeaders[Partido](','))
        .evalMap(p => insertPartido(p).transact(xa))
        .compile
        .drain
    }

}