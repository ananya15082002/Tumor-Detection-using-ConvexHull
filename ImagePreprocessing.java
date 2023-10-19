import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImagePreprocessing {

    public static void main(String[] args) {
        try {
            String sourceBaseDirectory = "C:\\Users\\asus\\Desktop\\Projects\\Minor1\\Dataset";
            String destBaseDirectory = "C:\\Users\\asus\\Desktop\\Projects\\Minor1\\ImagePreprocessed";

            // Iterate over the three folders: "yes," "no," and "pred"
            String[] folders = {"yes", "no", "pred"};

            for (String folder : folders) {
                // Source directory for the current category
                String sourceDirectoryPath = sourceBaseDirectory + "\\" + folder;
                // Destination directory for preprocessed images
                String destDirectoryPath = destBaseDirectory + "\\" + "ImagePreprocessed-" + folder;

                // Check if the source directory exists
                File sourceDirectory = new File(sourceDirectoryPath);
                if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
                    throw new IOException("Source directory does not exist: " + folder);
                }

                // Create the destination directory if it doesn't exist
                File destDirectory = new File(destDirectoryPath);
                if (!destDirectory.exists()) {
                    destDirectory.mkdirs();
                }

                // Get a list of files in the source directory
                File[] files = sourceDirectory.listFiles();

                if (files != null) {
                    for (File file : files) {
                        // Check if it's a file and if it's a JPEG image
                        if (file.isFile() && file.getName().toLowerCase().endsWith(".jpg")) {
                            // Read the original image
                            BufferedImage originalImage = ImageIO.read(file);

                            if (originalImage != null) {
                                // Specify the new width and height
                                int newWidth = 100;
                                int newHeight = 100;

                                // Resize the image
                                BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                                Graphics2D g2d = resizedImage.createGraphics();
                                g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                                g2d.dispose();

                                // Convert to grayscale (RGB to BW)
                                BufferedImage grayscaleImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
                                Graphics2D g = grayscaleImage.createGraphics();
                                g.drawImage(resizedImage, 0, 0, null);
                                g.dispose();

                                // Normalize the pixel values
                                double minPixelValue = 0;
                                double maxPixelValue = 255; // Assuming 8-bit grayscale
                                double scaleFactor = 1.0 / (maxPixelValue - minPixelValue);

                                for (int y = 0; y < newHeight; y++) {
                                    for (int x = 0; x < newWidth; x++) {
                                        int pixelValue = grayscaleImage.getRGB(x, y) & 0xFF;
                                        double normalizedValue = (pixelValue - minPixelValue) * scaleFactor;

                                        // Store or process the normalized value here
                                    }
                                }

                                // Save the preprocessed image
                                File destFile = new File(destDirectoryPath, file.getName());
                                ImageIO.write(grayscaleImage, "jpg", destFile);
                            } else {
                                System.out.println("Failed to read image: " + file.getName());
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}
