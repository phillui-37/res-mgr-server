package xyz.kgy.production.controller

import zio._
import zio.http._
import zio.json._
import xyz.kgy.production.domain._
import xyz.kgy.production.service._

object ResourceController {
  
  def routes = Routes(
    Method.GET / "api" / "resources" ->
      handler { (_: Request) =>
        ResourceService.getAll
          .map(resources => Response.json(resources.toJson))
          .orDie
      },
    
    Method.GET / "api" / "resources" / string("id") ->
      handler { (id: String, _: Request) =>
        (for {
          resourceId <- ZIO.attempt(id.toLong)
          resource <- ResourceService.getById(resourceId)
          response <- resource match {
            case Some(r) => ZIO.succeed(Response.json(r.toJson))
            case None => ZIO.succeed(Response.notFound)
          }
        } yield response).catchAll(_ => ZIO.succeed(Response.badRequest("Invalid resource ID")))
      },
    
    Method.POST / "api" / "resources" ->
      handler { (req: Request) =>
        (for {
          body <- req.body.asString
          createRequest <- ZIO.fromEither(body.fromJson[CreateResourceRequest])
          resource <- ResourceService.create(createRequest)
        } yield Response.json(resource.toJson).status(Status.Created))
          .catchAll(_ => ZIO.succeed(Response.badRequest("Invalid JSON")))
      },
    
    Method.PUT / "api" / "resources" / string("id") ->
      handler { (id: String, req: Request) =>
        (for {
          resourceId <- ZIO.attempt(id.toLong)
          body <- req.body.asString
          updateRequest <- ZIO.fromEither(body.fromJson[UpdateResourceRequest])
          resource <- ResourceService.update(resourceId, updateRequest)
          response <- resource match {
            case Some(r) => ZIO.succeed(Response.json(r.toJson))
            case None => ZIO.succeed(Response.notFound)
          }
        } yield response).catchAll(_ => ZIO.succeed(Response.badRequest("Invalid request")))
      },
    
    Method.DELETE / "api" / "resources" / string("id") ->
      handler { (id: String, _: Request) =>
        (for {
          resourceId <- ZIO.attempt(id.toLong)
          deleted <- ResourceService.delete(resourceId)
          response <- if (deleted) ZIO.succeed(Response.ok) else ZIO.succeed(Response.notFound)
        } yield response).catchAll(_ => ZIO.succeed(Response.badRequest("Invalid resource ID")))
      }
  )
} 