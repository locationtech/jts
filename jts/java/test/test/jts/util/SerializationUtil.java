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

package test.jts.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializationUtil
{
  public static byte[] serialize(Object obj) throws IOException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(bos);
    out.writeObject(obj);
    out.close();
    byte[] treeBytes = bos.toByteArray();
    return treeBytes;
  }

  public static Object deserialize(byte[] data) 
    throws IOException, ClassNotFoundException
  {
    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
    return in.readObject();
  }

}
