

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

import java.util.ArrayList;
import java.util.List;

/**
 * Models a collection of {@link Polygon}s.
 * <p>
 * As per the OGC SFS specification,
 * the Polygons in a MultiPolygon may not overlap,
 * and may only touch at single points.
 * This allows the topological point-set semantics
 * to be well-defined.
 *
 *
 *@version 1.7
 */
public class MultiPolygon<T,G extends Polygon<T>>
	extends GeometryCollection<T,G>
	implements Polygonal
{
  private static final long serialVersionUID = -551033529766975875L;
  /**
   *  Constructs a <code>MultiPolygon</code>.
   *
   *@param  polygons        the <code>Polygon</code>s for this <code>MultiPolygon</code>
   *      , or <code>null</code> or an empty array to create the empty geometry.
   *      Elements may be empty <code>Polygon</code>s, but not <code>null</code>
   *      s. The polygons must conform to the assertions specified in the <A
   *      HREF="http://www.opengis.org/techno/specs.htm">OpenGIS Simple Features
   *      Specification for SQL</A> .
   *@param  precisionModel  the specification of the grid of allowable points
   *      for this <code>MultiPolygon</code>
   *@param  SRID            the ID of the Spatial Reference System used by this
   *      <code>MultiPolygon</code>
   * @deprecated Use GeometryFactory instead
   */
  public MultiPolygon(G[] polygons, PrecisionModel precisionModel, int SRID) {
    this(polygons, new GeometryFactory<>(precisionModel, SRID));
  }


  /**
   * @param polygons
   *            the <code>Polygon</code>s for this <code>MultiPolygon</code>,
   *            or <code>null</code> or an empty array to create the empty
   *            geometry. Elements may be empty <code>Polygon</code>s, but
   *            not <code>null</code>s. The polygons must conform to the
   *            assertions specified in the <A
   *            HREF="http://www.opengis.org/techno/specs.htm">OpenGIS Simple
   *            Features Specification for SQL</A>.
   */
  public MultiPolygon(G[] polygons, GeometryFactory<T> factory) {
    super(polygons, factory);
  }

  public int getDimension() {
    return 2;
  }

  public int getBoundaryDimension() {
    return 1;
  }

  public String getGeometryType() {
    return Geometry.TYPENAME_MULTIPOLYGON;
  }

  /*
  public boolean isSimple() {
    return true;
  }
*/

  /**
   * Computes the boundary of this geometry
   *
   * @return a lineal geometry (which may be empty)
   * @see Geometry#getBoundary
   */
  public Geometry<T> getBoundary() {
    if (isEmpty()) {
      return getFactory().createMultiLineString();
    }
    List<Geometry<T>> allRings = new ArrayList<>();
    for (Geometry<T> geometry : geometries) {
      Geometry<T> polygon = geometry;
      Geometry<T> rings = polygon.getBoundary();
      for (int j = 0; j < rings.getNumGeometries(); j++) {
        allRings.add(rings.getGeometryN(j));
      }
    }
    @SuppressWarnings("unchecked")
    LineString<T>[] allRingsArray = new LineString[allRings.size()];
    return getFactory().createMultiLineString(allRings.toArray(allRingsArray));
  }

  public boolean equalsExact(Geometry<?> other, double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    return super.equalsExact(other, tolerance);
  }

  /**
   * Creates a {@link MultiPolygon} with
   * every component reversed.
   * The order of the components in the collection are not reversed.
   *
   * @return a MultiPolygon in the reverse order
   */
  public MultiPolygon<T,G> reverse() {
    return (MultiPolygon<T,G>) super.reverse();
  }
  @SuppressWarnings("unchecked")
  protected MultiPolygon<T,G> reverseInternal() {
    G[] polygons = (G[]) new Polygon[this.geometries.length];
    for (int i = 0; i < polygons.length; i++) {
      polygons[i] = (G) this.geometries[i].reverse();
    }
    return new MultiPolygon<>(polygons, factory);
  }
  @SuppressWarnings("unchecked")

  protected MultiPolygon<T,G> copyInternal() {
    G[] polygons = (G[]) new Polygon[this.geometries.length];
    for (int i = 0; i < polygons.length; i++) {
      polygons[i] = (G) this.geometries[i].copy();
    }
    return new MultiPolygon<>(polygons, factory);
  }

  protected int getTypeCode() {
    return Geometry.TYPECODE_MULTIPOLYGON;
  }
}


