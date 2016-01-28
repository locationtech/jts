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

package org.locationtech.jtstest.testbuilder.topostretch;

import java.util.*;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.*;

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
