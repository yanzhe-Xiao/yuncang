# Development Environment

> **Relevant source files**
> * [.gitattributes](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/.gitattributes)
> * [.gitignore](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/.gitignore)
> * [package-lock.json](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/package-lock.json)
> * [pom.xml](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/pom.xml)

This document provides comprehensive guidance for setting up a local development environment for the yuncang warehouse management system. It covers prerequisites, tooling, IDE configuration, and development workflows necessary for contributors to build, test, and modify the codebase effectively.

For information about deploying the complete system using Docker, see [Docker Deployment](/yanzhe-Xiao/yuncang/2.1-docker-deployment). For understanding the overall system architecture and design patterns, see [System Architecture](/yanzhe-Xiao/yuncang/3-system-architecture).

## Prerequisites

### Java Development Kit

The yuncang system requires **Java 21** as specified in the Maven configuration. This is a modern LTS version that provides enhanced performance and language features.

| Component | Version | Purpose |
| --- | --- | --- |
| Java JDK | 21 | Runtime and compilation target |
| Maven | 3.6+ | Build automation and dependency management |
| Git | 2.20+ | Version control system |

**Sources:** [pom.xml L30](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/pom.xml#L30-L30)

### Development Tools

The project supports multiple IDEs and development environments as evidenced by the comprehensive `.gitignore` configuration:

```mermaid
flowchart TD

GIT["Git Repository"]
GITIGNORE[".gitignore"]
GITATTRIBUTES[".gitattributes<br>(LFS for binaries)"]
IDEA["IntelliJ IDEA<br>(.idea, *.iml)"]
ECLIPSE["Eclipse/STS<br>(.classpath, .project)"]
NETBEANS["NetBeans<br>(/nbproject/)"]
VSCODE["VS Code<br>(.vscode/)"]
MAVEN["Maven<br>(pom.xml)"]
TARGET["target/<br>(build output)"]

IDEA --> MAVEN
ECLIPSE --> MAVEN
NETBEANS --> MAVEN
VSCODE --> MAVEN

subgraph subGraph1 ["Build System"]
    MAVEN
    TARGET
    MAVEN --> TARGET
end

subgraph subGraph0 ["Supported IDEs"]
    IDEA
    ECLIPSE
    NETBEANS
    VSCODE
end

subgraph subGraph2 ["Version Control"]
    GIT
    GITIGNORE
    GITATTRIBUTES
    GIT --> GITIGNORE
    GIT --> GITATTRIBUTES
end
```

**Sources:** [.gitignore L16-L33](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/.gitignore#L16-L33)

 [.gitattributes L1-L4](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/.gitattributes#L1-L4)

## Core Dependencies and Framework Stack

### Spring Boot Framework

The application is built on **Spring Boot 3.4.4**, providing a modern, production-ready foundation with extensive auto-configuration capabilities.

```mermaid
flowchart TD

KNIFE4J["knife4j-openapi3 v4.1.0<br>(API documentation)"]
SPRING_TEST["spring-boot-starter-test<br>(testing framework)"]
JWT["java-jwt v4.3.0<br>(authentication tokens)"]
FASTJSON["fastjson2 v2.0.51<br>(JSON processing)"]
JACKSON["jackson-databind v2.18.3<br>(JSON serialization)"]
LOMBOK["lombok v1.18.30<br>(code generation)"]
MYBATIS_PLUS["mybatis-plus-spring-boot3-starter<br>v3.5.12 (ORM)"]
MYSQL_CONNECTOR["mysql-connector-j<br>(database driver)"]
COMMONS_POOL["commons-pool2<br>(connection pooling)"]
STARTER_WEB["spring-boot-starter-web<br>(REST controllers)"]
STARTER_REDIS["spring-boot-starter-data-redis<br>(caching/sessions)"]
STARTER_SECURITY["spring-boot-starter-security<br>(authentication)"]
STARTER_MAIL["spring-boot-starter-mail<br>(email notifications)"]
DEVTOOLS["spring-boot-devtools<br>(hot reload)"]

subgraph subGraph3 ["Development Tools"]
    KNIFE4J
    SPRING_TEST
end

subgraph subGraph2 ["Security & Utilities"]
    JWT
    FASTJSON
    JACKSON
    LOMBOK
end

subgraph subGraph1 ["Data Layer"]
    MYBATIS_PLUS
    MYSQL_CONNECTOR
    COMMONS_POOL
end

subgraph subGraph0 ["Spring Boot 3.4.4 Core"]
    STARTER_WEB
    STARTER_REDIS
    STARTER_SECURITY
    STARTER_MAIL
    DEVTOOLS
end
```

**Sources:** [pom.xml L32-L132](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/pom.xml#L32-L132)

### Build Configuration

The Maven build process is configured with Spring Boot's plugin for packaging and development workflow optimization:

```mermaid
flowchart TD

SOURCE["Source Code<br>(src/main/java)"]
COMPILE["Maven Compile<br>(mvn compile)"]
TEST["Run Tests<br>(mvn test)"]
PACKAGE["Package JAR<br>(mvn package)"]
RUN["Spring Boot Run<br>(mvn spring-boot:run)"]
POM["pom.xml"]
PLUGIN["spring-boot-maven-plugin"]
EXCLUDE["Lombok exclusion"]

SOURCE --> COMPILE
COMPILE --> TEST
TEST --> PACKAGE
PACKAGE --> RUN
POM --> COMPILE
PLUGIN --> PACKAGE
EXCLUDE --> PACKAGE

subgraph subGraph0 ["Maven Configuration"]
    POM
    PLUGIN
    EXCLUDE
end
```

**Sources:** [pom.xml L135-L152](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/pom.xml#L135-L152)

## IDE Setup and Configuration

### IntelliJ IDEA Configuration

For IntelliJ IDEA development, the project excludes several generated files and directories from version control:

* `.idea/` - IDE settings and configuration
* `*.iws` - workspace files
* `*.iml` - module files
* `*.ipr` - project files

### Eclipse/STS Configuration

Eclipse and Spring Tool Suite users will find these artifacts excluded:

* `.apt_generated` - annotation processing output
* `.classpath` - Eclipse classpath configuration
* `.factorypath` - factory path settings
* `.project` - Eclipse project file
* `.settings/` - workspace settings
* `.springBeans` - Spring configuration
* `.sts4-cache` - STS cache files

### Development Workflow

```mermaid
flowchart TD

CHECKOUT["Git Checkout<br>(feature branch)"]
CODE["Code Changes<br>(Java/Properties)"]
HOTRELOAD["Hot Reload<br>(spring-boot-devtools)"]
TEST_LOCAL["Local Testing<br>(mvn test)"]
COMMIT["Git Commit"]
MVN_CLEAN["mvn clean"]
MVN_COMPILE["mvn compile"]
MVN_TEST["mvn test"]
MVN_PACKAGE["mvn package"]
DOCKER_DB["Docker MySQL/Redis<br>(development data)"]
INTEGRATION_TEST["Integration Tests"]
API_TEST["API Testing<br>(Knife4j/Swagger)"]

COMMIT --> MVN_CLEAN
MVN_PACKAGE --> DOCKER_DB

subgraph subGraph2 ["Integration Testing"]
    DOCKER_DB
    INTEGRATION_TEST
    API_TEST
    DOCKER_DB --> INTEGRATION_TEST
    INTEGRATION_TEST --> API_TEST
end

subgraph subGraph1 ["Build Verification"]
    MVN_CLEAN
    MVN_COMPILE
    MVN_TEST
    MVN_PACKAGE
    MVN_CLEAN --> MVN_COMPILE
    MVN_COMPILE --> MVN_TEST
    MVN_TEST --> MVN_PACKAGE
end

subgraph subGraph0 ["Local Development Cycle"]
    CHECKOUT
    CODE
    HOTRELOAD
    TEST_LOCAL
    COMMIT
    CHECKOUT --> CODE
    CODE --> HOTRELOAD
    HOTRELOAD --> TEST_LOCAL
    TEST_LOCAL --> COMMIT
end
```

**Sources:** [pom.xml L54-L57](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/pom.xml#L54-L57)

 [.gitignore L35-L39](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/.gitignore#L35-L39)

## Development Dependencies

### Runtime Dependencies

| Dependency | Version | Purpose | Scope |
| --- | --- | --- | --- |
| `spring-boot-devtools` | 3.4.4 | Hot reload, automatic restart | `runtime` |
| `mysql-connector-j` | Latest | MySQL database connectivity | `runtime` |
| `lombok` | 1.18.30 | Code generation annotations | `provided` |

### Test Dependencies

The project includes comprehensive testing support through Spring Boot's test starter, which provides:

* JUnit 5 testing framework
* Spring Test context support
* MockMvc for web layer testing
* TestContainers integration capabilities

**Sources:** [pom.xml L64-L73](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/pom.xml#L64-L73)

## Binary File Management

The project uses Git LFS (Large File Storage) for managing binary artifacts:

```mermaid
flowchart TD

JAR["*.jar files<br>(filter=lfs)"]
ZIP["*.zip files<br>(filter=lfs)"]
BINARY["Binary artifacts<br>(merge=binary)"]
GITATTR[".gitattributes"]
LFS["Git LFS Storage"]

GITATTR --> JAR
GITATTR --> ZIP
JAR --> LFS
ZIP --> LFS
BINARY --> LFS

subgraph subGraph1 ["Version Control"]
    GITATTR
    LFS
end

subgraph subGraph0 ["Git LFS Configuration"]
    JAR
    ZIP
    BINARY
end
```

**Sources:** [.gitattributes L2-L3](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/.gitattributes#L2-L3)

## Docker Development Integration

The development environment integrates with Docker for database and cache services, with runtime data excluded from version control:

* `docker/mysql/data/` - MySQL development data
* `docker/redis/data/` - Redis development data

This allows developers to maintain clean local databases while preserving development state between sessions.

**Sources:** [.gitignore L36-L39](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/.gitignore#L36-L39)

## API Development and Testing

The project includes Knife4j OpenAPI integration for interactive API documentation and testing during development. This provides:

* Swagger UI interface at `/doc.html`
* OpenAPI 3.0 specification generation
* Interactive API testing capabilities
* Request/response schema validation

**Sources:** [pom.xml L114-L119](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/pom.xml#L114-L119)