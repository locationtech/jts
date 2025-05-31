/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
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
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
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
public class GeometryPartDeleter 
{
  public static Geometry deleteComponentsAndVertices(Geometry geom, 
      Envelope env)
  {
    return deleteComponentsAndVertices(geom, env, false);
  }
  
  public static Geometry deleteComponentsAndVertices(Geometry geom, 
      Envelope env, boolean deleteIntersectingComponents)
  {
    Geometry gComp = deleteComponents(geom, env, deleteIntersectingComponents);
    if (gComp != geom) return gComp;
    
    // if deleting by intersection, don't continue to delete vertices
    if (deleteIntersectingComponents) return geom;
    
    // otherwise, try and edit vertices
    Geometry gVert = deleteVertices(geom, env);
    if (gVert != geom) return gVert;

    // no edits - return original
    return geom;
  }
  
  public static Geometry deleteComponents(Geometry geom, Envelope env, boolean deleteIntersecting)
  {
    GeometryEditor editor = new GeometryEditor();
    BoxDeleteComponentOperation compOp = new BoxDeleteComponentOperation(env, deleteIntersecting);
    Geometry compEditGeom = editor.edit(geom, compOp);
    if (compOp.isEdited()) return compEditGeom;
    return geom;
  }
  
  public static Geometry deleteVertices(Geometry geom, Envelope env)
  {
    GeometryEditor editor = new GeometryEditor();
    BoxDeleteVertexOperation vertexOp = new BoxDeleteVertexOperation(env);
    Geometry vertexEditGeom = editor.edit(geom, vertexOp);
    if (vertexOp.isEdited()) return vertexEditGeom;
    return geom;
  }
  
  private static class BoxDeleteComponentOperation
    implements GeometryEditor.GeometryEditorOperation
  {
    private Envelope env;
    private boolean isEdited = false;
    private boolean deleteIntersecting;
    private PreparedGeometry envPrepGeom;
    
    public BoxDeleteComponentOperation(Envelope env)
    {
      this(env, false);
    }
    
    public BoxDeleteComponentOperation(Envelope env, boolean deleteIntersecting)
    {
      this.env = env;
      this.deleteIntersecting = deleteIntersecting;
    }
    
    public boolean isEdited() { return isEdited; }

    public Geometry edit(Geometry geometry, GeometryFactory factory)
    {
      // Allow any number of components to be deleted
      //if (isEdited) return geometry;
      
      // only edit individual components
      if (geometry.getNumGeometries() > 1) return geometry;
      
      boolean isDeleted = false;
      if (deleteIntersecting) {
        isDeleted = getEnvelopeGeometry(factory).intersects(geometry);
      }
      else {
        isDeleted = env.contains(geometry.getEnvelopeInternal());
      }
          
      if (isDeleted) {
          isEdited = true;
          return null;
      }
      return geometry;
    }
    
    private PreparedGeometry getEnvelopeGeometry(GeometryFactory geomFactory) {
      if (envPrepGeom == null) {
        Geometry envGeom = geomFactory.toGeometry(env);
        envPrepGeom = PreparedGeometryFactory.prepare(envGeom);
      }
      return envPrepGeom;
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
