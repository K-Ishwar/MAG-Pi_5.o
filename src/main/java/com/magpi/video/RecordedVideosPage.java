package com.magpi.video;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Page that displays recorded videos and allows playback
 */
public class RecordedVideosPage extends JFrame {
    private final String videoDirectory;
    private JList<String> videoList;
    private DefaultListModel<String> listModel;
    private JLabel videoDisplayLabel;
    private JButton playButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JLabel statusLabel;

    // For video playback
    private VideoCapture videoCapture;
    private boolean isPlaying = false;
    private Thread playbackThread;

    static {
        // Load OpenCV native library
        try {
            nu.pattern.OpenCV.loadLocally();
            System.out.println("OpenCV loaded successfully: " + Core.VERSION);
        } catch (Exception e) {
            System.err.println("Error loading OpenCV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create a new RecordedVideosPage
     * @param videoDirectory The directory where videos are stored
     */
    public RecordedVideosPage(String videoDirectory) {
        super("Recorded Videos");
        this.videoDirectory = videoDirectory;

        initializeUI();
        loadVideoFiles();

        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Clean up resources when the window is closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Create list of videos with part number extraction
        listModel = new DefaultListModel<>();
        videoList = new JList<>(listModel);
        videoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        videoList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                String filename = (String) value;
                // Extract part number from filename (Part123_20230615_120000.avi)
                Pattern pattern = Pattern.compile("Part(\\d+)_.*");
                Matcher matcher = pattern.matcher(filename);

                if (matcher.find()) {
                    String partNumber = matcher.group(1);
                    setText("Part " + partNumber + " - " + filename);
                }

                return this;
            }
        });

        // Add scrollable list to the left side
        JScrollPane scrollPane = new JScrollPane(videoList);
        scrollPane.setPreferredSize(new Dimension(300, getHeight()));
        add(scrollPane, BorderLayout.WEST);

        // Create video panel for playback
        JPanel videoPanel = new JPanel(new BorderLayout());
        videoPanel.setBackground(Color.BLACK);

        videoDisplayLabel = new JLabel("Select a video to play", JLabel.CENTER);
        videoDisplayLabel.setForeground(Color.WHITE);
        videoDisplayLabel.setFont(videoDisplayLabel.getFont().deriveFont(Font.BOLD, 16));
        videoPanel.add(videoDisplayLabel, BorderLayout.CENTER);

        // Create status label below video
        statusLabel = new JLabel("Ready", JLabel.CENTER);
        videoPanel.add(statusLabel, BorderLayout.SOUTH);

        add(videoPanel, BorderLayout.CENTER);

        // Add control buttons at the bottom
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        playButton = new JButton("Play");
        pauseButton = new JButton("Pause");
        stopButton = new JButton("Stop");
        deleteButton = new JButton("Delete");
        refreshButton = new JButton("Refresh");

        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);

        playButton.addActionListener(e -> playSelectedVideo());
        pauseButton.addActionListener(e -> pausePlayback());
        stopButton.addActionListener(e -> stopPlayback());
        deleteButton.addActionListener(e -> deleteSelectedVideo());
        refreshButton.addActionListener(e -> loadVideoFiles());

        controlPanel.add(playButton);
        controlPanel.add(pauseButton);
        controlPanel.add(stopButton);
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

        if (listModel.isEmpty()) {
            statusLabel.setText("No videos found in " + videoDirectory);
        } else {
            statusLabel.setText(listModel.size() + " videos found");
        }
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }

        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] bytes = new byte[bufferSize];
        mat.get(0, 0, bytes);

        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(bytes, 0, targetPixels, 0, bytes.length);

        return image;
    }

    private void updateImageUI(BufferedImage image) {
        if (image != null) {
            ImageIcon icon = new ImageIcon(image);
            videoDisplayLabel.setText("");
            videoDisplayLabel.setIcon(icon);
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

        // Stop any existing playback
        stopPlayback();

        // Start new playback
        File videoFile = new File(videoDirectory, selectedVideo);
        videoCapture = new VideoCapture(videoFile.getAbsolutePath());

        if (!videoCapture.isOpened()) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open video file: " + selectedVideo,
                    "Playback Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get video details
        double frameCount = videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);
        double fps = videoCapture.get(Videoio.CAP_PROP_FPS);

        if (fps <= 0) fps = 30.0; // Use default if fps not available
        final int frameDelay = (int) (1000 / fps);

        isPlaying = true;

        // Update UI controls
        playButton.setEnabled(false);
        pauseButton.setEnabled(true);
        stopButton.setEnabled(true);

        statusLabel.setText("Playing: " + selectedVideo);

        // Start playback thread
        double finalFps = fps;
        playbackThread = new Thread(() -> {
            Mat frame = new Mat();
            int frameNumber = 0;

            while (isPlaying && videoCapture.isOpened()) {
                if (videoCapture.read(frame)) {
                    if (!frame.empty()) {
                        BufferedImage image = matToBufferedImage(frame);
                        updateImageUI(image);
                        frameNumber++;

                        // Update status with progress
                        if (frameCount > 0) {
                            final int currentFrame = frameNumber;
                            final double duration = frameCount / finalFps;
                            final double currentTime = frameNumber / finalFps;

                            SwingUtilities.invokeLater(() ->
                                    statusLabel.setText(String.format("Playing: %s - %.1f/%.1f sec (Frame %d/%d)",
                                            selectedVideo, currentTime, duration, currentFrame, (int)frameCount)));
                        }
                    } else {
                        break; // End of video or error
                    }
                } else {
                    break; // End of video or error
                }

                try {
                    Thread.sleep(frameDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // Clean up when playback ends
            if (videoCapture.isOpened()) {
                // If we reached the end normally
                SwingUtilities.invokeLater(() -> {
                    playButton.setEnabled(true);
                    pauseButton.setEnabled(false);
                    stopButton.setEnabled(false);
                    statusLabel.setText("Playback complete: " + selectedVideo);
                });

                videoCapture.release();
            }
        });

        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    private void pausePlayback() {
        if (isPlaying) {
            isPlaying = false;
            playButton.setEnabled(true);
            pauseButton.setEnabled(false);
            statusLabel.setText("Paused");
        }
    }

    private void stopPlayback() {
        isPlaying = false;

        if (playbackThread != null) {
            playbackThread.interrupt();
            try {
                playbackThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            playbackThread = null;
        }

        if (videoCapture != null && videoCapture.isOpened()) {
            videoCapture.release();
            videoCapture = null;
        }

        // Reset UI
        videoDisplayLabel.setIcon(null);
        videoDisplayLabel.setText("Select a video to play");
        playButton.setEnabled(true);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);
        statusLabel.setText("Ready");
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
            // Stop playback if this is the current video
            stopPlayback();

            File videoFile = new File(videoDirectory, selectedVideo);
            if (videoFile.exists() && videoFile.delete()) {
                loadVideoFiles(); // Refresh the list
                statusLabel.setText("Deleted: " + selectedVideo);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete the video file",
                        "Deletion Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Clean up resources when the window is closed
     */
    public void cleanup() {
        stopPlayback();
    }

    /**
     * Main method for testing
     */
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