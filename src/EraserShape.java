import java.awt.*;

public class EraserShape extends WhiteBoardShape {
    private int size;

    public EraserShape(int x, int y, int size) {
        super(x, y, x + 10, y + 10, Color.WHITE);
        this.size = size;
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(c);
        g.fillRect(x1, y1, size, size);
    }
}