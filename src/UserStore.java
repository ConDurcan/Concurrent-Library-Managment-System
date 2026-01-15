import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;

public class UserStore {

	private static final String FILE_NAME = "user.dat";
	private static ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

	// Load users from file(call this when server starts)
	public static void loadUsers() {
		File file = new File(FILE_NAME);

		if (file.exists()) {
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
				Object obj = ois.readObject();
				if (obj instanceof ConcurrentHashMap) {
					users.putAll((ConcurrentHashMap<String, User>) obj);
				}
				System.out.println("Loaded " + users.size() + " users from file.");
			} catch (IOException | ClassNotFoundException e) {
				System.err.println("Error Loading users: " + e.getMessage());
				e.printStackTrace();
			}
		} else {
			System.out.println("File does not exist. Starting with empty user list");
		}
	}

	public static synchronized void saveUsers() {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
			oos.writeObject(users);
			System.out.println("Saved " + users.size() + " users to file.");
		} catch (IOException e) {
			System.err.println("Error saving users: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	 public static synchronized boolean addUser(User user) {
	        // Check if email already exists
	        if (users.containsKey(user.getEmail())) {
	            System.out.println("Registration failed: Email already exists - " + user.getEmail());
	            return false;
	        }
	        
	        // Check if student ID already exists
	        for (User existingUser : users.values()) {
	            if (existingUser.getId().equals(user.getId())) {
	                System.out.println("Registration failed: Student ID already exists - " + user.getId());
	                return false;
	            }
	        }
	        
	        // Add user (email is the key)
	        users.put(user.getEmail(), user);
	        saveUsers();
	        System.out.println("User registered successfully: " + user.getEmail());
	        return true;
	    }
	 
	 public static User getUserByEmail(String email) {
	        return users.get(email);
	    }
	    
	    public static User getUserById(String studentId) {
	        for (User user : users.values()) {
	            if (user.getId().equals(studentId)) {
	                return user;
	            }
	        }
	        return null;
	    }
	    

	    public static User validateLogin(String email, String password) {
	        User user = users.get(email);
	        
	        if (user != null && user.getPassword().equals(password)) {
	            System.out.println("Login successful: " + email);
	            return user;
	        }
	        
	        System.out.println("Login failed: Invalid credentials for " + email);
	        return null;
	    }
	    
	    
	    public static synchronized boolean updatePassword(String email, String oldPassword, String newPassword) {
	        User user = users.get(email);
	        
	        if (user == null) {
	            System.out.println("Password update failed: User not found - " + email);
	            return false;
	        }
	        
	        if (!user.getPassword().equals(oldPassword)) {
	            System.out.println("Password update failed: Incorrect old password");
	            return false;
	        }
	        
	        user.setPassword(newPassword);
	        saveUsers(); // Persist changes
	        System.out.println("Password updated successfully for: " + email);
	        return true;
	    }
	    
	    public static ConcurrentHashMap<String, User> getAllUsers() {
	        return new ConcurrentHashMap<>(users); // Returns a copy for safety
	    }
	    
	    
	    public static boolean userExists(String email) {
	        return users.containsKey(email);
	    }
	    
	    
	    public static int getUserCount() {
	        return users.size();
	    }
}
