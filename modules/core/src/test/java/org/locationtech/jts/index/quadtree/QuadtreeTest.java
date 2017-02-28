/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.index.quadtree;

import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.locationtech.jts.geom.Envelope;

public class QuadtreeTest extends TestCase {
  public static void main(String args[]) {
    TestRunner.run(QuadtreeTest.class);
  }

  public QuadtreeTest(String name) {
    super(name);
  }

  @SuppressWarnings("rawtypes")
	public void testNullQuery() {
  	Quadtree qt = new Quadtree();
  	List result1 = qt.query(null); 
  	assertTrue(result1.size() == 0);
  	
  	qt.insert(new Envelope(0, 10, 0, 10), "some data");
  	List result2 = qt.query(null); 
  	assertTrue(result2.size() == 0);
  }


}
