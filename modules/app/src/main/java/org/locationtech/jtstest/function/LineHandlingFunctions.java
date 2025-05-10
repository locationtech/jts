/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.dissolve.LineDissolver;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.linemerge.LineSequencer;
import org.locationtech.jtstest.geomfunction.Metadata;

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

  /**
   * Trims line A to geometry B.
   * Equivalent to the projection of B onto A.
   * 
   * @param a line to trim
   * @param b trimming geometry
   * @return line A trimmed to B
   */
  @Metadata(description="Trim line A to geometry B")
  public static Geometry trim(Geometry a, Geometry b) {
    return LinearReferencingFunctions.project(a, b);
  }

}
