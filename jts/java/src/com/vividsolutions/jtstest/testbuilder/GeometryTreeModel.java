package com.vividsolutions.jtstest.testbuilder;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryTreeModel implements TreeModel
{
  private Vector<TreeModelListener> treeModelListeners = new Vector<TreeModelListener>();

  private GeometricObjectNode rootGeom;

  public GeometryTreeModel(Geometry geom)
  {
    rootGeom = GeometryNode.create(geom);
  }

  // ////////////// TreeModel interface implementation ///////////////////////

  /**
   * Adds a listener for the TreeModelEvent posted after the tree changes.
   */
  public void addTreeModelListener(TreeModelListener l)
  {
    treeModelListeners.addElement(l);
  }

  /**
   * Returns the child of parent at index index in the parent's child array.
   */
  public Object getChild(Object parent, int index)
  {
    GeometricObjectNode gn = (GeometricObjectNode) parent;
    return gn.getChildAt(index);
  }

  /**
   * Returns the number of children of parent.
   */
  public int getChildCount(Object parent)
  {
    GeometricObjectNode gn = (GeometricObjectNode) parent;
    return gn.getChildCount();
  }

  /**
   * Returns the index of child in parent.
   */
  public int getIndexOfChild(Object parent, Object child)
  {
    GeometricObjectNode gn = (GeometricObjectNode) parent;
    return gn.getIndexOfChild((GeometricObjectNode) child);
  }

  /**
   * Returns the root of the tree.
   */
  public Object getRoot()
  {
    return rootGeom;
  }

  /**
   * Returns true if node is a leaf.
   */
  public boolean isLeaf(Object node)
  {
    GeometricObjectNode gn = (GeometricObjectNode) node;
    return gn.isLeaf();
  }

  /**
   * Removes a listener previously added with addTreeModelListener().
   */
  public void removeTreeModelListener(TreeModelListener l)
  {
    treeModelListeners.removeElement(l);
  }

  /**
   * Messaged when the user has altered the value for the item identified by
   * path to newValue. Not used by this model.
   */
  public void valueForPathChanged(TreePath path, Object newValue)
  {
    System.out
        .println("*** valueForPathChanged : " + path + " --> " + newValue);
  }
}

abstract class GeometricObjectNode
{
  protected static String indexString(int index)
  {
    return "[" + index + "]";
  }

  protected static String sizeString(int size)
  {
    return "(" + size + ")";
  }

  protected int index = -1;

  protected String text = "";;

  public GeometricObjectNode(String text)
  {
    this.text = text;
  }

  public void setIndex(int index)
  {
    this.index = index;
  }

  public String getText()
  {
    if (index >= 0) {
      return indexString(index) + " : " + text;
    }
    return text;
  }
  
  public abstract Geometry getGeometry();

  public abstract boolean isLeaf();

  public abstract GeometricObjectNode getChildAt(int index);

  public abstract int getChildCount();

  public abstract int getIndexOfChild(GeometricObjectNode child);

}

abstract class GeometryNode extends GeometricObjectNode
{
  public static GeometryNode create(Geometry geom)
  {
    if (geom instanceof GeometryCollection)
      return new GeometryCollectionNode((GeometryCollection) geom);
    if (geom instanceof Polygon)
      return new PolygonNode((Polygon) geom);
    if (geom instanceof LineString)
      return new LineStringNode((LineString) geom);
    if (geom instanceof LinearRing)
      return new LinearRingNode((LinearRing) geom);
    if (geom instanceof Point)
      return new PointNode((Point) geom);
    return null;
  }

  private boolean isLeaf;
  protected List<GeometricObjectNode> children = null;

  public GeometryNode(Geometry geom)
  {
    this(geom, 0, null);
  }

  public GeometryNode(Geometry geom, int size, String tag)
  {
    super(geometryText(geom, size, tag));
    if (geom.isEmpty()) {
      isLeaf = true;
    }
  }

  private static String geometryText(Geometry geom, int size, String tag)
  {
    StringBuilder buf = new StringBuilder();
    if (tag != null && tag.length() > 0) {
      buf.append(tag + " : ");
    }
    buf.append(geom.getGeometryType());
    if (geom.isEmpty()) {
      buf.append(" EMPTY");
    }
    else {
      if (size > 0) {
        buf.append(" " + sizeString(size));
      }
    }
    
    buf.append(" --     Len: " + geom.getLength());
    if (geom.getDimension() >= 2) 
      buf.append("      Area: " + geom.getArea());
    
    return buf.toString();
  }
  public boolean isLeaf()
  {
    return isLeaf;
  }

