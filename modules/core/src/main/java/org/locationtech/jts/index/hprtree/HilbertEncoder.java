/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.index.hprtree;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.shape.fractal.HilbertCode;

public class HilbertEncoder {
  private int level;
  private double minx;
  private double miny;
  private double strideX;
  private double strideY;

  public HilbertEncoder(int level, Envelope extent) {
    this.level = level;
    int hside = (int) Math.pow(2, level) - 1;
    
    minx = extent.getMinX();
    strideX = extent.getWidth() / hside;
    
    miny = extent.getMinY();
    strideY = extent.getHeight() / hside;
  }

  public int encode(Envelope env) {
    double midx = env.getWidth()/2 + env.getMinX();
    int x = (int) ((midx - minx) / strideX);

    double midy = env.getHeight()/2 + env.getMinY();
    int y = (int) ((midy - miny) / strideY);
      
    return HilbertCode.encode(level, x, y);
  }

  /**
   * Sorts a list of {@link Geometry} objects in-place by their spatial order
   * using Hilbert curve encoding of their envelopes.
   *
   * @param geoms the list of geometries to sort
   */
  public static void sort(List<Geometry> geoms) {
	  sort(geoms, 12);
  }
  
  /**
   * Sorts a list of {@link Geometry} objects in-place by their spatial order
   * using Hilbert curve encoding of their envelopes.
   *
   * @param geoms the list of geometries to sort
   * @param level the resolution level for Hilbert curve encoding
   */
  public static void sort(List<Geometry> geoms, int level) {
    int n = geoms.size();
    if (n < 2)
      return;

    Envelope globalExtent = new Envelope();
    for (Geometry g : geoms) {
      globalExtent.expandToInclude(g.getEnvelopeInternal());
    }

    HilbertEncoder encoder = new HilbertEncoder(level, globalExtent);
    int[] keys = new int[n];
    for (int i = 0; i < n; i++) {
      Envelope e = geoms.get(i).getEnvelopeInternal();
      keys[i] = encoder.encode(e);
    }
    sortInPlaceByKeys(keys, geoms);
  }

  private static <T> void sortInPlaceByKeys(int[] keys, List<T> values) {
    final int n = keys.length;

    Integer[] idx = IntStream.range(0, n).boxed().toArray(Integer[]::new);
    Arrays.sort(idx, Comparator.comparingInt(i -> keys[i]));

    // rearrange keys and values in-place by following permutation cycles,
    // so that both arrays are sorted according to hilbert order key.
    boolean[] seen = new boolean[n];
    for (int i = 0; i < n; i++) {
      if (seen[i] || idx[i] == i)
        continue;

      int cycleStart = i;
      int j = i;
      int savedKey = keys[j];
      T savedVal = values.get(j);

      do {
        seen[j] = true;
        int next = idx[j];
        keys[j] = keys[next];
        values.set(j, values.get(next));

        j = next;
      } while (j != cycleStart);

      keys[j] = savedKey;
      values.set(j, savedVal);
      seen[j] = true;
    }
  }

}
