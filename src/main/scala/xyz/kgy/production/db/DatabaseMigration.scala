package xyz.kgy.production.db

import xyz.kgy.production.db.Tables._
import zio._
import slick.dbio.DBIO

/**
 * Database migration service for schema management
 */
trait DatabaseMigration {
  def migrate: ZIO[DatabaseProvider, Throwable, Unit]
  def createTables: ZIO[DatabaseProvider, Throwable, Unit]
  def dropTables: ZIO[DatabaseProvider, Throwable, Unit]
}

/**
 * Slick-based implementation of DatabaseMigration
 */
class SlickDatabaseMigration extends DatabaseMigration {
  import slick.jdbc.PostgresProfile.api._

  private def runDBIO[T](action: DBIO[T]): ZIO[DatabaseProvider, Throwable, T] = {
    for {
      provider <- ZIO.service[DatabaseProvider]
      result <- ZIO.fromFuture(_ => provider.database.run(action))
    } yield result
  }

  override def migrate: ZIO[DatabaseProvider, Throwable, Unit] = {
    for {
      _ <- ZIO.logInfo("Starting database migration...")
      _ <- createTables
      _ <- ZIO.logInfo("Database migration completed successfully!")
    } yield ()
  }

  override def createTables: ZIO[DatabaseProvider, Throwable, Unit] = {
    val createTablesAction = Tables.schema.createIfNotExists
    for {
      _ <- ZIO.logInfo("Creating database tables if they don't exist...")
      _ <- runDBIO(createTablesAction)
      _ <- ZIO.logInfo("Database tables created successfully!")
    } yield ()
  }

  override def dropTables: ZIO[DatabaseProvider, Throwable, Unit] = {
    val dropTablesAction = Tables.schema.dropIfExists
    for {
      _ <- ZIO.logInfo("Dropping database tables...")
      _ <- runDBIO(dropTablesAction)
      _ <- ZIO.logInfo("Database tables dropped successfully!")
    } yield ()
  }
}

object DatabaseMigration {
  /**
   * ZLayer for the DatabaseMigration service
   */
  val live: ZLayer[DatabaseProvider, Nothing, DatabaseMigration] = 
    ZLayer.succeed(new SlickDatabaseMigration())

  // Service accessor methods
  def migrate: ZIO[DatabaseMigration & DatabaseProvider, Throwable, Unit] = 
    ZIO.serviceWithZIO[DatabaseMigration](_.migrate)

  def createTables: ZIO[DatabaseMigration & DatabaseProvider, Throwable, Unit] = 
    ZIO.serviceWithZIO[DatabaseMigration](_.createTables)

  def dropTables: ZIO[DatabaseMigration & DatabaseProvider, Throwable, Unit] = 
    ZIO.serviceWithZIO[DatabaseMigration](_.dropTables)
}