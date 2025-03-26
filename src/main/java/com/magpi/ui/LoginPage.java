package com.magpi.ui;

import com.magpi.model.TestSession;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Panel for user login and session initialization
 */
public class LoginPage extends JPanel {
    private JTextField companyNameField;
    private JPasswordField passwordField;
    private JTextField machineIdField;
    private JTextField supervisorIdField;
    private JComboBox<String> operatorComboBox;
    private JComboBox<String> partDescriptionComboBox;
    private ArrayList<String> operatorsList;
    private ArrayList<String> partDescriptionList;
    
    /**
     * Creates a new login panel
     */
    public LoginPage() {
        initializeComponents();
        setupUI();
    }
    
    private void initializeComponents() {
        // Initialize the lists
        operatorsList = new ArrayList<>(Arrays.asList(
                "John Smith",
                "Maria Garcia",
                "David Johnson",
                "Sarah Wilson",
                "James Chen",
                "Lisa Anderson",
                "Michael Brown",
                "Emma Davis"
        ));
        
        partDescriptionList = new ArrayList<>(Arrays.asList(
                "Bearing Assembly - Type A",
                "Gear Box Component - GB101",
                "Control Panel Unit - CPU-X",
                "Motor Housing - MH200",
                "Cooling Fan - CF500",
                "Drive Shaft - DS-150",
                "Hydraulic Pump - HP300",
                "Electronic Control Board - ECB-V2",
                "Pressure Valve - PV100",
                "Filter Assembly - FA-201"
        ));
        
        // Initialize form components
        companyNameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        machineIdField = new JTextField(15);
        supervisorIdField = new JTextField(15);
        
        operatorComboBox = new JComboBox<>(operatorsList.toArray(new String[0]));
        operatorComboBox.setEditable(true);
        
        partDescriptionComboBox = new JComboBox<>(partDescriptionList.toArray(new String[0]));
        partDescriptionComboBox.setEditable(true);
    }
    
    private void setupUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Add background image (optional)
        setOpaque(false);
        
        // Add form fields
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(5, 5, 5, 5);
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Company Name
        formGbc.gridx = 0;
        formGbc.gridy = 0;
        formPanel.add(new JLabel("Company Name:"), formGbc);
        
        formGbc.gridx = 1;
        formPanel.add(companyNameField, formGbc);
        
        // Password
        formGbc.gridx = 0;
        formGbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), formGbc);
        
        formGbc.gridx = 1;
        formPanel.add(passwordField, formGbc);
        
        // Machine ID
        formGbc.gridx = 0;
        formGbc.gridy = 2;
        formPanel.add(new JLabel("Machine ID:"), formGbc);
        
        formGbc.gridx = 1;
        formPanel.add(machineIdField, formGbc);
        
        // Supervisor ID
        formGbc.gridx = 0;
        formGbc.gridy = 3;
        formPanel.add(new JLabel("Supervisor ID:"), formGbc);
        
        formGbc.gridx = 1;
        formPanel.add(supervisorIdField, formGbc);
        
        // Operator section
        formGbc.gridx = 0;
        formGbc.gridy = 4;
        formPanel.add(new JLabel("Operator Name:"), formGbc);
        
        JPanel operatorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        operatorComboBox.setPreferredSize(new Dimension(150, 25));
        operatorPanel.add(operatorComboBox);
        
        JButton addOperatorButton = new JButton("Add New");
        addOperatorButton.setMargin(new Insets(2, 8, 2, 8));
        addOperatorButton.addActionListener(e -> addNewOperator());
        operatorPanel.add(addOperatorButton);
        
        formGbc.gridx = 1;
        formPanel.add(operatorPanel, formGbc);
        
        // Part Description section
        formGbc.gridx = 0;
        formGbc.gridy = 5;
        formPanel.add(new JLabel("Part Description:"), formGbc);
        
        JPanel partDescPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        partDescriptionComboBox.setPreferredSize(new Dimension(150, 25));
        partDescPanel.add(partDescriptionComboBox);
        
        JButton addPartButton = new JButton("Add New");
        addPartButton.setMargin(new Insets(2, 8, 2, 8));
        addPartButton.addActionListener(e -> addNewPartDescription());
        partDescPanel.add(addPartButton);
        
        formGbc.gridx = 1;
        formPanel.add(partDescPanel, formGbc);
        
        // Add form panel to main panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(formPanel, gbc);
    }
    
    /**
     * Adds a login button with the provided action listener
     * @param loginAction The action to perform on login
     */
    public void addLoginButton(ActionListener loginAction) {
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(loginAction);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 10, 0);
        
        add(loginButton, gbc);
    }
    
    private void addNewOperator() {
        String newOperator = JOptionPane.showInputDialog(this, "Enter new operator name:");
        if (newOperator != null && !newOperator.trim().isEmpty()) {
            if (!operatorsList.contains(newOperator)) {
                operatorsList.add(newOperator);
                operatorComboBox.addItem(newOperator);
                operatorComboBox.setSelectedItem(newOperator);
            }
        }
    }
    
    private void addNewPartDescription() {
        String newPartDesc = JOptionPane.showInputDialog(this, "Enter new part description:");
        if (newPartDesc != null && !newPartDesc.trim().isEmpty()) {
            if (!partDescriptionList.contains(newPartDesc)) {
                partDescriptionList.add(newPartDesc);
                partDescriptionComboBox.addItem(newPartDesc);
                partDescriptionComboBox.setSelectedItem(newPartDesc);
            }
        }
    }
    
    /**
     * Updates the session with login information
     * @param session The session to update
     * @return true if all required fields are filled, false otherwise
     */
    public boolean updateSessionWithLoginInfo(TestSession session) {
        String companyName = companyNameField.getText().trim();
        String machineId = machineIdField.getText().trim();
        String supervisorId = supervisorIdField.getText().trim();
        String operatorName = (String) operatorComboBox.getSelectedItem();
        String partDescription = (String) partDescriptionComboBox.getSelectedItem();
        
        // Validate required fields
        if (companyName.isEmpty() || machineId.isEmpty() || 
            supervisorId.isEmpty() || operatorName == null || 
            operatorName.trim().isEmpty() || partDescription == null || 
            partDescription.trim().isEmpty()) {
            
            JOptionPane.showMessageDialog(this, 
                    "Please fill all required fields", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Update session
        session.setCompanyName(companyName);
        session.setMachineId(machineId);
        session.setSupervisorId(supervisorId);
        session.setOperatorName(operatorName);
        session.setPartDescription(partDescription);
        
        return true;
    }
}