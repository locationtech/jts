package com.vividsolutions.jts.operation.distance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.AbstractNode;
import com.vividsolutions.jts.index.strtree.Boundable;
import com.vividsolutions.jts.index.strtree.ItemBoundable;
import com.vividsolutions.jts.util.PriorityQueue;

/**
 * A pair of {@link Boundable}s, one from each
 * indexed geometry, whose leaf items are 
 * {@link FacetSequence}s.
 * Used to compute the distance between the members,
 * and to expand a member relative to the other
 * in order to produce new branches of the evaluation tree.
 * Provides an ordering based on the distance between the members.
 * 
 * @author Martin Davis
 *
 */
class FacetBoundablePair
  implements Comparable
{
  private Boundable boundable1;
  private Boundable boundable2;
  private double minDistance;
  //private double maxDistance = -1.0;
  
  public FacetBoundablePair(Boundable node1, Boundable node2)
  {
    this.boundable1 = node1;
    this.boundable2 = node2;
    minDistance = distance();
  }
  
  /**
   * Computes the distance between the {@link Boundables} in this pair.
   * @return
   */
  private double distance()
  {
    // if items, compute exact distance
    if (boundable1 instanceof ItemBoundable && boundable2 instanceof ItemBoundable) {
      FacetSequence gf1 = getLeafItem(boundable1);
      FacetSequence gf2 = getLeafItem(boundable2);
      return gf1.distance(gf2);
    }
    // otherwise compute distance between bounds of items
    return ((Envelope) boundable1.getBounds()).distance(
        ((Envelope) boundable2.getBounds()));
  }

  private static FacetSequence getLeafItem(Boundable b)
  {
    return (FacetSequence) ((ItemBoundable) b).getItem();
  }

  /*
  public double getMaximumDistance()
  {
  	if (maxDistance < 0.0)
  		maxDistance = maxDistance();
  	return maxDistance;
  }
  */
  
  private double maxDistance()
  {
    return maximumDistance( 
        (Envelope) boundable1.getBounds(),
        (Envelope) boundable2.getBounds());      	
  }
  
  private static double maximumDistance(Envelope env1, Envelope env2)
  {
  	double minx = Math.min(env1.getMinX(), env2.getMinX());
  	double miny = Math.min(env1.getMinY(), env2.getMinY());
  	double maxx = Math.max(env1.getMaxX(), env2.getMaxX());
  	double maxy = Math.max(env1.getMaxY(), env2.getMaxY());
    Coordinate min = new Coordinate(minx, miny);
    Coordinate max = new Coordinate(maxx, maxy);
    return min.distance(max);
  }
  
  /**
   * Gets the minimum possible distance between the Boundables in
   * this pair. 
   * If the members are both items, this will be the
   * exact distance between them.
   * Otherwise, this distance will be a lower bound on 
   * the distances between the items in the members.
   * 
   * @return the exact or lower bound distance for this pair
   */
  public double getMinimumDistance() { return minDistance; }
  
  /**
   * Compares two pairs based on their minimum distances
   */
  public int compareTo(Object o)
  {
    FacetBoundablePair nd = (FacetBoundablePair) o;
    if (minDistance < nd.minDistance) return -1;
    if (minDistance > nd.minDistance) return 1;
    return 0;
  }

  /**
   * Tests if both elements of the pair are leaf nodes
   * 
   * @return true if both pair elements are leaf nodes
   */
  public boolean isLeaf()
  {
    return ! (isComposite(boundable1) || isComposite(boundable2));
  }
  
  public static boolean isComposite(Object item)
  {
    return (item instanceof AbstractNode); 
  }
  
  private static double area(Boundable b)
  {
    return ((Envelope) b.getBounds()).getArea();
  }
  
  /**
   * For a pair which is not a leaf 
   * (i.e. has at least one composite boundable)
   * computes a list of new pairs 
   * from the expansion of the larger boundable.
   * 
   * @return a List of new pairs
   */
  public List expand()
  {
    boolean isComp1 = isComposite(boundable1);
    boolean isComp2 = isComposite(boundable2);
    
    /**
     * HEURISTIC: If both boundable are composite,
     * choose the one with largest area to expand.
     * Otherwise, simply expand whichever is composite.
     */
    if (isComp1 && isComp2) {
      if (area(boundable1) > area(boundable2)) {
        return expand(boundable1, boundable2);
      }
      else {
        return expand(boundable2, boundable1);        
      }
    }
    else if (isComp1) {
      return expand(boundable1, boundable2);
    }
    else if (isComp2) {
      return expand(boundable2, boundable1);
    }
    
    throw new IllegalArgumentException("neither boundable is composite");
  }
  
  private List expand(Boundable bndComposite, Boundable bndOther)
  {
    List expansion = new ArrayList();
    List children = ((AbstractNode) bndComposite).getChildBoundables();
    for (Iterator i = children.iterator(); i.hasNext(); ) {
      Boundable child = (Boundable) i.next();
      expansion.add(new FacetBoundablePair(child, bndOther));
    }
    return expansion;
  }
  
  /**
   * For a pair which is not a leaf 
   * (i.e. has at least one composite boundable)
   * computes a list of new pairs 
   * from the expansion of the larger boundable.
   * 
   * @return a List of new pairs
   */
  public void expandToQueue(PriorityQueue priQ, double minDistance)
  {
    boolean isComp1 = isComposite(boundable1);
    boolean isComp2 = isComposite(boundable2);
    
    /**
     * HEURISTIC: If both boundable are composite,
     * choose the one with largest area to expand.
     * Otherwise, simply expand whichever is composite.
     */
    if (isComp1 && isComp2) {
      if (area(boundable1) > area(boundable2)) {
        expand(boundable1, boundable2, priQ, minDistance);
        return;
      }
      else {
        expand(boundable2, boundable1, priQ, minDistance);
        return;
      }
    }
    else if (isComp1) {
      expand(boundable1, boundable2, priQ, minDistance);
      return;
    }
    else if (isComp2) {
      expand(boundable2, boundable1, priQ, minDistance);
      return;
    }
    
    throw new IllegalArgumentException("neither boundable is composite");
  }
  
  private void expand(Boundable bndComposite, Boundable bndOther,
      PriorityQueue priQ, double minDistance)
  {
    List children = ((AbstractNode) bndComposite).getChildBoundables();
    for (Iterator i = children.iterator(); i.hasNext(); ) {
      Boundable child = (Boundable) i.next();
      FacetBoundablePair bp = new FacetBoundablePair(child, bndOther);
      // only add to queue if this pair might contain the closest points
      // MD - it's actually faster to construct the object rather than called distance(child, bndOther)!
      if (bp.getMinimumDistance() < minDistance) {
        priQ.add(bp);
      }
    }
  }
}
