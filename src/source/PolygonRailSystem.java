package source;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.*;

import source.PolygonRS.InputEnum;


public class PolygonRailSystem
{
	public static void main(String[] args)
	{
        JFrame frame = new JFrame();
        frame.pack();
        
        frame.setTitle("Polygon Rail System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        frame.setResizable(true);

        frame.createBufferStrategy(3);

		PolygonWindow prs = new PolygonWindow();
		
        frame.add(prs);
        frame.setVisible(true);
        
        //	Add Input Listeners
        
        frame.addKeyListener(new KeyListener() 
        {
            @Override
            public void keyPressed(KeyEvent e) 
            {
            	switch (e.getKeyChar())
            	{
            	case 'o':
            		prs.showBase = !prs.showBase;
            		break;
            	case 'p':
            		prs.logicRunning = !prs.logicRunning;
            		break;
            		
            	case '[':
            		if (prs.root != null)
            			prs.root.adjustCyclePeriod(-1);
            		break;
            	case ']':
            		if (prs.root != null)
            			prs.root.adjustCyclePeriod(1);
            		break;
            	case 'r':
            		if (prs.root != null)
            			prs.root.resetCyclePeriod();
            		break;
            		
            	case '{':
            		if (prs.root != null)
            			prs.root.adjustCyclePeriodPower(-0.0125d);
            	break;
            	case '}':
            		if (prs.root != null)
            			prs.root.adjustCyclePeriodPower(0.0125d);
            	break;
            	case 'R':
            		if (prs.root != null)
            			prs.root.resetCyclePeriodPower();
            	break;
            		
            		
            	case '0':
            		prs.root = null;
            		PolygonRS.resetSettings();
            		break;
            	case '1':
            		prs.preset1();
            		break;
            	case'2':
            		prs.preset2();
            		break;
            	case '3':
            		prs.preset3();
            		break;
            	case '4':
            		prs.preset4();
            		break;
            		
            	default:
            		break;
            	}
            }

            @Override
            public void keyReleased(KeyEvent e) { }
            @Override
            public void keyTyped(KeyEvent e) { }
        });
	}
}

class PolygonWindow extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	boolean showBase = false;
	int maxX, maxY, centerX, centerY;
	PolygonRS root = null;

	long lastUpdate = 0;
	boolean logicRunning = true;
	
