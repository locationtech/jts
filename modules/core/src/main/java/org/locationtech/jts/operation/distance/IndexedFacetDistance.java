/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.operation.distance;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Lineal;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.Puntal;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.index.strtree.ItemBoundable;
import org.locationtech.jts.index.strtree.ItemDistance;
import org.locationtech.jts.index.strtree.STRtree;

/**
 * Computes the distance between the facets (segments and vertices) 
 * of two {@link Geometry}s
 * using a Branch-and-Bound algorithm.
 * The Branch-and-Bound algorithm operates over a 
 * traversal of R-trees built
 * on the target and the query geometries.
 * <p>
 * This approach provides the following benefits:
 * <ul>
 * <li>Performance is dramatically improved due to the use of the 
 * R-tree index
 * and the pruning due to the Branch-and-Bound approach
 * <li>The spatial index on the target geometry is cached
 * which allow reuse in an repeated query situation.
 * </ul>
 * Using this technique is usually much more performant 
 * than using the brute-force {@link Geometry#distance(Geometry)} 
 * when one or both input geometries are large, 
 * or when evaluating many distance computations against 
 * a single geometry.
 * <p>
 * This class is thread-safe.
 * 
 * @author Martin Davis
 *
 */
public class IndexedFacetDistance 
{
  private static final FacetSequenceDistance FACET_SEQ_DIST = new FacetSequenceDistance();

  /**
   * Computes the distance between facets of two geometries.
   * <p>
   * For geometries with many segments or points, 
   * this can be faster than using a simple distance
   * algorithm.
   * 
   * @param g1 a geometry
   * @param g2 a geometry
   * @return the distance between facets of the geometries
   */
  public static double distance(Geometry g1, Geometry g2)
  {
    IndexedFacetDistance dist = new IndexedFacetDistance(g1);
    return dist.distance(g2);
  }
  
  /**
   * Tests whether the facets of two geometries lie within a given distance.
   * 
   * @param g1 a geometry
   * @param g2 a geometry
   * @param distance the distance limit
   * @return true if two facets lie with the given distance
   */
  public static boolean isWithinDistance(Geometry g1, Geometry g2, double distance) {
    IndexedFacetDistance dist = new IndexedFacetDistance(g1);
    return dist.isWithinDistance(g2, distance);
  }
  
  /**
   * Computes the nearest points of the facets of two geometries.   
   * 
   * @param g1 a geometry
   * @param g2 a geometry
   * @return the nearest points on the facets of geometry g1 and g2
   */
  public static Coordinate[] nearestPoints(Geometry g1, Geometry g2) {
    IndexedFacetDistance dist = new IndexedFacetDistance(g1);
    return dist.nearestPoints(g2);
  }
  
  private STRtree cachedTree;
  private Geometry baseGeometry;
  
  /**
   * Creates a new distance-finding instance for a given target {@link Geometry}.
   * <p>
   * Distances will be computed to all facets of the input geometry.
   * The facets of the geometry are the discrete segments and points 
   * contained in its components.  
   * In the case of {@link Lineal} and {@link Puntal} inputs,
   * this is equivalent to computing the conventional distance.
   * In the case of {@link Polygonal} inputs, this is equivalent 
   * to computing the distance to the polygon boundaries. 
   * 
   * @param geom a Geometry, which may be of any type.
   */
  public IndexedFacetDistance(Geometry geom) {
    this.baseGeometry = geom;
    cachedTree = FacetSequenceTreeBuilder.build(geom);
  }

  /**
   * Computes the distance from the base geometry to 
   * the given geometry.
   *  
   * @param g the geometry to compute the distance to
   * 
   * @return the computed distance
   */
  public double distance(Geometry g)
  {
    Object[] obj = nearestFacets(g);
    FacetSequence fs1 = (FacetSequence) obj[0];
    FacetSequence fs2 = (FacetSequence) obj[1];
    return fs1.distance(fs2);
  }

  private Object[] nearestFacets(Geometry g) {
    STRtree tree2 = FacetSequenceTreeBuilder.build(g);
    Object[] obj = cachedTree.nearestNeighbour(tree2, 
        FACET_SEQ_DIST);
    return obj;
  }
  
  /**
   * Computes the nearest points on the base geometry
   * and another geometry.
   * 
   * @param g the geometry to compute the nearest location to
   * @return the nearest points on the base and the argument geometry
   */
  public Coordinate[] nearestPoints(Geometry g)
  {
    Object[] obj = nearestFacets(g);
    FacetSequence fs1 = (FacetSequence) obj[0];
    FacetSequence fs2 = (FacetSequence) obj[1];
    return fs1.nearestLocations(fs2);
  }

  /**
   * Computes the nearest points on the base geometry
   * and a point.
   * 
   * @param p the point coordinate
   * @return the nearest point on the base geometry and the point
   */
  public Coordinate nearestPoint(Coordinate p)
  {
    CoordinateSequence seq = new CoordinateArraySequence(new Coordinate[] { p });
    FacetSequence fs2 = new FacetSequence(seq, 0);
    Object nearest = cachedTree.nearestNeighbour(fs2.getEnvelope(), fs2, FACET_SEQ_DIST);
    FacetSequence fs1 = (FacetSequence) nearest;
    return fs1.nearestLocations(fs2)[0];
  }
  
  public double distance(Coordinate p) {
    return p.distance(nearestPoint(p));
  }
  
  public double distance(Coordinate p0, Coordinate p1)
  {
    CoordinateSequence seq = new CoordinateArraySequence(new Coordinate[] { p0, p1 });
    FacetSequence fs2 = new FacetSequence(seq, 0, 2);
    Object nearest = cachedTree.nearestNeighbour(fs2.getEnvelope(), fs2, FACET_SEQ_DIST);
    FacetSequence fs1 = (FacetSequence) nearest;
    Coordinate[] loc = fs1.nearestLocations(fs2);
    return loc[0].distance(loc[1]);
  }
  
  /**
   * Tests whether the base geometry lies within
   * a specified distance of the given geometry.
   * 
   * @param g the geometry to test
   * @param maxDistance the maximum distance to test
   * @return true if the geometry lies with the specified distance
   */
  public boolean isWithinDistance(Geometry g, double maxDistance) {
    // short-ciruit check
    double envDist = baseGeometry.getEnvelopeInternal().distance(g.getEnvelopeInternal());
    if (envDist > maxDistance)
      return false;

    STRtree tree2 = FacetSequenceTreeBuilder.build(g);
    return cachedTree.isWithinDistance(tree2, 
        FACET_SEQ_DIST, maxDistance);
  }  
 
  private static class FacetSequenceDistance
  implements ItemDistance
  {
    public double distance(ItemBoundable item1, ItemBoundable item2) {
      FacetSequence fs1 = (FacetSequence) item1.getItem();
      FacetSequence fs2 = (FacetSequence) item2.getItem();
      return fs1.distance(fs2);    
    }
  }

}


