
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Requester {
	Socket requestSocket;
	ObjectOutputStream out;
	ObjectInputStream in;
	String message;
	Scanner input;
	Boolean running;
	private User currentUser;

	Requester() {

		input = new Scanner(System.in);
	}

	void run() {
		try {
			// 1. creating a socket to connect to the server

			requestSocket = new Socket("127.0.0.1", 2004);
			System.out.println("Connected to localhost in port 2004");
			// 2. get Input and Output streams
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());

			running = true;
			while (running) {
				if (currentUser == null) {
					// Not logged in - show login/register menu
					running = showLoginMenu();
				} else {
					// Logged in - show main menu
					running = showMainMenu();
				}
			}

			/// Client Conversation......
		} catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			// 4: Closing connection
			try {
				in.close();
				out.close();
				requestSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	private boolean showLoginMenu() {
		System.out.println("\n=== Library Management System ===");
		System.out.println("1. Register");
		System.out.println("2. Login");
		System.out.println("3. Exit");
		System.out.print("Choose an option: ");

		try {
			int choice = input.nextInt();
			input.nextLine();

			switch (choice) {
			case 1:
				handleRegistration();
				break;
			case 2:
				handleLogin();
				break;
			case 3:
				System.out.println("Goodbye!");
				return false;
			default:
				System.out.println("Invalid option. Please try again.");
			}
		} catch (Exception e) {
			System.out.println("Invalid input. Please enter a number.");
			input.nextLine();
		}

		return true;
	}

	private boolean showMainMenu() {
		System.out.println("\n=== Welcome, " + currentUser.getName() + " ===");
		System.out.println("1. Create Library Record");
		System.out.println("2. View All Book Records");

		if (currentUser.isLibrarian()) {
			System.out.println("3. Assign Borrowing Request");
			System.out.println("4. View My Assigned Records");
		}
		System.out.println("5. Update Password");
		System.out.println("6. Logout");
		System.out.print("Choose an option: ");

		try {
			int choice = input.nextInt();
			input.nextLine();

			switch (choice) {
			case 1:
				handleCreateRecord();
				break;
			case 2:
				handleViewAllRecords();
				break;
			case 3:
				if (currentUser.isLibrarian()) {
					handleAssignRequest();
				} else {
					System.out.println("Invalid option.");
				}
				break;
			case 4:
				if(currentUser.isLibrarian()) {
				handleViewMyRecords();
				}
				else {
					System.out.println("Invalid option.");
				}
				break;
			case 5:
				handleUpdatePassword();
				break;
			case 6:
				currentUser = null;
				System.out.println("Logged out successfully.");
				break;
			default:
				System.out.println("Invalid option. Please try again.");
			}
		} catch (Exception e) {
			System.out.println("Invalid input. Please enter a number.");
			input.nextLine();
		}

		return true;
	}

	/**
	 * Handle user registration
	 */
	private void handleRegistration() {
		try {
			System.out.println("\n=== User Registration ===");

			System.out.print("Name: ");
			String name = input.nextLine();

			System.out.print("Student ID: ");
			String studentId = input.nextLine();

			System.out.print("Email: ");
			String email = input.nextLine();

			System.out.print("Password: ");
			String password = input.nextLine();

			System.out.print("Department: ");
			String department = input.nextLine();

			System.out.println("Role: 1) Student  2) Librarian");
			System.out.print("Choose role: ");
			int roleChoice = input.nextInt();
			input.nextLine();

			String role = (roleChoice == 2) ? "LIBRARIAN" : "STUDENT";

			// Send registration request to server
			String message = "REGISTER|" + name + "|" + studentId + "|" + email + "|" + password + "|" + department
					+ "|" + role;
			sendMessage(message);

			// Receive response from server
			String response = (String) in.readObject();

			if (response.startsWith("SUCCESS")) {
				System.out.println("\n✓ Registration successful! You can now login.");
			} else {
				System.out.println("\n✗ Registration failed: " + response.substring(6));
			}

		} catch (Exception e) {
			System.err.println("Registration error: " + e.getMessage());
		}
	}

	/**
	 * Handle user login
	 */
	private void handleLogin() {
		try {
			System.out.println("\n=== User Login ===");

			System.out.print("Email: ");
			String email = input.nextLine();

			System.out.print("Password: ");
			String password = input.nextLine();

			// Send login request to server
			String message = "LOGIN|" + email + "|" + password;
			sendMessage(message);

			// Receive response from server (User object if successful)
			Object response = in.readObject();

			if (response instanceof User) {
				currentUser = (User) response;
				System.out.println("\n✓ Login successful! Welcome, " + currentUser.getName());
			} else if (response instanceof String) {
				System.out.println("\n✗ Login failed: " + response);
			}

		} catch (Exception e) {
			System.err.println("Login error: " + e.getMessage());
		}
	}

	/**
	 * Handle creating a library record
	 */
	private void handleCreateRecord() {
		try {
			System.out.println("\n=== Create Library Record ===");
			System.out.println("Record Type: 1) New Book Entry  2) Borrow Request");
			System.out.print("Choose type: ");
			int typeChoice = input.nextInt();
			input.nextLine();

			String recordType = (typeChoice == 1) ? "NEW_BOOK_ENTRY" : "BORROW_REQUEST";

			// Send request to server
			String message = "CREATE_RECORD|" + recordType + "|" + currentUser.getId();
			sendMessage(message);

			// Receive response
			String response = (String) in.readObject();
			System.out.println(response);

		} catch (Exception e) {
			System.err.println("Error creating record: " + e.getMessage());
		}
	}

	/**
	 * Handle viewing all book records
	 */
	private void handleViewAllRecords() {
		try {
			System.out.println("\n=== All Book Records ===");

			// Send request to server
			sendMessage("VIEW_ALL_RECORDS");

			// Receive response
			Object response = in.readObject();
			System.out.println(response);

		} catch (Exception e) {
			System.err.println("Error viewing records: " + e.getMessage());
		}
	}

	/**
	 * Handle assigning a borrowing request (Librarian only)
	 */
	private void handleAssignRequest() {
		try {
			System.out.println("\n=== Assign Borrowing Request ===");
			System.out.print("Enter Record ID to assign: ");
			String recordId = input.nextLine();

			// Send request to server
			String message = "ASSIGN_REQUEST|" + recordId + "|" + currentUser.getId();
			sendMessage(message);

			// Receive response
			String response = (String) in.readObject();
			System.out.println(response);

		} catch (Exception e) {
			System.err.println("Error assigning request: " + e.getMessage());
		}
	}

	/**
	 * Handle viewing records assigned to current user
	 */
	private void handleViewMyRecords() {
		try {
			System.out.println("\n=== My Assigned Records ===");

			// Send request to server
			String message = "VIEW_MY_RECORDS|" + currentUser.getId();
			sendMessage(message);

			// Receive response
			Object response = in.readObject();
			System.out.println(response);

		} catch (Exception e) {
			System.err.println("Error viewing records: " + e.getMessage());
		}
	}

	/**
	 * Handle password update
	 */
	private void handleUpdatePassword() {
		try {
			System.out.println("\n=== Update Password ===");

			System.out.print("Current Password: ");
			String oldPassword = input.nextLine();

			System.out.print("New Password: ");
			String newPassword = input.nextLine();

			System.out.print("Confirm New Password: ");
			String confirmPassword = input.nextLine();

			if (!newPassword.equals(confirmPassword)) {
				System.out.println("✗ Passwords do not match!");
				return;
			}

			// Send request to server
			String message = "UPDATE_PASSWORD|" + currentUser.getEmail() + "|" + oldPassword + "|" + newPassword;
			sendMessage(message);

			// Receive response
			String response = (String) in.readObject();
			System.out.println(response);

		} catch (Exception e) {
			System.err.println("Error updating password: " + e.getMessage());
		}
	}

	void sendMessage(String msg) {
		try {
			out.writeObject(msg);
			out.flush();
			System.out.println("client>" + msg);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	public static void main(String args[]) {
		Requester client = new Requester();
		client.run();
	}
}