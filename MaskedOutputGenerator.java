import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class MaskedOutputGenerator {

    public static void main(String[] args) {
        String inputDirectory = "C:\\Users\\asus\\Desktop\\Projects\\Minor1\\ConvexHullOutput";
        String outputDirectory = "C:\\Users\\asus\\Desktop\\Projects\\Minor1\\MaskedOutput";

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

            File[] csvFiles = classInputDir.listFiles((dir, name) -> name.endsWith("_convex_hull.csv"));

            if (csvFiles != null) {
                for (File csvFile : csvFiles) {
                    try {
                        // Load convex hull points from the CSV file
                        List<Point> convexHull = loadConvexHullFromCSV(csvFile);

                        // Create a masked image
                        BufferedImage maskedImage = createMaskedImage(convexHull);

                        // Save the resulting masked image
                        String outputImagePath = classOutputDir.getPath() + File.separator + csvFile.getName().replace("_convex_hull.csv", "_masked.jpg");
                        saveImage(maskedImage, outputImagePath);

                        System.out.println("Category: " + classLabel + " - " + outputImagePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static List<Point> loadConvexHullFromCSV(File convexHullCSVFile) throws IOException {
        List<Point> convexHull = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(convexHullCSVFile));

        // Skip the header
        reader.readLine();

        String line;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");
            int x = Integer.parseInt(values[0]);
            int y = Integer.parseInt(values[1]);
            convexHull.add(new Point(x, y));
        }

        reader.close();
        return convexHull;
    }

    private static BufferedImage createMaskedImage(List<Point> convexHull) {
        int imageWidth = 100; // Set your desired width
        int imageHeight = 100; // Set your desired height

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Draw the convex hull region on the image
        drawConvexHull(convexHull, g2d);

        g2d.dispose();
        return image;
    }

    private static void drawConvexHull(List<Point> convexHull, Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
    
        int[] xPoints = convexHull.stream().mapToInt(point -> (int) point.getX()).toArray();
        int[] yPoints = convexHull.stream().mapToInt(point -> (int) point.getY()).toArray();
        int nPoints = convexHull.size();
    
        g2d.fillPolygon(xPoints, yPoints, nPoints);
    }
    

    private static void saveImage(BufferedImage image, String outputImagePath) {
        try {
            ImageIO.write(image, "jpg", new File(outputImagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
