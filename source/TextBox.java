import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import java.lang.Math;
import javax.swing.BorderFactory;

public class TextBox extends JPanel {
    Point start, end;
    int x, y;
    int height, width;
    public static Color sticky_note = new Color (255, 255, 153);
    ArrayList<String> text;

    public TextBox(Point first, Point last) {
        this.start = first;
        this.end = last;

        x = Math.abs((int) first.getX());
        y = Math.abs((int) first.getY());

        this.height = (int) (start.getY() - end.getY());
        this.width = (int) (start.getX() - end.getX());

        text = new ArrayList<String>();
    }

    public Point getStart() {
        return this.start;
    }

    public Point getEnd() {
        return this.end;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void addText(String s) {
        text.add(s);
    }

    public void backspace() {
        if (!text.isEmpty()) {
            text.remove(text.size()-1);
        }
    }

    public ArrayList<String> getText() {
        return text;
    }
}
