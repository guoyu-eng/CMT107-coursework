import Objects.Corner;
import com.jogamp.nativewindow.util.Point;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.List;


import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

public class HarrisCornerDetector extends Component implements KeyListener {

    private BufferedImage in, out;
    int width, height;
    File inputFile;
    private double k = 0.04; // Harris角点数

    private double[][] cornerResponse;

    private double maxQualityMeasure = Double.MIN_VALUE;




    public HarrisCornerDetector() {
        loadImage();
        addKeyListener(this);
        cornerResponse = new double[height][width];

    }

    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    public void paint(Graphics g) {
        g.drawImage(out, 0, 0, null);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Image Processing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        HarrisCornerDetector img = new HarrisCornerDetector();
        frame.add("Center", img);
        frame.pack();
        img.requestFocusInWindow();
        frame.setVisible(true);

//        img.processing();


    }
    private double computeThreshold(double qualityLevel) {
//        double maxQualityMeasure = 1000.0;

        return qualityLevel * maxQualityMeasure;
    }
    private int countCornersAboveThreshold(double threshold) {
        int count = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (cornerResponse[y][x] > threshold) {
                    count++;
                }
            }
        }
        return count;
    }


    private void processing() {

//        for (int y = 0; y < height; y++)
//            for (int x = 0; x < width; x++) {
//                Color pixel = new Color(in.getRGB(x, y)); // get the color
//                int r = pixel.getRed();                   // red component
//                int g = pixel.getGreen();                 // green component
//                int b = pixel.getBlue();                  // blue component
////				int b  = 255 -b1;
//                r = g = b = (int) (0.299*r + 0.587*g + 0.114*b); //grayscale
//                out.setRGB(x, y, (new Color(r, g, b)).getRGB());
//
//            }

       
        computeCornerResponse(computeGradientX(), computeGradientY());



      
        double qualityLevel = 0.01;
        double threshold = computeThreshold(qualityLevel);

      
        processAndDisplayCorners(threshold);

        System.out.println("Max Quality Measure: " + maxQualityMeasure);
        System.out.println("Threshold: " + threshold);



        
//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++) {
//                System.out.println("Response at (" + x + ", " + y + "): " + cornerResponse[y][x]);
//            }
//        }

        // 
        int cornerCount = countCornersAboveThreshold(threshold);
        System.out.println("Number of corners above threshold: " + cornerCount);

        repaint();
    }



    @Override
    public void keyPressed(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_ESCAPE)
            System.exit(0);

        if (ke.getKeyChar() == 's' || ke.getKeyChar() == 'S') {// Save the processed image
            saveImage();
        } else if (ke.getKeyChar() == 'p' || ke.getKeyChar() == 'P') {// Image Processing
            processing();
        }
    }

