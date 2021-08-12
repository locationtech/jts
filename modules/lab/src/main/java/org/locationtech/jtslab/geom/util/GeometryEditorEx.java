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
package org.locationtech.jtslab.geom.util;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryTransformer;
import org.locationtech.jts.util.Assert;


/**
 * A class which supports creating new {@link Geometry}s 
 * which are modifications of existing ones,
 * maintaining the same type structure.
 * Geometry objects are intended to be treated as immutable.
 * This class "modifies" Geometry instances
 * by traversing them, applying a user-defined
 * {@link GeometryEditorOperation}, {@link CoordinateSequenceOperation} or {@link CoordinateOperation}  
 * and creating new Geometry instances with the same structure but
 * (possibly) modified components.
 * <p>
 * Examples of the kinds of modifications which can be made are:
 * <ul>
 * <li>the values of the coordinates may be changed.
 *     The editor does not check whether changing coordinate values makes the result Geometry invalid
 * <li>the coordinate lists may be changed
 *     (e.g. by adding, deleting or modifying coordinates).
 *     The modifed coordinate lists must be consistent with their original parent component
 *     (e.g. a <tt>LinearRing</tt> must always have at least 4 coordinates, and the first and last
 *     coordinate must be equal)
 * <li>components of the original geometry may be deleted
 *    (e.g. holes may be removed from a Polygon, or LineStrings removed from a MultiLineString).
 *     Deletions will be propagated up the component tree appropriately.
 * </ul>
 * All changes must be consistent with the original Geometry's structure
 * (e.g. a <tt>Polygon</tt> cannot be collapsed into a <tt>LineString</tt>).
 * If changing the structure is required, use a {@link GeometryTransformer}.
 * <p>
 * This class supports creating an edited Geometry
 * using a different <code>GeometryFactory</code> via the {@link org.locationtech.jts.geom.util.GeometryEditor#GeometryEditor(GeometryFactory)}
 * constructor.  
 * Examples of situations where this is required is if the geometry is 
 * transformed to a new SRID and/or a new PrecisionModel.
 * <p>
 * <b>Usage Notes</b>
 * <ul>
 * <li>The resulting Geometry is not checked for validity.
 * If validity needs to be enforced, the new Geometry's 
 * {@link Geometry#isValid} method should be called.
 * <li>By default the UserData of the input geometry is not copied to the result.
 * </ul>
 * 
 * @see GeometryTransformer
 * @see Geometry#isValid
 *
 * @version 1.7
 */
public class GeometryEditorEx
{
  /**
   * The factory used to create the modified Geometry.
   * If <tt>null</tt> the GeometryFactory of the input is used.
   */
  private GeometryFactory targetFactory = null;
  private boolean isUserDataCopied = false;
  private GeometryEditorOperation operation;

  /**
   * Creates a new GeometryEditor object which will create
   * edited {@link Geometry}s with the same {@link GeometryFactory} as the input Geometry.
   */
  public GeometryEditorEx()
  {
    this(new NoOpGeometryOperation());
  }

  /**
   * Creates a new GeometryEditor object which will create
   * edited {@link Geometry}s with the given {@link GeometryFactory}.
   *
   * @param targetFactory the GeometryFactory to create edited Geometries with
   */
  public GeometryEditorEx(GeometryFactory targetFactory)
  {
    this(new NoOpGeometryOperation(), targetFactory);
  }

  /**
   * Creates a GeometryEditor which edits geometries using
   * a given {@link  GeometryOperation}
   * and the same {@link GeometryFactory} as the input Geometry.
   * 
   * @param operation the edit operation to use
   */
  public GeometryEditorEx(GeometryEditorOperation operation)
  {
    this.operation = operation;
  }

  /**
   * Creates a GeometryEditor which edits geometries using
   * a given {@link GeometryOperation}
   * and the given {@link GeometryFactory}.
   * 
   * @param operation the edit operation to use
   * @param targetFactory the GeometryFactory to create  edited Geometrys with
   * 
   */
  public GeometryEditorEx(GeometryEditorOperation operation, GeometryFactory targetFactory)
  {
    this.operation = operation;
    this.targetFactory = targetFactory;
  }


  /**
   * Sets whether the User Data is copied to the edit result.
   * Only the object reference is copied.
   * 
   * @param isUserDataCopied true if the input user data should be copied.
   */
  public void setCopyUserData(boolean isUserDataCopied)
  {
    this.isUserDataCopied = isUserDataCopied;
  }
  
