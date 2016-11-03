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

package org.locationtech.jts.geom.util;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;

/**
 * A visitor to {@link Geometry} componets, which 
 * allows short-circuiting when a defined condition holds.
 *
 * @version 1.7
 */
public abstract class ShortCircuitedGeometryVisitor
{
  private boolean isDone = false;

  public ShortCircuitedGeometryVisitor() {
  }

  public void applyTo(Geometry geom) {
    for (int i = 0; i < geom.getNumGeometries() && ! isDone; i++) {
      Geometry element = geom.getGeometryN(i);
      if (! (element instanceof GeometryCollection)) {
        visit(element);
        if (isDone()) {
          isDone = true;
          return;
        }
      }
      else
        applyTo(element);
    }
  }

  protected abstract void visit(Geometry element);

  protected abstract boolean isDone();
}
