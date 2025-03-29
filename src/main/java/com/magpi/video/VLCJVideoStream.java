package com.magpi.video;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class for video streaming and recording using OpenCV for webcam integration
 */
public class VLCJVideoStream {
    // Default location where videos are saved
    public static final String saveLocation =
            Paths.get(System.getProperty("user.home"), "MagPi", "Videos").toString();

    static {
        // Load the OpenCV native library
        try {
            nu.pattern.OpenCV.loadLocally();
            System.out.println("OpenCV loaded successfully: " + Core.VERSION);
        } catch (Exception e) {
            System.err.println("Error loading OpenCV: " + e.getMessage());
            e.printStackTrace();
        }

        // Create videos directory if it doesn't exist
        File directory = new File(saveLocation);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    private JFrame frame;
    private JLabel videoLabel;
    private JButton startButton;
    private JButton stopButton;
    private JLabel timerLabel;
    private boolean isRunning = false;
    private boolean isRecording = false;
    private VideoCapture camera;
    private VideoWriter videoWriter;
    private Thread cameraThread;
    private Thread timerThread;
    private int partNumber = 0; // Will be set by caller
    private long recordingStartTime = 0;

    /**
     * Creates a new video stream
     */
    public VLCJVideoStream() {
        createUI();
    }

    /**
     * Creates a new video stream for a specific part
     * @param partNumber The part number to associate with recorded videos
     */
    public VLCJVideoStream(int partNumber) {
        this.partNumber = partNumber;
        createUI();
    }

    private void createUI() {
        frame = new JFrame("Webcam Stream");
        frame.setSize(320, 240);
        frame.setLocation(
                Toolkit.getDefaultToolkit().getScreenSize().width - frame.getWidth()-20,
                Toolkit.getDefaultToolkit().getScreenSize().height - frame.getHeight() -70
        );
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopCamera();
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Video panel
        videoLabel = new JLabel();
        videoLabel.setBackground(Color.BLACK);
        videoLabel.setOpaque(true);
        videoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        videoLabel.setPreferredSize(new Dimension(320, 240));

        // Status panel for recording information
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(0, 0, 0, 80)); // Semi-transparent background

        timerLabel = new JLabel("00:00:00");
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        timerLabel.setVisible(false); // Hidden until recording starts

        JLabel partLabel = new JLabel("Part #" + partNumber);
        partLabel.setForeground(Color.WHITE);
        partLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        statusPanel.add(partLabel);
        statusPanel.add(timerLabel);

        // Place the status panel over the video with a layered pane
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(320, 240));

        videoLabel.setBounds(0, 0, 320, 240);
        layeredPane.add(videoLabel, JLayeredPane.DEFAULT_LAYER);

        statusPanel.setBounds(5, 5, 200, 30);
        layeredPane.add(statusPanel, JLayeredPane.PALETTE_LAYER);

        // Control panel
        JPanel controlPanel = new JPanel();
        startButton = new JButton("Start Recording");
        stopButton = new JButton("Stop Recording");
        stopButton.setEnabled(false);

        startButton.addActionListener(e -> startRecording());
        stopButton.addActionListener(e -> stopRecording());

        controlPanel.add(startButton);
        controlPanel.add(stopButton);

        mainPanel.add(layeredPane, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.pack();
    }

    /**
     * Starts the webcam stream
     */
    private void startCamera() {
        camera = new VideoCapture();

        // Try to open the default camera (index 0)
        if (!camera.open(0)) {
            JOptionPane.showMessageDialog(frame,
                    "Failed to open webcam",
                    "Camera Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        isRunning = true;

        // Start a thread to continuously update the video feed
        cameraThread = new Thread(() -> {
            Mat frame = new Mat();

            while (isRunning) {
                if (camera.read(frame)) {
                    if (!frame.empty()) {
                        BufferedImage image = matToBufferedImage(frame);
                        updateImageUI(image);

                        // Also write to video file if recording
                        if (isRecording && videoWriter != null && videoWriter.isOpened()) {
                            videoWriter.write(frame);
                        }
                    }
                }

                try {
                    Thread.sleep(33); // ~30 fps
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (camera.isOpened()) {
                camera.release();
            }
        });

        cameraThread.setDaemon(true);
        cameraThread.start();
    }

    private void updateImageUI(BufferedImage image) {
        if (image != null) {
            ImageIcon icon = new ImageIcon(image);
            SwingUtilities.invokeLater(() -> videoLabel.setIcon(icon));
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

    /**
     * Updates the timer display with the elapsed recording time
     */
    private void updateTimer() {
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }

        timerThread = new Thread(() -> {
            timerLabel.setVisible(true);
            recordingStartTime = System.currentTimeMillis();

            while (isRecording) {
                long elapsedTimeMillis = System.currentTimeMillis() - recordingStartTime;
                updateTimerLabel(elapsedTimeMillis);

                try {
                    Thread.sleep(1000); // Update every second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            timerLabel.setVisible(false);
        });

        timerThread.setDaemon(true);
        timerThread.start();
    }

    /**
     * Formats and displays the elapsed time
     */
    private void updateTimerLabel(long elapsedMillis) {
        long seconds = (elapsedMillis / 1000) % 60;
        long minutes = (elapsedMillis / (1000 * 60)) % 60;
        long hours = (elapsedMillis / (1000 * 60 * 60)) % 24;

        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        SwingUtilities.invokeLater(() -> timerLabel.setText(timeString));
    }

    /**
     * Starts video recording
     */
    private void startRecording() {
        if (camera == null || !camera.isOpened()) {
            startCamera();
        }

        if (!isRecording && camera != null && camera.isOpened()) {
            // Generate filename with part number and timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("Part%d_%s.avi", partNumber, timestamp);
            File outputFile = new File(saveLocation, filename);

            // Get camera properties
            double width = camera.get(Videoio.CAP_PROP_FRAME_WIDTH);
            double height = camera.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            double fps = camera.get(Videoio.CAP_PROP_FPS);
            if (fps <= 0) fps = 30.0; // Default to 30fps if not available

            // Create VideoWriter
            videoWriter = new VideoWriter();
            videoWriter.open(outputFile.getAbsolutePath(),
                    VideoWriter.fourcc('M', 'J', 'P', 'G'),
                    fps,
                    new org.opencv.core.Size(width, height),
                    true);

            if (videoWriter.isOpened()) {
                isRecording = true;
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                System.out.println("Recording started: " + outputFile.getAbsolutePath());

                // Start the recording timer
                updateTimer();

                // Show notification with the recording path
                timerLabel.setForeground(Color.RED); // Red color to indicate recording
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Failed to create video writer",
                        "Recording Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Stops video recording
     */
    private void stopRecording() {
        if (isRecording && videoWriter != null) {
            isRecording = false;
            videoWriter.release();
            videoWriter = null;

            // Stop the timer thread
            if (timerThread != null && timerThread.isAlive()) {
                timerThread.interrupt();
                timerThread = null;
            }

            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            timerLabel.setVisible(false);
            System.out.println("Recording stopped");
        }
    }

    /**
     * Stops the camera and releases resources
     */
    private void stopCamera() {
        isRunning = false;

        if (isRecording) {
            stopRecording();
        }

        if (cameraThread != null) {
            cameraThread.interrupt();
            try {
                cameraThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (camera != null && camera.isOpened()) {
            camera.release();
        }
    }

    /**
     * Shows the video stream window
     */
    public void show() {
        frame.setVisible(true);
        startCamera();
    }

    /**
     * For testing the video stream standalone
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            VLCJVideoStream stream = new VLCJVideoStream(999); // Test with part 999
            stream.show();
        });
    }
}