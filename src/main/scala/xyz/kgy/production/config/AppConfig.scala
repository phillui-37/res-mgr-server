package xyz.kgy.production.config

import zio._
import com.typesafe.config.ConfigFactory
import scala.util.Try

/**
 * Application configuration service that reads values from environment variables first,
 * then falls back to application.conf if not found in environment
 */
trait AppConfig {
  def port: Int
  def dbHost: String
  def dbPort: String
  def dbName: String
  def dbUser: String
  def dbPassword: String
  def env: String
}

object AppConfig {
  /**
   * Implementation of AppConfig that prioritizes environment variables over config file
   */
  private class AppConfigImpl(config: com.typesafe.config.Config) extends AppConfig {
    // Helper method to get value with priority: sys env -> application.conf -> default
    private def getFromEnvOrConfig[T](envName: String, configPath: String, convert: String => T, default: => T): T = {
      // First try environment variable
      sys.env.get(envName)
        .flatMap(envValue => Try(convert(envValue)).toOption)
        .getOrElse {
          // Then try application.conf
          Try(config.getString(configPath))
            .map(convert)
            .getOrElse {
              // Finally fall back to default
              default
            }
        }
    }

    override val port: Int = getFromEnvOrConfig(
      "PORT",
      "app.port",
      _.toInt,
      config.getInt("app.port")
    )

    override val dbHost: String = getFromEnvOrConfig(
      "DB_HOST",
      "database.properties.serverName",
      identity,
      config.getString("database.properties.serverName")
    )

    override val dbPort: String = getFromEnvOrConfig(
      "DB_PORT",
      "database.properties.portNumber",
      identity,
      config.getString("database.properties.portNumber")
    )

    override val dbName: String = getFromEnvOrConfig(
      "DB_NAME",
      "database.properties.databaseName",
      identity,
      config.getString("database.properties.databaseName")
    )

    override val dbUser: String = getFromEnvOrConfig(
      "DB_USER",
      "database.properties.user",
      identity,
      config.getString("database.properties.user")
    )

    override val dbPassword: String = getFromEnvOrConfig(
      "DB_PASSWORD",
      "database.properties.password",
      identity,
      config.getString("database.properties.password")
    )
    
    override val env: String = getFromEnvOrConfig(
      "DEBUG",
      "app.env",
      identity,
      config.getString("app.env")
    )
  }

  /**
   * ZIO Layer that provides AppConfig
   */
  val live: ZLayer[Any, Throwable, AppConfig] = 
    ZLayer.fromZIO {
      for {
        config <- ZIO.attempt(ConfigFactory.load())
      } yield new AppConfigImpl(config)
    }
}