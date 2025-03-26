package com.magpi.video;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

/**
 * Placeholder for a page that displays recorded videos.
 * In a real application, this would show video files and allow playback.
 */
public class RecordedVideosPage extends JFrame {
    private final String videoDirectory;
    private JList<String> videoList;
    private DefaultListModel<String> listModel;
    
    /**
     * Create a new RecordedVideosPage
     * @param videoDirectory The directory where videos are stored
     */
    public RecordedVideosPage(String videoDirectory) {
        super("Recorded Videos");
        this.videoDirectory = videoDirectory;
        
        initializeUI();
        loadVideoFiles();
        
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Create list of videos
        listModel = new DefaultListModel<>();
        videoList = new JList<>(listModel);
        videoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add scrollable list to the left side
        JScrollPane scrollPane = new JScrollPane(videoList);
        scrollPane.setPreferredSize(new Dimension(300, getHeight()));
        add(scrollPane, BorderLayout.WEST);
        
        // Create a placeholder panel for video playback
        JPanel videoPanel = new JPanel(new BorderLayout());
        videoPanel.setBackground(Color.BLACK);
        videoPanel.add(new JLabel("Select a video to play", JLabel.CENTER), BorderLayout.CENTER);
        add(videoPanel, BorderLayout.CENTER);
        
        // Add control buttons at the bottom
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton playButton = new JButton("Play");
        JButton deleteButton = new JButton("Delete");
        JButton refreshButton = new JButton("Refresh");
        
        playButton.addActionListener(e -> playSelectedVideo());
        deleteButton.addActionListener(e -> deleteSelectedVideo());
        refreshButton.addActionListener(e -> loadVideoFiles());
        
        controlPanel.add(playButton);
        controlPanel.add(deleteButton);
        controlPanel.add(refreshButton);
        
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    private void loadVideoFiles() {
        File directory = new File(videoDirectory);
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        listModel.clear();
        
        File[] videoFiles = directory.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".mp4") || 
                name.toLowerCase().endsWith(".avi") ||
                name.toLowerCase().endsWith(".mov"));
        
        if (videoFiles != null) {
            Arrays.sort(videoFiles, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            
            for (File file : videoFiles) {
                listModel.addElement(file.getName());
            }
        }
    }
    
    private void playSelectedVideo() {
        String selectedVideo = videoList.getSelectedValue();
        if (selectedVideo == null) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a video to play", 
                    "No Selection", 
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // In a real application, this would play the selected video
        JOptionPane.showMessageDialog(this, 
                "Playing: " + selectedVideo, 
                "Video Playback", 
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void deleteSelectedVideo() {
        String selectedVideo = videoList.getSelectedValue();
        if (selectedVideo == null) {
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete " + selectedVideo + "?", 
                "Confirm Deletion", 
                JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            File videoFile = new File(videoDirectory, selectedVideo);
            if (videoFile.exists() && videoFile.delete()) {
                loadVideoFiles(); // Refresh the list
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Failed to delete the video file", 
                        "Deletion Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Perform cleanup when the window is closed
     */
    public void cleanup() {
        // In a real application, this would release any resources
        System.out.println("Cleaning up resources...");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            RecordedVideosPage page = new RecordedVideosPage(
                    VLCJVideoStream.saveLocation);
            page.setVisible(true);
        });
    }
} 