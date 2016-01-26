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

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;

public class GeometryVertexDeleter 
{
  public static Geometry delete(Geometry geom, 
      LineString line, 
      int vertexIndex)
  {
    GeometryEditor editor = new GeometryEditor();
    editor.setCopyUserData(true);
    return editor.edit(geom, new DeleteVertexOperation(line, vertexIndex));
  }
  
  private static class DeleteVertexOperation
    extends GeometryEditor.CoordinateOperation
  {
    private LineString line;
    private int vertexIndex;
    private Coordinate newVertex;
    
    public DeleteVertexOperation(LineString line, int vertexIndex)
    {
      this.line = line;
      this.vertexIndex = vertexIndex;
    }
    
    public Coordinate[] edit(Coordinate[] coords,
        Geometry geometry)
    {
      if (geometry != line) return coords;
      
      int minLen = 2;
      if (geometry instanceof LinearRing) minLen = 4;
      
      // don't change if would make geometry invalid
      if (coords.length <= minLen)
        return coords;
      
      int newLen = coords.length - 1;
      Coordinate[] newPts = new Coordinate[newLen];
      int newIndex = 0;
      for (int i = 0; i < coords.length; i++) {
        if (i != vertexIndex) {
          newPts[newIndex] = coords[i];
          newIndex++;
        }
      }
      
      // close ring if required
      if (geometry instanceof LinearRing) {
        if (newPts[newLen - 1] == null 
            || ! newPts[newLen - 1].equals2D(newPts[0])) {
          newPts[newLen - 1] = new Coordinate(newPts[0]);
        }
      }
      
      return newPts; 
    }
  }

  
}
