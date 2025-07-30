package xyz.kgy.production.db

import org.flywaydb.core.Flyway
import xyz.kgy.production.config.AppConfig
import zio._

trait FlywayMigration {
  def migrate: Task[Unit]
  def clean: Task[Unit]
  def info: Task[String]
}

class FlywayMigrationImpl(appConfig: AppConfig) extends FlywayMigration {
  private def createFlyway = {
    Flyway.configure()
      .dataSource(
        s"jdbc:postgresql://${appConfig.dbHost}:${appConfig.dbPort}/${appConfig.dbName}",
        appConfig.dbUser,
        appConfig.dbPassword
      )
      .load()
  }
  
  override def migrate: Task[Unit] = ZIO.attempt {
    val flyway = createFlyway
    val result = flyway.migrate()
    println(s"Applied ${result.migrationsExecuted} migrations")
  }
  
  override def clean: Task[Unit] = ZIO.attempt {
    val flyway = createFlyway
    flyway.clean()
  }
  
  override def info: Task[String] = ZIO.attempt {
    val flyway = createFlyway
    val info = flyway.info()
    val migrations = info.all().map { mi =>
      s"${mi.getVersion} - ${mi.getDescription} - ${mi.getState}"
    }.mkString("\n")
    
    s"Database migrations:\n$migrations"
  }
}

object FlywayMigration {
  val live: ZLayer[AppConfig, Nothing, FlywayMigration] =
    ZLayer.fromFunction(appConfig => new FlywayMigrationImpl(appConfig))
    
  def migrate: ZIO[FlywayMigration, Throwable, Unit] =
    ZIO.serviceWithZIO[FlywayMigration](_.migrate)
    
  def clean: ZIO[FlywayMigration, Throwable, Unit] =
    ZIO.serviceWithZIO[FlywayMigration](_.clean)
    
  def info: ZIO[FlywayMigration, Throwable, String] =
    ZIO.serviceWithZIO[FlywayMigration](_.info)
}

