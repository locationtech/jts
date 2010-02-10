
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
package test.jts.index;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.index.strtree.STRtree;


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
