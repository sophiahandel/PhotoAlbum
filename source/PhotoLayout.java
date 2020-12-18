
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.io.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.BorderFactory;
import dollar.DollarRecognizer;

public class PhotoLayout {

    //create global variables needed for action events
    static JLabel status = new JLabel("Status");
    JMenuItem importItem;
    static JMenuItem deleteItem;
    JMenuItem exitItem;
    static JRadioButtonMenuItem photoView, gridView;
    static JButton next;
    static JButton prev;
    static JButton school_mag, family_mag, vacation_mag, work_mag, mag_delete;
    static ArrayList<Magnet> mags = new ArrayList<>();
    static boolean schoolMag, workMag, famMag, vacayMag;
    JRadioButton pen, text;
    static JCheckBox vacation, family, school, work;
    static JPanel contentPanel;
    JScrollPane scrollPanel = new JScrollPane();
    static ArrayList<PhotoComponent> images = new ArrayList();
    static EDIT_MODE edit_mode;
    static VIEW_MODE view_mode = VIEW_MODE.PHOTO;
    static LightTable lt;
    static dollar.DollarRecognizer recognizer = new DollarRecognizer();
    static int curr = 0; //current image to be displayed


    public PhotoLayout() {
        //set up top level JFrame
        JFrame main = new JFrame("Photo Layout");
        main.setSize(1000, 1000);
        main.setLayout(new BorderLayout());
        //exit app when runtime environment is closed
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //STATUS BAR
        JPanel statusPanel = new JPanel();
        statusPanel.setPreferredSize(new Dimension(main.getWidth(), 35));
        //give the status Panel its own BorderLayout
        statusPanel.setLayout(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        //add status to the status Panel, align to center
        status.setHorizontalAlignment(JLabel.CENTER);
        statusPanel.add(status, BorderLayout.CENTER);

        //Create Menu Bar
        JMenuBar menuBar = new JMenuBar();

        //CONTROL PANEL
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(4, 1));
        controlPanel.setPreferredSize(new Dimension(200, main.getHeight()));
        controlPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        //create navigation panel
        controlPanel.add(createNavigation(main));
        //create tag panel
        controlPanel.add(createTag(main));
        //create MagnetPanel
        controlPanel.add(createMagnet(main));
        //create edit panel
        controlPanel.add(createEdit(main));

        //CONTENT PANEL
        contentPanel = new JPanel();
        JViewport view = new JViewport();
        view.setView(contentPanel);
        scrollPanel.setViewport(view); //create ScrollPanel with JPanel as Viewport
        scrollPanel.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPanel.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        contentPanel.setPreferredSize(new Dimension(main.getWidth() - controlPanel.getWidth(), main.getHeight() - statusPanel.getHeight()));
        contentPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        contentPanel.setLayout(new BorderLayout());
        if (images.isEmpty()) {  //if no images are read from command line, set JLabel to no photos imported text
            JLabel empty = new JLabel("No Photos Imported");
            empty.setHorizontalAlignment(JLabel.CENTER);
            contentPanel.add(empty, BorderLayout.CENTER);
        } else { //if images imported from cmd line, display image at index 0
            contentPanel.add(images.get(0), BorderLayout.CENTER);
        }

        //FILE MENU
        JMenu fileMenu = new JMenu("File");

        //populate file menu with import, delete, and exit items

        //import
        importItem = new JMenuItem("Import");
        importItem.addActionListener(ev -> {
            importPhoto();
            if (!(images.isEmpty()) && view_mode.toString().equals("photo")) {
                next.setEnabled(true);
                prev.setEnabled(true);
                next.setForeground(Color.BLACK);
                prev.setForeground(Color.BLACK);
                contentPanel.add(images.get(curr));
            } else if (!(images.isEmpty()) && view_mode.toString().equals("grid")) {
                for (PhotoComponent pc : images) {
                    pc.setScaleFactor(0.5);
                    pc.rescale();
                    pc.setEnabled(false);
                    pc.flipped = false;
                }
                lt = new LightTable(images, mags, contentPanel);
                contentPanel.add(lt, BorderLayout.CENTER);
            }
            status.setText("Imported Photos");
            contentPanel.revalidate();
            contentPanel.repaint();
        });
        fileMenu.add(importItem);

        //delete
        deleteItem = new JMenuItem("Delete");
        if (images.isEmpty()) { //disable delete option if images array is empty
            deleteItem.setEnabled(false);
            deleteItem.setForeground(Color.LIGHT_GRAY);
        }
        deleteItem.addActionListener(ev -> {
            deletePhoto();
        });
        fileMenu.add(deleteItem);

        //exit
        exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(ev -> {
            status.setText("Exit Button Clicked");
            System.exit(0);
        });
        fileMenu.add(exitItem);

        //add file menu to menu bar
        menuBar.add(fileMenu);

        //VIEW MENU
        JMenu viewMenu = new JMenu("View");
        //create Button Group so grid/photo selection can be mutually exclusive
        ButtonGroup viewGroup = new ButtonGroup();
        //populate view menu
        photoView = new JRadioButtonMenuItem("Photo");
        photoView.setSelected(true);
        photoView.addActionListener(ev -> {
            view_mode = VIEW_MODE.PHOTO;
            switchViewMode();
        });
        viewGroup.add(photoView);
        viewMenu.add(photoView);
        gridView = new JRadioButtonMenuItem("Grid");
        gridView.addActionListener(ev -> {
            view_mode = VIEW_MODE.GRID;
            switchViewMode();
        });
        viewGroup.add(gridView);
        viewMenu.add(gridView);
        //add view menu to menu bar
        menuBar.add(viewMenu);


        //add all components to main frame and set to visible
        main.setJMenuBar(menuBar);
        main.add(statusPanel, BorderLayout.SOUTH);
        main.add(controlPanel, BorderLayout.WEST);
        main.add(scrollPanel, BorderLayout.CENTER);
        main.setVisible(true);

    }

