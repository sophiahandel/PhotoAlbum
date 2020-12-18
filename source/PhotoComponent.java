import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.lang.Math;
import java.awt.FontMetrics;

public class PhotoComponent extends JComponent {
    public ImageIcon pic;
    Image image; //global to maintain original image size for scaling
    boolean school, work, family, vacation, flipped;
    static ArrayList<Point> stroke = new ArrayList<Point>();
    ArrayList<ArrayList<Point>> draw = new ArrayList<>();
    public ArrayList<TextBox> write = new ArrayList<TextBox>();
    Point start, curr;
    String s = "";
    private static Font serifFont = new Font("Serif", Font.BOLD, 12);
    public double scaleFactor = 1;
    boolean rightClick = false;
    ArrayList<Rectangle> rects = new ArrayList<>();
    ArrayList<Rectangle> circles = new ArrayList<>();
    static ArrayList<Point2D> gesture = new ArrayList<>();

    public PhotoComponent(ImageIcon p) {
        pic = p;
        image = p.getImage();
        school = false;
        work = false;
        family = false;
        vacation = false;
        flipped = false;
        this.setSize(new Dimension(pic.getIconWidth(), pic.getIconHeight()));
        addListeners();
    }

    public void paintComponent(Graphics g) {
        //default
        super.paintComponent(g);
        //cast to Graphics2D
        Graphics2D g2 = (Graphics2D) g;
        g2.scale(this.scaleFactor, this.scaleFactor);
        //center
        int left = ((this.getWidth() / 2) - (pic.getIconWidth() / 2));
        int top = ((this.getHeight() / 2) - (pic.getIconHeight() / 2));
        g2.setClip(left, top, pic.getIconWidth(), pic.getIconHeight());

        //get focus for keypress events
        this.setFocusable(true);
        this.requestFocus();

        if (flipped) {//draw white rect and enable draw/write if in flipped state
            g2.setColor(Color.WHITE);
            g2.fillRect(left, top, pic.getIconWidth(), pic.getIconHeight());
            g2.setColor(Color.BLACK);
            for (ArrayList<Point> line : draw) { //connect each point to the next point with a line for drawing
                for (int i = 0; i < line.size() - 1; i++) {
                    Point p1 = line.get(i);
                    Point p2 = line.get(i + 1);
                    g2.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
                }
            }
            if (curr != null) {
                g2.setColor(TextBox.sticky_note); //if currently drawing, paint curr for drag effect
                int width = Math.abs((int) (curr.getX() - start.getX()));
                int height = Math.abs((int) (curr.getY() - start.getY()));
                g2.fill3DRect((int) start.getX(), (int) start.getY(), width, height, true);
            }
            for (TextBox rect : write) { //draw all textBoxes in write array
                g2.setColor(TextBox.sticky_note);
                g2.fillRect((int) rect.getStart().getX(), (int) rect.getStart().getY(), Math.abs(rect.getWidth()),
                        Math.abs(rect.getHeight()));
                if (rect == write.get(write.size()-1) && PhotoLayout.edit_mode.toString().equals("text")) {
                    g2.setColor(Color.BLACK); //if most recent textBox change color to black for active border
                } else {
                    g2.setColor(TextBox.sticky_note);
                }
                g2.drawRect((int) rect.getStart().getX(), ((int) rect.getStart().getY()), Math.abs(rect.getWidth()),
                        Math.abs(rect.getHeight())); //draw border
                FontMetrics fm = g2.getFontMetrics();
                g2.setFont(serifFont);
                g2.setColor(Color.BLACK);
                fm = g2.getFontMetrics();
                String type = "";
                int stringWidth = fm.stringWidth(type);
                int height = g2.getFontMetrics().getHeight();
                int x = (int)(rect.getStart().getX() - rect.getWidth()/10) - (stringWidth/4);
                int y = (int)(rect.getStart().getY() - rect.getHeight()/10) + height / 2;
                for (String text : rect.getText()) {
                    //wrap text to next line if reaches textBox bounds
                    if (fm.stringWidth(type + text) > Math.abs(rect.getWidth()) - Math.abs(rect.getWidth()/6)) {
                        type = ""; //clear type string
                        y +=  g2.getFontMetrics().getHeight();
                    }
                    type += text;
                    g2.setColor(Color.black);
                    g2.drawString(type, x, y);
                }
            }
            g2.setColor(Color.BLACK);
            if (!rects.isEmpty()) {
                for (Rectangle rect : rects) {
                    g2.drawRect((int) rect.getBounds().getX(), (int) rect.getBounds().getY(), (int) rect.getBounds().getWidth(), (int) rect.getBounds().getHeight());
                }
            }
            if (!circles.isEmpty()) {
                for (Rectangle circle : circles) {
                    g2.drawOval((int) circle.getBounds().getX(), (int) circle.getBounds().getY(), (int) circle.getBounds().getWidth(), (int) circle.getBounds().getHeight());
                }
            }
            if (!gesture.isEmpty()) {
                g2.setColor(Color.RED);
                for (int i = 0; i < gesture.size() - 1; i++) {
                    Point2D p1 = gesture.get(i);
                    Point2D p2 = gesture.get(i + 1);
                    g2.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
                }
            }
        } else { //if not flipped, paint as normal
            pic.paintIcon(this, g2, left, top);
            if (!gesture.isEmpty()) {
                g2.setColor(Color.RED);
                for (int i = 0; i < gesture.size() - 1; i++) {
                    Point2D p1 = gesture.get(i);
                    Point2D p2 = gesture.get(i + 1);
                    g2.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
                }
            }
        }
    }

