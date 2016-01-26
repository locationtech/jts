

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
package com.vividsolutions.jts.io;

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


