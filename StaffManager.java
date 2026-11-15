import java.sql.*;
import java.util.ArrayList;

public class StaffManager {
    public static void saveStaff(int id, String name, String department, String phone, String availability) throws Exception {
        Connection con = Database.getConnection();
        if (id == 0) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO staff(name,department,phone,availability) VALUES(?,?,?,?)");
            ps.setString(1,name); ps.setString(2,department); ps.setString(3,phone); ps.setString(4,availability);
            ps.executeUpdate(); ps.close();
        } else {
            PreparedStatement ps = con.prepareStatement("UPDATE staff SET name=?,department=?,phone=?,availability=? WHERE id=?");
            ps.setString(1,name); ps.setString(2,department); ps.setString(3,phone); ps.setString(4,availability); ps.setInt(5,id);
            ps.executeUpdate(); ps.close();
        }
        con.close();
    }

    public static void deactivate(int id) throws Exception {
        Connection con = Database.getConnection();
        PreparedStatement ps = con.prepareStatement("UPDATE staff SET active=0 WHERE id=?");
        ps.setInt(1,id); ps.executeUpdate(); ps.close(); con.close();
    }

    public static ArrayList<Object[]> getStaff() throws Exception {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        Connection con = Database.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT s.id,s.name,s.department,s.phone,s.availability,COUNT(c.id) " +
                "FROM staff s LEFT JOIN complaints c ON s.id=c.assigned_staff AND c.status!='Resolved' " +
                "WHERE s.active=1 GROUP BY s.id ORDER BY s.id");
        while(rs.next()) list.add(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getInt(6)});
        rs.close(); st.close(); con.close();
        return list;
    }

    public static ArrayList<Object[]> getStaffOptions() throws Exception {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        Connection con = Database.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT id,name,department FROM staff WHERE active=1 ORDER BY name");
        while(rs.next()) list.add(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3)});
        rs.close(); st.close(); con.close();
        return list;
    }
}
