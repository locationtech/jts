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
package com.vividsolutions.jtstest.testrunner;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jtstest.util.StringUtil;

/**
 *  A set of tests for two Geometry's.
 *
 *@author     jaquino
 *@created    June 22, 2001
 *
 * @version 1.7
 */
public class TestCase implements Runnable {
  private String description;
  private Geometry a;
  private Geometry b;
  private Vector tests = new Vector();
  private TestRun testRun;
  private int caseIndex;
  private int lineNumber;
  private File aWktFile;
  private File bWktFile;
  private boolean isRun = false;

  /**
   *  Creates a TestCase with the given description. The tests will be applied
   *  to a and b.
   */
  public TestCase(String description, Geometry a, Geometry b, File aWktFile,
      File bWktFile, TestRun testRun, int caseIndex, int lineNumber) {
    this.description = description;
    this.a = a;
    this.b = b;
    this.aWktFile = aWktFile;
    this.bWktFile = bWktFile;
    this.testRun = testRun;
    this.caseIndex = caseIndex;
    this.lineNumber = lineNumber;
  }

  public int getLineNumber() { return lineNumber; }

  public void setGeometryA(Geometry a) {
    aWktFile = null;
    this.a = a;
  }

  public void setGeometryB(Geometry b) {
    bWktFile = null;
    this.b = b;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isRun() 
  {
  	return isRun;
  }
  
  public Geometry getGeometryA() {
    return a;
  }

  public Geometry getGeometryB() {
    return b;
  }

  /**
   *  Returns the number of tests.
   *
   *@return    The testCount value
   */
  public int getTestCount() {
    return tests.size();
  }

  public List getTests() {
    return Collections.unmodifiableList(tests);
  }

  public TestRun getTestRun() {
    return testRun;
  }

  public int getCaseIndex() {
    return caseIndex;
  }

  public String getDescription() {
    return description;
  }

  /**
   *  Adds a Test to the TestCase.
   */
  public void add(Test test) {
    tests.add(test);
  }

  public void remove(Test test) {
    tests.remove(test);
  }

  public void run() {
  	isRun = true;
    for (Iterator i = tests.iterator(); i.hasNext(); ) {
      Test test = (Test) i.next();
      test.run();
    }
  }

  public String toXml() {
    WKTWriter writer = new WKTWriter();
    String xml = "";
    xml += "<case>" + StringUtil.newLine;
    if (description != null && description.length() > 0) {
      xml += "  <desc>" + StringUtil.escapeHTML(description) + "</desc>" +
          StringUtil.newLine;
    }
    xml += xml("a", a, aWktFile, writer) + StringUtil.newLine;
    xml += xml("b", b, bWktFile, writer);
    for (Iterator i = tests.iterator(); i.hasNext(); ) {
      Test test = (Test) i.next();
      xml += test.toXml();
    }
    xml += "</case>" + StringUtil.newLine;
    return xml;
  }

  private String xml(String id, Geometry g, File wktFile, WKTWriter writer) {
    if (g == null) {
      return "";
    }
    if (wktFile != null) {
      return "  <" + id + " file=\"" + wktFile + "\"/>";
    }
    String xml = "";
    xml += "  <" + id + ">" + StringUtil.newLine;
    xml += StringUtil.indent(writer.writeFormatted(g), 4) + StringUtil.newLine;
    xml += "  </" + id + ">" + StringUtil.newLine;
    return xml;
  }
}


