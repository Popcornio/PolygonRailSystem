import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.util.*;

@SuppressWarnings("serial")
public class PolygonRS extends Polygon
{
	enum InputEnum
	{
		Create,	//	create new polygon
		AddSide,	//	add side
		RemoveSide,	//	remove side
		Recolor	//	recolor polygon
	}
	class PolygonPosition
	{
		int pointIndex = 0;
		float currentDistance = 0;	//	distance from the current point to the next point
	}
	
	//	Static-Final Attributes
	static final int MIN_SIDES = 3;
	static final int MAX_SIDES = 16;
	static final int MAX_DEPTH = 5;

	static final int INITIAL_DEPTH = 0;	//	this should never be modified; this is just here to describe the value
	static final int INITIAL_RADIUS = 100;	//	radius of the circle-base used to draw a polygon with equal sides
	static final int INITIAL_CYCLE_PERIOD = 10;
	
	//	Private Attributes
	private PolygonRS parent = null, root = null;
	
	private List<PolygonRS> children = new ArrayList<PolygonRS>();
	private PolygonPosition polygonPosition = new PolygonPosition();
	private int depth;	//	depth of current node in tree
	
	private int baseRadius;	//	pixels
	private float speed;		//	pixels/sec
	private float cyclePeriod;	//	How long it should take for a train should take to cycle its track (seconds).
	
	//	Public Attributes
	Point position = new Point(); 
	
	//	Constructor
	PolygonRS(PolygonRS parent)
	{
		if (parent == null)
		{
			this.parent = null;
			root = this;
			
			depth = INITIAL_DEPTH;
			baseRadius = INITIAL_RADIUS;
			cyclePeriod = INITIAL_CYCLE_PERIOD;
			
			resize(MIN_SIDES);
			speed = calculateSpeed();	//	speed needs to be calculated AFTER the polygon is defined!
		}
		else
		{
			this.parent = parent;
			root = this.parent.root;
			
			depth = parent.depth + 1;
			baseRadius = (int)(Math.pow(parent.baseRadius, 0.9d) + 0.5d);	//	the function to calculate a child base radius is modifiable
			cyclePeriod = (int)(Math.pow(parent.baseRadius, 1.1d) + 0.5d);	//	the function to calculate a child cycle period is modifiable
			
			resize(MIN_SIDES);
			speed = calculateSpeed();	//	speed needs to be calculated AFTER the polygon is defined!
		}
	}
	
	//	Private Methods
	private void addChild()
	{
		if (!canAddChild())
			return;
			
		//	Create new child PolygonRS, initialize its position, and then add it
		PolygonRS p = new PolygonRS(this);
		
		children.add(p);
	}
	private void grow()
	{
		resize(npoints + 1);
	}	
	private void shrink()
	{
		int shrinkedSize = npoints - 1;
		if (shrinkedSize < MIN_SIDES)
			delete();
		resize(shrinkedSize);
	}		
	private void resize(int size)
	{
		if (size >= MIN_SIDES && size <= MAX_SIDES)
		{
			reset();
			for (int i = 0; i < size; i ++)
			{
				int x = (int)Math.round((baseRadius + baseRadius * Math.cos(i * 2 * Math.PI / size)));
				int y = (int)Math.round((baseRadius + baseRadius * Math.sin(i * 2 * Math.PI / size)));
				addPoint(x,y);
			}
		}
	}	
	private void delete()
	{
		if (parent != null)
			parent.children.remove(this);
	}

	private void update(float deltaTime)
	{
		// Update position on parent rail-system
		// Update self, then children
		//	deltaTime is the time between updates
		
		float distance = deltaTime * speed + polygonPosition.currentDistance;	//	new distance along current line

		int pAi = polygonPosition.pointIndex;
		int pBi = (pAi + 1) % parent.npoints;
		
		Point a = new Point(parent.xpoints[pAi], parent.ypoints[pAi]);
		Point b = new Point(parent.xpoints[pBi], parent.ypoints[pBi]);
		
		float abLength = (float) Math.sqrt((b.x - a.x)*(b.x - a.x) + (b.y - a.y) * (b.y - a.y));

		// Wrap-around
		if (distance > abLength)
		{
			//	Shouldn't be able to pass multiple points in an update, so don't worry about multiple wraps
			polygonPosition.currentDistance = (distance - abLength);
			polygonPosition.pointIndex = pBi;
			
			//	Update the point indeces and the points
			pAi++;
			a = new Point(parent.xpoints[pBi], parent.ypoints[pBi]);
			pBi = (pAi + 1) % parent.npoints;
			b = new Point(parent.xpoints[pBi], parent.ypoints[pBi]);
		}
		else
		{
			//	Update position along current line
			polygonPosition.currentDistance += distance;
		}
		
		//	Get unit vector
		Point u = new Point(b.x - a.x, b.y - a.y);
		u.x /= abLength;
		u.y /= abLength;
		
		//	Get offset to point
		Point v = new Point((int) (u.x * polygonPosition.currentDistance), (int) (u.y * polygonPosition.currentDistance));
		
		position = new Point(a.x + v.x, a.y + v.y);
		
		//	Propagate updates from the parent polygon to its children.
		for (int i = 0; i < children.size(); i++)
			children.get(i).update(deltaTime);
	}
	
	
	//	Public Methods
	void sendInput(Point inputPos, InputEnum inputType)
	{
    	//	TODO: make this more space-efficient
    	Stack<PolygonRS> polyStack = new Stack<PolygonRS>();	//	a stack prioritizes leaves over roots
    	polyStack.add(this);	//	input starts at the object used as a reference, and works its way down.
    	
    	//	Find a valid PolygonRS to send the input to
    	PolygonRS p = null;
    	while (polyStack.size() > 0)
    	{
    		PolygonRS current = polyStack.pop();
			if (current.canReceiveInput() && current.contains(inputPos))
			{
				p = current;
				break;
			}
    		
    		for (int i = 0; i < current.children.size(); i++)
    			polyStack.push(current.children.get(i));
    	}
    	
    	//	Apply input to applicable PolygonRS
    	switch(inputType)
    	{
    	case Create:
			p.addChild();
    		break;
    		
    	case AddSide:
    		p.grow();
    		break;
    		
   	 	case RemoveSide:
   	 		p.shrink();
			break;
			
    	case Recolor:
    		//	does nothing
    		break;
    		
    	default: break;
    	}
	}
	void updateRoot(Point screenSize, float deltaTime)
	{
		//	A specialized update as the root does not move (but it stays in the center of the screen)
		
		Point screenMid = new Point((int) (0.5 * screenSize.x + 0.5), (int) (0.5 * screenSize.y + 0.5));
		position = screenMid;

		//	Propagate updates from the parent polygon to its children.
		for (int i = 0; i < children.size(); i++)
			children.get(i).update(deltaTime);
	}
	
	int getMaxChildren()
	{
		return (int)Math.log(npoints);
	}
	
	float calculateSpeed()
	{
		Point a = new Point(parent.xpoints[0], parent.ypoints[0]);
		Point b = new Point(parent.xpoints[1], parent.ypoints[1]);
		float sideLength = (float) Math.sqrt((b.x - a.x)*(b.x - a.x) + (b.y - a.y) * (b.y - a.y));
		
		float perimeter = sideLength * npoints;
		float speed = perimeter / cyclePeriod;
		
		return speed;
	}
	
	boolean canAddChild()
	{
		return children.size() < getMaxChildren() && depth < MAX_DEPTH;
	}
	boolean canGrowPolygon()
	{
		return npoints < MAX_SIDES;
	}
	boolean canReceiveInput()
	{
		return children.size() == 0;
	}

}
