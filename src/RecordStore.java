import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RecordStore manages all library record data persistence and operations
 * Uses ConcurrentHashMap for thread-safe operations
 */
public class RecordStore {
    
    private static final String FILE_NAME = "records.dat";
    
    // ConcurrentHashMap is thread-safe for multi-threaded server
    // Key: recordId (unique), Value: LibraryRecord object
    private static ConcurrentHashMap<String, LibraryRecord> records = new ConcurrentHashMap<>();
    
    // Counter for generating unique record IDs
    private static AtomicInteger recordCounter = new AtomicInteger(1000);
    
    /**
     * Load records from file when server starts
     */
    public static void loadRecords() {
        File file = new File(FILE_NAME);
        
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object obj = ois.readObject();
                
                if (obj instanceof ConcurrentHashMap) {
                    records.putAll((ConcurrentHashMap<String, LibraryRecord>) obj);
                    
                    // Update counter to be higher than any existing record ID
                    int maxId = 1000;
                     for (String recordId : records.keySet()) {
                        try {
                            int id = Integer.parseInt(recordId.substring(1)); // Remove "R" prefix
                            if (id > maxId) {
                                maxId = id;
                            }
                        } catch (NumberFormatException e) {
                            // Skip invalid IDs
                        }
                    }
                    recordCounter.set(maxId + 1);
                }
                
                System.out.println("Loaded " + records.size() + " records from file.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading records: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No existing record file found. Starting with empty record list.");
        }
    }
    
    /**
     * Save all records to file
     */
    public static synchronized void saveRecords() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(records);
            System.out.println("Saved " + records.size() + " records to file.");
        } catch (IOException e) {
            System.err.println("Error saving records: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create a new library record
     * @param recordTypeStr Type of record (NEW_BOOK_ENTRY or BORROW_REQUEST)
     * @param studentId ID of student creating the record
     * @return The created LibraryRecord or null if failed
     */
    public static synchronized LibraryRecord createRecord(String recordTypeStr, String studentId) {
        try {
            // Parse record type
            LibraryRecord.RecordType recordType = 
                LibraryRecord.RecordType.valueOf(recordTypeStr.toUpperCase());
            
            // Generate unique record ID (format: R1001, R1002, etc.)
            String recordId = "R" + recordCounter.getAndIncrement();
            
            // Create the record
            LibraryRecord record = new LibraryRecord(recordId, recordType, studentId);
            
            // Add to map
            records.put(recordId, record);
            
            // Save to file
            saveRecords();
            
            System.out.println("Record created: " + recordId + " by student " + studentId);
            return record;
            
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid record type: " + recordTypeStr);
            return null;
        }
    }
    
    /**
     * Get a record by its ID
     * @param recordId The record ID
     * @return LibraryRecord or null if not found
     */
    public static LibraryRecord getRecordById(String recordId) {
        return records.get(recordId);
    }
    
    /**
     * Get all records
     * @return ConcurrentHashMap of all records
     */
    public static ConcurrentHashMap<String, LibraryRecord> getAllRecords() {
        return new ConcurrentHashMap<>(records);
    }
    
    /**
     * Get records by user (different behavior for students vs librarians)
     * @param userId The user's ID
     * @param role The user's role
     * @return List of records relevant to this user
     */
    public static List<LibraryRecord> getRecordsByUser(String userId, User.Role role) {
        List<LibraryRecord> userRecords = new ArrayList<>();
        
        for (LibraryRecord record : records.values()) {
            if (role == User.Role.STUDENT) {
                // Students see records they created
                if (record.getStudentId().equals(userId)) {
                    userRecords.add(record);
                }
            } else if (role == User.Role.LIBRARIAN || role == User.Role.ADMIN) {
                // Librarians see records assigned to them
                if (userId.equals(record.getAssignedLibrarianId())) {
                    userRecords.add(record);
                }
            }
        }
        
        return userRecords;
    }
    
    /**
     * Assign a librarian to a borrowing request
     * @param recordId The record ID
     * @param librarianId The librarian's ID
     * @return true if successful, false otherwise
     */
    public static synchronized boolean assignLibrarian(String recordId, String librarianId) {
        LibraryRecord record = records.get(recordId);
        
        if (record == null) {
            System.err.println("Record not found: " + recordId);
            return false;
        }
        
        // Check if already assigned
        if (record.isAssigned()) {
            System.err.println("Record already assigned: " + recordId);
            return false;
        }
        
        // Assign librarian
        record.setAssignedLibrarianId(librarianId);
        
        // Update status if it's a borrow request
        if (record.isBorrowRequest() && record.getStatus() == LibraryRecord.Status.REQUESTED) {
            record.setStatus(LibraryRecord.Status.BORROWED);
        }
        
        // Save changes
        saveRecords();
        
        System.out.println("Librarian " + librarianId + " assigned to record " + recordId);
        return true;
    }
    
    /**
     * Update the status of a record
     * @param recordId The record ID
     * @param newStatus The new status
     * @return true if successful, false otherwise
     */
    public static synchronized boolean updateRecordStatus(String recordId, LibraryRecord.Status newStatus) {
        LibraryRecord record = records.get(recordId);
        
        if (record == null) {
            System.err.println("Record not found: " + recordId);
            return false;
        }
        
        record.setStatus(newStatus);
        saveRecords();
        
        System.out.println("Record " + recordId + " status updated to " + newStatus);
        return true;
    }
    
    /**
     * Get all unassigned borrow requests
     * @return List of unassigned borrow requests
     */
    public static List<LibraryRecord> getUnassignedRequests() {
        List<LibraryRecord> unassigned = new ArrayList<>();
        
        for (LibraryRecord record : records.values()) {
            if (record.isBorrowRequest() && !record.isAssigned()) {
                unassigned.add(record);
            }
        }
        
        return unassigned;
    }
    
    /**
     * Get total number of records
     * @return Number of records
     */
    public static int getRecordCount() {
        return records.size();
    }
    
    /**
     * Check if a record exists
     * @param recordId The record ID
     * @return true if record exists
     */
    public static boolean recordExists(String recordId) {
        return records.containsKey(recordId);
    }
}