import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Main server application for Library Management System
 * Starts the server and accepts client connections
 */
public class LibraryServer {
    
    private static final int PORT = 2004;
    
    public static void main(String[] args) {
        
        System.out.println("===========================================");
        System.out.println("   Library Management Server Starting     ");
        System.out.println("===========================================");
        
        // Load existing data from files
        System.out.println("\nLoading data from files...");
        UserStore.loadUsers();
        RecordStore.loadRecords();
        
        System.out.println("- Users loaded: " + UserStore.getUserCount());
        System.out.println("- Records loaded: " + RecordStore.getRecordCount());
        
        // Start server
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            
            System.out.println("\n✓ Server started successfully on port " + PORT);
            System.out.println("Waiting for client connections...\n");
            
            // Accept client connections indefinitely
            while (true) {
                try {
                    // Wait for a client to connect
                    Socket clientSocket = serverSocket.accept();
                    
                    // Create a new thread to handle this client
                    ServerThread clientThread = new ServerThread(clientSocket);
                    clientThread.start();
                    
                    System.out.println("Active connections: " + Thread.activeCount());
                    
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
