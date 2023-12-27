import java.awt.BasicStroke;
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

public class ConvexHullToImage {
    public static void main(String[] args) {
        List<Point> dataPoints = new ArrayList<>();
        
        // Read the CSV file with x, y coordinates
        try (BufferedReader br = new BufferedReader(new FileReader("y0_convex_hull.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    int X = Integer.parseInt(parts[0]);
                    int Y = Integer.parseInt(parts[1]);
                    dataPoints.add(new Point(X, Y));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Calculate the convex hull
        List<Point> convexHull = calculateConvexHull(dataPoints);

        // Determine image dimensions based on the data points
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Point p : dataPoints) {
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
        }

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        // Offset all points to fit within the image dimensions
        for (Point p : dataPoints) {
            p.translate(-minX, -minY);
        }
        for (Point p : convexHull) {
            p.translate(-minX, -minY);
        }

        // Create a BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Fill the background with white
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Set the color and thickness for drawing the convex hull
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        // Draw the convex hull by connecting the points
        for (int i = 0; i < convexHull.size(); i++) {
            Point p1 = convexHull.get(i);
            Point p2 = convexHull.get((i + 1) % convexHull.size());
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Save the image to a file
        try {
            File outputFile = new File("convex_hull.png");
            ImageIO.write(image, "PNG", outputFile);
            System.out.println("Image saved to " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        g2d.dispose();
    }

    // Implement your Convex Hull algorithm here
    private static List<Point> calculateConvexHull(List<Point> dataPoints) {
        // Replace this with your Convex Hull algorithm implementation
        return new ArrayList<>();
    }
}