    public static void deletePhoto() {
        if (view_mode.toString().equals("photo")) {
            for (Component pc : contentPanel.getComponents()) {
                if (pc instanceof PhotoComponent || pc instanceof JLabel) {
                    contentPanel.remove(pc);
                }
            }
            contentPanel.revalidate();
            contentPanel.repaint();
            images.remove(curr); //remove image from image array
            if (images.isEmpty()) { //if last image deleted, set new PhotoComponent to no photos imported text, and disable next/prev/delete
                deleteItem.setEnabled(false);
                next.setEnabled(false);
                prev.setEnabled(false);
                next.setForeground(Color.LIGHT_GRAY);
                prev.setForeground(Color.LIGHT_GRAY);
                deleteItem.setForeground(Color.LIGHT_GRAY);
                JLabel empty = new JLabel("No Photos Imported");
                empty.setHorizontalAlignment(JLabel.CENTER);
                contentPanel.add(empty, BorderLayout.CENTER);
                curr = 0;
            } else { //if last image in array is deleted, set former previous image as current JLabel displayed
                if (curr > images.size()-1 && images.size() > 0) {
                    curr--;
                }
                contentPanel.add(images.get(curr)); //display new curr
                updateCheckboxes();
            }
        } else { //similar construction, except remove and re-add LightTable
            for (Component lt : contentPanel.getComponents()) {
                if (lt instanceof LightTable) {
                    contentPanel.remove(lt);
                }
            }
            images.remove(curr);
            if (images.isEmpty()) {
                deleteItem.setEnabled(false);
                deleteItem.setForeground(Color.LIGHT_GRAY);
                JLabel empty = new JLabel("No Photos Imported");
                empty.setHorizontalAlignment(JLabel.CENTER);
                contentPanel.add(empty, BorderLayout.CENTER);
                curr = 0;
            } else {
                if (curr > images.size() - 1 && images.size() > 0) {
                    curr--;
                }
                lt = new LightTable(images, mags, contentPanel);
                contentPanel.add(lt, BorderLayout.CENTER);
                updateCheckboxes();
            }
            contentPanel.repaint();
            contentPanel.revalidate();
        }
        status.setText("Photo Deleted");
    }

