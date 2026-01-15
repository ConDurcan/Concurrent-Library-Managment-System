import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * ServerThread handles each client connection Each client gets their own thread
 * for concurrent access
 */
public class ServerThread extends Thread {

	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String message;
	private User currentUser;

	public ServerThread(Socket s) {
		socket = s;
	}

	@Override
	public void run() {
		try {
			System.out.println("Connection received from " + socket.getInetAddress().getHostName());

			// Setup streams
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(socket.getInputStream());

			// Main server loop - handle client requests
			boolean running = true;
			while (running) {
				try {
					// Read message from client
					message = (String) in.readObject();
					System.out.println("Received: " + message);

					// Parse command (format: COMMAND|param1|param2|...)
					String[] parts = message.split("\\|");
					String command = parts[0];

					// Route to appropriate handler
					switch (command) {
					case "REGISTER":
						handleRegistration(parts);
						break;

					case "LOGIN":
						handleLogin(parts);
						break;

					case "CREATE_RECORD":
						if (isAuthenticated()) {
							handleCreateRecord(parts);
						} else {
							sendMessage("ERROR|Not authenticated");
						}
						break;

					case "VIEW_ALL_RECORDS":
						if (isAuthenticated()) {
							handleViewAllRecords();
						} else {
							sendMessage("ERROR|Not authenticated");
						}
						break;

					case "ASSIGN_REQUEST":
						if (isAuthenticated() && currentUser.isLibrarian()) {
							handleAssignRequest(parts);
						} else {
							sendMessage("ERROR|Unauthorized - Librarian access required");
						}
						break;

					case "VIEW_MY_RECORDS":
						if (isAuthenticated()) {
							handleViewMyRecords(parts);
						} else {
							sendMessage("ERROR|Not authenticated");
						}
						break;

					case "UPDATE_PASSWORD":
						if (isAuthenticated()) {
							handleUpdatePassword(parts);
						} else {
							sendMessage("ERROR|Not authenticated");
						}
						break;

					case "LOGOUT":
						currentUser = null;
						sendMessage("SUCCESS|Logged out");
						break;

					case "EXIT":
						running = false;
						break;

					default:
						sendMessage("ERROR|Unknown command: " + command);
					}

				} catch (ClassNotFoundException e) {
					System.err.println("Invalid message format");
					break;
				}
			}

		} catch (IOException e) {
			System.err.println("Connection error: " + e.getMessage());
		} finally {
			cleanup();
		}
	}

	/**
	 * Handle user registration Format:
	 * REGISTER|name|studentId|email|password|department|role
	 */
	private void handleRegistration(String[] parts) {
		try {
			if (parts.length != 7) {
				sendMessage("ERROR|Invalid registration format");
				return;
			}

			String name = parts[1];
			String studentId = parts[2];
			String email = parts[3];
			String password = parts[4];
			String department = parts[5];
			String roleStr = parts[6];

			// Parse role
			User.Role role;
			try {
				role = User.Role.valueOf(roleStr.toUpperCase());
			} catch (IllegalArgumentException e) {
				sendMessage("ERROR|Invalid role");
				return;
			}

			// Create user
			User newUser = new User(name, studentId, email, password, department, role);

			// Add to UserStore
			boolean success = UserStore.addUser(newUser);

			if (success) {
				sendMessage("SUCCESS|Registration successful");
			} else {
				sendMessage("ERROR|Email or Student ID already exists");
			}

		} catch (Exception e) {
			sendMessage("ERROR|Registration failed: " + e.getMessage());
		}
	}

	/**
	 * Handle user login Format: LOGIN|email|password
	 */
	private void handleLogin(String[] parts) {
		try {
			if (parts.length != 3) {
				sendMessage("ERROR|Invalid login format");
				return;
			}

			String email = parts[1];
			String password = parts[2];

			// Validate credentials
			User user = UserStore.validateLogin(email, password);

			if (user != null) {
				currentUser = user;
				// Send User object back to client
				sendObject(user);
				System.out.println("User logged in: " + email);
			} else {
				sendMessage("ERROR|Invalid email or password");
			}

		} catch (Exception e) {
			sendMessage("ERROR|Login failed: " + e.getMessage());
		}
	}

	/**
	 * Handle creating a library record Format: CREATE_RECORD|recordType|studentId
	 */
	private void handleCreateRecord(String[] parts) {
		try {
			if (parts.length != 3) {
				sendMessage("ERROR|Invalid format");
				return;
			}

			String recordType = parts[1];
			String studentId = parts[2];

			// Create record through RecordStore
			LibraryRecord record = RecordStore.createRecord(recordType, studentId);

			if (record != null) {
				sendMessage("SUCCESS|Record created with ID: " + record.getRecordId());
			} else {
				sendMessage("ERROR|Failed to create record");
			}

		} catch (Exception e) {
			sendMessage("ERROR|" + e.getMessage());
		}
	}

