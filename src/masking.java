
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class masking extends JFrame {

    public static void main(String[] args) {
        new masking();
    }

    public masking() {
        this.setSize(500, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(new PaintSurface(), BorderLayout.CENTER);
        this.setVisible(true);
    }

    private class PaintSurface extends JComponent {
        ArrayList<Polygon> shapes = new ArrayList<Polygon>();
        int numOfShapes = 4;
        int xPoints[][] = {{200,250, 150},
                {175, 225, 125},
                {150, 200, 100},
                {225, 275, 175}};
        int yPoints [][] = {{200, 150, 150},
                {175, 125, 125},
                {150, 100, 100},
                {225, 175, 175}};

        public PaintSurface() {
            for(int i = 0; i < numOfShapes; i++){
                Polygon poly = new Polygon();
                for(int j = 0; j < 3; j++){
                    poly.addPoint(xPoints[i][j], yPoints[i][j]);
                }
                shapes.add(i, poly);
            }
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

        private Rectangle2D.Float makeRectangle(int x1, int y1, int x2, int y2) {
            return new Rectangle2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
        }
    }
}