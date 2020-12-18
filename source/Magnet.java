import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;

public class Magnet extends JComponent {
    public TAG tag;
    public JLabel label;

    public Magnet(TAG tag, LightTable lt) {
        this.setLayout(new BorderLayout());
        this.setSize(60,60);
        this.setLocation(lt.getWidth()/3, lt.getHeight()/3);
        label = new JLabel(tag.toString());
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBackground(Color.RED);
        label.setOpaque(true);
        this.add(label);
        this.setBackground(Color.RED);
        this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        this.tag = tag;
        addListeners();
    }

    public TAG getTag() {
        return this.tag;
    }


    //called when magnet is dragged
    public void updateLocation(int newX, int newY) {
        this.setLocation(newX, newY);
    }

    //pass events to LightTable
    public void addListeners() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                LightTable.mag_selected = (Magnet) e.getComponent();
                PhotoLayout.lt.revalidate();
                PhotoLayout.lt.repaint();
            }

            @Override
            public void mousePressed (MouseEvent e){
                Component source=(Component)e.getSource();
                source.getParent().dispatchEvent(e);
                return;
            }

            @Override
            public void mouseReleased (MouseEvent e){

            }

            @Override
            public void mouseEntered (MouseEvent e){

            }

            @Override
            public void mouseExited (MouseEvent e){

            }

            @Override
            public void mouseMoved (MouseEvent e){

            }
        });

        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged (MouseEvent e){
                Component source=(Component)e.getSource();
                source.getParent().dispatchEvent(e);
                return;
            }
        });
    }
}
