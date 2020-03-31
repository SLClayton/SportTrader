package tools;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tools.printer.print;


public class ScreenReader {

    public Tesseract tesseract;
    public Robot robot;
    public JFrame jFrame;
    public JPanel jPanel;
    public static Pattern runner_regex = Pattern.compile("[a-z]{3,}+\\d{1,2}/\\d{1,2}");
    int runners_width = 196;
    int runners_height = 170;

    int winner_height = 28;
    int winner_width = 170;


    public ScreenReader(){
        jFrame = new JFrame();
        jPanel = new JPanel();
        jFrame.add(jPanel);

        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        tesseract = new Tesseract();
        tesseract.setDatapath("resources");
        tesseract.setLanguage("eng");
    }



    public void show(BufferedImage image){
        jPanel.removeAll();
        jPanel.add(new JLabel(new ImageIcon(image)));
        jFrame.setSize(new Dimension(image.getWidth() + 50, image.getHeight() + 50));
        jFrame.setVisible(true);
        jPanel.revalidate();
        jPanel.repaint();
    }


    public static void _show(BufferedImage image){
        JPanel jPanel = new JPanel();
        jPanel.add(new JLabel(new ImageIcon(image)));
        JFrame jFrame = new JFrame();
        jFrame.setSize(new Dimension(image.getWidth() + 50, image.getHeight() + 50));
        jFrame.add(jPanel);
        jFrame.setVisible(true);
    }



    public static BufferedImage grey(BufferedImage image){

        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = result.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }


    public static BufferedImage bw(BufferedImage image){

        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics g = result.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }


    public static BufferedImage black(BufferedImage image, double threshold){

        Color white = new Color(255,255,255);
        Color black = new Color(0,0,0);
        int t = (int) ((black.getRGB() - white.getRGB()) * threshold);


        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);

        for (int x=0; x<image.getWidth(); x++){
            for (int y=0; y<image.getHeight(); y++){

                if (image.getRGB(x, y) < t){
                    result.setRGB(x, y, black.getRGB());
                }
                else{
                    result.setRGB(x, y, white.getRGB());
                }

            }
        }
        return result;
    }


    public static BufferedImage scale(BufferedImage image, double factor){
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage after = new BufferedImage((int) (w*factor), (int) (h*factor), BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(factor, factor);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(image, after);
        return after;
    }


    public static BufferedImage brightness(BufferedImage image, double scale, int offset){
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        RescaleOp rescaleOp = new RescaleOp((float) scale, offset, null);
        rescaleOp.filter(image, result);
        return result;
    }


    public List<String> extractRunners(String raw){
        String text = raw.replaceAll("\\s", "").toLowerCase();
        Matcher matches = runner_regex.matcher(text);

        List<String> runners = new ArrayList<>();
        while (matches.find()){
            String runner = matches.group();

            int i = 0;
            for (i = 0; !Character.isDigit(runner.charAt(i)); i++){}

            runner = runner.substring(0, i) + " " + runner.substring(i);

            runners.add(runner);
        }
        return runners;
    }


    public String getText(BufferedImage image) {
        try {
            return tesseract.doOCR(image);
        }
        catch (TesseractException e){
            e.printStackTrace();
            return null;
        }
    }


    public String findWinner(int x, int y, Collection<String> possible_winners){
        BufferedImage screenshot = winner_screenshot(x, y);
        screenshot = scale(screenshot, 4.5);
        show(screenshot);

        for (BufferedImage this_image: black_range(screenshot, 0.6, 0.7, 5)){
            String rawtext = getText(this_image).toLowerCase().replaceAll("\\s", "");

            for (String horse_name: possible_winners){
                if (rawtext.contains(horse_name)){
                    return horse_name;
                }
            }
        }
        return null;
    }


    public List<String> extractRunnersFromScreen(int x, int y, double black_from, double black_to, int num_images, boolean show) {

        BufferedImage image = screenshot(x, y, runners_width, runners_height);
        image = scale(image, 4.0);
        List<BufferedImage> images = black_range(image, 0.6, 0.7, 4);

        List<String> runners = new ArrayList<>();
        for (BufferedImage this_image: images){
            runners.addAll(extractRunners(getText(this_image)));
        }

        if (show) {
            show(scale(image, 400.0/image.getWidth()));
        }

        return runners;
    }



    public BufferedImage screenshot(int x, int y, int width, int height) {

        if (robot == null){
            try {
                robot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
                return null;
            }
        }

        BufferedImage image = robot.createScreenCapture(new Rectangle(x, y, width, height));
        return image;
    }


    public BufferedImage runners_screenshot(int x, int y){
        return screenshot(x, y, runners_width, runners_height);
    }

    public BufferedImage winner_screenshot(int x, int y){
        return screenshot(x, y, winner_width, winner_height);
    }


    public static List<BufferedImage> black_range(BufferedImage image, double from, double to, int num_images){

        List<BufferedImage> images = new ArrayList<>(num_images);
        double interval = (to - from) / (num_images - 1);

        for (int i=0; i<num_images; i++){
            double t = from + (i * interval);
            images.add(black(image, t));
        }

        return images;
    }


    public String getScreenText(int x, int y, double threshold, boolean show)
            throws AWTException, TesseractException {

        Instant a = Instant.now();

        if (robot == null){
            robot = new Robot();
        }

        int width = 196;
        int height = 200;
        BufferedImage image = robot.createScreenCapture(new Rectangle(x, y, width, height));

        Instant b = Instant.now();

        image = scale(image, 4.0);
        image = black(image, threshold);

        Instant c = Instant.now();

        if (show) {
            show(scale(image, 400.0/image.getWidth()));
        }

        String text = getText(image);

        Instant d = Instant.now();

        //print("SS: " + (b.toEpochMilli() - a.toEpochMilli()));
        //print("PR: " + (c.toEpochMilli() - b.toEpochMilli()));
        //print("RD: " + (d.toEpochMilli() - c.toEpochMilli()));
        //print("ALL: " + (d.toEpochMilli() - a.toEpochMilli()));


        return text;
    }


    public static void main(String[] args){
        ScreenReader sr = new ScreenReader();


        for (BufferedImage i: black_range(sr.screenshot(2040, 400, 300, 300), 0.6, 0.7, 5)){
            _show(i);
        }
    }

}
