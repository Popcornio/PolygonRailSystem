package source;
/**
 * Notes:		October 20th
 * 				For testing purposes, I set a dialog box to demonstrate adding and removing
 * 				the first polygon to the screen. We discussed that removing the base polygon
 * 				would show the introduction text again, so that's what I was trying to simulate.
 */

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.*;

import source.PolygonRS.InputEnum;


public class PolygonRailSystem
{
	public static void main(String[] args)
	{
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        
        frame.setTitle("Polygon Rail System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(50, 50, 700, 500);
        frame.	setResizable(true);

        frame.createBufferStrategy(2);

		//PolygonWindow prs = new PolygonWindow();
        frame.add(new PolygonWindow());
        frame.setVisible(true);
	}
}

class PolygonWindow extends JPanel
{
	boolean showBase = false;
	int maxX, maxY, centerX, centerY;
	PolygonRS root = null;

	long lastUpdate = 0;
	
	// Used for to call the repaint() method to allow the polygons to move
	Timer logicTimer = new Timer(50, new ActionListener(){
		public void actionPerformed(ActionEvent evt) {
			root.initializeUpdate(getSize(), (System.currentTimeMillis() - lastUpdate) / 1000d);
			lastUpdate = System.currentTimeMillis();
	    }    
	});
	Timer graphicsTimer = new Timer(100, new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			repaint();
	    }    
	});
	
	
	public PolygonWindow()
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
				logicTimer.start();
				graphicsTimer.start();
			} 
		}); 
		
		if (false)
		{
			root = new PolygonRS();
			Queue<PolygonRS> q = new ArrayDeque();
			q.add(root);
			
			while (!q.isEmpty())
			{
				for (int i = 0; i < 16; i++)
					q.peek().addPolygonSide();
				for (int i = 0; i < 4; i++)
					q.peek().createPolygonRSChild();
				for(int i = 0; i < 4; i++)
					q.add(q.peek().getChildren().get(i));
				q.remove();
			}
			graphicsTimer.start();
			repaint();
		}
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
						+ "Daniel Pacheco, Jessican Jennings,\n"
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
				g2.setPaint(Color.BLACK);
				g.drawPolygon(polygonRSList.get(i).getPolygon());
				g2.setPaint(colors[(colorIndex++) % 7]);	//%3 if adding more colors increase
				g2.fill(polygonRSList.get(i).getPolygon());	//fill

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