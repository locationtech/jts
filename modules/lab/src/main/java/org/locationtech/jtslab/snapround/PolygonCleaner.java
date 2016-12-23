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
package org.locationtech.jtslab.snapround;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jtslab.geom.util.GeometryEditorEx;
import org.locationtech.jtslab.geom.util.GeometryEditorEx.GeometryEditorOperation;

public class PolygonCleaner implements GeometryEditorOperation {

  public static Geometry clean(Geometry geom) {
    GeometryEditorEx editor = new GeometryEditorEx(new PolygonCleaner());
    return editor.edit(geom);
  }
  
  @Override
  public Geometry edit(Geometry geometry, GeometryFactory targetFactory) {
    if (geometry instanceof Polygonal) {
      return geometry.buffer(0);
    }
    return geometry;
  }

}
