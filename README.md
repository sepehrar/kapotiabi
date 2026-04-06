# Kapotiabi

Car repair marketplace for Estonia. Car owners describe their problem, get price estimates from repair shops, and book appointments.

## Documentation

- [Authentication (login, register, password reset)](docs/auth.md)

## Running Locally

```bash
# Backend
cd backend
SPRING_DATASOURCE_PASSWORD=kapotiabi ./mvnw spring-boot:run

# Run tests
cd backend
./mvnw test
```

See [docs/auth.md](docs/auth.md) for full setup instructions and API reference.
