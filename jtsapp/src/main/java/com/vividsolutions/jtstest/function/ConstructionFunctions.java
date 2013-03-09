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

import java.util.*;
import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;
import com.vividsolutions.jts.densify.*;
import com.vividsolutions.jts.operation.polygonize.*;
import com.vividsolutions.jts.operation.linemerge.*;

public class ConstructionFunctions {
  public static Geometry octagonalEnvelope(Geometry g) 
  {      
    OctagonalEnvelope octEnv = new OctagonalEnvelope(g);
    return octEnv.toGeometry(g.getFactory());
  }
  
  public static Geometry minimumDiameterLine(Geometry g) {      return (new MinimumDiameter(g)).getDiameter();  }
  public static double minimumDiameter(Geometry g) {      return (new MinimumDiameter(g)).getDiameter().getLength();  }

  public static Geometry minimumRectangle(Geometry g) {      return (new MinimumDiameter(g)).getMinimumRectangle();  }
  public static Geometry minimumBoundingCircle(Geometry g) {      return (new MinimumBoundingCircle(g)).getCircle();  }
  public static Geometry minimumBoundingCirclePoints(Geometry g) {      return 
    g.getFactory().createLineString((new MinimumBoundingCircle(g)).getExtremalPoints());  }
  public static double maximumDiameter(Geometry g) {      return 2 * (new MinimumBoundingCircle(g)).getRadius();  }
  
  public static Geometry boundary(Geometry g) {      return g.getBoundary();  }
  public static Geometry convexHull(Geometry g) {      return g.convexHull();  }
  public static Geometry centroid(Geometry g) {      return g.getCentroid();  }
  public static Geometry interiorPoint(Geometry g) {      return g.getInteriorPoint();  }

  public static Geometry densify(Geometry g, double distance) { return Densifier.densify(g, distance); }
  
}
