# MyOwnBranch

## MCP Server Setup - Atlassian/Jira Integration

This repository contains an MCP (Model Context Protocol) server configuration for integrating with Atlassian/Jira.

### Prerequisites

- Node.js installed on your system
- A Jira/Atlassian account
- A Jira Personal Access Token (PAT)

### Setup Instructions

1. **Create your environment file:**
   ```powershell
   Copy-Item .env.example .env
   ```

2. **Configure your Jira credentials in `.env`:**
   - `JIRA_URL`: Your Atlassian instance URL (e.g., `https://your-domain.atlassian.net`)
   - `JIRA_EMAIL`: Your Atlassian account email
   - `JIRA_PAT`: Your Personal Access Token

3. **Generate a Jira Personal Access Token:**
   - Visit: https://id.atlassian.com/manage-profile/security/api-tokens
   - Click "Create API token"
   - Copy the token and add it to your `.env` file

4. **Load environment variables (PowerShell):**
   ```powershell
   Get-Content .env | ForEach-Object {
       if ($_ -match '^([^=]+)=(.*)$') {
           [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
       }
   }
   ```

### Usage

The MCP server configuration is in `mcp-config.json`. This can be used with MCP-compatible clients to interact with Jira.

### Features

With this MCP server, you can:
- Search and retrieve Jira issues
- Create and update issues
- Add comments and worklogs
- Query project information
- And more Atlassian/Jira operations

### Security Note

⚠️ Never commit your `.env` file to version control. It's already added to `.gitignore`.