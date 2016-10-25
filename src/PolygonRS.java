import java.awt.Point;
import java.awt.Polygon;
import java.util.*;

@SuppressWarnings("serial")
public class PolygonRS extends Polygon
{
    class PolygonPosition
    {
        int pointIndex = 0;
        float currentDistance = 0;
    }
    
    private PolygonRS parent = null, root = null;
    private List<PolygonRS> children = new ArrayList<PolygonRS>();
    private PolygonPosition polygonPosition = new PolygonPosition();
    
    
    private float speed = 10;		//	pixels/sec
    private int baseRadius = 100;	//	pixels
    private int depth = 0;	//	depth of current node in tree
    
    static final int initialSideCount = 3;
    static final int maximumSideCount = 12;
    static final int maximumDepth = 5;
    
    PolygonRS(PolygonRS parent)
    {
        if (parent == null)
        {
            root = this;
        }
        else
        {
            this.parent = parent;
            depth = parent.depth + 1;
        }
        
        //	Initialize Polygon to Triangle
        reset();
        for (int i = 0; i < initialSideCount; i ++)
        {
            int x = (int)Math.round((baseRadius + baseRadius * Math.cos(i * 2 * Math.PI / initialSideCount)));
            int y = (int)Math.round((baseRadius + baseRadius * Math.sin(i * 2 * Math.PI / initialSideCount)));
            addPoint(x,y);
        }
    }
    
    int getMaxChildren()
    {
        return (int)Math.log(npoints);
    }
    boolean canAddChild()
    {
        return children.size() < getMaxChildren() && depth < maximumDepth;
    }
    boolean canGrowPolygon()
    {
        return npoints < maximumSideCount;
    }
    boolean canModify()
    {
        return children.size() == 0;
    }
    void addChild()
    {
        if (!canAddChild())
            return;
        children.add(new PolygonRS(this));
        
        //	Set initial child position
        children.get(children.size() - 1).polygonPosition.pointIndex = npoints / children.size();
    }
    void delete()
    {
        if (parent != null)
            parent.children.remove(this);
        // else terminate program
    }
    void grow()
    {
        int newSize = npoints + 1;
        if (newSize < maximumSideCount)
        {
            reset();
            for (int i = 0; i < newSize; i ++)
            {
                int x = (int)Math.round((baseRadius + baseRadius * Math.cos(i * 2 * Math.PI / newSize)));
                int y = (int)Math.round((baseRadius + baseRadius * Math.sin(i * 2 * Math.PI / newSize)));
                addPoint(x,y);
            }
        }
    }
    void shrink()
    {
        if (npoints <= 3)
            delete();
        int newSize = npoints - 1;
        if (newSize < maximumSideCount)
        {
            reset();
            for (int i = 0; i < newSize; i ++)
            {
                int x = (int)Math.round((baseRadius + baseRadius * Math.cos(i * 2 * Math.PI / newSize)));
                int y = (int)Math.round((baseRadius + baseRadius * Math.sin(i * 2 * Math.PI / newSize)));
                addPoint(x,y);
            }
        }
    }
    void update(float deltaTime)
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
        }
        else
        {
            //	Update position along current line
            polygonPosition.currentDistance += distance;
        }
        
        //	Propagate updates from the parent polygon to its children.
        for (int i = 0; i < children.size(); i++)
            children.get(i).update(deltaTime);
    }
}