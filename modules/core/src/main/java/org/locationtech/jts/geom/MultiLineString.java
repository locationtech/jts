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
package org.locationtech.jts.geom;

import org.locationtech.jts.operation.BoundaryOp;

/**
 * Models a collection of (@link LineString}s.
 * <p>
 * Any collection of LineStrings is a valid MultiLineString.
 *
 *@version 1.7
 */
public class MultiLineString 
	extends GeometryCollection
	implements Lineal
	{
  private static final long serialVersionUID = 8166665132445433741L;
  /**
   *  Constructs a <code>MultiLineString</code>.
   *
   *@param  lineStrings     the <code>LineString</code>s for this <code>MultiLineString</code>
   *      , or <code>null</code> or an empty array to create the empty geometry.
   *      Elements may be empty <code>LineString</code>s, but not <code>null</code>
   *      s.
   *@param  precisionModel  the specification of the grid of allowable points
   *      for this <code>MultiLineString</code>
   *@param  SRID            the ID of the Spatial Reference System used by this
   *      <code>MultiLineString</code>
   * @deprecated Use GeometryFactory instead
   */
  public MultiLineString(LineString[] lineStrings, PrecisionModel precisionModel, int SRID) {
    super(lineStrings, new GeometryFactory(precisionModel, SRID));
  }



  /**
   * @param lineStrings
   *            the <code>LineString</code>s for this <code>MultiLineString</code>,
   *            or <code>null</code> or an empty array to create the empty
   *            geometry. Elements may be empty <code>LineString</code>s,
   *            but not <code>null</code>s.
   */
  public MultiLineString(LineString[] lineStrings, GeometryFactory factory) {
    super(lineStrings, factory);
  }

  public int getDimension() {
    return 1;
  }

  public int getBoundaryDimension() {
    if (isClosed()) {
      return Dimension.FALSE;
    }
    return 0;
  }

  public String getGeometryType() {
    return "MultiLineString";
  }

  public boolean isClosed() {
    if (isEmpty()) {
      return false;
    }
    for (int i = 0; i < geometries.length; i++) {
      if (!((LineString) geometries[i]).isClosed()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the boundary of this geometry.
   * The boundary of a lineal geometry is always a zero-dimensional geometry (which may be empty).
   *
   * @return the boundary geometry
   * @see Geometry#getBoundary
   */
  public Geometry getBoundary()
  {
    return (new BoundaryOp(this)).getBoundary();
  }

  /**
   * Creates a {@link MultiLineString} in the reverse
   * order to this object.
   * Both the order of the component LineStrings
   * and the order of their coordinate sequences
   * are reversed.
   *
   * @return a {@link MultiLineString} in the reverse order
   */
  public Geometry reverse()
  {
    int nLines = geometries.length;
    LineString[] revLines = new LineString[nLines];
    for (int i = 0; i < geometries.length; i++) {
      revLines[nLines - 1 - i] = (LineString)geometries[i].reverse();
    }
    return getFactory().createMultiLineString(revLines);
  }

  public boolean equalsExact(Geometry other, double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    return super.equalsExact(other, tolerance);
  }

}

