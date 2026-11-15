public class Complaint {
    private int id;
    private String roomNo, guest, category, description, priority, status, createdAt, deadline, resolutionNotes;
    private int assignedStaff;

    public Complaint(int id, String roomNo, String guest, String category, String description,
                     String priority, String status, int assignedStaff, String createdAt,
                     String deadline, String resolutionNotes) {
        this.id = id;
        this.roomNo = roomNo;
        this.guest = guest;
        this.category = category;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.assignedStaff = assignedStaff;
        this.createdAt = createdAt;
        this.deadline = deadline;
        this.resolutionNotes = resolutionNotes;
    }

    public int getId() { return id; }
    public String getRoomNo() { return roomNo; }
    public String getGuest() { return guest; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public int getAssignedStaff() { return assignedStaff; }
    public String getCreatedAt() { return createdAt; }
    public String getDeadline() { return deadline; }
    public String getResolutionNotes() { return resolutionNotes; }
}
