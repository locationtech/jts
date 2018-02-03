package org.locationtech.jts.operation.union;

import org.locationtech.jts.geom.Geometry;

public interface UnionFunction {
  UnionFunction CLASSIC = new UnionFunction() {
    public Geometry union(Geometry g0, Geometry g1) {
      return g0.union(g1);
    }
  };

  Geometry union(Geometry g0, Geometry g1);
}
