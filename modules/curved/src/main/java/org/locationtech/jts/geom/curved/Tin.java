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

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

/** A {@link PolyhedralSurface} whose patches are all triangles (TIN). */
public class Tin extends PolyhedralSurface {
  private static final long serialVersionUID = 1L;

  public Tin(Polygon[] patches, GeometryFactory factory) {
    super(patches, factory);
  }

  @Override
  public String getGeometryType() {
    return "Tin";
  }

  @Override
  protected Tin copyInternal() {
    int n = getNumGeometries();
    Polygon[] patches = new Polygon[n];
    for (int i = 0; i < n; i++) {
      patches[i] = (Polygon) getGeometryN(i).copy();
    }
    return new Tin(patches, getFactory());
  }
}
