package com.vividsolutions.jtstest.testbuilder.topostretch;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;

public class GeometryVerticesMover 
{
  public static Geometry move(Geometry geom, Map moves)
  {
  	GeometryVerticesMover mover = new GeometryVerticesMover(geom, moves);
    return mover.move();
  }
  
  private Geometry geom; 
  private Map moves;
  private List modifiedCoords = new ArrayList();
  
  public GeometryVerticesMover(Geometry geom, Map moves)
  {
  	this.geom = geom;
  	this.moves = moves;
  }
  
  public Geometry move()
  {
    GeometryEditor editor = new GeometryEditor();
    MoveVerticesOperation op = new MoveVerticesOperation(moves);
    Geometry movedGeom = editor.edit(geom, new MoveVerticesOperation(moves));
  	return movedGeom;
  }
  
  public List getModifiedCoordinates()
  {
  	return modifiedCoords;
  }

  private class MoveVerticesOperation
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
    	Coordinate mod = (Coordinate) newLoc.clone();
    	modifiedCoords.add(mod);
    	return mod;
    }
  }

  
}
