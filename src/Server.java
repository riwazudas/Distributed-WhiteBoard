import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.awt.*;
import java.io.*;

import javax.swing.*;

public class Server extends JFrame
{
    private static JLabel numClients;
    private static JLabel numConversations;

    private static JTextArea serverLogTextArea;
    private static JLabel whiteboardNameLabel;
    private static JLabel clientCountLabel;



    public void createGUI() {
        setTitle("ServerGUI");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(400, 300));

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        JPanel whiteboardPanel = new JPanel();
        whiteboardPanel.setLayout(new FlowLayout());
        JLabel whiteboardLabel = new JLabel("Whiteboard Count:");
        whiteboardNameLabel = new JLabel("0");
        whiteboardPanel.add(whiteboardLabel);
        whiteboardPanel.add(whiteboardNameLabel);

        JPanel clientPanel = new JPanel();
        clientPanel.setLayout(new FlowLayout());
        JLabel clientLabel = new JLabel("Client Count:");
        clientCountLabel = new JLabel("0");
        clientPanel.add(clientLabel);
        clientPanel.add(clientCountLabel);
        controlPanel.add(whiteboardPanel);
        controlPanel.add(clientPanel);

        serverLogTextArea = new JTextArea(15, 30);
        serverLogTextArea.setEditable(false);
        serverLogTextArea.setText("");
        JScrollPane scrollPane = new JScrollPane(serverLogTextArea);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(controlPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void updateGUI()	//called when GUI needs to be updated
    {
        clientCountLabel.setText("Number of clients connected: "+ Handle.numClients);
        whiteboardNameLabel.setText("Number of conversations: "+Handle.numConversations);
        serverLogTextArea.setText("");
        for (WhiteBoard board: Handle.whiteBoards){
            serverLogTextArea.append("\nWhiteboard Name: "+ board.ID+"\n");

            if (board.clients.size()==0){
                serverLogTextArea.append("No Clients Found\n");
            }else{
                serverLogTextArea.append("Clients: ");
                for (User u:board.clients){
                    serverLogTextArea.append("\t"+u.getName()+"\n");
                }
            }

        }

    }

    public static void main(String[] args)
    {
        if (args.length != 1) {
            System.err.println("Usage: java Server <port>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        InetAddress ipAddress;
        try {
            ipAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.err.println("Unable to determine the local host IP address.");
            return;
        }

        System.out.println("Server is hosted on IP address: " + ipAddress.getHostAddress());

        new Server().createGUI();
        ServerSocket serverSocket = null;

        try
        {
            serverSocket = new ServerSocket(port);
            System.out.println("Server has started listening on port: "+port);
        }
        catch (IOException e)
        {
            System.err.println("Error: Cannot listen on port "+port+" : " + e);
            System.exit(1);
        }
        while (true)
        {
            Socket clientSocket = null;
            try
            {
                clientSocket = serverSocket.accept();
                System.out.println("Server has just accepted socket connection from a client");
            }
            catch (IOException e)
            {
                System.err.println("Accept failed: "+port + e);
                break;
            }
            Handle con = new Handle(clientSocket);
            con.start();

        }
        try
        {
            System.out.println("Closing server socket. ");
            serverSocket.close();
        }
        catch (IOException e)
        {
            System.err.println("Could not close server socket. " + e.getMessage());
        }
    }
}