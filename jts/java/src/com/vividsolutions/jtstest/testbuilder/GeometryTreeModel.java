package com.vividsolutions.jtstest.testbuilder;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class GeometryTreeModel implements TreeModel {
    private Vector treeModelListeners =
        new Vector();
    private GeometryNode rootGeom;

    public GeometryTreeModel(Geometry geom) {
        rootGeom = GeometryNode.create(geom);
    }


//////////////// TreeModel interface implementation ///////////////////////

    /**
     * Adds a listener for the TreeModelEvent posted after the tree changes.
     */
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.addElement(l);
    }

    /**
     * Returns the child of parent at index index in the parent's child array.
     */
    public Object getChild(Object parent, int index) {
    	GeometryNode gn = (GeometryNode)parent;
      return gn.getChildAt(index);
    }

    /**
     * Returns the number of children of parent.
     */
    public int getChildCount(Object parent) {
    	GeometryNode gn = (GeometryNode)parent;
        return gn.getChildCount();
    }

    /**
     * Returns the index of child in parent.
     */
    public int getIndexOfChild(Object parent, Object child) {
    	GeometryNode gn = (GeometryNode)parent;
        return gn.getIndexOfChild((GeometryNode)child);
    }

    /**
     * Returns the root of the tree.
     */
    public Object getRoot() {
        return rootGeom;
    }

    /**
     * Returns true if node is a leaf.
     */
    public boolean isLeaf(Object node) {
    	GeometryNode gn = (GeometryNode)node;
        return gn.isLeaf();
    }

    /**
     * Removes a listener previously added with addTreeModelListener().
     */
    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.removeElement(l);
    }

    /**
     * Messaged when the user has altered the value for the item
     * identified by path to newValue.  Not used by this model.
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
        System.out.println("*** valueForPathChanged : "
                           + path + " --> " + newValue);
    }
}

abstract class GeometryNode 
{
	public static GeometryNode create(Coordinate p)
	{
		return new CoordinateNode(p);
	}
	public static GeometryNode create(Geometry geom)
	{
		if (geom instanceof GeometryCollection) return new GeometryCollectionNode((GeometryCollection) geom);
		if (geom instanceof Polygon) return new PolygonNode((Polygon) geom);
		if (geom instanceof LineString) return new LineStringNode((LineString) geom);
		if (geom instanceof LinearRing) return new LinearRingNode((LinearRing) geom);
		return null;
	}
	
	protected boolean isLeaf = false;
	protected int index = -1;
	protected String text = "";;
	protected List children = null;
	
	public GeometryNode(String text)
	{
		this.text = text;
	}
	
	public GeometryNode(Geometry geom)
	{
		this(geom, 0, null);
	}
	
	public GeometryNode(Geometry geom, int size, String tag)
	{
		text = "";
		if (tag != null && tag.length() > 0) {
			text += tag + " : ";
		}
		text += geom.getGeometryType();
		if (geom.isEmpty()) {
			isLeaf = true;
			text = text + " EMPTY";
		}
		else {
			if (size > 0) {
				text += " [ " + size + " ]";
			}
		}
	}
	
	public GeometryNode(Geometry geom, int size, int index)
	{
		this(geom, size, "[ " + index + " ]");
	}
	
	public void setIndex(int index)
	{
		this.index = index;
	}
	public boolean isLeaf() { return isLeaf; }
	
	public GeometryNode getChildAt(int index) 
	{
		if (isLeaf) return null;
		populateChildren();
		return (GeometryNode) children.get(index);
	}
	public int getChildCount()
	{
		if (isLeaf) return 0;
		populateChildren();
		return children.size();
	}
	public int getIndexOfChild(GeometryNode child)
	{
		if (isLeaf) return -1;
		populateChildren();
		return children.indexOf(child);
	}
	
	public String getText()
	{
		if (index >= 0) {
			return "[" + index + "] : " + text; 
		}
		return text;
	}
	protected void populateChildren()
	{
		if (children != null) return;
		createChildren();
	}
	private void createChildren()
	{
		children = new ArrayList();
	}
	protected void populateChildren(Coordinate[] pt)
	{
		createChildren();
		for (int i = 0; i < pt.length; i++) {
			GeometryNode node = create(pt[i]);
			node.setIndex(i);
			children.add(node);
		}
	}
}

class PolygonNode extends GeometryNode 
{
	Polygon poly;
	
	PolygonNode(Polygon poly)
	{
		super(poly, 0, null);
		this.poly = poly;
	}
	protected void populateChildren()
	{
		children = new ArrayList();
		children.add(new LinearRingNode((LinearRing) poly.getExteriorRing(), "Shell"));
		for (int i = 0; i < poly.getNumInteriorRing(); i++) {
			children.add(new LinearRingNode((LinearRing) poly.getInteriorRingN(i), "Hole " + i ));
		}
	}

}
class LineStringNode extends GeometryNode 
{
	LineString line;
	
	public LineStringNode(LineString line)
	{
		this(line, null);
	}
	public LineStringNode(LineString line, String tag)
	{
		super(line, line.getNumPoints(), tag);
		this.line = line;
	}
	protected void populateChildren()
	{
		populateChildren(line.getCoordinates());
	}
}

class LinearRingNode extends LineStringNode 
{
	public LinearRingNode(LinearRing line)
	{
		super(line);
	}
	public LinearRingNode(LinearRing ring, String tag)
	{
		super(ring, tag);
	}
}
class PointNode extends GeometryNode 
{
	Point pt;
	
	public PointNode(Point p)
	{
		super(p);
		pt = p;
	}
	protected void populateChildren()
	{
		children = new ArrayList();
		children.add(create(pt.getCoordinate()));
	}

}

class GeometryCollectionNode extends GeometryNode 
{
	GeometryCollection coll;
	
	GeometryCollectionNode(GeometryCollection coll)
	{
		super(coll, coll.getNumGeometries(), null);
		this.coll = coll;
	}
	protected void populateChildren()
	{
		children = new ArrayList();
		for (int i = 0; i < coll.getNumGeometries(); i++) {
			GeometryNode node = create(coll.getGeometryN(i));
			node.setIndex(i);
			children.add(node);
		}
	}
}

class CoordinateNode extends GeometryNode 
{
	public CoordinateNode(Coordinate coord)
	{
		super(coord.x + ", " + coord.y);
		isLeaf = true;
	}
}

