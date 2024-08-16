import java.awt.*;
import java.util.LinkedList;


public class WhiteBoardCanvas extends Canvas	//canvas for client
{
    private final LinkedList<WhiteBoardShape> shapes;	//shapes in whiteboard
    private WhiteBoardShape currentShape;	//shape currently being drawn

    public WhiteBoardCanvas()
    {
        super();
        shapes = new LinkedList<>();
        currentShape = null;
    }
    public void paint(Graphics g)
    {
        try{
            for(WhiteBoardShape s: shapes)
            {
                s.paint(g);
            }
            if(currentShape!=null)
            {
                currentShape.paint(g);	//paints temp shape for drag draw effect
                currentShape=null;
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

    }
    public void add(WhiteBoardShape s)	//shape added
    {
        shapes.add(s);
        repaint();
    }
    public void addTemp(WhiteBoardShape s) //temp shape when mouse is being dragged
    {
        currentShape = s;
        repaint();
    }
    public void clear()	//clears the board
    {
        shapes.clear();
        repaint();
    }
}
