
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
package org.locationtech.jts.io;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.util.GeometricShapeFactory;

import junit.framework.TestCase;
import junit.textui.TestRunner;


public class SerializabilityTest
extends TestCase
{

  static GeometryFactory fact = new GeometryFactory();

  public static void main(String args[]) {
    TestRunner.run(SerializabilityTest.class);
  }


  public SerializabilityTest(String name) { super(name); }

  public void testSerializable()
      throws Exception
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);

    GeometricShapeFactory gsf = new GeometricShapeFactory(fact);
    Geometry g = gsf.createCircle();
    oos.writeObject(g);
  }
}