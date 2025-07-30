package xyz.kgy.production.db

import slick.jdbc.PostgresProfile
import zio._
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import xyz.kgy.production.config.AppConfig

// Define DatabaseProvider trait since we're using it directly
trait DatabaseProvider {
  def database: slick.jdbc.PostgresProfile.api.Database
  def profile: PostgresProfile
}

/**
 * Database configuration and provider setup
 */
object DatabaseConfig {

  /**
   * Simple implementation of DatabaseProvider that creates a database connection
   * using the configuration from AppConfig
   */
  private class SlickDatabaseProvider(db: slick.jdbc.PostgresProfile.api.Database) extends DatabaseProvider {
    override val database: slick.jdbc.PostgresProfile.api.Database = db
    override val profile: PostgresProfile = PostgresProfile
  }

  /**
   * Creates a Typesafe Config for database configuration using AppConfig values
   */
  private def createDatabaseConfig(appConfig: AppConfig): Config = {
    import com.typesafe.config.ConfigValueFactory._
    ConfigFactory.empty()
      .withValue("connectionPool", fromAnyRef("HikariCP"))
      .withValue("dataSourceClass", fromAnyRef("org.postgresql.ds.PGSimpleDataSource"))
      .withValue("properties.serverName", fromAnyRef(appConfig.dbHost))
      .withValue("properties.portNumber", fromAnyRef(appConfig.dbPort))
      .withValue("properties.databaseName", fromAnyRef(appConfig.dbName))
      .withValue("properties.user", fromAnyRef(appConfig.dbUser))
      .withValue("properties.password", fromAnyRef(appConfig.dbPassword))
      .withValue("numThreads", fromAnyRef(10))
      .withValue("maxConnections", fromAnyRef(10))
      .withValue("minConnections", fromAnyRef(1))
  }

  /**
   * Layer that provides the database provider
   * Creates a connection using the configuration from AppConfig
   */
  val live: ZLayer[AppConfig, Throwable, DatabaseProvider] = 
    ZLayer.scoped {
      ZIO.acquireRelease {
        for {
          appConfig <- ZIO.service[AppConfig]
          _ <- ZIO.logInfo(s"Connecting to database at ${appConfig.dbHost}:${appConfig.dbPort}/${appConfig.dbName} with user ${appConfig.dbUser}")
          dbConfig = createDatabaseConfig(appConfig)
          database <- ZIO.attempt {
            import slick.jdbc.PostgresProfile.api._
            Database.forConfig("", dbConfig)
          }
          provider <- ZIO.succeed(new SlickDatabaseProvider(database))
        } yield provider
      } { provider =>
        ZIO.attempt(provider.database.close()).orDie
      }
    }
}