import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

public class HarrisCornerDetector2 extends Component implements KeyListener {

    private BufferedImage in, out;
    private static final int radius = 5; // 可以根
    int width, height;
    File inputFile;

    public HarrisCornerDetector2() {
        loadImage();
        addKeyListener(this);
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
        HarrisCornerDetector2 img = new HarrisCornerDetector2();
        frame.add("Center", img);
        frame.pack();
        img.requestFocusInWindow();
        frame.setVisible(true);
    }

    private void processing() {
        // 将输入图像转换为灰度
        BufferedImage grayImage = convertToGrayScale(in);

        // 调用Harris角点检测算法
        HarrisCornerDetectionAlgorithm(grayImage);

        // Update 'out' to display the modified grayImage
        out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = out.getGraphics();
        g.drawImage(grayImage, 0, 0, null);

        repaint();
    }




    private void HarrisCornerDetectionAlgorithm(BufferedImage grayImage) {
        // 调用Harris角点检测算法
        double[][] cornerStrength = calculateHarrisCornerStrength(grayImage);


        // 找到最大的质量度量 Rmax
        double Rmax = Double.MIN_VALUE;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (cornerStrength[i][j] > Rmax) {
                    Rmax = cornerStrength[i][j];
                }
            }
        }
        System.out.println("Max Corner Strength: " + Rmax);

        // 使用阈值过滤角点
        double qualityLevel = 0.05; // 可调参数
        double cornerStrengthThreshold = qualityLevel * Rmax;

        // 打印阈值
        System.out.println("Threshold: " + cornerStrengthThreshold);


        // Detect and mark corners
        // 计数器
        int cornerCount = 0;

// Detect and mark corners
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (cornerStrength[i][j] > cornerStrengthThreshold) {
                    // Increment the counter for each corner
                    cornerCount++;

                    // Mark the pixel as a corner
                    drawCircle(out, i, j);
                    out.setRGB(i, j, Color.RED.getRGB());

                }
            }
        }


// 打印超出阈值的角点数量
        System.out.println("Number of corners above threshold: " + cornerCount);



    }

    private void drawCircle(BufferedImage image, int centerX, int centerY) {
        System.out.println("Drawing circle at (" + centerX + ", " + centerY + ")");
        Graphics2D g = image.createGraphics();
        g.setColor(Color.RED); // 自定义颜色
        int diameter = 2 * radius;
        g.fillOval(centerX - radius, centerY - radius, diameter, diameter);
        g.dispose();
    }

    private BufferedImage convertToGrayScale(BufferedImage colorImage) {
        int width = colorImage.getWidth();
        int height = colorImage.getHeight();

        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = colorImage.getRGB(i, j);
                int grayValue = getGrayScale(rgb);
                grayImage.setRGB(i, j, new Color(grayValue, grayValue, grayValue).getRGB());
            }
        }

        return grayImage;
    }

    private int getGrayScale(int rgb) {
        Color color = new Color(rgb);
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        return (int) (0.299 * r + 0.587 * g + 0.114 * b);
    }

    private double[][] calculateHarrisCornerStrength(BufferedImage grayImage) {
        int width = grayImage.getWidth();
        int height = grayImage.getHeight();
        double[][] cornerStrength = new double[width][height];

        // 参数
        double k = 0.06;  // 可调参数
        int windowSize = 3;  // 窗口大小，可调参数

        // 计算梯度
        double[][][] gradients = calculateGradients(grayImage);
// 计算Harris响应函数
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
//                double IxIx = gradients[x][y][0] * gradients[x][y][0];
//                double IyIy = gradients[x][y][1] * gradients[x][y][1];
//                double IxIy = gradients[x][y][0] * gradients[x][y][1];

                // 计算M矩阵
                double sumIxIx = 0, sumIyIy = 0, sumIxIy = 0;
                for (int i = -windowSize/2; i <= windowSize/2; i++) {
                    for (int j = -windowSize/2; j <= windowSize/2; j++) {
                        int neighborX = x + i;
                        int neighborY = y + j;

                        if (neighborX >= 0 && neighborX < width && neighborY >= 0 && neighborY < height) {
                            double neighborIx = gradients[neighborX][neighborY][0];
                            double neighborIy = gradients[neighborX][neighborY][1];

                            sumIxIx += neighborIx * neighborIx;
                            sumIyIy += neighborIy * neighborIy;
                            sumIxIy += neighborIx * neighborIy;
                        }
                    }
                }

                // 计算Harris响应函数
                double detM = sumIxIx * sumIyIy - sumIxIy * sumIxIy;
                double traceM = sumIxIx + sumIyIy;
                cornerStrength[x][y] = detM - k * traceM * traceM;








            }
        }



        return cornerStrength;
    }

    private double[][][] calculateGradients(BufferedImage grayImage) {
        int width = grayImage.getWidth();
        int height = grayImage.getHeight();
        double[][][] gradients = new double[width][height][2];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int neighborX = (x + 1) % width; // 处理图像边界
                int neighborY = (y + 1) % height;

                int pixelX = grayImage.getRGB(neighborX, y) & 0xFF;
                int pixelY = grayImage.getRGB(x, neighborY) & 0xFF;

                gradients[x][y][0] = pixelX;  // 水平梯度
                gradients[x][y][1] = pixelY;  // 垂直梯度
            }
        }



        return gradients;
    }

    private void loadImage() {
        try {
            in = ImageIO.read(new File("Room.jpg"));
            width = in.getWidth();
            height = in.getHeight();
            out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = out.getGraphics();
            g.drawImage(in, 0, 0, null);
        } catch (IOException e) {
            System.out.println("Image could not be read");
            System.exit(1);
        }
    }

    private void saveImage() {
        try {
            ImageIO.write(out, "jpg", new File("DaffodilG.jpg"));
        } catch (IOException ex) {
        }
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

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }
}
