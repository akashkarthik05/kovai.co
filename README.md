# Document360 Drive Folder API Client

A Java console application that performs CRUD operations on Document360 Drive Folders API.

## Features

- **GET**: Fetch all drive folders
- **POST**: Create a new drive folder
- **PUT**: Update folder name
- **DELETE**: Remove folder
- **Error Handling**: Comprehensive error handling with status code validation
- **Dynamic Folder ID Management**: Stores and reuses folder IDs across operations
- **Request/Response Logging**: Full logging of API requests and responses

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Setup

1. Clone or download this project
2. Navigate to the project directory
3. Build the project:
   ```bash
   mvn clean compile
   ```

## Usage

### Run the Application

```bash
mvn exec:java -Dexec.mainClass="com.document360.DriveFolderManager"
```

### API Token

You'll need a valid Document360 API token. When prompted, enter your API token to authenticate with the API.

### Menu Options

1. **GET - Fetch all drive folders**: Retrieves all folders from your Document360 drive
2. **POST - Create a new folder**: Creates a new folder (can be nested or root level)
3. **PUT - Update folder name**: Renames an existing folder
4. **DELETE - Remove folder**: Deletes a folder (with confirmation)
5. **Exit**: Closes the application

## API Endpoints

- **GET**: `https://apihub.document360.io/v2/Drive/Folders`
- **POST**: `https://apihub.document360.io/v2/Drive/Folders`
- **PUT**: `https://apihub.document360.io/v2/Drive/Folders/{folderId}`
- **DELETE**: `https://apihub.document360.io/v2/Drive/Folders/{folderId}`

## Request Headers

All requests include:
- `api_token: <your_api_token>`
- `Content-Type: application/json`

## Error Handling

The application handles various HTTP status codes:
- 400: Bad Request
- 401: Unauthorized
- 403: Forbidden
- 404: Not Found
- 409: Conflict
- 422: Unprocessable Entity
- 429: Too Many Requests
- 500: Internal Server Error

## Features Implemented

✅ **Modular Design**: Separate methods for each CRUD operation
✅ **Dynamic Folder ID**: Automatically stores and reuses folder IDs
✅ **Comprehensive Logging**: Logs request URL, headers, and body
✅ **Error Handling**: Validates HTTP status codes and response structure
✅ **User-Friendly Interface**: Interactive console menu
✅ **Input Validation**: Validates user inputs before making API calls
✅ **Confirmation Prompts**: Safety confirmation for delete operations

## Project Structure

```
src/
└── main/
    └── java/
        └── com/
            └── document360/
                └── DriveFolderManager.java
pom.xml
README.md
```

## Dependencies

- `org.json:json:20231013` - For JSON parsing and manipulation

## Example Workflow

1. Run the application
2. Enter your API token when prompted
3. Choose option 1 to view existing folders
4. Choose option 2 to create a new folder
5. Choose option 3 to update the folder name (uses stored ID)
6. Choose option 4 to delete the folder (uses stored ID)
7. Choose option 5 to exit

## Notes

- The application uses Java's built-in `HttpURLConnection` for API calls
- No third-party HTTP libraries are used (as per constraints)
- Folder IDs are dynamically stored and reused across operations
- All requests and responses are logged for debugging purposes
- The application includes timeout handling (10 seconds for connect and read)
