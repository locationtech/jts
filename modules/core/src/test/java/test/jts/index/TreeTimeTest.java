
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
package test.jts.index;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.index.strtree.STRtree;



/**
 * @version 1.7
 */
public class TreeTimeTest {
  public static final int NUM_ITEMS = 10000;

  public static void main(String[] args) throws Exception
  {
    int n = 10000;
    TreeTimeTest test = new TreeTimeTest();
    List items = IndexTester.createGridItems(n);
    System.out.println("----------------------------------------------");
    System.out.println("Dummy run to ensure classes are loaded before real run");
    System.out.println("----------------------------------------------");
    test.run(items);
    System.out.println("----------------------------------------------");
    System.out.println("Real run");
    System.out.println("----------------------------------------------");
    test.run(items);
  }

  public TreeTimeTest()
  {
  }

  public List run(List items) throws Exception
  {
    ArrayList indexResults = new ArrayList();
    System.out.println("# items = " + items.size());
    indexResults.add(run(new QuadtreeIndex(), items));
    indexResults.add(run(new STRtreeIndex(10), items));
    //indexResults.add(run(new QXtreeIndex(), n));
    //indexResults.add(run(new EnvelopeListIndex(), n));
    return indexResults;
  }

  public IndexTester.IndexResult run(Index index, List items) throws Exception
  {
    return new IndexTester(index).testAll(items);
  }

  class STRtreeIndex
    implements Index
  {
    public String toString() { return "STR[M=" + index.getNodeCapacity() + "]"; }
//    public String toString() { return "" + index.getNodeCapacity() + ""; }
    public STRtreeIndex(int nodeCapacity)
    {
      index = new STRtree(nodeCapacity);
    }
    STRtree index;

    public void insert(Envelope itemEnv, Object item)
    {
      index.insert(itemEnv, item);
    }
    public List query(Envelope searchEnv)
    {
      return index.query(searchEnv);
    }
    public void finishInserting()
    {
      index.build();
    }
  }

  class QuadtreeIndex
    implements Index
  {
    Quadtree index = new Quadtree();
    public String toString() { return "Quad"; }
    public void insert(Envelope itemEnv, Object item)
    {
      index.insert(itemEnv, item);
    }
    public List query(Envelope searchEnv)
    {
      return index.query(searchEnv);
    }
    public void finishInserting()
    {
    }
  }

  class EnvelopeListIndex
    implements Index
  {
    EnvelopeList index = new EnvelopeList();
    public String toString() { return "Env"; }
    public void insert(Envelope itemEnv, Object item)
    {
      index.add(itemEnv);
    }
    public List query(Envelope searchEnv)
    {
      return index.query(searchEnv);
    }
    public void finishInserting()
    {
    }
  }


}