  public GeometricObjectNode getChildAt(int index)
  {
    if (isLeaf)
      return null;
    populateChildren();
    return (GeometricObjectNode) children.get(index);
  }

  public int getChildCount()
  {
    if (isLeaf)
      return 0;
    populateChildren();
    return children.size();
  }

  public int getIndexOfChild(GeometricObjectNode child)
  {
    if (isLeaf)
      return -1;
    populateChildren();
    return children.indexOf(child);
  }

  /**
   * Lazily creates child nodes
   */
  private void populateChildren()
  {
    // already initialized
    if (children != null)
      return;

    children = new ArrayList<GeometricObjectNode>();
    fillChildren();
  }

  protected abstract void fillChildren();
}


class PolygonNode extends GeometryNode
{
  Polygon poly;

  PolygonNode(Polygon poly)
  {
    super(poly, poly.getNumPoints(), null);
    this.poly = poly;
  }

  public Geometry getGeometry()
  {
    return poly;
  }

  protected void fillChildren()
  {
    children.add(new LinearRingNode((LinearRing) poly.getExteriorRing(),
        "Shell"));
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      children.add(new LinearRingNode((LinearRing) poly.getInteriorRingN(i),
          "Hole " + i));
    }
  }

}

class LineStringNode extends GeometryNode
{
  private LineString line;

  public LineStringNode(LineString line)
  {
    this(line, null);
  }

  public LineStringNode(LineString line, String tag)
  {
    super(line, line.getNumPoints(), tag);
    this.line = line;
  }

  public Geometry getGeometry()
  {
    return line;
  }

  protected void fillChildren()
  {
    populateChildren(line.getCoordinates());
  }

  private void populateChildren(Coordinate[] pt)
  {
    Envelope env = line.getEnvelopeInternal();
    
    
    for (int i = 0; i < pt.length; i++) {
      double dist = Double.NaN;
      if (i < pt.length - 1) dist = pt[i].distance(pt[i + 1]);
      GeometricObjectNode node = CoordinateNode.create(pt[i], i, dist);
      children.add(node);
    }
  }
}

class LinearRingNode extends LineStringNode
{
  public LinearRingNode(LinearRing ring)
  {
    super(ring);
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

  public Geometry getGeometry()
  {
    return pt;
  }

  protected void fillChildren()
  {
    children.add(CoordinateNode.create(pt.getCoordinate()));
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

  public Geometry getGeometry()
  {
    return coll;
  }

  protected void fillChildren()
  {
    for (int i = 0; i < coll.getNumGeometries(); i++) {
      GeometryNode node = create(coll.getGeometryN(i));
      node.setIndex(i);
      children.add(node);
    }
  }
}

/**
 * Coordinate is the only leaf node now, but could be 
 * refactored into a LeafNode class.
 * 
 * @author Martin Davis
 *
 */
class CoordinateNode extends GeometricObjectNode
{
  public static CoordinateNode create(Coordinate p)
  {
    return new CoordinateNode(p);
  }

  public static CoordinateNode create(Coordinate p, int i, double distPrev)
  {
    return new CoordinateNode(p, i, distPrev);
  }

  private static DecimalFormat fmt = new DecimalFormat("0.#################", new DecimalFormatSymbols());
  
  private static String label(Coordinate coord, int i, double distPrev)
  {
    String lbl = fmt.format(coord.x) + "   " + fmt.format(coord.y);
    if (! Double.isNaN(distPrev)) {
      lbl += "  --  dist: " + distPrev;
    }
    return lbl;
  }
  

  Coordinate coord;

  public CoordinateNode(Coordinate coord)
  {
    this(coord, 0, Double.NaN);
  }

  public CoordinateNode(Coordinate coord, int i, double distPrev)
  {
    super(label(coord, i, distPrev));
    this.coord = coord;
    this.index = i;
  }

  public Geometry getGeometry()
  {
    GeometryFactory geomFact = new GeometryFactory();
    return geomFact.createPoint(coord);
  }

  @Override
  public boolean isLeaf()
  {
    return true;
  }

  @Override
  public GeometricObjectNode getChildAt(int index)
  {
    throw new IllegalStateException("should not be here");
  }

  @Override
  public int getChildCount()
  {
    return 0;
  }

  @Override
  public int getIndexOfChild(GeometricObjectNode child)
  {
    throw new IllegalStateException("should not be here");
  }
}
