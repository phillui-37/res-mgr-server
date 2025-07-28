package xyz.kgy.production.service

import zio._
import xyz.kgy.production.domain._

trait ResourceService {
  def getAll: Task[List[Resource]]
  def getById(id: Long): Task[Option[Resource]]
  def create(request: CreateResourceRequest): Task[Resource]
  def update(id: Long, request: UpdateResourceRequest): Task[Option[Resource]]
  def delete(id: Long): Task[Boolean]
}

object ResourceService {
  def getAll: ZIO[ResourceService, Throwable, List[Resource]] = 
    ZIO.serviceWithZIO[ResourceService](_.getAll)
  
  def getById(id: Long): ZIO[ResourceService, Throwable, Option[Resource]] = 
    ZIO.serviceWithZIO[ResourceService](_.getById(id))
  
  def create(request: CreateResourceRequest): ZIO[ResourceService, Throwable, Resource] = 
    ZIO.serviceWithZIO[ResourceService](_.create(request))
  
  def update(id: Long, request: UpdateResourceRequest): ZIO[ResourceService, Throwable, Option[Resource]] = 
    ZIO.serviceWithZIO[ResourceService](_.update(id, request))
  
  def delete(id: Long): ZIO[ResourceService, Throwable, Boolean] = 
    ZIO.serviceWithZIO[ResourceService](_.delete(id))
} 