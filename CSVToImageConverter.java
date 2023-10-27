import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;

public class CSVToImageConverter {

    public static void main(String[] args) {
        String csvFileName = "y0_thresholded.csv"; // Replace with your CSV file name
        String outputImageFileName = "output.jpg"; // Output JPG file name

        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFileName));
            String line;

            int width = 0;
            int height = 0;

            // Find the dimensions of the image (assuming rectangular CSV data)
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                width = values.length;
                height++;
            }
            br.close();

            // Read CSV file and create BufferedImage
            br = new BufferedReader(new FileReader(csvFileName));
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int y = 0;

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                for (int x = 0; x < width; x++) {
                    double pixelValue = Double.parseDouble(values[x]);
                    int rgb = (pixelValue >= 0.5) ? 0x00 : 0xFFFFFF; // Black or white based on threshold (0.5)
                    image.setRGB(x, y, rgb);
                }
                y++;
            }
            br.close();

            // Save the BufferedImage as JPG
            ImageIO.write(image, "jpg", new File(outputImageFileName));
            System.out.println("CSV data converted to binary JPG image: " + outputImageFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
