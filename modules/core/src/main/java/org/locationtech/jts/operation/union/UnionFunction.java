package org.locationtech.jts.operation.union;

import org.locationtech.jts.geom.Geometry;

public interface UnionFunction {

  Geometry union(Geometry g0, Geometry g1);
}
