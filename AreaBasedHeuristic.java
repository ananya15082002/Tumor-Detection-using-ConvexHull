import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AreaBasedHeuristic {

    public static void main(String[] args) {
        String inputDirectory = "C:\\Users\\asus\\Desktop\\Projects\\Minor1\\RegionSegmentationOutput";
        String outputDirectory = "C:\\Users\\asus\\Desktop\\Projects\\Minor1\\AreaBasedOutput";

        File mainInputDir = new File(inputDirectory);
        File mainOutputDir = new File(outputDirectory);

        if (!mainOutputDir.exists()) {
            mainOutputDir.mkdirs();
        }

        String[] classLabels = {"yes", "no", "pred"};

        for (String classLabel : classLabels) {
            File classInputDir = new File(mainInputDir.getPath() + File.separator + classLabel);
            File classOutputDir = new File(mainOutputDir.getPath() + File.separator + classLabel);

            if (!classOutputDir.exists()) {
                classOutputDir.mkdirs();
            }

            File[] csvFiles = classInputDir.listFiles((dir, name) -> name.endsWith("_regions.csv"));

            if (csvFiles != null) {
                for (File csvFile : csvFiles) {
                    try {
                        // Load the region properties from the CSV file
                        List<RegionProperties> regionProperties = loadRegionProperties(csvFile);

                        // Identify potential tumor regions based on the area-based heuristic
                        RegionProperties potentialTumor = identifyPotentialTumorRegion(regionProperties);

                        // Store potential tumor region properties in a new CSV file with category
                        String outputCSVFilePath = classOutputDir.getPath() + File.separator + csvFile.getName();
                        storeRegionPropertiesAsCSV(List.of(potentialTumor), outputCSVFilePath);

                        System.out.println("Category: " + classLabel + " - " + outputCSVFilePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static List<RegionProperties> loadRegionProperties(File regionCSVFile) throws IOException {
        List<RegionProperties> regionProperties = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(regionCSVFile));

        // Read the header and skip it
        String header = reader.readLine();

        String line;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");
            int label = Integer.parseInt(values[0]);
            int area = Integer.parseInt(values[1]);
            int topLeftX = Integer.parseInt(values[2]);
            int topLeftY = Integer.parseInt(values[3]);
            int bottomRightX = Integer.parseInt(values[4]);
            int bottomRightY = Integer.parseInt(values[5]);

            RegionProperties region = new RegionProperties(label, area, topLeftX, topLeftY, bottomRightX, bottomRightY);
            regionProperties.add(region);
        }

        reader.close();
        return regionProperties;
    }

    private static RegionProperties identifyPotentialTumorRegion(List<RegionProperties> regionProperties) {
        // Sort the region properties by convex area in descending order
        regionProperties.sort((a, b) -> Integer.compare(b.getConvexArea(), a.getConvexArea()));

        // Calculate the threshold for potential tumor region (30% of total area)
        int totalArea = regionProperties.stream().mapToInt(RegionProperties::getArea).sum();
        int tumorAreaThreshold = (int) (0.30 * totalArea);

        // Identify the potential tumor region
        RegionProperties potentialTumor = null;
        for (RegionProperties region : regionProperties) {
            if (region.getConvexArea() >= tumorAreaThreshold) {
                potentialTumor = region;
                break;
            }
        }

        return potentialTumor;
    }

    private static void storeRegionPropertiesAsCSV(List<RegionProperties> regionProperties, String outputFilePath) throws IOException {
        FileWriter writer = new FileWriter(outputFilePath);

        // Write the header
        writer.write("Label,Area,TopLeftX,TopLeftY,BottomRightX,BottomRightY\n");

        // Write region properties
        for (RegionProperties region : regionProperties) {
            writer.write(String.format("%d,%d,%d,%d,%d,%d\n",
                    region.getLabel(),
                    region.getArea(),
                    region.getTopLeftX(),
                    region.getTopLeftY(),
                    region.getBottomRightX(),
                    region.getBottomRightY()));
        }

        writer.close();
    }

    static class RegionProperties {
        private int label;
        private int area;
        private int topLeftX;
        private int topLeftY;
        private int bottomRightX;
        private int bottomRightY;

        RegionProperties(int label, int area, int topLeftX, int topLeftY, int bottomRightX, int bottomRightY) {
            this.label = label;
            this.area = area;
            this.topLeftX = topLeftX;
            this.topLeftY = topLeftY;
            this.bottomRightX = bottomRightX;
            this.bottomRightY = bottomRightY;
        }

        int getLabel() {
            return label;
        }

        int getArea() {
            return area;
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

        int getConvexArea() {
            return (bottomRightX - topLeftX + 1) * (bottomRightY - topLeftY + 1);
        }
    }
}
