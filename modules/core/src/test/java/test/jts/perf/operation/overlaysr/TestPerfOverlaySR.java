package test.jts.perf.operation.overlaysr;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.operation.overlaysr.OverlaySR;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

public class TestPerfOverlaySR 
extends PerformanceTestCase
{
  private static final int N_ITER = 1;
  
  static double ORG_X = 100;
  static double ORG_Y = 100;
  static double SIZE = 100;
  static int N_ARMS = 20;
  static double ARM_RATIO = 0.3;
  
  static int GRID_SIZE = 5;
  static double GRID_CELL_SIZE = 20;
  
  static int NUM_CASES = GRID_SIZE * GRID_SIZE;
  
  private Geometry geomA;
  private Geometry[] geomB;

  private PrecisionModel precisionModel;
  
  public static void main(String args[]) {
    PerformanceTestRunner.run(TestPerfOverlaySR.class);
  }
  
  public TestPerfOverlaySR(String name) {
    super(name);
    setRunSize(new int[] { 100, 1000, 10000, 100000 });
    setRunIterations(N_ITER);
  }

  public void setUp()
  {
    System.out.println("OverlaySR perf test");
    System.out.println("SineStar: origin: ("
        + ORG_X + ", " + ORG_Y + ")  size: " + SIZE
        + "  # arms: " + N_ARMS + "  arm ratio: " + ARM_RATIO);   
    System.out.println("# Iterations: " + N_ITER);
    System.out.println("# Test Cases: " + NUM_CASES);
  }
  
  public void startRun(int npts)
  {
    iter = 0;
    precisionModel = new PrecisionModel(100000);

    geomA = SineStarFactory.create(new Coordinate(ORG_X, ORG_Y), SIZE, npts, N_ARMS, ARM_RATIO);

    geomB = new Geometry[ NUM_CASES ];
    createTestGeoms(npts);

    System.out.println("\nRunning with # pts = " + npts );

    //if (size <= 1000) System.out.println(sineStar);
  }
  
  private void createTestGeoms(int npts) {
    int index = 0;
    for (int i = 0; i < GRID_SIZE; i++) {
      for (int j = 0; j < GRID_SIZE; j++) {
        double x = ORG_X + i * GRID_CELL_SIZE;
        double y = ORG_Y + i * GRID_CELL_SIZE;
        Geometry geom = SineStarFactory.create(new Coordinate(x, y), SIZE, npts, N_ARMS, ARM_RATIO);
        geomB[index++] = geom;
      }
    }
  }

  private int iter = 0;
  
  public void runTest1()
  {
    for (Geometry b : geomB) {
      //OverlaySR.overlay(geomA, b, precisionModel, OverlayOp.INTERSECTION);
      geomA.intersection(b);
    }
  }
}
