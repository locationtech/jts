package test.jts.perf.operation.overlayng;

import static org.locationtech.jts.operation.overlayng.OverlayNG.INTERSECTION;
import static org.locationtech.jts.operation.overlayng.OverlayNG.UNION;
import static org.locationtech.jts.operation.overlayng.OverlayNG.DIFFERENCE;
import static org.locationtech.jts.operation.overlayng.OverlayNG.SYMDIFFERENCE;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.operation.overlayng.OverlayNG;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

public class OverlayNGPerfTest 
extends PerformanceTestCase
{
  private static final int PREC_SCALE_FACTOR = 1000000;

  private static final int N_ITER = 1;
  
  static double ORG_X = 100;
  static double ORG_Y = ORG_X;
  static double SIZE = 2 * ORG_X;
  static int N_ARMS = 6;
  static double ARM_RATIO = 0.3;
  
  static int GRID_SIZE = 20;
  static double GRID_CELL_SIZE = SIZE / GRID_SIZE;
  
  static int NUM_CASES = GRID_SIZE * GRID_SIZE;
  
  private Geometry geomA;
  private Geometry[] geomB;

  private PrecisionModel precisionModel;
  
  public static void main(String args[]) {
    PerformanceTestRunner.run(OverlayNGPerfTest.class);
  }
  
  public OverlayNGPerfTest(String name) {
    super(name);
    setRunSize(new int[] { 100, 1000, 10000, 100000, 200000 });
    //setRunSize(new int[] { 200000 });
    setRunIterations(N_ITER);
  }

  public void setUp()
  {
    System.out.println("OverlaySR perf test");
    System.out.println("SineStar: origin: ("
        + ORG_X + ", " + ORG_Y + ")  size: " + SIZE
        + "  # arms: " + N_ARMS + "  arm ratio: " + ARM_RATIO);   
    System.out.println("# Iterations: " + N_ITER);
    System.out.println("# B geoms: " + NUM_CASES);
    System.out.println("Precision scale: " + PREC_SCALE_FACTOR);
  }
  
  public void startRun(int npts)
  {
    iter = 0;
    precisionModel = new PrecisionModel(PREC_SCALE_FACTOR);

    geomA = SineStarFactory.create(new Coordinate(ORG_X, ORG_Y), SIZE, npts, N_ARMS, ARM_RATIO);

    int nptsB = npts / NUM_CASES;
    if (nptsB < 10 ) nptsB = 10;
    
    geomB =  createTestGeoms(NUM_CASES, nptsB);

    System.out.println("\n-------  Running with A: # pts = " + npts + "   B # pts = " +  nptsB);
    
    if (npts == 999) {
      System.out.println(geomA);
      
      for (Geometry g : geomB) {
        System.out.println(g);
      }
    }

  }
  
  private Geometry[] createTestGeoms(int nGeoms, int npts) {
    Geometry[] geoms = new Geometry[ NUM_CASES ];
    int index = 0;
    for (int i = 0; i < GRID_SIZE; i++) {
      for (int j = 0; j < GRID_SIZE; j++) {
        double x = GRID_CELL_SIZE/2 + i * GRID_CELL_SIZE;
        double y = GRID_CELL_SIZE/2 + j * GRID_CELL_SIZE;
        Geometry geom = SineStarFactory.create(new Coordinate(x, y), GRID_CELL_SIZE, npts, N_ARMS, ARM_RATIO);
        geoms[index++] = geom;
      }
    }
    return geoms;
  }

  private int iter = 0;
  
  public void runIntersectionOLD()
  {
    for (Geometry b : geomB) {
      geomA.intersection(b);
    }
  }  
  
  public void xrunUnionNG()
  {
    for (Geometry b : geomB) {
      OverlayNG.overlay(geomA, b, UNION, precisionModel);
    }
  }
  
  public void xrunUnionOLD()
  {
    for (Geometry b : geomB) {
      geomA.union(b);
    }
  }
  
  public void runIntersectionOLDOpt()
  {
    for (Geometry b : geomB) {
      intersectionOpt(geomA, b);
    }
  }
  
  public void runIntersectionNG()
  {
    for (Geometry b : geomB) {
      OverlayNG.overlay(geomA, b, INTERSECTION, precisionModel);
    }
  }  
  
  public void runIntersectionNGFloating()
  {
    for (Geometry b : geomB) {
      intersectionNGFloating(geomA, b);
    }
  }  
  
  public void runIntersectionNGOpt()
  {
    for (Geometry b : geomB) {
      intersectionNGOpt(geomA, b);
    }
  }
  
  public void xrunIntersectionNGNoClip()
  {
    for (Geometry b : geomB) {
      intersectionNGNoClip(geomA, b);
    }
  }
  
  public void xrunIntersectionNGPrepNoCache()
  {
    for (Geometry b : geomB) {
      intersectionNGPrepNoCache(geomA, b);
    }
  }
  
  /**
   * Switching input order doesn't make much difference.
   * Update: actually it looks like having the smaller geometry
   * as the prepared one is faster (by a variable amount)
   */
  public void xrunIntersectionNGPrepNoCacheBA()
  {
    for (Geometry b : geomB) {
      intersectionNGPrepNoCache(b, geomA);
    }
  }
  
  public Geometry intersectionNGOpt(Geometry a, Geometry b) {
    Geometry intFast = fastIntersect(a, b);
    if (intFast != null) return intFast;
    return OverlayNG.overlay(a, b, OverlayNG.INTERSECTION, precisionModel);
  }

  public Geometry intersectionNGNoClip(Geometry a, Geometry b) {
    OverlayNG overlay = new OverlayNG(a, b, precisionModel, OverlayNG.INTERSECTION);
    overlay.setOptimized(false);
    return overlay.getResult();
  }

  public Geometry intersectionNGFloating(Geometry a, Geometry b) {
    OverlayNG overlay = new OverlayNG(a, b, OverlayNG.INTERSECTION);
    overlay.setOptimized(false);
    return overlay.getResult();
  }

  public Geometry intersectionNGPrep(Geometry a, Geometry b) {
    PreparedGeometry pg = cacheFetch(a);
    if (! pg.intersects(b)) return null;
    if (pg.covers(b)) return b.copy();
    return OverlayNG.overlay(a, b, OverlayNG.INTERSECTION, precisionModel);
  }
  
  public Geometry intersectionNGPrepNoCache(Geometry a, Geometry b) {
    Geometry intFast = fastintersectsPrepNoCache(a, b);
    if (intFast != null) return intFast;
 
    return OverlayNG.overlay(a, b, OverlayNG.INTERSECTION, precisionModel);
  }

  private Geometry fastintersectsPrepNoCache(Geometry a, Geometry b) {
    PreparedGeometry aPG = (new PreparedGeometryFactory()).create(a);
    
    if (! aPG.intersects(b)) {
      return a.getFactory().createEmpty(a.getDimension());
    }
    if (aPG.covers(b)) {
      return b.copy();
    }
    if (b.covers(a)) { 
      return a.copy();
    }
    // null indicates full overlay required
    return null;
  }

  private static Geometry fastIntersect(Geometry a, Geometry b) {
    IntersectionMatrix im = a.relate(b);
    if (! im.isIntersects()) 
      return a.getFactory().createEmpty(a.getDimension());
    if (im.isCovers()) 
      return b.copy();
    if (im.isCoveredBy()) 
      return a.copy();
    // null indicates full overlay required
    return null;
  }
  
  /**
   * Use spatial predicates as a filter
   * in front of intersection.
   * 
   * @param a a geometry
   * @param b a geometry
   * @return the intersection of the geometries
   */
  public static Geometry intersectionOpt(Geometry a, Geometry b) {
    Geometry intFast = fastIntersect(a, b);
    if (intFast != null) return intFast;
    return a.intersection(b);
  }
  
  public Geometry intersectionOptPrepNoCache(Geometry a, Geometry b) {
    Geometry intFast = fastintersectsPrepNoCache(a, b);
    if (intFast != null) return intFast;
    return a.intersection(b);
  }

  /**
   * Use prepared geometry spatial predicates as a filter
   * in front of intersection,
   * with the first operand prepared.
   * 
   * @param a a geometry to prepare
   * @param b a geometry
   * @return the intersection of the geometries
   */
  public static Geometry intersectionOptPrep(Geometry a, Geometry b) {
    PreparedGeometry pg = cacheFetch(a);
    if (! pg.intersects(b)) return null;
    if (pg.covers(b)) return b.copy();
    return a.intersection(b);
  }
  
  private static Geometry cacheKey = null;
  private static PreparedGeometry cache = null;
  

  private static PreparedGeometry cacheFetch(Geometry g) {
    if (g != cacheKey) {
      cacheKey = g;
      cache = (new PreparedGeometryFactory()).create(g);
    }
    return cache;
  }
}