    public void setSchool() {
        school = !(this.school);
        PhotoLayout.lt.updateThumbnails();
    }

    public boolean getSchool() {
        return this.school;
    }

    public void setWork() {
        work = !(this.work);
        PhotoLayout.lt.updateThumbnails();
    }

    public boolean getWork() {
        return this.work;
    }

    public void setFamily() {
        family = !(this.family);
        PhotoLayout.lt.updateThumbnails();
    }

    public boolean getFamily() {
        return this.family;
    }

    public void setVacation() {
        vacation = !(this.vacation);
        PhotoLayout.lt.updateThumbnails();
    }

    public boolean getVacation() {
        return this.vacation;
    }

    public double getScaleFactor() {
        return this.scaleFactor;
    }

    public void setScaleFactor(double sf) {
        this.scaleFactor = sf;
    }

    public void rescale() { //called when switching between grid and photo view to scale images
        Image scale  = image.getScaledInstance((int) (image.getWidth(null) * scaleFactor), (int) (image.getHeight(null) * scaleFactor),  Image.SCALE_DEFAULT);
        this.pic = new ImageIcon(scale);
        this.setSize(new Dimension(pic.getIconWidth(), pic.getIconHeight()));
        revalidate();
    }

    public void addListeners() {

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!(isEnabled())) {
                    Component source=(Component)e.getSource();
                    source.getParent().dispatchEvent(e);
                    return;
                } if (e.getClickCount() == 2) { //double click changes flip state (photo mode) or mode (grid mode)
                    flipped = !flipped;
                }
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!(isEnabled())) {
                    return;
                }
                if (e.getButton() == MouseEvent.BUTTON3) {
                    rightClick = true;
                    Point2D Point = new Point(e.getX(), e.getY());
                    gesture.add(Point);
                } else {
                    rightClick = false;
                }
                if (!rightClick && flipped) {
                    if (PhotoLayout.edit_mode.toString().equals("pen")) {
                        Point point = new Point(e.getX(), e.getY()); //stroke start point for draw
                        stroke.add(point);
                    } else if (PhotoLayout.edit_mode.toString().equals("text")) {
                        start = new Point(e.getX(), e.getY()); //textBox start point for text
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!(isEnabled())) {
                    return;
                } if (rightClick) {
                    rightClick = false;
                    dollar.Result result = PhotoLayout.recognizer.recognize(gesture);
                    //perform specific actions based on recognized gesture
                    if (result.getName().equals("delete")) {
                        PhotoLayout.deletePhoto();
                    } else if (result.getName().equals("v")) {
                        PhotoLayout.nextPhoto();
                    } else if (result.getName().equals("caret")) {
                        PhotoLayout.previousPhoto();
                    } else if (result.getName().equals("star")) {
                        PhotoLayout.toggleFamily();
                    } else if (result.getName().equals("check")) {
                        PhotoLayout.toggleSchool();
                    } else if (result.getName().equals("x")) {
                        PhotoLayout.toggleWork();
                    } else if (result.getName().equals("pigtail")) {
                        PhotoLayout.toggleVacation();
                    } else if (result.getName().equals("rectangle") && flipped) {
                        rects.add(result.getBoundingBox());
                    } else if (result.getName().equals("circle") && flipped) {
                        circles.add(result.getBoundingBox());
                    }
                    PhotoLayout.status.setText("Recognized as " + result.getName() +
                            " at a score of " + result.getScore());
                    gesture.clear();
                    repaint();
                }
                if (PhotoLayout.edit_mode.toString().equals("pen")) {
                    //add stroke to drawing on mouse release so new stroke can be started
                    ArrayList<Point> line = new ArrayList<>();
                    for (int i = 0; i < stroke.size(); i++) {
                        line.add(stroke.get(i));
                    }
                    stroke.clear();
                    draw.add(line);
                } else if (PhotoLayout.edit_mode.toString().equals("text")) {
                    TextBox text = new TextBox(start, curr);
                    write.add(text);
                    start = null;
                    curr = null;
                }
                repaint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!(isEnabled())) {
                    return;
                } if (rightClick) {
                    Point Point = new Point(e.getX(), e.getY());
                    gesture.add(Point);
                    repaint();
                }
                if (!rightClick && flipped) {
                    if (PhotoLayout.edit_mode.toString().equals("pen")) { // add new points to stroke as mouse is dragged
                        Point point = new Point(e.getX(), e.getY());
                        stroke.add(point);
                        draw.add(stroke);
                    } else if (PhotoLayout.edit_mode.toString().equals("text")) { // update new current drag spot of textbox
                        curr = new Point(e.getX(), e.getY());
                    }
                }
                repaint();
            }
        });

        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                repaint();
            }
        });

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!(isEnabled())) {
                    return;
                }
                if (flipped && PhotoLayout.edit_mode.toString().equals("text")) {
                    TextBox currBox = write.get(write.size()-1);
                    char typedText = e.getKeyChar();
                    if (typedText == KeyEvent.VK_BACK_SPACE || e.getKeyChar() == KeyEvent.VK_DELETE) { //remove last char w backapce
                        currBox.backspace();
                    } else if (Character.isAlphabetic(typedText) || e.getKeyChar() == KeyEvent.VK_SPACE){ //if letter or space, add
                        s += typedText;
                        currBox.addText(s);
                        s = "";
                    }
                    repaint();
                }
            }
        });
    }
}
