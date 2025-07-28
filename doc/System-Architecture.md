# System Architecture

> **Relevant source files**
> * [pom.xml](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/pom.xml)
> * [src/main/java/com/xhz/yuncang/YuncangApplication.java](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/YuncangApplication.java)
> * [src/main/java/com/xhz/yuncang/config/WebSecurityConfig.java](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/config/WebSecurityConfig.java)

This document provides a comprehensive overview of the yuncang warehouse management system architecture, including its layered design, core components, technology stack, and architectural patterns. It covers the high-level system structure, component interactions, and design decisions that shape the application.

For detailed information about specific subsystems, see [Application Structure](/yanzhe-Xiao/yuncang/3.1-application-structure), [Security Architecture](/yanzhe-Xiao/yuncang/3.2-security-architecture), and [Data Layer Architecture](/yanzhe-Xiao/yuncang/3.3-data-layer-architecture). For business domain functionality, refer to [Warehouse Operations](/yanzhe-Xiao/yuncang/4-warehouse-operations).

## Overview

The yuncang system follows a traditional three-tier layered architecture built on Spring Boot 3, implementing modern enterprise patterns for warehouse management operations. The system supports automated guided vehicle (AGV) management, order processing, inventory tracking, and comprehensive warehouse operations through REST APIs.

## Technology Stack

| Component | Technology | Version | Purpose |
| --- | --- | --- | --- |
| Application Framework | Spring Boot | 3.4.4 | Core application framework |
| Runtime | Java | 21 | Application runtime environment |
| Security | Spring Security | 6.x | Authentication and authorization |
| ORM | MyBatis-Plus | 3.5.12 | Database access and mapping |
| Database | MySQL | 8.0 | Primary data storage |
| Cache | Redis | 6.2 | Session storage and caching |
| Authentication | JWT | 4.3.0 | Stateless token-based auth |
| API Documentation | Knife4j | 4.1.0 | OpenAPI/Swagger integration |
| Build Tool | Maven | - | Dependency management and build |

