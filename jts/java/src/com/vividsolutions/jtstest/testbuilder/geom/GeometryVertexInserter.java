package com.vividsolutions.jtstest.testbuilder.geom;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;

public class GeometryVertexInserter 
{
  public static Geometry insert(Geometry geom, 
      LineString line, 
      int segIndex,
      Coordinate newVertex)
  {
    GeometryEditor editor = new GeometryEditor();
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
