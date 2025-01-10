/*
 * Copyright (c) 2023 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtstest.testbuilder.model;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class ListGeometryContainer implements GeometryContainer {

  private List<Geometry> geomList = new ArrayList<Geometry>();
  private Geometry cache;

  public ListGeometryContainer() {
  }

  public void add(Geometry geom) {
    geomList.add(geom);
    cache = null;
  }

  @Override
  public void clear() {
    cache = null;
    geomList.clear();
  }

  @Override
  public Geometry getGeometry() {
    if ( cache == null ) {
      cache = createCache(geomList);
    }
    return cache;
  }

  private static Geometry createCache(List<Geometry> geomList) {
    if (geomList.size() == 0)
      return null;
    if ( geomList.size() == 1 ) {
      return geomList.get(0);
    }
    // TODO: use common TestBuilder factory
    GeometryFactory geomFact = new GeometryFactory();
    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(geomList));
  }

}
