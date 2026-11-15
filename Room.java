public class Room {
    private String roomNo, floor, type, maintenanceStatus;

    public Room(String roomNo, String floor, String type, String maintenanceStatus) {
        this.roomNo = roomNo;
        this.floor = floor;
        this.type = type;
        this.maintenanceStatus = maintenanceStatus;
    }

    public String getRoomNo() { return roomNo; }
    public String getFloor() { return floor; }
    public String getType() { return type; }
    public String getMaintenanceStatus() { return maintenanceStatus; }
}
