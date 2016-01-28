
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