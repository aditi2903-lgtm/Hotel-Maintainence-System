public class MaintenanceTask {
    private int id;
    private String title, location, roomNo, frequency, scheduledDate, status, completionNotes;

    public MaintenanceTask(int id, String title, String location, String roomNo, String frequency,
                           String scheduledDate, String status, String completionNotes) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.roomNo = roomNo;
        this.frequency = frequency;
        this.scheduledDate = scheduledDate;
        this.status = status;
        this.completionNotes = completionNotes;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public String getRoomNo() { return roomNo; }
    public String getFrequency() { return frequency; }
    public String getScheduledDate() { return scheduledDate; }
    public String getStatus() { return status; }
    public String getCompletionNotes() { return completionNotes; }
}
