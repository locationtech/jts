/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.geomop;

import static org.locationtech.jts.operation.overlayng.OverlayNG.DIFFERENCE;
import static org.locationtech.jts.operation.overlayng.OverlayNG.INTERSECTION;
import static org.locationtech.jts.operation.overlayng.OverlayNG.SYMDIFFERENCE;
import static org.locationtech.jts.operation.overlayng.OverlayNG.UNION;

import java.util.Collection;

import org.locationtech.jts.densify.Densifier;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.precision.MinimumClearance;

/**
 * Geometry functions which
 * augment the existing methods on {@link Geometry},
 * for use in XML Test files.
 * This is the default used in the TestRunner, 
 * and thus all the operations 
 * in this class should be named differently to the Geometry methods
 * (otherwise they will shadow the real Geometry methods).
 * <p>
 * If replacing a Geometry method is desired, this
 * can be done via the -geomfunc argument to the TestRunner.
 * 
 * @author Martin Davis
 *
 */
public class TestCaseGeometryFunctions 
{
	public static Geometry bufferMitredJoin(Geometry g, double distance)	
	{
    BufferParameters bufParams = new BufferParameters();
    bufParams.setJoinStyle(BufferParameters.JOIN_MITRE);
    
    return BufferOp.bufferOp(g, distance, bufParams);
	}

  public static Geometry densify(Geometry g, double distance) 
  {
    return Densifier.densify(g, distance);
  }

  public static double minClearance(Geometry g) 
  {
    return MinimumClearance.getDistance(g);
  }

  public static Geometry minClearanceLine(Geometry g) 
  {
    return MinimumClearance.getLine(g);
  }

  private static Geometry polygonize(Geometry g, boolean extractOnlyPolygonal) {
    Collection lines = LinearComponentExtracter.getLines(g);
    Polygonizer polygonizer = new Polygonizer(extractOnlyPolygonal);
    polygonizer.add(lines);
    return polygonizer.getGeometry();
  }
  
  public static Geometry polygonize(Geometry g) {
    return polygonize(g, false);
  }
  
  public static Geometry polygonizeValidPolygonal(Geometry g) {
    return polygonize(g, true);
  }
  
  public static Geometry intersectionNG(Geometry geom0, Geometry geom1) {
    return OverlayNG.overlay(geom0, geom1, OverlayNG.INTERSECTION);
  }
  public static Geometry unionNG(Geometry geom0, Geometry geom1) {
    return OverlayNG.overlay(geom0, geom1, OverlayNG.UNION);
  }
  public static Geometry differenceNG(Geometry geom0, Geometry geom1) {
    return OverlayNG.overlay(geom0, geom1, OverlayNG.DIFFERENCE);
  }
  public static Geometry symDifferenceNG(Geometry geom0, Geometry geom1) {
    return OverlayNG.overlay(geom0, geom1, OverlayNG.SYMDIFFERENCE);
  }
  
  public static Geometry intersectionSR(Geometry geom0, Geometry geom1, double scale) {
    PrecisionModel pm = new PrecisionModel(scale);
    return OverlayNG.overlay(geom0, geom1, OverlayNG.INTERSECTION, pm);
  }
  public static Geometry unionSr(Geometry geom0, Geometry geom1, double scale) {
    PrecisionModel pm = new PrecisionModel(scale);
    return OverlayNG.overlay(geom0, geom1, OverlayNG.UNION, pm);
  }
  public static Geometry differenceSR(Geometry geom0, Geometry geom1, double scale) {
    PrecisionModel pm = new PrecisionModel(scale);
    return OverlayNG.overlay(geom0, geom1, OverlayNG.DIFFERENCE, pm);
  }
  public static Geometry symDifferenceSR(Geometry geom0, Geometry geom1, double scale) {
    PrecisionModel pm = new PrecisionModel(scale);
    return OverlayNG.overlay(geom0, geom1, OverlayNG.SYMDIFFERENCE, pm);
  }

  public static double unionArea(Geometry geom) {
    return geom.union().getArea();
  }
  
  public static double unionLength(Geometry geom) {
    return geom.union().getLength();
  }
  
  public static boolean overlayAreaTest(Geometry a, Geometry b) {
    double areaDelta = areaDelta(a, b);
    return areaDelta < 1e-6; 
  }
  
  /**
   * Computes the maximum area delta value
   * resulting from identity equations over the overlay operations.
   * The delta value is normalized to the total area of the geometries.
   * If the overlay operations are computed correctly 
   * the area delta is expected to be very small (e.g. < 1e-6).
   *  
   * @param a a geometry
   * @param b a geometry
   * @return the computed maximum area delta
   */
  private static double areaDelta(Geometry a, Geometry b) {
    
    double areaA = a == null ? 0 : a.getArea();
    double areaB = b == null ? 0 : b.getArea();
    
    // if an input is non-polygonal delta is 0
    if (areaA == 0 || areaB == 0)
      return 0;
    
    double areaU   = a.union( b ).getArea();    
    double areaI   = a.intersection( b ).getArea();
    double areaDab = a.difference( b ).getArea();
    double areaDba = b.difference( a ).getArea();
    double areaSD  = a.symDifference( b ).getArea();
    
    double maxDelta = 0;

    // & : intersection
    // - : difference
    // + : union
    // ^ : symdifference


    // A = ( A & B ) + ( A - B )
    double delta = Math.abs( areaA - areaI - areaDab );
    if (delta > maxDelta) {
        maxDelta = delta;
    }

    // B = ( A & B ) + ( B - A )
    delta = Math.abs( areaB - areaI - areaDba );
    if (delta > maxDelta) {
        maxDelta = delta;
    }

    //  ( A ^ B ) = ( A - B ) + ( B - A )
    delta = Math.abs( areaDab + areaDba - areaSD );
    if (delta > maxDelta) {
        maxDelta = delta;
    }

    //  ( A + B ) = ( A & B ) + ( A ^ B )
    delta = Math.abs( areaI + areaSD - areaU );
    if (delta > maxDelta) {
        maxDelta = delta;
    }

    //  ( A + B ) = ( A & B ) + ( A - B ) + ( A - B )
    delta = Math.abs( areaU - areaI - areaDab - areaDba );
    if (delta > maxDelta) {
        maxDelta = delta;
    }
    
    // normalize the area delta value
    return maxDelta / (areaA + areaB);
  }
  
}
;