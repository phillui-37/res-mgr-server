# Resource Manager Server

A lightweight CRUD server built with ZIO 2.1.20 and ZIO HTTP.

## Features

- **ZIO 2.1.20**: Modern functional programming with ZIO
- **ZIO HTTP**: Lightweight HTTP server
- **JSON Support**: Built-in JSON serialization/deserialization
- **Logging**: Structured logging with SLF4J
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

```bash
sbt run
```

The server will start on port 8080.

## Testing

```bash
sbt test
```

## Project Structure

```
src/
├── main/scala/
│   ├── Main.scala                    # Application entry point
│   ├── domain/
│   │   └── Resource.scala            # Domain models
│   ├── service/
│   │   ├── ResourceService.scala     # Service interface
│   │   └── ResourceServiceImpl.scala # Service implementation
│   └── controller/
│       └── ResourceController.scala  # HTTP controllers
└── test/scala/
    └── ResourceServiceSpec.scala     # Tests
```

## Dependencies

- **ZIO**: Core functional programming library
- **ZIO HTTP**: HTTP server
- **ZIO JSON**: JSON serialization
- **ZIO Logging**: Structured logging
- **Slick**: Database access (configured but not used in current implementation)
- **PostgreSQL**: Database driver
- **Cats**: Functional programming utilities

## Development

This is a lightweight CRUD server that can be easily extended with:

- Database persistence (Slick is already configured)
- Authentication and authorization
- Additional domain models
- More complex business logic

The current implementation uses in-memory storage for simplicity. 