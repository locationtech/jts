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

import java.io.IOException;

/**
 * A interface for classes providing an output stream of bytes.
 * This interface is similar to the Java <code>OutputStream</code>,
 * but with a narrower interface to make it easier to implement.
 */
public interface OutStream
{
  void write(byte[] buf, int len) throws IOException;
}
