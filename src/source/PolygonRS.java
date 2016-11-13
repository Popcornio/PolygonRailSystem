package source;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Polygon;
import java.util.*;

@SuppressWarnings("serial")
public class PolygonRS extends Polygon
{
	public enum InputEnum
	{
		Create,	//	create new polygon
		AddSide,	//	add side
		RemoveSide,	//	remove side
		Recolor	//	re-color polygon
	}
	class PolygonPosition
	{
		int pointIndex = 0;
		double currentDistance = 0;	//	distance from the current point to the next point
	}
	
	//	Static-Final Attributes
	static final int MIN_SIDES = 3;
	static final int MAX_SIDES = 16;
	static final int MAX_DEPTH = 5;

	static final int INITIAL_DEPTH = 0;	//	this should never be modified; this is just here to describe the value
	static final int INITIAL_RADIUS = 100;	//	radius of the circle-base used to draw a polygon with equal sides
	static final int INITIAL_CYCLE_PERIOD = 10;
	
	//	Private Attributes
	private PolygonRS parent = null;
	
	private List<PolygonRS> children = new ArrayList<PolygonRS>();
	private PolygonPosition polygonPosition = new PolygonPosition();
	private int depth;	//	depth of current node in tree
	
	private int baseRadius;	//	pixels
	private double speed;		//	pixels/sec
	private double cyclePeriod;	//	How long it should take for a train should take to cycle its track (seconds).
	
	private Point center = new Point();	//	modified in every update 
		
	//	Constructor
	
	PolygonRS()
	{
		this.parent = null;
		
		depth = INITIAL_DEPTH;
		baseRadius = INITIAL_RADIUS;
		cyclePeriod = INITIAL_CYCLE_PERIOD;
			
		resize(MIN_SIDES);
		speed = calculateSpeed();	//	speed needs to be calculated AFTER the polygon is defined!
	}
	private PolygonRS(PolygonRS parent)
	{
		if (parent == null)
			System.gc();
		
		this.parent = parent;
			
		depth = parent.depth + 1;
		baseRadius = (int)(Math.pow(parent.baseRadius, 0.9d) + 0.5d);	//	the function to calculate a child base radius is modifiable
		cyclePeriod = (int)(Math.pow(parent.baseRadius, 1.1d) + 0.5d);	//	the function to calculate a child cycle period is modifiable
			
		resize(MIN_SIDES);
		speed = calculateSpeed();	//	speed needs to be calculated AFTER the polygon is defined!
		
	}
	
	//	Private Methods

	private void addPolygonSide() { resize(npoints + 1); setChildrenInitPos(); }
	private void removePolygonSide() { children.clear(); resize(npoints - 1); }
	private void createPolygonRSChild()
	{
		if (!canAddChild())
			return;

		PolygonRS p = new PolygonRS(this);
		children.add(p);
		
		//	Create the new child and add it to the list of children
		setChildrenInitPos();
	}
	private void resize(int size)
	{
		if (size < MIN_SIDES)
		{
			delete();
		}
		else if (size <= MAX_SIDES)
		{
			reset();
			
			double thetaAddend = size % 2 == 0? -Math.PI / size : -Math.PI / 2;
			for (int i = 0; i < size; i ++)
			{
				int x = (int)Math.round((baseRadius + baseRadius * Math.cos(i * 2 * Math.PI / size + thetaAddend)));
				int y = (int)Math.round((baseRadius + baseRadius * Math.sin(i * 2 * Math.PI / size + thetaAddend)));
				addPoint(x,y);
			}
		}
	}
	
	private void setChildrenInitPos()
	{
		//	Reset the children's positions and then space them relative to each other.
		if (children.size() == 0)
			return;

		double timeOffset = children.get(0).cyclePeriod / (double) children.size();
		for (int i = 0; i < children.size(); i++)
		{
			children.get(i).resetPosition();
			children.get(i).update(i * timeOffset);
		}
	}
	private void resetPosition()
	{
		polygonPosition.currentDistance = 0;
		polygonPosition.pointIndex = 0;
		
		for (int i = 0; i < children.size(); i++)
			children.get(i).resetPosition();
	}
	
	private void delete()
	{
		children.clear();
		if (parent != null)
		{
			parent.children.remove(this);
			parent.setChildrenInitPos();
		}
		else
			System.gc();
	}

	private void update(double deltaTime)
	{
		//	Update position on parent rail-system
		
		//	Update self, then children

		int pAi = polygonPosition.pointIndex;
		int pBi = (pAi + 1) % parent.npoints;
		
		Point a = new Point(parent.xpoints[pAi], parent.ypoints[pAi]);
		Point b = new Point(parent.xpoints[pBi], parent.ypoints[pBi]);
		
		double abLength = Math.sqrt((b.x - a.x)*(b.x - a.x) + (b.y - a.y) * (b.y - a.y));

		polygonPosition.currentDistance += deltaTime * speed;	//	new distance along current line
		
		//	Account for overlapping
		int overflow = (int) (polygonPosition.currentDistance / abLength);
		if (polygonPosition.currentDistance > abLength)
		{
			polygonPosition.pointIndex = (pAi + overflow) % parent.npoints;
			polygonPosition.currentDistance = -overflow * abLength;
			update(deltaTime);
			return;
		}
		
		//	Get unit vector
		double ux = (double) (b.x - a.x) / abLength, uy = (double) (b.y - a.y) / abLength;
		
		//	Get offset to point
		int vx = (int) (ux * polygonPosition.currentDistance + 0.5), vy = (int) (uy * polygonPosition.currentDistance + 0.5);
		
		center = parent.getPolygonOrigin();
		center.x += a.x + vx;
		center.y += a.y + vy;

		//	Propagate updates from the parent polygon to its children.
		for (int i = 0; i < children.size(); i++)
			children.get(i).update(deltaTime);
	}
	
