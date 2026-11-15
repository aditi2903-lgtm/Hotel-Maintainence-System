import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ComplaintManager {
    public static int addComplaint(String room, String guest, String category, String description,
                                   String priority, int staffId) throws Exception {
        Connection con = Database.getConnection();
        PreparedStatement check = con.prepareStatement(
                "SELECT COUNT(*) FROM complaints WHERE room_no=? AND category=? AND status!='Resolved'");
        check.setString(1, room);
        check.setString(2, category);
        ResultSet cr = check.executeQuery();
        boolean duplicate = cr.next() && cr.getInt(1) > 0;
        cr.close(); check.close();

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        int hours = priority.equals("High") ? 2 : (priority.equals("Medium") ? 12 : 24);
        String deadline = LocalDateTime.now().plusHours(hours).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO complaints(room_no,guest,category,description,priority,status,assigned_staff,created_at,deadline) " +
                "VALUES(?,?,?,?,?,'Pending',?,?,?)", Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, room); ps.setString(2, guest); ps.setString(3, category);
        ps.setString(4, description); ps.setString(5, priority);
        if (staffId <= 0) ps.setNull(6, Types.INTEGER); else ps.setInt(6, staffId);
        ps.setString(7, now); ps.setString(8, deadline);
        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        int id = keys.next() ? keys.getInt(1) : -1;
        keys.close(); ps.close();

        addHistory(con, id, "Complaint registered");
        con.close();
        if (duplicate) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "A similar unresolved complaint already exists for this room and category.",
                    "Possible Duplicate", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
        return id;
    }

    public static void updateStatus(int id, String status, String notes) throws Exception {
        Connection con = Database.getConnection();
        PreparedStatement ps = con.prepareStatement(
                "UPDATE complaints SET status=?, resolution_notes=? WHERE id=?");
        ps.setString(1, status); ps.setString(2, notes); ps.setInt(3, id);
        ps.executeUpdate(); ps.close();
        addHistory(con, id, "Status changed to " + status);
        if (notes != null && !notes.trim().isEmpty()) addHistory(con, id, "Resolution note: " + notes);
        con.close();
    }

    public static void assignStaff(int id, int staffId) throws Exception {
        Connection con = Database.getConnection();
        PreparedStatement ps = con.prepareStatement("UPDATE complaints SET assigned_staff=? WHERE id=?");
        if (staffId <= 0) ps.setNull(1, Types.INTEGER); else ps.setInt(1, staffId);
        ps.setInt(2, id); ps.executeUpdate(); ps.close();
        addHistory(con, id, "Staff assignment updated");
        con.close();
    }

    public static void deleteComplaint(int id) throws Exception {
        Connection con = Database.getConnection();
        PreparedStatement h = con.prepareStatement("DELETE FROM complaint_history WHERE complaint_id=?");
        h.setInt(1, id); h.executeUpdate(); h.close();
        PreparedStatement p = con.prepareStatement("DELETE FROM complaints WHERE id=?");
        p.setInt(1, id); p.executeUpdate(); p.close();
        con.close();
    }

    public static ArrayList<Object[]> getComplaints(String search, String status, String category, String priority) throws Exception {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        Connection con = Database.getConnection();
        String sql = "SELECT c.id,c.room_no,c.guest,c.category,c.priority,c.status,IFNULL(s.name,'Unassigned'),c.created_at,c.deadline " +
                "FROM complaints c LEFT JOIN staff s ON c.assigned_staff=s.id WHERE " +
                "(CAST(c.id AS TEXT) LIKE ? OR c.guest LIKE ? OR c.room_no LIKE ?) AND c.status LIKE ? AND c.category LIKE ? AND c.priority LIKE ? ORDER BY c.id DESC";
        PreparedStatement ps = con.prepareStatement(sql);
        String q = "%" + search + "%";
        ps.setString(1,q); ps.setString(2,q); ps.setString(3,q);
        ps.setString(4,status.equals("All") ? "%" : status);
        ps.setString(5,category.equals("All") ? "%" : category);
        ps.setString(6,priority.equals("All") ? "%" : priority);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),
                    rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8),rs.getString(9)});
        }
        rs.close(); ps.close(); con.close();
        return list;
    }

    public static ArrayList<String> getHistory(int id) throws Exception {
        ArrayList<String> list = new ArrayList<String>();
        Connection con = Database.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT activity,activity_time FROM complaint_history WHERE complaint_id=? ORDER BY id");
        ps.setInt(1,id);
        ResultSet rs = ps.executeQuery();
        while(rs.next()) list.add(rs.getString(2) + " - " + rs.getString(1));
        rs.close(); ps.close(); con.close();
        return list;
    }

    private static void addHistory(Connection con, int id, String activity) throws Exception {
        PreparedStatement ps = con.prepareStatement("INSERT INTO complaint_history(complaint_id,activity,activity_time) VALUES(?,?,?)");
        ps.setInt(1,id); ps.setString(2,activity);
        ps.setString(3,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        ps.executeUpdate(); ps.close();
    }
}
