/**
 * VCCW04 - Image Processing and Corner Detection
 *
 * This program performs image processing and corner detection using the Harris Corner Detection algorithm.
 * It uses Sobel filters to compute the gradient of the image, calculates the Harris corner strength,
 * and applies a threshold to detect corners. Additionally, it implements an improved version that rejects
 * weaker corners in close proximity to stronger ones.
 *
 * The program displays the original image, the result of the standard corner detection, and the improved corner detection
 * side by side for visual comparison.
 *
 *
 * Note: The program reads an image file named "Room.jpg" .
 *
 * @author Guoyu
 */
import Objects.Corner;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class VCCW04 extends Component implements KeyListener {

    private BufferedImage in, out;



    int width, height;
    File inputFile;
    public static final float[] SOBELx = {
            -1.f, 0.f, 1.f,
            -2.f, 0.f, 2.f,
            -1.f, 0.f, 1.f
    };

    public static final float[] SOBELy = {
            1.f, 2.f, 1.f,
            0.f, 0.f, 0.f,
            -1.f, -2.f, -1.f
    };
    public static final float[] SHARPEN3x3 = { // sharpening filter kernel
            0.f, -1.f,  0.f,
            -1.f,  5.f, -1.f,
            0.f, -1.f,  0.f
    };

    public static final float[] BLUR3x3 = {
            0.1f, 0.1f, 0.1f,    // low-pass filter kernel
            0.1f, 0.2f, 0.1f,
            0.1f, 0.1f, 0.1f
    };

    public VCCW04() {
        loadImage();
    }

    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    public void paint(Graphics g) {
        g.drawImage(out, 0, 0, null);
    }

    public static void main(String[] args) {
        VCCW04 img = new VCCW04();
        img.processing();
    }

    private void processing() {

        // keep the original picture
        BufferedImage originalImage = new BufferedImage(width, height, in.getType());
        Graphics g_original = originalImage.getGraphics();
        g_original.drawImage(in, 0, 0, null);
        g_original.dispose();

        //Compute Gaussian derivatives using Sobel filters
        //calculate gradient
        float[][] img_x = imageGradient(SOBELx);
        float[][] img_y = imageGradient(SOBELy);

        //normal one
        float[][] cornerResponseValues = calculateAndPrintThreshold(img_x, img_y);
        System.out.println(cornerResponseValues);

        // create  BufferedImage to save the result of calPrintThresholdReject
        BufferedImage filteredImage = new BufferedImage(width, height, in.getType());
        Graphics g_filtered = filteredImage.getGraphics();

        // in filteredImage draw the original picture
        g_filtered.drawImage(originalImage, 0, 0, null);

        // improverment one
        List<Corner> filteredCorners = calPrintThresholdReject(img_x, img_y);
        for (Corner corner : filteredCorners) {
            g_filtered.setColor(Color.RED);
            g_filtered.fillRect(corner.x, corner.y, 3, 3);
        }

        // side by side display
        showImagesSideBySide(new BufferedImage[]{out, filteredImage});

        repaint();
    }
    // use to make the picture side by side
    private void showImagesSideBySide(BufferedImage[] images) {
        int combinedWidth = 0;
        int maxHeight = 0;

        // calculate the width and height
        for (BufferedImage image : images) {
            combinedWidth += image.getWidth();
            maxHeight = Math.max(maxHeight, image.getHeight());
        }

        // create a new  BufferedImage use side by side
        BufferedImage combinedImage = new BufferedImage(combinedWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g_combined = combinedImage.getGraphics();

        int currentX = 0;

        // in the new  BufferedImage to draw picture
        for (BufferedImage image : images) {
            g_combined.drawImage(image, currentX, 0, null);
            currentX += image.getWidth();
        }

        // show the picture
        JFrame frame = new JFrame("Processed Images(right is the improve)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JLabel(new ImageIcon(combinedImage)));
        frame.pack();
        frame.setVisible(true);
    }

    //Sobel  gray  and gradient
    private float[][] imageGradient(float[] SOBEL) {
        //Filter the image using Sobel filter
        float[][] gradient = new float[width][height];

        int[] rArray = new int[width*height];
        int[] gArray = new int[width*height];
        int[] bArray = new int[width*height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixel = new Color(in.getRGB(x, y));
                rArray[y * width + x] = pixel.getRed();
                gArray[y * width + x] = pixel.getGreen();
                bArray[y * width + x] = pixel.getBlue();
            }
        }

        for (int y = 1; y < height-1; y++){
            for (int x = 1; x < width-1; x++){
                int[] rNeighbour = {
                        rArray[(y-1)*width+x-1], rArray[(y-1)*width+x], rArray[(y-1)*width+x+1],
                        rArray[y*width+x-1], rArray[y*width+x], rArray[y*width+x+1],
                        rArray[(y+1)*width+x-1], rArray[(y+1)*width+x], rArray[(y+1)*width+x+1]
                };
                int[] gNeighbour = {
                        gArray[(y-1)*width+x-1], gArray[(y-1)*width+x], gArray[(y-1)*width+x+1],
                        gArray[y*width+x-1], gArray[y*width+x], gArray[y*width+x+1],
                        gArray[(y+1)*width+x-1], gArray[(y+1)*width+x], gArray[(y+1)*width+x+1]
                };
                int[] bNeighbour = {
                        bArray[(y-1)*width+x-1], bArray[(y-1)*width+x], bArray[(y-1)*width+x+1],
                        bArray[y*width+x-1], bArray[y*width+x], bArray[y*width+x+1],
                        bArray[(y+1)*width+x-1], bArray[(y+1)*width+x], bArray[(y+1)*width+x+1]
                };

                float r = 0;
                float g = 0;
                float b = 0;
                for (int i = 0; i < 9; i++) {
                    r = r + rNeighbour[i] * SOBEL[i];
                    g = g + gNeighbour[i] * SOBEL[i];
                    b = b + bNeighbour[i] * SOBEL[i];
                }
                gradient[x][y] = (float) (0.299 * r + 0.587 * g + 0.114 * b);
            }
        }
        return gradient;
    }
    // normal :  use  threshold to choose corner
    private float[][] calculateAndPrintThreshold(float[][] gradientX, float[][] gradientY) {
        // Harris  cornot to detection
        float[][] cornerStrength = calculateHarrisCornerStrength(gradientX, gradientY);

        // find Rmax
        double Rmax = Double.MIN_VALUE;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (cornerStrength[i][j] > Rmax) {
                    Rmax = cornerStrength[i][j];
                }
            }
        }
        System.out.println("Max Corner Strength: " + Rmax);

        // Filter corner points using threshold
        double qualityLevel = 0.05; //Adjustable parameters
        double cornerStrengthThreshold = qualityLevel * Rmax;

        // print the threshold
        System.out.println("Threshold: " + cornerStrengthThreshold);

        // counter claculate the number of the point pass the filter
        int count = 0;


        //Traverse the corner point intensity matrix and count the number of points through the threshold
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (cornerStrength[i][j] > cornerStrengthThreshold) {
                    count++;
                    Graphics g = out.getGraphics();
                    g.setColor(Color.RED);
                    g.fillRect(i, j, 3, 3);
                }
            }
        }

        // the number of the point that pass the filter
        System.out.println("Number of Points Above Threshold（deal with normal ): " + count);

        return cornerStrength;
    }


    //improvement :  use T and threshold to choose corner and use T to reject the lower corner
    private List<Corner> calPrintThresholdReject(float[][] gradientX, float[][] gradientY) {
        List<Corner> corners = new ArrayList<>();

        // Call Harris corner detection algorithm
        float[][] cornerStrength = calculateHarrisCornerStrength(gradientX, gradientY);

        // Find the maximum quality measure Rmax
        double Rmax = Double.MIN_VALUE;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (cornerStrength[i][j] > Rmax) {
                    Rmax = cornerStrength[i][j];
                }
            }
        }
        System.out.println("Max Corner Strength: " + Rmax);

        // use the threshold to as filter
        double qualityLevel = 0.04;
        double cornerStrengthThreshold = qualityLevel * Rmax;

        System.out.println("Threshold: " + cornerStrengthThreshold);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (cornerStrength[i][j] > cornerStrengthThreshold) {
                    // Check if there is a strong corner point within a distance less than T
                    boolean isStrongerCorner = isStrongerCornerPresent(corners, i, j, cornerStrength[i][j], 10.0);
                    if (!isStrongerCorner) {
                        corners.add(new Corner(i, j, cornerStrength[i][j]));
                        Graphics g = out.getGraphics();
                        g.setColor(Color.RED);
                        g.fillRect(i, j, 3, 3);
                    }
                }
            }
        }

        // use the T to reject
        List<Corner> filteredCorners = filterCorners(corners, 1.0);

        // Print the number of corners filtered by threshold and distance T
        System.out.println("Number of Points Above Threshold and T(improvement): " + filteredCorners.size());

        return filteredCorners;
    }
    //Check if a stronger corner already exists in the list filteredCorners (greater strength)
    private List<Corner> filterCorners(List<Corner> corners, double distanceThreshold) {
        List<Corner> filteredCorners = new ArrayList<>();

        for (Corner corner : corners) {
            // Check if there is a stronger corner point within a distance less than T
            if (!isStrongerCornerPresent(filteredCorners, corner.x, corner.y, corner.strength, distanceThreshold)) {
                filteredCorners.add(corner);
            }
        }

        return filteredCorners;
    }
    //Calculate the Euclidean distance between the current point (x, y) and the corner point
    //check if there is a stronger corner point with a distance smaller than distanceThreshold
    private boolean isStrongerCornerPresent(List<Corner> corners, int x, int y, float currentStrength, double distanceThreshold) {
        for (Corner corner : corners) {
            // Calculate the Euclidean distance between two points
            double distance = Math.sqrt(Math.pow(x - corner.x, 2) + Math.pow(y - corner.y, 2));

            // Check if there is a stronger corner point within a distance less than T
            if (distance < distanceThreshold && corner.strength > currentStrength) {
                return true;
            }
        }
        return false;
    }


    //calculate the every response point  in this picture
    private float[][] calculateHarrisCornerStrength(float[][] gradientX, float[][] gradientY) {
        int width = gradientX.length;
        int height = gradientX[0].length;
        float[][] cornerStrength = new float[width][height];

        float k = 0.04f;  // Adjustable parameters
        int windowSize = 3;  // Window size, adjustable parameters

        // define Gaussian window
        float[] gaussW = {
                1.f, 2.f, 1.f,
                2.f, 4.f, 2.f,
                1.f, 2.f, 1.f
        };

        // Normalized Gaussian window
        float sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += gaussW[i];
        }
        for (int i = 0; i < 9; i++) {
            gaussW[i] /= sum;
        }

        // Calculate the Harris response function
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Calculate the sum of M matrices and weights
                float sumIxIx = 0, sumIyIy = 0, sumIxIy = 0;
                float sumWeights = 0.0f;

                for (int i = -windowSize/2; i <= windowSize/2; i++) {
                    for (int j = -windowSize/2; j <= windowSize/2; j++) {
                        int neighborX = x + i;
                        int neighborY = y + j;

                        if (neighborX >= 0 && neighborX < width && neighborY >= 0 && neighborY < height) {
                            float neighborIx = gradientX[neighborX][neighborY];
                            float neighborIy = gradientY[neighborX][neighborY];

                            // get Gaussian window weight
                            float weight = gaussW[(i + windowSize / 2) * windowSize + (j + windowSize / 2)];

                            sumWeights += weight;

                            sumIxIx += weight * neighborIx * neighborIx;
                            sumIyIy += weight * neighborIy * neighborIy;
                            sumIxIy += weight * neighborIx * neighborIy;
                        }
                    }
                }

                // Normalized sum of squared gradients
                sumIxIx /= sumWeights;
                sumIyIy /= sumWeights;
                sumIxIy /= sumWeights;

                // calculate Harris function of horris
                float detM = sumIxIx * sumIyIy - sumIxIy * sumIxIy;
                float traceM = sumIxIx + sumIyIy;
                cornerStrength[x][y] = detM - k * traceM * traceM;
            }
        }

        return cornerStrength;
    }



    public void keyPressed(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_ESCAPE)
            System.exit(0);

        if (ke.getKeyChar() == 's' || ke.getKeyChar() == 'S') {// Save the processed image
            saveImage();
        } else if (ke.getKeyChar() == 'p' || ke.getKeyChar() == 'P') {// Image Processing
            processing();
        }
    }

    private void loadImage() {
        try {
            in = ImageIO.read(new File("Room.jpg"));
            System.out.println("Image Type: " + in.getType());

            width = in.getWidth();
            height = in.getHeight();
            //out = in;
            out = new BufferedImage(width, height, in.getType());
            Graphics g_out = out.getGraphics();
            g_out.drawImage(in, 0, 0, null);
            g_out.dispose();
        } catch (IOException e) {
            System.out.println("Image could not be read");
            System.exit(1);
        }
    }
    //save image function
    private void saveImage() {
        try {
            ImageIO.write(out, "jpg", new File("DaffodilG_corners.jpg"));
        } catch (IOException ex) {
            System.out.println("Image could not be written");
            System.exit(2);
        }
    }

    // blur picture
    private void applyMedianBlur() {
        int windowSize = 3;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                // Collect pixel values within a window
                int[] valuesR = new int[windowSize * windowSize];
                int[] valuesG = new int[windowSize * windowSize];
                int[] valuesB = new int[windowSize * windowSize];

                int index = 0;
                for (int dy = -windowSize / 2; dy <= windowSize / 2; dy++) {
                    for (int dx = -windowSize / 2; dx <= windowSize / 2; dx++) {
                        int pixelX = x + dx;
                        int pixelY = y + dy;

                        Color pixel = new Color(in.getRGB(pixelX, pixelY));
                        valuesR[index] = pixel.getRed();
                        valuesG[index] = pixel.getGreen();
                        valuesB[index] = pixel.getBlue();

                        index++;
                    }
                }

                // Sort pixel values
                Arrays.sort(valuesR);
                Arrays.sort(valuesG);
                Arrays.sort(valuesB);

                // Use the median value of the sorted array as the color of the output pixel
                int medianR = valuesR[windowSize * windowSize / 2];
                int medianG = valuesG[windowSize * windowSize / 2];
                int medianB = valuesB[windowSize * windowSize / 2];

                // Set the calculated median color to the color at (x, y) of the output image
                out.setRGB(x, y, new Color(medianR, medianG, medianB).getRGB());
            }
        }
    }


    public void keyReleased(KeyEvent e) {

    }

    public void keyTyped(KeyEvent e) {

    }

}
