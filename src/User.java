import java.io.Serializable;

public class User implements Serializable{
	private static final long serialVersionUID = 1L;

	public enum Role {
		STUDENT,
		LIBRARIAN,
		ADMIN
	}

	//User variables
	private String name;
	private String id;		// unique
	private String email;	// unique
	private String password;
	private String departmentName;
	private Role role;
	
	public User(String name, String id, String email, String password, String departmentName, User.Role role) {
		super();
		this.name = name;
		this.id = id;
		this.email = email;
		this.password = password;
		this.departmentName = departmentName;
		this.role = role;
	}


	//Getters and setters
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getDepartmentName() {
		return departmentName;
	}


	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}


	public Role getRole() {
		return role;
	}


	public void setRole(Role role) {
		this.role = role;
	}
	
	 @Override
	    public String toString() {
	        return "User{" +
	                "name='" + name + '\'' +
	                ", id='" + id + '\'' +
	                ", email='" + email + '\'' +
	                ", department='" + departmentName + '\'' +
	                ", role=" + role +
	                '}';
	    }
	 
	 /**
	     * Helper method to check if this user is a librarian
	     */
	    public boolean isLibrarian() {
	        return role == Role.LIBRARIAN || role == Role.ADMIN;
	    }

	    /**
	     * Helper method to check if this user is a student
	     */
	    public boolean isStudent() {
	        return role == Role.STUDENT;
	    }
}
