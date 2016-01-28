/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2016 Vivid Solutions
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 *
 */
package org.locationtech.jtstest.function;

import java.util.*;
import org.locationtech.jts.algorithm.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.*;
import org.locationtech.jts.densify.*;
import org.locationtech.jts.dissolve.LineDissolver;
import org.locationtech.jts.operation.polygonize.*;
import org.locationtech.jts.operation.linemerge.*;

public class LineHandlingFunctions {

  public static Geometry mergeLines(Geometry g)
  {
    LineMerger merger = new LineMerger();
    merger.add(g);
    Collection lines = merger.getMergedLineStrings();
    return g.getFactory().buildGeometry(lines);
  }

  public static Geometry sequenceLines(Geometry g)
  {
    return LineSequencer.sequence(g);
  }

  public static Geometry extractLines(Geometry g)
  {
    List lines = LinearComponentExtracter.getLines(g);
    return g.getFactory().buildGeometry(lines);
  }
  public static Geometry extractSegments(Geometry g)
  {
    List lines = LinearComponentExtracter.getLines(g);
    List segments = new ArrayList();
    for (Iterator it = lines.iterator(); it.hasNext(); ) {
      LineString line = (LineString) it.next();
      for (int i = 1; i < line.getNumPoints(); i++) {
        LineString seg = g.getFactory().createLineString(
            new Coordinate[] { line.getCoordinateN(i-1), line.getCoordinateN(i) }
          );
        segments.add(seg);
      }
    }
    return g.getFactory().buildGeometry(segments);
  }
  public static Geometry extractChains(Geometry g, int maxChainSize)
  {
    List lines = LinearComponentExtracter.getLines(g);
    List chains = new ArrayList();
    for (Iterator it = lines.iterator(); it.hasNext(); ) {
      LineString line = (LineString) it.next();
      for (int i = 0; i < line.getNumPoints() - 1; i += maxChainSize) {
        LineString chain = extractChain(line, i, maxChainSize);
        chains.add(chain);
      }
    }
    return g.getFactory().buildGeometry(chains);
  }

  private static LineString extractChain(LineString line, int index, int maxChainSize)
  {
    int size = maxChainSize + 1;
    if (index + size > line.getNumPoints())
      size = line.getNumPoints() - index;
    Coordinate[] pts = new Coordinate[size];
    for (int i = 0; i < size; i++) {
      pts[i] = line.getCoordinateN(index + i);
    }
    return line.getFactory().createLineString(pts);
  }

  public static Geometry dissolve(Geometry geom)
  {
    return LineDissolver.dissolve(geom);
  }

}
