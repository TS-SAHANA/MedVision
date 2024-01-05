import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class DiseaseForecastingApp {
    private JFrame frame;
    private JTextField symptomsField;
    private JTextField diseaseField;
    private JTextField solutionField;
    private JTextArea resultArea;
    private Connection conn;

    public DiseaseForecastingApp() {
        initialize();
        connectToDatabase();
    }

    private void initialize() {
        // Create the main frame
        frame = new JFrame("MedVision");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create input panel with a grid layout
        JPanel inputPanel = new JPanel(new GridLayout(8, 4, 10, 10));

        // Initialize text fields
        symptomsField = new JTextField(20);
        diseaseField = new JTextField(20);
        solutionField = new JTextField(20);

        // Create buttons for different operations
        JButton predictButton = new JButton("Predict Disease");
        JButton insertButton = new JButton("Insert Record");
        JButton updateButton = new JButton("Update Record");
        JButton deleteButton = new JButton("Delete Record");
        JButton clearButton = new JButton("Clear");
        JButton displayAllButton = new JButton("Display Records");
  
        // Attach action listeners to buttons  
        predictButton.addActionListener(this::predictDisease);
        insertButton.addActionListener(this::insertRecord);
        updateButton.addActionListener(this::updateRecord);
        deleteButton.addActionListener(this::deleteRecord);
        displayAllButton.addActionListener(e -> displayAllRecords());
        clearButton.addActionListener(e -> clearFields());

        // Add labels, text fields, and buttons to the input panel
        inputPanel.add(new JLabel("Enter Symptoms: "));
        inputPanel.add(symptomsField);
        inputPanel.add(new JLabel("Disease: "));
        inputPanel.add(diseaseField);
        inputPanel.add(new JLabel("Solution: "));
        inputPanel.add(solutionField);
        inputPanel.add(predictButton);
        inputPanel.add(insertButton);
        inputPanel.add(updateButton);
        inputPanel.add(deleteButton);
        inputPanel.add(clearButton);
        inputPanel.add(displayAllButton);

         // Add the input panel to the main frame
        frame.add(inputPanel, BorderLayout.NORTH);

        // Create a text area for displaying results
        resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Set the frame to be visible
        frame.setVisible(true);
    }

    private void connectToDatabase() {
        try {
            // Load the MySQL JDBC driver and establish a connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/disease_symptoms", "root", "TooManyCats@Home");
        } catch (Exception e) {
           // Show an error dialog and exit the program if connection fails
            showErrorDialog("Error connecting to the database.");
            System.exit(1);
        }
    }

    private void displayResults(ResultSet resultSet) throws SQLException {
     // Process the result set and display information in the text area
    StringBuilder resultText = new StringBuilder();
    while (resultSet.next()) {
        String disease = resultSet.getString("disease");
        String solution = resultSet.getString("solution");
        resultText.append("Disease: ").append(disease).append("\nSolution: ").append(solution).append("\n\n");
    }

    // Display the results or a message indicating no matching diseases
    if (resultText.length() == 0) {
        resultArea.setText("No matching diseases found.");
    } else {
        resultArea.setText(resultText.toString());
    }
}

private void displayAllRecords() {
    // Display all records from the database
    try {
        String query = "SELECT * FROM disease_solution";
        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet resultSet = pstmt.executeQuery()) {
            displayResults(resultSet);
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
        showErrorDialog("Error displaying all records.");
    }
}


    private void predictDisease(ActionEvent event) {
    // Predict disease based on entered symptoms
    String symptoms = symptomsField.getText();
    if (symptoms.isEmpty()) {
        showErrorDialog("Please enter symptoms.");
        return;
    }

    try {
        String query = "SELECT disease, solution FROM disease_solution WHERE physical_symptoms LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "%" + symptoms + "%");
            try (ResultSet resultSet = pstmt.executeQuery()) {
                displayResults(resultSet);
            }
        }

    } catch (SQLException ex) {
        ex.printStackTrace();
        showErrorDialog("Error querying the database.");
    } 
}


    private void insertRecord(ActionEvent event) {
        // Insert a new record into the database
        String symptoms = symptomsField.getText();
        String disease = diseaseField.getText();
        String solution = solutionField.getText();

        if (symptoms.isEmpty() || disease.isEmpty() || solution.isEmpty()) {
            showErrorDialog("Please enter all fields for record insertion.");
            return;
        }

        try {
            String query = "INSERT INTO disease_solution (physical_symptoms, disease, solution) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, symptoms);
                pstmt.setString(2, disease);
                pstmt.setString(3, solution);
                pstmt.executeUpdate();

                showInfoDialog("Record inserted successfully.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorDialog("Error inserting the record.");
        } finally {
            clearFields();
        }
    }

    private void updateRecord(ActionEvent event) {
    // Update an existing record in the database
    String symptoms = symptomsField.getText();
    String disease = diseaseField.getText();
    String solution = solutionField.getText();

        if (symptoms.isEmpty() || solution.isEmpty()) {
        showErrorDialog("Please enter symptoms or solution for record update.");
        return;
    }

    try {
        String query = "UPDATE disease_solution SET solution = ? WHERE physical_symptoms LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, solution);
            pstmt.setString(2, "%" + symptoms + "%");

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                showInfoDialog("Record updated successfully.");
            } else {
                showErrorDialog("No matching records found for update.");
            }
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
        showErrorDialog("Error updating the record.");
    } finally {
            clearFields();
        }
}


    private void deleteRecord(ActionEvent event) {
    // Delete a record from the database
    String disease = diseaseField.getText();

    if (disease.isEmpty()) {
        showErrorDialog("Please enter a disease for record deletion.");
        return;
    }

    try {
        String query = "DELETE FROM disease_solution WHERE disease LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "%" + disease + "%");

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                showInfoDialog("Record(s) deleted successfully.");
            } else {
                showErrorDialog("No matching records found for deletion.");
            }
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
        showErrorDialog("Error deleting the record.");
    } finally {
            clearFields();
        }
}

private void clearFields() {
        // Clear input fields and result area
        symptomsField.setText("");
        diseaseField.setText("");
        solutionField.setText("");
        resultArea.setText("");
    }

    private void showErrorDialog(String message) {
        // Display an error dialog with the specified message
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfoDialog(String message) {
        // Display an information dialog with the specified message
        JOptionPane.showMessageDialog(frame, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        // Run the application on the event dispatch thread
        SwingUtilities.invokeLater(DiseaseForecastingApp::new);
    }
}