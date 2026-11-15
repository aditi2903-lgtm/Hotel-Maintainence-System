import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class MainFrame extends JFrame {
    private JTable complaintTable, staffTable, roomTable, taskTable, expenseTable;
    private DefaultTableModel complaintModel, staffModel, roomModel, taskModel, expenseModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter, categoryFilter, priorityFilter;

    public MainFrame() {
        setTitle("Hotel Maintenance & Complaint Management System");
        setSize(1120, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", dashboardPanel());
        tabs.addTab("Complaints", complaintsPanel());
        tabs.addTab("Staff", staffPanel());
        tabs.addTab("Rooms", roomsPanel());
        tabs.addTab("Maintenance", maintenancePanel());
        tabs.addTab("Expenses", expensesPanel());
        add(tabs);

        refreshAll();
        setVisible(true);
    }

    private JPanel dashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
        JLabel heading = new JLabel("Hotel Maintenance Dashboard", SwingConstants.CENTER);
        heading.setFont(new Font("Arial",Font.BOLD,25));
        panel.add(heading,BorderLayout.NORTH);
        JPanel stats = new JPanel(new GridLayout(2,3,15,15));
        String[] labels = {"Total Complaints","Pending","In Progress","Resolved","Overdue","Maintenance Tasks"};
        for(String label: labels) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY),
                    BorderFactory.createEmptyBorder(18,10,18,10)));
            JLabel name = new JLabel(label,SwingConstants.CENTER);
            JLabel value = new JLabel("0",SwingConstants.CENTER);
            value.setFont(new Font("Arial",Font.BOLD,28));
            card.add(name,BorderLayout.NORTH); card.add(value,BorderLayout.CENTER);
            card.putClientProperty("valueLabel",value);
            stats.add(card);
        }
        panel.add(stats,BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh Dashboard");
        refresh.addActionListener(e -> updateDashboard(stats));
        panel.add(refresh,BorderLayout.SOUTH);
        SwingUtilities.invokeLater(() -> updateDashboard(stats));
        return panel;
    }

    private void updateDashboard(JPanel stats) {
        try {
            String[] queries = {
                "SELECT COUNT(*) FROM complaints",
                "SELECT COUNT(*) FROM complaints WHERE status='Pending'",
                "SELECT COUNT(*) FROM complaints WHERE status='In Progress'",
                "SELECT COUNT(*) FROM complaints WHERE status='Resolved'",
                "SELECT COUNT(*) FROM complaints WHERE status!='Resolved' AND deadline < datetime('now','localtime')",
                "SELECT COUNT(*) FROM maintenance_tasks"
            };
            Connection con = Database.getConnection();
            for(int i=0;i<queries.length;i++) {
                Statement st=con.createStatement(); ResultSet rs=st.executeQuery(queries[i]);
                if(rs.next()) {
                    JPanel card=(JPanel)stats.getComponent(i);
                    JLabel value=(JLabel)card.getClientProperty("valueLabel");
                    value.setText(String.valueOf(rs.getInt(1)));
                }
                rs.close(); st.close();
            }
            con.close();
        } catch(Exception ex) { showError(ex); }
    }

    private JPanel complaintsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8,8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(13);
        statusFilter = new JComboBox<String>(new String[]{"All","Pending","In Progress","Resolved"});
        categoryFilter = new JComboBox<String>(new String[]{"All","Plumbing","Electrical","Housekeeping","AC","Other"});
        priorityFilter = new JComboBox<String>(new String[]{"All","Low","Medium","High"});
        JButton search = new JButton("Search / Filter");
        JButton add = new JButton("Register Complaint");
        JButton update = new JButton("Update Status");
        JButton assign = new JButton("Assign Staff");
        JButton history = new JButton("View History");
        JButton delete = new JButton("Delete");
        top.add(new JLabel("Search ID / Guest / Room:")); top.add(searchField);
        top.add(statusFilter); top.add(categoryFilter); top.add(priorityFilter);
        top.add(search); top.add(add); top.add(update); top.add(assign); top.add(history); top.add(delete);
        panel.add(top,BorderLayout.NORTH);

        complaintModel = model(new String[]{"ID","Room","Guest","Category","Priority","Status","Assigned To","Created","Deadline"});
        complaintTable = new JTable(complaintModel);
        panel.add(new JScrollPane(complaintTable),BorderLayout.CENTER);

        search.addActionListener(e -> refreshComplaints());
        searchField.addActionListener(e -> refreshComplaints());
        add.addActionListener(e -> addComplaintDialog());
        update.addActionListener(e -> updateComplaintDialog());
        assign.addActionListener(e -> assignComplaintDialog());
        history.addActionListener(e -> showHistory());
        delete.addActionListener(e -> deleteComplaint());
        return panel;
    }

    private void addComplaintDialog() {
        try {
            ArrayList<String> rooms=RoomManager.getRoomNumbers();
            if(rooms.size()==0) { JOptionPane.showMessageDialog(this,"Add an active room first."); return; }
            JComboBox<String> room=new JComboBox<String>(rooms.toArray(new String[0]));
            JTextField guest=new JTextField();
            JComboBox<String> category=new JComboBox<String>(new String[]{"Plumbing","Electrical","Housekeeping","AC","Other"});
            JComboBox<String> priority=new JComboBox<String>(new String[]{"Low","Medium","High"});
            JComboBox<String> staff=staffCombo();
            JTextArea desc=new JTextArea(4,20);
            JPanel form=new JPanel(new GridLayout(0,2,6,6));
            form.add(new JLabel("Room")); form.add(room);
            form.add(new JLabel("Guest name")); form.add(guest);
            form.add(new JLabel("Category")); form.add(category);
            form.add(new JLabel("Priority")); form.add(priority);
            form.add(new JLabel("Assign staff (optional)")); form.add(staff);
            form.add(new JLabel("Description")); form.add(new JScrollPane(desc));
            int result=JOptionPane.showConfirmDialog(this,form,"Register Complaint",JOptionPane.OK_CANCEL_OPTION);
            if(result==JOptionPane.OK_OPTION) {
                if(guest.getText().trim().isEmpty() || desc.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this,"Guest name and description are required."); return;
                }
                int staffId=selectedId(staff);
                int id=ComplaintManager.addComplaint((String)room.getSelectedItem(),guest.getText().trim(),
                        (String)category.getSelectedItem(),desc.getText().trim(),(String)priority.getSelectedItem(),staffId);
                JOptionPane.showMessageDialog(this,"Complaint registered. Complaint ID: "+id);
                refreshAll();
            }
        } catch(Exception ex) { showError(ex); }
    }

    private void updateComplaintDialog() {
        int id=selectedComplaintId(); if(id<0) return;
        JComboBox<String> status=new JComboBox<String>(new String[]{"Pending","In Progress","Resolved"});
        JTextArea notes=new JTextArea(4,22);
        JPanel p=new JPanel(new GridLayout(0,1,5,5)); p.add(new JLabel("New status")); p.add(status);
        p.add(new JLabel("Resolution / update notes")); p.add(new JScrollPane(notes));
        if(JOptionPane.showConfirmDialog(this,p,"Update Complaint #"+id,JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            try { ComplaintManager.updateStatus(id,(String)status.getSelectedItem(),notes.getText().trim()); refreshAll(); }
            catch(Exception ex) { showError(ex); }
        }
    }

    private void assignComplaintDialog() {
        int id=selectedComplaintId(); if(id<0) return;
        try {
            JComboBox<String> staff=staffCombo();
            if(JOptionPane.showConfirmDialog(this,staff,"Assign Staff",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
                ComplaintManager.assignStaff(id,selectedId(staff)); refreshAll();
            }
        } catch(Exception ex) { showError(ex); }
    }

    private void showHistory() {
        int id=selectedComplaintId(); if(id<0) return;
        try {
            ArrayList<String> history=ComplaintManager.getHistory(id);
            JTextArea area=new JTextArea(12,45); area.setEditable(false);
            for(String line:history) area.append(line+"\n");
            JOptionPane.showMessageDialog(this,new JScrollPane(area),"Complaint #"+id+" History",JOptionPane.INFORMATION_MESSAGE);
        } catch(Exception ex) { showError(ex); }
    }

    private void deleteComplaint() {
        int id=selectedComplaintId(); if(id<0) return;
        if(JOptionPane.showConfirmDialog(this,"Delete complaint #"+id+"?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
            try { ComplaintManager.deleteComplaint(id); refreshAll(); } catch(Exception ex) { showError(ex); }
        }
    }

    private JPanel staffPanel() {
        JPanel panel=new JPanel(new BorderLayout(8,8));
        JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add=new JButton("Add Staff"), edit=new JButton("Edit Selected"), deactivate=new JButton("Deactivate Selected");
        top.add(add); top.add(edit); top.add(deactivate); panel.add(top,BorderLayout.NORTH);
        staffModel=model(new String[]{"ID","Name","Department","Phone","Availability","Active Workload"});
        staffTable=new JTable(staffModel); panel.add(new JScrollPane(staffTable),BorderLayout.CENTER);
        add.addActionListener(e -> staffDialog(0));
        edit.addActionListener(e -> { int row=staffTable.getSelectedRow(); if(row>=0) staffDialog((Integer)staffModel.getValueAt(row,0)); else warnSelect(); });
        deactivate.addActionListener(e -> {
            int row=staffTable.getSelectedRow(); if(row<0){warnSelect();return;}
            try { StaffManager.deactivate((Integer)staffModel.getValueAt(row,0)); refreshAll(); } catch(Exception ex){showError(ex);}
        });
        return panel;
    }

    private void staffDialog(int id) {
        try {
        JTextField name=new JTextField(), phone=new JTextField();
        JComboBox<String> dept=new JComboBox<String>(new String[]{"Plumbing","Electrical","Housekeeping","HVAC / AC","General Maintenance"});
        JComboBox<String> availability=new JComboBox<String>(new String[]{"Available","Busy","On Leave"});
        if(id>0) {
            int row=findRow(staffModel,id);
            if(row>=0) { name.setText((String)staffModel.getValueAt(row,1)); dept.setSelectedItem(staffModel.getValueAt(row,2)); phone.setText((String)staffModel.getValueAt(row,3)); availability.setSelectedItem(staffModel.getValueAt(row,4)); }
        }
        JPanel p=new JPanel(new GridLayout(0,2,5,5));
        p.add(new JLabel("Name"));p.add(name);p.add(new JLabel("Department"));p.add(dept);p.add(new JLabel("Phone"));p.add(phone);p.add(new JLabel("Availability"));p.add(availability);
        if(JOptionPane.showConfirmDialog(this,p,id==0?"Add Staff":"Edit Staff",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            if(name.getText().trim().isEmpty()){JOptionPane.showMessageDialog(this,"Name is required.");return;}
            try { StaffManager.saveStaff(id,name.getText().trim(),(String)dept.getSelectedItem(),phone.getText().trim(),(String)availability.getSelectedItem());refreshAll(); }
            catch(Exception ex){showError(ex);}
        }
        } catch(Exception ex) { showError(ex); }
    }

    private JPanel roomsPanel() {
        JPanel panel=new JPanel(new BorderLayout(8,8));
        JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add=new JButton("Add / Edit Room"), deactivate=new JButton("Deactivate Selected");
        top.add(add);top.add(deactivate);panel.add(top,BorderLayout.NORTH);
        roomModel=model(new String[]{"Room No","Floor","Type","Maintenance Status"});
        roomTable=new JTable(roomModel);panel.add(new JScrollPane(roomTable),BorderLayout.CENTER);
        add.addActionListener(e -> roomDialog());
        deactivate.addActionListener(e -> {
            int row=roomTable.getSelectedRow();if(row<0){warnSelect();return;}
            String room=(String)roomModel.getValueAt(row,0);
            try { RoomManager.deactivateRoom(room);refreshAll(); } catch(Exception ex){showError(ex);}
        });
        return panel;
    }

    private void roomDialog() {
        JTextField room=new JTextField(),floor=new JTextField();
        JComboBox<String> type=new JComboBox<String>(new String[]{"Standard","Deluxe","Suite","Other"});
        JComboBox<String> status=new JComboBox<String>(new String[]{"Operational","Under Maintenance","Maintenance Completed"});
        JPanel p=new JPanel(new GridLayout(0,2,5,5));
        p.add(new JLabel("Room number"));p.add(room);p.add(new JLabel("Floor"));p.add(floor);p.add(new JLabel("Room type"));p.add(type);p.add(new JLabel("Maintenance status"));p.add(status);
        if(JOptionPane.showConfirmDialog(this,p,"Add / Edit Room",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            if(room.getText().trim().isEmpty()||floor.getText().trim().isEmpty()){JOptionPane.showMessageDialog(this,"Room number and floor are required.");return;}
            try { RoomManager.saveRoom(room.getText().trim(),floor.getText().trim(),(String)type.getSelectedItem(),(String)status.getSelectedItem());refreshAll(); }
            catch(Exception ex){showError(ex);}
        }
    }

    private JPanel maintenancePanel() {
        JPanel panel=new JPanel(new BorderLayout(8,8));
        JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add=new JButton("Schedule Maintenance");
        JButton update=new JButton("Update Selected Task");
        JButton delete=new JButton("Delete Selected Task");
        top.add(add);
        top.add(update);
        top.add(delete);
        panel.add(top,BorderLayout.NORTH);
        taskModel=model(new String[]{"ID","Task","Location","Room","Frequency","Scheduled Date","Assigned Staff","Status"});
        taskTable=new JTable(taskModel);panel.add(new JScrollPane(taskTable),BorderLayout.CENTER);
        add.addActionListener(e -> taskDialog());
        update.addActionListener(e -> updateTaskDialog());
        delete.addActionListener(e -> deleteTaskDialog());
        return panel;
    }

    private void taskDialog() {
        JTextField title=new JTextField(),location=new JTextField(),date=new JTextField(LocalDate.now().toString());
        JComboBox<String> room=new JComboBox<String>();
        room.addItem("Common Area");
        try { for(String r:RoomManager.getRoomNumbers()) room.addItem(r); } catch(Exception ex){showError(ex);return;}
        JComboBox<String> frequency=new JComboBox<String>(new String[]{"One-time","Daily","Weekly","Monthly","Quarterly","Yearly"});
        JComboBox<String> staff;
        try { staff=staffCombo(); } catch(Exception ex) { showError(ex); return; }
        JPanel p=new JPanel(new GridLayout(0,2,5,5));
        p.add(new JLabel("Task"));p.add(title);p.add(new JLabel("Location / Equipment"));p.add(location);
        p.add(new JLabel("Room (or Common Area)"));p.add(room);p.add(new JLabel("Frequency"));p.add(frequency);
        p.add(new JLabel("Scheduled date (YYYY-MM-DD)"));p.add(date);p.add(new JLabel("Assign staff"));p.add(staff);
        if(JOptionPane.showConfirmDialog(this,p,"Schedule Maintenance",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            if(title.getText().trim().isEmpty()||location.getText().trim().isEmpty()){JOptionPane.showMessageDialog(this,"Task and location are required.");return;}
            try {
                LocalDate.parse(date.getText().trim());
                String selectedRoom=(String)room.getSelectedItem();
                MaintenanceManager.addTask(title.getText().trim(),location.getText().trim(),selectedRoom.equals("Common Area")?null:selectedRoom,
                        (String)frequency.getSelectedItem(),date.getText().trim(),selectedId(staff),"Scheduled","");
                refreshAll();
            } catch(Exception ex){JOptionPane.showMessageDialog(this,"Check the date format (YYYY-MM-DD) and try again.\n"+ex.getMessage());}
        }
    }

    private void updateTaskDialog() {
        int row=taskTable.getSelectedRow();if(row<0){warnSelect();return;}
        int id=(Integer)taskModel.getValueAt(row,0);
        JComboBox<String> status=new JComboBox<String>(new String[]{"Scheduled","In Progress","Completed","Cancelled"});
        JTextArea notes=new JTextArea(4,22);
        JPanel p=new JPanel(new GridLayout(0,1));p.add(status);p.add(new JLabel("Completion notes"));p.add(new JScrollPane(notes));
        if(JOptionPane.showConfirmDialog(this,p,"Update Task #"+id,JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            try {MaintenanceManager.updateTask(id,(String)status.getSelectedItem(),notes.getText().trim());refreshAll();}
            catch(Exception ex){showError(ex);}
        }
    }

    private void deleteTaskDialog() {
    int row = taskTable.getSelectedRow();
    if (row < 0) {
        warnSelect();
        return;
    }
    int id = (Integer) taskModel.getValueAt(row, 0);
    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to delete task #" + id + "?",
        "Confirm Deletion",
        JOptionPane.YES_NO_OPTION
    );
    if (confirm == JOptionPane.YES_OPTION) {
        try {
            MaintenanceManager.deleteTask(id);
            refreshAll();

            JOptionPane.showMessageDialog(this,
                "Maintenance task deleted successfully.");

        } catch (Exception ex) {
            showError(ex);
        }
    }
}

    private JPanel expensesPanel() {
        JPanel panel=new JPanel(new BorderLayout(8,8));
        JButton add=new JButton("Add Expense");
        JButton delete=new JButton("Delete Selected Expense");
        JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(add);
        top.add(delete);
        panel.add(top,BorderLayout.NORTH);
        expenseModel=model(new String[]{"ID","Type","Amount (INR)","Date","Notes","Complaint ID","Task ID"});
        expenseTable=new JTable(expenseModel);panel.add(new JScrollPane(expenseTable),BorderLayout.CENTER);
        add.addActionListener(e -> expenseDialog());
        delete.addActionListener(e -> deleteExpenseDialog());
        return panel;
    }

    private void deleteExpenseDialog() {
    int row = expenseTable.getSelectedRow();
    if (row < 0) {
        warnSelect();
        return;
    }
    int id = (Integer) expenseModel.getValueAt(row, 0);
    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to delete expense #" + id + "?",
        "Confirm Deletion",
        JOptionPane.YES_NO_OPTION
    );
    if (confirm == JOptionPane.YES_OPTION) {
        try {
            MaintenanceManager.deleteExpense(id);
            refreshAll();

            JOptionPane.showMessageDialog(this,
                "Expense deleted successfully.");

        } catch (Exception ex) {
            showError(ex);
        }
    }
}

    private void expenseDialog() {
        JComboBox<String> type=new JComboBox<String>(new String[]{"Labour","Replacement Parts","Repair","Other"});
        JTextField amount=new JTextField(),date=new JTextField(LocalDate.now().toString()),complaint=new JTextField(),task=new JTextField();
        JTextArea notes=new JTextArea(3,20);
        JPanel p=new JPanel(new GridLayout(0,2,5,5));
        p.add(new JLabel("Expense type"));p.add(type);p.add(new JLabel("Amount (INR)"));p.add(amount);
        p.add(new JLabel("Date (YYYY-MM-DD)"));p.add(date);p.add(new JLabel("Complaint ID (optional)"));p.add(complaint);
        p.add(new JLabel("Maintenance Task ID (optional)"));p.add(task);p.add(new JLabel("Notes"));p.add(new JScrollPane(notes));
        if(JOptionPane.showConfirmDialog(this,p,"Add Maintenance Expense",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            try {
                double value=Double.parseDouble(amount.getText().trim());
                if(value<=0) throw new Exception("Amount must be greater than zero.");
                LocalDate.parse(date.getText().trim());
                int cid=complaint.getText().trim().isEmpty()?0:Integer.parseInt(complaint.getText().trim());
                int tid=task.getText().trim().isEmpty()?0:Integer.parseInt(task.getText().trim());
                MaintenanceManager.addExpense((String)type.getSelectedItem(),value,date.getText().trim(),notes.getText().trim(),cid,tid);
                refreshAll();
            } catch(Exception ex){JOptionPane.showMessageDialog(this,"Enter a valid positive amount, date, and optional numeric IDs.\n"+ex.getMessage());}
        }
    }

    private JComboBox<String> staffCombo() throws Exception {
        JComboBox<String> combo=new JComboBox<String>();
        combo.addItem("Unassigned");
        ArrayList<Object[]> list=StaffManager.getStaffOptions();
        for(Object[] row:list) combo.addItem(row[0]+" - "+row[1]+" ("+row[2]+")");
        return combo;
    }

    private int selectedId(JComboBox<String> combo) {
        String value=(String)combo.getSelectedItem();
        if(value==null||value.equals("Unassigned")) return 0;
        try { return Integer.parseInt(value.substring(0,value.indexOf(" - "))); }
        catch(Exception e){return 0;}
    }

    private int selectedComplaintId() {
        int row=complaintTable.getSelectedRow();
        if(row<0){warnSelect();return -1;}
        return (Integer)complaintModel.getValueAt(row,0);
    }

    private int findRow(DefaultTableModel model,int id) {
        for(int i=0;i<model.getRowCount();i++) if(((Integer)model.getValueAt(i,0))==id) return i;
        return -1;
    }

    private void refreshAll() {
        refreshComplaints(); refreshStaff(); refreshRooms(); refreshTasks(); refreshExpenses();
    }

    private void refreshComplaints() {
        if(complaintModel==null) return;
        try {
            complaintModel.setRowCount(0);
            String search=searchField==null?"":searchField.getText().trim();
            String status=statusFilter==null?"All":(String)statusFilter.getSelectedItem();
            String category=categoryFilter==null?"All":(String)categoryFilter.getSelectedItem();
            String priority=priorityFilter==null?"All":(String)priorityFilter.getSelectedItem();
            for(Object[] row:ComplaintManager.getComplaints(search,status,category,priority)) complaintModel.addRow(row);
        } catch(Exception ex){showError(ex);}
    }

    private void refreshStaff() {
        if(staffModel==null)return;
        try {staffModel.setRowCount(0);for(Object[] row:StaffManager.getStaff())staffModel.addRow(row);}catch(Exception ex){showError(ex);}
    }
    private void refreshRooms() {
        if(roomModel==null)return;
        try {roomModel.setRowCount(0);for(Object[] row:RoomManager.getRooms())roomModel.addRow(row);}catch(Exception ex){showError(ex);}
    }
    private void refreshTasks() {
        if(taskModel==null)return;
        try {taskModel.setRowCount(0);for(Object[] row:MaintenanceManager.getTasks())taskModel.addRow(row);}catch(Exception ex){showError(ex);}
    }
    private void refreshExpenses() {
        if(expenseModel==null)return;
        try {expenseModel.setRowCount(0);for(Object[] row:MaintenanceManager.getExpenses())expenseModel.addRow(row);}catch(Exception ex){showError(ex);}
    }

    private DefaultTableModel model(String[] columns) {
        return new DefaultTableModel(columns,0) {
            public boolean isCellEditable(int row,int column){return false;}
        };
    }
    private void warnSelect(){JOptionPane.showMessageDialog(this,"Select a row first.");}
    private void showError(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
}
