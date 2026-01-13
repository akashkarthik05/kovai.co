package com.document360;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;
import org.json.JSONArray;


public class DriveFolderManager {

    private static final String BASE_URL = "https://apihub.document360.io/v2/Drive/Folders";
    private static String apiToken;
    private static String lastCreatedFolderId;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Document360 Drive Folder Manager");
        System.out.print("Enter your API token: ");
        apiToken = scanner.nextLine().trim();

        if (apiToken.isEmpty()) {
            System.err.println("API token is required!");
            return;
        }

        while (true) {
            System.out.println("\n--- Menu ---");
            System.out.println("1. GET - Fetch all drive folders");
            System.out.println("2. POST - Create a new folder");
            System.out.println("3. PUT - Update folder name");
            System.out.println("4. DELETE - Remove folder");
            System.out.println("5. Exit");
            System.out.print("Choose an operation (1-5): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    getAllFolders();
                    break;
                case "2":
                    createFolder(scanner);
                    break;
                case "3":
                    updateFolder(scanner);
                    break;
                case "4":
                    deleteFolder(scanner);
                    break;
                case "5":
                    System.out.println("Exiting application...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }


    public static void getAllFolders() {
        System.out.println("\n=== GET All Drive Folders ===");

        try {
            URL url = new URL(BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("api_token", apiToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            System.out.println("Request URL: " + url);
            System.out.println("Request Method: " + connection.getRequestMethod());
            System.out.println("Request Headers:");
            System.out.println("  api_token: " + apiToken);
            System.out.println("  Content-Type: application/json");

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            String responseBody = getResponseBody(connection);
            System.out.println("Response Body: " + responseBody);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Successfully retrieved all folders");
                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    if (jsonResponse.has("data")) {
                        JSONArray folders = jsonResponse.getJSONArray("data");
                        System.out.println("\nFound " + folders.length() + " folders:");
                        for (int i = 0; i < folders.length(); i++) {
                            JSONObject folder = folders.getJSONObject(i);
                            System.out.println("  - ID: " + folder.optString("id", "N/A") +
                                    ", Name: " + folder.optString("name", "N/A") +
                                    ", Parent ID: " + folder.optString("parentId", "Root"));
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Note: Could not parse JSON response structure");
                }
            } else {
                System.err.println("Failed to retrieve folders");
                handleError(responseCode, responseBody);
            }

            connection.disconnect();

        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
        }
    }


    public static void createFolder(Scanner scanner) {
        System.out.println("\n=== POST Create New Folder ===");

        System.out.print("Enter folder name: ");
        String folderName = scanner.nextLine().trim().replaceAll("^\"|\"$", "");

        if (folderName.isEmpty()) {
            System.out.println("Folder name cannot be empty!");
            return;
        }

        System.out.print("Enter parent folder ID (leave empty for root level): ");
        String parentId = scanner.nextLine().trim().replaceAll("^\"|\"$", "");

        System.out.print("Enter user ID: ");
        String userId = scanner.nextLine().trim().replaceAll("^\"|\"$", "");
        if (userId.isEmpty()) {
            System.out.println("User ID is required!");
            return;
        }

        try {
            URL url = new URL(BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("api_token", apiToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            JSONObject requestBody = new JSONObject();
            requestBody.put("title", folderName);
            requestBody.put("user_id", userId);
            if (!parentId.isEmpty()) {
                requestBody.put("parentId", parentId);
            }

            String jsonBody = requestBody.toString();

            System.out.println("Request URL: " + url);
            System.out.println("Request Method: " + connection.getRequestMethod());
            System.out.println("Request Headers:");
            System.out.println("  api_token: " + apiToken);
            System.out.println("  Content-Type: application/json");
            System.out.println("Request Body: " + jsonBody);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            String responseBody = getResponseBody(connection);
            System.out.println("Response Body: " + responseBody);

            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Successfully created folder: " + folderName);

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    if (jsonResponse.has("data")) {
                        JSONObject folderData = jsonResponse.getJSONObject("data");
                        lastCreatedFolderId = folderData.optString("media_folder_id");
                        System.out.println("New folder ID: " + lastCreatedFolderId);
                    }
                } catch (Exception e) {
                    System.out.println("Note: Could not extract folder ID from response");
                }
            } else {
                System.err.println("Failed to create folder");
                handleError(responseCode, responseBody);
            }

            connection.disconnect();

        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
        }
    }

    public static void updateFolder(Scanner scanner) {
        System.out.println("\n=== PUT Update Folder Name ===");

        System.out.print("Enter folder ID to update (or press Enter to use last created folder): ");
        String folderId = scanner.nextLine().trim();

        if (folderId.isEmpty()) {
            if (lastCreatedFolderId != null && !lastCreatedFolderId.isEmpty()) {
                folderId = lastCreatedFolderId;
                System.out.println("Using last created folder ID: " + folderId);
            } else {
                System.out.println("No folder ID available. Please create a folder first or enter a folder ID.");
                return;
            }
        }

        System.out.print("Enter new folder name: ");
        String newName = scanner.nextLine().trim();

        if (newName.isEmpty()) {
            System.out.println("Folder name cannot be empty!");
            return;
        }

        System.out.print("Enter user ID: ");
        String userId = scanner.nextLine().trim().replaceAll("^\"|\"$", "");
        if (userId.isEmpty()) {
            System.out.println("User ID is required!");
            return;
        }

        try {
            URL url = new URL(BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set up request
            connection.setRequestMethod("POST");
            connection.setRequestProperty("api_token", apiToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // Create request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("action", "update");
            requestBody.put("folderId", folderId);
            requestBody.put("title", newName);
            requestBody.put("user_id", userId);

            String jsonBody = requestBody.toString();

            // Log request details
            System.out.println("Request URL: " + url);
            System.out.println("Request Method: " + connection.getRequestMethod());
            System.out.println("Request Headers:");
            System.out.println("  api_token: " + apiToken);
            System.out.println("  Content-Type: application/json");
            System.out.println("Request Body: " + jsonBody);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            String responseBody = getResponseBody(connection);
            System.out.println("Response Body: " + responseBody);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Successfully updated folder name to: " + newName);
                System.out.println("Folder ID: " + folderId);
            } else {
                System.err.println("Failed to update folder");
                handleError(responseCode, responseBody);
            }

            connection.disconnect();

        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
        }
    }

    public static void deleteFolder(Scanner scanner) {
        System.out.println("\n=== DELETE Remove Folder ===");

        System.out.print("Enter folder ID to delete (or press Enter to use last created folder): ");
        String folderId = scanner.nextLine().trim();

        if (folderId.isEmpty()) {
            if (lastCreatedFolderId != null && !lastCreatedFolderId.isEmpty()) {
                folderId = lastCreatedFolderId;
                System.out.println("Using last created folder ID: " + folderId);
            } else {
                System.out.println("No folder ID available. Please create a folder first or enter a folder ID.");
                return;
            }
        }

        System.out.print("Enter user ID: ");
        String userId = scanner.nextLine().trim().replaceAll("^\"|\"$", "");
        if (userId.isEmpty()) {
            System.out.println("User ID is required!");
            return;
        }

        System.out.print("Are you sure you want to delete folder " + folderId + "? (y/N): ");
        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("y") && !confirmation.equalsIgnoreCase("yes")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        try {
            URL url = new URL(BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("api_token", apiToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            JSONObject requestBody = new JSONObject();
            requestBody.put("action", "delete");
            requestBody.put("folderId", folderId);
            requestBody.put("user_id", userId);

            String jsonBody = requestBody.toString();

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            System.out.println("Request URL: " + url);
            System.out.println("Request Method: " + connection.getRequestMethod());
            System.out.println("Request Headers:");
            System.out.println("  api_token: " + apiToken);
            System.out.println("  Content-Type: application/json");
            System.out.println("Request Body: " + jsonBody);

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            String responseBody = getResponseBody(connection);
            System.out.println("Response Body: " + responseBody);

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                System.out.println("Successfully deleted folder");
                System.out.println("Deleted folder ID: " + folderId);

                // Clear stored folder ID if it matches the deleted one
                if (folderId.equals(lastCreatedFolderId)) {
                    lastCreatedFolderId = null;
                }
            } else {
                System.err.println("Failed to delete folder");
                handleError(responseCode, responseBody);
            }

            connection.disconnect();

        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
        }
    }


    private static String getResponseBody(HttpURLConnection connection) throws IOException {
        BufferedReader reader;
        if (connection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                reader = new BufferedReader(new InputStreamReader(errorStream));
            } else {
                return "";
            }
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return response.toString();
    }


    private static void handleError(int responseCode, String responseBody) {
        System.err.println("Error Details:");
        System.err.println("  Status Code: " + responseCode);

        String statusMessage;
        switch (responseCode) {
            case 400:
                statusMessage = "Bad Request - Invalid parameters";
                break;
            case 401:
                statusMessage = "Unauthorized - Invalid API token";
                break;
            case 403:
                statusMessage = "Forbidden - Insufficient permissions";
                break;
            case 404:
                statusMessage = "Not Found - Folder does not exist";
                break;
            case 409:
                statusMessage = "Conflict - Folder with same name exists";
                break;
            case 422:
                statusMessage = "Unprocessable Entity - Validation failed";
                break;
            case 429:
                statusMessage = "Too Many Requests - Rate limit exceeded";
                break;
            case 500:
                statusMessage = "Internal Server Error";
                break;
            case 502:
                statusMessage = "Bad Gateway";
                break;
            case 503:
                statusMessage = "Service Unavailable";
                break;
            default:
                statusMessage = "Unknown error";
                break;
        }

        System.err.println("  Status Message: " + statusMessage);

        if (responseBody != null && !responseBody.isEmpty()) {
            try {
                JSONObject errorJson = new JSONObject(responseBody);
                if (errorJson.has("message")) {
                    System.err.println("  Error Message: " + errorJson.getString("message"));
                }
                if (errorJson.has("error")) {
                    System.err.println("  Error Details: " + errorJson.getString("error"));
                }
            } catch (Exception e) {
                System.err.println("  Raw Response: " + responseBody);
            }
        }
    }
}
