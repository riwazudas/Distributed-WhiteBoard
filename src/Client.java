import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;


public class Client extends JFrame implements Runnable
{
    private static ObjectOutputStream os = null;

    private static String clientName;
    private ObjectInputStream is = null;
    private static LinkedList<String> clients;
    private static String manager;
    private int x1;	//for drawing shapes
    private int y1;
    private WhiteBoardShape s;
    Color c;

    private WaitingDialog wdialog;

    public ArrayList<Line> freeHandList;
    public ArrayList<EraserShape> eraserList;
    JPanel connectionPanel;	//different areas of frame
    JPanel westPanel;
    JPanel eastPanel;
    JPanel toolsPanel;
    JPanel colorPanel;
    JPanel userPanel;
    JPanel chatPanel;
    static WhiteBoardCanvas canvas;	//canvas for drawing shapes

    static JButton newCanvasButton;	//connection buttons
    static JButton joinButton;
    static JButton disconnectButton;
    static JToggleButton freeHandButton;

    static JToggleButton eraserButton;
    static JToggleButton eraserSmallButton;
    static JToggleButton lineButton;
    static JToggleButton rectangleButton;
    static JToggleButton filledRectButton;
    static JToggleButton ovalButton;
    static JToggleButton filledOvalButton;
    static JToggleButton textButton;
    static TextArea onlineUsers;
    TextArea chatBox;
    static JToggleButton redButton;	//color buttons
    static JToggleButton blueButton;
    static JToggleButton greenButton;
    static JToggleButton yellowButton;
    static JToggleButton blackButton;
    static JToggleButton grayButton;
    static JToggleButton brownButton;
    static JToggleButton turquoiseButton;
    static JToggleButton purpleButton;
    static JToggleButton pinkButton;
    static JToggleButton darkgrayButton;
    static JToggleButton lightgrayButton;
    static JToggleButton bronzeButton;
    static JToggleButton beigeButton;
    static JToggleButton magentaButton;
    static JToggleButton orangeButton;

    static JMenuBar menuBar;
    JMenu fileMenu;
    JMenu helpMenu;
    static JMenu kickMenu;
    static JTextArea textArea;
    JPanel textPanel;
    static JButton searchButton;
    static JTextField searchBox;

    ButtonGroup toolGroup;	//our button groups
    ButtonGroup colorGroup;


