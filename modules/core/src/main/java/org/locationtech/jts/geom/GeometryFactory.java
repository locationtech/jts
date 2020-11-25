

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
package org.locationtech.jts.geom;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;
import org.locationtech.jts.geom.util.GeometryEditor;
import org.locationtech.jts.util.Assert;

/**
 * Supplies a set of utility methods for building Geometry objects from lists
 * of Coordinates.
 * <p>
 * Note that the factory constructor methods do <b>not</b> change the input coordinates in any way.
 * In particular, they are not rounded to the supplied <tt>PrecisionModel</tt>.
 * It is assumed that input Coordinates meet the given precision.
 * <p>
 * Instances of this class are thread-safe.
 *
 * @version 1.7
 */
public class GeometryFactory<T>
    implements Serializable
{
  private static final long serialVersionUID = -6820524753094095635L;
  private PrecisionModel precisionModel;

  private CoordinateSequenceFactory coordinateSequenceFactory;


  public static <T>Point<T> createPointFromInternalCoord(Coordinate coord, Geometry<T> exemplar)
  {
    exemplar.getPrecisionModel().makePrecise(coord);
    return exemplar.getFactory().createPoint(coord);
  }

  /**
   * Constructs a GeometryFactory that generates Geometries having the given
   * PrecisionModel, spatial-reference ID, and CoordinateSequence implementation.
   */
  public GeometryFactory(PrecisionModel precisionModel, int SRID,
                         CoordinateSequenceFactory coordinateSequenceFactory) {
      this.precisionModel = precisionModel;
      this.coordinateSequenceFactory = coordinateSequenceFactory;
      this.SRID = SRID;
  }

  /**
   * Constructs a GeometryFactory that generates Geometries having the given
   * CoordinateSequence implementation, a double-precision floating PrecisionModel and a
   * spatial-reference ID of 0.
   */
  public GeometryFactory(CoordinateSequenceFactory coordinateSequenceFactory) {
    this(new PrecisionModel(), 0, coordinateSequenceFactory);
  }

  /**
   * Constructs a GeometryFactory that generates Geometries having the given
   * {@link PrecisionModel} and the default CoordinateSequence
   * implementation.
   *
   * @param precisionModel the PrecisionModel to use
   */
  public GeometryFactory(PrecisionModel precisionModel) {
    this(precisionModel, 0, getDefaultCoordinateSequenceFactory());
  }

  /**
   * Constructs a GeometryFactory that generates Geometries having the given
   * {@link PrecisionModel} and spatial-reference ID, and the default CoordinateSequence
   * implementation.
   *
   * @param precisionModel the PrecisionModel to use
   * @param SRID the SRID to use
   */
  public GeometryFactory(PrecisionModel precisionModel, int SRID) {
    this(precisionModel, SRID, getDefaultCoordinateSequenceFactory());
  }

  /**
   * Constructs a GeometryFactory that generates Geometries having a floating
   * PrecisionModel and a spatial-reference ID of 0.
   */
  public GeometryFactory() {
    this(new PrecisionModel(), 0);
  }

  private static CoordinateSequenceFactory getDefaultCoordinateSequenceFactory()
  {
    return CoordinateArraySequenceFactory.instance();
  }

  /**
   *  Converts the <code>List</code> to an array.
   *
   *@param  points  the <code>List</code> of Points to convert
   *@return         the <code>List</code> in array format
   */
  public static<T> Point<T>[] toPointArray(Collection<? extends Point<T>> points) {
    @SuppressWarnings("unchecked")
    Point<T>[] pointArray = new Point[points.size()];
    return points.toArray(pointArray);
  }

  /**
   *  Converts the <code>List</code> to an array.
   *
   *@return            the <code>List</code> in array format
   * @param  geometries  the list of <code>Geometry's</code> to convert
   */

  public static <T,G extends Geometry<T>> G[] toGeometryArray(Collection<? extends Geometry<T>> geometries) {
    if (geometries == null) return null;
    @SuppressWarnings("unchecked")//can't create generic array
    G[] geometryArray = (G[]) new Geometry[geometries.size()];
    return geometries.toArray(geometryArray);
  }

  /**
   *  Converts the <code>List</code> to an array.
   *
   *@param  linearRings  the <code>List</code> of LinearRings to convert
   *@return              the <code>List</code> in array format
   */
  public static <T>LinearRing<T>[] toLinearRingArray(Collection<? extends LinearRing<T>> linearRings) {
    @SuppressWarnings("unchecked")
    LinearRing<T>[] linearRingArray = new LinearRing[linearRings.size()];
    return linearRings.toArray(linearRingArray);
  }

  /**
   *  Converts the <code>List</code> to an array.
   *
   *@param  lineStrings  the <code>List</code> of LineStrings to convert
   *@return              the <code>List</code> in array format
   */
  public static <T> LineString<T>[] toLineStringArray(Collection<? extends LineString<T>> lineStrings) {
    @SuppressWarnings("unchecked")//can't create generic arrays
    LineString<T>[] lineStringArray = new LineString[lineStrings.size()];
    return lineStrings.toArray(lineStringArray);
  }

  /**
   *  Converts the <code>List</code> to an array.
   *
   *@param  polygons  the <code>List</code> of Polygons to convert
   *@return           the <code>List</code> in array format
   */
  public static <T, P extends Polygon<T>> Polygon<T>[] toPolygonArray(Collection<? extends Polygon<T>> polygons) {
    @SuppressWarnings("unchecked")//can't create generic arrays
    Polygon<T>[] polygonArray = (P[]) new Polygon[polygons.size()];
    return polygons.toArray(polygonArray);
  }

  /**
   *  Converts the <code>List</code> to an array.
   *
   *@param  multiPolygons  the <code>List</code> of MultiPolygons to convert
   *@return                the <code>List</code> in array format
   */
  public static <T,G extends Polygon<T>>MultiPolygon<T,G>[] toMultiPolygonArray(Collection<? extends MultiPolygon<T,G>> multiPolygons) {
    @SuppressWarnings("unchecked")
    MultiPolygon<T,G>[] multiPolygonArray = new MultiPolygon[multiPolygons.size()];
    return multiPolygons.toArray(multiPolygonArray);
  }

  /**
   *  Converts the <code>List</code> to an array.
   *
   *@param  multiLineStrings  the <code>List</code> of MultiLineStrings to convert
   *@return                   the <code>List</code> in array format
   */
  public static <T>MultiLineString<T>[] toMultiLineStringArray(Collection<? extends MultiLineString<T>> multiLineStrings) {
    @SuppressWarnings("unchecked")//can't create generic arrays
    MultiLineString<T>[] multiLineStringArray = new MultiLineString[multiLineStrings.size()];
    return multiLineStrings.toArray(multiLineStringArray);
  }

  /**
   *  Converts the <code>List</code> to an array.
   *
   *@param  multiPoints  the <code>List</code> of MultiPoints to convert
   *@return              the <code>List</code> in array format
   */
  public static <T>MultiPoint<T>[] toMultiPointArray(Collection<? extends MultiPoint<T>> multiPoints) {
    @SuppressWarnings("unchecked")
    MultiPoint<T>[] multiPointArray = new MultiPoint[multiPoints.size()];
    return multiPoints.toArray(multiPointArray);
  }

  /**
   * Creates a {@link Geometry} with the same extent as the given envelope.
   * The Geometry returned is guaranteed to be valid.  
   * To provide this behaviour, the following cases occur:
   * <p>
   * If the <code>Envelope</code> is:
   * <ul>
   * <li>null : returns an empty {@link Point}
   * <li>a point : returns a non-empty {@link Point}
   * <li>a line : returns a two-point {@link LineString}
   * <li>a rectangle : returns a {@link Polygon} whose points are (minx, miny),
   *  (minx, maxy), (maxx, maxy), (maxx, miny), (minx, miny).
   * </ul>
   * 
   *@param  envelope the <code>Envelope</code> to convert
   *@return an empty <code>Point</code> (for null <code>Envelope</code>s), 
   *	a <code>Point</code> (when min x = max x and min y = max y) or a
   *      <code>Polygon</code> (in all other cases)
   */
  public Geometry<T> toGeometry(Envelope envelope)
  {
  	// null envelope - return empty point geometry
    if (envelope.isNull()) {
      return createPoint();
    }
    
    // point?
    if (envelope.getMinX() == envelope.getMaxX() && envelope.getMinY() == envelope.getMaxY()) {
      return createPoint(new Coordinate(envelope.getMinX(), envelope.getMinY()));
    }
    
    // vertical or horizontal line?
    if (envelope.getMinX() == envelope.getMaxX()
    		|| envelope.getMinY() == envelope.getMaxY()) {
    	return createLineString(new Coordinate[]{
          new Coordinate(envelope.getMinX(), envelope.getMinY()),
          new Coordinate(envelope.getMaxX(), envelope.getMaxY())
          });
    }

    // create a CW ring for the polygon 
    return createPolygon(createLinearRing(new Coordinate[]{
        new Coordinate(envelope.getMinX(), envelope.getMinY()),
        new Coordinate(envelope.getMinX(), envelope.getMaxY()),
        new Coordinate(envelope.getMaxX(), envelope.getMaxY()),
        new Coordinate(envelope.getMaxX(), envelope.getMinY()),
        new Coordinate(envelope.getMinX(), envelope.getMinY())
        }), null);
  }

  /**
   * Returns the PrecisionModel that Geometries created by this factory
   * will be associated with.
   * 
   * @return the PrecisionModel for this factory
   */
  public PrecisionModel getPrecisionModel() {
    return precisionModel;
  }

  /**
   * Constructs an empty {@link Point} geometry.
   * 
   * @return an empty Point
   */
  public Point<T> createPoint() {
	return createPoint(getCoordinateSequenceFactory().create(new Coordinate[]{}));
  }
  
  /**
   * Creates a Point using the given Coordinate.
   * A null Coordinate creates an empty Geometry.
   * 
   * @param coordinate a Coordinate, or null
   * @return the created Point
   */
  public Point<T> createPoint(Coordinate coordinate) {
    return createPoint(coordinate != null ? getCoordinateSequenceFactory().create(new Coordinate[]{coordinate}) : null);
  }

  /**
   * Creates a Point using the given CoordinateSequence; a null or empty
   * CoordinateSequence will create an empty Point.
   * 
   * @param coordinates a CoordinateSequence (possibly empty), or null
   * @return the created Point
   */
  public Point<T> createPoint(CoordinateSequence coordinates) {
  	return new Point<>(coordinates, this);
  }
  
  /**
   * Constructs an empty {@link MultiLineString} geometry.
   * 
   * @return an empty MultiLineString
   */
  public MultiLineString<T> createMultiLineString() {
    return new MultiLineString<>(null, this);
  }

  /**
   * Creates a MultiLineString using the given LineStrings; a null or empty
   * array will create an empty MultiLineString.
   * 
   * @param lineStrings LineStrings, each of which may be empty but not null
   * @return the created MultiLineString
   */
  public MultiLineString<T> createMultiLineString(LineString<T>[] lineStrings) {
  	return new MultiLineString<>(lineStrings, this);
  }
  
  /**
   * Constructs an empty {@link GeometryCollection} geometry.
   * 
   * @return an empty GeometryCollection
   */
  public <G extends Geometry<T>>GeometryCollection<T,G> createGeometryCollection() {
    return new GeometryCollection<>(null, this);
  }

  /**
   * Creates a GeometryCollection using the given Geometries; a null or empty
   * array will create an empty GeometryCollection.
   * 
   * @param geometries an array of Geometries, each of which may be empty but not null, or null
   * @return the created GeometryCollection
   */
  public <G extends Geometry<T>>GeometryCollection<T,G> createGeometryCollection(G[] geometries) {
  	return new GeometryCollection<>(geometries, this);
  }
  
  /**
   * Constructs an empty {@link MultiPolygon} geometry.
   * 
   * @return an empty MultiPolygon
   */
  public MultiPolygon<T,?> createMultiPolygon() {
    return new MultiPolygon<>(null, this);
  }

  /**
   * Creates a MultiPolygon using the given Polygons; a null or empty array
   * will create an empty Polygon. The polygons must conform to the
   * assertions specified in the <A
   * HREF="http://www.opengis.org/techno/specs.htm">OpenGIS Simple Features
   * Specification for SQL</A>.
   *
   * @param polygons
   *            Polygons, each of which may be empty but not null
   * @return the created MultiPolygon
   */
  public <P extends Polygon<T>>MultiPolygon<T, P> createMultiPolygon(P[] polygons) {
    return new MultiPolygon<>(polygons, this);
  }
  
  /**
   * Constructs an empty {@link LinearRing} geometry.
   * 
   * @return an empty LinearRing
   */
  public LinearRing<T> createLinearRing() {
    return createLinearRing(getCoordinateSequenceFactory().create(new Coordinate[]{}));
  }

  /**
   * Creates a {@link LinearRing} using the given {@link Coordinate}s.
   * A null or empty array creates an empty LinearRing. 
   * The points must form a closed and simple linestring. 
   * @param coordinates an array without null elements, or an empty array, or null
   * @return the created LinearRing
   * @throws IllegalArgumentException if the ring is not closed, or has too few points
   */
  public LinearRing<T> createLinearRing(Coordinate[] coordinates) {
    return createLinearRing(coordinates != null ? getCoordinateSequenceFactory().create(coordinates) : null);
  }

  /**
   * Creates a {@link LinearRing} using the given {@link CoordinateSequence}. 
   * A null or empty array creates an empty LinearRing. 
   * The points must form a closed and simple linestring. 
   * 
   * @param coordinates a CoordinateSequence (possibly empty), or null
   * @return the created LinearRing
   * @throws IllegalArgumentException if the ring is not closed, or has too few points
   */
  public LinearRing<T> createLinearRing(CoordinateSequence coordinates) {
    return new LinearRing<>(coordinates, this);
  }
  
  /**
   * Constructs an empty {@link MultiPoint} geometry.
   * 
   * @return an empty MultiPoint
   */
  public MultiPoint<T> createMultiPoint() {
    return new MultiPoint<>(null, this);
  }

  /**
   * Creates a {@link MultiPoint} using the given {@link Point}s.
   * A null or empty array will create an empty MultiPoint.
   *
   * @param point an array of Points (without null elements), or an empty array, or <code>null</code>
   * @return a MultiPoint object
   */
  public MultiPoint<T> createMultiPoint(Point<T>[] point) {
  	return new MultiPoint<>(point, this);
  }

  /**
   * Creates a {@link MultiPoint} using the given {@link Coordinate}s.
   * A null or empty array will create an empty MultiPoint.
   *
   * @param coordinates an array (without null elements), or an empty array, or <code>null</code>
   * @return a MultiPoint object
   * @deprecated Use {@link GeometryFactory#createMultiPointFromCoords} instead
   */
  public MultiPoint<T> createMultiPoint(Coordinate[] coordinates) {
      return createMultiPoint(coordinates != null
                              ? getCoordinateSequenceFactory().create(coordinates)
                              : null);
  }

  /**
   * Creates a {@link MultiPoint} using the given {@link Coordinate}s.
   * A null or empty array will create an empty MultiPoint.
   *
   * @param coordinates an array (without null elements), or an empty array, or <code>null</code>
   * @return a MultiPoint object
   */
  public MultiPoint<T> createMultiPointFromCoords(Coordinate[] coordinates) {
      return createMultiPoint(coordinates != null
                              ? getCoordinateSequenceFactory().create(coordinates)
                              : null);
  }

  /**
   * Creates a {@link MultiPoint} using the 
   * points in the given {@link CoordinateSequence}.
   * A <code>null</code> or empty CoordinateSequence creates an empty MultiPoint.
   *
   * @param coordinates a CoordinateSequence (possibly empty), or <code>null</code>
   * @return a MultiPoint geometry
   */
  public MultiPoint<T> createMultiPoint(CoordinateSequence coordinates) {
    if (coordinates == null) {
      @SuppressWarnings("unchecked")
      Point<T>[] emptyArr = new Point[0];
      return createMultiPoint(emptyArr);
    }
    @SuppressWarnings("unchecked")
    Point<T>[] points = new Point[coordinates.size()];
    for (int i = 0; i < coordinates.size(); i++) {
      CoordinateSequence ptSeq = getCoordinateSequenceFactory()
        .create(1, coordinates.getDimension(), coordinates.getMeasures());
      CoordinateSequences.copy(coordinates, i, ptSeq, 0, 1);
      points[i] = createPoint(ptSeq);
    }
    return createMultiPoint(points);
  }

  /**
   * Constructs a <code>Polygon</code> with the given exterior boundary and
   * interior boundaries.
   *
   * @param shell
   *            the outer boundary of the new <code>Polygon</code>, or
   *            <code>null</code> or an empty <code>LinearRing</code> if
   *            the empty geometry is to be created.
   * @param holes
   *            the inner boundaries of the new <code>Polygon</code>, or
   *            <code>null</code> or empty <code>LinearRing</code> s if
   *            the empty geometry is to be created.
   * @throws IllegalArgumentException if a ring is invalid
   */
  public Polygon<T> createPolygon(LinearRing<T> shell, LinearRing<T>[] holes) {
    return new Polygon<>(shell, holes, this);
  }

  /**
   * Constructs a <code>Polygon</code> with the given exterior boundary.
   *
   * @param shell
   *            the outer boundary of the new <code>Polygon</code>, or
   *            <code>null</code> or an empty <code>LinearRing</code> if
   *            the empty geometry is to be created.
   * @throws IllegalArgumentException if the boundary ring is invalid
   */
  public Polygon<T> createPolygon(CoordinateSequence shell) {
    return createPolygon(createLinearRing(shell));
  }

  /**
   * Constructs a <code>Polygon</code> with the given exterior boundary.
   *
   * @param shell
   *            the outer boundary of the new <code>Polygon</code>, or
   *            <code>null</code> or an empty <code>LinearRing</code> if
   *            the empty geometry is to be created.
   * @throws IllegalArgumentException if the boundary ring is invalid
   */
  public Polygon<T> createPolygon(Coordinate[] shell) {
    return createPolygon(createLinearRing(shell));
  }

  /**
   * Constructs a <code>Polygon</code> with the given exterior boundary.
   *
   * @param shell
   *            the outer boundary of the new <code>Polygon</code>, or
   *            <code>null</code> or an empty <code>LinearRing</code> if
   *            the empty geometry is to be created.
   * @throws IllegalArgumentException if the boundary ring is invalid
   */
  public Polygon<T> createPolygon(LinearRing<T> shell) {
    return createPolygon(shell, null);
  }
  
  /**
   * Constructs an empty {@link Polygon} geometry.
   * 
   * @return an empty polygon
   */
  public Polygon<T> createPolygon() {
    return createPolygon(null, null);
  }

  /**
   *  Build an appropriate <code>Geometry</code>, <code>MultiGeometry</code>, or
   *  <code>GeometryCollection</code> to contain the <code>Geometry</code>s in
   *  it.
   * For example:<br>
   *
   *  <ul>
   *    <li> If <code>geomList</code> contains a single <code>Polygon</code>,
   *    the <code>Polygon</code> is returned.
   *    <li> If <code>geomList</code> contains several <code>Polygon</code>s, a
   *    <code>MultiPolygon</code> is returned.
   *    <li> If <code>geomList</code> contains some <code>Polygon</code>s and
   *    some <code>LineString</code>s, a <code>GeometryCollection</code> is
   *    returned.
   *    <li> If <code>geomList</code> is empty, an empty <code>GeometryCollection</code>
   *    is returned
   *  </ul>
   *
   * Note that this method does not "flatten" Geometries in the input, and hence if
   * any MultiGeometries are contained in the input a GeometryCollection containing
   * them will be returned.
   *
   *@param  geomList  the <code>Geometry</code>s to combine
   *@return           a <code>Geometry</code> of the "smallest", "most
   *      type-specific" class that can contain the elements of <code>geomList</code>
   *      .
   */
  @SuppressWarnings("unchecked")
  public <G extends Geometry<T>> Geometry<T> buildGeometry(Collection<? extends Geometry<T>> geomList) {
  	
  	/**
  	 * Determine some facts about the geometries in the list
  	 */
    Class<?extends Geometry<T>> geomClass = null;
    boolean isHeterogeneous = false;
    boolean hasGeometryCollection = false;
    for (Geometry<T> geom : geomList) {
      Class<? extends Geometry<T>> partClass = (Class<? extends Geometry<T>>) geom.getClass();
      if (geomClass == null) {
        geomClass = partClass;
      }
      if (partClass != geomClass) {
        isHeterogeneous = true;
      }
      if (geom instanceof GeometryCollection)
        hasGeometryCollection = true;
    }
    
    /**
     * Now construct an appropriate geometry to return
     */
    // for the empty geometry, return an empty GeometryCollection
    if (geomClass == null) {
      return createGeometryCollection();
    }
    if (isHeterogeneous || hasGeometryCollection) {
      return createGeometryCollection(toGeometryArray( geomList));
    }
    // at this point we know the collection is hetereogenous.
    // Determine the type of the result from the first Geometry in the list
    // this should always return a geometry, since otherwise an empty collection would have already been returned
    Geometry<T> geom0 =  geomList.iterator().next();
    boolean isCollection = geomList.size() > 1;
    if (isCollection) {
      if (geom0 instanceof Polygon) {
        return (Geometry<T>) createMultiPolygon(toPolygonArray((Collection<? extends Polygon<T>>) geomList));
      }
      else if (geom0 instanceof LineString) {
        return (Geometry<T>) createMultiLineString(toLineStringArray((Collection<? extends LineString<T>>) geomList));
      }
      else if (geom0 instanceof Point) {
        return createMultiPoint(toPointArray((Collection<? extends Point<T>>)geomList));
      }
      Assert.shouldNeverReachHere("Unhandled class: " + geom0.getClass().getName());
    }
    return geom0;
  }
  
  /**
   * Constructs an empty {@link LineString} geometry.
   * 
   * @return an empty LineString
   */
  public LineString<T> createLineString() {
    return createLineString(getCoordinateSequenceFactory().create(new Coordinate[]{}));
  }

  /**
   * Creates a LineString using the given Coordinates.
   * A null or empty array creates an empty LineString. 
   * 
   * @param coordinates an array without null elements, or an empty array, or null
   */
  public LineString<T> createLineString(Coordinate[] coordinates) {
    return createLineString(coordinates != null ? getCoordinateSequenceFactory().create(coordinates) : null);
  }
  /**
   * Creates a LineString using the given CoordinateSequence.
   * A null or empty CoordinateSequence creates an empty LineString. 
   * 
   * @param coordinates a CoordinateSequence (possibly empty), or null
   */
  public LineString<T> createLineString(CoordinateSequence coordinates) {
	return new LineString<>(coordinates, this);
  }

  /**
   * Creates an empty atomic geometry of the given dimension.
   * If passed a dimension of -1 will create an empty {@link GeometryCollection}.
   * 
   * @param dimension the required dimension (-1, 0, 1 or 2)
   * @return an empty atomic geometry of given dimension
   */
  public Geometry<T> createEmpty(int dimension) {
    switch (dimension) {
    case -1: return createGeometryCollection();
    case 0: return createPoint();
    case 1: return createLineString();
    case 2: return createPolygon();
    default:
      throw new IllegalArgumentException("Invalid dimension: " + dimension);
    }
  }
  
  /**
   * Creates a deep copy of the input {@link Geometry}.
   * The {@link CoordinateSequenceFactory} defined for this factory
   * is used to copy the {@link CoordinateSequence}s
   * of the input geometry.
   * <p>
   * This is a convenient way to change the <tt>CoordinateSequence</tt>
   * used to represent a geometry, or to change the 
   * factory used for a geometry.
   * <p>
   * {@link Geometry#copy()} can also be used to make a deep copy,
   * but it does not allow changing the CoordinateSequence type.
   * 
   * @return a deep copy of the input geometry, using the CoordinateSequence type of this factory
   * 
   * @see Geometry#copy() 
   */
  public Geometry<T> createGeometry(Geometry<T> g)
  {
    GeometryEditor editor = new GeometryEditor(this);
    return editor.edit(g, new CoordSeqCloneOp(coordinateSequenceFactory));
  }

  private static class CoordSeqCloneOp extends GeometryEditor.CoordinateSequenceOperation {
    CoordinateSequenceFactory coordinateSequenceFactory;
    public CoordSeqCloneOp(CoordinateSequenceFactory coordinateSequenceFactory) {
      this.coordinateSequenceFactory = coordinateSequenceFactory;
    }
    public CoordinateSequence edit(CoordinateSequence coordSeq, Geometry geometry) {
      return coordinateSequenceFactory.create(coordSeq);
    }
  }

  /**
   * Gets the SRID value defined for this factory.
   * 
   * @return the factory SRID value
   */
  public int getSRID() {
    return SRID;
  }

  private int SRID;

  public CoordinateSequenceFactory getCoordinateSequenceFactory() {
    return coordinateSequenceFactory;
  }

}

