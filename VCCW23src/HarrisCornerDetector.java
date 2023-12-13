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

        // 先计算角响应
        computeCornerResponse(computeGradientX(), computeGradientY());



        // 设置阈值
        double qualityLevel = 0.01;
        double threshold = computeThreshold(qualityLevel);

        // 处理和显示角点
        processAndDisplayCorners(threshold);

        System.out.println("Max Quality Measure: " + maxQualityMeasure);
        System.out.println("Threshold: " + threshold);



        // 直接打印角响应值
//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++) {
//                System.out.println("Response at (" + x + ", " + y + "): " + cornerResponse[y][x]);
//            }
//        }

        // 根据阈值进行处理
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

            // 计算结构张量 M 的元素
            int gx = gradientX[y][x];
            int gy = gradientY[y][x];

            // 处理梯度为零的情况
            if (gx != 0 || gy != 0) {
                M[0][0] = gx * gx;
                M[0][1] = gx * gy;
                M[1][0] = gx * gy;
                M[1][1] = gy * gy;

                // 计算结构张量的行列式和迹
                double detM = M[0][0] * M[1][1] - M[0][1] * M[1][0];
                double traceM = M[0][0] + M[1][1];

                // 计算角点响应
                double R = detM - k * traceM * traceM * 2;
                System.out.println("Response at (" + x + ", " + y + "): " + R);

                // 将响应值存储在 cornerResponse 数组中
                cornerResponse[y][x] = R;

                // 更新最大质量度量
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

        // 在控制台上打印或使用其他方式显示处理后的角点
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


//
//
//import Objects.Corner;
//
//import javax.imageio.ImageIO;
//import javax.swing.*;
//        import java.awt.*;
//        import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//
//public class VCCW04 extends Component implements KeyListener {
//
//    private BufferedImage in, out;
//
//    List<Corner> corners = new ArrayList<Corner>();
//
//
//    int width, height;
//    File inputFile;
//    //这是 Sobel 滤波器的定义，用于计算图像的梯度。
//    public static final float[] SOBELx = {
//            -1.f, 0.f, 1.f,
//            -2.f, 0.f, 2.f,
//            -1.f, 0.f, 1.f
//    };
//
//    public static final float[] SOBELy = {
//            1.f, 2.f, 1.f,
//            0.f, 0.f, 0.f,
//            -1.f, -2.f, -1.f
//    };
//    public static final float[] SHARPEN3x3 = { // sharpening filter kernel
//            0.f, -1.f,  0.f,
//            -1.f,  5.f, -1.f,
//            0.f, -1.f,  0.f
//    };
//
//    public static final float[] BLUR3x3 = {
//            0.1f, 0.1f, 0.1f,    // low-pass filter kernel
//            0.1f, 0.2f, 0.1f,
//            0.1f, 0.1f, 0.1f
//    };
//
//    public VCCW04() {
//        loadImage();
//        addKeyListener(this);
//    }
//
//    public Dimension getPreferredSize() {
//        return new Dimension(width, height);
//    }
//
//    public void paint(Graphics g) {
//        g.drawImage(out, 0, 0, null);
//    }
//
//    public static void main(String[] args) {
//        JFrame frame = new JFrame("Image Processing");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        VCCW04 img = new VCCW04();
//        frame.add("Center", img);
//        frame.pack();
//        img.requestFocusInWindow();
//        frame.setVisible(true);
//    }
//
//    private void processing() {
//
//        // keep the original picture
//        BufferedImage originalImage = new BufferedImage(width, height, in.getType());
//        Graphics g_original = originalImage.getGraphics();
//        g_original.drawImage(in, 0, 0, null);
//        g_original.dispose();
//
//        //Compute Gaussian derivatives using Sobel filters
//        //calculate gradient
//        float[][] img_x = imageGradient(SOBELx);
//        float[][] img_y = imageGradient(SOBELy);
//
//        float maxCornerResponse = Float.MIN_VALUE;
//
//
//
//        //normall
////        float[][] cornerResponseValues = calculateAndPrintThreshold(img_x, img_y);
////        System.out.println(cornerResponseValues);
//
//        // after filter
//        List<Corner> filteredCorners = calPrintThresholdReject(img_x, img_y);
//        System.out.println("Number of Points Above Threshold and T: " + filteredCorners.size());
//
//        // side by side display
//        showImagesSideBySide(originalImage, out);
//
//        repaint();
//    }
//
//    private void showImagesSideBySide(BufferedImage image1, BufferedImage image2) {
//        int combinedWidth = image1.getWidth() + image2.getWidth();
//        int maxHeight = Math.max(image1.getHeight(), image2.getHeight());
//
//        // create a new  BufferedImage use to the picture side by side
//        BufferedImage combinedImage = new BufferedImage(combinedWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
//        Graphics g_combined = combinedImage.getGraphics();
//
//        // in the new BufferedImage to draw the picture
//        g_combined.drawImage(image1, 0, 0, null);
//        g_combined.drawImage(image2, image1.getWidth(), 0, null);
//
//        // show the picture
//        JFrame frame = new JFrame("Original and Processed Images");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.add(new JLabel(new ImageIcon(combinedImage)));
//        frame.pack();
//        frame.setVisible(true);
//    }
//    private static void printArray(float[][] array) {
//        for (int i = 0; i < array.length; i++) {
//            for (int j = 0; j < array[i].length; j++) {
//                System.out.print(array[i][j] + " ");
//            }
//            System.out.println();
//        }
//    }
//    //Sobel  gray  and gradient
//    private float[][] imageGradient(float[] SOBEL) {
//        //Filter the image using Sobel filter
//        float[][] gradient = new float[width][height];
//
//        int[] rArray = new int[width*height];
//        int[] gArray = new int[width*height];
//        int[] bArray = new int[width*height];
//
//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++) {
//                Color pixel = new Color(in.getRGB(x, y));
//                rArray[y * width + x] = pixel.getRed();
//                gArray[y * width + x] = pixel.getGreen();
//                bArray[y * width + x] = pixel.getBlue();
//            }
//        }
//
//        for (int y = 1; y < height-1; y++){
//            for (int x = 1; x < width-1; x++){
//                int[] rNeighbour = {
//                        rArray[(y-1)*width+x-1], rArray[(y-1)*width+x], rArray[(y-1)*width+x+1],
//                        rArray[y*width+x-1], rArray[y*width+x], rArray[y*width+x+1],
//                        rArray[(y+1)*width+x-1], rArray[(y+1)*width+x], rArray[(y+1)*width+x+1]
//                };
//                int[] gNeighbour = {
//                        gArray[(y-1)*width+x-1], gArray[(y-1)*width+x], gArray[(y-1)*width+x+1],
//                        gArray[y*width+x-1], gArray[y*width+x], gArray[y*width+x+1],
//                        gArray[(y+1)*width+x-1], gArray[(y+1)*width+x], gArray[(y+1)*width+x+1]
//                };
//                int[] bNeighbour = {
//                        bArray[(y-1)*width+x-1], bArray[(y-1)*width+x], bArray[(y-1)*width+x+1],
//                        bArray[y*width+x-1], bArray[y*width+x], bArray[y*width+x+1],
//                        bArray[(y+1)*width+x-1], bArray[(y+1)*width+x], bArray[(y+1)*width+x+1]
//                };
//
//                float r = 0;
//                float g = 0;
//                float b = 0;
//                for (int i = 0; i < 9; i++) {
//                    r = r + rNeighbour[i] * SOBEL[i];
//                    g = g + gNeighbour[i] * SOBEL[i];
//                    b = b + bNeighbour[i] * SOBEL[i];
//                }
//                gradient[x][y] = (float) (0.299 * r + 0.587 * g + 0.114 * b);
//            }
//        }
//        return gradient;
//    }
//
//    private float[][] calculateAndPrintThreshold(float[][] gradientX, float[][] gradientY) {
//        // Harris  cornot to detection
//        float[][] cornerStrength = calculateHarrisCornerStrength(gradientX, gradientY);
//
//        // find Rmax
//        double Rmax = Double.MIN_VALUE;
//        for (int i = 0; i < width; i++) {
//            for (int j = 0; j < height; j++) {
//                if (cornerStrength[i][j] > Rmax) {
//                    Rmax = cornerStrength[i][j];
//                }
//            }
//        }
//        System.out.println("Max Corner Strength: " + Rmax);
//
//        // 使用阈值过滤角点
//        double qualityLevel = 0.03; // 可调参数
//        double cornerStrengthThreshold = qualityLevel * Rmax;
//
//        // 打印阈值
//        System.out.println("Threshold: " + cornerStrengthThreshold);
//
//        // counter claculate the number of the point pass the filter
//        int count = 0;
//
//        // 遍历角点强度矩阵，统计通过阈值的点的数量
////        Traverse the corner point intensity matrix and count the number of points through the threshold
//        for (int i = 0; i < width; i++) {
//            for (int j = 0; j < height; j++) {
//                if (cornerStrength[i][j] > cornerStrengthThreshold) {
//                    count++;
////                    System.out.println("Filtered Point Coordinates: (" + i + ", " + j + ")");
////                    corners.add(new Corner(i, j, cornerStrength[i][j]));
//                    Graphics g = out.getGraphics();
//                    g.setColor(Color.RED);
//                    g.fillRect(i, j, 3, 3);
//                }
//            }
//        }
//
//
//        // the number of the point that pass the filter
//        System.out.println("Number of Points Above Threshold: " + count);
//
//        return cornerStrength;
//    }
//
//
//
//    private List<Corner> calPrintThresholdReject(float[][] gradientX, float[][] gradientY) {
//        List<Corner> corners = new ArrayList<>();
//
//        // 调用Harris角点检测算法
//        float[][] cornerStrength = calculateHarrisCornerStrength(gradientX, gradientY);
//
//        // 找到最大的质量度量 Rmax
//        double Rmax = Double.MIN_VALUE;
//        for (int i = 0; i < width; i++) {
//            for (int j = 0; j < height; j++) {
//                if (cornerStrength[i][j] > Rmax) {
//                    Rmax = cornerStrength[i][j];
//                }
//            }
//        }
//        System.out.println("Max Corner Strength: " + Rmax);
//
//        // 使用阈值过滤角点
//        double qualityLevel = 0.03;
//        double cornerStrengthThreshold = qualityLevel * Rmax;
//
//        // 打印阈值
//        System.out.println("Threshold: " + cornerStrengthThreshold);
//
//        // 遍历角点强度矩阵，统计通过阈值的点的数量
//        for (int i = 0; i < width; i++) {
//            for (int j = 0; j < height; j++) {
//                if (cornerStrength[i][j] > cornerStrengthThreshold) {
//                    // 检查是否有更强的角点在距离小于T的范围内
//                    boolean isStrongerCorner = isStrongerCornerPresent(corners, i, j, cornerStrength[i][j], 10.0);
//                    if (!isStrongerCorner) {
//                        corners.add(new Corner(i, j, cornerStrength[i][j]));
//                        Graphics g = out.getGraphics();
//                        g.setColor(Color.RED);
//                        g.fillRect(i, j, 3, 3);
//                    }
//                }
//            }
//        }
//
//        // 使用 T 进行剔除
//        List<Corner> filteredCorners = filterCorners(corners, 10.0);
//
//        // 打印通过阈值和距离 T 过滤后的角点数量
//        System.out.println("Number of Points Above Threshold and T: " + filteredCorners.size());
//
//        return filteredCorners;
//    }
//
//    private List<Corner> filterCorners(List<Corner> corners, double distanceThreshold) {
//        List<Corner> filteredCorners = new ArrayList<>();
//
//        for (Corner corner : corners) {
//            // 检查是否有更强的角点在距离小于T的范围内
//            if (!isStrongerCornerPresent(filteredCorners, corner.x, corner.y, corner.strength, distanceThreshold)) {
//                filteredCorners.add(corner);
//            }
//        }
//
//        return filteredCorners;
//    }
//    private boolean isStrongerCornerPresent(List<Corner> corners, int x, int y, float currentStrength, double distanceThreshold) {
//        for (Corner corner : corners) {
//            // 计算两点之间的欧几里得距离
//            double distance = Math.sqrt(Math.pow(x - corner.x, 2) + Math.pow(y - corner.y, 2));
//
//            // 检查是否存在更强的角点在距离小于T的范围内
//            if (distance < distanceThreshold && corner.strength > currentStrength) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//
//
//
//    //calculate the every response point  in this picture
//    private float[][] calculateHarrisCornerStrength(float[][] gradientX, float[][] gradientY) {
//        int width = gradientX.length;
//        int height = gradientX[0].length;
//        float[][] cornerStrength = new float[width][height];
//
//        // 参数
//        float k = 0.06f;  // 可调参数
//        int windowSize = 3;  // 窗口大小，可调参数
//
//        // 定义Gaussian window
//        float[] gaussW = {
//                1.f, 2.f, 1.f,
//                2.f, 4.f, 2.f,
//                1.f, 2.f, 1.f
//        };
//
//        // 归一化Gaussian window
//        float sum = 0;
//        for (int i = 0; i < 9; i++) {
//            sum += gaussW[i];
//        }
//        for (int i = 0; i < 9; i++) {
//            gaussW[i] /= sum;
//        }
//
//        // 计算Harris响应函数
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                // 计算M矩阵和权重的和
//                float sumIxIx = 0, sumIyIy = 0, sumIxIy = 0;
//                float sumWeights = 0.0f;
//
//                for (int i = -windowSize/2; i <= windowSize/2; i++) {
//                    for (int j = -windowSize/2; j <= windowSize/2; j++) {
//                        int neighborX = x + i;
//                        int neighborY = y + j;
//
//                        if (neighborX >= 0 && neighborX < width && neighborY >= 0 && neighborY < height) {
//                            float neighborIx = gradientX[neighborX][neighborY];
//                            float neighborIy = gradientY[neighborX][neighborY];
//
//                            // 获取Gaussian window的权重
//                            float weight = gaussW[(i + windowSize / 2) * windowSize + (j + windowSize / 2)];
//
//                            sumWeights += weight;
//
//                            sumIxIx += weight * neighborIx * neighborIx;
//                            sumIyIy += weight * neighborIy * neighborIy;
//                            sumIxIy += weight * neighborIx * neighborIy;
//                        }
//                    }
//                }
//
//                // 归一化梯度平方和
//                sumIxIx /= sumWeights;
//                sumIyIy /= sumWeights;
//                sumIxIy /= sumWeights;
//
//                // calculate Harris function of horris
//                float detM = sumIxIx * sumIyIy - sumIxIy * sumIxIy;
//                float traceM = sumIxIx + sumIyIy;
//                cornerStrength[x][y] = detM - k * traceM * traceM;
//            }
//        }
//
//        return cornerStrength;
//    }
//
//
//
//    public void keyPressed(KeyEvent ke) {
//        if (ke.getKeyCode() == KeyEvent.VK_ESCAPE)
//            System.exit(0);
//
//        if (ke.getKeyChar() == 's' || ke.getKeyChar() == 'S') {// Save the processed image
//            saveImage();
//        } else if (ke.getKeyChar() == 'p' || ke.getKeyChar() == 'P') {// Image Processing
//            processing();
//        }
//    }
//
//    private void loadImage() {
//        try {
//            in = ImageIO.read(new File("Room.jpg"));
//            System.out.println("Image Type: " + in.getType());
//
//            width = in.getWidth();
//            height = in.getHeight();
//            //out = in;
//            out = new BufferedImage(width, height, in.getType());
//            Graphics g_out = out.getGraphics();
//            g_out.drawImage(in, 0, 0, null);
//            g_out.dispose();
//        } catch (IOException e) {
//            System.out.println("Image could not be read");
//            System.exit(1);
//        }
//    }
//
//    private void saveImage() {
//        try {
//            ImageIO.write(out, "jpg", new File("DaffodilG_corners.jpg"));
//        } catch (IOException ex) {
//            System.out.println("Image could not be written");
//            System.exit(2);
//        }
//    }
//    //gray picture
//    private int getGrayScale(int rgb) {
//        Color color = new Color(rgb);
//        int r = color.getRed();
//        int g = color.getGreen();
//        int b = color.getBlue();
//        return (int) (0.299 * r + 0.587 * g + 0.114 * b);
//    }
//    // blur picture
//    private void applyMedianBlur() {
//        int windowSize = 3; // 调整窗口大小
//
//        for (int y = 1; y < height - 1; y++) {
//            for (int x = 1; x < width - 1; x++) {
//                // 收集窗口内的像素值
//                int[] valuesR = new int[windowSize * windowSize];
//                int[] valuesG = new int[windowSize * windowSize];
//                int[] valuesB = new int[windowSize * windowSize];
//
//                int index = 0;
//                for (int dy = -windowSize / 2; dy <= windowSize / 2; dy++) {
//                    for (int dx = -windowSize / 2; dx <= windowSize / 2; dx++) {
//                        int pixelX = x + dx;
//                        int pixelY = y + dy;
//
//                        Color pixel = new Color(in.getRGB(pixelX, pixelY));
//                        valuesR[index] = pixel.getRed();
//                        valuesG[index] = pixel.getGreen();
//                        valuesB[index] = pixel.getBlue();
//
//                        index++;
//                    }
//                }
//
//                // 对像素值进行排序
//                Arrays.sort(valuesR);
//                Arrays.sort(valuesG);
//                Arrays.sort(valuesB);
//
//                // 将排序后的数组的中值作为输出像素的颜色
//                int medianR = valuesR[windowSize * windowSize / 2];
//                int medianG = valuesG[windowSize * windowSize / 2];
//                int medianB = valuesB[windowSize * windowSize / 2];
//
//                // 将计算得到的中值颜色设置为输出图像 (x, y) 处的颜色
//                out.setRGB(x, y, new Color(medianR, medianG, medianB).getRGB());
//            }
//        }
//    }
//
//
//    public void keyReleased(KeyEvent e) {
//
//    }
//
//    public void keyTyped(KeyEvent e) {
//
//    }
//
//}