    public Client(String ip,int port)
    {
        super("WhiteBoard Client");
        if(!connectToServer(ip,port))
        {
            System.err.println("Cannot open socket connection...");
        }
        else	//mostly GUI stuff
        {

            this.setBounds(100, 100, 600, 400);
            this.setResizable(true);
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
            this.setVisible(true);

            this.setLayout(new BorderLayout());	//chooses layout

            menuBar=new JMenuBar();
            fileMenu=new JMenu("File");
            helpMenu=new JMenu("Help");
            kickMenu=new JMenu("Kick");
            JMenuItem newBoard=new JMenuItem("New");
            JMenuItem saveBoard=new JMenuItem("Save As Image");
            JMenuItem aboutMessage= new JMenuItem("About");
            JMenuItem closeBoard= new JMenuItem("Close");

            fileMenu.add(newBoard);
            fileMenu.add(saveBoard);
            fileMenu.add(closeBoard);
            helpMenu.add(aboutMessage);
            menuBar.add(fileMenu);
            menuBar.add(helpMenu);
            menuBar.add(kickMenu);
            menuBar.setVisible(false);
            this.setJMenuBar(menuBar);

            connectionPanel = new JPanel(new FlowLayout());
            westPanel = new JPanel(new GridLayout(2,1));
            toolsPanel = new JPanel(new GridLayout(0,2));
            colorPanel = new JPanel(new GridLayout(0,3));
            eastPanel = new JPanel(new GridLayout(2,1));
            userPanel= new JPanel(new GridLayout(1,1));
            chatPanel= new JPanel(new BorderLayout());
            canvas = new WhiteBoardCanvas();
            canvas.setPreferredSize(new Dimension(500,350));
            chatPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

            connectionPanel.setBorder(BorderFactory.createBevelBorder(0));
            toolsPanel.setBorder(BorderFactory.createBevelBorder(0));
            colorPanel.setBorder(BorderFactory.createBevelBorder(0));
            userPanel.setBorder(BorderFactory.createBevelBorder(0));
            chatPanel.setBorder(BorderFactory.createBevelBorder(0));

            westPanel.add(toolsPanel);
            westPanel.add(colorPanel);
            eastPanel.add(userPanel);
            eastPanel.add(chatPanel);

            onlineUsers = new TextArea();
            onlineUsers.setEditable(false);
            onlineUsers.setPreferredSize(new Dimension(150, 200));

            textPanel = new JPanel(new GridBagLayout());
            textPanel.setPreferredSize(new Dimension(eastPanel.getWidth(), 20));
            textPanel.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
            westPanel.add(textPanel, BorderLayout.SOUTH);

            searchBox = new JTextField();
            GridBagConstraints textConstraints = new GridBagConstraints();
            textConstraints.weightx = 10;
            textConstraints.fill = GridBagConstraints.BOTH;
            searchBox.setFont(new Font("Arial", Font.PLAIN, 10));
            textPanel.add(searchBox, textConstraints);

            searchButton = new JButton(">");
            GridBagConstraints buttonConstraints = new GridBagConstraints();
            buttonConstraints.weightx = 1;
            searchButton.setFont(new Font("Arial", Font.PLAIN, 10));
            textPanel.add(searchButton, buttonConstraints);

            textArea = new JTextArea();
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(new Font("Arial", Font.PLAIN, 14));
            textArea.setMargin(new Insets(3, 5, 3, 5));
            chatPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

            userPanel.add(onlineUsers);
            chatPanel.add(textPanel,BorderLayout.SOUTH);
            searchBox.setEnabled(false);
            searchButton.setEnabled(false);
            textArea.setEditable(false);


            this.add("North",connectionPanel);	//adds panels
            this.add("West",westPanel);
            this.add("Center",canvas);
            this.add("East",eastPanel);

            newCanvasButton = new JButton("Create");
            joinButton = new JButton("Join");
            disconnectButton = new JButton("Disconnect");
            disconnectButton.setEnabled(false);

            ImageIcon FreeHandIcon= new ImageIcon("images/freehand.gif");
            ImageIcon EraserIcon= new ImageIcon("images/eraser.gif");
            ImageIcon EraserSmallIcon= new ImageIcon("images/eraserSmall.gif");
            ImageIcon LineIcon= new ImageIcon("images/line.gif");
            ImageIcon RectangleIcon=new ImageIcon("images/rectangle.gif");
            ImageIcon FilledRectangleIcon= new ImageIcon("images/filledRect.gif");
            ImageIcon OvalIcon= new ImageIcon("images/circle.gif");
            ImageIcon FilledOvalIcon=new ImageIcon("images/filledCirc.gif");
            ImageIcon TextIcon=new ImageIcon("images/text.gif");

            freeHandButton=new JToggleButton(FreeHandIcon);
            eraserButton=new JToggleButton(EraserIcon);
            eraserSmallButton=new JToggleButton(EraserSmallIcon);
            lineButton = new JToggleButton(LineIcon);
            rectangleButton = new JToggleButton(RectangleIcon);
            filledRectButton = new JToggleButton(FilledRectangleIcon);
            ovalButton = new JToggleButton(OvalIcon);
            filledOvalButton  = new JToggleButton(FilledOvalIcon);
            textButton = new JToggleButton(TextIcon);

            freeHandButton.setEnabled(false);
            eraserButton.setEnabled(false);
            eraserSmallButton.setEnabled(false);
            lineButton.setEnabled(false);
            rectangleButton.setEnabled(false);
            filledRectButton.setEnabled(false);
            ovalButton.setEnabled(false);
            filledOvalButton.setEnabled(false);
            textButton.setEnabled(false);

            freeHandButton.setActionCommand("freeHand");
            eraserButton.setActionCommand("eraser");
            eraserSmallButton.setActionCommand("eraserSmall");
            lineButton.setActionCommand("line");
            rectangleButton.setActionCommand("rect");
            filledRectButton.setActionCommand("filledRect");
            ovalButton.setActionCommand("oval");
            filledOvalButton.setActionCommand("filledOval");
            textButton.setActionCommand("text");

            ImageIcon RedIcon= new ImageIcon("images/red.gif");
            ImageIcon BlueIcon=new ImageIcon("images/blue.gif");
            ImageIcon GreenIcon= new ImageIcon("images/green.gif");
            ImageIcon YellowIcon= new ImageIcon("images/yellow.gif");
            ImageIcon BlackIcon=new ImageIcon("images/black.gif");
            ImageIcon GrayIcon=new ImageIcon("images/gray.gif");
            ImageIcon TurquoiseIcon=new ImageIcon("images/turquoise.gif");
            ImageIcon BrownIcon=new ImageIcon("images/brown.gif");
            ImageIcon PinkIcon=new ImageIcon("images/pink.gif");
            ImageIcon PurpleIcon=new ImageIcon("images/purple.gif");
            ImageIcon DarkGrayIcon=new ImageIcon("images/dark_gray.gif");
            ImageIcon LightGrayIcon=new ImageIcon("images/light_gray.gif");
            ImageIcon BronzeIcon=new ImageIcon("images/bronze.gif");
            ImageIcon BeigeIcon=new ImageIcon("images/beige.gif");
            ImageIcon MagentaIcon=new ImageIcon("images/magenta.gif");
            ImageIcon OrangeIcon=new ImageIcon("images/orange.gif");

            redButton = new JToggleButton(RedIcon);
            blueButton = new JToggleButton(BlueIcon);
            greenButton = new JToggleButton(GreenIcon);
            yellowButton = new JToggleButton(YellowIcon);
            blackButton = new JToggleButton(BlackIcon);
            grayButton = new JToggleButton(GrayIcon);
            turquoiseButton = new JToggleButton(TurquoiseIcon);
            brownButton = new JToggleButton(BrownIcon);
            pinkButton = new JToggleButton(PinkIcon);
            purpleButton = new JToggleButton(PurpleIcon);
            darkgrayButton= new JToggleButton(DarkGrayIcon);
            lightgrayButton= new JToggleButton(LightGrayIcon);
            bronzeButton= new JToggleButton(BronzeIcon);
            beigeButton= new JToggleButton(BeigeIcon);
            magentaButton= new JToggleButton(MagentaIcon);
            orangeButton= new JToggleButton(OrangeIcon);

            redButton.setEnabled(false);
            blueButton.setEnabled(false);
            greenButton.setEnabled(false);
            yellowButton.setEnabled(false);
            blackButton.setEnabled(false);
            grayButton.setEnabled(false);
            brownButton.setEnabled(false);
            turquoiseButton.setEnabled(false);
            pinkButton.setEnabled(false);
            purpleButton.setEnabled(false);
            darkgrayButton.setEnabled(false);
            lightgrayButton.setEnabled(false);
            bronzeButton.setEnabled(false);
            beigeButton.setEnabled(false);
            magentaButton.setEnabled(false);
            orangeButton.setEnabled(false);

            redButton.setActionCommand("red");
            blueButton.setActionCommand("blue");
            greenButton.setActionCommand("green");
            yellowButton.setActionCommand("yellow");
            blackButton.setActionCommand("black");
            grayButton.setActionCommand("gray");
            brownButton.setActionCommand("brown");
            turquoiseButton.setActionCommand("turquoise");
            pinkButton.setActionCommand("pink");
            purpleButton.setActionCommand("purple");
            darkgrayButton.setActionCommand("darkgray");
            lightgrayButton.setActionCommand("lightgray");
            bronzeButton.setActionCommand("bronze");
            beigeButton.setActionCommand("beige");
            magentaButton.setActionCommand("magenta");
            orangeButton.setActionCommand("orange");

            toolGroup = new ButtonGroup();
            colorGroup = new ButtonGroup();

            connectionPanel.add(newCanvasButton);
            connectionPanel.add(joinButton);
            connectionPanel.add(disconnectButton);


            toolGroup.add(freeHandButton);
            toolGroup.add(eraserButton);
            toolGroup.add(eraserSmallButton);
            toolGroup.add(lineButton);
            toolGroup.add(rectangleButton);
            toolGroup.add(filledRectButton);
            toolGroup.add(ovalButton);
            toolGroup.add(filledOvalButton);
            toolGroup.add(textButton);

            colorGroup.add(redButton);
            colorGroup.add(blueButton);
            colorGroup.add(greenButton);
            colorGroup.add(yellowButton);
            colorGroup.add(blackButton);
            colorGroup.add(grayButton);
            colorGroup.add(brownButton);
            colorGroup.add(turquoiseButton);
            colorGroup.add(pinkButton);
            colorGroup.add(purpleButton);
            colorGroup.add(darkgrayButton);
            colorGroup.add(lightgrayButton);
            colorGroup.add(bronzeButton);
            colorGroup.add(beigeButton);
            colorGroup.add(magentaButton);
            colorGroup.add(orangeButton);

            lineButton.setSelected(true);

            toolsPanel.add(freeHandButton);
            toolsPanel.add(eraserButton);
            toolsPanel.add(eraserSmallButton);
            toolsPanel.add(lineButton);
            toolsPanel.add(rectangleButton);
            toolsPanel.add(filledRectButton);
            toolsPanel.add(ovalButton);
            toolsPanel.add(filledOvalButton);
            toolsPanel.add(textButton);

            blackButton.setSelected(true);

            colorPanel.add(redButton);
            colorPanel.add(blueButton);
            colorPanel.add(greenButton);
            colorPanel.add(yellowButton);
            colorPanel.add(blackButton);
            colorPanel.add(grayButton);
            colorPanel.add(brownButton);
            colorPanel.add(turquoiseButton);
            colorPanel.add(pinkButton);
            colorPanel.add(purpleButton);
            colorPanel.add(darkgrayButton);
            colorPanel.add(lightgrayButton);
            colorPanel.add(bronzeButton);
            colorPanel.add(beigeButton);
            colorPanel.add(magentaButton);
            colorPanel.add(orangeButton);
            canvas.setBackground(Color.WHITE);


            canvas.setEnabled(false);

            this.pack();

            closeBoard.addActionListener(e -> disconnect());

            saveBoard.addActionListener(e -> saveBoardAsJPEG());

            aboutMessage.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    Component parentComponent = (Component) e.getSource();
                    while (!(parentComponent instanceof JFrame) && parentComponent != null) {
                        parentComponent = parentComponent.getParent();
                    }
                    JOptionPane.showMessageDialog(parentComponent, "Created By Riwaz Udas 1547555");
                }
            });

            newBoard.addActionListener(e -> {
                Component parentComponent = (Component) e.getSource();
                while (!(parentComponent instanceof JFrame) && parentComponent != null) {
                    parentComponent = parentComponent.getParent();
                }
                int resp = JOptionPane.showOptionDialog(parentComponent,
                        "Are you sure any unsaved changes will be removed",
                        "Confirm",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        null,
                        JOptionPane.NO_OPTION);
                if (resp==0){
                    try {
                        os.writeObject("New");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });

            MouseAdapter mouseAdapter = (new MouseAdapter()	//for painting shapes on screen
            {
                public void mousePressed ( MouseEvent e ) {
                    freeHandList = new ArrayList<>();
                    eraserList=new ArrayList<>();
                    if (Objects.equals(colorGroup.getSelection().getActionCommand(), "red"))
                        c = Color.red;
                    if (Objects.equals(colorGroup.getSelection().getActionCommand(), "blue"))
                        c = Color.blue;
                    if (Objects.equals(colorGroup.getSelection().getActionCommand(), "green"))
                        c = Color.green;
                    if (Objects.equals(colorGroup.getSelection().getActionCommand(), "yellow"))
                        c = Color.yellow;
                    if (Objects.equals(colorGroup.getSelection().getActionCommand(), "black"))
                        c = Color.black;
                    if (Objects.equals(colorGroup.getSelection().getActionCommand(), "gray"))
                        c = Color.gray;
                    if (Objects.equals(colorGroup.getSelection().getActionCommand(), "brown"))
                        c = new Color(139, 69, 19);
                    if (Objects.equals(colorGroup.getSelection().getActionCommand(), "turquoise"))
                        c = new Color(64, 224, 208);
                    if (Objects.equals(colorGroup.getSelection().getActionCommand(), "pink"))
                        c = Color.pink;
                    if (Objects.equals(colorGroup.getSelection().getActionCommand(), "purple"))
                        c = new Color(128, 0, 128);
                    if (Objects.equals(colorGroup.getSelection().getActionCommand(), "dark_gray"))
                        c = Color.DARK_GRAY;
                    if (Objects.equals(colorGroup.getSelection().getActionCommand(), "light_gray"))
                        c = Color.LIGHT_GRAY;
                    if (Objects.equals(colorGroup.getSelection().getActionCommand(), "orange"))
                        c = Color.ORANGE;
                    if (Objects.equals(colorGroup.getSelection().getActionCommand(), "magenta"))
                        c = Color.MAGENTA;
                    if (Objects.equals(colorGroup.getSelection().getActionCommand(), "bronze"))
                        c = new Color(205, 127, 50);
                    if (Objects.equals(colorGroup.getSelection().getActionCommand(), "beige"))
                        c = new Color(245, 245, 220);
                    x1 = e.getX();
                    y1 = e.getY();

                    if (Objects.equals(toolGroup.getSelection().getActionCommand(), "freeHand")) {
                        Line l = new Line(e.getX(), e.getY(), e.getX(), e.getY(), c);
                        canvas.addTemp(l);
                        freeHandList.add(l);
                    }
                    if (Objects.equals(toolGroup.getSelection().getActionCommand(), "eraser")){
                        EraserShape er = new EraserShape(e.getX(), e.getY(), 30);
                        canvas.addTemp(er);
                        eraserList.add(er);
                    }
                    if (Objects.equals(toolGroup.getSelection().getActionCommand(), "eraserSmall")){
                        EraserShape er = new EraserShape(e.getX(), e.getY(), 10);
                        canvas.addTemp(er);
                        eraserList.add(er);
                    }
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "line"))
                        canvas.addTemp(new Line(e.getX(),e.getY(),e.getX(),e.getY(),c));
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "rect"))
                        canvas.addTemp(new Rectangle(e.getX(),e.getY(),e.getX(),e.getY(),c));
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "filledRect"))
                        canvas.addTemp(new FilledRectangle(e.getX(),e.getY(),e.getX(),e.getY(),c));
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "oval"))
                        canvas.addTemp(new Oval(e.getX(),e.getY(),e.getX(),e.getY(),c));
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "filledOval"))
                        canvas.addTemp(new FilledOval(e.getX(),e.getY(),e.getX(),e.getY(),c));
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "text"))
                        writeText();
                }

                public void mouseDragged ( MouseEvent e )	//drag effect while shape is being drawn
                {
                    
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "freeHand")) {
                        Line l = new Line(e.getX(), e.getY(), e.getX(), e.getY(), c);
                        canvas.addTemp(l);
                        freeHandList.add(l);
                    }
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "eraser")) {
                        EraserShape er = new EraserShape(e.getX(), e.getY(), 30);
                        canvas.addTemp(er);
                        eraserList.add(er);
                    }
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "eraserSmall")) {
                        EraserShape er = new EraserShape(e.getX(), e.getY(), 10);
                        canvas.addTemp(er);
                        eraserList.add(er);
                    }
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "line"))
                        canvas.addTemp(new Line(x1,y1,e.getX(),e.getY(),c));
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "rect"))
                        canvas.addTemp(new Rectangle(x1,y1,e.getX(),e.getY(),c));
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "filledRect"))
                        canvas.addTemp(new FilledRectangle(x1,y1,e.getX(),e.getY(),c));
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "oval"))
                        canvas.addTemp(new Oval(x1,y1,e.getX(),e.getY(),c));
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "filledOval"))
                        canvas.addTemp(new FilledOval(x1,y1,e.getX(),e.getY(),c));
                }

                public void mouseReleased ( MouseEvent e )
                {
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "freeHand"))
                    {
                        try {
                            for(Line l :freeHandList) {
                                canvas.add(l);
                                os.writeObject(l);
                            }
                            os.flush();
                        } catch (IOException e1) {
                            System.err.println("Error writing object");
                        }
                    }
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "eraser"))
                    {
                        try {
                            for(EraserShape er :eraserList) {
                                canvas.add(er);

                                os.writeObject(er);

                            }
                            os.flush();
                        } catch (IOException e1) {
                            System.err.println("Error writing object");
                        }
                    }
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "eraserSmall"))
                    {
                        try {
                            for(EraserShape er :eraserList) {
                                canvas.add(er);

                                os.writeObject(er);

                            }
                            os.flush();
                        } catch (IOException e1) {
                            System.err.println("Error writing object");
                        }
                    }
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "line"))
                    {
                        Line l = new Line(x1,y1,e.getX(),e.getY(),c);
                        canvas.add(l);
                        try {
                            os.writeObject(l);
                            os.flush();
                        } catch (IOException e1) {
                            System.err.println("Error writing object");
                        }
                    }

                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "rect"))
                    {
                        Rectangle r = new Rectangle(x1,y1,e.getX(),e.getY(),c);
                        canvas.add(r);
                        try {
                            os.writeObject(r);
                            os.flush();
                        } catch (IOException e1) {
                            System.err.println("Error writing object");
                        }
                    }
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "filledRect"))
                    {
                        FilledRectangle r = new FilledRectangle(x1,y1,e.getX(),e.getY(),c);
                        canvas.add(r);
                        try {
                            os.writeObject(r);
                            os.flush();
                        } catch (IOException e1) {
                            System.err.println("Error writing object");
                        }
                    }
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "oval"))
                    {
                        Oval o = new Oval(x1,y1,e.getX(),e.getY(),c);
                        canvas.add(o);
                        try {
                            os.writeObject(o);
                            os.flush();
                        } catch (IOException e1) {
                            System.err.println("Error writing object");
                        }
                    }
                    if(Objects.equals(toolGroup.getSelection().getActionCommand(), "filledOval"))
                    {
                        FilledOval o = new FilledOval(x1,y1,e.getX(),e.getY(),c);
                        canvas.add(o);
                        try {
                            os.writeObject(o);
                            os.flush();
                        } catch (IOException e1) {
                            System.err.println("Error writing object");
                        }
                    }
                }
            });

            newCanvasButton.addActionListener(e -> createCanvas());

            joinButton.addActionListener(e -> joinCanvas());

            disconnectButton.addActionListener(e -> disconnect());

            searchButton.addActionListener(e -> searchAction());

            canvas.clear();
            canvas.addMouseListener(mouseAdapter);
            canvas.addMouseMotionListener(mouseAdapter);
            this.run();
        }

    }

    public void searchAction(){
        String textmsg= searchBox.getText();
        if (!Objects.equals(textmsg, "")){
            try{
                os.writeObject("text "+clientName+"$"+textmsg);
                searchBox.setText("");
            }catch(IOException e){
                System.err.println(e.getMessage());
            }


        }
    }

    private void saveBoardAsJPEG() {
        BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics imageGraphics = image.getGraphics();
        canvas.printAll(imageGraphics);
        imageGraphics.dispose();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as JPEG");
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File outputFile = fileChooser.getSelectedFile();
            try {
                if (!outputFile.getPath().endsWith(".jpg")) {
                    outputFile = new File(outputFile.getPath() + ".jpg");
                }
                ImageIO.write(image, "jpg", outputFile);
                System.out.println("Canvas content saved as JPEG successfully!");
            } catch (IOException ex) {
                System.err.println("Error saving canvas content as JPEG: " + ex.getMessage());
            }
        }
    }
    private boolean connectToServer(String ip, int port)	 //attempts to connect to server with ip
    {
        try
        {
            Socket socket = new Socket(ip, port);
            this.os = new ObjectOutputStream(socket.getOutputStream());
            this.is = new ObjectInputStream(socket.getInputStream());
            System.out.print("Connected to Server\n");
        }catch(ConnectException e){
            JOptionPane.showOptionDialog(null,"Server not started yet","Connection Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, new Object[]{"OK"},"OK");
            return false;
        }catch (Exception ex)
        {
            System.err.print("Failed to Connect to Server\n");
            System.err.println(ex.getMessage());
            return false;
        }
        return true;
    }
    public void writeText()
    {
        String s = JOptionPane.showInputDialog(null,"Enter Text: ","", JOptionPane.INFORMATION_MESSAGE);	//dialog box for input
        if(s!=null)
        {
            Text t = new Text(x1,y1,0,0,c,s);
            canvas.add(t);
            try {
                os.writeObject(t);
                os.flush();
            } catch (IOException e1) {
                System.err.println("Error writing object");
            }
        }
    }
    public void createCanvas()
    {
        clientName = JOptionPane.showInputDialog(null,"Enter Username: ","Enter User Name", JOptionPane.INFORMATION_MESSAGE);
        if (clientName!=null){
            if(clientName.compareTo("")==0){
                JOptionPane.showMessageDialog(this, "Please enter valid name.");
                return;
            }
            else{
                try {
                    this.setTitle(clientName+" Client");
                    os.writeObject("new "+clientName);
                    os.flush();
                } catch (IOException e) {
                    System.err.println("Error creating conversation");
                }
            }
            String s = JOptionPane.showInputDialog(null,"Create name for new conversation: ","Enter Board Name", JOptionPane.INFORMATION_MESSAGE);
            setButtonTrue(s);
        }
    }

    public void joinCanvas()
    {
        clientName = JOptionPane.showInputDialog(null,"Enter Username: ","Enter User Name", JOptionPane.INFORMATION_MESSAGE);
        if (clientName!=null){
            if(clientName.compareTo("")==0){
                JOptionPane.showMessageDialog(this, "Please enter valid name.");
                return;
            }
            else{
                try {
                    this.setTitle(clientName+" Client");
                    os.writeObject("join "+clientName);	//writes a string so handle knows what to do
                    os.flush();
                } catch (IOException e) {
                    System.err.println("Error creating conversation");
                }
            }
            String s = JOptionPane.showInputDialog(null,"Enter name for existing conversation: ","Enter Board Name", JOptionPane.INFORMATION_MESSAGE);
            if(s!=null) {
                if (s.compareTo("") == 0) {
                    JOptionPane.showMessageDialog(this, "Please enter valid string.");
                } else {
                    try {
                        os.writeObject(s);
                        os.flush();
                    } catch (IOException e) {
                        System.err.println("Error writing ID");
                    }
                    wdialog= new WaitingDialog(this, "Joining WhiteBoard");
                    setToolEnable(true);
                    setButtonEnable(true);
                    canvas.setEnabled(true);
                    searchButton.setEnabled(true);
                    searchBox.setEnabled(true);
                    newCanvasButton.setEnabled(false);
                    joinButton.setEnabled(false);
                    disconnectButton.setEnabled(true);
                    wdialog.setEnabled(true);
                    wdialog.setVisible(true);
                }
            }
        }
    }

    public static class WaitingDialog extends Dialog {

        public WaitingDialog(Frame owner, String title) {
            super(owner, title, true); // true makes the dialog modal

            // Set layout
            setLayout(new BorderLayout());

            // Add label with waiting message
            Label messageLabel = new Label("Waiting for server response...", Label.CENTER);
            add(messageLabel, BorderLayout.CENTER);

            // Add cancel button
            Button cancelButton = new Button("Cancel");
            cancelButton.addActionListener(e -> {
                resetGUI();
                setVisible(false);
            });

            Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(cancelButton);
            add(buttonPanel, BorderLayout.SOUTH);

            // Set dialog size and position
            setSize(300, 100);
            setLocationRelativeTo(owner); // Center the dialog relative to its owner
        }
    }

    public void setButtonTrue(String s) {
        if(s!=null) {
            if (s.compareTo("") == 0) {
                JOptionPane.showMessageDialog(this, "Please enter valid string.");
            } else {
                try {
                    os.writeObject(s);
                    os.flush();
                } catch (IOException e) {
                    System.err.println("Error writing ID");
                }
                setToolEnable(true);
                setButtonEnable(true);
                searchButton.setEnabled(true);
                searchBox.setEnabled(true);
                canvas.setEnabled(true);
                newCanvasButton.setEnabled(false);
                joinButton.setEnabled(false);
                disconnectButton.setEnabled(true);
            }
        }
    }

    public void disconnect()
    {
        try {
            this.setTitle("WhiteBoard Client");
            os.writeObject("disconnect "+clientName);
            clientName="";
            os.flush();
            canvas.clear();
        } catch (IOException e) {
            System.err.println("Error creating conversation");
        }
        resetGUI();
    }

    public static void resetGUI(){
        setButtonEnable(false);
        setToolEnable(false);
        onlineUsers.setText("");
        canvas.setEnabled(false);
        newCanvasButton.setEnabled(true);
        searchButton.setEnabled(false);
        searchBox.setEnabled(false);
        joinButton.setEnabled(true);
        textArea.setText("");
        disconnectButton.setEnabled(false);
        menuBar.setVisible(false);
        canvas.clear();
    }

    public static void setToolEnable(boolean state){
        freeHandButton.setEnabled(state);
        eraserButton.setEnabled(state);
        eraserSmallButton.setEnabled(state);
        lineButton.setEnabled(state);
        rectangleButton.setEnabled(state);
        filledRectButton.setEnabled(state);
        ovalButton.setEnabled(state);
        filledOvalButton.setEnabled(state);
        textButton.setEnabled(state);
    }

    public static void setButtonEnable(Boolean state) {
        redButton.setEnabled(state);
        blueButton.setEnabled(state);
        greenButton.setEnabled(state);
        yellowButton.setEnabled(state);
        blackButton.setEnabled(state);
        grayButton.setEnabled(state);
        brownButton.setEnabled(state);
        turquoiseButton.setEnabled(state);
        pinkButton.setEnabled(state);
        darkgrayButton.setEnabled(state);
        purpleButton.setEnabled(state);
        lightgrayButton.setEnabled(state);
        bronzeButton.setEnabled(state);
        beigeButton.setEnabled(state);
        magentaButton.setEnabled(state);
        orangeButton.setEnabled(state);
    }

    public static void main(String [] args)	//main method, takes ip argument
    {
        // Check if both server address and port are provided as command-line arguments
        if (args.length != 2) {
            System.err.println("Usage: java Client <server-address> <port>");
            System.exit(1);
        }
        String serverAddress = args[0];
        int port = Integer.parseInt(args[1]);
        new Client(serverAddress, port);

    }
    @Override
    public void run()
    {
        boolean isRunning = true;
        while(isRunning)
        {
            try {
                Object response = is.readObject();
                System.out.println(response);
                if(response instanceof String){
                    if (((String) response).startsWith("Clients ")) {
                        clients = new LinkedList<>();
                        response=((String) response).substring(8);
                        if (((String) response).contains(" ")){
                            clients.addAll(Arrays.asList(((String) response).split(" ")));
                        }else{
                            clients.add((String) response);
                        }
                        updateGUI();
                    }else if (((String) response).startsWith("Manager ")) {
                        clients = new LinkedList<>();
                        response=((String) response).substring(8);
                        manager=(String) response;
                        updateGUI();
                    }else if(((String) response).startsWith("Closed")){
                        JOptionPane.showMessageDialog(this,"WhiteBoard Closed By Manager");
                        resetGUI();
                    }
                    else if(((String) response).startsWith("Error ")){
                        JOptionPane.showMessageDialog(this,((String)response).substring(6));
//                        wdialog.dispose();
                        resetGUI();
                    }else if (((String) response).startsWith("Reqjoin ")){
                        response= ((String) response).substring(8);
                        int choice=JOptionPane.showOptionDialog(this,((String)response)+" wants to join","Request to join",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,null,JOptionPane.NO_OPTION);
                        if (choice==0){
                            os.writeObject("reqresponse accept "+response);
                        }
                        else{
                            os.writeObject("reqresponse reject "+response);
                        }
                    }else if(((String) response).startsWith("Allowed")){
                        os.writeObject("Allowed "+clientName);
                        wdialog.dispose();
                    }else if(((String) response).startsWith("Denied")){
                        os.writeObject("Denied "+clientName);
                        wdialog.dispose();
                        JOptionPane.showMessageDialog(this,"Access Denied");
                        resetGUI();
                    }else if(((String) response).startsWith("Kicked")){
                        JOptionPane.showMessageDialog(this,"Kicked by Manager");
                        resetGUI();
                    }else if(((String) response).startsWith("clear")){
                        canvas.clear();
                    }else if(((String) response).startsWith("text")){
                        response= ((String) response).substring(4);
                        String[] msg= ((String) response).split("\\$");
                        textArea.append(msg[0]+": "+msg[1]+"\n");
                    }

                }else if (response instanceof WhiteBoardShape){
                    s = (WhiteBoardShape) response;
                }
            }catch (SocketException e){
                int resp=JOptionPane.showOptionDialog(null,"Disconnected from Server","Connection Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, new Object[]{"OK"},"OK");
                if (resp==0){
                    this.dispose();
                }
                return;
            } catch (Exception e){
                isRunning = false;
                System.out.println(e.getMessage());
                System.err.println("Server has stopped running, please exit");
            }
            if(s!=null)
                canvas.add(s);
        }
    }

    public static void updateGUI(){
        onlineUsers.setText("");
        if (Objects.equals(clientName, manager)){
            menuBar.setVisible(true);
        }
        kickMenu.removeAll();
        kickMenu.setVisible(false);
        if (clients.size()>1){
            kickMenu.setVisible(true);
        }

        if(!clients.isEmpty()) {
            for (String client : clients) {

                if (Objects.equals(client, manager)){
                    onlineUsers.append(client + " (Manager)\n");
                }else{
                    JMenuItem clientItem = new JMenuItem(client);
                    clientItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            try {
                                os.writeObject("Kick "+clientItem.getText());
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                            System.out.println("hello "+clientItem.getText());
                        }
                    });
                    onlineUsers.append(client + "\n");
                    kickMenu.add(clientItem);
                }

            }
        }
    }
}
