import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class ConvexHullGeneration {
    public static void main(String[] args) {
        String inputDirectory = "C:\\Users\\asus\\Desktop\\Projects\\Minor1\\AreaBasedOutput";
        String outputDirectory = "C:\\Users\\asus\\Desktop\\Projects\\Minor1\\ConvexHullOutput";

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

                        // Identify potential tumor region based on the area-based heuristic
                        RegionProperties potentialTumor = identifyPotentialTumorRegion(regionProperties);

                        // Generate the convex hull for the potential tumor region
                        List<Point> convexHull = grahamScanConvexHull(potentialTumor.getPoints());

                        // Store convex hull points in a new CSV file with category
                        String outputCSVFilePath = classOutputDir.getPath() + File.separator + csvFile.getName().replace("_regions.csv", "_convex_hull.csv");
                        storeConvexHullAsCSV(convexHull, outputCSVFilePath);

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
        // Sort the region properties by area in descending order
        regionProperties.sort((a, b) -> Integer.compare(b.getArea(), a.getArea()));

        // Choose the region with the largest area as the potential tumor
        return regionProperties.get(0);
    }

    private static List<Point> grahamScanConvexHull(List<Point> points) {
        int n = points.size();
        if (n < 3) {
            return points; // Convex hull is not possible with less than 3 points
        }

        Point startPoint = findLowestPoint(points);

        // Sort the points by polar angle from the startPoint
        Collections.sort(points, (p1, p2) -> {
            double angle1 = Math.atan2(p1.y - startPoint.y, p1.x - startPoint.x);
            double angle2 = Math.atan2(p2.y - startPoint.y, p2.x - startPoint.x);
            if (angle1 < angle2) return -1;
            if (angle1 > angle2) return 1;
            return Double.compare(p1.distance(startPoint), p2.distance(startPoint));
        });

        // Initialize the convex hull
        Stack<Point> hull = new Stack<>();
        hull.push(startPoint);
        hull.push(points.get(1));

        for (int i = 2; i < n; i++) {
            while (hull.size() > 1) {
                Point top = hull.pop();
                Point nextToTop = hull.peek();
                if (orientation(nextToTop, top, points.get(i)) < 0) {
                    hull.push(top);
                    break;
                }
            }
            hull.push(points.get(i));
        }

        return new ArrayList<>(hull);
    }

    private static Point findLowestPoint(List<Point> points) {
        Point lowest = points.get(0);
        for (Point p : points) {
            if (p.y < lowest.y || (p.y == lowest.y && p.x < lowest.x)) {
                lowest = p;
            }
        }
        return lowest;
    }

    private static int orientation(Point p, Point q, Point r) {
        int val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
        if (val == 0) return 0;  // Collinear
        return (val > 0) ? 1 : -1; // Clockwise or counterclockwise
    }

    private static void storeConvexHullAsCSV(List<Point> convexHull, String outputCSVFilePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputCSVFilePath));
        writer.write("X,Y\n");
        for (Point point : convexHull) {
            writer.write(point.x + "," + point.y + "\n");
        }
        writer.close();
    }

    private static class RegionProperties {
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

        int getArea() {
            return area;
        }

        List<Point> getPoints() {
            List<Point> points = new ArrayList<>();
            for (int x = topLeftX; x <= bottomRightX; x++) {
                for (int y = topLeftY; y <= bottomRightY; y++) {
                    points.add(new Point(x, y));
                }
            }
            return points;
        }
    }
}
