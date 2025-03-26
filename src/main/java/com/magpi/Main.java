package com.magpi;

import com.magpi.model.TestSession;
import com.magpi.ui.HistoryPage;
import com.magpi.ui.LoginPage;
import com.magpi.ui.TablePage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main class for the application
 */
public class Main {
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private LoginPage loginPage;
    private TablePage tablePage;
    private HistoryPage historyPage;
    private TestSession session;
    
    /**
     * Creates and initializes the application
     */
    public Main() {
        // Initialize the session
        session = new TestSession();
        
        // Set up the main frame
        initializeFrame();
        
        // Create the panels
        createPanels();
        
        // Add panels to the tabbed pane
        createTabbedPane();
        
        // Create the menu bar
        createMenuBar();
        
        // Show the frame
        frame.setVisible(true);
    }
    
    private void initializeFrame() {
        frame = new JFrame("MAG-Pi: Magnetic Particle Inspection System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLayout(new BorderLayout());
        
        // Handle the window closing event to clean up resources
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (tablePage != null) {
                    tablePage.shutdown();
                }
            }
        });
    }
    
    private void createPanels() {
        // Create login page
        loginPage = new LoginPage();
        loginPage.addLoginButton(e -> handleLogin());
        
        // Table page and history page are created after login
    }
    
    private void createTabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Login", loginPage);
        
        frame.add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setLayout(new BorderLayout());
        menuBar.setBackground(Color.DARK_GRAY);
        
        // Add Logo and Brand Name (left side)
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoPanel.setOpaque(false);
        
        // You would use actual image resources for a real application
        // For now, using a text placeholder
        JLabel logoLabel = new JLabel("MAG-Pi");
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel companyLabel = new JLabel("Vinze Magnafield Controls");
        companyLabel.setForeground(new Color(209, 125, 88));
        companyLabel.setFont(new Font("Arial", Font.BOLD, 15));
        
        logoPanel.add(logoLabel);
        logoPanel.add(companyLabel);
        menuBar.add(logoPanel, BorderLayout.WEST);
        
        // Add navigation buttons (right side)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton homeButton = new JButton("Home");
        homeButton.setBackground(Color.LIGHT_GRAY);
        homeButton.addActionListener(e -> tabbedPane.setSelectedIndex(0));
        
        JButton historyButton = new JButton("View History");
        historyButton.setBackground(Color.CYAN);
        historyButton.addActionListener(e -> {
            if (historyPage != null) {
                tabbedPane.setSelectedIndex(tabbedPane.indexOfComponent(historyPage));
            }
        });
        
        JButton aboutButton = new JButton("About");
        aboutButton.setBackground(Color.ORANGE);
        aboutButton.addActionListener(e -> showAboutDialog());
        
        buttonPanel.add(homeButton);
        buttonPanel.add(historyButton);
        buttonPanel.add(aboutButton);
        
        menuBar.add(buttonPanel, BorderLayout.EAST);
        frame.setJMenuBar(menuBar);
    }
    
    private void handleLogin() {
        // Update session with login info
        if (!loginPage.updateSessionWithLoginInfo(session)) {
            return; // Login validation failed
        }
        
        // Create the table page and history page
        tablePage = new TablePage(session);
        historyPage = new HistoryPage(session);
        
        // Add reference to the history page in the session
        session.setHistoryPanel(historyPage);
        
        // Add pages to the tabbed pane
        tabbedPane.addTab("Table", tablePage);
        tabbedPane.addTab("History", historyPage);
        
        // Navigate to the table page
        tabbedPane.setSelectedIndex(1);
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(frame,
                "MAG-Pi: Magnetic Particle Inspection System\n" +
                "Version 1.0\n" +
                "© 2023 Vinze Magnafield Controls\n\n" +
                "A data acquisition system for magnetic particle inspection.",
                "About MAG-Pi",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Main method to start the application
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Use the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create the application on the event dispatch thread
        SwingUtilities.invokeLater(() -> new Main());
    }
} 