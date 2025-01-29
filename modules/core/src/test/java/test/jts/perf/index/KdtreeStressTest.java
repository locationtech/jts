package test.jts.perf.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;

/**
 * Tests an issue where deep KdTrees caused a {@link StackOverflowError}
 * when using a recursive query implementation.
 * 
 * See a fix for this issue in GEOS 
 * at https://github.com/libgeos/geos/pull/481.
 * 
 * @author mdavis
 *
 */
public class KdtreeStressTest {

  // In code with recursive query 50,000 points causes StackOverflowError
  int NUM_PTS = 50000;
  
  public static void main(String[] args) throws Exception
  {
    KdtreeStressTest test = new KdtreeStressTest();
    test.run();
  }

  private void run() {
    System.out.format("Loading iIndex with %d points\n", NUM_PTS);
    KdTree index = createUnbalancedTree(NUM_PTS);
    
    System.out.format("Querying Index loaded with %d points\n", NUM_PTS);
    for (int i = 0; i < NUM_PTS; i++) {
      Envelope env = new Envelope(i, i + 10, 0, 1);
      index.query(env);
    }
    System.out.format("Queries complete\n");
    
    testNearestNeighborsPerformance();
  }
  
  private void testNearestNeighborsPerformance() {
      int n = 1_000_000;
      int k = 1000;
      KdTree tree = new KdTree();
      Random rand = new Random();

      List<Coordinate> points = new ArrayList<>();
      for (int i = 0; i < n; i++) {
          double x = rand.nextDouble();
          double y = rand.nextDouble();
          points.add(new Coordinate(x, y));
      }
      long startTime = System.nanoTime();
      for (Coordinate coordinate : points) {
		tree.insert(coordinate);
      }
      long insertTime = System.nanoTime() - startTime;
      System.out.println("Time to insert " + n + " points: " + (insertTime / 1_000_000) + " ms");

      Coordinate query = new Coordinate(rand.nextDouble(), rand.nextDouble());

      // Time k-NN query using k-d tree
      startTime = System.nanoTime();
      List<KdNode> nearest = tree.nearestNeighbors(query, k);
      long knnTime = System.nanoTime() - startTime;
      System.out.println("Time to find " + k + " nearest neighbors using k-d tree: " + (knnTime / 1_000_000) + " ms");

      // Time k-NN query using brute-force
      startTime = System.nanoTime();
      List<Coordinate> bruteForceNearest = bruteForceNearestNeighbors(tree, query, k);
      long bruteForceTime = System.nanoTime() - startTime;
      System.out.println("Time to find " + k + " nearest neighbors using brute-force: " + (bruteForceTime / 1_000_000) + " ms");
  }

  /**
   * Create an unbalanced tree by loading a 
   * series of monotonically increasing points
   * 
   * @param numPts number of points to load
   * @return a new index
   */
  private KdTree createUnbalancedTree(int numPts) {
    KdTree index = new KdTree();
    for (int i = 0; i < numPts; i++) {
      Coordinate pt = new Coordinate(i, 0);
      index.insert(pt);
    }
    return index;
  }
  
  private List<Coordinate> bruteForceNearestNeighbors(KdTree tree, Coordinate query, int k) {
      List<Coordinate> allPoints = getAllPoints(tree);

      // Sort all points by distance to the query point
      allPoints.sort(Comparator.comparingDouble(point -> query.distance(point)));

      // Return the first k points (ordered closest first)
      return allPoints.subList(0, Math.min(k, allPoints.size()));
  }
  
  private List<Coordinate> getAllPoints(KdTree tree) {
	  return Arrays.stream(KdTree.toCoordinates(tree.getNodes())).collect(Collectors.toList());
  }
}
