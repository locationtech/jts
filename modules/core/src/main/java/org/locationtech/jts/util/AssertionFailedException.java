

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


