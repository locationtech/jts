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
package org.locationtech.jts.io.oracle;

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
