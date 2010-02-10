/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package test.jts.perf.algorithm;

import java.util.*;

import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.algorithm.locate.PointOnGeometryLocator;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;
import com.vividsolutions.jts.noding.*;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.chain.*;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * Determines the location of {@link Coordinate}s relative to
 * a {@link Polygonal} geometry, using indexing for efficiency.
 * This algorithm is suitable for use in cases where
 * many points will be tested against a given area.
 * 
 * @author Martin Davis
 *
 */
public class MCIndexedPointInAreaLocator 
	implements PointOnGeometryLocator
{
	private Geometry areaGeom;
	private MCIndexedGeometry index;
	private double maxXExtent;
		
	public MCIndexedPointInAreaLocator(Geometry g)
	{
		areaGeom = g;
		if (! (g instanceof Polygonal))
			throw new IllegalArgumentException("Argument must be Polygonal");
		buildIndex(g);
    Envelope env = g.getEnvelopeInternal();
		maxXExtent = env.getMaxX() + 1.0;
	}
	
	private void buildIndex(Geometry g)
	{
		index = new MCIndexedGeometry(g);
	}
		
  /**
   * Determines the {@link Location} of a point in an areal {@link Geometry}.
   * 
   * @param p the point to test
   * @return the location of the point in the geometry  
   */
	public int locate(Coordinate p)
	{
		RayCrossingCounter rcc = new RayCrossingCounter(p);
		MCSegmentCounter mcSegCounter = new MCSegmentCounter(rcc);
		Envelope rayEnv = new Envelope(p.x, maxXExtent, p.y, p.y);
		List mcs = index.query(rayEnv);
		countSegs(rcc, rayEnv, mcs, mcSegCounter);
		
		return rcc.getLocation();
	}
	
	private void countSegs(RayCrossingCounter rcc, Envelope rayEnv, List monoChains, MCSegmentCounter mcSegCounter)
	{
		for (Iterator i = monoChains.iterator(); i.hasNext(); ) {
			MonotoneChain mc = (MonotoneChain) i.next();
			mc.select(rayEnv, mcSegCounter);
			// short-circuit if possible
			if (rcc.isOnSegment()) return;
		}
	}
	
  static class MCSegmentCounter extends MonotoneChainSelectAction
  {
  	RayCrossingCounter rcc;

    public MCSegmentCounter(RayCrossingCounter rcc)
    {
      this.rcc = rcc;
    }

    public void select(LineSegment ls)
    {
      rcc.countSegment(ls.getCoordinate(0), ls.getCoordinate(1));
    }
  }

}

class MCIndexedGeometry
{
  private SpatialIndex index= new STRtree();

	public MCIndexedGeometry(Geometry geom)
	{
		init(geom);
	}
	
	private void init(Geometry geom)
	{
		List lines = LinearComponentExtracter.getLines(geom);
		for (Iterator i = lines.iterator(); i.hasNext(); ) {
			LineString line = (LineString) i.next();
			Coordinate[] pts = line.getCoordinates();
			addLine(pts);
		}
	}
	
	private void addLine(Coordinate[] pts)
	{
			SegmentString segStr = new BasicSegmentString(pts, null);
	    List segChains = MonotoneChainBuilder.getChains(segStr.getCoordinates(), segStr);
	    for (Iterator i = segChains.iterator(); i.hasNext(); ) {
	      MonotoneChain mc = (MonotoneChain) i.next();
	      index.insert(mc.getEnvelope(), mc);
	    }
	}
	
	public List query(Envelope searchEnv)
	{
		return index.query(searchEnv);
	}
}


