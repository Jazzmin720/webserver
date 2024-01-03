import java.io.*;
import java.net.*;
import java.util.Scanner;

public class WebServer {

    private static final int LISTENING_PORT = 8080;
    private final static String ROOT_DIRECTORY = "myserver";

    public static void main(String[] args) {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(LISTENING_PORT);
            System.out.println("Listening on port " + LISTENING_PORT);
            while (true) {
                Socket connection = serverSocket.accept();
                System.out.println("\nConnection from " + connection.getRemoteSocketAddress());
                ConnectionThread thread = new ConnectionThread(connection);
                thread.start();
            }
        } catch (Exception e) {
            System.out.println("Server socket shut down unexpectedly!");
            System.out.println("Error: " + e);
            System.out.println("Exiting.");
        }
    }

    private static void handleConnection(Socket connection) {
        try {
            InputStream socketIn = connection.getInputStream();
            OutputStream socketOut = connection.getOutputStream();
            try (Scanner scanner = new Scanner(socketIn)) {
                // Parse the request and extract file path
                String requestLine = scanner.nextLine();
                String[] requestParts = requestLine.split(" ");
                if (requestParts.length < 3 || !requestParts[0].equals("GET")) {
                    sendErrorResponse(400, socketOut);
                    return;
                }
                String requestedFilePath = requestParts[1];

                // Locate the requested file
                File file = new File(ROOT_DIRECTORY, requestedFilePath);
               

            if (file.exists() && file.canRead() && !file.isDirectory()) {
                long fileLength = file.length();
                // Send HTTP response headers and status line
                PrintWriter writer = new PrintWriter(socketOut, true);
                writer.print("HTTP/1.1 200 OK\r\n");
                writer.print("Content-Length: " + fileLength + "\r\n");
                writer.print("Content-Type: " + getMimeType(file.getName()) + "\r\n");
                writer.print("\r\n");
                writer.flush();

                // Send file content
                sendFile(file, socketOut);
            } else {
                // Send appropriate error response (404, 403, etc.)
                sendErrorResponse(404, socketOut);
            }
        } catch (Exception e) {
            // Handle exceptions
        } finally {
            try {
                connection.close();
            } catch (IOException e) {
                // Handle closing exception
            }
        }
    }

    private static void sendFile(File file, OutputStream socketOut) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file));
                OutputStream out = new BufferedOutputStream(socketOut)) {
            int x;
            while ((x = in.read()) != -1) { // read one byte from file
                out.write(x); // write the byte to the socket
            }
            out.flush();
        } catch (IOException e) {
            // Handle exceptions
        }
    }

    private static String getMimeType(String fileName) {
        int pos = fileName.lastIndexOf('.');
        if (pos < 0) { // no file extension in name
            return "x-application/x-unknown";
        }
        String ext = fileName.substring(pos + 1).toLowerCase();

        switch (ext) {
            case "txt":
                return "text/plain";
            case "html":
            case "htm":
                return "text/html";
            case "css":
                return "text/css";
            case "js":
                return "text/javascript";
            case "java":
                return "text/x-java";
            case "jpeg":
            case "jpg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "ico":
                return "image/x-icon";
            case "class":
                return "application/java-vm";
            case "jar":
                return "application/java-archive";
            case "zip":
                return "application/zip";
            case "xml":
                return "application/xml";
            case "xhtml":
                return "application/xhtml+xml";
            default:
                return "x-application/x-unknown";
        }
    }

    private static void sendErrorResponse(int errorCode, OutputStream socketOut) {
        PrintWriter writer = new PrintWriter(socketOut, true);
        String statusLine;
        String errorMessage;

        switch (errorCode) {
            case 404:
                statusLine = "HTTP/1.1 404 Not Found";
                errorMessage = "<h2>Error: 404 Not Found</h2><p>The resource you requested does not exist on this server.</p>";
                break;
            case 403:
                statusLine = "HTTP/1.1 403 Forbidden";
                errorMessage = "<h2>Error: 403 Forbidden</h2><p>You do not have permission to access the requested resource.</p>";
                break;
            case 400:
                statusLine = "HTTP/1.1 400 Bad Request";
                errorMessage = "<h2>Error: 400 Bad Request</h2><p>Your browser sent a request that this server could not understand.</p>";
                break;
            case 501:
                statusLine = "HTTP/1.1 501 Not Implemented";
                errorMessage = "<h2>Error: 501 Not Implemented</h2><p>The requested method is not supported.</p>";
                break;
            case 500:
            default:
                statusLine = "HTTP/1.1 500 Internal Server Error";
                errorMessage = "<h2>Error: 500 Internal Server Error</h2><p>The server encountered an unexpected condition.</p>";
                break;
        }

        try {
            writer.print(statusLine + "\r\n");
            writer.print("Connection: close\r\n");
            writer.print("Content-Type: text/html\r\n");
            writer.print("\r\n");
            writer.print("<html><head><title>Error</title></head><body>");
            writer.print(errorMessage);
            writer.print("</body></html>");
            writer.flush();
        } catch (Exception e) {
            // If an exception occurs, there's not much we can do, so we just ignore it.
        }
    }

    private static class ConnectionThread extends Thread {
        Socket connection;

        ConnectionThread(Socket connection) {
            this.connection = connection;
        }

        public void run() {
            handleConnection(connection);
        }
    }

}
