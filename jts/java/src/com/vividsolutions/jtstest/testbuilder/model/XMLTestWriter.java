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

import java.io.File;
import java.util.Iterator;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jtstest.test.TestCase;
import com.vividsolutions.jtstest.test.TestCaseList;
import com.vividsolutions.jtstest.test.Testable;
import com.vividsolutions.jtstest.util.StringUtil;


/**
 * @version 1.7
 */
public class XMLTestWriter 
{
  public static String toXML(PrecisionModel precisionModel) {
    if (precisionModel.isFloating()) {
      return "<precisionModel type=\"FLOATING\"/>";
    }
    return "<precisionModel type=\"FIXED\" scale=\""
         + precisionModel.getScale() + "\"/>";
  }

  private WKTWriter wktWriter = new WKTWriter();
  private WKBWriter wkbWriter = new WKBWriter();

  public XMLTestWriter() {
  }

  public String getTestXML(Geometry geometry, String opName, String[] arguments) {
    String xml = "  <test>\n";
    xml += "    <op name=\"" + opName + "\" arg1=\"A\"";
    int j = 2;
    for (int i = 0; i < arguments.length; i++) {
      String argument = arguments[i];
      Assert.isTrue(argument != null);
      xml += " arg" + j + "=\"" + argument  + "\"";
      j++;
    }
    xml += ">\n";
    xml += StringUtil.indent(wktWriter.writeFormatted(geometry) + "\n", 6);
    xml += "    </op>\n";
    xml += "  </test>\n";
    return xml;
  }

  private boolean isGdbcTestCase(TestCase testCase) {
    if (testCase.getName() == null || testCase.getDescription() == null) {
      return false;
    }
    if (testCase.getName().equalsIgnoreCase(testCase.getDescription())) {
      return false;
    }
    int nameColonIndex = testCase.getName().indexOf(":");
    int descriptionColonIndex = testCase.getDescription().indexOf(":");
    if (nameColonIndex == -1 || descriptionColonIndex == -1) {
      return false;
    }
    if (nameColonIndex != descriptionColonIndex) {
      return false;
    }
    return true;
  }

  public String getDescriptionForXmlFromGdbcTestCase(TestCase testCase) {
    int descriptionColonIndex = testCase.getDescription().indexOf(":");
    return "<desc>" + StringUtil.escapeHTML(testCase.getName() + " ["
        + testCase.getDescription().substring(1+descriptionColonIndex).trim()
        + "]") + "</desc>\n";
  }

  public String getDescriptionForXml(TestCase testCase) {
    if (isGdbcTestCase(testCase)) {
      return getDescriptionForXmlFromGdbcTestCase(testCase);
    }
    if (testCase.getDescription() != null && testCase.getDescription().length() > 0) {
      return "<desc>" + StringUtil.escapeHTML(testCase.getDescription()) + "</desc>\n";
    }
    if (testCase.getName() != null && testCase.getName().length() > 0) {
      return "<desc>" + StringUtil.escapeHTML(testCase.getName()) + "</desc>\n";
    }
    return "<desc> " 
    + getGeometryArgPairCode(testCase.getGeometries())
    + " </desc>\n";
  }

  private String getGeometryArgPairCode(Geometry[] geom)
  {
   return getGeometryCode(geom[0]) + "/" + getGeometryCode(geom[1]); 
  }
  
  private String getGeometryCode(Geometry geom)
  {
    String dimCode = "";
    if (geom instanceof Puntal) dimCode = "P";
    if (geom instanceof Lineal) dimCode = "L";
    if (geom instanceof Polygonal) dimCode = "L";
    
    if (geom instanceof GeometryCollection) return "m" + dimCode;
    
    return dimCode;
  }
  
  public String getTestXML(TestRunnerTestCaseAdapter adapter) {
    return adapter.getTestRunnerTestCase().toXml();
  }

  public String getTestXML(Testable testable, boolean useWKT) {
    if (testable instanceof TestCase) {
      return getTestXML((TestCase)testable, useWKT);
    }
    if (testable instanceof TestCaseEdit) {
      return getTestXML(((TestCaseEdit)testable).getTestable(), useWKT);
    }
    if (testable instanceof TestRunnerTestCaseAdapter) {
      return getTestXML((TestRunnerTestCaseAdapter)testable, useWKT);
    }
    Assert.shouldNeverReachHere();
    return null;
  }

  public String getTestXML(Testable testCase)
  {
    return getTestXML(testCase, true);
  }
  
