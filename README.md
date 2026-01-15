# Concurrent-Library-Managment-System
This is a Multi-Threaded Library Management System; it manages library records and user accounts. It has many features including record management, authentication, user registration and role-based access to certain menu options.

### System Archictecture
This application uses a **Provider-Service** model built on TCP Sockets:
- **The Server (`LibraryServer`):** Acts as the connection listener. It runs an infinite loop, deploying a new `ServerThread` for every client that connects.
- **The Threading (`ServerThread`):** Handles individual client logic, parsing delimited commands and interacting with the data stores.
- **Persistence:** Uses Object Serialization to save data to `users.dat` and `records.dat`.
- **Concurrency:** Employs `ConcurrentHashMap`, `AtomicInteger`, and `synchronized` blocks to prevent race conditions during multi-user access.

## How to Run (Step-by-Step)

To run this project, you will need at least **two terminal windows** open (one for the Server, one or more for Clients).

### 1. Clean & Compile
Navigate to your source folder and run the following command to ensure you are running the latest version of the code:
```powershell
# Deletes old class files and recompiles everything
rm *.class
javac *.java

### 2. Start the Server
Run the Library Server. it will listen on port 2004
java Library Server

### 3. Start the Client
Open a new terminal tab or window and run
java Requester

### Command Protocol
Communication uses a pipe-delimited string format: COMMAND|param1|param2|... Example: REGISTER|John Doe|S123|john@email.com|pass|CS|STUDENT