//    private void loadImage() {
//        try {
//            in = ImageIO.read(new File("WelshDragon.jpg"));
//            width = in.getWidth();
//            height = in.getHeight();
//            out = in;
//        } catch (IOException e) {
//            System.out.println("Image could not be read");
//            System.exit(1);
//        }
//    }
    // step 1 : deal with  color image to gray directly
    private void loadImage() {
        try {
            in = ImageIO.read(new File("Room.jpg"));
            width = in.getWidth();
            height = in.getHeight();

            // Create a new BufferedImage for grayscale image
            BufferedImage grayscaleImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = grayscaleImage.getGraphics();
            g.drawImage(in, 0, 0, null);
            g.dispose();

            out = grayscaleImage;


        } catch (IOException e) {
            System.out.println("Image could not be read");
            System.exit(1);
        }
    }
    // step 1 : deal with  color image to gray directly
    private double[][][] computeStructureTensor(int[][] gradientX, int[][] gradientY, int neighborhoodSize) {
        double[][][] structureTensor = new double[height][width][2];

        int halfSize = neighborhoodSize / 2;

        for (int y = halfSize; y < height - halfSize; y++) {
            for (int x = halfSize; x < width - halfSize; x++) {
                double sumIX2 = 0;
                double sumIY2 = 0;
                double sumIXIY = 0;

                for (int j = -halfSize; j <= halfSize; j++) {
                    for (int i = -halfSize; i <= halfSize; i++) {
                        int currentX = x + i;
                        int currentY = y + j;

                        int ix = gradientX[currentY][currentX];
                        int iy = gradientY[currentY][currentX];

                        sumIX2 += ix * ix;
                        sumIY2 += iy * iy;
                        sumIXIY += ix * iy;
                    }
                }

                // set structure tensor (math.)
                structureTensor[y][x][0] = sumIX2;
                structureTensor[y][x][1] = sumIY2;
            }
        }

        return structureTensor;
    }


   //calculate  R save in  cornerResponse
    private void computeCornerResponse(int[][] gradientX, int[][] gradientY) {for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            double[][] M = new double[2][2];

            // 
            int gx = gradientX[y][x];
            int gy = gradientY[y][x];

            // 
            if (gx != 0 || gy != 0) {
                M[0][0] = gx * gx;
                M[0][1] = gx * gy;
                M[1][0] = gx * gy;
                M[1][1] = gy * gy;

                
                double detM = M[0][0] * M[1][1] - M[0][1] * M[1][0];
                double traceM = M[0][0] + M[1][1];

                
                double R = detM - k * traceM * traceM * 2;
                System.out.println("Response at (" + x + ", " + y + "): " + R);

              
                cornerResponse[y][x] = R;

    
                if (R > maxQualityMeasure) {
                    maxQualityMeasure = R;
                    System.out.println(R);
                }
            }
        }
    }
    }


    private int[][] computeGradientX() {
        int[][] gradientX = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gx = 0;

                if (x > 0 && x < width - 1) {
                    gx = getGrayScale(in.getRGB(x + 1, y)) - getGrayScale(in.getRGB(x - 1, y));
                } else if (x == 0) {
                    gx = getGrayScale(in.getRGB(x + 1, y)) - getGrayScale(in.getRGB(x, y));
                } else if (x == width - 1) {
                    gx = getGrayScale(in.getRGB(x, y)) - getGrayScale(in.getRGB(x - 1, y));
                }

                gradientX[y][x] = gx;
            }
        }

        return gradientX;
    }

    private int[][] computeGradientY() {
        int[][] gradientY = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gy = 0;

                if (y > 0 && y < height - 1) {
                    gy = getGrayScale(in.getRGB(x, y + 1)) - getGrayScale(in.getRGB(x, y - 1));
                } else if (y == 0) {
                    gy = getGrayScale(in.getRGB(x, y + 1)) - getGrayScale(in.getRGB(x, y));
                } else if (y == height - 1) {
                    gy = getGrayScale(in.getRGB(x, y)) - getGrayScale(in.getRGB(x, y - 1));
                }

                gradientY[y][x] = gy;
            }
        }

        return gradientY;
    }

    // get gray
    private int getGrayScale(int rgb) {
        Color color = new Color(rgb);
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        return (int) (0.299 * r + 0.587 * g + 0.114 * b);
    }


    private List<Point> findCornersAboveThreshold(double threshold) {
        List<Point> corners = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (cornerResponse[y][x] > threshold) {
                    corners.add(new Point(x, y));
                }
            }
        }

        return corners;
    }


    private List<Point> filterAndSortCorners(double threshold) {
        List<Point> cornersAboveThreshold = findCornersAboveThreshold(threshold);

        // 3) Reject corners whose quality measure is less than TR
        cornersAboveThreshold.removeIf(point -> cornerResponse[point.getY()][point.getX()] <= threshold);

        // 4) Sort the remaining corners by the quality measure
        cornersAboveThreshold.sort((point1, point2) -> Double.compare(cornerResponse[point2.getY()][point2.getX()], cornerResponse[point1.getY()][point1.getX()]));

        return cornersAboveThreshold;
    }

    private void processAndDisplayCorners(double threshold) {
        List<Point> filteredAndSortedCorners = filterAndSortCorners(threshold);

        
        System.out.println("Filtered and Sorted Corners:");
        for (Point corner : filteredAndSortedCorners) {
            double qualityMeasure = cornerResponse[corner.getY()][corner.getX()];
            System.out.println("Corner at (" + corner.getX() + ", " + corner.getY() + ") - Quality Measure: " + qualityMeasure);
        }


    }

    //3






    private void saveImage() {
        try {
            ImageIO.write(out, "jpg", new File("DaffodilG.jpg"));
        } catch (IOException ex) {
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }
}



