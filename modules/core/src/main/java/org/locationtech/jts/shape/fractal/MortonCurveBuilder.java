/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.shape.fractal;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.shape.GeometricShapeBuilder;

/**
 * Generates Morton (Z) Curve linestrings.
 * 
 * @author Martin Davis
 *
 */
public class MortonCurveBuilder
extends GeometricShapeBuilder
{

  private int order = -1;

  /**
   * Creates a new instance using the provided {@link GeometryFactory}.
   * 
   * @param geomFactory the geometry factory to use
   */
  public MortonCurveBuilder(GeometryFactory geomFactory)
  {
    super(geomFactory);
    // use a null extent to indicate no transformation
    extent = null;
  }

  /**
   * Sets the order of curve to generate.
   * The order must be in the range [0 - 16].
   * If set this determines the 
   * number of points in the generated curve.
   * 
   * @param order the order of the curve
   */
  public void setOrder(int order) {
    this.order = order;
  }
  
  @Override
  public Geometry getGeometry() {
    if (order < 0) {
     //TODO:  compute order from numPts
    }
    else {
      numPts = MortonCurve.size(order);
    }
    
    double scale = 1;
    double baseX = 0;
    double baseY = 0;
    if (extent != null) {
      LineSegment baseLine = getSquareBaseLine();
      baseX = baseLine.minX();
      baseY = baseLine.minY();
      double width = baseLine.getLength();
      int maxOrdinate = HilbertCurve.maxOrdinate(order);
      scale = width / maxOrdinate;
    }
    
    Coordinate[] pts = new Coordinate[numPts];
    for (int i = 0; i < numPts; i++) {
       Coordinate pt = MortonCurve.decode(i);
       double x = transform(pt.getX(), scale, baseX);
       double y = transform(pt.getY(), scale, baseY);
       pts[i] = new Coordinate(x, y);
    }
    return geomFactory.createLineString(pts);
  }
  
  private static double transform(double val, double scale, double offset) {
    return val * scale + offset;
  }

}