  private String getTestXML(TestCase testCase, boolean useWKT) {
    Geometry[] geom = testCase.getGeometries();
    StringBuffer xml = new StringBuffer();
    xml.append("<case>\n");
    xml.append(getDescriptionForXml(testCase));
    if (geom[0] != null) {
      String wkt0 = getWKTorWKB(geom[0], useWKT);
      xml.append("  <a>\n" + wkt0 + "\n    </a>\n");
    }
    if (geom[1] != null) {
      String wkt1 = getWKTorWKB(geom[1], useWKT);
      xml.append("  <b>\n" + wkt1 + "\n    </b>\n");
    }
    if (testCase.getExpectedIntersectionMatrix() != null) {
      xml.append("  <test>\n");
      xml.append("    <op name=\"relate\" arg1=\"A\" arg2=\"B\" arg3=\"" + testCase.getExpectedIntersectionMatrix() + "\">true</op>\n");
      xml.append("  </test>\n");
    }
    if (testCase.getExpectedBoundary() != null) {
      xml.append(getTestXML(testCase.getExpectedBoundary(), "getboundary", new String[] {}));
    }
    if (testCase.getExpectedConvexHull() != null) {
      xml.append(getTestXML(testCase.getExpectedConvexHull(), "convexhull", new String[] {}));
    }
    if (testCase.getExpectedIntersection() != null) {
      xml.append(getTestXML(testCase.getExpectedIntersection(), "intersection", new String[] {"B"}));
    }
    if (testCase.getExpectedUnion() != null) {
      xml.append(getTestXML(testCase.getExpectedUnion(), "union", new String[] {"B"}));
    }
    if (testCase.getExpectedDifference() != null) {
      xml.append(getTestXML(testCase.getExpectedDifference(), "difference", new String[] {"B"}));
    }
    if (testCase.getExpectedSymDifference() != null) {
      xml.append(getTestXML(testCase.getExpectedSymDifference(), "symdifference", new String[] {"B"}));
    }
    xml.append("</case>\n");
    return xml.toString();
  }

  private String getWKTorWKB(Geometry g, boolean useWKT)
  {
    if (useWKT)
      return wktWriter.writeFormatted(g);
    return WKBWriter.toHex(wkbWriter.write(g));
  }
  
  public String getTestXML(TestCaseList tcList) {
    StringBuffer xml = new StringBuffer();
    for (int i = 0; i < tcList.getList().size(); i++) {
      xml.append("\n");
      xml.append(getTestXML((Testable) tcList.getList().get(i)));
    }
    xml.append("\n");
    return xml.toString();
  }

  public static String getRunXml(TestCaseList tcList, PrecisionModel precModel) {
    String runXML = "<run>" + StringUtil.newLine;
    runXML += getRunDescription(tcList);
    runXML += getRunWorkspace(tcList);
    runXML += toXML(precModel) + StringUtil.newLine;
    runXML += (new XMLTestWriter()).getTestXML(tcList) + "</run>";
    return runXML;
  }

  public static String getRunDescription(TestCaseList l) {
    for (Iterator i = l.getList().iterator(); i.hasNext(); ) {
      TestCaseEdit tce = (TestCaseEdit) i.next();
      if (tce.getTestable() instanceof TestRunnerTestCaseAdapter) {
        TestRunnerTestCaseAdapter a = (TestRunnerTestCaseAdapter) tce.getTestable();
        String description = a.getTestRunnerTestCase().getTestRun().getDescription();
        if (description != null && description.length() > 0) {
          return "  <desc>" + StringUtil.escapeHTML(description)
               + "</desc>" + StringUtil.newLine;
        }
        return "";
      }
    }
    return "";
  }

  public static String getRunWorkspace(TestCaseList l) {
    for (Iterator i = l.getList().iterator(); i.hasNext(); ) {
      TestCaseEdit tce = (TestCaseEdit) i.next();
      if (tce.getTestable() instanceof TestRunnerTestCaseAdapter) {
        TestRunnerTestCaseAdapter a = (TestRunnerTestCaseAdapter) tce.getTestable();
        File workspace = a.getTestRunnerTestCase().getTestRun().getWorkspace();
        if (workspace != null) {
          return "  <workspace file=\"" + StringUtil.escapeHTML(workspace.toString())
               + "\"/>" + StringUtil.newLine;
        }
        return "";
      }
    }
    return "";
  }


}
