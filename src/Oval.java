import java.awt.Color;
import java.awt.Graphics;

public  class Oval extends WhiteBoardShape
{

    public Oval(int x1, int y1, int x2, int y2, Color c)
    {
        super(x1, y1, x2, y2, c);
    }
    public void paint(Graphics g)
    {
        g.setColor(this.c);
        if(x1<x2&&y1<y2)
            g.drawOval(x1, y1, (x2-x1), (y2-y1));
        if(x1<x2&&y1>y2)
            g.drawOval(x1, y2, (x2-x1), (y1-y2));
        if(x1>x2&&y1>y2)
            g.drawOval(x2, y2, (x1-x2), (y1-y2));
        if(x1>x2&&y1<y2)
            g.drawOval(x2, y1, (x1-x2), (y2-y1));
    }
}
