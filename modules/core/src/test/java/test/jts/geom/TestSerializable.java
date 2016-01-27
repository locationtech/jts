
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