    //called by import button action event, opens file chooser to allow user to select a directory of images
    private void importPhoto() {
        if (images.isEmpty()) { //if no images previous imported, remove text to import photos
            for (Component pc : contentPanel.getComponents()) {
                if (pc instanceof PhotoComponent || pc instanceof JLabel) {
                    contentPanel.remove(pc);
                }
            }
        }

        //new file chooser, open dialog box in new frame
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Show the dialog; wait until dialog is closed
        Component frame = null;
        fc.showOpenDialog(frame);

        File dir = null;

        // Retrieve the JPEG files from the directory, add to static images ArrayList
        dir = fc.getSelectedFile();
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.getName().endsWith(".jpg")) {
                    ImageIcon icon = new ImageIcon(child.getAbsolutePath());
                    PhotoComponent pc = new PhotoComponent(icon);
                    images.add(pc);
                }
            }
        }

        if (!(images.isEmpty())) { //enable prev, next, and delete options now that images are imported
            deleteItem.setEnabled(true);
            deleteItem.setForeground(Color.BLACK);
        }
    }

    //called by constructor to initialize previous/next buttons and action listeners
    private JPanel createNavigation(JFrame parent) {
        // Create Navigation Panel
        JPanel navigationPanel = new JPanel();
        navigationPanel.setPreferredSize(new Dimension(150, parent.getHeight() / 20));
        navigationPanel.setLayout(new GridLayout(2, 1));
        navigationPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        // Create prev/next buttons
        next = new JButton("Next>>");
        //disable and gray out if no images imported
        if (images.isEmpty()) {
            next.setEnabled(false);
            next.setForeground(Color.LIGHT_GRAY);
        }
        next.addActionListener(ev -> {
            nextPhoto();
            status.setText("Next Photo");
        });
        navigationPanel.add(next);

        prev = new JButton(" <<Previous");
        //disable and gray out if no images imported
        if (images.isEmpty()) {
            prev.setEnabled(false);
            prev.setForeground(Color.LIGHT_GRAY);
        }
        prev.addActionListener(ev -> {
            previousPhoto();
            status.setText("Previous Photo");
        });
        navigationPanel.add(prev);
        return navigationPanel;
    }

    public static void previousPhoto() {
        if (view_mode.toString().equals("photo")) {
            //remove currently displayed image
            for (Component pc : contentPanel.getComponents()) {
                if (pc instanceof PhotoComponent || pc instanceof JLabel) {
                    contentPanel.remove(pc);
                }
            }
            contentPanel.revalidate();
            contentPanel.repaint();
            if (curr - 1 < 0) { //decrement current image index
                curr = images.size() - 1;
            } else {
                curr--;
            }
            contentPanel.add(images.get(curr)); //display new current image
        } else {
            if (curr - 1 < 0) { //decrement current image index
                curr = images.size() - 1;
            } else {
                curr--;
            }
            lt.updateSelected();
            lt.revalidate();
            lt.repaint();
        }
        updateCheckboxes();
    }

    public static void nextPhoto() {
        if (view_mode.toString().equals("photo")) {
            //remove currently displayed image
            for (Component pc : contentPanel.getComponents()) {
                if (pc instanceof PhotoComponent || pc instanceof JLabel) {
                    contentPanel.remove(pc);
                }
            }
            if (curr + 1 > images.size() - 1) { //increment currently displayed photo index
                curr = 0;
            } else {
                curr++;
            }
            contentPanel.add(images.get(curr)); //display new current image
            contentPanel.revalidate();
            contentPanel.repaint();
        } else {
            if (curr + 1 > images.size() - 1) { //increment currently displayed photo index
                curr = 0;
            } else {
                curr++;
            }
            lt.updateSelected();
            lt.revalidate();
            lt.repaint();
        }
        updateCheckboxes();
    }

    //called by constructor to create panel with tag checkboxes
    private JPanel createTag(JFrame parent) {
        //Create Panel for Tags
        JPanel tagPanel = new JPanel();
        tagPanel.setLayout(new GridLayout(5, 1));
        tagPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        //create header text
        //Add header label
        JLabel tagLabel = new JLabel("Tag Photo");
        tagLabel.setHorizontalAlignment(JLabel.CENTER);
        tagPanel.add(tagLabel);

        // Create tag checkboxes
        school = new JCheckBox("School");
        school.addActionListener(ev -> {
            toggleSchool();
            if (!(school.isSelected())) {
                status.setText("Unchecked School");
            } else {
                status.setText("Checked School");
            }
        });
        tagPanel.add(school);

        work = new JCheckBox("Work");
        work.addActionListener(ev -> {
            toggleWork();
            if (!(work.isSelected())) {
                status.setText("Unchecked Work");
            } else {
                status.setText("Checked Work");
            }
        });
        tagPanel.add(work);

        family = new JCheckBox("Family");
        family.addActionListener(ev -> {
            toggleFamily();
            if (!(family.isSelected())) {
                status.setText("Unchecked Family");
            } else {
                status.setText("Checked Family");
            }
        }
        );
        tagPanel.add(family);

        vacation = new JCheckBox("Vacation");
        vacation.addActionListener(ev -> {
            toggleVacation();
            if (!(vacation.isSelected())) {
                status.setText("Unchecked Vacation");
            } else {
                status.setText("Checked Vacation");
            }
        });
        tagPanel.add(vacation);

        return tagPanel;
    }

    public static void toggleWork() {
        images.get(curr).setWork();
        updateCheckboxes();
    }

    public static void toggleSchool() {
        images.get(curr).setSchool();
        updateCheckboxes();
    }
    public static void toggleVacation() {
        images.get(curr).setVacation();
        updateCheckboxes();
    }
    public static void toggleFamily() {
        images.get(curr).setFamily();
        updateCheckboxes();
    }

    //called by constructor to create panel with editing option radio buttons
    private JPanel createEdit(JFrame parent) {
        //Create Edit Panel
        JPanel editPanel = new JPanel();
        editPanel.setPreferredSize(new Dimension(150, parent.getHeight() / 4));
        editPanel.setLayout(new GridLayout(3, 1));
        editPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        //Add header label
        JLabel editLabel = new JLabel("Edit Photo");
        editLabel.setHorizontalAlignment(JLabel.CENTER);
        editPanel.add(editLabel);

        //create radio button group so pen & text are mutually exclusive
        ButtonGroup editGroup = new ButtonGroup();

        //add pen and text radio buttons
        pen = new JRadioButton("Pen");
        pen.addActionListener(ev -> {
            edit_mode = EDIT_MODE.PEN;
            status.setText("Selected Pen");
            contentPanel.repaint();
        });
        editGroup.add(pen);
        editPanel.add(pen);
        text = new JRadioButton("Text");
        text.addActionListener(ev -> {
            edit_mode = EDIT_MODE.TEXT;
            status.setText("Selected Text");
            contentPanel.repaint();
        });
        editGroup.add(text);
        editPanel.add(text);

        return editPanel;
    }

    private JPanel createMagnet(JFrame parent) {
        JPanel magnetPanel = new JPanel();
        magnetPanel.setPreferredSize(new Dimension(150, parent.getHeight() / 8));
        magnetPanel.setLayout(new GridLayout(6, 1));
        magnetPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        //create Header Label
        JLabel magLabel = new JLabel("Add Magnets");
        magLabel.setHorizontalAlignment(JLabel.CENTER);
        magnetPanel.add(magLabel);

        // Create buttons for each tag magnet
        school_mag = new JButton("School");
        school_mag.addActionListener(ev -> {
            if (view_mode.toString().equals("grid")) {
                if (!schoolMag) {
                    Magnet mag = new Magnet(TAG.SCHOOL, lt);
                    mags.add(mag);
                    lt = new LightTable(images, mags, contentPanel);
                    lt.mag_selected = mag;
                    status.setText("School Magnet Added");
                    schoolMag = true;
                }
                contentPanel.add(lt, BorderLayout.CENTER);
                lt.updateThumbnails();
                contentPanel.revalidate();
                contentPanel.repaint();;
            }
        });
        magnetPanel.add(school_mag);
        family_mag = new JButton("Family");
        family_mag.addActionListener(ev -> {
            if (view_mode.toString().equals("grid")) {
                if (!famMag) {
                    Magnet mag = new Magnet(TAG.FAMILY, lt);
                    mags.add(mag);
                    lt = new LightTable(images, mags, contentPanel);
                    lt.mag_selected = mag;
                    status.setText("Family Magnet Added");
                    famMag = true;
                }
                contentPanel.add(lt, BorderLayout.CENTER);
                lt.updateThumbnails();
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        });
        magnetPanel.add(family_mag);
        vacation_mag = new JButton("Vacation");
        vacation_mag.addActionListener(ev -> {
            if (view_mode.toString().equals("grid")) {
                if (!vacayMag) {
                    Magnet mag = new Magnet(TAG.VACATION, lt);
                    mags.add(mag);
                    lt = new LightTable(images, mags, contentPanel);
                    lt.mag_selected = mag;
                    status.setText("Vacation Magnet Added");
                    vacayMag = true;
                }
                contentPanel.add(lt, BorderLayout.CENTER);
                lt.updateThumbnails();
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        });
        magnetPanel.add(vacation_mag);
        work_mag = new JButton("Work");
        work_mag.addActionListener(ev -> {
            if (view_mode.toString().equals("grid")) {
                if (!workMag) {
                    Magnet mag = new Magnet(TAG.WORK, lt);
                    mags.add(mag);
                    lt = new LightTable(images, mags, contentPanel);
                    lt.mag_selected = mag;
                    status.setText("Work Magnet Added");
                    workMag = true;
                }
                contentPanel.add(lt, BorderLayout.CENTER);
                lt.updateThumbnails();
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        });
        magnetPanel.add(work_mag);
        mag_delete = new JButton("Remove Magnet");
        mag_delete.addActionListener(ev -> {
            if (view_mode.toString().equals("grid")) {
                if (lt.getSelectedMagnet() != null) {
                    Magnet deleted = lt.getSelectedMagnet();
                    if (deleted.tag.toString().equals("work")) {
                        workMag = false;
                    } else if (deleted.tag.toString().equals("family")) {
                        famMag = false;
                    } else if (deleted.tag.toString().equals("vacation")) {
                        vacayMag = false;
                    } else if (deleted.tag.toString().equals("school")) {
                        schoolMag = false;
                    }
                    mags.remove(deleted);
                    for (Component lt : contentPanel.getComponents()) {
                        if (lt instanceof LightTable) {
                            contentPanel.remove(lt);
                        }
                    }
                    lt = new LightTable(images, mags, contentPanel);
                    status.setText(deleted.tag.toString() + " magnet removed");
                    contentPanel.add(lt, BorderLayout.CENTER);
                    if (mags.isEmpty()) {
                        lt.timer.stop();
                    }
                    lt.updateThumbnails();
                    contentPanel.repaint();
                    contentPanel.revalidate();
                }
        } });
        magnetPanel.add(mag_delete);
        family_mag.setEnabled(false);
        work_mag.setEnabled(false);
        school_mag.setEnabled(false);
        vacation_mag.setEnabled(false);
        mag_delete.setEnabled(false);
        return magnetPanel;
    }

    //update checkbox states to reflect current photo's tags
    public static void updateCheckboxes() {
        if (images.size() > 0 && images.get(curr).getVacation()) {
            vacation.setSelected(true);
        } else {
            vacation.setSelected(false);
        }
        if (images.size() > 0 && images.get(curr).getSchool()) {
            school.setSelected(true);
        } else {
            school.setSelected(false);
        }
        if (images.size() > 0 && images.get(curr).getFamily()) {
            family.setSelected(true);
        } else {
            family.setSelected(false);
        }
        if (images.size() > 0 && images.get(curr).getWork()) {
            work.setSelected(true);
        } else {
            work.setSelected(false);
        }
    }

    public static void switchViewMode() {
        if (view_mode.toString().equals("photo")) { //switch to photo View
            lt.timer.stop();
            status.setText("Switched to Photo View");
            photoView.setSelected(true);
            next.setEnabled(true);
            prev.setEnabled(true);
            next.setForeground(Color.BLACK);
            prev.setForeground(Color.BLACK);
            family_mag.setEnabled(false);
            work_mag.setEnabled(false);
            school_mag.setEnabled(false);
            vacation_mag.setEnabled(false);
            mag_delete.setEnabled(false);
            contentPanel.remove(lt);
            if (!images.isEmpty()) {
                for (Component lt : contentPanel.getComponents()) {
                    if (lt instanceof LightTable) {
                        contentPanel.remove(lt);
                    }
                }
                for (PhotoComponent pc : images) {
                    pc.setScaleFactor(1);
                    pc.rescale();
                    pc.setEnabled(true);
                    contentPanel.add(images.get(curr));
                }
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        }
        else { //switch to grid view
            status.setText("Switched to Grid View");
            family_mag.setEnabled(true);
            work_mag.setEnabled(true);
            school_mag.setEnabled(true);
            vacation_mag.setEnabled(true);
            mag_delete.setEnabled(true);
            view_mode = VIEW_MODE.GRID;
            gridView.setSelected(true);
            if (!(images.isEmpty())) { //remove PhotoComponent
                for (Component pc : contentPanel.getComponents()) {
                    if (pc instanceof PhotoComponent) {
                        contentPanel.remove(pc);
                    }
                }
                for (PhotoComponent pc : images) {
                    pc.setScaleFactor(0.5); //scale down images to thumbnail size
                    pc.rescale(); //update size of component
                    pc.setEnabled(false);
                    pc.flipped = false;
                }
                lt  = new LightTable(images, mags, contentPanel); //add light table with scaled images
                lt.setOriginal();
                lt.updateThumbnails();
                contentPanel.add(lt, BorderLayout.CENTER);
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        }
    }

    //main method
    public static void main(String[] args) {
        if (args.length != 0) { //reads image files from folder if passed in through cmd line
            File dir = new File(args[0]);
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                for (File child : directoryListing) {
                    if (child.getName().endsWith(".jpg")) {
                        ImageIcon icon = new ImageIcon(child.getAbsolutePath());
                        PhotoComponent photo = new PhotoComponent(icon);
                        images.add(photo);
                    }
                }
            }
        }
        new PhotoLayout();
    }
}
