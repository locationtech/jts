/*
 * Copyright (c) 2026 grootstebozewolf
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom.curved;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 * A planar triangle as defined by OGC SFA / ISO 19125-2: a {@link Polygon}
 * with a single 4-point closed exterior ring and no holes.
 * <p>
 * Note: the unrelated static-utility class
 * {@code org.locationtech.jts.geom.Triangle} (centroid, circumradius, etc.)
 * is preserved unchanged in jts-core. The geometry type lives here in the
 * curved package to avoid that name collision.
 */
public class Triangle extends Polygon implements Linearizable {
  private static final long serialVersionUID = 1L;

  public Triangle(LinearRing shell, GeometryFactory factory) {
    super(shell, null, factory);
  }

  public Triangle(GeometryFactory factory) {
    super(null, null, factory);
  }

  @Override
  public String getGeometryType() {
    return "Triangle";
  }

  @Override
  protected Triangle copyInternal() {
    GeometryFactory f = getFactory();
    if (isEmpty()) return new Triangle(f);
    return new Triangle((LinearRing) getExteriorRing().copy(), f);
  }

  @Override
  public Geometry toLinear(double tolerance) {
    GeometryFactory f = getFactory();
    if (isEmpty()) return f.createPolygon();
    return f.createPolygon((LinearRing) getExteriorRing().copy());
  }
}
