package com.vividsolutions.jtstest.testbuilder.topostretch;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;

public class GeometryVerticesMover 
{

  public static Geometry move(Geometry geom, Map moves)
  {
    GeometryEditor editor = new GeometryEditor();
    return editor.edit(geom, new MoveVerticesOperation(moves));
  }
  
  private static class MoveVerticesOperation
    extends GeometryEditor.CoordinateOperation
  {
    private Map moves;
    
    public MoveVerticesOperation(Map moves)
    {
      this.moves = moves;
    }
    
    public Coordinate[] edit(Coordinate[] coords,
        Geometry geometry)
    {
      Coordinate[] newPts = new Coordinate[coords.length];
      for (int i = 0; i < coords.length; i++) {
        newPts[i] = movedPt(coords[i]);                   
      }
      return newPts;
    }
    
    private Coordinate movedPt(Coordinate orig)
    {
    	Coordinate newLoc = (Coordinate) moves.get(orig);
    	if (newLoc == null) 
    		return orig;
    	return (Coordinate) newLoc.clone();
    }
  }

  
}