	private void applyInput(PolygonRS p, InputEnum inputType)
	{
    	switch(inputType)
    	{
    	case Create:
			p.createPolygonRSChild();
    		break;
    		
    	case AddSide:
    		p.addPolygonSide();
    		break;
    		
   	 	case RemoveSide:
   	 		p.removePolygonSide();
			break;
			
    	case Recolor:
    		//	does nothing
    		break;
    		
    	default: break;
    	}
	}
	
	
	//	Public Methods

	public Point getPolygonOrigin() { return new Point(center.x - baseRadius, center.y - baseRadius); }
	public int getMaxChildren() { return (int) (Math.log(npoints) / Math.log(2)); }
	public int getBaseRadius() { return baseRadius; }	
	public boolean canAddChild() { return children.size() < getMaxChildren() && depth < MAX_DEPTH; }
	public boolean canGrowPolygon() { return npoints < MAX_SIDES; }
	public boolean canModifyPolygon() { return children.size() == 0; }
	public double calculateSpeed()
	{
		//	Calculate the speed to travel at for the existing railway this train is on, and the cycle period.
		if (parent == null)
			return 0;
		
		Point a = new Point(parent.xpoints[0], parent.ypoints[0]);
		Point b = new Point(parent.xpoints[1], parent.ypoints[1]);
		double sideLength = (float) Math.sqrt((b.x - a.x)*(b.x - a.x) + (b.y - a.y) * (b.y - a.y));
		
		double perimeter = sideLength * parent.npoints;
		double speed = perimeter / cyclePeriod;
		
		return speed;
	}
	
	public void initializeUpdate(Dimension screenSize, double deltaTime)
	{
		//	A specialized update as the root does not move (but it stays in the center of the screen)		
		Point screenMid = new Point((int) (0.5 * screenSize.width + 0.5), (int) (0.5 * screenSize.height + 0.5));
		center = screenMid;

		//	Propagate updates from the parent polygon to its children.
		for (int i = 0; i < children.size(); i++)
			children.get(i).update(deltaTime);
	}
	
	/*
	public void sendInput(Point inputPos, InputEnum inputType)
	{
    	Stack<PolygonRS> polyStack = new Stack<PolygonRS>();	//	a stack prioritizes leaves over roots
    	polyStack.add(this);	//	input starts at the object used as a reference, and works its way down.
    	
    	//	Find a valid PolygonRS to send the input to
    	while (polyStack.size() > 0)
    	{
    		PolygonRS current = polyStack.pop();
			if (current.getPolygon().contains(inputPos))
			{
				applyInput(current, inputType);
		    	return;
			}
    		
    		for (int i = 0; i < current.children.size(); i++)
    			polyStack.push(current.children.get(i));
    	}
	}
	*/
	
	public void sendInput(Point inputPos, InputEnum inputType)
	{
    	Stack<PolygonRS> polyStack = new Stack<PolygonRS>();	//	a stack prioritizes leaves over roots
    	polyStack.add(this);	//	input starts at the object used as a reference, and works its way down.
    	
    	//	Find a valid PolygonRS to send the input to

    	switch(inputType)
    	{
    	case Create:
        	while (polyStack.size() > 0)
        	{
        		PolygonRS current = polyStack.pop();
    			if (current.getPolygon().contains(inputPos) && current.canAddChild())
    			{
    				current.createPolygonRSChild();
    		    	return;
    			}
        		
        		for (int i = 0; i < current.children.size(); i++)
        			polyStack.push(current.children.get(i));
        	}
    		break;
    		
    	case AddSide:
        	while (polyStack.size() > 0)
        	{
        		PolygonRS current = polyStack.pop();
    			if (current.getPolygon().contains(inputPos) && current.canModifyPolygon())
    			{
    				current.addPolygonSide();
    		    	return;
    			}
        		
        		for (int i = 0; i < current.children.size(); i++)
        			polyStack.push(current.children.get(i));
        	}
    		break;
    		
   	 	case RemoveSide:
   	    	while (polyStack.size() > 0)
   	    	{
   	    		PolygonRS current = polyStack.pop();
   				if (current.getPolygon().contains(inputPos) && current.canModifyPolygon())
   				{
   					current.removePolygonSide();
   			    	return;
   				}
   	    		
   	    		for (int i = 0; i < current.children.size(); i++)
   	    			polyStack.push(current.children.get(i));
   	    	}
			break;
			
    	case Recolor:
    		//	does nothing
    		break;
    		
    	default: break;
    	}
	}
	
	public List<PolygonRS> getRSList()
	{
		//	Return a PolygonRS list ordered by depth from shallowest to deepest (least to greatest).
		
		List<PolygonRS> polygonList = new LinkedList<PolygonRS>();
		Queue<PolygonRS> polygonListBufferQueue = new ArrayDeque<PolygonRS>();	//	use a queue to prioritize shallower children
		
		//	Generate a depth-ordered list starting at the referenced object (this)
		polygonListBufferQueue.add(this);
		
		//	Traverse and store the results of the PolygonRS tree
		while (polygonListBufferQueue.size() > 0)
		{
			PolygonRS current = polygonListBufferQueue.remove();
			polygonList.add(current);
			for (int i = 0; i < current.children.size(); i++)
				polygonListBufferQueue.add(current.children.get(i));
		}
		
		return polygonList;
	}
	
	public Polygon getPolygon()
	{
		Polygon p = new Polygon();
		Point offset = getPolygonOrigin();
		for (int i = 0; i < npoints; i++)
			p.addPoint(xpoints[i] + offset.x, ypoints[i] + offset.y);
		
		return p;
	}

}
