import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;

public abstract class WhiteBoardShape implements Serializable //shape abstract class
{
    int x1,y1,x2,y2;	//coords
    Color c;
    public WhiteBoardShape(int x1,int y1, int x2, int y2, Color c)
    {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.c = c;
    }
    public abstract void paint(Graphics g);	//all shapes must have paint method
}
