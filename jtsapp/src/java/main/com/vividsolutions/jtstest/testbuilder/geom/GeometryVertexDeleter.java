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
