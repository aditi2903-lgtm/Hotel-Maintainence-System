import java.sql.*;
import java.util.ArrayList;

public class MaintenanceManager {
    public static void addTask(String title, String location, String room, String frequency,
                               String date, int staffId, String status, String notes) throws Exception {
        Connection con = Database.getConnection();
        PreparedStatement ps = con.prepareStatement("INSERT INTO maintenance_tasks(title,location,room_no,frequency,scheduled_date,assigned_staff,status,completion_notes) VALUES(?,?,?,?,?,?,?,?)");
        ps.setString(1,title); ps.setString(2,location); ps.setString(3,room); ps.setString(4,frequency);
        ps.setString(5,date);
        if(staffId <= 0) ps.setNull(6,Types.INTEGER); else ps.setInt(6,staffId);
        ps.setString(7,status); ps.setString(8,notes);
        ps.executeUpdate(); ps.close(); con.close();
    }

    public static ArrayList<Object[]> getTasks() throws Exception {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        Connection con = Database.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT t.id,t.title,t.location,IFNULL(t.room_no,'Common Area'),t.frequency,t.scheduled_date,IFNULL(s.name,'Unassigned'),t.status FROM maintenance_tasks t LEFT JOIN staff s ON t.assigned_staff=s.id ORDER BY t.scheduled_date");
        while(rs.next()) list.add(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8)});
        rs.close(); st.close(); con.close();
        return list;
    }

    public static void updateTask(int id, String status, String notes) throws Exception {
        Connection con = Database.getConnection();
        PreparedStatement ps = con.prepareStatement("UPDATE maintenance_tasks SET status=?,completion_notes=? WHERE id=?");
        ps.setString(1,status); ps.setString(2,notes); ps.setInt(3,id); ps.executeUpdate();
        ps.close(); con.close();
    }

    public static void deleteTask(int id) throws Exception {
    Connection con = Database.getConnection();
    PreparedStatement ps = con.prepareStatement(
        "DELETE FROM maintenance_tasks WHERE id=?"
    );
    ps.setInt(1, id);
    ps.executeUpdate();
    ps.close();
    con.close();
    }
    
    public static void addExpense(String type, double amount, String date, String notes, int complaintId, int taskId) throws Exception {
        Connection con = Database.getConnection();
        PreparedStatement ps = con.prepareStatement("INSERT INTO expenses(expense_type,amount,expense_date,notes,complaint_id,task_id) VALUES(?,?,?,?,?,?)");
        ps.setString(1,type); ps.setDouble(2,amount); ps.setString(3,date); ps.setString(4,notes);
        if(complaintId <= 0) ps.setNull(5,Types.INTEGER); else ps.setInt(5,complaintId);
        if(taskId <= 0) ps.setNull(6,Types.INTEGER); else ps.setInt(6,taskId);
        ps.executeUpdate(); ps.close(); con.close();
    }

    public static void deleteExpense(int id) throws Exception {
    Connection con = Database.getConnection();
    PreparedStatement ps = con.prepareStatement(
        "DELETE FROM expenses WHERE id=?"
    );
    ps.setInt(1, id);
    ps.executeUpdate();
    ps.close();
    con.close();
    }
    public static ArrayList<Object[]> getExpenses() throws Exception {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        Connection con = Database.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT id,expense_type,amount,expense_date,notes,IFNULL(complaint_id,''),IFNULL(task_id,'') FROM expenses ORDER BY expense_date DESC");
        while(rs.next()) list.add(new Object[]{rs.getInt(1),rs.getString(2),rs.getDouble(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7)});
        rs.close(); st.close(); con.close();
        return list;
    }
}
