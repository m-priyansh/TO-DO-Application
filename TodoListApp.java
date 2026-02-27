import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

class TodoItem implements Serializable {
    private static final long serialVersionUID = 1L;
    String task;
    LocalDateTime dateTime;
    boolean completed;
    
    public TodoItem(String task, LocalDateTime dateTime) {
        this.task = task;
        this.dateTime = dateTime;
        this.completed = false;
    }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
        return task + " - " + dateTime.format(formatter);
    }
}

public class TodoListApp extends JFrame {
    private DefaultListModel<TodoItem> todoModel;
    private JList<TodoItem> todoList;
    private JTextField inputField;
    private JSpinner dateSpinner;
    private JSpinner timeSpinner;
    private static final String SAVE_FILE = "todolist_data.ser";

    public TodoListApp() {
        setTitle("Todo List with Date & Time");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set modern color scheme
        Color darkGray = new Color(45, 45, 45);
        Color lightGray = new Color(240, 240, 240);
        Color mediumGray = new Color(180, 180, 180);
        
        getContentPane().setBackground(lightGray);

        todoModel = new DefaultListModel<>();
        todoList = new JList<>(todoModel);
        todoList.setFont(new Font("Arial", Font.PLAIN, 14));
        todoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        todoList.setBackground(Color.WHITE);
        todoList.setSelectionBackground(mediumGray);
        todoList.setSelectionForeground(Color.BLACK);
        
        // Add key listener for Delete key
        todoList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteTodo();
                }
            }
        });

        // Custom cell renderer for strikethrough effect
        todoList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                
                label.setBorder(new EmptyBorder(8, 10, 8, 10));
                
                if (value instanceof TodoItem) {
                    TodoItem item = (TodoItem) value;
                    if (item.completed) {
                        label.setText("<html><strike>" + item.toString() + "</strike></html>");
                        if (!isSelected) {
                            label.setForeground(new Color(150, 150, 150));
                            label.setBackground(new Color(250, 250, 250));
                        }
                    } else {
                        label.setText(item.toString());
                        if (!isSelected) {
                            label.setForeground(new Color(45, 45, 45));
                            label.setBackground(Color.WHITE);
                        }
                    }
                }
                
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(todoList);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1), 
            "My Tasks",
            0,
            0,
            new Font("Arial", Font.BOLD, 14),
            new Color(45, 45, 45)
        ));
        scrollPane.setBackground(Color.WHITE);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(new Color(240, 240, 240));
        
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBackground(new Color(240, 240, 240));
        
        JLabel taskLabel = new JLabel("New Task:");
        taskLabel.setFont(new Font("Arial", Font.BOLD, 12));
        taskLabel.setForeground(new Color(45, 45, 45));
        topPanel.add(taskLabel, BorderLayout.NORTH);
        
        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputField.addActionListener(e -> addTodo());
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        topPanel.add(inputField, BorderLayout.CENTER);

        // Date and Time panel
        JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        dateTimePanel.setBackground(new Color(240, 240, 240));
        
        // Date spinner
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "MMM dd, yyyy");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setPreferredSize(new Dimension(150, 30));
        
        // Time spinner
        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "hh:mm a");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setPreferredSize(new Dimension(100, 30));
        
        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 12));
        dateLabel.setForeground(new Color(45, 45, 45));
        
        JLabel timeLabel = new JLabel("Time:");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        timeLabel.setForeground(new Color(45, 45, 45));
        
        dateTimePanel.add(dateLabel);
        dateTimePanel.add(dateSpinner);
        dateTimePanel.add(timeLabel);
        dateTimePanel.add(timeSpinner);

        JButton addButton = new JButton("Add Task");
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.setBackground(new Color(45, 45, 45));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        addButton.addActionListener(e -> addTodo());

        inputPanel.add(topPanel, BorderLayout.NORTH);
        inputPanel.add(dateTimePanel, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.SOUTH);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        buttonPanel.setBackground(new Color(240, 240, 240));

        JButton completeButton = new JButton("Mark Complete");
        completeButton.setBackground(Color.WHITE);
        completeButton.setForeground(new Color(45, 45, 45));
        completeButton.setFocusPainted(false);
        completeButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        completeButton.addActionListener(e -> markComplete());

        JButton deleteButton = new JButton("Delete Task");
        deleteButton.setBackground(Color.WHITE);
        deleteButton.setForeground(new Color(45, 45, 45));
        deleteButton.setFocusPainted(false);
        deleteButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        deleteButton.addActionListener(e -> deleteTodo());

        JButton clearButton = new JButton("Clear All");
        clearButton.setBackground(Color.WHITE);
        clearButton.setForeground(new Color(45, 45, 45));
        clearButton.setFocusPainted(false);
        clearButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        clearButton.addActionListener(e -> clearAll());

        buttonPanel.add(completeButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        // Main layout
        setLayout(new BorderLayout(10, 10));
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load saved tasks or add sample tasks
        if (!loadTasks()) {
            addSampleTasks();
        }
        
        // Save tasks when window closes
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveTasks();
            }
        });
    }

    private void addTodo() {
        String task = inputField.getText().trim();
        if (!task.isEmpty()) {
            // Get date from date spinner
            java.util.Date date = (java.util.Date) dateSpinner.getValue();
            java.util.Date time = (java.util.Date) timeSpinner.getValue();
            
            // Combine date and time
            java.util.Calendar dateCal = java.util.Calendar.getInstance();
            dateCal.setTime(date);
            
            java.util.Calendar timeCal = java.util.Calendar.getInstance();
            timeCal.setTime(time);
            
            dateCal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY));
            dateCal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE));
            dateCal.set(java.util.Calendar.SECOND, 0);
            
            // Convert to LocalDateTime
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                dateCal.toInstant(), 
                java.time.ZoneId.systemDefault()
            );
            
            TodoItem item = new TodoItem(task, dateTime);
            todoModel.addElement(item);
            inputField.setText("");
            inputField.requestFocus();
            saveTasks(); // Auto-save after adding
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please enter a task!", 
                "Empty Task", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void markComplete() {
        int selectedIndex = todoList.getSelectedIndex();
        if (selectedIndex != -1) {
            TodoItem item = todoModel.getElementAt(selectedIndex);
            if (item.completed) {
                JOptionPane.showMessageDialog(this, 
                    "This task is already completed and cannot be unmarked!", 
                    "Already Completed", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                item.completed = true;
                todoList.repaint();
                saveTasks(); // Auto-save after marking complete
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a task to mark as complete!", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteTodo() {
        int selectedIndex = todoList.getSelectedIndex();
        if (selectedIndex != -1) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this task?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                todoModel.remove(selectedIndex);
                saveTasks(); // Auto-save after deleting
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a task to delete!", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearAll() {
        if (todoModel.size() > 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear all tasks?",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                todoModel.clear();
                saveTasks(); // Auto-save after clearing
            }
        }
    }

    private void addSampleTasks() {
        LocalDateTime now = LocalDateTime.now();
        
        TodoItem item1 = new TodoItem("Buy groceries", now.plusHours(2));
        TodoItem item2 = new TodoItem("Finish Java project", now.plusDays(1));
        TodoItem item3 = new TodoItem("Call dentist", now.plusDays(2).withHour(14).withMinute(30));
        
        todoModel.addElement(item1);
        todoModel.addElement(item2);
        todoModel.addElement(item3);
    }
    
    private void saveTasks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            ArrayList<TodoItem> tasks = new ArrayList<>();
            for (int i = 0; i < todoModel.size(); i++) {
                tasks.add(todoModel.getElementAt(i));
            }
            oos.writeObject(tasks);
        } catch (IOException e) {
            System.err.println("Error saving tasks: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private boolean loadTasks() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            return false;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            ArrayList<TodoItem> tasks = (ArrayList<TodoItem>) ois.readObject();
            for (TodoItem item : tasks) {
                todoModel.addElement(item);
            }
            return true;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading tasks: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            TodoListApp app = new TodoListApp();
            app.setVisible(true);
        });
    }
}