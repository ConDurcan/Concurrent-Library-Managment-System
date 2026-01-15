import java.io.Serializable;
import java.time.LocalDate;

/**
 * LibraryRecord represents a library transaction record Can be either a new
 * book entry or a borrow request
 */
public class LibraryRecord implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum RecordType {
		NEW_BOOK_ENTRY, BORROW_REQUEST
	}

	public enum Status {
		AVAILABLE, REQUESTED, BORROWED, RETURNED
	}

	// Record fields
	private String recordId; // Unique ID (auto-generated)
	private RecordType recordType; // NEW_BOOK_ENTRY or BORROW_REQUEST
	private LocalDate date; // Date record was created
	private String studentId; // ID of student who created the record
	private Status status; // Current status
	private String assignedLibrarianId; // ID of librarian assigned (null if unassigned)

	/**
	 * Constructor for creating a new record
	 */
	public LibraryRecord(String recordId, RecordType recordType, String studentId) {
		this.recordId = recordId;
		this.recordType = recordType;
		this.date = LocalDate.now();
		this.studentId = studentId;

		// Set initial status based on record type
		if (recordType == RecordType.NEW_BOOK_ENTRY) {
			this.status = Status.AVAILABLE;
		} else {
			this.status = Status.REQUESTED;
		}

		this.assignedLibrarianId = null; // Initially unassigned
	}

	/**
	 * Default constructor
	 */
	public LibraryRecord() {
		// Empty constructor
	}

	// Getters and Setters
	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public RecordType getRecordType() {
		return recordType;
	}

	public void setRecordType(RecordType recordType) {
		this.recordType = recordType;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getAssignedLibrarianId() {
		return assignedLibrarianId;
	}

	public void setAssignedLibrarianId(String assignedLibrarianId) {
		this.assignedLibrarianId = assignedLibrarianId;
	}

	/**
	 * Check if this record has been assigned to a librarian
	 */
	public boolean isAssigned() {
		return assignedLibrarianId != null && !assignedLibrarianId.isEmpty();
	}

	/**
	 * Check if this is a borrow request
	 */
	public boolean isBorrowRequest() {
		return recordType == RecordType.BORROW_REQUEST;
	}

	/**
	 * Check if this is a new book entry
	 */
	public boolean isNewBookEntry() {
		return recordType == RecordType.NEW_BOOK_ENTRY;
	}

	@Override
	public String toString() {
		return "LibraryRecord{" + "recordId='" + recordId + '\'' + ", recordType=" + recordType + ", date=" + date
				+ ", studentId='" + studentId + '\'' + ", status=" + status + ", assignedLibrarianId='"
				+ (assignedLibrarianId != null ? assignedLibrarianId : "Unassigned") + '\'' + '}';
	}
}