/**
 * Notes:		October 20th
 * 				For testing purposes, I set a dialog box to demonstrate adding and removing
 * 				the first polygon to the screen. We discussed that removing the base polygon
 * 				would show the introduction text again, so that's what I was trying to simulate.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PolygonRailSystem extends JFrame
{
	public static void main(String[] args)
	{
		PolygonWindow prs = new PolygonWindow();
	}
}

class PolygonWindow extends JFrame
{
	int maxX, maxY, centerX, centerY;
	int numPolys = 0;
	
	public PolygonWindow()
	{
		setTitle("Polygon Rail System");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(50, 50, 700, 500);
		setResizable(true);
		setVisible(true);
		
		Handler h = new Handler();
		addMouseListener(h);
	}
	
	// Retreives the dimensions of the frame
	void initgr()
	{
		   Dimension d = getSize();
		   maxX = d.width - 1;
		   maxY = d.height - 1;
		   centerX = maxX / 2;
		   centerY = maxY / 2;
	}
	
	// MouseListener functionality
	private class Handler implements MouseListener
	{
		public void mouseClicked(MouseEvent e)
		{
			if (SwingUtilities.isLeftMouseButton(e))
			{
				JOptionPane.showMessageDialog(null, "Polygon Added");
				numPolys++;
				repaint();
			}
			if (SwingUtilities.isRightMouseButton(e))
			{
				JOptionPane.showMessageDialog(null, "Polygon Removed");
				
				// Doesn't allow for a negative number of polygons
				if (numPolys > 0)
					numPolys--;
				
				repaint();
			}
			
			return;
		}
		
		public void mousePressed(MouseEvent e)
		{
			return;
		}
		
		public void mouseReleased(MouseEvent e)
		{
			return;
		}
		
		public void mouseEntered(MouseEvent e)
		{
			return;
		}
		
		public void mouseExited(MouseEvent e)
		{
			return;
		}
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
						+ "Left Click: Add Polygon\n"
						+ "Right Click - Remove Polygon\n\n"
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
		
		if (numPolys == 0)
			introductionScreen(g);
	}
	
}