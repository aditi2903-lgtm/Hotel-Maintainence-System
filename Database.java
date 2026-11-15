import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {
    private static final String URL = "jdbc:sqlite:hotel.db";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL);
    }

    public static void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection con = getConnection();
            Statement st = con.createStatement();

            st.executeUpdate("CREATE TABLE IF NOT EXISTS rooms (" +
                    "room_no TEXT PRIMARY KEY, floor TEXT, room_type TEXT, maintenance_status TEXT, active INTEGER DEFAULT 1)");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS staff (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, department TEXT, phone TEXT, availability TEXT, active INTEGER DEFAULT 1)");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS complaints (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, room_no TEXT, guest TEXT, category TEXT, description TEXT, " +
                    "priority TEXT, status TEXT, assigned_staff INTEGER, created_at TEXT, deadline TEXT, resolution_notes TEXT, " +
                    "FOREIGN KEY(room_no) REFERENCES rooms(room_no), FOREIGN KEY(assigned_staff) REFERENCES staff(id))");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS complaint_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, complaint_id INTEGER, activity TEXT, activity_time TEXT)");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS maintenance_tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, location TEXT, room_no TEXT, frequency TEXT, " +
                    "scheduled_date TEXT, assigned_staff INTEGER, status TEXT, completion_notes TEXT)");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS expenses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, expense_type TEXT, amount REAL, expense_date TEXT, notes TEXT, complaint_id INTEGER, task_id INTEGER)");

            seedRooms(con);
            st.close();
            con.close();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Could not initialise the database.\nCheck that sqlite-jdbc.jar is added to the project.\n\n" + e.getMessage(),
                    "Database Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void seedRooms(Connection con) throws Exception {
        String[][] rooms = {
            {"101","1","Standard"}, {"102","1","Standard"}, {"103","1","Deluxe"},
            {"201","2","Standard"}, {"202","2","Deluxe"}, {"203","2","Suite"},
            {"301","3","Standard"}, {"302","3","Deluxe"}, {"303","3","Suite"}
        };
        java.sql.PreparedStatement ps = con.prepareStatement(
                "INSERT OR IGNORE INTO rooms(room_no,floor,room_type,maintenance_status,active) VALUES(?,?,?,'Operational',1)");
        for (int i = 0; i < rooms.length; i++) {
            ps.setString(1, rooms[i][0]);
            ps.setString(2, rooms[i][1]);
            ps.setString(3, rooms[i][2]);
            ps.executeUpdate();
        }
        ps.close();
    }
}
