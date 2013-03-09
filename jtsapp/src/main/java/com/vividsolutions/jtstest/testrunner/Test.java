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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jtstest.geomop.GeometryOperation;
import com.vividsolutions.jtstest.util.StringUtil;

/**
 *  A test for two geometries.
 *
 * @version 1.7
 */
public class Test implements Runnable 
{
  private String description;
  private String operation;
  private Result expectedResult;
  private int testIndex;
  private String geometryIndex;
  private ArrayList arguments;
  private TestCase testCase;
  private boolean passed;
  private double tolerance;
  
  // cache for actual computed result
  private Geometry targetGeometry;
  private Object[] operationArgs;
  private boolean isRun = false;
  private Result actualResult = null;
  private Exception exception = null;

  /**
   *  Creates a Test with the given description. The given operation (e.g.
   *  "equals") will be performed, the expected result of which is <tt>expectedResult</tt>.
   */
  public Test(TestCase testCase, int testIndex, String description, String operation, String geometryIndex,
      List arguments, Result expectedResult, double tolerance) {
    this.tolerance = tolerance;
    this.description = description;
    this.operation = operation;
    this.expectedResult = expectedResult;
    this.testIndex = testIndex;
    this.geometryIndex = geometryIndex;
    this.arguments = new ArrayList(arguments);
    this.testCase = testCase;
  }

  public void setResult(Result result) {
    this.expectedResult = result;
  }

  public void setArgument(int i, String value) {
    arguments.set(i, value);
  }

  public String getDescription() {
    return description;
  }

  public String getGeometryIndex() {
    return geometryIndex;
  }

  public Result getExpectedResult() {
    return expectedResult;
  }

  public String getOperation() {
    return operation;
  }

  public int getTestIndex() {
    return testIndex;
  }

  public String getArgument(int i) {
    return (String) arguments.get(i);
  }

  public int getArgumentCount() {
    return arguments.size();
  }

  /**
   *  Returns whether the Test is passed.
   */
  public boolean isPassed() {
    return passed;
  }

  public Exception getException() {
    return exception;
  }

  public TestCase getTestCase() {
    return testCase;
  }

  public void removeArgument(int i) {
    arguments.remove(i);
  }

  public void run() {
    try {
      exception = null;
      passed = computePassed();
    }
    catch (Exception e) {
      exception = e;
    }
  }

  public boolean isRun()
  {
  	return isRun;
  }
  
  public boolean computePassed()
      throws Exception
  {
    Result actualResult = getActualResult();
    ResultMatcher matcher = testCase.getTestRun().getResultMatcher();
    
    // check that provided expected result geometry is valid
    // MD - disable except for testing
    //if (! isExpectedResultGeometryValid()) return false;
    
    return matcher.isMatch(targetGeometry, operation, operationArgs, 
    		actualResult, expectedResult, 
    		tolerance);
//    return expectedResult.equals(actualResult, tolerance);
  }

  private boolean isExpectedResultGeometryValid()
  {
    if (expectedResult instanceof GeometryResult) {
    	Geometry expectedGeom = ((GeometryResult) expectedResult).getGeometry();
    	return expectedGeom.isValid();
    }
    return true;
  }
  
  /**
   * Computes the actual result and caches the result value.
   * 
   * @return the actual result computed
   * @throws Exception if the operation fails
   */
  public Result getActualResult() throws Exception 
  {
  	if (isRun)
  		return actualResult;
  	
  	isRun = true;
    targetGeometry = geometryIndex.equalsIgnoreCase("A")
         ? testCase.getGeometryA()
         : testCase.getGeometryB();

         operationArgs = convertArgs(arguments);
    GeometryOperation op = getGeometryOperation();
    actualResult = op.invoke(operation, targetGeometry, operationArgs);
    return actualResult;
  }

  private GeometryOperation getGeometryOperation()
  {
  	return testCase.getTestRun().getGeometryOperation();
  }
  	
  public String toXml() {
    String xml = "";
    xml += "<test>" + StringUtil.newLine;
    if (description != null && description.length() > 0) {
      xml += "  <desc>" + StringUtil.escapeHTML(description) + "</desc>" +
          StringUtil.newLine;
    }
    xml += "  <op name=\"" + operation + "\"";
    xml += " arg1=\"" + geometryIndex + "\"";
    int j = 2;
    for (Iterator i = arguments.iterator(); i.hasNext(); ) {
      String argument = (String) i.next();
      Assert.isTrue(argument != null);
      xml += " arg" + j + "=\"" + argument + "\"";
      j++;
    }

    xml += ">" + StringUtil.newLine;
    xml += StringUtil.indent(expectedResult.toFormattedString(), 4) + StringUtil.newLine;
    xml += "  </op>" + StringUtil.newLine;
    xml += "</test>" + StringUtil.newLine;
    return xml;
  }

  private Object[] convertArgs(List argStr)
  {
    Object[] args = new Object[argStr.size()];
    for (int i = 0; i < args.length; i++) {
      args[i] = convertArgToGeomOrString((String) argStr.get(i));
    }
    return args;
  }

  private Object convertArgToGeomOrString(String argStr)
  {
    if (argStr.equalsIgnoreCase("null")) {
      return null;
    }
    if (argStr.equalsIgnoreCase("A")) {
      return testCase.getGeometryA();
    }
    if (argStr.equalsIgnoreCase("B")) {
      return testCase.getGeometryB();
    }
    return argStr;
  }


}

