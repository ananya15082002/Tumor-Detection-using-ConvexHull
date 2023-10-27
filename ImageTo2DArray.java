import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageTo2DArray {

    public static void main(String[] args) {
        try {
            String sourceBaseDirectory = "C:\\Users\\asus\\Desktop\\Projects\\Minor1\\ImagePreprocessed";
            String outputBaseDirectory = "C:\\Users\\asus\\Desktop\\Projects\\Minor1\\ProcessedData";

            // Iterate over the subfolders ("yes," "no," and "pred")
            String[] subfolders = {"yes", "no", "pred"};

            for (String subfolder : subfolders) {
                String destDirectoryPath = sourceBaseDirectory + "\\ImagePreprocessed-" + subfolder;
                File destDirectory = new File(destDirectoryPath);

                if (destDirectory.exists() && destDirectory.isDirectory()) {
                    File[] files = destDirectory.listFiles();

                    if (files != null) {
                        for (File file : files) {
                            if (file.isFile() && file.getName().toLowerCase().endsWith(".jpg")) {
                                BufferedImage grayscaleImage = ImageIO.read(file);

                                // Create a new 2D array for each image
                                double[][][] processedImageData = new double[3][100][100];

                                for (int x = 0; x < 100; x++) {
                                    for (int y = 0; y < 100; y++) {
                                        int pixelValue = grayscaleImage.getRGB(x, y) & 0xFF; // Get pixel value (0-255)

                                        // Normalize pixel value to the range 0 to 1
                                        double normalizedValue = pixelValue / 255.0;

                                        // Store pixel information in the processedImageData array
                                        processedImageData[0][x][y] = normalizedValue; // Blackness/whiteness
                                        processedImageData[1][x][y] = x; // X-coordinate
                                        processedImageData[2][x][y] = y; // Y-coordinate
                                    }
                                }

                                // Create subdirectories for each class ("yes," "no," "pred") under ProcessedData
                                String classOutputDirectory = outputBaseDirectory + "\\" + subfolder;
                                File classOutputDir = new File(classOutputDirectory);
                                if (!classOutputDir.exists()) {
                                    classOutputDir.mkdirs();
                                }

                                // Store this processed data in a separate CSV file for each image
                                String imageName = file.getName().replace(".jpg", "");
                                String csvFileName = classOutputDirectory + "\\" + imageName + ".csv";
                                storeProcessedDataAsCSV(processedImageData, csvFileName);
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    // Function to store the processed data as a CSV file
    private static void storeProcessedDataAsCSV(double[][][] data, String outputFilePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));

        for (int x = 0; x < data[0].length; x++) {
            for (int y = 0; y < data[0][x].length; y++) {
                StringBuilder line = new StringBuilder();
                line.append(data[0][x][y]); // Blackness/whiteness
                line.append(",");
                line.append(data[1][x][y]); // X-coordinate
                line.append(",");
                line.append(data[2][x][y]); // Y-coordinate
                writer.write(line.toString());
                writer.newLine();
            }
        }

        writer.close();
    }
}
