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
package com.vividsolutions.jtstest.testbuilder.model;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jtstest.test.TestCaseList;
import com.vividsolutions.jtstest.test.Testable;
import com.vividsolutions.jtstest.util.StringUtil;


/**
 * @version 1.7
 */
public class JavaTestWriter {
  public static String getRunJava(String className, TestBuilderModel tbModel) {
    return
        "package com.vividsolutions.jtstest.testsuite;" + StringUtil.newLine
         + "" + StringUtil.newLine
         + "import com.vividsolutions.jtstest.test.*;" + StringUtil.newLine
         + "" + StringUtil.newLine
         + "public class " + className + " extends TestCaseList {" + StringUtil.newLine
         + "  public static void main(String[] args) {" + StringUtil.newLine
         + "    " + className + " test = new " + className + "();" + StringUtil.newLine
         + "    test.run();" + StringUtil.newLine
         + "  }" + StringUtil.newLine
         + "" + StringUtil.newLine
         + "  public " + className + "() {" + StringUtil.newLine
         + getTestJava(tbModel.getTestCaseList())
         + "  }" + StringUtil.newLine
         + "}";
  }

    public static String getTestJava(TestCaseList tcList) {
      StringBuffer java = new StringBuffer();
      for (int i = 0; i < tcList.getList().size(); i++) {
        java.append((new JavaTestWriter()).write((Testable) tcList.getList().get(i)));
      }
      return java.toString();
    }


    private WKTWriter writer = new WKTWriter();

    public JavaTestWriter() {}

    public String write(Testable testable) {
        StringBuffer text = new StringBuffer();
        text.append("    add(new TestCase(\n");
        String name = testable.getName() == null ? "" : testable.getName();
        String description = testable.getDescription() == null ? "" : testable.getDescription();
        String a = testable.getGeometry(0) == null ? null : writer.write(testable.getGeometry(0));
        String b = testable.getGeometry(1) == null ? null : writer.write(testable.getGeometry(1));
        String im =
            testable.getExpectedIntersectionMatrix() != null
                ? testable.getExpectedIntersectionMatrix().toString()
                : null;
        text.append("          \"" + name + "\",\n");
        text.append("          \"" + description + "\",\n");
        text.append("          " + (a == null ? "null" : "\"" + a + "\"") + ",\n");
        text.append("          " + (b == null ? "null" : "\"" + b + "\"") + ",\n");
        text.append("          " + (im == null ? "null" : "\"" + im + "\"") + ",\n");
        text.append("          " + write(testable.getExpectedConvexHull()) + ",\n");
        text.append("          " + write(testable.getExpectedIntersection()) + ",\n");
        text.append("          " + write(testable.getExpectedUnion()) + ",\n");
        text.append("          " + write(testable.getExpectedDifference()) + ",\n");
        text.append("          " + write(testable.getExpectedSymDifference()) + ",\n");
        text.append("          " + write(testable.getExpectedBoundary()) + "));\n");
        return text.toString();
    }

    private String write(Geometry geometry) {
        if (geometry == null) {
            return "null";
        }
        return "\"" + writer.write(geometry) + "\"";
    }
}
