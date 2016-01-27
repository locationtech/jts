
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
package test.jts.index;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.index.bintree.Bintree;
import com.vividsolutions.jts.index.bintree.Interval;
import com.vividsolutions.jts.util.Stopwatch;


/**
 * @version 1.7
 */
public class BinTreeCorrectTest {

  public static void main(String[] args) throws Exception
  {
    //testBinaryPower();
    BinTreeCorrectTest test = new BinTreeCorrectTest();
    test.run();
  }


  static final int NUM_ITEMS = 20000;
  static final double MIN_EXTENT = -1000.0;
  static final double MAX_EXTENT = 1000.0;

  IntervalList intervalList = new IntervalList();
  Bintree btree = new Bintree();

  public BinTreeCorrectTest() {
  }

  public void run()
  {
    fill();
    System.out.println("depth = " + btree.depth()
      + "  size = " + btree.size() );
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
        double x = MIN_EXTENT + gridInc * i;
        Interval interval = new Interval(x, x + cellSize   );
        btree.insert(interval, interval);
        intervalList.add(interval);
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
        double x = MIN_EXTENT + gridInc * i;
        Interval interval = new Interval(x, x + cellSize);
        queryTest(interval);
        //queryTime(env);
    }
    System.out.println("Time = " + sw.getTimeString());
  }

  void queryTime(Interval interval)
  {
    //List finalList = getOverlapping(q.query(env), env);

    List eList = intervalList.query(interval);
  }

  void queryTest(Interval interval)
  {
    List candidateList = btree.query(interval);
    List finalList = getOverlapping(candidateList, interval);

    List eList = intervalList.query(interval);
System.out.println(finalList.size());

    if (finalList.size() != eList.size() )
      throw new RuntimeException("queries do not match");
  }

  private List getOverlapping(List items, Interval searchInterval)
  {
    List result = new ArrayList();
    for (int i = 0; i < items.size(); i++) {
      Interval interval = (Interval) items.get(i);
      if (interval.overlaps(searchInterval)) result.add(interval);
    }
    return result;
  }

}
