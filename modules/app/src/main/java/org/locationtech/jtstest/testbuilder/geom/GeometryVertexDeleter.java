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

package org.locationtech.jtstest.testbuilder.geom;

import java.util.*;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.*;

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
