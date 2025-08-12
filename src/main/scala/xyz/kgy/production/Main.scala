package xyz.kgy.production

import xyz.kgy.production.controller.ResourceController
import xyz.kgy.production.db.{DatabaseProvider, FlywayMigration}
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
  private val initializeDatabase: ZIO[DatabaseProvider & FlywayMigration, Throwable, Unit] =
    for {
      _ <- ZIO.logInfo("Running database migrations...")
      _ <- FlywayMigration.migrate
      _ <- ZIO.logInfo("Database migrations completed!")
    } yield ()

  override def run: ZIO[Any, Any, Any] = {
    for {
      config <- ZIO.service[AppConfig]
      _ <- ZIO.when(config.env == "DEBUG")(
        ZIO.logDebug(s"config: $config")
      )
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