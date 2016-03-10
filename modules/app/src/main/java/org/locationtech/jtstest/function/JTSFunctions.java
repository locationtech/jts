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

import org.locationtech.jts.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.util.*;

public class JTSFunctions 
{
  public static String jtsVersion(Geometry g)
  {
    return JTSVersion.CURRENT_VERSION.toString();
  }
  
  private static final double HEIGHT = 70;
  private static final double WIDTH = 150; //125;
  private static final double J_WIDTH = 30;
  private static final double J_RADIUS = J_WIDTH - 5;
  
  private static final double S_RADIUS = HEIGHT / 4;
  
  private static final double T_WIDTH = WIDTH - 2 * S_RADIUS - J_WIDTH;

  
  public static Geometry logoLines(Geometry g)
  {
    return create_J(g)
    .union(create_T(g))
    .union(create_S(g));
  }
  
  public static Geometry logoBuffer(Geometry g, double distance)
  {
    Geometry lines = logoLines(g);
    BufferParameters bufParams = new BufferParameters();
    bufParams.setEndCapStyle(BufferParameters.CAP_SQUARE);   
    return BufferOp.bufferOp(lines, distance, bufParams);
  }
  
  private static Geometry create_J(Geometry g)
  {
    GeometryFactory gf = FunctionsUtil.getFactoryOrDefault(g);
    
    Coordinate[] jTop = new Coordinate[] {
        new Coordinate(0,HEIGHT),
        new Coordinate(J_WIDTH,HEIGHT),
        new Coordinate(J_WIDTH,J_RADIUS)
    };
    Coordinate[] jBottom = new Coordinate[] {
        new Coordinate(J_WIDTH - J_RADIUS,0),
        new Coordinate(0,0)
    };
    
    GeometricShapeFactory gsf = new GeometricShapeFactory(gf);
    gsf.setBase(new Coordinate(J_WIDTH - 2 * J_RADIUS, 0));
    gsf.setSize(2 * J_RADIUS);
    gsf.setNumPoints(10);
    LineString jArc = gsf.createArc(1.5 * Math.PI, 0.5 * Math.PI);
    
    CoordinateList coordList = new CoordinateList();
    coordList.add(jTop, false);
    coordList.add(jArc.reverse().getCoordinates(), false, 1, jArc.getNumPoints() - 1);
    coordList.add(jBottom, false);
    
    return gf.createLineString(coordList.toCoordinateArray());
  }
  
  private static Geometry create_T(Geometry g)
  {
    GeometryFactory gf = FunctionsUtil.getFactoryOrDefault(g);
    
    Coordinate[] tTop = new Coordinate[] {
        new Coordinate(J_WIDTH,HEIGHT),
        new Coordinate(WIDTH - S_RADIUS - 5, HEIGHT)
    };
    Coordinate[] tBottom = new Coordinate[] {
        new Coordinate(J_WIDTH + 0.5 * T_WIDTH,HEIGHT),
        new Coordinate(J_WIDTH + 0.5 * T_WIDTH,0)
    };
    LineString[] lines = new LineString[] {
        gf.createLineString(tTop),
        gf.createLineString(tBottom)
    };
    return gf.createMultiLineString(lines);
  }

  private static Geometry create_S(Geometry g)
  {
    GeometryFactory gf = FunctionsUtil.getFactoryOrDefault(g);
    
    double centreX = WIDTH - S_RADIUS;
    
    Coordinate[] top = new Coordinate[] {
        new Coordinate(WIDTH, HEIGHT),
        new Coordinate(centreX, HEIGHT)
    };
    Coordinate[] bottom = new Coordinate[] {
        new Coordinate(centreX, 0),
        new Coordinate(WIDTH - 2 * S_RADIUS, 0)
    };
    
    GeometricShapeFactory gsf = new GeometricShapeFactory(gf);
    gsf.setCentre(new Coordinate(centreX, HEIGHT - S_RADIUS));
    gsf.setSize(2 * S_RADIUS);
    gsf.setNumPoints(10);
    LineString arcTop = gsf.createArc(0.5 * Math.PI, Math.PI);
    
    GeometricShapeFactory gsf2 = new GeometricShapeFactory(gf);
    gsf2.setCentre(new Coordinate(centreX, S_RADIUS));
    gsf2.setSize(2 * S_RADIUS);
    gsf2.setNumPoints(10);
    LineString arcBottom = (LineString) gsf2.createArc(1.5 * Math.PI, Math.PI).reverse();
    
    CoordinateList coordList = new CoordinateList();
    coordList.add(top, false);
    coordList.add(arcTop.getCoordinates(), false, 1, arcTop.getNumPoints() - 1);
    coordList.add(new Coordinate(centreX, HEIGHT/2));
    coordList.add(arcBottom.getCoordinates(), false, 1, arcBottom.getNumPoints() - 1);
    coordList.add(bottom, false);
    
    
    
    return gf.createLineString(coordList.toCoordinateArray());
  }

}
