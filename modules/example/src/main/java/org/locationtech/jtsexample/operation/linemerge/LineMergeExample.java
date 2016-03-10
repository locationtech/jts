
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
package org.locationtech.jtsexample.operation.linemerge;


import java.util.ArrayList;
import java.util.Collection;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.linemerge.LineMerger;


/**
 * Example of using the LineMerger class to sew together a set of fully noded 
 * linestrings.
 *
 * @version 1.7
 */
public class LineMergeExample {
  private WKTReader reader = new WKTReader();

  public LineMergeExample() {
  }

  public static void main(String[] args) throws Exception {
    LineMergeExample test = new LineMergeExample();
    try {
      test.run();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  void run() throws Exception {
    Collection lineStrings = getData();
    
    LineMerger lineMerger = new LineMerger();
    lineMerger.add(lineStrings);
    Collection mergedLineStrings = lineMerger.getMergedLineStrings();
    
    System.out.println("Lines formed (" + mergedLineStrings.size() + "):");
    System.out.println(mergedLineStrings);
  }

  Collection getData() {
    Collection lines = new ArrayList();
    lines.add(read("LINESTRING (220 160, 240 150, 270 150, 290 170)"));
    lines.add(read("LINESTRING (60 210, 30 190, 30 160)"));
    lines.add(read("LINESTRING (70 430, 100 430, 120 420, 140 400)"));
    lines.add(read("LINESTRING (160 310, 160 280, 160 250, 170 230)"));
    lines.add(read("LINESTRING (170 230, 180 210, 200 180, 220 160)"));
    lines.add(read("LINESTRING (30 160, 40 150, 70 150)"));
    lines.add(read("LINESTRING (160 310, 200 330, 220 340, 240 360)"));
    lines.add(read("LINESTRING (140 400, 150 370, 160 340, 160 310)"));
    lines.add(read("LINESTRING (160 310, 130 300, 100 290, 70 270)"));
    lines.add(read("LINESTRING (240 360, 260 390, 260 410, 250 430)"));
    lines.add(read("LINESTRING (70 150, 100 180, 100 200)"));
    lines.add(read("LINESTRING (70 270, 60 260, 50 240, 50 220, 60 210)"));
    lines.add(read("LINESTRING (100 200, 90 210, 60 210)"));

    return lines;
  }

  Geometry read(String lineWKT) {
    try {
      Geometry geom = reader.read(lineWKT);

      return geom;
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return null;
  }
}
