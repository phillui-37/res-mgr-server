package xyz.kgy.production.db

import zio._
import xyz.kgy.production.config.AppConfig

/**
 * Command-line interface for Flyway migration operations
 * Allows running migration commands directly from the command line
 */
object FlywayMigrationCLI extends ZIOAppDefault {
  
  override def run: ZIO[Any, Any, Any] = {
    val program = for {
      args <- getArgs
      _ <- args.toList match {
        case "migrate" :: _ => 
          for {
            _ <- Console.printLine("Running database migrations...")
            _ <- FlywayMigration.migrate
            _ <- Console.printLine("Database migrations completed successfully!")
          } yield ()
          
        case "clean" :: _ => 
          for {
            _ <- Console.printLine("WARNING: This will delete all data in the database!")
            _ <- Console.printLine("Are you sure? (y/N)")
            input <- Console.readLine
            _ <- if (input.trim.toLowerCase == "y") {
              for {
                _ <- Console.printLine("Cleaning database...")
                _ <- FlywayMigration.clean
                _ <- Console.printLine("Database cleaned successfully!")
              } yield ()
            } else {
              Console.printLine("Operation cancelled.")
            }
          } yield ()
          
        case "info" :: _ => 
          for {
            _ <- Console.printLine("Fetching migration info...")
            info <- FlywayMigration.info
            _ <- Console.printLine(info)
          } yield ()
          
        case _ => 
          Console.printLine(
            """
              |Usage: FlywayMigrationCLI <command>
              |
              |Available commands:
              |  migrate - Run pending migrations
              |  clean   - Clean the database (WARNING: deletes all data)
              |  info    - Show migration information
              |""".stripMargin)
      }
    } yield ()
    
    program.provide(
      AppConfig.live,
      FlywayMigration.live
    )
  }
}