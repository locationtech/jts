/*
 * Copyright (c) 2025 Martin Davis.
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
import java.util.Collections;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.geomfunction.Metadata;

public class MetricFunctions {
  
  /**
   * Returns a line graph of segment lengths.
   * Graph is scaled to maximum segment length.
   * 
   * @param geom geometry to sample
   * @param numSamples number of points in line graph
   * @return line graph of segment lengths
   */
  public static Geometry segmentLengths(final Geometry geom, 
      @Metadata(title="# of samples")
      int numSamples) {
    
    if (numSamples < 1)
      numSamples = 1;
    
    List<Double> segLen = new ArrayList<Double>();
    CoordinateSequenceFilter segLenFilter = new CoordinateSequenceFilter() {

      @Override
      public void filter(CoordinateSequence seq, int i) {
        if (i == 0) {
          segLen.add(0.0);
          return;
        }
        Coordinate p0 = seq.getCoordinate(i);
        Coordinate p1 = seq.getCoordinate(i-1);
        double len = p0.distance(p1);
        segLen.add(len);
      }

      @Override
      public boolean isDone() {
        return false;
      }

      @Override
      public boolean isGeometryChanged() {
        return false;
      }
      
    };
    geom.apply(segLenFilter);
    Collections.sort(segLen);
    
    double maxLen = segLen.get(segLen.size() - 1);
    Coordinate[] pts = new Coordinate[numSamples + 1];
    int breakSize = segLen.size() / numSamples + 1;
    double dx = maxLen / numSamples;
    for (int i = 0; i < numSamples + 1; i++) {
      
      double x = (i >= numSamples) ? maxLen : i * dx;
      
      int sampleIndex = i * breakSize; 
      if (sampleIndex >= segLen.size())
        sampleIndex = segLen.size() - 1;
      double y = segLen.get(sampleIndex);
      pts[i] = new Coordinate(x, y);
    }
    
    return geom.getFactory().createLineString(pts);
  }
}
