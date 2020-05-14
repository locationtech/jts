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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.TopologyException;


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
    try {
      // start with operation using floating PM
      result = OverlayNG.overlay(geom0, geom1, opCode, PM_FLOAT); 
      return result;
    }
    catch (TopologyException ex) {
    	// ignore this exception, since the operation will be rerun
      System.out.println("Overlay failed");
    }
    // on failure retry with a "safe" fixed PM
  	// this should not throw an exception, but if it does just let it go
    double scaleSafe = PrecisionUtil.safeScale(geom0, geom1);
    PrecisionModel pmSafe = new PrecisionModel(scaleSafe);
    result = OverlayNG.overlay(geom0, geom1, opCode, pmSafe);
    return result;
  }
}
