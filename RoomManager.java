import java.sql.*;
import java.util.ArrayList;

public class RoomManager {
    public static void saveRoom(String room, String floor, String type, String status) throws Exception {
        Connection con = Database.getConnection();
        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO rooms(room_no,floor,room_type,maintenance_status,active) VALUES(?,?,?,?,1) " +
                "ON CONFLICT(room_no) DO UPDATE SET floor=excluded.floor,room_type=excluded.room_type,maintenance_status=excluded.maintenance_status,active=1");
        ps.setString(1,room); ps.setString(2,floor); ps.setString(3,type); ps.setString(4,status);
        ps.executeUpdate(); ps.close(); con.close();
    }

    public static void deactivateRoom(String room) throws Exception {
        Connection con = Database.getConnection();
        PreparedStatement ps = con.prepareStatement("UPDATE rooms SET active=0 WHERE room_no=?");
        ps.setString(1,room); ps.executeUpdate(); ps.close(); con.close();
    }

    public static ArrayList<Object[]> getRooms() throws Exception {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        Connection con = Database.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT room_no,floor,room_type,maintenance_status FROM rooms WHERE active=1 ORDER BY room_no");
        while(rs.next()) list.add(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4)});
        rs.close(); st.close(); con.close();
        return list;
    }

    public static ArrayList<String> getRoomNumbers() throws Exception {
        ArrayList<String> list = new ArrayList<String>();
        Connection con = Database.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT room_no FROM rooms WHERE active=1 ORDER BY room_no");
        while(rs.next()) list.add(rs.getString(1));
        rs.close(); st.close(); con.close();
        return list;
    }
}
