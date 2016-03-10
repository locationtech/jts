
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

package org.locationtech.jtstest;

import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jtstest.testbuilder.JTSTestBuilderFrame;
import org.locationtech.jtstest.testbuilder.model.*;

import junit.framework.TestCase;



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
