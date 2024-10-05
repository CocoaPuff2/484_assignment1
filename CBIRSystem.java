import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CBIRSystem extends JFrame {
    private Map<String, int[]> histograms = new HashMap<>();
    private JButton loadButton; // Button (load the query image)
    private BufferedImage queryImage; // BufferedImage (hold the loaded image)
    private static final int IMAGES_PER_PAGE = 20; // Max number of images per page
    private static final int MAX_PAGES = 5; // Max number of pages
    private int currentPage = 0; // Current page number
    private JLabel pageLabel; // Display page number
    private JPanel queryImagePanel; // Panel --> Display the selected image (right side)
    private JPanel imageGridPanel; // Display the grid of images (left side)
    private JButton previousButton; // Button --> previous page
    private JButton nextButton; // Button --> next page
    private String[] imagePaths; // Array of image file paths
    private JLabel queryImageLabel; // Label --> Display the selected/query image

    private String selectedMethod = "Intensity"; // Default histogram method

    public CBIRSystem() {
        setTitle("Content-Based Image Retrieval System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create the main panel (holds the main content)
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        // Dropdown menu for method selection
        JPanel methodPanel = new JPanel(); // Create a new panel for dropdown
        String[] methods = {"Intensity Method", "Color Code Method"};
        JComboBox<String> methodComboBox = new JComboBox<>(methods);
        methodComboBox.setFont(new Font("Serif", Font.BOLD, 18));
        methodComboBox.addActionListener(e -> {
            selectedMethod = (String) methodComboBox.getSelectedItem(); // Update the selected method
        });
        methodPanel.add(methodComboBox); // Add dropdown to panel
        mainPanel.add(methodPanel, BorderLayout.NORTH); // Add method panel to main panel

        // Load images from the "images" directory
        loadImages();

        // LEFT SIDE: Create image grid panel (with a black border)
        imageGridPanel = new JPanel(new GridLayout(0, 5)); // 5 images per row
        imageGridPanel.setBorder(LineBorder.createBlackLineBorder());
        mainPanel.add(imageGridPanel, BorderLayout.WEST);

        // RIGHT SIDE: Create selected image panel (with header)
        queryImagePanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Query Image", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Serif", Font.BOLD, 30));

        queryImageLabel = new JLabel();
        queryImageLabel.setPreferredSize(new Dimension(300, 200)); // Sets preferred size for selected image
        queryImageLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center selected image
        queryImagePanel.add(headerLabel, BorderLayout.NORTH);
        queryImagePanel.add(queryImageLabel, BorderLayout.CENTER);
        mainPanel.add(queryImagePanel, BorderLayout.EAST);

        // Navigation panel (page control)
        JPanel navigationPanel = new JPanel();
        pageLabel = new JLabel("Page: " + (currentPage + 1));
        pageLabel.setFont(new Font("Serif", Font.PLAIN, 17));

        previousButton = new JButton("Previous");
        previousButton.setFont(new Font("Serif", Font.PLAIN, 15));
        nextButton = new JButton("Next");
        nextButton.setFont(new Font("Serif", Font.PLAIN, 15));

        // Action --> Previous button
        previousButton.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                displayImages();
            }
        });

        // Action --> Next button
        nextButton.addActionListener(e -> {
            if (currentPage < (Math.min(MAX_PAGES, (imagePaths.length + IMAGES_PER_PAGE - 1) / IMAGES_PER_PAGE) - 1)) {
                currentPage++;
                displayImages();
            }
        });

        // Add buttons and page label to the navigation panel
        navigationPanel.add(previousButton);
        navigationPanel.add(pageLabel);
        navigationPanel.add(nextButton);
        mainPanel.add(navigationPanel, BorderLayout.SOUTH);

        // Display the first page of images
        displayImages();
        setVisible(true);
    }

    // Load image file paths from the "images" directory
    private void loadImages() {
        File imagesDir = new File("images"); // Path to the images folder
        if (imagesDir.exists() && imagesDir.isDirectory()) {
            imagePaths = imagesDir.list((dir, name) -> name.endsWith(".jpg"));
        }
    }

    // Display the images for the current page in the image grid panel
    private void displayImages() {
        imageGridPanel.removeAll(); // Clear current images
        int startIndex = currentPage * IMAGES_PER_PAGE;
        int endIndex = Math.min(startIndex + IMAGES_PER_PAGE, imagePaths.length);

        // Update page label to reflect the current page number
        pageLabel.setText("Page: " + (currentPage + 1));

        // Load images into the grid layout with spacing for better UI
        for (int i = startIndex; i < endIndex; i++) {
            JLabel imageLabel = new JLabel(resizeImage("images/" + imagePaths[i], 80, 80)); // Resize to a smaller size
            imageLabel.setBorder(new EmptyBorder(5, 5, 5, 5)); // Add spacing around grid images
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the grid images

            // Add a mouse listener to select the query image
            final String imagePath = imagePaths[i]; // Make image path final for use in inner class
            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Set the selected image as the query image
                    queryImageLabel.setIcon(resizeImage("images/" + imagePath, 300, 200));

                    try {
                        // Load the query image
                        queryImage = ImageIO.read(new File("images/" + imagePath));

                        // Calculate the histogram based on the selected method
                        int[] histogram;
                        if (selectedMethod.equals("Intensity Method")) {
                            histogram = Histograms.intensityMethod(queryImage);
                        } else {
                            histogram = Histograms.colorCodeMethod(queryImage);
                        }

                        // Store the histogram of the query image
                        histograms.put(imagePath, histogram);
                        // Sort the images based on distance from the query image
                        sortImages(histogram);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            imageGridPanel.add(imageLabel); // Add the image to the grid panel
        }

        imageGridPanel.revalidate(); // Refresh the grid panel
        imageGridPanel.repaint(); // Repaint the grid panel
    }

    // Sort images based on distance to the query image
    private void sortImages(int[] queryHistogram) {
        Arrays.sort(imagePaths, (path1, path2) -> {
            int[] histo1 = histograms.get(path1);
            int[] histo2 = histograms.get(path2);
            double distance1 = manhattanDistance(queryHistogram, histo1);
            double distance2 = manhattanDistance(queryHistogram, histo2);
            return Double.compare(distance1, distance2);
        });
        displayImages(); // Redisplay images after sorting
    }

    // Manhattan distance calculation
    public static double manhattanDistance(int[] histo1, int[] histo2) {
        double distance = 0;
        for (int i = 0; i < histo1.length; i++) {
            distance += Math.abs(histo1[i] - histo2[i]);
        }
        return distance;
    }

    // Method that resizes images while maintaining aspect ratio
    private Icon resizeImage(String imagePath, int width, int height) {
        ImageIcon icon = new ImageIcon(imagePath);
        Image img = icon.getImage(); // Get the original image
        Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH); // Scale the image
        return new ImageIcon(scaledImg); // Return the resized image as an Icon
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CBIRSystem::new);
    }
}
