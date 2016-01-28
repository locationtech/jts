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

package com.vividsolutions.jtstest.testbuilder.geom;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.*;

public class GeometryVertexMover 
{

  public static Geometry move(Geometry geom, Coordinate fromLoc, Coordinate toLoc)
  {
    GeometryEditor editor = new GeometryEditor();
    editor.setCopyUserData(true);
    return editor.edit(geom, new MoveVertexOperation(fromLoc, toLoc));
  }
  
  private static class MoveVertexOperation
    extends GeometryEditor.CoordinateOperation
  {
    private Coordinate fromLoc;
    private Coordinate toLoc;
    
    public MoveVertexOperation(Coordinate fromLoc, Coordinate toLoc)
    {
      this.fromLoc = fromLoc;
      this.toLoc = toLoc;
    }
    
    public Coordinate[] edit(Coordinate[] coords,
        Geometry geometry)
    {
      Coordinate[] newPts = new Coordinate[coords.length];
      for (int i = 0; i < coords.length; i++) {
        newPts[i] = 
          (coords[i].equals2D(fromLoc)) 
            ? (Coordinate) toLoc.clone()
                : (Coordinate) coords[i].clone();
                   
      }
      return newPts;
    }
  }

  
}
