

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

/**
 *  Thrown by a <code>WKTReader</code> when a parsing problem occurs.
 *
 *@version 1.7
 */
public class ParseException extends Exception {

  /**
   *  Creates a <code>ParseException</code> with the given detail message.
   *
   *@param  message  a description of this <code>ParseException</code>
   */
  public ParseException(String message) {
    super(message);
  }

  /**
   *  Creates a <code>ParseException</code> with <code>e</code>s detail message.
   *
   *@param  e  an exception that occurred while a <code>WKTReader</code> was
   *      parsing a Well-known Text string
   */
  public ParseException(Exception e) {
    this(e.toString(), e);
  }
  
  /**
   *  Creates a <code>ParseException</code> with <code>e</code>s detail message.
   *  
   *@param  message  a description of this <code>ParseException</code>
   *@param  e  a throwable that occurred while a com.vividsolutions.jts.io reader was
   *      parsing a string representation
   */
  public ParseException(String message, Throwable e) {
          super(message, e);
  }
}


