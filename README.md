# Stocks Project

A full-featured stock tracking application built with Spring Boot. Manage users, track stocks, save favorites, and receive email notifications with secure JWT authentication.

## Overview

This is a comprehensive stock management REST API that allows users to register, authenticate, browse stocks, and manage their favorite stock watchlist. The application features role-based access control, JWT authentication, and email notification capabilities.

## Features

- **User Management**: Registration, authentication, profile management
- **Stock Tracking**: Browse and search stock data with metadata
- **Favorites**: Save and manage favorite stocks per user
- **Authentication**: JWT-based secure authentication
- **Role-Based Access**: Admin and User roles with different permissions
- **Email Notifications**: Send stock information via email
- **API Documentation**: Swagger/OpenAPI integration

## Tech Stack

- **Framework**: Spring Boot 3.1.3
- **Language**: Java 17
- **Database**: PostgreSQL (production), H2 (testing)
- **Security**: Spring Security with JWT (jjwt 0.12.2)
- **ORM**: Spring Data JPA / Hibernate
- **Documentation**: SpringDoc OpenAPI
- **Build Tool**: Maven
- **Other**: Lombok, Spring Mail

## Installation

### Prerequisites

- Java 17+
- PostgreSQL
- Maven

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/Abdurauf23/stocks-project.git
   cd stocks-project
   ```

2. Configure the database in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/stocks_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. Initialize the database schema:
   ```bash
   psql -U your_username -d stocks_db -f database_schema.sql
   ```

4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/register` | User registration |

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user |

### Stocks
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/stocks` | Get all stocks |
| GET | `/api/stocks/{symbol}` | Get stock by symbol |
| POST | `/api/stocks` | Add new stock |
| DELETE | `/api/stocks/{id}` | Delete stock |

### Favorites
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/favorites` | Get user's favorite stocks |
| POST | `/api/favorites` | Add stock to favorites |
| DELETE | `/api/favorites/{stockId}` | Remove from favorites |

### Security Info
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/security-info` | Get security information |

## Database Schema

The application uses the following main tables:

- **stocks_user**: User profiles and personal information
- **security_info**: Authentication credentials and roles
- **role**: User roles (ADMIN, USER)
- **stock_meta**: Stock metadata (symbol, exchange, currency)
- **stock_value**: Historical stock values (OHLCV data)
- **stock_users_fav_stocks**: User-stock favorites relationship

## Project Structure

```
stocks-project/
├── src/main/java/com/stocks/project/
│   ├── config/           # Configuration classes
│   ├── controller/       # REST controllers
│   ├── exception/        # Custom exceptions
│   ├── model/            # Entity classes and DTOs
│   ├── repository/       # JPA repositories
│   ├── security/         # JWT and Spring Security
│   ├── service/          # Business logic
│   └── utils/            # Mapper utilities
├── src/test/java/        # Unit and integration tests
├── database_schema.sql   # Database initialization script
├── pom.xml               # Maven dependencies
└── README.md
```

## Security

- JWT tokens for stateless authentication
- Password encoding with BCrypt
- Role-based access control (ADMIN/USER)
- Secure endpoints with Spring Security

## API Documentation

Access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## Testing

Run tests with:
```bash
./mvnw test
```

The project includes:
- Controller tests
- Service tests
- Repository tests
