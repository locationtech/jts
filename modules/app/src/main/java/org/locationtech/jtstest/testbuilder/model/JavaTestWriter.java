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
package org.locationtech.jtstest.testbuilder.model;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jtstest.test.TestCaseList;
import org.locationtech.jtstest.test.Testable;
import org.locationtech.jtstest.util.StringUtil;



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
