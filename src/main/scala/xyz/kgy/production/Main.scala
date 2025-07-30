package xyz.kgy.production

import xyz.kgy.production.controller.ResourceController
import xyz.kgy.production.service.ResourceService
import xyz.kgy.production.db.{DatabaseProvider, DatabaseMigration}
import xyz.kgy.production.config.AppConfig
import zio.*
import zio.http.*
import zio.http.template.Html
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {

  val app = Routes(
    Method.GET / "health" ->
      handler {
        Response.json("""{"status": "ok"}""")
      },

    Method.GET / Root ->
      handler { (request: Request) =>
        for {
          _ <- ZIO.logInfo(s"Src Address: ${request.remoteAddress.getOrElse("")}")
          _ <- ZIO.logInfo(s"Header: ${request.headers}")
        } yield Response.html(Html.raw("<div>well received</div>"))
      }
  ) ++ ResourceController.routes

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  /**
   * Initialize database by running migrations
   */
  private val initializeDatabase: ZIO[DatabaseProvider & DatabaseMigration, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo("Initializing database...")
      migration <- ZIO.service[DatabaseMigration]
      _ <- migration.migrate
      _ <- ZIO.logInfo("Database initialization completed!")
    } yield ()

  override def run: ZIO[Any, Any, Any] = {
    for {
      config <- ZIO.service[AppConfig]
      port = config.port
      _ <- ZIO.logInfo(s"Starting Resource Manager Server on port $port")
      _ <- initializeDatabase
      _ <- Server.serve(app)
    } yield ()
  }.provide(
    AppLayers.appLayer,
    AppConfig.live,
    Server.live,
    ZLayer.fromZIO(ZIO.service[AppConfig].map(config => Server.Config.default.port(port = config.port)))
  )

} 