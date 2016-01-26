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
package com.vividsolutions.jts.io.oracle;

import junit.framework.TestCase;

/**
 * Tests OraReader without requiring an Oracle connection.
 * 
 * @author mbdavis
 *
 */
public class BaseOraTestCase extends TestCase
{
  public BaseOraTestCase(String name)
  {
    super(name);
  }

  protected static final int NULL = -1;
  protected static final double DNULL = Double.NaN;

}
