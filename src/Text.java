import java.awt.Color;
import java.awt.Graphics;

public  class Text extends WhiteBoardShape
{

    String text;
    public Text(int x1, int y1, int x2, int y2, Color c,String s)
    {
        super(x1, y1, x2, y2, c);
        text = s;
    }
    public void paint(Graphics g)
    {
        g.setColor(this.c);
        g.drawString(text,x1,y1);
    }
}