  /**
   * Edit a {@link Geometry}.
   * Clients can create subclasses of {@link GeometryEditorOperation} or
   * {@link CoordinateOperation} to perform required modifications.
   *
   * @param geometry the Geometry to edit
   * @return a new {@link Geometry} which is the result of the editing (which may be empty)
   */
  public Geometry edit(Geometry geometry)
  {
    // nothing to do
    if (geometry == null) return null;
    
    Geometry result = editInternal(geometry);
    if (isUserDataCopied) {
      result.setUserData(geometry.getUserData());
    }
    return result;
  }
  
  private Geometry editInternal(Geometry geometry)
  {
    // if client did not supply a GeometryFactory, use the one from the input Geometry
    if (targetFactory == null)
      targetFactory = geometry.getFactory();

    if (geometry instanceof GeometryCollection) {
      return editGeometryCollection((GeometryCollection) geometry);
    }
    if (geometry instanceof Polygon) {
      return editPolygon((Polygon) geometry);
    }
    if (geometry instanceof Point) {
      return editPoint((Point) geometry);
    }
    if (geometry instanceof LinearRing) {
      return editLinearRing((LinearRing) geometry);
    }
    if (geometry instanceof LineString) {
      return editLineString((LineString) geometry);
    }

    Assert.shouldNeverReachHere("Unsupported Geometry class: " + geometry.getClass().getName());
    return null;
  }

  private Point editPoint(Point geom) {
    Point newGeom = (Point) operation.edit(geom, targetFactory);
    if (newGeom == null) {
      // null return means create an empty one
      newGeom = targetFactory.createPoint((CoordinateSequence) null);
    }
    else if (newGeom == geom) {
     // If geometry was not modified, copy it
      newGeom = (Point) targetFactory.createGeometry(geom);
    }
    return newGeom;
  }
  
  private LineString editLineString(LineString geom) {
    LineString newGeom = (LineString) operation.edit(geom, targetFactory);
    if (newGeom == null) {
      // null return means create an empty one
      newGeom = targetFactory.createLineString((CoordinateSequence) null);
    }
    else if (newGeom == geom) {
     // If geometry was not modified, copy it
      newGeom = (LineString) targetFactory.createGeometry(geom);
    }
    return newGeom;
  }
  
  private LinearRing editLinearRing(LinearRing geom) {
    LinearRing newGeom = (LinearRing) operation.edit(geom, targetFactory);
    if (newGeom == null) {
      // null return means create an empty one
      newGeom = targetFactory.createLinearRing((CoordinateSequence) null);
    }
    else if (newGeom == geom) {
     // If geometry was not modified, copy it
      newGeom = (LinearRing) targetFactory.createGeometry(geom);
    }
    return newGeom;
  }
  
  private Polygon editPolygon(Polygon polygon) {
    Polygon newPolygon = (Polygon) operation.edit(polygon, targetFactory);
    // create one if needed
    if (newPolygon == null) {
      newPolygon = targetFactory.createPolygon((CoordinateSequence) null);
      return newPolygon;
    }
    /**
     * If geometry was modified, return it
     */
    if (newPolygon != polygon) {
      return newPolygon;
    }

    LinearRing shell = (LinearRing) edit(newPolygon.getExteriorRing());
    if (shell == null || shell.isEmpty()) {
      return targetFactory.createPolygon(null, null);
    }

    ArrayList<LinearRing> holes = new ArrayList<>();
    for (int i = 0; i < newPolygon.getNumInteriorRing(); i++) {
      LinearRing hole = (LinearRing) edit(newPolygon.getInteriorRingN(i));
      if (hole == null || hole.isEmpty()) {
        continue;
      }
      holes.add(hole);
    }

    return targetFactory.createPolygon(shell,
                                 (LinearRing[]) holes.toArray(new LinearRing[] {  }));
  }

  private GeometryCollection editGeometryCollection(
      GeometryCollection collection) {
    // first edit the entire collection
    // MD - not sure why this is done - could just check original collection?
    GeometryCollection collectionForType = (GeometryCollection) operation.edit(collection,
        targetFactory);
    
    if (collectionForType != collection) {
      return collectionForType;
    }
    
    // edit the component geometries
    ArrayList<Geometry> geometries = new ArrayList<>();
    for (int i = 0; i < collectionForType.getNumGeometries(); i++) {
      Geometry geometry = edit(collectionForType.getGeometryN(i));
      if (geometry == null || geometry.isEmpty()) {
        continue;
      }
      geometries.add(geometry);
    }

    if (collectionForType.getClass() == MultiPoint.class) {
      return targetFactory.createMultiPoint((Point[]) geometries.toArray(
            new Point[] {  }));
    }
    if (collectionForType.getClass() == MultiLineString.class) {
      return targetFactory.createMultiLineString(geometries.toArray(
            new LineString[] {  }));
    }
    if (collectionForType.getClass() == MultiPolygon.class) {
      return targetFactory.createMultiPolygon(geometries.toArray(
            new Polygon[] {  }));
    }
    return targetFactory.createGeometryCollection(geometries.toArray(
          new Geometry[] {  }));
  }

