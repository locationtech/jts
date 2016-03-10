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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

public class PreparedGeometryFunctions 
{
  private static PreparedGeometry createPG(Geometry g)
  {
    return (new PreparedGeometryFactory()).create(g);
  }
  
  public static boolean preparedIntersects(Geometry g1, Geometry g2)
  {
    return createPG(g1).intersects(g2);
  }
  
  public static boolean intersects(Geometry g1, Geometry g2)
  {
    return g1.intersects(g2);
  }
  

}
