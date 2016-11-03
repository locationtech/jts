
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

package test.jts.junit;

import junit.framework.TestCase;
import junit.swingui.TestRunner;


/**
 * @version 1.7
 */
public class SimpleTest extends TestCase {

  public static void main(String args[]) {
    TestRunner.run(SimpleTest.class);
  }

  public SimpleTest(String name) { super(name); }

  public void testThisIsATest() throws Exception {
    assertTrue(true);
  }

}

