/*
 * Copyright (c) 2023 Martin Davis.
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
import org.locationtech.jts.geom.Dimension;

/**
 * Converts the node sections at a polygon node where
 * a shell and one or more holes touch, or two or more holes touch.
 * This converts the node topological structure from
 * the OGC "touching-rings" (AKA "minimal-ring") model to the equivalent "self-touch"
 * (AKA "inverted/exverted ring" or "maximal ring") model.
 * In the "self-touch" model the converted NodeSection corners enclose areas 
 * which all lies inside the polygon
 * (i.e. they does not enclose hole edges).
 * This allows {@link RelateNode} to use simple area-additive semantics 
 * for adding edges and propagating edge locations.
 * <p>
 * The input node sections are assumed to have canonical orientation
 * (CW shells and CCW holes).
 * The arrangement of shells and holes must be topologically valid.
 * Specifically, the node sections must not cross or be collinear.
 * <p>
 * This supports multiple shell-shell touches 
 * (including ones containing holes), and hole-hole touches, 
 * This generalizes the relate algorithm to support
 * both the OGC model and the self-touch model.
 * 
 * @author Martin Davis
 * @see RelateNode
 */
class PolygonNodeConverter {
  
  /**
   * Converts a list of sections of valid polygon rings
   * to have "self-touching" structure.
   * There are the same number of output sections as input ones.
   * 
   * @param polySections the original sections
   * @return the converted sections
   */
  public static List<NodeSection> convert(List<NodeSection> polySections) {
    polySections.sort(new NodeSection.EdgeAngleComparator());
    
    //TODO: move uniquing up to caller
    List<NodeSection> sections = extractUnique(polySections);
    if (sections.size() == 1)
      return sections;
    
    //-- find shell section index
    int shellIndex = findShell(sections);
    if (shellIndex < 0) {
      return convertHoles(sections);
    }
    //-- at least one shell is present.  Handle multiple ones if present
    List<NodeSection> convertedSections = new ArrayList<NodeSection>();
    int nextShellIndex = shellIndex;
    do {
      nextShellIndex = convertShellAndHoles(sections, nextShellIndex, convertedSections);
    } while (nextShellIndex != shellIndex);
    
    return convertedSections;
  }

  private static int convertShellAndHoles(List<NodeSection> sections, int shellIndex, 
      List<NodeSection> convertedSections) {
    NodeSection shellSection = sections.get(shellIndex);
    Coordinate inVertex = shellSection.getVertex(0);
    int i = next(sections, shellIndex);
    NodeSection holeSection = null;
    while (! sections.get(i).isShell()) {
      holeSection = sections.get(i);
      // Assert: holeSection.isShell() = false
      Coordinate outVertex = holeSection.getVertex(1);
      NodeSection ns = createSection(shellSection, inVertex, outVertex);
      convertedSections.add(ns);
      
      inVertex = holeSection.getVertex(0);
      i = next(sections, i);
    }
    //-- create final section for corner from last hole to shell
    Coordinate outVertex = shellSection.getVertex(1);
    NodeSection ns = createSection(shellSection, inVertex, outVertex);
    convertedSections.add(ns);
    return i;
  }

  private static List<NodeSection> convertHoles(List<NodeSection> sections) {
    List<NodeSection> convertedSections = new ArrayList<NodeSection>();
    NodeSection copySection = sections.get(0);
    for (int i = 0; i < sections.size(); i++) {
      int inext = next(sections, i);
      Coordinate inVertex = sections.get(i).getVertex(0);
      Coordinate outVertex = sections.get(inext).getVertex(1);
      NodeSection ns = createSection(copySection, inVertex, outVertex);
      convertedSections.add(ns);
    }
    return convertedSections;
  }
  
  private static NodeSection createSection(NodeSection ns, Coordinate v0, Coordinate v1) {
    return new NodeSection(ns.isA(), 
        Dimension.A, ns.id(), 0, ns.getPolygonal(), 
        ns.isNodeAtVertex(),
        v0, ns.nodePt(), v1);
  }

  private static List<NodeSection> extractUnique(List<NodeSection> sections) {
    List<NodeSection> uniqueSections = new ArrayList<NodeSection>();
    NodeSection lastUnique = sections.get(0);
    uniqueSections.add(lastUnique);
    for (NodeSection ns : sections) {
      if (0 != lastUnique.compareTo(ns)) {
        uniqueSections.add(ns);
        lastUnique = ns;
      }
    }
    return uniqueSections;
  }

  private static int next(List<NodeSection> ns, int i) {
    int next = i + 1;
    if (next >= ns.size())
      next = 0;
    return next;
  }

  private static int findShell(List<NodeSection> polySections) {
    for (int i = 0; i < polySections.size(); i++) {
      if (polySections.get(i).isShell())
        return i;
    }
    return -1;
  }
}
