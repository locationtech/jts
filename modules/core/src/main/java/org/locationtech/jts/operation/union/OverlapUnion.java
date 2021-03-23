/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.operation.union;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.geom.util.GeometryCombiner;

/**
 * Unions MultiPolygons efficiently by
 * using full topological union only for polygons which may overlap,
 * and combining with the remaining polygons.
 * Polygons which may overlap are those which intersect the common extent of the inputs.
 * Polygons wholly outside this extent must be disjoint to the computed union.
 * They can thus be simply combined with the union result,
 * which is much more performant.
 * (There is one caveat to this, which is discussed below).
 * <p>
 * This situation is likely to occur during cascaded polygon union,
 * since the partitioning of polygons is done heuristically
 * and thus may group disjoint polygons which can lie far apart.
 * It may also occur in real world data which contains many disjoint polygons
 * (e.g. polygons representing parcels on different street blocks).
 * 
 * <h2>Algorithm</h2>
 * The overlap region is determined as the common envelope of intersection.
 * The input polygons are partitioned into two sets:
 * <ul>
 * <li>Overlapping: Polygons which intersect the overlap region, and thus potentially overlap each other
 * <li>Disjoint: Polygons which are disjoint from (lie wholly outside) the overlap region
 * </ul>
 * The Overlapping set is fully unioned, and then combined with the Disjoint set.
 * Performing a simple combine works because 
 * the disjoint polygons do not interact with each
 * other (since the inputs are valid MultiPolygons).
 * They also do not interact with the Overlapping polygons, 
 * since they are outside their envelope.
 * 
 * <h2>Discussion</h2>
 * In general the Overlapping set of polygons will 
 * extend beyond the overlap envelope.  This means that the union result
 * will extend beyond the overlap region.
 * There is a small chance that the topological 
 * union of the overlap region will shift the result linework enough
 * that the result geometry intersects one of the Disjoint geometries.
 * This situation is detected and if it occurs 
 * is remedied by falling back to performing a full union of the original inputs.
 * Detection is done by a fairly efficient comparison of edge segments which
 * extend beyond the overlap region.  If any segments have changed
 * then there is a risk of introduced intersections, and full union is performed.
 * <p>
 * This situation has not been observed in JTS using floating precision, 
 * but it could happen due to snapping.  It has been observed 
 * in other APIs (e.g. GEOS) due to more aggressive snapping.
 * It is more likely to happen if a Snap-Rounding overlay is used.
 * <p>
 * <b>NOTE: Test has shown that using this heuristic impairs performance.
 * It has been removed from use.</b>
 * 
 * 
 * @author mbdavis
 * 
 * @deprecated due to impairing performance
 *
 */
public class OverlapUnion 
{
  /**
   * Union a pair of geometries,
   * using the more performant overlap union algorithm if possible.
   * 
   * @param g0 a geometry to union
   * @param g1 a geometry to union
   * @param unionFun 
   * @return the union of the inputs
   */
	public static Geometry union(Geometry g0, Geometry g1, UnionStrategy unionFun)
	{
		OverlapUnion union = new OverlapUnion(g0, g1, unionFun);
		return union.union();
	}
	
	private GeometryFactory geomFactory;
	
	private Geometry g0;
	private Geometry g1;

  private boolean isUnionSafe;

  private UnionStrategy unionFun;

	
  /**
   * Creates a new instance for unioning the given geometries.
   * 
   * @param g0 a geometry to union
   * @param g1 a geometry to union
   */
	public OverlapUnion(Geometry g0, Geometry g1)
	{
		this(g0, g1, CascadedPolygonUnion.CLASSIC_UNION);
	}
	
	public OverlapUnion(Geometry g0, Geometry g1, UnionStrategy unionFun) {
    this.g0 = g0;
    this.g1 = g1;
    geomFactory = g0.getFactory();
    this.unionFun = unionFun;
  }

  /**
   * Unions the input geometries,
   * using the more performant overlap union algorithm if possible.	 
   * 
   * @return the union of the inputs
	 */
	public Geometry union()
	{
    Envelope overlapEnv = overlapEnvelope(g0,  g1);
    
    /**
     * If no overlap, can just combine the geometries
     */
    if (overlapEnv.isNull()) {
      Geometry g0Copy = g0.copy();
      Geometry g1Copy = g1.copy();
      return GeometryCombiner.combine(g0Copy, g1Copy);
    }
    
    List<Geometry> disjointPolys = new ArrayList<Geometry>();
    
    Geometry g0Overlap = extractByEnvelope(overlapEnv, g0, disjointPolys);
    Geometry g1Overlap = extractByEnvelope(overlapEnv, g1, disjointPolys);
    
//    System.out.println("# geoms in common: " + intersectingPolys.size());
    Geometry unionGeom = unionFull(g0Overlap, g1Overlap); 
    
    Geometry result = null;
    isUnionSafe = isBorderSegmentsSame(unionGeom, overlapEnv);
    if (! isUnionSafe) {
      // overlap union changed border segments... need to do full union
      //System.out.println("OverlapUnion: Falling back to full union");
      result = unionFull(g0, g1);
    }
    else {
      //System.out.println("OverlapUnion: fast path");
      result = combine(unionGeom, disjointPolys);
    }
    return result;
	}

