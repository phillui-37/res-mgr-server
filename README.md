# Resource Manager Server

A robust CRUD server built with ZIO 2.1.20, ZIO HTTP, and PostgreSQL database integration.

## Features

- **ZIO 2.1.20**: Modern functional programming with ZIO
- **ZIO HTTP**: Lightweight HTTP server
- **PostgreSQL Integration**: Full database persistence with Slick
- **Flyway Migrations**: Database schema versioning and migrations
- **Docker Support**: Containerized deployment with Docker and Docker Compose
- **JSON Support**: Built-in JSON serialization/deserialization
- **Logging**: Structured logging with SLF4J
- **Environment-based Configuration**: Support for both application.conf and environment variables
- **Testing**: ZIO Test framework for comprehensive testing

## API Endpoints

### Resources

- `GET /api/resources` - List all resources
- `GET /api/resources/{id}` - Get resource by ID
- `POST /api/resources` - Create new resource
- `PUT /api/resources/{id}` - Update resource
- `DELETE /api/resources/{id}` - Delete resource

### Health Check

- `GET /health` - Health check endpoint

## Running the Application

### Local Development

```bash
# Start PostgreSQL (if not already running)
docker run -d --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=mysecretpassword -e POSTGRES_DB=resource_manager postgres:17

# Run the application
sbt run
```

### Docker Compose

```bash
docker-compose up -d
```

The server will start on port 8080 by default.

## Project Structure

```
src/
├── main/
│   ├── resources/
│   │   ├── application.conf          # Application configuration
│   │   ├── logback.xml               # Logging configuration
│   │   └── db/migration/             # Flyway database migrations
│   │       └── V1__Create_resources_table.sql
│   └── scala/xyz/kgy/production/
│       ├── Main.scala                # Application entry point
│       ├── AppLayers.scala           # ZIO dependency injection layers
│       ├── config/
│       │   └── AppConfig.scala       # Configuration service
│       ├── db/
│       │   ├── DatabaseConfig.scala  # Database connection setup
│       │   ├── FlywayMigration.scala # Database migration service
│       │   ├── ResourceRepository.scala # Database repository
│       │   ├── DatabaseMigration.scala # Schema management
│       │   └── Tables.scala          # Slick table definitions
│       ├── domain/
│       │   └── Resource.scala        # Domain models
│       ├── service/
│       │   ├── ResourceService.scala # Service interface
│       │   └── ResourceServiceImpl.scala # Service implementation
│       ├── controller/
│       │   └── ResourceController.scala # HTTP controllers
│       └── util/
│           └── SysUtil.scala         # Utility functions
└── test/scala/
    └── ResourceServiceSpec.scala     # Tests
```

## Configuration

The application can be configured using either:

1. **Environment Variables**:
   - `PORT` - HTTP server port (default: 8080)
   - `DB_HOST` - Database hostname (default: localhost)
   - `DB_PORT` - Database port (default: 5432)
   - `DB_NAME` - Database name (default: resource_manager)
   - `DB_USER` - Database username (default: postgres)
   - `DB_PASSWORD` - Database password

2. **application.conf**:
   - Used as fallback when environment variables are not set

## Database Migrations

Database schema is managed using Flyway migrations:

```bash
# Migrations are automatically applied on startup
# To manually view migration status:
sbt "runMain xyz.kgy.production.db.FlywayMigrationCLI info"
```

To add new migrations, create versioned SQL files in `src/main/resources/db/migration/`:
- `V2__Add_new_feature.sql`
- `V3__Update_schema.sql`

## Dependencies

- **ZIO**: Core functional programming library
- **ZIO HTTP**: HTTP server
- **ZIO JSON**: JSON serialization
- **ZIO Logging**: Structured logging
- **Slick**: Database access layer
- **Flyway**: Database migration tool
- **PostgreSQL**: Database driver
- **HikariCP**: Connection pooling
- **Cats**: Functional programming utilities

## Docker Support

The application includes:
- `Dockerfile` for containerization
- `docker-compose.yml` for local development

## Development

This server can be easily extended with:

- Authentication and authorization
- Additional domain models
- More complex business logic
- Additional database migrations 