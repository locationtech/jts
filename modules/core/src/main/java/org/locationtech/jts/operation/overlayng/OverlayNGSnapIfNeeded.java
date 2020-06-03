/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.noding.IntersectionAdder;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.ValidatingNoder;
import org.locationtech.jts.noding.snap.SnappingNoder;


/**
 * Performs an overlay operation using full precision
 * if possible, and snap-rounding only as a fall-back for failure.
 * <p>
 * <b>WARNING</b> - this approach can produce artifacts 
 * when unioning polygonal coverages. 
 * The issue occurs when one group of polygons is snapped,
 * and an adjacent polygon is not.  A gap can be 
 * introduced between snapped and non-snapped segments.
 *     
 * @author Martin Davis
 */
public class OverlayNGSnapIfNeeded
{

  private static final double SNAP_TOL_FACTOR = 1e12;

  public static Geometry intersection(Geometry g0, Geometry g1)
  {
     return overlay(g0, g1, OverlayNG.INTERSECTION);
  }

  public static Geometry union(Geometry g0, Geometry g1)
  {
     return overlay(g0, g1, OverlayNG.UNION);
  }

  public static Geometry difference(Geometry g0, Geometry g1)
  {
     return overlay(g0, g1, OverlayNG.DIFFERENCE);
  }

  public static Geometry symDifference(Geometry g0, Geometry g1)
  {
     return overlay(g0, g1, OverlayNG.SYMDIFFERENCE);
  }
  
  private static PrecisionModel PM_FLOAT = new PrecisionModel();

  public static Geometry overlay(Geometry geom0, Geometry geom1, int opCode)
  {
    Geometry result;
    RuntimeException exOriginal;
    try {
      // start with operation using floating PM
      result = OverlayNG.overlay(geom0, geom1, opCode, PM_FLOAT ); 
      //result = OverlayNG.overlay(geom0, geom1, opCode, createFloatingNoder()); 
      return result;
    }
    catch (RuntimeException ex) {
      exOriginal = ex;
    	// ignore this exception, since the operation will be rerun
      //System.out.println("Overlay failed");
    }
    // on failure retry using snapping noding with a "safe" tolerance
  	// if this throws an exception just let it go
    
    double snapTol = snapTolerance(geom0, geom1);
    for (int i = 0; i < 5; i++) {
      result = overlaySnapping(geom0, geom1, opCode, snapTol);
      if (result != null) return result;
      snapTol = snapTol * 10;
    }
    System.out.println(geom0);
    System.out.println(geom1);
    throw exOriginal;
  }

  private static Noder createFloatingNoder() {
    MCIndexNoder noder = new MCIndexNoder();
    LineIntersector li = new RobustLineIntersector();
    noder.setSegmentIntersector(new IntersectionAdder(li));
    return noder;
  }
    
  private static Noder createSnappingtNoder(double tolerance) {
    SnappingNoder snapNoder = new SnappingNoder(tolerance);
    return snapNoder;
    //return new ValidatingNoder(snapNoder);
  }

  private static Geometry overlaySnapping(Geometry geom0, Geometry geom1, int opCode, double snapTol) {
    Geometry result;
    try {
      Noder noder = createSnappingtNoder(snapTol);
      //System.out.println("Snapping with " + snapTol);

      result = OverlayNG.overlay(geom0, geom1, opCode, noder);
      return result;
    }
    catch (TopologyException ex) {
      System.out.println("Snapping with " + snapTol + " - FAILED");
      //System.out.println(geom0);
      //System.out.println(geom1);
    }
    return null;
  }

  private static double snapTolerance(Geometry geom0, Geometry geom1) {
    double tol0 = snapTolerance(geom0);
    double tol1 = snapTolerance(geom1);
    double snapTol = Math.max(tol0,  tol1);
    return snapTol;
  }
  
  public static double snapTolerance(Geometry geom) {
    double magnitude = ordinateMagnitude(geom);
    return magnitude / SNAP_TOL_FACTOR;
  }
  
  private static double ordinateMagnitude(Geometry geom) {
    Envelope env = geom.getEnvelopeInternal();
    double magMax = Math.max(
        Math.abs(env.getMaxX()), Math.abs(env.getMaxY()));
    double magMin = Math.max(
        Math.abs(env.getMinX()), Math.abs(env.getMinY()));
    return Math.max(magMax, magMin);
  }
  
  public static Geometry overlaySR(Geometry geom0, Geometry geom1, int opCode)
  {
    Geometry result;
    try {
      // start with operation using floating PM
      result = OverlayNG.overlay(geom0, geom1, opCode, PM_FLOAT); 
      return result;
    }
    catch (TopologyException ex) {
      // ignore this exception, since the operation will be rerun
      //System.out.println("Overlay failed");
    }
    // on failure retry with a "safe" fixed PM
    // this should not throw an exception, but if it does just let it go
    double scaleSafe = PrecisionUtil.safeScale(geom0, geom1);
    PrecisionModel pmSafe = new PrecisionModel(scaleSafe);
    result = OverlayNG.overlay(geom0, geom1, opCode, pmSafe);
    return result;
  }

}
