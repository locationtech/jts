
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package com.vividsolutions.jtstest;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jtstest.testbuilder.JTSTestBuilderFrame;
import com.vividsolutions.jtstest.testbuilder.model.*;


/**
 * @version 1.7
 */
public class TestBuilderTest extends TestCase {

  public TestBuilderTest(String Name_) {
    super(Name_);
  }

  public static void main(String[] args) {
    String[] testCaseName = {TestBuilderTest.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
  }

  public void testPrecisionModelXml1() {
    PrecisionModel precisionModel = new PrecisionModel();
    assertEquals("<precisionModel type=\"FLOATING\"/>", XMLTestWriter.toXML(precisionModel));
  }

  public void testPrecisionModelXml2() {
    PrecisionModel precisionModel = new PrecisionModel(1);
    assertEquals("<precisionModel type=\"FIXED\" scale=\"1.0\"/>", XMLTestWriter.toXML(precisionModel));
  }

}
