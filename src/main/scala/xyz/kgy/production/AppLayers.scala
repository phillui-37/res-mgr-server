package xyz.kgy.production

import zio._
import xyz.kgy.production.db._
import xyz.kgy.production.service._
import xyz.kgy.production.config.AppConfig

/**
 * Application layer composition following ZIO best practices
 * This file combines all the layers needed for the application
 */
object AppLayers {

  /**
   * Complete database layer stack
   * Provides: DatabaseProvider, ResourceRepository, DatabaseMigration
   */
  val databaseLayer: ZLayer[AppConfig, Throwable, DatabaseProvider & ResourceRepository & DatabaseMigration] =
    DatabaseConfig.live >+> (ResourceRepository.live ++ DatabaseMigration.live)

  /**
   * Flyway migration layer
   * Provides: FlywayMigration
   */
  val flywayLayer: ZLayer[AppConfig, Nothing, FlywayMigration] =
    FlywayMigration.live

  /**
   * Service layer that depends on database layer
   * Provides: ResourceService
   */
  val serviceLayer: ZLayer[DatabaseProvider & ResourceRepository, Nothing, ResourceService] =
    ResourceServiceImpl.layer

  /**
   * Complete application layer stack
   * Provides all services needed by the application
   * Requires AppConfig as input
   */
  val appLayer: ZLayer[AppConfig, Throwable, DatabaseProvider & ResourceRepository & DatabaseMigration & ResourceService & FlywayMigration] =
    databaseLayer >+> serviceLayer ++ flywayLayer
}