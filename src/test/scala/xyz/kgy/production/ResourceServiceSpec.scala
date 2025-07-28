package xyz.kgy.production

import zio.*
import zio.test.*
import xyz.kgy.production.service.{ResourceService, ResourceServiceImpl}
import xyz.kgy.production.domain.{CreateResourceRequest, UpdateResourceRequest}

object ResourceServiceSpec extends ZIOSpecDefault {
  
  def spec = suite("ResourceService")(
    test("should create and retrieve a resource") {
      for {
        service <- ZIO.service[ResourceService]
        createRequest = CreateResourceRequest("Test Resource", Some("Test Description"))
        created <- service.create(createRequest)
        retrieved <- service.getById(created.id.get)
      } yield assertTrue(
        created.name == "Test Resource",
        created.description.contains("Test Description"),
        retrieved.contains(created)
      )
    },
    
    test("should update a resource") {
      for {
        service <- ZIO.service[ResourceService]
        createRequest = CreateResourceRequest("Original Name", Some("Original Description"))
        created <- service.create(createRequest)
        updateRequest = UpdateResourceRequest(Some("Updated Name"), Some("Updated Description"))
        updated <- service.update(created.id.get, updateRequest)
        retrieved <- service.getById(created.id.get)
      } yield assertTrue(
        updated.isDefined,
        updated.get.name == "Updated Name",
        updated.get.description.contains("Updated Description"),
        retrieved.contains(updated.get)
      )
    },
    
    test("should delete a resource") {
      for {
        service <- ZIO.service[ResourceService]
        createRequest = CreateResourceRequest("To Delete", None)
        created <- service.create(createRequest)
        deleted <- service.delete(created.id.get)
        retrieved <- service.getById(created.id.get)
      } yield assertTrue(
        deleted,
        retrieved.isEmpty
      )
    },
    
    test("should list all resources") {
      for {
        service <- ZIO.service[ResourceService]
        _ <- service.create(CreateResourceRequest("Resource 1", None))
        _ <- service.create(CreateResourceRequest("Resource 2", None))
        all <- service.getAll
      } yield assertTrue(all.length >= 2)
    }
  ).provide(ResourceServiceImpl.layer)
} 