Sources: [pom.xml L1-L153](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/pom.xml#L1-L153)

## Layered Architecture

The system implements a clean layered architecture with clear separation of concerns:

### System Layers

```mermaid
flowchart TD

REST["REST Controllers"]
SWAGGER["Knife4j/Swagger UI"]
FILTER["JwtAuthenticationFilter"]
SVC["Service Classes"]
CACHE["Redis Cache"]
EMAIL["Mail Service"]
MAPPER["MyBatis Mappers"]
ENTITY["Entity Classes"]
MYBATIS["MyBatis-Plus"]
MYSQL["MySQL Database"]
REDIS_DB["Redis Store"]
CONFIG["Configuration Classes"]

REST --> SVC
SVC --> MAPPER
MYBATIS --> MYSQL
CACHE --> REDIS_DB
CONFIG --> SVC
CONFIG --> FILTER

subgraph subGraph3 ["Infrastructure Layer"]
    MYSQL
    REDIS_DB
    CONFIG
end

subgraph subGraph2 ["Data Access Layer"]
    MAPPER
    ENTITY
    MYBATIS
    MAPPER --> MYBATIS
end

subgraph subGraph1 ["Business Layer"]
    SVC
    CACHE
    EMAIL
    SVC --> CACHE
    SVC --> EMAIL
end

subgraph subGraph0 ["Presentation Layer"]
    REST
    SWAGGER
    FILTER
    FILTER --> REST
    SWAGGER --> REST
end
```

Sources: [src/main/java/com/xhz/yuncang/YuncangApplication.java L1-L18](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/YuncangApplication.java#L1-L18)

 [src/main/java/com/xhz/yuncang/config/WebSecurityConfig.java L1-L104](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/config/WebSecurityConfig.java#L1-L104)

## Core Application Structure

The main application follows Spring Boot conventions with specific configurations for warehouse management:

### Application Bootstrap

```mermaid
flowchart TD

MAIN["YuncangApplication.main()"]
SPRING["SpringApplication.run()"]
SCAN["@MapperScan('com.xhz.yuncang.mapper')"]
CACHE["@EnableCaching"]
BOOT["@SpringBootApplication"]
MAPPERS["MyBatis Mapper Discovery"]
REDIS["Redis Cache Configuration"]
COMPONENT["Component Scanning"]

MAIN --> SPRING
SPRING --> SCAN
SPRING --> CACHE
SPRING --> BOOT
SCAN --> MAPPERS
CACHE --> REDIS
BOOT --> COMPONENT
```

The `YuncangApplication` class serves as the entry point with key annotations:

* `@MapperScan("com.xhz.yuncang.mapper")` - Enables MyBatis mapper scanning
* `@EnableCaching` - Activates Redis-based caching
* `@SpringBootApplication` - Standard Spring Boot configuration

Sources: [src/main/java/com/xhz/yuncang/YuncangApplication.java L8-L16](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/YuncangApplication.java#L8-L16)

## Security Architecture Overview

The system implements JWT-based stateless authentication with Spring Security 6:

### Security Filter Chain

```mermaid
flowchart TD

REQUEST["HTTP Request"]
CORS["CORS Filter"]
JWT_FILTER["JwtAuthenticationFilter"]
VALIDATION["JWT Token Validation"]
AUTH_CHECK["Token Valid?"]
SET_AUTH["Set Authentication Context"]
REJECT["Return 401 Unauthorized"]
CONTROLLER["REST Controller"]
SECURITY_CHECK["Method Security (@PreAuthorize)"]
BUSINESS["Business Logic"]

REQUEST --> CORS
CORS --> JWT_FILTER
JWT_FILTER --> VALIDATION
VALIDATION --> AUTH_CHECK
AUTH_CHECK --> SET_AUTH
AUTH_CHECK --> REJECT
SET_AUTH --> CONTROLLER
CONTROLLER --> SECURITY_CHECK
SECURITY_CHECK --> BUSINESS
```

Key security components:

* `JwtAuthenticationFilter` - Custom JWT token processing
* `SessionCreationPolicy.STATELESS` - Completely stateless session management
* `WebSecurityConfig` - Central security configuration
* Method-level security with `@EnableMethodSecurity`

Sources: [src/main/java/com/xhz/yuncang/config/WebSecurityConfig.java L59-L89](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/config/WebSecurityConfig.java#L59-L89)

## Domain Architecture

The system is organized around warehouse management domains with dedicated controllers and services:

### Domain Components

```mermaid
flowchart TD

FACTORY_CTRL["FactoryConfigController"]
ADMIN_SVC["Admin Services"]
USER_CTRL["User Controllers"]
AGV_CTRL["AgvCarController"]
AGV_SVC["AGV Services"]
PATH_CTRL["AgvPathPlanningController"]
BFS_UTIL["BfsFindPath"]
INBOUND_CTRL["InboundOrderController"]
ORDER_SVC["Order Services"]
OUTBOUND_CTRL["OutboundOrderController"]
DETAIL_CTRL["InboundOrderDetailController"]
INV_CTRL["InventoryController"]
INV_SVC["Inventory Services"]
PROD_CTRL["ProductController"]
SHELF_CTRL["AllStorageController"]

subgraph subGraph3 ["System Domain"]
    FACTORY_CTRL
    ADMIN_SVC
    USER_CTRL
    FACTORY_CTRL --> ADMIN_SVC
    USER_CTRL --> ADMIN_SVC
end

subgraph subGraph2 ["AGV Domain"]
    AGV_CTRL
    AGV_SVC
    PATH_CTRL
    BFS_UTIL
    AGV_CTRL --> AGV_SVC
    PATH_CTRL --> AGV_SVC
    PATH_CTRL --> BFS_UTIL
end

subgraph subGraph1 ["Order Domain"]
    INBOUND_CTRL
    ORDER_SVC
    OUTBOUND_CTRL
    DETAIL_CTRL
    INBOUND_CTRL --> ORDER_SVC
    OUTBOUND_CTRL --> ORDER_SVC
    DETAIL_CTRL --> ORDER_SVC
end

subgraph subGraph0 ["Inventory Domain"]
    INV_CTRL
    INV_SVC
    PROD_CTRL
    SHELF_CTRL
    INV_CTRL --> INV_SVC
    PROD_CTRL --> INV_SVC
    SHELF_CTRL --> INV_SVC
end
```

Sources: Based on controller structure inferred from the overall architecture diagrams

## Data Flow Architecture

The system processes data through well-defined layers with caching and persistence:

### Request Processing Flow

```mermaid
sequenceDiagram
  participant Client
  participant JwtAuthenticationFilter
  participant Controller
  participant Service
  participant RedisCache
  participant MyBatisMapper
  participant MySQL

  Client->>JwtAuthenticationFilter: "HTTP Request + JWT"
  JwtAuthenticationFilter->>JwtAuthenticationFilter: "Validate JWT Token"
  JwtAuthenticationFilter->>Controller: "Forward Authenticated Request"
  Controller->>Service: "Business Method Call"
  Service->>RedisCache: "Check Cache"
  loop [Cache Hit]
    RedisCache->>Service: "Return Cached Data"
    Service->>MyBatisMapper: "Database Query"
    MyBatisMapper->>MySQL: "SQL Execution"
    MySQL->>MyBatisMapper: "Result Set"
    MyBatisMapper->>Service: "Mapped Objects"
    Service->>RedisCache: "Update Cache"
  end
  Service->>Controller: "Business Result"
  Controller->>Client: "HTTP Response"
```

Sources: [src/main/java/com/xhz/yuncang/config/WebSecurityConfig.java L60-L89](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/config/WebSecurityConfig.java#L60-L89)

 [src/main/java/com/xhz/yuncang/YuncangApplication.java L9-L10](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/YuncangApplication.java#L9-L10)

## Configuration Architecture

The system uses Spring Boot's configuration management with profiles and external configuration:

### Configuration Components

```mermaid
flowchart TD

WEB_SEC["WebSecurityConfig"]
JWT_CONFIG["JWT Configuration"]
REDIS_CONFIG["Redis Configuration"]
DB_CONFIG["Database Configuration"]
MAIL_CONFIG["Mail Configuration"]
APP_PROPS["application.properties"]
ENV_VARS["Environment Variables"]
DOCKER_CONFIG["Docker Environment"]
SECURITY["Spring Security"]
DATA_SOURCE["DataSource"]
REDIS_TEMPLATE["RedisTemplate"]
MAIL_SENDER["JavaMailSender"]

APP_PROPS --> WEB_SEC
APP_PROPS --> DB_CONFIG
APP_PROPS --> REDIS_CONFIG
APP_PROPS --> MAIL_CONFIG
DOCKER_CONFIG --> DB_CONFIG
DOCKER_CONFIG --> REDIS_CONFIG
WEB_SEC --> SECURITY
DB_CONFIG --> DATA_SOURCE
REDIS_CONFIG --> REDIS_TEMPLATE
MAIL_CONFIG --> MAIL_SENDER

subgraph subGraph2 ["Spring Components"]
    SECURITY
    DATA_SOURCE
    REDIS_TEMPLATE
    MAIL_SENDER
end

subgraph subGraph1 ["Properties Sources"]
    APP_PROPS
    ENV_VARS
    DOCKER_CONFIG
    ENV_VARS --> DOCKER_CONFIG
end

subgraph subGraph0 ["Configuration Classes"]
    WEB_SEC
    JWT_CONFIG
    REDIS_CONFIG
    DB_CONFIG
    MAIL_CONFIG
end
```

Key configuration features:

* `@Configuration` classes for modular setup
* Environment-specific profiles
* Docker container integration
* External configuration support

Sources: [src/main/java/com/xhz/yuncang/config/WebSecurityConfig.java L43-L46](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/config/WebSecurityConfig.java#L43-L46)

 [pom.xml L33-L132](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/pom.xml#L33-L132)

## Deployment Architecture

The system is designed for containerized deployment with Docker Compose orchestration:

### Container Architecture

```mermaid
flowchart TD

APP["yuncang-app:8080"]
SPRING["Spring Boot Application"]
MYSQL_C["yuncang-mysql:3307"]
MYSQL_DATA["Persistent Volume"]
INIT_SQL["init.sql Script"]
REDIS_C["yuncang-redis:6380"]
REDIS_DATA["Persistent Volume"]
CLIENT["Client Applications"]
API_DOCS["Swagger UI"]

CLIENT --> APP
API_DOCS --> APP

subgraph subGraph4 ["External Access"]
    CLIENT
    API_DOCS
end

subgraph subGraph3 ["Docker Compose Stack"]
    APP --> MYSQL_C
    APP --> REDIS_C

subgraph subGraph2 ["Cache Container"]
    REDIS_C
    REDIS_DATA
    REDIS_C --> REDIS_DATA
end

subgraph subGraph1 ["Database Container"]
    MYSQL_C
    MYSQL_DATA
    INIT_SQL
    MYSQL_C --> MYSQL_DATA
    MYSQL_C --> INIT_SQL
end

subgraph subGraph0 ["Application Container"]
    APP
    SPRING
end
end
```

Deployment characteristics:

* Multi-container orchestration with Docker Compose
* Persistent data volumes for database and cache
* Port mapping for external access
* Environment-based configuration
* Database initialization scripts

Sources: Based on Docker deployment architecture from the provided diagrams

## Integration Points

The system provides multiple integration interfaces:

| Integration Type | Technology | Purpose | Endpoints |
| --- | --- | --- | --- |
| REST API | Spring Web MVC | External system integration | `/api/*` |
| Database | MySQL JDBC | Data persistence | Direct connection |
| Caching | Redis | Performance optimization | Redis protocol |
| Email | Spring Mail | Notifications | SMTP |
| Documentation | Knife4j | API documentation | `/doc.html` |

The architecture supports both synchronous REST API interactions and asynchronous processing through the caching layer, enabling scalable warehouse management operations.

Sources: [pom.xml L39-L40](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/pom.xml#L39-L40)

 [pom.xml L35-L36](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/pom.xml#L35-L36)

 [pom.xml L129-L131](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/pom.xml#L129-L131)

 [pom.xml L115-L119](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/pom.xml#L115-L119)