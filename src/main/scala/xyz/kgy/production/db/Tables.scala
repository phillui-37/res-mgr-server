package xyz.kgy.production.db

import slick.jdbc.PostgresProfile.api._
import xyz.kgy.production.domain.Resource
import java.time.Instant

/**
 * Slick table definitions for the Resource Manager application
 */
object Tables {

  // Custom column type for Instant
  implicit val instantColumnType: BaseColumnType[Instant] = 
    MappedColumnType.base[Instant, java.sql.Timestamp](
      instant => java.sql.Timestamp.from(instant),
      timestamp => timestamp.toInstant
    )

  /**
   * Resources table definition
   */
  class ResourcesTable(tag: Tag) extends Table[Resource](tag, "resources") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def description = column[Option[String]]("description")
    def createdAt = column[Instant]("created_at")
    def updatedAt = column[Instant]("updated_at")

    def * = (id.?, name, description, createdAt, updatedAt) <> (Resource.apply.tupled, Resource.unapply)
  }

  // Table query for Resources
  val resources = TableQuery[ResourcesTable]

  /**
   * Database schema for all tables
   */
  val schema = resources.schema
}