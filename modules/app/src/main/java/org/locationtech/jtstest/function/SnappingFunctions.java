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

import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.overlay.snap.*;

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
