package tca.experiments.fundamentaldiagrams;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class DataFileGraphics extends JPanel {

    private double[] dataX; // Array to store data from the first column
    private double[] dataY; // Array to store data from the second column
    private String xAxisName; // Name of the x-axis
    private String yAxisName; // Name of the y-axis
    private String graphicName; // Name of the graphic

    public DataFileGraphics(String filename, boolean flow) {
        // Set the preferred size of the panel
        setPreferredSize(new Dimension(500, 500));
        // Read data from the .data file
        xAxisName = "щільність, ТЗ/клітинку";
        if(flow){
            yAxisName = "густина потоку, ТЗ/часокрок";
        }else{
            yAxisName = "середня швидкість, клітинки/часокрок";
        }

        graphicName = filename;

        readDataFromFile(filename,flow);
    }

    private void readDataFromFile(String filename, boolean flow) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            // Assuming the data is separated by commas and has two columns
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\t");
                if (values.length >= 2) {

                    double x = Double.parseDouble(values[0]);
                    if (flow) {
                        double y = Double.parseDouble(values[1]);
                        addDataPoint(x, y);
                    }else{
                        double y = Double.parseDouble(values[2]);
                        addDataPoint(x, y);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addDataPoint(double x, double y) {
        if (dataX == null || dataY == null) {
            dataX = new double[1];
            dataY = new double[1];
        } else {
            int newSize = dataX.length + 1;
            double[] newDataX = new double[newSize];
            double[] newDataY = new double[newSize];
            System.arraycopy(dataX, 0, newDataX, 0, dataX.length);
            System.arraycopy(dataY, 0, newDataY, 0, dataY.length);
            dataX = newDataX;
            dataY = newDataY;
        }
        dataX[dataX.length - 1] = x;
        dataY[dataY.length - 1] = y;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (dataX != null && dataY != null) {
            double maxX = getMaxValue(dataX);
            double maxY = getMaxValue(dataY);
            int scaledWidth = getWidth() - 20;
            int scaledHeight = getHeight() - 20;

            // Draw x-axis name
            g.drawString(xAxisName, getWidth() / 2 - 10, getHeight() - 5);
            // Draw y-axis name
            g.drawString(yAxisName, 10, getHeight() / 2);
            // Draw graphic name
            g.drawString(graphicName, 10, 20);

            // Draw grid lines and values
            g.setColor(Color.LIGHT_GRAY);
            int numGridLines = 10;
            for (int i = 0; i < numGridLines; i++) {
                int x1 = 10 + i * (scaledWidth / numGridLines);
                int y1 = 10;
                int x2 = x1;
                int y2 = getHeight() - 10;
                g.drawLine(x1, y1, x2, y2);
                // Draw x-axis grid values
                String value = String.format("%.2f", (i / (double) numGridLines) * maxX);
                g.drawString(value, x1 - 10, y1 + 480);
            }
            for (int i = 0; i < numGridLines; i++) {
                int x1 = 10;
                int y1 = 10 + i * (scaledHeight / numGridLines);
                int x2 = getWidth() - 10;
                int y2 = y1;
                g.drawLine(x1, y1, x2, y2);
                // Draw y-axis grid values
                String value = String.format("%.2f", (1 - (i / (double) numGridLines)) * maxY);
                g.drawString(value, x1 , y1 + 5);
            }

            // Draw data points
            for (int i = 0; i < dataX.length; i++) {
                int x = 10 + (int) ((double) dataX[i] / maxX * scaledWidth);
                int y = getHeight() - 10 - (int) ((double) dataY[i] / maxY * scaledHeight);
                g.setColor(Color.BLUE);
                g.fillOval(x - 3, y - 3, 6, 6);
            }
        }
    }

    public void setAxisNames(String xAxisName, String yAxisName) {
        this.xAxisName = xAxisName;
        this.yAxisName = yAxisName;
    }

    public void setGraphicName(String graphicName) {
        this.graphicName = graphicName;
    }

    private double getMaxValue(double[] array) {
        double max = Double.MIN_VALUE;
        for (double value : array) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public static void main(String[] args) {


//        URL packageUrl=classLoader.getResource("C:\\Users\\Owner\\Documents\\Downloads\\smtca-sourceCOPY\\");
        String directoryPath = "C:\\Users\\Owner\\Desktop\\tcaBlavt\\";
        // Create a File object for the directory
        File directory = new File(directoryPath);

        // Get the list of files in the directory
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".data"));

        List<String> names = new ArrayList<>() {
        };
        if (files != null) {
            // Process each file
            for (File file : files) {
                if (file.isFile()) {
                    // Read the file as needed
                    System.out.println("File: " + file.getName());
//                    System.out.println("Path: " + file.getAbsolutePath());
                names.add(file.getName());
                }
            }
        } else {
            System.out.println("No files found in the directory.");
        }

        names.remove(names.remove(names.size()-1));

        System.out.println(names.size());
        for (String f:
             names) {
            String filename = f;
            boolean flow = true;
            JFrame frame = new JFrame(f);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new DataFileGraphics(filename, flow));
            frame.pack();
            frame.setVisible(true);
            try {
                // Get the size of the screen
                Thread.sleep(2000);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Robot robot = new Robot();
                BufferedImage image = robot.createScreenCapture(new Rectangle(frame.getLocationOnScreen(), frame.getSize()));
                File outputFile = new File("flow-"+f+".png");
                ImageIO.write(image, "png", outputFile);
                System.out.println("Diagram saved as image: " + outputFile.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (String f:
                names) {
            String filename = f;
            boolean flow = true;
            JFrame frame = new JFrame(f);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new DataFileGraphics(filename, !flow));
            frame.pack();
            frame.setVisible(true);
            try {
                // Get the size of the screen
                Thread.sleep(2000);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Robot robot = new Robot();
                BufferedImage image = robot.createScreenCapture(new Rectangle(frame.getLocationOnScreen(), frame.getSize()));
                File outputFile = new File("speed-"+f+".png");
                ImageIO.write(image, "png", outputFile);
                System.out.println("Diagram saved as image: " + outputFile.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
