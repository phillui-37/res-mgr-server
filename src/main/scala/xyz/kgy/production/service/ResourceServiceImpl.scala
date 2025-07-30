package xyz.kgy.production.service

import zio._
import xyz.kgy.production.domain._
import xyz.kgy.production.db.{ResourceRepository, DatabaseProvider}

/**
 * Database-backed implementation of ResourceService using ResourceRepository
 */
class ResourceServiceImpl(repository: ResourceRepository) extends ResourceService {
  
  override def getAll: ZIO[DatabaseProvider, Throwable, List[Resource]] = 
    repository.findAll
  
  override def getById(id: Long): ZIO[DatabaseProvider, Throwable, Option[Resource]] = 
    repository.findById(id)
  
  override def create(request: CreateResourceRequest): ZIO[DatabaseProvider, Throwable, Resource] = 
    repository.create(request)
  
  override def update(id: Long, request: UpdateResourceRequest): ZIO[DatabaseProvider, Throwable, Option[Resource]] = 
    repository.update(id, request)
  
  override def delete(id: Long): ZIO[DatabaseProvider, Throwable, Boolean] = 
    repository.delete(id)
}

object ResourceServiceImpl {
  /**
   * ZLayer that provides ResourceService implementation backed by database
   */
  val layer: ZLayer[ResourceRepository & DatabaseProvider, Nothing, ResourceService] = 
    ZLayer.fromFunction((repository: ResourceRepository) => new ResourceServiceImpl(repository))
} 