  /**
   * A interface which specifies an edit operation for Geometries.
   *
   * @version 1.7
   */
  public interface GeometryEditorOperation
  {
    /**
     * Edits a Geometry by returning a new Geometry with a modification.
     * The returned geometry may be:
     * <ul>
     * <li>the input geometry itself
     * The returned Geometry might be the same as the Geometry passed in.
     * It may be <code>null</code> if the geometry is to be deleted.
     *
     * @param geometry the Geometry to modify
     * @param targetFactory the factory with which to construct the modified Geometry
     * (may be different to the factory of the input geometry)
     * @return a new Geometry which is a modification of the input Geometry
     * @return null if the Geometry is to be deleted completely
     */
    Geometry edit(Geometry geometry,GeometryFactory targetFactory);
  }

  /**
   * A GeometryEditorOperation which does not modify
   * the input geometry.
   * This can be used for simple changes of 
   * GeometryFactory (including PrecisionModel and SRID).
   * 
   * @author mbdavis
   *
   */
  public static class NoOpGeometryOperation
  implements GeometryEditorOperation
  {
  	public Geometry edit(Geometry geometry, GeometryFactory targetFactory)
  	{
  		return geometry;
  	}
  }
  
  /**
   * A {@link GeometryEditorOperation} which edits the coordinate list of a {@link Geometry}.
   * Operates on Geometry subclasses which contains a single coordinate list.
   */
  public abstract static class CoordinateOperation
      implements GeometryEditorOperation
  {
    public final Geometry edit(Geometry geometry, GeometryFactory targetFactory) {
      if (geometry instanceof LinearRing) {
        return targetFactory.createLinearRing(edit(geometry.getCoordinates(),
            geometry));
      }

      if (geometry instanceof LineString) {
        return targetFactory.createLineString(edit(geometry.getCoordinates(),
            geometry));
      }

      if (geometry instanceof Point) {
        Coordinate[] newCoordinates = edit(geometry.getCoordinates(),
            geometry);

        return targetFactory.createPoint((newCoordinates.length > 0)
                                   ? newCoordinates[0] : null);
      }

      return geometry;
    }

    /**
     * Edits the array of {@link Coordinate}s from a {@link Geometry}.
     * <p>
     * If it is desired to preserve the immutability of Geometry instances,
     * if the coordinates are changed a new array should be created
     * and returned.
     *
     * @param coordinates the coordinate array to operate on
     * @param geometry the geometry containing the coordinate list
     * @return an edited coordinate array (which may be the same as the input)
     */
    public abstract Coordinate[] edit(Coordinate[] coordinates,
                                      Geometry geometry);
  }
  
  /**
   * A {@link GeometryEditorOperation} which edits the {@link CoordinateSequence}
   * of a {@link Geometry}.
   * Operates on Geometry subclasses which contains a single coordinate list.
   */
  public abstract static class CoordinateSequenceOperation
      implements GeometryEditorOperation
  {
    public final Geometry edit(Geometry geometry, GeometryFactory targetFactory) {
      if (geometry instanceof LinearRing) {
        return targetFactory.createLinearRing(edit(
            ((LinearRing)geometry).getCoordinateSequence(),
            geometry, targetFactory));
      }

      if (geometry instanceof LineString) {
        return targetFactory.createLineString(edit(
            ((LineString)geometry).getCoordinateSequence(),
            geometry, targetFactory));
      }

      if (geometry instanceof Point) {
        return targetFactory.createPoint(edit(
            ((Point)geometry).getCoordinateSequence(),
            geometry, targetFactory));
      }

      return geometry;
    }

    /**
     * Edits a {@link CoordinateSequence} from a {@link Geometry}.
     *
     * @param coordSeq the coordinate array to operate on
     * @param geometry the geometry containing the coordinate list
     * @return an edited coordinate sequence (which may be the same as the input)
     */
    public abstract CoordinateSequence edit(CoordinateSequence coordSeq,
                                      Geometry geometry, GeometryFactory targetFactory);
  }
}
