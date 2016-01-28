/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Martin Davis
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Martin Davis BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package org.locationtech.jts.operation.distance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryComponentFilter;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.index.strtree.STRtree;


public class FacetSequenceTreeBuilder {
  // 6 seems to be a good facet sequence size
  private static final int FACET_SEQUENCE_SIZE = 6;

  // Seems to be better to use a minimum node capacity
  private static final int STR_TREE_NODE_CAPACITY = 4;

  public static STRtree build(Geometry g) {
    STRtree tree = new STRtree(STR_TREE_NODE_CAPACITY);
    List sections = computeFacetSequences(g);
    for (Iterator i = sections.iterator(); i.hasNext();) {
      FacetSequence section = (FacetSequence) i.next();
      tree.insert(section.getEnvelope(), section);
    }
    tree.build();
    return tree;
  }

  /**
   * Creates facet sequences
   * 
   * @param g
   * @return List<GeometryFacetSequence>
   */
  private static List computeFacetSequences(Geometry g) {
    final List sections = new ArrayList();

    g.apply(new GeometryComponentFilter() {

      public void filter(Geometry geom) {
        CoordinateSequence seq = null;
        if (geom instanceof LineString) {
          seq = ((LineString) geom).getCoordinateSequence();
          addFacetSequences(seq, sections);
        }
        else if (geom instanceof Point) {
          seq = ((Point) geom).getCoordinateSequence();
          addFacetSequences(seq, sections);
        }
      }
    });
    return sections;
  }

  private static void addFacetSequences(CoordinateSequence pts, List sections) {
    int i = 0;
    int size = pts.size();
    while (i <= size - 1) {
      int end = i + FACET_SEQUENCE_SIZE + 1;
      // if only one point remains after this section, include it in this
      // section
      if (end >= size - 1)
        end = size;
      FacetSequence sect = new FacetSequence(pts, i, end);
      sections.add(sect);
      i = i + FACET_SEQUENCE_SIZE;
    }
  }
}
