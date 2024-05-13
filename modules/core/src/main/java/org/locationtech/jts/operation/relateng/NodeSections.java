/*
 * Copyright (c) 2024 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.relateng;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

class NodeSections {

  private Coordinate nodePt;
  
  private List<NodeSection> sections = new ArrayList<NodeSection>();;

  public NodeSections(Coordinate pt) {
    this.nodePt = pt;
  }

  public Coordinate getCoordinate() {
    return nodePt;
  }

  public void addNodeSection(NodeSection e) {
//System.out.println(e);
    sections.add(e);
  }
  
  public boolean hasInteractionAB() {
    boolean isA = false;
    boolean isB = false;
    for (NodeSection ns : sections) {
      if (ns.isA())
        isA = true;
      else 
        isB = true;
      if (isA && isB)
        return true;
    }
    return false;
  }


  public Geometry getPolygonal(boolean isA) {
    for (NodeSection ns : sections) {
      if (ns.isA() == isA) {
        Geometry poly = ns.getPolygonal();
        if (poly != null)
          return poly;
      }
    }
    return null;
  }
  
  public RelateNode createNode() {
    prepareSections();
    
    RelateNode node = new RelateNode(nodePt);
    int i = 0;
    while (i < sections.size()) {
      int blockSize = 1;
      NodeSection ns = sections.get(i);
      //-- if there multiple polygon sections incident at node convert them to maximal-ring structure 
      if (ns.isArea() && hasMultiplePolygonSections(sections, i)) {
        List<NodeSection> polySections = collectPolygonSections(sections, i);
        List<NodeSection> nsConvert = PolygonNodeConverter.convert(polySections);
        node.addEdges(nsConvert);
        blockSize = polySections.size();
      }
      else {
        //-- the most common case is a line or a single polygon ring section
        node.addEdges(ns);
      }
      i += blockSize;
    }
    return node;
  }

  private void prepareSections() {
    /**
     * Sort sections with lines before areas,
     * and edges for polygons are together.
     */
    sections.sort(null);
    //TODO: remove duplicate sections
  }

  private static boolean hasMultiplePolygonSections(List<NodeSection> sections, int i) {
    //-- if last section can only be one
    if (i >= sections.size() - 1)
      return false;
    //-- check if there are at least two sections for same polygon
    NodeSection ns = sections.get(i);
    NodeSection nsNext = sections.get(i + 1);
    return ns.isSamePolygon(nsNext);
  }
  
  private static List<NodeSection> collectPolygonSections(List<NodeSection> sections, int i) {
    List<NodeSection> polySections = new ArrayList<NodeSection>();
    //-- note ids are only unique to a geometry
    NodeSection polySection = sections.get(i);
    while (i < sections.size() &&
        polySection.isSamePolygon(sections.get(i))) {
      polySections.add(sections.get(i));
      i++;
    }
    return polySections;
  }

}
