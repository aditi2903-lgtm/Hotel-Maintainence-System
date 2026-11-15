public class Staff {
    private int id;
    private String name, department, phone, availability;

    public Staff(int id, String name, String department, String phone, String availability) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.phone = phone;
        this.availability = availability;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getPhone() { return phone; }
    public String getAvailability() { return availability; }
}
