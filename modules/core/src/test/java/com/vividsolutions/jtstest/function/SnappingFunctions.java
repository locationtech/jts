/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jtstest.function;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.overlay.snap.*;

/**
 * Implementations for various geometry functions.
 * 
 * @author Martin Davis
 * 
 */
public class SnappingFunctions 
{
  public static Geometry snapAtoB(Geometry g, Geometry g2, double distance) 
  {         
    Geometry[] snapped = GeometrySnapper.snap(g, g2, distance);
    return snapped[0];
  }
  
  public static Geometry snapToSelfAndClean(Geometry g, double distance) 
  {         
    return GeometrySnapper.snapToSelf(g, distance, true);
  }

}
