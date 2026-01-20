# MyOwnBranch

## MCP Server with H2 Database - Spring Boot Application

A Maven-based Spring Boot application providing MCP (Model Context Protocol) server integration with Atlassian/Jira and H2 in-memory database support.

### Project Overview

**SCRUM-1 Implementation:** Add table row in H2

This project demonstrates:
- Spring Boot 3.2.1 with Spring Data JPA
- H2 in-memory database with automatic schema initialization
- RESTful API for User and Task management
- Atlassian/Jira integration via MCP
- Complete CRUD operations with proper entity relationships

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- Git

### Project Structure

```
src/
├── main/
│   ├── java/com/mcp/
│   │   ├── McpServerApplication.java        # Main Spring Boot application
│   │   ├── controller/
│   │   │   ├── UserController.java          # REST API for Users
│   │   │   └── TaskController.java          # REST API for Tasks
│   │   ├── entity/
│   │   │   ├── User.java                    # User JPA entity
│   │   │   └── Task.java                    # Task JPA entity
│   │   └── repository/
│   │       ├── UserRepository.java          # User data access
│   │       └── TaskRepository.java          # Task data access
│   └── resources/
│       ├── application.properties           # Spring Boot configuration
│       ├── schema.sql                       # Database schema
│       └── data.sql                         # Initial sample data
pom.xml                                      # Maven project configuration
.env.example                                 # Environment variables template
mcp-config.json                              # MCP server configuration
```

### Setup Instructions

#### 1. Clone and Build

```bash
# Navigate to project directory
cd MyOwnBranch

# Build the project
mvn clean install
```

#### 2. Configure Environment Variables

```bash
# Copy the example file
cp .env.example .env

# Edit .env with your Jira credentials
JIRA_URL=https://raghupardhu.atlassian.net
JIRA_EMAIL=your-email@example.com
JIRA_PAT=your-personal-access-token
```

#### 3. Run the Application

```bash
# Start the Spring Boot application
mvn spring-boot:run
```

The application will start on `http://localhost:8080/mcp`

### API Endpoints

#### User API
- `GET /mcp/api/users` - Get all users
- `GET /mcp/api/users/{id}` - Get user by ID
- `POST /mcp/api/users` - Create new user
- `PUT /mcp/api/users/{id}` - Update user
- `DELETE /mcp/api/users/{id}` - Delete user

#### Task API
- `GET /mcp/api/tasks` - Get all tasks
- `GET /mcp/api/tasks/{id}` - Get task by ID
- `POST /mcp/api/tasks` - Create new task
- `PUT /mcp/api/tasks/{id}` - Update task
- `DELETE /mcp/api/tasks/{id}` - Delete task
- `GET /mcp/api/tasks/status/{status}` - Get tasks by status

### Database

#### H2 Console
Access the H2 database console at: **http://localhost:8080/mcp/h2-console**

**Connection Details:**
- Driver: `org.h2.Driver`
- JDBC URL: `jdbc:h2:mem:mcpdb`
- Username: `sa`
- Password: (leave blank)

#### Database Schema

**Users Table:**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**Tasks Table:**
```sql
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    assigned_to BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (assigned_to) REFERENCES users(id)
);
```

### Sample Data

The application automatically initializes with sample data:

**Users:**
1. admin (admin@example.com)
2. jdoe (john.doe@example.com)
3. rkonala (raghu.pardhu@gmail.com)

**Tasks:**
1. Setup MCP Server - COMPLETED (assigned to admin)
2. Add H2 Database Support - IN_PROGRESS (assigned to rkonala)
3. Test Jira Integration - PENDING (assigned to jdoe)

### MCP Configuration

The `mcp-config.json` file configures the MCP server for Atlassian integration:

```json
{
  "mcpServers": {
    "atlassian": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-atlassian"],
      "env": {
        "JIRA_URL": "${JIRA_URL}",
        "JIRA_EMAIL": "${JIRA_EMAIL}",
        "JIRA_PAT": "${JIRA_PAT}"
      }
    }
  }
}
```

### Features

✅ Spring Boot 3.2.1 with Spring Data JPA
✅ H2 in-memory database with automatic initialization
✅ JPA entities with lifecycle management
✅ RESTful CRUD APIs
✅ H2 Database Console access
✅ Atlassian/Jira MCP integration
✅ Lombok for reduced boilerplate code
✅ Proper logging configuration

### Security Notes

⚠️ **Never commit the following to version control:**
- `.env` file with actual credentials (included in `.gitignore`)
- API tokens or personal access tokens

The `.env` file contains sensitive information and should only exist locally.

### Building for Production

```bash
# Create a JAR file
mvn clean package

# Run the JAR
java -jar target/mcp-server-1.0.0.jar
```

### Troubleshooting

**Port already in use:**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

**H2 Console not accessible:**
- Ensure the application is running on the correct port
- Check that `spring.h2.console.enabled=true` in `application.properties`

**Database not initializing:**
- Verify `schema.sql` and `data.sql` are in `src/main/resources/`
- Check application logs for initialization errors

### Related Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [H2 Database](http://www.h2database.com)
- [Atlassian MCP Server](https://github.com/modelcontextprotocol/servers/tree/main/src/atlassian)

### Related Jira Issue

- **SCRUM-1:** Add table row in H2

### License

This project is part of the MyOwnBranch MCP implementation.
