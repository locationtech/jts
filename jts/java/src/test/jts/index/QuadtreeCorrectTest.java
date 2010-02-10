
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
import com.vividsolutions.jts.util.Stopwatch;


/**
 * @version 1.7
 */
public class QuadtreeCorrectTest {

  public static void main(String[] args) throws Exception
  {
    //testBinaryPower();
    QuadtreeCorrectTest test = new QuadtreeCorrectTest();
    test.run();
  }

/*
  public static void testBinaryPower()
  {
    printBinaryPower(1004573397.0);
    printBinaryPower(100.0);
    printBinaryPower(0.234);
    printBinaryPower(0.000003455);
  }

  public static void printBinaryPower(double num)
  {
    BinaryPower pow2 = new BinaryPower();
    int exp = BinaryPower.exponent(num);
    double p2 = pow2.power(exp);
    System.out.println(num + " : pow2 = " +  Math.pow(2.0, exp)
        + "   exp = " + exp + "   2^exp = " + p2);
  }
*/
  static final int NUM_ITEMS = 2000;
  static final double MIN_EXTENT = -1000.0;
  static final double MAX_EXTENT = 1000.0;

  EnvelopeList envList = new EnvelopeList();
  Quadtree q = new Quadtree();

  public QuadtreeCorrectTest() {
  }

  public void run()
  {
    fill();
    System.out.println("depth = " + q.depth()
      + "  size = " + q.size() );
    runQueries();
  }

  void fill()
  {
    createGrid(NUM_ITEMS);
  }

  void createGrid(int nGridCells)
  {
    int gridSize = (int) Math.sqrt((double) nGridCells);
    gridSize += 1;
    double extent = MAX_EXTENT - MIN_EXTENT;
    double gridInc = extent / gridSize;
    double cellSize = 2 * gridInc;

    for (int i = 0; i < gridSize; i++) {
      for (int j = 0; j < gridSize; j++) {
        double x = MIN_EXTENT + gridInc * i;
        double y = MIN_EXTENT + gridInc * j;
        Envelope env = new Envelope(x, x + cellSize,
                                    y, y + cellSize);
        q.insert(env, env);
        envList.add(env);
      }
    }
  }

  void runQueries()
  {
    int nGridCells = 100;
    int cellSize = (int) Math.sqrt((double) NUM_ITEMS);
    double extent = MAX_EXTENT - MIN_EXTENT;
    double queryCellSize =  2.0 * extent / cellSize;

    queryGrid(nGridCells, queryCellSize);

    //queryGrid(200);
  }

  void queryGrid(int nGridCells, double cellSize)
  {
    Stopwatch sw = new Stopwatch();
    sw.start();

    int gridSize = (int) Math.sqrt((double) nGridCells);
    gridSize += 1;
    double extent = MAX_EXTENT - MIN_EXTENT;
    double gridInc = extent / gridSize;

    for (int i = 0; i < gridSize; i++) {
      for (int j = 0; j < gridSize; j++) {
        double x = MIN_EXTENT + gridInc * i;
        double y = MIN_EXTENT + gridInc * j;
        Envelope env = new Envelope(x, x + cellSize,
                                    y, y + cellSize);
        queryTest(env);
        //queryTime(env);
      }
    }
    System.out.println("Time = " + sw.getTimeString());
  }

  void queryTime(Envelope env)
  {
    //List finalList = getOverlapping(q.query(env), env);

    List eList = envList.query(env);
  }

  void queryTest(Envelope env)
  {
    List candidateList = q.query(env);
    List finalList = getOverlapping(candidateList, env);

    List eList = envList.query(env);
//System.out.println(finalList.size());

    if (finalList.size() != eList.size() )
      throw new RuntimeException("queries do not match");
  }

  private List getOverlapping(List items, Envelope searchEnv)
  {
    List result = new ArrayList();
    for (int i = 0; i < items.size(); i++) {
      Envelope env = (Envelope) items.get(i);
      if (env.intersects(searchEnv)) result.add(env);
    }
    return result;
  }

}
