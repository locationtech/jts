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
import org.locationtech.jts.geom.Dimension;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class PolygonNodeConverterTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(PolygonNodeConverterTest.class);
  }
  
  public PolygonNodeConverterTest(String name) {
    super(name);
  }
  
  public void testShells() {
    checkConversion(
        collect(
            sectionShell( 1,1, 5,5, 9,9 ),
            sectionShell( 8,9, 5,5, 6,9 ),
            sectionShell( 4,9, 5,5, 2,9 ) ),
        collect(
            sectionShell( 1,1, 5,5, 9,9 ),
            sectionShell( 8,9, 5,5, 6,9 ),
            sectionShell( 4,9, 5,5, 2,9 ) )
        );
  }

  public void testShellAndHole() {
    checkConversion(
        collect(
            sectionShell( 1,1, 5,5, 9,9 ),
            sectionHole(  6,0, 5,5, 4,0 ) ),
        collect(
            sectionShell( 1,1, 5,5, 4,0 ),
            sectionShell( 6,0, 5,5, 9,9 ) )
        );
  }

  public void testShellsAndHoles() {
    checkConversion(
        collect(
            sectionShell( 1,1, 5,5, 9,9 ),
            sectionHole(  6,0, 5,5, 4,0 ),
            
            sectionShell( 8,8, 5,5, 1,8 ),
            sectionHole(  4,8, 5,5, 6,8 ) 
            ),
        collect(
            sectionShell( 1,1, 5,5, 4,0 ),
            sectionShell( 6,0, 5,5, 9,9 ),
            
            sectionShell( 4,8, 5,5, 1,8 ),
            sectionShell( 8,8, 5,5, 6,8 ) 
            )
        );
  }

  public void testShellAnd2Holes() {
    checkConversion(
        collect(
            sectionShell( 1,1, 5,5, 9,9 ),
            sectionHole(  7,0, 5,5, 6,0 ),
            sectionHole(  4,0, 5,5, 3,0 ) ),
        collect(
            sectionShell( 1,1, 5,5, 3,0 ),
            sectionShell( 4,0, 5,5, 6,0 ),
            sectionShell( 7,0, 5,5, 9,9 ) )
        );
  }
  
  public void testHoles() {
    checkConversion(
        collect(
            sectionHole(  7,0, 5,5, 6,0 ),
            sectionHole(  4,0, 5,5, 3,0 ) ),
        collect(
            sectionShell( 4,0, 5,5, 6,0 ),
            sectionShell( 7,0, 5,5, 3,0 ) )
        );
  }
  
  private void checkConversion(List<NodeSection> input, List<NodeSection> expected) {
    List<NodeSection> actual = PolygonNodeConverter.convert(input);
    boolean isEqual = checkSectionsEqual(actual, expected);
    if (! isEqual) {
      System.out.println("Expected:" + formatSections(expected));
      System.out.println("Actual:" + formatSections(actual));      
    }
    assertTrue(isEqual);
  }

  private String formatSections(List<NodeSection> sections) {
    StringBuilder sb = new StringBuilder();
    for (NodeSection ns : sections) {
      sb.append(ns + "\n");
    }
    return sb.toString();
  }

  private boolean checkSectionsEqual(List<NodeSection> ns1, List<NodeSection> ns2) {
    if (ns1.size() != ns2.size())
      return false;
    sort(ns1);
    sort(ns2);
    for (int i = 0; i < ns1.size(); i++) {
      int comp = ns1.get(i).compareTo(ns2.get(i));
      if (comp != 0)
        return false;
    }
    return true;
  }

  private void sort(List<NodeSection> ns) {
    ns.sort(new NodeSection.EdgeAngleComparator());
  }

  private List<NodeSection> collect(NodeSection... sections) {
    List<NodeSection> sectionList = new ArrayList<NodeSection>();
    for (NodeSection s : sections) {
      sectionList.add(s);
    }
    return sectionList;
  }

  private NodeSection sectionHole(double v0x, double v0y, double nx, double ny, double v1x, double v1y) {
    return section(1, v0x, v0y, nx, ny, v1x, v1y);
  }

  private NodeSection section(int ringId, double v0x, double v0y, double nx, double ny, double v1x, double v1y) {
    return new NodeSection(true, Dimension.A, 1, ringId, null, false, 
        new Coordinate(v0x, v0y), new Coordinate(nx, ny), new Coordinate(v1x, v1y)); 
  }

  private NodeSection sectionShell(double v0x, double v0y, double nx, double ny, double v1x, double v1y) {
    return section(0, v0x, v0y, nx, ny, v1x, v1y);
  }
}
