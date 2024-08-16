
import java.net.*;
import java.util.*;
import java.io.*;

public class Handle extends Thread
{
    public static final LinkedList<WhiteBoard> whiteBoards = new LinkedList<WhiteBoard>();
    public static final Map<User,String> pendingRequest=new HashMap<>();
    public static int numClients=0;
    public static int numConversations=0;
    private final Socket clientSocket;
    private ObjectInputStream is;
    ObjectOutputStream os;
    private WhiteBoard w;

    public Handle(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
    }

    // The main thread execution method 

    public void run()
    {

        try
        {
            this.is = new ObjectInputStream(clientSocket.getInputStream());
            this.os = new ObjectOutputStream(clientSocket.getOutputStream());
            while (this.readCommand()) { }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private boolean readCommand() throws IOException {

        //for reading commands
        Object o;
        try
        {
            o = is.readObject();
        }
        catch (Exception e)
        {
            o = null;
        }
        if (o == null)
        {
            this.closeSocket();
            return false;
        }

        if(o.getClass() == String.class)
        {
            String s = (String) o;
            String[] newS = s.split(" ");
            List<String> listS = null;
            listS=Arrays.stream(newS).toList();
            System.out.println("message: "+o);
            if(newS[0].compareTo("new")==0)
                newConvo(String.join(" ", listS.subList(1,listS.size())));
            if(newS[0].compareTo("join")==0)
                joinConvo(String.join(" ", listS.subList(1,listS.size())));
            if(newS[0].compareTo("reqresponse")==0)
                sendResponse(String.join(" ", listS.subList(1,listS.size())));
            if(newS[0].compareTo("disconnect")==0)
                disconnect(String.join(" ", listS.subList(1,listS.size())));
            if(s.startsWith("Allowed")){
                joinAccept(String.join(" ", listS.subList(1,listS.size())));
            }
            if(s.startsWith("Denied")){
                joinReject(String.join(" ", listS.subList(1,listS.size())));
            }
            if(newS[0].compareTo("Kick")==0)
                kick(String.join(" ", listS.subList(1,listS.size())));
            if(newS[0].compareTo("New")==0)
                newBoard();
            if(s.startsWith("text")){
                for (User user : w.clients){
                    Handle h= user.getHandle();
                    h.os.writeObject(s);
                }
            }

        }
        else
        {
            WhiteBoardShape w = (WhiteBoardShape) o;
            addShape(w);
        }

        return true;
    }

    public synchronized void addShape(WhiteBoardShape shape)
    {
        w.add(shape,this);
    }
    public void sendShape(WhiteBoardShape shape)
    {
        try {
            os.writeObject(shape);
            os.flush();
        } catch (IOException e) {
            System.err.println("Error writing shape to client");
        }

    }

    public void closeSocket()
    {
        try
        {
            if(w!=null)
                w.removeClient(this);
            this.os.close();
            this.is.close();
            this.clientSocket.close();
        }
        catch (Exception ex)
        {
            System.err.println(ex);
        }
    }
    public void updateClient()
    {
        for(WhiteBoardShape s: w.shapes)
        {
            try {
                os.writeObject(s);
                os.flush();
            } catch (IOException e) {
                System.err.println("Error writing initial shapes to client");
            }
        }
    }

    public void sendResponse(String str) throws IOException {
        String name=str.substring(7);
        User u=null;

        for (Map.Entry<User, String> entry : pendingRequest.entrySet() ){
            User user = entry.getKey();
            System.out.println(user.getName());
            System.out.println(name);
            if (Objects.equals(user.getName(), name)){
                u=user;
                break;
            }
        }
        if (str.startsWith("accept ")){
            assert u != null;
            u.getHandle().os.writeObject("Allowed");
        }else{
            assert u != null;
            u.getHandle().os.writeObject("Denied");
        }
    }

    public void newConvo(String name) throws IOException {
        numClients++;
        Server.updateGUI();
        boolean isMatch = false;
        String s = null;
        try {
            s = (String) is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(WhiteBoard board: whiteBoards)
        {
            assert s != null;
            if(s.compareTo(board.ID)==0)
            {
                try{
                    os.writeObject("Error Whiteboard name already exists");
                    numClients--;
                    isMatch=true;
                    break;
                }
                catch(Exception e1)
                {
                    System.err.println(e1.getMessage());
                }
            }
        }
        if(!isMatch)	//no match
        {
            try{
                w = new WhiteBoard(s);
                w.addClient(name,this);
                w.manager=name;
                os.writeObject("Manager "+name);
                os.writeObject("Clients "+name);
                whiteBoards.add(w);
                numConversations++;
                Server.updateGUI();
            }catch(Exception e1)
            {
                System.err.println(e1.getMessage());
            }
        }
    }

    public void joinReject(String name) throws  IOException{
        Server.updateGUI();
        for (Map.Entry<User, String> entry : pendingRequest.entrySet() ){
            User user = entry.getKey();
            String board=entry.getValue();
            if (Objects.equals(user.getName(), name)){
                pendingRequest.remove(user);
                break;
            }
        }

    }

    public void joinAccept(String name) throws  IOException{
        numClients++;
        Server.updateGUI();
        String s = null;
        System.out.println(pendingRequest);
        System.out.println(s);

        for (Map.Entry<User, String> entry : pendingRequest.entrySet() ){
            User user = entry.getKey();
            String board=entry.getValue();
            System.out.println("now about now");

            System.out.println("user:"+user.getName());
            System.out.println("name:"+name);
            System.out.println(Objects.equals(user.getName(), name));

            if (Objects.equals(user.getName(), name)){
                s=board;
                pendingRequest.remove(user);
                break;
            }
        }
        System.out.println("this us boards");
        System.out.print(whiteBoards);
        for(WhiteBoard board: whiteBoards)
        {
            assert s != null;
            if(s.compareTo(board.ID)==0)
            {
                try{
                    w = board;
                    w.addClient(name, this);
                    System.out.println("this us clients");
                    System.out.print(w.clients);
                    System.out.print(w.manager);
                    StringBuilder nameList = new StringBuilder();
                    for (User u : board.clients) {
                        nameList.append(" ").append(u.getName());
                    }
                    String nList = nameList.toString().trim();
                    for (User u : board.clients) {
                        Handle h = u.getHandle();
                        h.os.writeObject("Manager "+board.manager);
                        h.os.writeObject("Clients " + nList);
                    }
                    updateClient();
                    whiteBoards.set(whiteBoards.indexOf(board), w);
                    Server.updateGUI();
                    break;

                }
                catch(Exception e1)
                {
                    System.err.println(e1.getMessage());
                }
            }
        }
    }

    public void newBoard() throws  IOException{
        w.shapes.clear();

        for(WhiteBoard board: whiteBoards)
        {
            assert w.ID != null;
            if(w.ID.compareTo(board.ID)==0)
            {
                try{
                    StringBuilder nameList = new StringBuilder();
                    for (User u : board.clients) {
                        nameList.append(" ").append(u.getName());
                    }
                    String nList = nameList.toString().trim();
                    for (User u : board.clients) {
                        Handle h = u.getHandle();
                        h.os.writeObject("clear");
                        h.os.writeObject("Manager "+board.manager);
                        h.os.writeObject("Clients " + nList);
                    }
                    updateClient();
                    whiteBoards.set(whiteBoards.indexOf(board), w);
                    Server.updateGUI();
                    break;
                }
                catch(Exception e1)
                {
                    System.err.println(e1.getMessage());
                }
            }
        }
    }

    public void joinConvo(String name) throws IOException {
        Server.updateGUI();
        boolean isMatch = false;
        boolean nameExists=false;
        String s = null;
        try {
            s = (String) is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("hello there");
        System.out.println(s);
        for(WhiteBoard board: whiteBoards)
        {
            assert s != null;
            if(s.compareTo(board.ID)==0)
            {
                try{
                    w = board;
                    for (User client:w.clients){
                        if (Objects.equals(client.getName(), name)){
                            os.writeObject("Error Username already in use");
                            nameExists=true;
                            isMatch=true;
                            numClients--;
                            break;
                        }
                    }
                    if (!nameExists) {
                        for (User u : board.clients) {
                            Handle h = u.getHandle();
                            if (Objects.equals(u.getName(), board.manager)){
                                h.os.writeObject("Reqjoin "+name);
                                pendingRequest.put(new User(name,this),s);
                                return;
                            }
                        }
                        System.out.println("this is being printed");
                        Server.updateGUI();
                        isMatch = true;
                        break;
                    }
                }
                catch(Exception e1)
                {
                    e1.getStackTrace();
                }
            }
        }
        if(!isMatch)
        {
            try{
                os.writeObject("Error Whiteboard not found");
                numClients--;
            }catch(Exception e1)
            {
                System.err.println(e1.getMessage());
            }
        }
    }

    public void kick(String name) throws IOException {
        try{
            Handle removeUserHandle=null;
            for (User user : w.clients){
                if (Objects.equals(user.getName(), name)){
                    removeUserHandle=user.getHandle();
                    break;
                }
            }
            assert removeUserHandle != null;
            removeUserHandle.os.writeObject("Kicked");
            w.removeClient(removeUserHandle);
            StringBuilder nameList= new StringBuilder();
            for (User u: w.clients){
                nameList.append(" ").append(u.getName());
            }
            String nList= nameList.toString().trim();
            for (User u:w.clients){
                Handle h = u.getHandle();
                h.os.writeObject("Clients "+nList);
            }
            updateClient();
            numClients--;
            Server.updateGUI();
        }catch(IOException e){
            System.err.println(e.getMessage());
        }

    }
    public void disconnect(String name)
    {
        try{
            if (Objects.equals(w.manager, name)){
                for (User u:w.clients){
                    Handle h = u.getHandle();
                    h.os.writeObject("Closed");
                }
                numConversations--;
                whiteBoards.remove(w);
            }else{
                w.removeClient(this);
                updateClient();
                StringBuilder nameList= new StringBuilder();
                for (User u: w.clients){
                    nameList.append(" ").append(u.getName());
                }
                String nList= nameList.toString().trim();
                for (User u:w.clients){
                    Handle h = u.getHandle();
                    h.os.writeObject("Clients "+nList);
                }

            }
            numClients--;
            Server.updateGUI();
        }catch(Exception e){
            System.err.println(e.getMessage());
        }

    }
}