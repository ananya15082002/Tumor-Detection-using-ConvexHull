import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ThresholdingYensMethod {

    public static void main(String[] args) {
        try {
            String inputBaseDirectory = "C:\\Users\\asus\\Desktop\\Projects\\Minor1\\ProcessedData";
            String outputBaseDirectory = "C:\\Users\\asus\\Desktop\\Projects\\Minor1\\ThresholdedData";

            // Iterate over the subfolders ("yes," "no," and "pred")
            String[] subfolders = {"yes", "no", "pred"};

            for (String subfolder : subfolders) {
                String classInputDirectoryPath = inputBaseDirectory + "\\" + subfolder;
                File classInputDirectory = new File(classInputDirectoryPath);

                if (classInputDirectory.exists() && classInputDirectory.isDirectory()) {
                    File[] files = classInputDirectory.listFiles();

                    if (files != null) {
                        for (File file : files) {
                            if (file.isFile() && file.getName().toLowerCase().endsWith(".csv")) {
                                double[][][] processedImageData = readProcessedDataFromCSV(file);

                                // Apply Yen's thresholding method
                                double threshold = calculateYensThreshold(processedImageData[0]);
                                double[][] binaryImage = applyThreshold(processedImageData[0], threshold);

                                // Create subdirectories for each class ("yes," "no," "pred") under ThresholdedData
                                String classOutputDirectory = outputBaseDirectory + "\\" + subfolder;
                                File classOutputDir = new File(classOutputDirectory);
                                if (!classOutputDir.exists()) {
                                    classOutputDir.mkdirs();
                                }

                                // Store the binary image as a CSV file
                                String imageName = file.getName().replace(".csv", "");
                                String thresholdedCSVFileName = classOutputDirectory + "\\" + imageName + "_thresholded.csv";
                                storeBinaryImageAsCSV(binaryImage, thresholdedCSVFileName);
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    // Function to read processed data from a CSV file
    private static double[][][] readProcessedDataFromCSV(File inputFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;
        int numRows = 100;
        int numCols = 100;
        double[][][] data = new double[3][numRows][numCols];

        int x = 0;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");
            if (values.length != 3) {
                continue;
            }
            data[0][x / numRows][x % numCols] = Double.parseDouble(values[0]);
            data[1][x / numRows][x % numCols] = Double.parseDouble(values[1]);
            data[2][x / numRows][x % numCols] = Double.parseDouble(values[2]);
            x++;
        }

        reader.close();
        return data;
    }

    // Function to calculate Yen's threshold (simplified)
    private static double calculateYensThreshold(double[][] grayscaleImage) {
        int numThresholds = 256; // Number of possible thresholds
        int[] histogram = new int[numThresholds];

        // Calculate the histogram
        for (int x = 0; x < grayscaleImage.length; x++) {
            for (int y = 0; y < grayscaleImage[x].length; y++) {
                int value = (int) (grayscaleImage[x][y] * 255);
                histogram[value]++;
            }
        }

        // Calculate the cumulative histogram
        int[] cumulativeHistogram = new int[numThresholds];
        cumulativeHistogram[0] = histogram[0];
        for (int i = 1; i < numThresholds; i++) {
            cumulativeHistogram[i] = cumulativeHistogram[i - 1] + histogram[i];
        }

        // Calculate the total number of pixels
        int totalPixels = grayscaleImage.length * grayscaleImage[0].length;

        double maxVariance = 0.0;
        int threshold = 0;

        for (int t = 0; t < numThresholds; t++) {
            int w0 = cumulativeHistogram[t];
            int w1 = totalPixels - w0;

            if (w0 == 0 || w1 == 0) {
                continue;
            }

            int sum0 = 0;
            int sum1 = 0;

            for (int i = 0; i <= t; i++) {
                sum0 += i * histogram[i];
            }

            for (int i = t + 1; i < numThresholds; i++) {
                sum1 += i * histogram[i];
            }

            double mean0 = sum0 / w0;
            double mean1 = sum1 / w1;

            double variance = (w0 * w1) * (mean0 - mean1) * (mean0 - mean1);

            if (variance > maxVariance) {
                maxVariance = variance;
                threshold = t;
            }
        }

        return threshold / 255.0; // Normalize the threshold value to [0, 1]
    }

    // Function to apply threshold to a grayscale image
    private static double[][] applyThreshold(double[][] grayscaleImage, double threshold) {
        int numRows = grayscaleImage.length;
        int numCols = grayscaleImage[0].length;
        double[][] binaryImage = new double[numRows][numCols];

        for (int x = 0; x < numRows; x++) {
            for (int y = 0; y < numCols; y++) {
                if (grayscaleImage[x][y] >= threshold) {
                    binaryImage[x][y] = 1.0; // Set to white
                } else {
                    binaryImage[x][y] = 0.0; // Set to black
                }
            }
        }

        return binaryImage;
    }

    // Function to store the binary image as a CSV file
    private static void storeBinaryImageAsCSV(double[][] binaryImage, String outputFilePath) throws IOException {
        FileWriter writer = new FileWriter(outputFilePath);

        for (int x = 0; x < binaryImage.length; x++) {
            for (int y = 0; y < binaryImage[x].length; y++) {
                writer.write(Double.toString(binaryImage[x][y]));
                writer.write(",");
            }
            writer.write("\n");
        }

        writer.close();
    }
}
