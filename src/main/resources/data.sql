-- H2 Database Initial Data
-- SCRUM-1: Add table row in H2

-- Insert sample users
INSERT INTO users (username, email) VALUES ('admin', 'admin@example.com');
INSERT INTO users (username, email) VALUES ('jdoe', 'john.doe@example.com');
INSERT INTO users (username, email) VALUES ('rkonala', 'raghu.pardhu@gmail.com');

-- Insert sample tasks (adding rows as per SCRUM-1)
INSERT INTO tasks (title, description, status, assigned_to) 
VALUES ('Setup MCP Server', 'Configure MCP server for Atlassian integration', 'COMPLETED', 1);

INSERT INTO tasks (title, description, status, assigned_to) 
VALUES ('Add H2 Database Support', 'Implement H2 database with sample tables', 'IN_PROGRESS', 3);

INSERT INTO tasks (title, description, status, assigned_to) 
VALUES ('Test Jira Integration', 'Verify Jira API integration works correctly', 'PENDING', 2);
