package xyz.kgy.production

import xyz.kgy.production.controller.ResourceController
import xyz.kgy.production.service.{ResourceService, ResourceServiceImpl}
import zio._
import zio.http._
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {
  
  val app = Routes(
    Method.GET / "health" ->
      handler {
        Response.json("""{"status": "ok"}""")
      },
    
    Method.GET / Root ->
      handler {
        Response.html("""
        <!DOCTYPE html>
        <html>
        <head>
          <title>Resource Manager Server</title>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <style>
            body { font-family: Arial, sans-serif; margin: 40px; }
            .container { max-width: 800px; margin: 0 auto; }
            h1 { color: #333; }
            .endpoint { background: #f5f5f5; padding: 10px; margin: 10px 0; border-radius: 5px; }
            .method { font-weight: bold; color: #0066cc; }
          </style>
        </head>
        <body>
          <div class="container">
            <h1>Resource Manager Server</h1>
            <p>Welcome to the Resource Manager Server built with ZIO!</p>
            <div class="endpoint">
              <strong>Health Check:</strong> <a href="/health">/health</a>
            </div>
            <h2>API Endpoints:</h2>
            <div class="endpoint">
              <span class="method">GET</span> <a href="/api/resources">/api/resources</a> - List all resources
            </div>
            <div class="endpoint">
              <span class="method">GET</span> /api/resources/{id} - Get resource by ID
            </div>
            <div class="endpoint">
              <span class="method">POST</span> /api/resources - Create new resource
            </div>
            <div class="endpoint">
              <span class="method">PUT</span> /api/resources/{id} - Update resource
            </div>
            <div class="endpoint">
              <span class="method">DELETE</span> /api/resources/{id} - Delete resource
            </div>
          </div>
        </body>
        </html>
      """)
      }
  ) ++ ResourceController.routes

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  override def run: ZIO[Any, Any, Any] = {
    val port = 34567
    
    for {
      _ <- ZIO.logInfo(s"Starting Resource Manager Server on port $port")
      _ <- Server.serve(app).provide(
        Server.defaultWithPort(port),
        ResourceServiceImpl.layer
      )
    } yield ()
  }
} 