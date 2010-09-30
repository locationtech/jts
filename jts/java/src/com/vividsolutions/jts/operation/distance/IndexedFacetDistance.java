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
 * The Branch-and-Bound algorithm operates over a traversal of R-trees built
 * on the target and possibly also the query geometries.
 * <p>
 * This approach provides the following benefits:
 * <ul>
 * <li>Performance is improved due to the effects of the R-tree spatial index
 * and the short-circuiting due to the BnB approach
 * <li>The spatial index on the target geometry can be cached
 * to allow reuse in an incremental query situation.
 * </ul>
 * Using this technique can be much more performant 
 * than using {@link #getDistance(Geometry)} 
 * when one or both
 * input geometries are large, 
 * or when evaluating many distance computations against 
 * a single geometry.
 * <p>
 * This class is NOT thread-safe.
 * 
 * @author Martin Davis
 *
 */
public class IndexedFacetDistance 
{
  /**
   * Computes the distance between two geometries using
   * the indexed approach.
   * <p>
   * For geometries with many segments or points, 
   * this can be faster than using a simple distance
   * algorithm.
   * 
   * @param g1 a geometry
   * @param g2 a geometry
   * @return the distance between the two geometries
   */
  public static double distance(Geometry g1, Geometry g2)
  {
    IndexedFacetDistance dist = new IndexedFacetDistance(g1);
    return dist.getDistance(g2);
  }
  
  // 6 seems to be a good facet sequence size
  private static final int FACET_SEQUENCE_SIZE = 6;
  // Seems to be better to use a minimum node capacity
  private static final int STR_TREE_NODE_CAPACITY = 4;

  private STRtree tree1;
  
  private PriorityQueue priQ;
  private double lastComputedDistance;
  private double minimumDistanceFound;
  // storing this allows determining the nearest points
  private GeometryFacetBoundablePair minPair;
  
  /**
   * Creates a new distance-finding instance for a given target {@link Geometry}.
   * <p>
   * Distances will be computed to all facets of the input geometry.
   * The facets of the geometry are the discrete segments and points 
   * contained in its components.  
   * In the case of {@link Lineal} and {@link Puntal} inputs,
   * this is equivalent to computing the conventional distance.
   * In the case of {@link Polygonal} inputs, this is equivalent 
   * to computing the distance to the polygons boundaries. 
   * 
   * @param g1 a Geometry, which may be of any type.
   */
  public IndexedFacetDistance(Geometry g1) {
    tree1 = computeFacetSequenceTree(g1);
  }

  /**
   * Computes the distance from the base geometry to 
   * the given geometry.
   *  
   * @param g the geometry to compute the distance to
   * 
   * @return the computed distance
   */
  public double getDistance(Geometry g)
  {
  	return getDistanceWithin(g, Double.MAX_VALUE);
  }
  
  /**
   * Computes the distance from the base geometry to 
   * the given geometry, up to and including a given 
   * maximum distance.
   * 
   * @param g the geometry to compute the distance to
   * @param maximumDistance the maximum distance to compute.
   * 
   * @return the computed distance,
   *    or <tt>maximumDistance</tt> if the true distance is determined to be greater
   */
  public double getDistanceWithin(Geometry g, double maximumDistance)
  {
    STRtree tree2 = computeFacetSequenceTree(g);
    double dist = findMinDistance(tree1, tree2, maximumDistance);
    return dist;
  }
  
  /**
   * Computes the distance from the base geometry to 
   * the given coordinate.
   * 
   * @param p the coordinate to compute the distance to
   * @return the computed distance
   */
  public double getDistance(Coordinate p)
  {
  	return getDistanceWithin(p, Double.MAX_VALUE);
  }
  
  /**
   * Computes the distance from the base geometry to 
   * the given coordinate, up to and including a given 
   * maximum distance.
   * 
   * @param p the coordinate to compute the distance to
   * @param maximumDistance the maximum distance to compute.
   * 
   * @return the computed distance,
   *    or <tt>maximumDistance</tt> if the true distance is determined to be greater
   */
  public double getDistanceWithin(Coordinate p, double maximumDistance)
  {
    findMinDistance(p, maximumDistance);
    return minimumDistanceFound;
  }
  
  /**
   * Tests whether the base geometry lies within
   * a specified distance of the given geometry.
   * 
   * @param g the geomtry to test
   * @param maximumDistance the maximum distance to test
   * @return true if the geometry lies with the specified distance
   */
  public boolean isWithinDistance(Geometry g, double maximumDistance)
  {
    STRtree tree2 = computeFacetSequenceTree(g);
    findMinDistance(tree1, tree2, maximumDistance);
    if (lastComputedDistance > minimumDistanceFound)
      return false;
    return true;
  }
  
  /**
   * Tests whether the base geometry lies within
   * a specified distance of the given Coordinate.
   * 
   * @param p the coordinate to test
   * @param maximumDistance the maximum distance to test
   * @return true if the coordinate lies with the specified distance
   */
  public boolean isWithinDistance(Coordinate p, double maximumDistance)
  {
    findMinDistance(p, maximumDistance);
    if (lastComputedDistance > minimumDistanceFound)
    	return false;
    return true;
  }
  
  private void findMinDistance(Coordinate p, double maxDistance)
  {
    GeometryFacetSequence fs = new GeometryFacetSequence(
        new CoordinateArraySequence(new Coordinate[] { p }), 0);
    Boundable bpt = new ItemBoundable(fs.getEnvelope(), fs);
    findMinDistance(tree1.getRoot(), bpt, maxDistance);
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
  
  private double findMinDistance(STRtree tree1, STRtree tree2, double maxDistance) 
  {
    // initialize queue
    Boundable b1 = tree1.getRoot();
    Boundable b2 = tree2.getRoot();
    return findMinDistance(b1, b2, maxDistance);
  }
  
  private double findMinDistance(Boundable b1, Boundable b2, double maxDistance) 
  {
    // initialize internal structures
    priQ = new PriorityQueue();
    minimumDistanceFound = maxDistance;

    // initialize queue
    GeometryFacetBoundablePair bndPair = new GeometryFacetBoundablePair(b1, b2);
    priQ.add(bndPair);
   
    runBranchAndBound();
    // testing - force recomputation of distance
    //double testdist = minPair.distance();
    
    return minimumDistanceFound;
  }
  
  private void runBranchAndBound()
  {
    while (! priQ.isEmpty() && minimumDistanceFound > 0.0) {
      // pop head of queue and expand one side of pair
      GeometryFacetBoundablePair bndPair = (GeometryFacetBoundablePair) priQ.poll();
      lastComputedDistance = bndPair.getMinimumDistance();
      
      /**
       * If the distance for the first node in the queue
       * is >= the current minimum distance, all other nodes
       * in the queue must also have a greater distance.
       * So the current minDistance must be the true minimum,
       * and we are done.
       */
      if (lastComputedDistance >= minimumDistanceFound) return;  

      /**
       * If the pair is a leaf (e.g. both members are facets)
       * update the minimum distance to reflect their distance. 
       */
      if (bndPair.isLeaf()) {
        // assert: dist < minDistance
        minimumDistanceFound = lastComputedDistance;
        minPair = bndPair;
      }
      else {
        // testing - does allowing a tolerance improve speed?
        // Ans: by only about 10% - not enough to matter
        /*
        double maxDist = bndPair.getMaximumDistance();
        if (maxDist * .99 < lastComputedDistance) 
          return;
        //*/

        /**
         * Otherwise, expand one side of the pair, and 
         * insert the new pairs into the queue
         */
        bndPair.expandToQueue(priQ, minimumDistanceFound);
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
      if (bp.getMinimumDistance() < minimumDistanceFound)
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


