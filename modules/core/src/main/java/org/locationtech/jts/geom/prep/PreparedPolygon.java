/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom.prep;


import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.noding.FastSegmentSetIntersectionFinder;
import org.locationtech.jts.noding.SegmentStringUtil;
import org.locationtech.jts.operation.predicate.RectangleContains;
import org.locationtech.jts.operation.predicate.RectangleIntersects;

/**
 * A prepared version for {@link Polygonal} geometries.
 * This class supports both {@link Polygon}s and {@link MultiPolygon}s.
 * <p>
 * This class does <b>not</b> support MultiPolygons which are non-valid 
 * (e.g. with overlapping elements). 
 * <p>
 * Instances of this class are thread-safe and immutable.
 * 
 * @author mbdavis
 *
 */
public class PreparedPolygon
  extends BasicPreparedGeometry
{
	private final boolean isRectangle;
	// create these lazily, since they are expensive
	private FastSegmentSetIntersectionFinder segIntFinder = null;
	private PointOnGeometryLocator pia = null;

  public PreparedPolygon(Polygonal poly) {
    super((Geometry) poly);
    isRectangle = getGeometry().isRectangle();
  }

  /**
   * Gets the indexed intersection finder for this geometry.
   * 
   * @return the intersection finder
   */
  public synchronized FastSegmentSetIntersectionFinder getIntersectionFinder()
  {
  	/**
  	 * MD - Another option would be to use a simple scan for 
  	 * segment testing for small geometries.  
  	 * However, testing indicates that there is no particular advantage 
  	 * to this approach.
  	 */
  	if (segIntFinder == null)
  		segIntFinder = new FastSegmentSetIntersectionFinder(SegmentStringUtil.extractSegmentStrings(getGeometry()));
  	return segIntFinder;
  }
  
  public synchronized PointOnGeometryLocator getPointLocator()
  {
  	if (pia == null)
      pia = new IndexedPointInAreaLocator(getGeometry());
 		
    return pia;
  }
  
  public boolean intersects(Geometry g)
  {
  	// envelope test
  	if (! envelopesIntersect(g)) return false;
  	
    // optimization for rectangles
    if (isRectangle) {
      return RectangleIntersects.intersects((Polygon) getGeometry(), g);
    }
    
    return PreparedPolygonIntersects.intersects(this, g);
  }
  
  public boolean contains(Geometry g)
  {
    // short-circuit test
    if (! envelopeCovers(g)) 
    	return false;
  	
    // optimization for rectangles
    if (isRectangle) {
      return RectangleContains.contains((Polygon) getGeometry(), g);
    }

    return PreparedPolygonContains.contains(this, g);
  }
  
  public boolean containsProperly(Geometry g)
  {
    // short-circuit test
    if (! envelopeCovers(g)) 
    	return false;
    return PreparedPolygonContainsProperly.containsProperly(this, g);
  }
  
  public boolean covers(Geometry g)
  {
    // short-circuit test
    if (! envelopeCovers(g)) 
    	return false;
    // optimization for rectangle arguments
    if (isRectangle) {
      return true;
    }
    return PreparedPolygonCovers.covers(this, g);
  }
}
