import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegionBasedSegmentation {

    public static void main(String[] args) {
        String inputDirectory = "C:\\Users\\asus\\Desktop\\Projects\\Minor1\\ThresholdedOtsusData";
        String outputBaseDirectory = "C:\\Users\\asus\\Desktop\\Projects\\Minor1\\RegionSegmentationOutput";

        File[] classDirectories = new File(inputDirectory).listFiles();

        if (classDirectories != null) {
            for (File classDirectory : classDirectories) {
                if (classDirectory.isDirectory()) {
                    String className = classDirectory.getName();
                    File[] binaryImages = classDirectory.listFiles((dir, name) -> name.endsWith("_thresholded.csv"));

                    if (binaryImages != null) {
                        for (File binaryImage : binaryImages) {
                            try {
                                // Load the binary image
                                double[][] binaryData = loadBinaryImage(binaryImage);

                                // Perform connected component labeling
                                List<RegionProperties> regionProperties = labelRegions(binaryData);

                                // Find the largest region (potential tumor region)
                                RegionProperties largestRegion = findLargestRegion(regionProperties);

                                // Create the output directory if it doesn't exist
                                String classOutputDirectory = outputBaseDirectory + "\\" + className;
                                File outputDir = new File(classOutputDirectory);
                                if (!outputDir.exists()) {
                                    outputDir.mkdirs();
                                }

                                // Store region properties in CSV
                                String imageName = binaryImage.getName().replace("_thresholded.csv", "");
                                String csvFilePath = classOutputDirectory + "\\" + imageName + "_regions.csv";
                                storeRegionPropertiesAsCSV(regionProperties, csvFilePath);

                                System.out.println("Processed: " + csvFilePath);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private static double[][] loadBinaryImage(File binaryImageFile) throws IOException {
        List<double[]> binaryData = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(binaryImageFile));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");
            double[] row = new double[values.length];
            for (int i = 0; i < values.length; i++) {
                row[i] = Double.parseDouble(values[i]);
            }
            binaryData.add(row);
        }

        reader.close();

        double[][] binaryImage = new double[binaryData.size()][binaryData.get(0).length];
        for (int i = 0; i < binaryData.size(); i++) {
            binaryImage[i] = binaryData.get(i);
        }

        return binaryImage;
    }

    private static List<RegionProperties> labelRegions(double[][] binaryImage) {
        List<RegionProperties> regionProperties = new ArrayList<>();
        int label = 1; // Initial region label

        int numRows = binaryImage.length;
        int numCols = binaryImage[0].length;
        boolean[][] visited = new boolean[numRows][numCols];

        // Iterate through the binary image
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                if (binaryImage[i][j] == 1 && !visited[i][j]) {
                    // Start a new region
                    RegionProperties region = new RegionProperties(label);

                    // Depth-first search to label connected components
                    dfsLabeling(binaryImage, visited, i, j, label, region);

                    // Add the region to the list
                    regionProperties.add(region);

                    // Increment the label for the next region
                    label++;
                }
            }
        }

        return regionProperties;
    }

    private static void dfsLabeling(double[][] binaryImage, boolean[][] visited, int x, int y, int label, RegionProperties region) {
        if (x < 0 || y < 0 || x >= binaryImage.length || y >= binaryImage[0].length || visited[x][y] || binaryImage[x][y] == 0) {
            return;
        }

        visited[x][y] = true;
        binaryImage[x][y] = label; // Assign the label to the region
        region.incrementArea(); // Increment region area
        region.updateBoundingBox(x, y);

        // Explore neighboring pixels
        dfsLabeling(binaryImage, visited, x + 1, y, label, region);
        dfsLabeling(binaryImage, visited, x - 1, y, label, region);
        dfsLabeling(binaryImage, visited, x, y + 1, label, region);
        dfsLabeling(binaryImage, visited, x, y - 1, label, region);
    }

    private static RegionProperties findLargestRegion(List<RegionProperties> regionProperties) {
        RegionProperties largestRegion = null;

        for (RegionProperties region : regionProperties) {
            if (largestRegion == null || region.getArea() > largestRegion.getArea()) {
                largestRegion = region;
            }
        }

        return largestRegion;
    }

    private static void storeRegionPropertiesAsCSV(List<RegionProperties> regionProperties, String outputFilePath) throws IOException {
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            writer.write("Label,Area,TopLeftX,TopLeftY,BottomRightX,BottomRightY\n");
            for (RegionProperties region : regionProperties) {
                writer.write(String.format("%d,%d,%d,%d,%d,%d\n",
                        region.getLabel(),
                        region.getArea(),
                        region.getBoundingBox().getTopLeftX(),
                        region.getBoundingBox().getTopLeftY(),
                        region.getBoundingBox().getBottomRightX(),
                        region.getBoundingBox().getBottomRightY()));
            }
        }
    }

    static class RegionProperties {
        private int label;
        private int area;
        private BoundingBox boundingBox;

        RegionProperties(int label) {
            this.label = label;
            this.area = 0;
            this.boundingBox = new BoundingBox();
        }

        int getLabel() {
            return label;
        }

        int getArea() {
            return area;
        }

        BoundingBox getBoundingBox() {
            return boundingBox;
        }

        void incrementArea() {
            this.area++;
        }

        void updateBoundingBox(int x, int y) {
            boundingBox.update(x, y);
        }
    }

    static class BoundingBox {
        private int topLeftX;
        private int topLeftY;
        private int bottomRightX;
        private int bottomRightY;

        BoundingBox() {
            topLeftX = Integer.MAX_VALUE;
            topLeftY = Integer.MAX_VALUE;
            bottomRightX = Integer.MIN_VALUE;
            bottomRightY = Integer.MIN_VALUE;
        }

        int getTopLeftX() {
            return topLeftX;
        }

        int getTopLeftY() {
            return topLeftY;
        }

        int getBottomRightX() {
            return bottomRightX;
        }

        int getBottomRightY() {
            return bottomRightY;
        }

        void update(int x, int y) {
            topLeftX = Math.min(topLeftX, x);
            topLeftY = Math.min(topLeftY, y);
            bottomRightX = Math.max(bottomRightX, x);
            bottomRightY = Math.max(bottomRightY, y);
        }
    }
}
