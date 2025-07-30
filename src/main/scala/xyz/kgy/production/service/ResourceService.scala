package xyz.kgy.production.service

import zio._
import xyz.kgy.production.domain._
import xyz.kgy.production.db.DatabaseProvider

trait ResourceService {
  def getAll: ZIO[DatabaseProvider, Throwable, List[Resource]]
  def getById(id: Long): ZIO[DatabaseProvider, Throwable, Option[Resource]]
  def create(request: CreateResourceRequest): ZIO[DatabaseProvider, Throwable, Resource]
  def update(id: Long, request: UpdateResourceRequest): ZIO[DatabaseProvider, Throwable, Option[Resource]]
  def delete(id: Long): ZIO[DatabaseProvider, Throwable, Boolean]
}

object ResourceService {
  def getAll: ZIO[ResourceService & DatabaseProvider, Throwable, List[Resource]] = 
    ZIO.serviceWithZIO[ResourceService](_.getAll)
  
  def getById(id: Long): ZIO[ResourceService & DatabaseProvider, Throwable, Option[Resource]] = 
    ZIO.serviceWithZIO[ResourceService](_.getById(id))
  
  def create(request: CreateResourceRequest): ZIO[ResourceService & DatabaseProvider, Throwable, Resource] = 
    ZIO.serviceWithZIO[ResourceService](_.create(request))
  
  def update(id: Long, request: UpdateResourceRequest): ZIO[ResourceService & DatabaseProvider, Throwable, Option[Resource]] = 
    ZIO.serviceWithZIO[ResourceService](_.update(id, request))
  
  def delete(id: Long): ZIO[ResourceService & DatabaseProvider, Throwable, Boolean] = 
    ZIO.serviceWithZIO[ResourceService](_.delete(id))
} 