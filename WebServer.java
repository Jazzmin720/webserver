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
                Thread thread = new Thread(() -> handleConnection(connection));
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
            Scanner scanner = new Scanner(socketIn);

            // Parse the request and extract file path
            String requestLine = scanner.nextLine();
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 3 || !requestParts[0].equals("GET")) {
                // Send a 400 Bad Request error response
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
                out.write(x);  // write the byte to the socket
            }
            out.flush();
        } catch (IOException e) {
            // Handle exceptions
        }
        }

    private static String getMimeType(String fileName) {
    int pos = fi

    eName.lastIndexOf('.');
    if (pos < 0) {  // no file extension in name
        return "x-application/x-unknown";
    }
    String ext = fileName.substring(pos + 1).toLowerCase();
    if (ext.equals("txt")) return "text/plain";
    else if (ext.equals("html")) return "text/html";
    else if (ext.equals("htm")) return "text/html";
    else if (ext.equals("css")) return "text/css";
    else if (ext.equals("js")) return "text/javascript";
    else if (ext.equals("java")) return "text/x-java";
    else if (ext.equals("jpeg")) return "image/jpeg";
    else if (ext.equals("jpg")) return "image/jpeg";
    else if (ext.equals("png")) return "image/png";
    else if (ext.equals("gif")) return "image/gif";
    else if (ext.equals("ico")) return "image/x-icon";
    else if (ext.equals("class")) return "application/java-vm";
    else if (ext.equals("jar")) return "application/java-archive";
    else if (ext.equals("zip")) return "application/zip";
    else if (ext.equals("xml")) return "application/xml";
    else if (ext.equals("xhtml")) return "application/xhtml+xml";
    else return "x-application/x-unknown";
    // Note: x-application/x-unknown is something made up;
    // it will probably make the browser offer to save the file.
}
