package com.vividsolutions.jtstest.testbuilder.geom;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;

public class GeometryVertexMover 
{

  public static Geometry move(Geometry geom, Coordinate fromLoc, Coordinate toLoc)
  {
    GeometryEditor editor = new GeometryEditor();
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
