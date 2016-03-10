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