	/**
	 * Handle viewing all library records Format: VIEW_ALL_RECORDS
	 */
	private void handleViewAllRecords() {
		try {
			StringBuilder response = new StringBuilder();
			response.append("\n=== All Library Records ===\n");

			var records = RecordStore.getAllRecords();

			if (records.isEmpty()) {
				response.append("No records found.\n");
			} else {
				for (LibraryRecord record : records.values()) {
					response.append(formatRecord(record)).append("\n");
				}
			}

			sendMessage(response.toString());

		} catch (Exception e) {
			sendMessage("ERROR|Failed to retrieve records: " + e.getMessage());
		}
	}

	/**
	 * Handle assigning a borrowing request to a librarian Format:
	 * ASSIGN_REQUEST|recordId|librarianId
	 */
	private void handleAssignRequest(String[] parts) {
		try {
			if (parts.length != 3) {
				sendMessage("ERROR|Invalid format");
				return;
			}

			String recordId = parts[1];
			String librarianId = parts[2];

			boolean success = RecordStore.assignLibrarian(recordId, librarianId);

			if (success) {
				sendMessage("SUCCESS|Request assigned successfully");
			} else {
				sendMessage("ERROR|Failed to assign request - Record not found or already assigned");
			}

		} catch (Exception e) {
			sendMessage("ERROR|" + e.getMessage());
		}
	}

	/**
	 * Handle viewing records assigned to current user Format:
	 * VIEW_MY_RECORDS|userId
	 */
	private void handleViewMyRecords(String[] parts) {
		try {
			if (parts.length != 2) {
				sendMessage("ERROR|Invalid format");
				return;
			}

			String userId = parts[1];
			StringBuilder response = new StringBuilder();
			response.append("\n=== My Assigned Records ===\n");

			var records = RecordStore.getRecordsByUser(userId, currentUser.getRole());

			if (records.isEmpty()) {
				response.append("No records assigned to you.\n");
			} else {
				for (LibraryRecord record : records) {
					response.append(formatRecord(record)).append("\n");
				}
			}

			sendMessage(response.toString());

		} catch (Exception e) {
			sendMessage("ERROR|Failed to retrieve records: " + e.getMessage());
		}
	}

	/**
	 * Handle password update Format: UPDATE_PASSWORD|email|oldPassword|newPassword
	 */
	private void handleUpdatePassword(String[] parts) {
		try {
			if (parts.length != 4) {
				sendMessage("ERROR|Invalid format");
				return;
			}

			String email = parts[1];
			String oldPassword = parts[2];
			String newPassword = parts[3];

			// Verify this is the current user
			if (!currentUser.getEmail().equals(email)) {
				sendMessage("ERROR|Unauthorized");
				return;
			}

			boolean success = UserStore.updatePassword(email, oldPassword, newPassword);

			if (success) {
				// Update current user's password
				currentUser.setPassword(newPassword);
				sendMessage("SUCCESS|Password updated successfully");
			} else {
				sendMessage("ERROR|Failed to update password - Check your current password");
			}

		} catch (Exception e) {
			sendMessage("ERROR|" + e.getMessage());
		}
	}

	/**
	 * Format a library record for display
	 */
	private String formatRecord(LibraryRecord record) {
		return String.format("ID: %s | Type: %s | Date: %s | Student: %s | Status: %s | Librarian: %s",
				record.getRecordId(), record.getRecordType(), record.getDate(), record.getStudentId(),
				record.getStatus(),
				record.getAssignedLibrarianId() != null ? record.getAssignedLibrarianId() : "Unassigned");
	}

	/**
	 * Check if user is authenticated
	 */
	private boolean isAuthenticated() {
		return currentUser != null;
	}

	/**
	 * Send a string message to the client
	 */
	void sendMessage(String msg) {
		try {
			out.writeObject(msg);
			out.flush();
			System.out.println("Sent: " + msg);
		} catch (IOException e) {
			System.err.println("Error sending message: " + e.getMessage());
		}
	}

	/**
	 * Send an object to the client (e.g., User object after login)
	 */
	void sendObject(Object obj) {
		try {
			out.writeObject(obj);
			out.flush();
			System.out.println("Sent object: " + obj.getClass().getSimpleName());
		} catch (IOException e) {
			System.err.println("Error sending object: " + e.getMessage());
		}
	}

	/**
	 * Cleanup resources
	 */
	private void cleanup() {
		try {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			if (socket != null)
				socket.close();
			System.out.println("Connection closed from " + socket.getInetAddress().getHostName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}