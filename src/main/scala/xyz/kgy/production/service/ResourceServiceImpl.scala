package xyz.kgy.production.service

import zio._
import xyz.kgy.production.domain._
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class ResourceServiceImpl extends ResourceService {
  private val idCounter = new AtomicLong(1)
  private val resources = scala.collection.mutable.Map[Long, Resource]()
  
  override def getAll: Task[List[Resource]] = 
    ZIO.succeed(resources.values.toList.sortBy(_.id))
  
  override def getById(id: Long): Task[Option[Resource]] = 
    ZIO.succeed(resources.get(id))
  
  override def create(request: CreateResourceRequest): Task[Resource] = 
    ZIO.succeed {
      val now = Instant.now()
      val id = idCounter.getAndIncrement()
      val resource = Resource(
        id = Some(id),
        name = request.name,
        description = request.description,
        createdAt = now,
        updatedAt = now
      )
      resources.put(id, resource)
      resource
    }
  
  override def update(id: Long, request: UpdateResourceRequest): Task[Option[Resource]] = 
    ZIO.succeed {
      resources.get(id).map { existing =>
        val updated = existing.copy(
          name = request.name.getOrElse(existing.name),
          description = request.description.orElse(existing.description),
          updatedAt = Instant.now()
        )
        resources.put(id, updated)
        updated
      }
    }
  
  override def delete(id: Long): Task[Boolean] = 
    ZIO.succeed(resources.remove(id).isDefined)
}

object ResourceServiceImpl {
  val layer: ZLayer[Any, Nothing, ResourceService] = 
    ZLayer.succeed(new ResourceServiceImpl())
} 