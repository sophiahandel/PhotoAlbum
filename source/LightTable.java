import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.Timer;
import java.util.HashMap;
import java.awt.event.*;


public class LightTable extends JComponent {

    public static ArrayList<PhotoComponent> thumbnails;
    private static final int row_count = 3; //number of images per row
    private static final int offset = 20; //padding to left and above first image
    static PhotoComponent selected = PhotoLayout.images.get(PhotoLayout.curr);
    static Magnet mag_selected;
    static ArrayList<Magnet> magnets = new ArrayList<>();
    static HashMap<PhotoComponent, Point> locations = new HashMap();
    public static Timer timer;

    public LightTable(ArrayList<PhotoComponent> images, ArrayList<Magnet> mags, JPanel parent) {
        this.setSize(new Dimension (parent.getWidth() - 500 ,parent.getHeight() - 500));
        magnets = mags;
        thumbnails = images;
        if (!(magnets.isEmpty())) {
            for (Magnet mag : magnets) {
                this.add(mag);
            }
        }
        if (!(thumbnails.isEmpty())) {
            for (PhotoComponent thumbnail : thumbnails) {
                this.add(thumbnail);
            }
        }
        mag_selected = null;
        selected = PhotoLayout.images.get(PhotoLayout.curr); //set the currently selected photo to PhotoLayout's curr
        addListeners();
        timer = new Timer(20, taskPerformer);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(4));
        g2.setColor(TextBox.sticky_note);
        //highlight currently selected photo
        if (selected!= null) {
            g2.drawRect(selected.getX() - 2, selected.getY() - 2, (int) (selected.getWidth() * 0.5) + 4, (int) (selected.getHeight() * 0.5) + 4);
        }
        if (mag_selected != null) {
            g2.setColor(Color.BLACK);
            g2.drawRect(mag_selected.getX() -2 , mag_selected.getY() -2 , (int) (mag_selected.getWidth()) + 4, (int) (mag_selected.getHeight()) + 4);
        }
    }

    @Override
    public void doLayout() {
        if (magnets.isEmpty() && !(thumbnails.isEmpty())) {
            int max_width = 0;
            int max_height = 0;
            int row = 0;
            for (PhotoComponent thumbnail : thumbnails) {
                if (thumbnail.getWidth() > max_width) {
                    max_width = thumbnail.getWidth(); //find largest width amongst all thumbnails
                } if (thumbnail.getHeight() > max_height) {
                    max_height = thumbnail.getHeight(); //find largest height amongst all thumbnails
                }
            }
            for (int i = 0; i < thumbnails.size(); i++) {
                if ((i + 1) % row_count == 0) { //at the end of the row, calculate positions for images in that row
                    for (int k = row_count; k > 0; k--) {
                        thumbnails.get(i - k + 1).setLocation((offset * (row_count - k + 1)) + (max_width * (row_count - k)),
                                (row * max_height) + offset);
                    }
                    row++; //increment row after row_count has been filled
                } else if (i == thumbnails.size() - 1) { //if you reach the last image, this row might not have full row_count
                    int last_row = (thumbnails.size()) % row_count; //number of images left over for last row
                    for (int j = last_row; j > 0; j--) { //calculate positions based on last_row count
                        thumbnails.get(thumbnails.size() - j).setLocation((offset * (last_row - j + 1)) + (max_width * (last_row - j)),
                                (row * max_height) + offset);
                    }
                }
            }
        }
    }

    public static void updateSelected() {
        selected = PhotoLayout.images.get(PhotoLayout.curr);
    }

    public Magnet getSelectedMagnet() {
        return mag_selected;
    }

    //snap back to original grid format if all magnets are deleted
    public static void setOriginal() {
        if (!(thumbnails.isEmpty())) {
            int max_width = 0;
            int max_height = 0;
            int row = 0;
            for (PhotoComponent thumbnail : thumbnails) {
                if (thumbnail.getWidth() > max_width) {
                    max_width = thumbnail.getWidth(); //find largest width amongst all thumbnails
                } if (thumbnail.getHeight() > max_height) {
                    max_height = thumbnail.getHeight(); //find largest height amongst all thumbnails
                }
            }
            for (int i = 0; i < thumbnails.size(); i++) {
                if ((i + 1) % row_count == 0) { //at the end of the row, calculate positions for images in that row
                    for (int k = row_count; k > 0; k--) {
                        thumbnails.get(i - k + 1).setLocation((offset * (row_count - k + 1)) + (max_width * (row_count - k)),
                                (row * max_height) + offset);
                    }
                    row++; //increment row after row_count has been filled
                } else if (i == thumbnails.size() - 1) { //if you reach the last image, this row might not have full row_count
                    int last_row = (thumbnails.size()) % row_count; //number of images left over for last row
                    for (int j = last_row; j > 0; j--) { //calculate positions based on last_row count
                        thumbnails.get(thumbnails.size() - j).setLocation((offset * (last_row - j + 1)) + (max_width * (last_row - j)),
                                (row * max_height) + offset);
                    }
                }
            }
        }
    }

    //calculate new locations based on magnet locations
    public static void updateThumbnails() {
        if (!(magnets.isEmpty()) && PhotoLayout.view_mode.toString().equals("grid")) {
            for (PhotoComponent thumbnail : thumbnails) {
                int mag_count = 0;
                int x = 0;
                int y = 0;
                for (Magnet mag : magnets) {
                    if (mag.tag.toString().equals("school") && thumbnail.school) {
                        mag_count++;
                        x += mag.getX();
                        y += mag.getY();
                    }
                    if (mag.tag.toString().equals("work") && thumbnail.work) {
                        mag_count++;
                        x += mag.getX();
                        y += mag.getY();
                    }
                    if (mag.tag.toString().equals("family") && thumbnail.family) {
                        mag_count++;
                        x += mag.getX();
                        y += mag.getY();
                    }
                    if (mag.tag.toString().equals("vacation") && thumbnail.vacation) {
                        mag_count++;
                        x += mag.getX();
                        y += mag.getY();
                    }
                }
                if (mag_count != 0) {
                    int pad = 5 * thumbnails.indexOf(thumbnail);
                    locations.put(thumbnail, new Point((x / mag_count) + pad, (y / mag_count) + pad));
                } else if (PhotoLayout.view_mode.toString().equals("grid")){
                    locations.put(thumbnail, thumbnail.getLocation());
                }
            }
        }
        //start timer once locations are calculated
        timer.start();
    }

    public static boolean approximatelyEqual(double goal, double current) {
        double diff = Math.abs(goal - current);
        return (diff < 7);
    }

    ActionListener taskPerformer = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            selected = null;
            for (PhotoComponent thumbnail : thumbnails) {
                if (locations.get(thumbnail) == null) {
                    continue;
                } else if (approximatelyEqual(thumbnail.getLocation().getX(), locations.get(thumbnail).getX())
                        && approximatelyEqual(thumbnail.getLocation().getY(), locations.get(thumbnail).getY())) {
                    continue;
                } else {
                    int final_x = (int) locations.get(thumbnail).getX();
                    int final_y = (int) locations.get(thumbnail).getY();
                    int x = thumbnail.getX();
                    int y = thumbnail.getY();
                    //if the x is lss than the goal x, move up 5, otherwise move down 5 (likewise with y)
                    x += final_x < x ? -5 : 5;
                    y += final_y < y ? -5 : 5;
                    thumbnail.setLocation(x, y);
                }
            }
            if (magnets.isEmpty()) {
                setOriginal();
            }
        }
    };

    public void addListeners() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                Component pc = e.getComponent();
                //select magnet
                if (!(magnets.isEmpty())) {
                    for (Magnet mag : magnets) {
                        if (mag.getBounds().contains(e.getPoint()) || pc instanceof Magnet) {
                            mag_selected = mag;
                            revalidate();
                            repaint();
                        }
                        break;
                    }
                    //select photo
                    for (PhotoComponent thumbnail : thumbnails) {
                        Rectangle box = new Rectangle(thumbnail.getX() - 2, thumbnail.getY() - 2, (int) (thumbnail.getWidth() * 0.5) + 4, (int) (thumbnail.getHeight() * 0.5) + 4);
                        if (box.contains(e.getPoint())) {
                            pc = thumbnail;
                            selected = (PhotoComponent) pc; //update selected
                            PhotoLayout.curr = PhotoLayout.images.indexOf(pc); //update curr in PhotoLayout
                            PhotoLayout.updateCheckboxes();
                            if (e.getClickCount() == 2) { //if double click, switch back to photo mode
                                selected = (PhotoComponent) pc;
                                PhotoLayout.view_mode = VIEW_MODE.PHOTO;
                                PhotoLayout.switchViewMode();
                            }
                        }
                    }
                }
                if (x < pc.getWidth() * 0.5 + 10 && y < pc.getHeight() * 0.5 + 10 && pc instanceof PhotoComponent) { //if click is within bounds of scaled photo
                    selected = (PhotoComponent) pc; //update selected
                    PhotoLayout.curr = PhotoLayout.images.indexOf(pc); //update curr in PhotoLayout
                    PhotoLayout.updateCheckboxes();
                    if (e.getClickCount() == 2) { //if double click, switch back to photo mode
                        PhotoLayout.view_mode = VIEW_MODE.PHOTO;
                        PhotoLayout.switchViewMode();
                    }
                }
                revalidate();
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Component pc = e.getComponent();
                if (!(magnets.isEmpty())) {
                    for (Magnet mag : magnets) {
                        if (mag.getBounds().contains(e.getPoint()) || pc instanceof Magnet) {
                            mag_selected = mag;
                        }
                    }
                }
                revalidate();
                repaint();
            }


            @Override
            public void mouseReleased(MouseEvent e) {
                if (mag_selected != null) {
                    updateThumbnails();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            //drag magnet
            public void mouseDragged(MouseEvent e) {
                if (mag_selected != null) {
                    mag_selected.updateLocation(e.getX() - mag_selected.getWidth()/2, e.getY() - mag_selected.getHeight()/2);
                    updateThumbnails();
                    revalidate();
                    repaint();
                }
            }
        });
    }

}
