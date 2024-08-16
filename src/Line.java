import java.awt.Color;
import java.awt.Graphics;

public  class Line extends WhiteBoardShape {

    public Line(int x1, int y1, int x2, int y2, Color c)
    {
        super(x1, y1, x2, y2, c);
    }
    public void paint(Graphics g)
    {
        g.setColor(this.c);
        g.drawLine(x1, y1, x2, y2);
    }
}
