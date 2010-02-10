
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package test.jts;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jtstest.testrunner.TestCase;
import com.vividsolutions.jtstest.testrunner.TestReader;
import com.vividsolutions.jtstest.testrunner.TestRun;
import com.vividsolutions.jtstest.util.FileUtil;
import com.vividsolutions.jtstest.util.StringUtil;


/**
 * @version 1.7
 */
public class TestFileGeometryExtractor {

  public TestFileGeometryExtractor() {
  }
  public static void main(String[] args) throws Exception {
    TestReader testReader = new TestReader();
    TestRun testRun = testReader.createTestRun(new File("c:\\blah\\isvalid.xml"), 0);
    ArrayList geometries = new ArrayList();
    for (Iterator i = testRun.getTestCases().iterator(); i.hasNext(); ) {
      TestCase testCase = (TestCase) i.next();
      add(testCase.getGeometryA(), geometries);
      add(testCase.getGeometryB(), geometries);
    }
    String run = "";
    int j = 0;
    for (Iterator i = geometries.iterator(); i.hasNext(); ) {
      Geometry geometry = (Geometry) i.next();
      j++;
      run += "<case>" + StringUtil.newLine;
      run += "  <desc>Test " + j + "</desc>" + StringUtil.newLine;
      run += "  <a>" + StringUtil.newLine;
      run += "    " + geometry + StringUtil.newLine;
      run += "  </a>" + StringUtil.newLine;
      run += "  <test> <op name=\"isValid\" arg1=\"A\"> true </op> </test>" + StringUtil.newLine;
      run += "</case>" + StringUtil.newLine;
    }
    FileUtil.setContents("c:\\blah\\isvalid2.xml", run);
  }

  private static void add(Geometry geometry, ArrayList geometries) {
    if (geometry == null) { return; }
    for (Iterator i = geometries.iterator(); i.hasNext(); ) {
      Geometry existingGeometry = (Geometry) i.next();
      if (geometry.equalsExact(existingGeometry)) { return; }
    }
    geometries.add(geometry);
  }
}
