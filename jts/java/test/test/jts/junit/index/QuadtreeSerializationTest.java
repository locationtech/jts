
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package test.jts.junit.index;

import java.io.*;
import java.io.IOException;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

import com.vividsolutions.jts.index.quadtree.Quadtree;

/**
 * @version 1.7
 */
public class QuadtreeSerializationTest extends TestCase {

  public QuadtreeSerializationTest(String Name_) {
    super(Name_);
  }

  public static void main(String[] args) {
    String[] testCaseName = {QuadtreeSerializationTest.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
  }

  public void testSerialization()
  throws Exception
  {
    SpatialIndexTester tester = new SpatialIndexTester();
    tester.setSpatialIndex(new Quadtree());
    tester.init();
    Quadtree tree = (Quadtree) tester.getSpatialIndex();
    byte[] data = serialize(tree);
    tree = (Quadtree) deserialize(data);
    tester.run();
    assertTrue(tester.isSuccess());
  }
  
  private static byte[] serialize(Object obj) throws IOException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(bos);
    out.writeObject(obj);
    out.close();
    byte[] treeBytes = bos.toByteArray();
    return treeBytes;
  }

  private static Object deserialize(byte[] data) 
    throws IOException, ClassNotFoundException
  {
    // deserialize tree
    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
    return in.readObject();
  }
}
