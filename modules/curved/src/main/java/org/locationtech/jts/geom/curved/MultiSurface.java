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
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

/** A collection of {@link Polygon} and {@link CurvePolygon} members. */
public class MultiSurface extends MultiPolygon implements Linearizable {
  private static final long serialVersionUID = 1L;

  public MultiSurface(Polygon[] members, GeometryFactory factory) {
    super(members, factory);
  }

  @Override
  public String getGeometryType() {
    return "MultiSurface";
  }

  @Override
  protected MultiSurface copyInternal() {
    int n = getNumGeometries();
    Polygon[] members = new Polygon[n];
    for (int i = 0; i < n; i++) {
      members[i] = (Polygon) getGeometryN(i).copy();
    }
    return new MultiSurface(members, getFactory());
  }

  @Override
  public Geometry toLinear(double tolerance) {
    GeometryFactory f = getFactory();
    int n = getNumGeometries();
    Polygon[] linearMembers = new Polygon[n];
    for (int i = 0; i < n; i++) {
      Geometry m = getGeometryN(i);
      if (m instanceof Linearizable) {
        linearMembers[i] = (Polygon) ((Linearizable) m).toLinear(tolerance);
      } else {
        linearMembers[i] = (Polygon) m.copy();
      }
    }
    return f.createMultiPolygon(linearMembers);
  }
}