	// Used for to call the repaint() method to allow the polygons to move
	Timer logicTimer = new Timer(20, new ActionListener(){
		public void actionPerformed(ActionEvent evt) {
			if (logicRunning && root != null)
				root.initializeUpdate(getSize(), (System.currentTimeMillis() - lastUpdate) / 1000d);
			lastUpdate = System.currentTimeMillis();
	    }    
	});
	Timer graphicsTimer = new Timer(20, new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			repaint();
	    }    
	});
	
	PolygonWindow()
	{
		addMouseListener(new MouseAdapter() { 
			public void mousePressed(MouseEvent e) 
			{ 
				if (SwingUtilities.isLeftMouseButton(e))
				{
					//System.out.println("Attempting to Grow Polygon...");
					if (root != null)
						root.sendInput(e.getPoint(), InputEnum.AddSide);
				}
				if (SwingUtilities.isRightMouseButton(e))
				{
					//System.out.println("Attempting to Shrink Polygon...");
					if (root != null && root.npoints == PolygonRS.MIN_SIDES && root.canModifyPolygon() && root.getPolygon().contains(e.getPoint()))
						root = null;
					else if (root != null)
						root.sendInput(e.getPoint(), InputEnum.RemoveSide);
				}
				if (SwingUtilities.isMiddleMouseButton(e))
				{
					//System.out.println("Attempting to Create Polygon...");
					if (root != null)
						root.sendInput(e.getPoint(), InputEnum.Create);
					else
					{
						root = new PolygonRS();
						root.initializeUpdate(getSize(), 0);
					}
				}
				if (root != null)
					restartTimers();
			} 
		});
	}
	
	void restartTimers()
	{
		lastUpdate = System.currentTimeMillis();
		logicTimer.restart();
		graphicsTimer.start();
	}
	
	void preset4()
	{
		root = new PolygonRS();
		root.initializeUpdate(getSize(), 0);
		Queue<PolygonRS> q = new ArrayDeque<PolygonRS>();
		q.add(root);
		
		while (!q.isEmpty())
		{
			PolygonRS p = q.remove();
			for (int i = 0; i < 16; i++)
				p.addPolygonSide();
			for (int i = 0; i < 4; i++)
				p.createPolygonRSChild();
			for(int i = 0; i < p.getChildren().size(); i++)
				q.add(p.getChildren().get(i));
		}
		restartTimers();
	}
	void preset3()
	{
		root = new PolygonRS();
		root.initializeUpdate(getSize(), 0);
		Queue<PolygonRS> q = new ArrayDeque<PolygonRS>();
		q.add(root);
		
		while (!q.isEmpty())
		{
			PolygonRS p = q.remove();
			for (int i = 0; i < 8; i++)
				p.addPolygonSide();
			for (int i = 0; i < 3; i++)
				p.createPolygonRSChild();
			for(int i = 0; i < p.getChildren().size(); i++)
				q.add(p.getChildren().get(i));
		}
		restartTimers();
	}
	void preset2()
	{
		root = new PolygonRS();
		root.initializeUpdate(getSize(), 0);
		Queue<PolygonRS> q = new ArrayDeque<PolygonRS>();
		q.add(root);
		
		while (!q.isEmpty())
		{
			PolygonRS p = q.remove();
			for (int i = 0; i < 4; i++)
				p.addPolygonSide();
			for (int i = 0; i < 2; i++)
				p.createPolygonRSChild();
			for(int i = 0; i < p.getChildren().size(); i++)
				q.add(p.getChildren().get(i));
		}
		restartTimers();
	}
	void preset1()
	{
		root = new PolygonRS();
		root.initializeUpdate(getSize(), 0);
		Queue<PolygonRS> q = new ArrayDeque<PolygonRS>();
		q.add(root);
		
		while (!q.isEmpty())
		{
			PolygonRS p = q.remove();
			for (int i = 0; i < 2; i++)
				p.addPolygonSide();
			for (int i = 0; i < 1; i++)
				p.createPolygonRSChild();
			for(int i = 0; i < p.getChildren().size(); i++)
				q.add(p.getChildren().get(i));
		}
		restartTimers();
	}
	
	// Retrieves the dimensions of the frame
	void initgr()
	{
		   Dimension d = getSize();
		   maxX = d.width - 1;
		   maxY = d.height - 1;
		   centerX = maxX / 2;
		   centerY = maxY / 2;
	}
	
	// Used to display the introduction text and ensures the text is centered onto the canvas
	public void introductionScreen(Graphics g)
	{
		int numLines	 = 0;
		int stringWidth	 = 0;
		int stringHeight = 0;
		
		String intro =    "Polygon Rail System\n"
						+ "Team #8\n"
						+ "Daniel Pacheco, Jessica Jennings,\n"
						+ "Zackary Hoyt, Tiffany Engen\n\n"
						+ "Controls:\n"
						+ "Left Click: Grow Polygon (+side)\n"
						+ "Middle Click: Add Polygon\n"
						+ "Right Click: Shrink Polygon (-side)\n\n"
						+ "Click anywhere to continue...";
		
		g.setFont(new Font("Verdana", Font.PLAIN, 14));
		FontMetrics metrics = g.getFontMetrics();
		
		// Determines the width and height of the introduction text
		for (String line : intro.split("\n"))
		{
			numLines++;
			
			if (metrics.stringWidth(line) > stringWidth)
			{
				stringWidth = metrics.stringWidth(line);
			}
		}
		stringHeight = numLines * metrics.getHeight();
		
		// Determines the position for the introduction text based on the string's dimensions
		int x = (maxX - stringWidth) / 2;
		int y = (maxY - stringHeight) / 2;
		
		// Displays the introduction text on screen
		for (String line : intro.split("\n"))
			g.drawString(line, x, y += g.getFontMetrics().getHeight());
	}
	
	public void paint(Graphics g)
	{
		initgr();
		super.paint(g);
		
		if (root == null )
		{
			graphicsTimer.stop();
			logicTimer.stop();
			introductionScreen(g);
		}
		else
		{
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Color[] colors = {Color.YELLOW , Color.RED, Color.BLUE, Color.PINK, Color.ORANGE, Color.GRAY, Color.GREEN};
			g2.setStroke(new BasicStroke(4));	//line thickness
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.60f)); //the opacity of the rectangles
			int colorIndex = 0;

			LinkedList<PolygonRS> polygonRSList = new LinkedList<PolygonRS>(root.getRSList());

			for (int i = 0; i < polygonRSList.size(); i++)
			{
				g2.setPaint(colors[(colorIndex++) % 7]);	//%3 if adding more colors increase
				g2.fill(polygonRSList.get(i).getPolygon());	//fill
				g2.setPaint(Color.BLACK);
				g.drawPolygon(polygonRSList.get(i).getPolygon());

				if (showBase)
				{
					Point center = polygonRSList.get(i).getPolygonOrigin();
					int size = 2 * polygonRSList.get(i).getBaseRadius();
					g2.drawOval(center.x, center.y, size, size);
				}
			}
		}
	}
}
