

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
package org.locationtech.jts.util;

/**
 *  Thrown when the application is in an inconsistent state. Indicates a problem
 *  with the code.
 *
 *@version 1.7
 */
public class AssertionFailedException extends RuntimeException {

  /**
   *  Creates an <code>AssertionFailedException</code>.
   */
  public AssertionFailedException() {
    super();
  }

  /**
   *  Creates a <code>AssertionFailedException</code> with the given detail
   *  message.
   *
   *@param  message  a description of the assertion
   */
  public AssertionFailedException(String message) {
    super(message);
  }
}