	/**
	 * Allows checking whether the optimized
	 * or full union was performed.
	 * Used for unit testing.
	 * 
	 * @return true if the optimized union was performed
	 */
	boolean isUnionOptimized() {
	  return isUnionSafe;
	}
	
  private static Envelope overlapEnvelope(Geometry g0, Geometry g1) {
    Envelope g0Env = g0.getEnvelopeInternal();
    Envelope g1Env = g1.getEnvelopeInternal();
    Envelope overlapEnv = g0Env.intersection(g1Env);
    return overlapEnv;
  }
  
  private Geometry combine(Geometry unionGeom, List<Geometry> disjointPolys) {
    if (disjointPolys.size() <= 0)
      return unionGeom;
    
    disjointPolys.add(unionGeom);
    Geometry result = GeometryCombiner.combine(disjointPolys);
    return result;
  }

  private Geometry extractByEnvelope(Envelope env, Geometry geom, 
      List<Geometry> disjointGeoms)
  {
    List<Geometry> intersectingGeoms = new ArrayList<Geometry>();
    for (int i = 0; i < geom.getNumGeometries(); i++) { 
      Geometry elem = geom.getGeometryN(i);
      if (elem.getEnvelopeInternal().intersects(env)) {
        intersectingGeoms.add(elem);
      }
      else {
        Geometry copy = elem.copy();
        disjointGeoms.add(copy);
      }
    }
    return geomFactory.buildGeometry(intersectingGeoms);
  }
  
  private Geometry unionFull(Geometry geom0, Geometry geom1) {
    // if both are empty collections, just return a copy of one of them
    if (geom0.getNumGeometries() == 0 
        && geom1.getNumGeometries() == 0)
      return geom0.copy();
    
    Geometry union = unionFun.union(geom0, geom1);
    //Geometry union = geom0.union(geom1);
    return union;
  }

  /**
   * Implements union using the buffer-by-zero trick.
   * This seems to be more robust than overlay union,
   * for reasons somewhat unknown.
   * 
   * @param g0 a geometry
   * @param g1 a geometry
   * @return the union of the geometries
   */
  private static Geometry unionBuffer(Geometry g0, Geometry g1)
  {
    GeometryFactory factory = g0.getFactory();
    Geometry gColl = factory.createGeometryCollection(new Geometry[] { g0, g1 } );
    Geometry union = gColl.buffer(0.0);
    return union;
  }
  
  private boolean isBorderSegmentsSame(Geometry result, Envelope env) {
    List<LineSegment> segsBefore = extractBorderSegments(g0, g1, env);
    
    List<LineSegment> segsAfter = new ArrayList<LineSegment>();
    extractBorderSegments(result, env, segsAfter);

    //System.out.println("# seg before: " + segsBefore.size() + " - # seg after: " + segsAfter.size());
    return isEqual(segsBefore, segsAfter);
  }
  
  private boolean isEqual(List<LineSegment> segs0, List<LineSegment> segs1) {
    if (segs0.size() != segs1.size())
      return false;
    
    Set<LineSegment> segIndex = new HashSet<LineSegment>(segs0);
    
    for (LineSegment seg : segs1) {
      if (! segIndex.contains(seg)) {
        //System.out.println("Found changed border seg: " + seg);
        return false;
      }
    }
    return true;
  }

  private List<LineSegment> extractBorderSegments(Geometry geom0, Geometry geom1, Envelope env) {
    List<LineSegment> segs = new ArrayList<LineSegment>();
    extractBorderSegments(geom0, env, segs);
    if (geom1 != null)
      extractBorderSegments(geom1, env, segs);
    return segs;
  }
  
  private static boolean intersects(Envelope env, Coordinate p0, Coordinate p1) {
    return env.intersects(p0) || env.intersects(p1);
  }

  private static boolean containsProperly(Envelope env, Coordinate p0, Coordinate p1) {
    return containsProperly(env, p0) && containsProperly(env, p1);
  }

  private static boolean containsProperly(Envelope env, Coordinate p) {
      if (env.isNull()) return false;
      return p.getX() > env.getMinX() &&
          p.getX() < env.getMaxX() &&
          p.getY() > env.getMinY() &&
          p.getY() < env.getMaxY();
  }

  private static void extractBorderSegments(Geometry geom, Envelope env, List<LineSegment> segs) {
    geom.apply(new CoordinateSequenceFilter() {

      public void filter(CoordinateSequence seq, int i) {
        if (i <= 0) return;
        
        // extract LineSegment
        Coordinate p0 = seq.getCoordinate(i - 1);
        Coordinate p1 = seq.getCoordinate(i);
        boolean isBorder = intersects(env, p0, p1) && ! containsProperly(env, p0, p1);
        if (isBorder) {
          LineSegment seg = new LineSegment(p0, p1);
          segs.add(seg);
        }
      }

      public boolean isDone() {   return false;   }

      public boolean isGeometryChanged() {   return false;   }
      
    });
  }

}

