package xyz.kgy.production.db

import xyz.kgy.production.domain.{Resource, CreateResourceRequest, UpdateResourceRequest}
import xyz.kgy.production.db.Tables._
import zio._
import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import slick.dbio.DBIO

/**
 * Repository trait defining database operations for Resources
 */
trait ResourceRepository {
  def findAll: ZIO[DatabaseProvider, Throwable, List[Resource]]
  def findById(id: Long): ZIO[DatabaseProvider, Throwable, Option[Resource]]
  def create(request: CreateResourceRequest): ZIO[DatabaseProvider, Throwable, Resource]
  def update(id: Long, request: UpdateResourceRequest): ZIO[DatabaseProvider, Throwable, Option[Resource]]
  def delete(id: Long): ZIO[DatabaseProvider, Throwable, Boolean]
}

/**
 * Slick-based implementation of ResourceRepository using zio-slick-interop
 */
class SlickResourceRepository extends ResourceRepository {
  import slick.jdbc.PostgresProfile.api._

  private def runDBIO[T](action: DBIO[T]): ZIO[DatabaseProvider, Throwable, T] = {
    for {
      provider <- ZIO.service[DatabaseProvider]
      result <- ZIO.fromFuture(_ => provider.database.run(action))
    } yield result
  }

  override def findAll: ZIO[DatabaseProvider, Throwable, List[Resource]] = {
    import slick.jdbc.PostgresProfile.api._
    val query = resources.sortBy(_.id.asc).result
    runDBIO(query).map(_.toList)
  }

  override def findById(id: Long): ZIO[DatabaseProvider, Throwable, Option[Resource]] = {
    import slick.jdbc.PostgresProfile.api._
    val query = resources.filter(_.id === id).result.headOption
    runDBIO(query)
  }

  override def create(request: CreateResourceRequest): ZIO[DatabaseProvider, Throwable, Resource] = {
    import slick.jdbc.PostgresProfile.api._
    val now = Instant.now()
    val newResource = Resource(
      id = None,
      name = request.name,
      description = request.description,
      createdAt = now,
      updatedAt = now
    )
    
    val insertQuery = (resources.returning(resources.map(_.id)).into((resource, id) => resource.copy(id = Some(id)))) += newResource
    runDBIO(insertQuery)
  }

  override def update(id: Long, request: UpdateResourceRequest): ZIO[DatabaseProvider, Throwable, Option[Resource]] = {
    import slick.jdbc.PostgresProfile.api._
    val action = (for {
      resourceOpt <- resources.filter(_.id === id).result.headOption
      result <- resourceOpt match {
        case Some(existing) =>
          val updated = existing.copy(
            name = request.name.getOrElse(existing.name),
            description = request.description.orElse(existing.description),
            updatedAt = Instant.now()
          )
          for {
            _ <- resources.filter(_.id === id).update(updated)
          } yield Some(updated)
        case None =>
          DBIO.successful(None)
      }
    } yield result).transactionally
    
    runDBIO(action)
  }

  override def delete(id: Long): ZIO[DatabaseProvider, Throwable, Boolean] = {
    import slick.jdbc.PostgresProfile.api._
    val deleteQuery = resources.filter(_.id === id).delete
    runDBIO(deleteQuery).map(_ > 0)
  }
}

object ResourceRepository {
  /**
   * ZLayer for the ResourceRepository service
   */
  val live: ZLayer[DatabaseProvider, Nothing, ResourceRepository] = 
    ZLayer.succeed(new SlickResourceRepository())

  // Service accessor methods
  def findAll: ZIO[ResourceRepository & DatabaseProvider, Throwable, List[Resource]] = 
    ZIO.serviceWithZIO[ResourceRepository](_.findAll)

  def findById(id: Long): ZIO[ResourceRepository & DatabaseProvider, Throwable, Option[Resource]] = 
    ZIO.serviceWithZIO[ResourceRepository](_.findById(id))

  def create(request: CreateResourceRequest): ZIO[ResourceRepository & DatabaseProvider, Throwable, Resource] = 
    ZIO.serviceWithZIO[ResourceRepository](_.create(request))

  def update(id: Long, request: UpdateResourceRequest): ZIO[ResourceRepository & DatabaseProvider, Throwable, Option[Resource]] = 
    ZIO.serviceWithZIO[ResourceRepository](_.update(id, request))

  def delete(id: Long): ZIO[ResourceRepository & DatabaseProvider, Throwable, Boolean] = 
    ZIO.serviceWithZIO[ResourceRepository](_.delete(id))
}