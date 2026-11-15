# Hotel Maintenance & Complaint Management System

A plain Java Swing desktop application using SQLite and JDBC. This version does not use Maven and does not use Java packages.

## Project files

Keep all `.java` files in the same folder:

- `Main.java` - starts the application
- `MainFrame.java` - Swing interface and event handling
- `Database.java` - creates the SQLite database and tables
- `Complaint.java`, `Staff.java`, `Room.java`, `MaintenanceTask.java` - model classes
- `ComplaintManager.java`, `StaffManager.java`, `RoomManager.java`, `MaintenanceManager.java` - database operations

The database file `hotel.db` is created automatically when the application starts. Do not create it manually.

## Requirement

- JDK 8 or newer
- Visual Studio Code with the Java Extension Pack
- SQLite JDBC driver (`sqlite-jdbc` JAR)

## Add the SQLite JDBC library in VS Code

1. Download the SQLite JDBC JAR from the Xerial SQLite JDBC releases page:
   https://github.com/xerial/sqlite-jdbc/releases
2. Choose the `.jar` file from the latest release and place it in this project folder.
3. In VS Code, open the Java Projects view.
4. Under **Referenced Libraries**, click the `+` button and select the downloaded JAR.
5. If VS Code asks to reload the Java project, allow it.

Do not rename the JAR to a `.java` file. It is a library.

## Run

1. Open this folder in VS Code.
2. Make sure all Java files are in the same folder.
3. Confirm the SQLite JDBC JAR appears under Referenced Libraries.
4. Open `Main.java`.
5. Click Run, or use the terminal:

   Windows:
   `javac -cp ".;sqlite-jdbc-<version>.jar" *.java`
   `java -cp ".;sqlite-jdbc-<version>.jar" Main`

   Replace `<version>` with the actual JAR filename.

   macOS / Linux:
   `javac -cp ".:sqlite-jdbc-<version>.jar" *.java`
   `java -cp ".:sqlite-jdbc-<version>.jar" Main`

## Features included

### Complaints
- Register complaints with room, guest, category, description and priority
- Automatic unique complaint ID
- Status updates: Pending, In Progress, Resolved
- Optional staff assignment and reassignment
- Search by complaint ID, guest name or room number
- Filter by status, category and priority
- Duplicate warning for an unresolved complaint with the same room and category
- Complaint history log
- Resolution/update notes
- SLA deadline stored at registration: High = 2 hours, Medium = 12 hours, Low = 24 hours
- Delete complaint

### Staff
- Add, edit and deactivate staff
- Department and availability fields
- Active unresolved workload count

### Rooms
- Starter room list is inserted on first run
- Add/edit rooms and update maintenance status
- Deactivate rooms rather than deleting them

### Preventive maintenance
- Schedule maintenance for a room or common area
- Frequency: one-time, daily, weekly, monthly, quarterly or yearly
- Assign a staff member
- Update task status and completion notes

### Expenses
- Record labour, replacement parts, repair or other expenses
- Optional link to a complaint or maintenance task

### Dashboard
- Complaint totals, status counts, overdue count and maintenance task count

## Important scope notes

- This is a local, single-computer management application. It has no guest login, web portal, SMS/email notifications or multi-branch support.
- Maintenance frequency is stored as a schedule label. The program does not automatically generate the next recurring task.
- The dashboard displays counts. Advanced charts, monthly trend graphs, average resolution-time analytics and CSV export are not included in this starter build.
- Staff performance is represented by active workload; detailed performance reports are not included.
- The deadline is saved when a complaint is registered. The dashboard uses the stored deadline to count overdue unresolved complaints.

## Database tables

`rooms`, `staff`, `complaints`, `complaint_history`, `maintenance_tasks`, `expenses`

## Before your demonstration

1. Add a staff member.
2. Register a complaint for one of the starter rooms.
3. Assign the complaint to a staff member.
4. Update its status and add a note.
5. Open its history.
6. Add a maintenance task and mark it completed.
7. Record an expense.
8. Close and reopen the application to show that data remains saved in `hotel.db`.
