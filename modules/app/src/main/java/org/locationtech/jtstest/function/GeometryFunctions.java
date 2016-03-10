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
package org.locationtech.jtstest.function;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.overlay.snap.*;


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

  public static Geometry getPolygonHoles(Geometry geom)
  {
    final List holePolys = new ArrayList();
    geom.apply(new GeometryFilter() {

      public void filter(Geometry geom) {
        if (geom instanceof Polygon) {
          Polygon poly = (Polygon) geom;
          for (int i = 0; i < poly.getNumInteriorRing(); i++) {
            Polygon hole = geom.getFactory().createPolygon((LinearRing) poly.getInteriorRingN(i), null);
            holePolys.add(hole);
          }
        }
      }      
    });
    return geom.getFactory().buildGeometry(holePolys);
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
