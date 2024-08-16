import java.util.LinkedList;


public class WhiteBoard	//whiteboard convo class
{
    public LinkedList<WhiteBoardShape> shapes;	//shapes in board
    public LinkedList<User> clients;	//clients connected
    public String manager;
    public String ID;

    public WhiteBoard(String ID)	//new whiteboard
    {
        shapes = new LinkedList<WhiteBoardShape>();
        clients = new LinkedList<User>();
        this.ID = ID;
    }

    public synchronized void add(WhiteBoardShape s,Handle handle)	//add a shape to board
    {
        shapes.add(s);
        for(User u:clients)	//tells all the clients connected
        {
            Handle h=u.getHandle();
            if(h!=handle)
                h.sendShape(s);
        }
    }
    public void addClient(String name,Handle handle)	//new client
    {
        clients.add(new User(name,handle));

    }
    public void removeClient(Handle handle)	//clients leaves
    {
        clients.removeIf(u -> u.getHandle() == handle);
    }
}
