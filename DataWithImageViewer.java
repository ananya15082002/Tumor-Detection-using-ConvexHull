import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class DataWithImageViewer {

    public static void main(String[] args) {
        try {
            String sourceDirectoryPath = "C:\\Users\\asus\\Desktop\\Projects\\Minor1\\Dataset";

            File sourceDirectory = new File(sourceDirectoryPath);
            if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
                throw new IOException("Source directory does not exist");
            }

            File[] files = sourceDirectory.listFiles();

            if (files != null) {
                int imagesProcessed = 0;
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".jpg")) {
                        BufferedImage image = ImageIO.read(file);
                        if (image != null) {
                            // Display the image in a Swing window
                            displayImage(image);
                            System.out.println("Image Name: " + file.getName());
                            System.out.println("Image Width: " + image.getWidth());
                            System.out.println("Image Height: " + image.getHeight());
                            imagesProcessed++;

                            // Limit the number of images displayed to 10
                            if (imagesProcessed >= 10) {
                                break;
                            }
                        } else {
                            System.out.println("Failed to read image: " + file.getName());
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    private static void displayImage(BufferedImage image) {
        JFrame frame = new JFrame("Image Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        JLabel label = new JLabel(new ImageIcon(image));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
}
