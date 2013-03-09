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
package com.vividsolutions.jtstest.function;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.overlay.snap.*;

/**
 * Implementations for various geometry functions.
 * 
 * @author Martin Davis
 * 
 */
public class GeometryFunctions 
{
	public static String lengthDescription = "Computes the length of perimeter of a Geometry";
	public static double length(Geometry g)				{		return g.getLength();	}
	public static double area(Geometry g)					{		return g.getArea();	}
	
  public static boolean isCCW(Geometry g)
  {
    Coordinate[] pts = null;
    if (g instanceof Polygon) {
      pts = ((Polygon) g).getExteriorRing().getCoordinates();
    } 
    else if (g instanceof LineString
        && ((LineString) g).isClosed()) {
      pts = g.getCoordinates();
    }
    if (pts == null) return false;
    return CGAlgorithms.isCCW(pts);
  }
  
	public static boolean isSimple(Geometry g)		{		return g.isSimple();	}
	public static boolean isValid(Geometry g)			{		return g.isValid();	}
	public static boolean isRectangle(Geometry g)	{		return g.isRectangle();	}
	public static boolean isClosed(Geometry g)	{
		if (g instanceof LineString) return ((LineString) g).isClosed();
		if (g instanceof MultiLineString) return ((MultiLineString) g).isClosed();
		// other geometry types are defined to be closed
		return true;	
		}
	
  public static Geometry envelope(Geometry g) 	{ return g.getEnvelope();  }
  public static Geometry reverse(Geometry g) {      return g.reverse();  }
  public static Geometry normalize(Geometry g) 
  {      
  	Geometry gNorm = (Geometry) g.clone();
  	gNorm.normalize();
    return gNorm;
  }

	public static Geometry getGeometryN(Geometry g, int i)
	{
		return g.getGeometryN(i);
	}

	public static Geometry getPolygonShell(Geometry g)
	{
		if (g instanceof Polygon) {
			LinearRing shell = (LinearRing) ((Polygon) g).getExteriorRing();
			return g.getFactory().createPolygon(shell, null);
		}
		if (g instanceof MultiPolygon) {
			Polygon[] poly = new Polygon[g.getNumGeometries()];
			for (int i = 0; i < g.getNumGeometries(); i++) {
				LinearRing shell = (LinearRing) ((Polygon) g.getGeometryN(i)).getExteriorRing();
				poly[i] = g.getFactory().createPolygon(shell, null);
			}
			return g.getFactory().createMultiPolygon(poly);
		}
		return null;
	}

	public static Geometry getPolygonHoleN(Geometry g, int i)
	{
		if (g instanceof Polygon) {
			LinearRing ring = (LinearRing) ((Polygon) g).getInteriorRingN(i);
			return ring;
		}
		return null;
	}

	public static Geometry convertToPolygon(Geometry g)
	{
		if (g instanceof Polygonal) return g;
		// TODO: ensure ring is valid
		LinearRing ring = g.getFactory().createLinearRing(g.getCoordinates());
		return g.getFactory().createPolygon(ring, null);
	}

	public static Geometry getCoordinates(Geometry g)
	{
		Coordinate[] pts = g.getCoordinates();
		return g.getFactory().createMultiPoint(pts);
	}
}
