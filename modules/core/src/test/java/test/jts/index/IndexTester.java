
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
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.util.Stopwatch;



/**
 * @version 1.7
 */
public class IndexTester {
  static final int NUM_ITEMS = 2000;
  static final double EXTENT_MIN = -1000.0;
  static final double EXTENT_MAX = 1000.0;

  Index index;

  public IndexTester(Index index)
  {
    this.index = index;
  }

  public static class IndexResult {
    public IndexResult(String indexName) { this.indexName = indexName; }
    public String indexName;
    public long loadMilliseconds;
    public long queryMilliseconds;
  }

  private static List victoriaItems = null;

  public IndexResult testAll(List items)
  {
    IndexResult result = new IndexResult(index.toString());
    System.out.print(index.toString() + "           ");
    System.gc();
    Stopwatch sw = new Stopwatch();
    sw.start();
    loadGrid(items);
    String loadTime = sw.getTimeString();
    result.loadMilliseconds = sw.getTime();
    System.gc();
    sw.start();
    //runQueries();
    runSelfQuery(items);
    String queryTime = sw.getTimeString();
    result.queryMilliseconds = sw.getTime();
    System.out.println("  Load Time = " + loadTime + "  Query Time = " + queryTime);
    return result;
  }

  public static List createGridItems(int nGridCells)
  {
    ArrayList items = new ArrayList();
    int gridSize = (int) Math.sqrt((double) nGridCells);
    gridSize += 1;
    double extent = EXTENT_MAX - EXTENT_MIN;
    double gridInc = extent / gridSize;
    double cellSize = gridInc;
    for (int i = 0; i < gridSize; i++) {
      for (int j = 0; j < gridSize; j++) {
        double x = EXTENT_MIN + gridInc * i;
        double y = EXTENT_MIN + gridInc * j;
        Envelope env = new Envelope(x, x + cellSize,
                                    y, y + cellSize);
        items.add(env);
      }
    }
    return items;
  }

  void loadGrid(List items)
  {
    for (Iterator i = items.iterator(); i.hasNext(); ) {
      Envelope item = (Envelope) i.next();
      index.insert(item, item);
    }
    index.finishInserting();
  }
  void runSelfQuery(List items)
  {
    double querySize = 0.0;
    for (int i = 0; i < items.size(); i++) {
      Envelope env = (Envelope) items.get(i);
      List list = index.query(env);
      Assert.isTrue(!list.isEmpty());
      querySize += list.size();
    }
    System.out.println("Avg query size = " + querySize / items.size());
  }
  void runGridQuery()
  {
    int nGridCells = 100;
    int cellSize = (int) Math.sqrt((double) NUM_ITEMS);
    double extent = EXTENT_MAX - EXTENT_MIN;
    double queryCellSize =  2.0 * extent / cellSize;

    queryGrid(nGridCells, queryCellSize);
  }

  void queryGrid(int nGridCells, double cellSize)
  {

    int gridSize = (int) Math.sqrt((double) nGridCells);
    gridSize += 1;
    double extent = EXTENT_MAX - EXTENT_MIN;
    double gridInc = extent / gridSize;

    for (int i = 0; i < gridSize; i++) {
      for (int j = 0; j < gridSize; j++) {
        double x = EXTENT_MIN + gridInc * i;
        double y = EXTENT_MIN + gridInc * j;
        Envelope env = new Envelope(x, x + cellSize,
                                    y, y + cellSize);
        index.query(env);
      }
    }
  }

}
