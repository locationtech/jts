/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.triangulate.tri;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.WKTWriter;

/**
 * Represents an edge in a {@link Tri}, 
 * to be used as a key for looking up Tris
 * while building a triangulation.
 * The edge value is normalized to allow lookup
 * of adjacent triangles.
 * 
 * @author mdavis
 *
 */
class TriEdge {
  public Coordinate p0;
  public Coordinate p1;

  public TriEdge(Coordinate a, Coordinate b) {
    p0 = a;
    p1 = b;
    normalize();
  }

  private void normalize() {
    if ( p0.compareTo(p1) < 0 ) {
      Coordinate tmp = p0;
      p0 = p1;
      p1 = tmp;
    }
  }
  
  @Override
  public int hashCode() {
    int result = 17;
    result = 37 * result + Coordinate.hashCode(p0.x);
    result = 37 * result + Coordinate.hashCode(p1.x);
    result = 37 * result + Coordinate.hashCode(p0.y);
    result = 37 * result + Coordinate.hashCode(p1.y);
    return result;
  }

  @Override
  public boolean equals(Object arg) {
    if ( !(arg instanceof TriEdge) )
      return false;
    TriEdge other = (TriEdge) arg;
    if ( p0.equals(other.p0) && p1.equals(other.p1) )
      return true;
    return false;
  }
  
  public String toString() {
    return WKTWriter.toLineString(new Coordinate[] { p0, p1});
  }
}
