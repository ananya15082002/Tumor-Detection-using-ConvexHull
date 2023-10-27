import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ThresholdingOtsusMethod {

    public static void main(String[] args) {
        try {
            String inputBaseDirectory = "C:\\Users\\asus\\Desktop\\Projects\\Minor1\\ProcessedData";
            String outputBaseDirectory = "C:\\Users\\asus\\Desktop\\Projects\\Minor1\\ThresholdedOtsusData";

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

                                // Apply Otsu's thresholding method
                                double threshold = calculateOtsusThreshold(processedImageData[0]);
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

    // Function to calculate Otsu's threshold
    private static double calculateOtsusThreshold(double[][] grayscaleImage) {
        int numThresholds = 256; // Number of possible thresholds
        int[] histogram = new int[numThresholds];

        // Calculate the histogram
        for (int x = 0; x < grayscaleImage.length; x++) {
            for (int y = 0; y < grayscaleImage[x].length; y++) {
                int value = (int) (grayscaleImage[x][y] * 255);
                histogram[value]++;
            }
        }

        // Calculate the total number of pixels
        int totalPixels = grayscaleImage.length * grayscaleImage[0].length;

        double sum = 0.0;
        for (int i = 0; i < numThresholds; i++) {
            sum += i * histogram[i];
        }

        double sumB = 0.0;
        int wB = 0;
        int wF;
        double maxVariance = 0.0;
        double threshold = 0;

        for (int i = 0; i < numThresholds; i++) {
            wB += histogram[i];
            if (wB == 0) {
                continue;
            }
            wF = totalPixels - wB;
            if (wF == 0) {
                break;
            }
            sumB += i * histogram[i];
            double mB = sumB / wB;
            double mF = (sum - sumB) / wF;
            double varianceBetween = wB * wF * (mB - mF) * (mB - mF);
            if (varianceBetween > maxVariance) {
                maxVariance = varianceBetween;
                threshold = i;
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
