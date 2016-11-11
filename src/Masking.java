
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class Masking extends JFrame {

    public Masking(ArrayList<Polygon> polys) {
        this.setSize(500, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(new PaintSurface(polys), BorderLayout.CENTER);
        this.setVisible(true);
    }

    private class PaintSurface extends JComponent {
        ArrayList<Polygon> shapes = new ArrayList<Polygon>();

        public PaintSurface(ArrayList<Polygon> polys) {
            shapes = polys;
            repaint();
        }
        private void paintBackground(Graphics2D g2){
            g2.setPaint(Color.WHITE);

        }
    public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintBackground(g2);
            Color[] colors = { Color.YELLOW , Color.RED,
                    Color.BLUE, Color.PINK, Color.ORANGE, Color.GRAY, Color.GREEN};
            int colorIndex = 0;

            g2.setStroke(new BasicStroke(4)); //line thickness
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.60f)); //the opacity of the rectangles

            for (Polygon p : shapes) {
                g2.setPaint(Color.BLACK);
                g2.draw(p);
                g2.setPaint(colors[(colorIndex++) % 7]); //%3 if adding more colors increase
                g2.fill(p); //fill
            }
        }
        }
}
