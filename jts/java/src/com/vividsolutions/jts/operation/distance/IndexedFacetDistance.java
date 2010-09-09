package com.vividsolutions.jts.operation.distance;

import java.util.*;
//import java.util.PriorityQueue;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.geom.util.LinearComponentExtracter;
import com.vividsolutions.jts.index.strtree.*;
import com.vividsolutions.jts.util.PriorityQueue;

/**
 * Computes the distance between the facets (segments and vertices) 
 * of two {@link Geometry}s
 * using a Branch-and-Bound algorithm.
 * The BnB algorithm operates over a traversal of R-trees built
 * on the target and possibly also the query geometries.
 * <p>
 * This approach provides the following benefits:
 * <ul>
 * <li>Performance is improved due to the effects of the R-tree spatial index
 * and the short-circuiting due to the BnB approach
 * <li>The spatial index on the target geometry can be cached
 * to allow reuse in an incremental query situation.
 * </ul>
 * 
 * @author Martin Davis
 *
 */
public class IndexedFacetDistance 
{
  public static double distance(Geometry g1, Geometry g2)
  {
    IndexedFacetDistance dist = new IndexedFacetDistance(g1);
    return dist.getDistance(g2);
  }
  
  public static double distance(Geometry g1, Coordinate p)
  {
    IndexedFacetDistance dist = new IndexedFacetDistance(g1);
    return dist.getDistance(p);
  }
  
  // 6 seems to be a good facet sequence size
  private static final int FACET_SEQUENCE_SIZE = 6;
  // Seesm to be better to use a minimum node capacity
  private static final int STR_TREE_NODE_CAPACITY = 4;

  private STRtree tree1;
  
  private PriorityQueue priQ;
  private double minDistance;
  // storing this allows determining the nearest points
  private GeometryFacetBoundablePair minPair;
  
  public IndexedFacetDistance(Geometry g1) {
    tree1 = computeFacetSequenceTree(g1);
  }

  public double getDistance(Geometry g2)
  {
    STRtree tree2 = computeFacetSequenceTree(g2);
    double dist = findMinDistance(tree1, tree2);
    return dist;
  }
  
  /**
   * Computes the distance from the base geometry to 
   * the given coordinate.
   * This is more efficient than using {@link #getDistance(Geometry)}.
   * 
   * @param p the coordinate to compute the distance to
   * @return the distance to the coordinate
   */
  public double getDistance(Coordinate p)
  {
    GeometryFacetSequence fs = new GeometryFacetSequence(
        new CoordinateArraySequence(new Coordinate[] { p }), 0);
    Boundable bpt = new ItemBoundable(fs.getEnvelope(), fs);
    double dist = findMinDistance(tree1.getRoot(), bpt);
    return dist;
  }
  
  private STRtree computeFacetSequenceTree(Geometry g)
  {
    STRtree tree = new STRtree(STR_TREE_NODE_CAPACITY);
    List sections = computeFacetSequences(g);
    for (Iterator i = sections.iterator(); i.hasNext(); ) {
      GeometryFacetSequence section = (GeometryFacetSequence) i.next();
      tree.insert(section.getEnvelope(), section);
    }
    tree.build();
    return tree;
  }
  
  private double findMinDistance(STRtree tree1, STRtree tree2) 
  {
    // initialize queue
    Boundable b1 = tree1.getRoot();
    Boundable b2 = tree2.getRoot();
    return findMinDistance(b1, b2);
  }
  
  private double findMinDistance(Boundable b1, Boundable b2) 
  {
    // initialize internal structures
    priQ = new PriorityQueue();
    minDistance = Double.MAX_VALUE;

    // initialize queue
    GeometryFacetBoundablePair bndPair = new GeometryFacetBoundablePair(b1, b2);
    priQ.add(bndPair);
   
    runBranchAndBound();
    // testing - force recomputation of distance
    //double testdist = minPair.distance();
    
    return minDistance;
  }
  
  private void runBranchAndBound()
  {
    while (! priQ.isEmpty() && minDistance > 0.0) {
      // pop head of queue and expand one side of pair
      GeometryFacetBoundablePair bndPair = (GeometryFacetBoundablePair) priQ.poll();
      double dist = bndPair.getDistance();
      
      /**
       * If the distance for the first node in the queue
       * is >= the current minimum distance, all other nodes
       * in the queue must also have a greater distance.
       * So the current minDistance must be the true minimum,
       * and we are done.
       */
      if (dist >= minDistance) return;  
      
      
      /**
       * If the pair is a leaf (e.g. both members are facets)
       * update the minimum distance to reflect their distance. 
       */
      if (bndPair.isLeaf()) {
        // assert: dist < minDistance
        minDistance = dist;
        minPair = bndPair;

        // testing - does allowing a tolerance improve speed?
        // Ans: not by enough to matter
        /*
        double maxDist = bndPair.getMaximumDistance();
        if (maxDist * .99 < minDistance) 
          return;
          */
      }
      else {
        /**
         * Otherwise, expand one side of the pair, and 
         * insert the new pairs into the queue
         */
        //addToQueue(bndPair.expand());
        bndPair.expandToQueue(priQ, minDistance);
      }
    }
  }

  /**
   * Add the BoundablePairs from the collection
   * into the queue which have their distance 
   * less than the current minimum distance.
   * 
   * @param boundablePairs the pairs to scan
   */
  private void addToQueue(Collection boundablePairs)
  {
    for (Iterator i = boundablePairs.iterator(); i.hasNext(); ) {
      GeometryFacetBoundablePair bp = (GeometryFacetBoundablePair) i.next();
      if (bp.getDistance() < minDistance)
        priQ.add(bp);
    }
//    System.out.println("PriQ size = " + priQ.size());
  }
    
  /**
   * Creates facet sequences
   * @param g
   * @return List<GeometryFacetSequence>
   */
  private List computeFacetSequences(Geometry g)
  {
    List sections = new ArrayList();
    
    List lines = LinearComponentExtracter.getLines(g);

    for (Iterator ii = lines.iterator(); ii.hasNext();) {
      LineString line = (LineString) ii.next();
      addFacetSequences(line, sections);
    }
    return sections;
  }
  
  private void addFacetSequences(LineString line, List sections) {
    CoordinateSequence pts = line.getCoordinateSequence();
    int i = 0;
    int size = pts.size();
    while (i <= size - 1) {
      int end = i + FACET_SEQUENCE_SIZE + 1;
      // if only one point remains after this section, include it in this section
      if (end >= size - 1)
        end = size;
      GeometryFacetSequence sect = new GeometryFacetSequence(pts, i, end);
      sections.add(sect);
      i = i + FACET_SEQUENCE_SIZE;
    }
  }
}


