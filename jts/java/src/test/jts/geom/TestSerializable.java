
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

package test.jts.geom;

import java.io.*;
import java.util.*;


import com.vividsolutions.jts.geom.*;


/**
 * @version 1.7
 */
public class TestSerializable {

  public static final String FILENAME = "c:\\testSerial.txt";
  public static final GeometryFactory fact = new GeometryFactory();

  public TestSerializable() {
  }

  public static void main(String[] args) {
    TestSerializable test = new TestSerializable();
    test.run();
  }

  public void run()
  {
    List objList = createData();
    writeData(objList);
    readData(objList);
  }

  List createData()
  {
    List objList = new ArrayList();

    Envelope env = new Envelope(123, 456, 123, 456);
    objList.add(env);

    objList.add(GeometryTestFactory.createBox(fact, 0.0, 100.0, 10, 10.0));

    return objList;

  }
  void writeData(List objList)
  {
    File file;                           // simply a file name
    FileOutputStream outStream;             // generic stream to the file
    ObjectOutputStream objStream;           // stream for objects to the file

    file = new File(FILENAME);

    try {
      // setup a stream to a physical file on the filesystem
      outStream = new FileOutputStream(file);

      // attach a stream capable of writing objects to the stream that is
      // connected to the file
      objStream = new ObjectOutputStream(outStream);

      objStream.writeObject(objList);
//      for (Iterator i = objList.iterator(); i.hasNext(); )
//      {
//        objStream.writeObject(i.next());
//      }
      objStream.close();

    } catch(IOException e) {
      System.err.println("Things not going as planned.");
      e.printStackTrace();
    }   // catch
  }
  void readData(List objList)
  {
    File file;                           // simply a file name
    FileInputStream stream;             // generic stream to the file
    ObjectInputStream objStream;           // stream for objects to the file

    file = new File(FILENAME);

    try {
      // setup a stream to a physical file on the filesystem
      stream = new FileInputStream(file);

      // attach a stream capable of writing objects to the stream that is
      // connected to the file
      objStream = new ObjectInputStream(stream);

      int count = 0;
      Object obj = objStream.readObject();
      List inputList = (List) obj;
      for (Iterator i = inputList.iterator(); i.hasNext(); ) {
        compare(objList.get(count++), i.next());
      }

//      while (objStream.available() > 0) {
//        Object obj = objStream.readObject();
//        compare(objList.get(count++), obj);
//      }
      objStream.close();

    } catch(Exception e) {
      System.err.println("Things not going as planned.");
      e.printStackTrace();
    }   // catch
  }

  boolean compare(Object o1, Object o2)
  {
    boolean matched = false;
    if (o1 instanceof Envelope) {
      if (! ((Envelope) o1).equals(o2) ) {
        System.out.println("expected " + o1 + ", found " + o2);
      }
      else
        matched = true;
    }
    else if (o1 instanceof Geometry) {
      if (! ((Geometry) o1).equalsExact((Geometry) o2) ) {
        System.out.println("expected " + o1 + ", found " + o2);
      }
      else
        matched = true;
    }
    if (matched)
      System.out.println("found match for object");
    return true;
  }

}
