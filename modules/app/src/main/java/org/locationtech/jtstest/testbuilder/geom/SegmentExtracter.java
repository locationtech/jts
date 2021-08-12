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
package org.locationtech.jtstest.testbuilder.geom;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class SegmentExtracter {

  public static Geometry extract(Geometry geom, Geometry aoi) {
    SegmentExtracterFilter filter = new SegmentExtracterFilter(aoi.getEnvelopeInternal());
    geom.apply(filter);
    return filter.getGeometry(geom.getFactory());
  }

  public static class SegmentExtracterFilter implements CoordinateSequenceFilter
  {
    private Envelope aoi;
    List<Coordinate[]> segSeq = new ArrayList<Coordinate[]>();
    CoordinateList coords;
    int lastIndex;

    public SegmentExtracterFilter(Envelope aoi) {
      this.aoi = aoi;
    }

    public Geometry getGeometry(GeometryFactory factory) {
      List<Geometry> lines = new ArrayList<Geometry>();
      for (Coordinate[] pts : segSeq) {
        Geometry line = factory.createLineString(pts);
        lines.add(line);
      }
      if (lines.size() == 1) 
        return lines.get(0);
      return factory.createMultiLineString(GeometryFactory.toLineStringArray(lines));
    }

    @Override
    public void filter(CoordinateSequence seq, int i) {
      if (i == 0) {
        clearCoords();
        return;
      }
      Coordinate p0 = seq.getCoordinate(i-1);
      Coordinate p1 = seq.getCoordinate(i);
      if (aoi.intersects(p0, p1)) {
        addSeg(i, p0, p1);
        //segSeq.add(new Coordinate[] { p0.copy(), p1.copy() });
      }
      if (i == seq.size() - 1) {
        saveCoords();
      }
    }

    private void addSeg(int index, Coordinate p0, Coordinate p1) {
      if (lastIndex < index - 1) {
        saveCoords();
      }
      if (coords == null) {
        coords = new CoordinateList();
      }
      coords.add(p0, false);
      coords.add(p1, false);
      lastIndex = index;
    }

    private void saveCoords() {
      if (coords != null) {
        segSeq.add(coords.toCoordinateArray());
        coords = null;
      }
    }

    private void clearCoords() {
      coords = null;
      lastIndex = 0;
    }

    @Override
    public boolean isDone() {
      return false;
    }

    @Override
    public boolean isGeometryChanged() {
      return false;
    }
    
  }
}
