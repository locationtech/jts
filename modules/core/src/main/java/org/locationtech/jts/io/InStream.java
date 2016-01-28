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

import java.io.IOException;

/**
 * A interface for classes providing an input stream of bytes.
 * This interface is similar to the Java <code>InputStream</code>,
 * but with a narrower interface to make it easier to implement.
 *
 */
public interface InStream
{
  /**
   * Reads <code>buf.length</code> bytes from the input stream
   * and stores them in the supplied buffer.
   *
   * @param buf the buffer to receive the bytes
   *
   * @throws IOException if an I/O error occurs
   */
  void read(byte[] buf) throws IOException;
}
