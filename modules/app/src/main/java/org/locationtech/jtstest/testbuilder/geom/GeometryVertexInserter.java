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

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.*;

public class GeometryVertexInserter 
{
  public static Geometry insert(Geometry geom, 
      LineString line, 
      int segIndex,
      Coordinate newVertex)
  {
    GeometryEditor editor = new GeometryEditor();
    editor.setCopyUserData(true);
    return editor.edit(geom, new InsertVertexOperation(line, segIndex, newVertex));
  }
  
  private static class InsertVertexOperation
    extends GeometryEditor.CoordinateOperation
  {
    private LineString line;
    private int segIndex;
    private Coordinate newVertex;
    
    public InsertVertexOperation(LineString line, int segIndex, Coordinate newVertex)
    {
      this.line = line;
      this.segIndex = segIndex;
      this.newVertex = newVertex;
    }
    
    public Coordinate[] edit(Coordinate[] coords,
        Geometry geometry)
    {
      if (geometry != line) return coords;
      
      Coordinate[] newPts = new Coordinate[coords.length + 1];
      for (int i = 0; i < coords.length; i++) {
        int actualIndex = i > segIndex ? i + 1 : i;
        newPts[actualIndex] = (Coordinate) coords[i].clone();
      }
      newPts[segIndex + 1] = (Coordinate) newVertex.clone();
      return newPts;
    }
  }

  
}
