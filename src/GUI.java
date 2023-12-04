import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;

public class GUI extends JFrame {
    private JTextField vectorWidthField, vectorHeightField, codebookField, filePathField;
    private JLabel originalImageLabel, compressedImageLabel;
    private BufferedImage originalImage, compressedImage;

    private VectorQuantizationCompression compression;

    public GUI() {
        // Set up the main frame
        setTitle("Vector Quantization");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create components
        vectorWidthField = new JTextField(10);
        vectorHeightField = new JTextField(10);
        codebookField = new JTextField(10);
        filePathField = new JTextField(20);
        originalImageLabel = new JLabel();
        compressedImageLabel = new JLabel();

        JButton browseButton = new JButton("Browse");
        JButton compressButton = new JButton("Compress");

        // Set layout
        setLayout(new BorderLayout());

        // Create panels
        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JPanel imagePanel = new JPanel(new GridLayout(1, 2));

        // Add components to the input panel
        inputPanel.add(new JLabel("Vector Width:"));
        inputPanel.add(vectorWidthField);

        inputPanel.add(new JLabel("Vector Height:"));
        inputPanel.add(vectorHeightField);

        inputPanel.add(new JLabel("Codebook Size:"));
        inputPanel.add(codebookField);

        inputPanel.add(new JLabel("File Path:"));
        inputPanel.add(filePathField);

        buttonPanel.add(browseButton);
        buttonPanel.add(compressButton);

        imagePanel.add(originalImageLabel);
        imagePanel.add(compressedImageLabel);

        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(imagePanel, BorderLayout.SOUTH);


        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    filePathField.setText(selectedFile.getAbsolutePath());

                    originalImage = loadImage(selectedFile);
                    displayImage(originalImage, originalImageLabel);
                    compressedImageLabel.setIcon(null);
                }
            }
        });

        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int vectorWidth = Integer.parseInt(vectorWidthField.getText());
                int vectorHeight = Integer.parseInt(vectorHeightField.getText());
                int codebook = Integer.parseInt(codebookField.getText());

                compression = new VectorQuantizationCompression(vectorHeight, vectorWidth, codebook, filePathField.getText());
                compression.run();

                compressedImage = createImageFromCompression(compression, filePathField.getText());
                displayImage(compressedImage, compressedImageLabel);

                saveImageToFile(compressedImage, "compressed_output.png");
            }
        });
    }

    private BufferedImage loadImage(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void displayImage(BufferedImage image, JLabel label) {
        ImageIcon icon = new ImageIcon(image.getScaledInstance(300, -1, Image.SCALE_DEFAULT));
        label.setIcon(icon);
    }

    private static BufferedImage createImageFromCompression(VectorQuantizationCompression compression, String path) {
        Vector<Vector<Vector<Double>>> averages = compression.getAverages();
        Map<Integer, Integer> indexTable = compression.getIndexTable();

        int imageHeight = compression.imageConverter(path).size();
        int imageWidth = compression.imageConverter(path).lastElement().size();

        BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

        int blockWidth = compression.getVectorWidth();
        int blockHeight = compression.getVectorHeight();

        int cnt = 0;
        for (int i = 0; i < imageHeight; i += blockHeight) {
            for (int j = 0; j < imageWidth; j += blockWidth) {
                Integer index = indexTable.get(cnt++);

                if (index != null && index < averages.size()) {
                    Vector<Vector<Double>> blockAverages = averages.get(index);

                    for (int k = 0; k < blockHeight && i + k < imageHeight; k++) {
                        for (int l = 0; l < blockWidth && j + l < imageWidth; l++) {
                            double pixelValue = blockAverages.get(k).get(l);

                            int intensity = (int) pixelValue;
                            int rgb = (intensity << 16) | (intensity << 8) | intensity;
                            bufferedImage.setRGB(j + l, i + k, rgb);
                        }
                    }
                }
            }
        }

        return bufferedImage;
    }

    private static void saveImageToFile(BufferedImage image, String filePath) {
        try {
            // Specify the file format (e.g., "png")
            String format = "png";
            // Write the image to the specified file
            ImageIO.write(image, format, new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }
}
