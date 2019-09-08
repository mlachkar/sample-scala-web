package com.sample.web

import java.time.Instant

import cats.effect.{ExitCode, IO, IOApp, _}
import cats.implicits._
import com.sample.infra.storage.{DbConf, H2, SampleDbSql}
import com.sample.web.controllers.{api, ui}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

object Launcher extends IOApp {
  private val logger = LoggerFactory.getLogger(this.getClass)
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def run(args: List[String]): IO[ExitCode] = {

    logger.info("Launching webserver (http4s)")
    val started = Instant.now()
    // TODO get conf from application.conf
    val version = "app version" // FIXME should get version from build
    //val dbConf: DbConf = PostgreSQL("jdbc:postgresql:world", "l.knuchel", "l.knuchel")
    val dbConf: DbConf = H2("org.postgresql.Driver", "jdbc:h2:mem:sample_db;MODE=PostgreSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1")
    val db = new SampleDbSql(dbConf)
    db.createTables().unsafeRunSync()
    val httpApp = Router(
      Routes.home -> new ui.HomeCtrl[IO].service,
      Routes.users -> new ui.UserCtrl[IO](db).service,
      "/api/users" -> new api.UserCtrl[IO](db).service,
      "/api/status" -> new api.HealthCtrl[IO](version, started).service,
      Routes.assets -> new ui.AssetCtrl[IO].service
    ).orNotFound
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}


object Routes {
  def home: String = "/"

  def users: String = "/users"

  def assets: String = "/assets"

  def asset(path: String): String = s"$assets/$path"
}
