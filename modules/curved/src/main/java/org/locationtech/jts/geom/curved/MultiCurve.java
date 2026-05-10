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
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

/**
 * A collection of {@link LineString}, {@link CircularString} and
 * {@link CompoundCurve} members.
 */
public class MultiCurve extends MultiLineString implements Linearizable {
  private static final long serialVersionUID = 1L;

  public MultiCurve(LineString[] members, GeometryFactory factory) {
    super(members, factory);
  }

  @Override
  public String getGeometryType() {
    return "MultiCurve";
  }

  @Override
  protected MultiCurve copyInternal() {
    int n = getNumGeometries();
    LineString[] members = new LineString[n];
    for (int i = 0; i < n; i++) {
      members[i] = (LineString) getGeometryN(i).copy();
    }
    return new MultiCurve(members, getFactory());
  }

  @Override
  public Geometry toLinear(double tolerance) {
    GeometryFactory f = getFactory();
    int n = getNumGeometries();
    LineString[] linearMembers = new LineString[n];
    for (int i = 0; i < n; i++) {
      Geometry m = getGeometryN(i);
      if (m instanceof Linearizable) {
        linearMembers[i] = (LineString) ((Linearizable) m).toLinear(tolerance);
      } else {
        linearMembers[i] = (LineString) m.copy();
      }
    }
    return f.createMultiLineString(linearMembers);
  }
}
