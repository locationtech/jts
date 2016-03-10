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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.util.GeometryEditor;

/**
 * Deletes vertices or components from a geometry
 * which lie inside a given box.
 * If the box completely contains one or more components
 * (including polygon holes), those components are deleted
 * and the operation stops.
 * Otherwise if the box contains a subset of vertices 
 * from a component, those vertices are deleted. 
 * When deleting vertices only <i>one</i> component of the geometry
 * is modified (the first one found which has vertices in the box).
 * 
 * @author Martin Davis
 *
 */
public class GeometryBoxDeleter 
{
  public static Geometry delete(Geometry geom, 
      Envelope env)
  {
    Geometry gComp = deleteComponents(geom, env);
    if (gComp != null) return gComp;
    
    // otherwise, try and edit vertices
    Geometry gVert = deleteVertices(geom, env);
    if (gVert != null) return gVert;

    // no edits - return original
    return geom;
  }
  
  private static Geometry deleteComponents(Geometry geom, Envelope env)
  {
    GeometryEditor editor = new GeometryEditor();
    BoxDeleteComponentOperation compOp = new BoxDeleteComponentOperation(env);
    Geometry compEditGeom = editor.edit(geom, compOp);
    if (compOp.isEdited()) return compEditGeom;
    return null;
  }
  
  private static Geometry deleteVertices(Geometry geom, Envelope env)
  {
    GeometryEditor editor = new GeometryEditor();
    BoxDeleteVertexOperation vertexOp = new BoxDeleteVertexOperation(env);
    Geometry vertexEditGeom = editor.edit(geom, vertexOp);
    if (vertexOp.isEdited()) return vertexEditGeom;
    return null;
  }
  
  private static class BoxDeleteComponentOperation
    implements GeometryEditor.GeometryEditorOperation
  {
    private Envelope env;
    private boolean isEdited = false;
    
    public BoxDeleteComponentOperation(Envelope env)
    {
      this.env = env;
    }
    
    public boolean isEdited() { return isEdited; }

    public Geometry edit(Geometry geometry, GeometryFactory factory)
    {
      // Allow any number of components to be deleted
      //if (isEdited) return geometry;
      if (env.contains(geometry.getEnvelopeInternal())) {
          isEdited = true;
          return null;
      }
      return geometry;
    }
  }
  
  private static class BoxDeleteVertexOperation
    extends GeometryEditor.CoordinateOperation
  {
    private Envelope env;
    private boolean isEdited = false;
    
    public BoxDeleteVertexOperation(Envelope env)
    {
      this.env = env;
    }
    
    public boolean isEdited() { return isEdited; }
  
    public Coordinate[] edit(Coordinate[] coords,
        Geometry geometry)
    {
      if (isEdited) return coords;
      if (! hasVertexInBox(coords))
        return coords;
      // only delete vertices of first component found
      
      int minLen = 2;
      if (geometry instanceof LinearRing) minLen = 4;
      
      Coordinate[] newPts = new Coordinate[coords.length];
      int newIndex = 0;
      for (int i = 0; i < coords.length; i++) {
        if (! env.contains(coords[i])) {
          newPts[newIndex++] = coords[i];
        }
      }
      Coordinate[] nonNullPts = CoordinateArrays.removeNull(newPts);
      Coordinate[] finalPts = nonNullPts;
      
      // close ring if required
      if (geometry instanceof LinearRing) {
        if (nonNullPts.length > 1 && ! nonNullPts[nonNullPts.length - 1].equals2D(nonNullPts[0])) {
          Coordinate[] ringPts = new Coordinate[nonNullPts.length + 1];
          CoordinateArrays.copyDeep(nonNullPts, 0, ringPts, 0, nonNullPts.length);
          ringPts[ringPts.length-1] = new Coordinate(ringPts[0]);
          finalPts = ringPts;
        }
      }
      
      // don't change if would make geometry invalid
      if (finalPts.length < minLen)
        return coords;

      isEdited = true;
      return finalPts; 
    }
    
    private boolean hasVertexInBox(Coordinate[] coords)
    {
      for (int i = 0; i < coords.length; i++) {
        if (env.contains(coords[i])) {
          return true;
        }
      }
      return false;
    }
  }

  
}
