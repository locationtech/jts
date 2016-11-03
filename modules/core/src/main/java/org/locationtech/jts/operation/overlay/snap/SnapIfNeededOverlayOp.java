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

package org.locationtech.jts.operation.overlay.snap;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.operation.overlay.OverlayOp;


/**
 * Performs an overlay operation using snapping and enhanced precision
 * to improve the robustness of the result.
 * This class only uses snapping
 * if an error is detected when running the standard JTS overlay code.
 * Errors detected include thrown exceptions 
 * (in particular, {@link TopologyException})
 * and invalid overlay computations.
 *     
 * @author Martin Davis
 * @version 1.7
 */
public class SnapIfNeededOverlayOp
{
  public static Geometry overlayOp(Geometry g0, Geometry g1, int opCode)
  {
  	SnapIfNeededOverlayOp op = new SnapIfNeededOverlayOp(g0, g1);
  	return op.getResultGeometry(opCode);
  }

  public static Geometry intersection(Geometry g0, Geometry g1)
  {
     return overlayOp(g0, g1, OverlayOp.INTERSECTION);
  }

  public static Geometry union(Geometry g0, Geometry g1)
  {
     return overlayOp(g0, g1, OverlayOp.UNION);
  }

  public static Geometry difference(Geometry g0, Geometry g1)
  {
     return overlayOp(g0, g1, OverlayOp.DIFFERENCE);
  }

  public static Geometry symDifference(Geometry g0, Geometry g1)
  {
     return overlayOp(g0, g1, OverlayOp.SYMDIFFERENCE);
  }
  
  private Geometry[] geom = new Geometry[2];

  public SnapIfNeededOverlayOp(Geometry g1, Geometry g2)
  {
    geom[0] = g1;
    geom[1] = g2;
  }

  public Geometry getResultGeometry(int opCode)
  {
    Geometry result = null;
    boolean isSuccess = false;
    RuntimeException savedException = null;
    try {
      // try basic operation with input geometries
      result = OverlayOp.overlayOp(geom[0], geom[1], opCode); 
      boolean isValid = true;
      // not needed if noding validation is used
//      boolean isValid = OverlayResultValidator.isValid(geom[0], geom[1], OverlayOp.INTERSECTION, result);
      if (isValid)
      	isSuccess = true;
    }
    catch (RuntimeException ex) {
    	savedException = ex;
    	// ignore this exception, since the operation will be rerun
//    	System.out.println(ex.getMessage());
//    	ex.printStackTrace();
    	//System.out.println(ex.getMessage());
    	//System.out.println("Geom 0: " + geom[0]);
    	//System.out.println("Geom 1: " + geom[1]);
    }
    if (! isSuccess) {
    	// this may still throw an exception
    	// if so, throw the original exception since it has the input coordinates
    	try {
    		result = SnapOverlayOp.overlayOp(geom[0], geom[1], opCode);
    	}
    	catch (RuntimeException ex) {
    		throw savedException;
    	}
    }
    return result;
  }
}
