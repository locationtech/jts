/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.perf.operation.overlayng;

import static org.locationtech.jts.operation.overlayng.OverlayNG.UNION;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.ValidatingNoder;
import org.locationtech.jts.noding.snap.SnappingNoder;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;

/**
 * Runs overlay operations on pairs of random polygonal geometries
 * to see if errors occur.
 * 
 * For OverlayNG a spectrum of scale factors is used.
 * Using a Floating precision model can be optionally specified.
 * 
 * @author mdavis
 *
 */
public class RandomPolygonOverlayFuzzer {
  
  private void overlay(Geometry poly1, Geometry poly2) {
    //overlayOrig(poly1, poly2);
    //overlayOrigNoSnap(poly1, poly2);
    //overlayNGFloat(poly1, poly2);
    overlayNGRobust(poly1, poly2);
    //overlayNG(poly1, poly2);
    //overlayNGSnapping(poly1, poly2);
  }
  
  static final boolean IS_VERBOSE = false;
  
  static final boolean IS_SAME_VORONOI = false;
  
  static final int N_PTS = 100;

  static final int N_TESTS = 1000;
  
  static double SCALE = 100000000;
  
  static double[] SCALES = new double[] {
    // 0, // floating PM
    1, 100, 10000, 1000000, 100000000, 1e12
    // , 1e15  
  };
  
  static void log(String msg) {
    if (IS_VERBOSE) {
      System.out.println(msg);
    }
  }
  
  private boolean useSameBase() {
    return 0 == testIndex % 2;
    //return false;
  }
  
  public static void main(String args[]) {
    (new RandomPolygonOverlayFuzzer()).run();
  }

  private int testIndex = 0;
  private int errCount = 0;
  private String testDesc = "";

  private void run() {
    System.out.printf("Running %d tests\n", N_TESTS);
    
    for (int i = 1; i <= N_TESTS; i++) {
      testIndex = i;
      overlayPolys();
      
      if( (i+1) % 100 == 0) {
        System.out.print(".");
        if( (i+1) % 10000 == 0) {
          System.out.println();
        }
      }
    }
    System.out.println("\n============================");
    System.out.printf("Tests: %d  Errors: %d\n", N_TESTS, errCount);
  }

  private void overlayPolys() {
    boolean isUseSameBase = useSameBase();
    Geometry[] poly = createPolygons(N_PTS, isUseSameBase);    
    process(poly[0], poly[1]);
  }

  private void process(Geometry poly1, Geometry poly2) {
    try {
      overlay(poly1, poly2);
    }
    catch (TopologyException ex) {
      errCount ++;
      System.out.println(stats());
      System.out.printf("ERROR - %s\n", ex.getMessage());
      System.out.println(poly1);
      System.out.println(poly2);
    }
  }
 
  private String stats() {
    return String.format("\nTest %d: %s   (# errs: %d = %d%%)\n", testIndex, testDesc, 
        errCount, (int) (100 * errCount) / testIndex );
  }

  private void overlayNG(Geometry poly1, Geometry poly2) {
    log("Test: " + testIndex + "  --------------------");
    for (double scale : SCALES) {
      overlayNG(poly1, poly2, scale);
    }
  }
  
  private void overlayNG(Geometry poly1, Geometry poly2, double scale) {
    testDesc = String.format("OverlayNG  scale: %f", scale);
    log(testDesc);
    PrecisionModel pm = precModel(scale);
    
    Geometry inter = OverlayNG.overlay(poly1, poly2, 
        OverlayNG.INTERSECTION,
        pm);
    Geometry symDiff = OverlayNG.overlay(poly1, poly2, 
        OverlayNG.SYMDIFFERENCE,
        pm);
    Geometry union = OverlayNG.overlay(onlyPolys(inter), onlyPolys(symDiff), 
        OverlayNG.UNION,
        pm);
  }
  
  private static Geometry onlyPolys(Geometry geom) {
    List polyList = PolygonExtracter.getPolygons(geom);
    return geom.getFactory().createMultiPolygon(GeometryFactory.toPolygonArray(polyList));
  }
  
  private PrecisionModel precModel(double scale) {
    // floating PM
    if (scale <= 0) return new PrecisionModel();
    
    return new PrecisionModel(scale);
  }

  private void overlayNGFloat(Geometry poly1, Geometry poly2) {
    OverlayNG.overlay(poly1, poly2, OverlayNG.INTERSECTION);
    //Geometry diff1 = poly1.difference(poly2);
    //Geometry diff2 = poly2.difference(poly1);
    //Geometry union = inter.union(diff1).union(diff2);
  }

  private void overlayNGRobust(Geometry poly1, Geometry poly2) {
    OverlayNGRobust.overlay(poly1, poly2, OverlayNG.INTERSECTION);
    poly1.intersection(poly2);
    //Geometry diff1 = poly1.difference(poly2);
    //Geometry diff2 = poly2.difference(poly1);
    //Geometry union = inter.union(diff1).union(diff2);
  }
  

  
  private void overlayOrig(Geometry poly1, Geometry poly2) {
    poly1.intersection(poly2);
    //Geometry diff1 = poly1.difference(poly2);
    //Geometry diff2 = poly2.difference(poly1);
    //Geometry union = inter.union(diff1).union(diff2);
  }
  
  private void overlayOrigNoSnap(Geometry a, Geometry b) {
    OverlayOp.overlayOp(a, b, OverlayOp.INTERSECTION);
  }
  
  private void overlayNGSnapping(Geometry a, Geometry b) {
    unionSnap(a, b, 0.00001);
  }
  
  public static Geometry unionSnap(Geometry a, Geometry b, double tolerance) {
    Noder noder = getNoder(tolerance);
    return OverlayNG.overlay(a, b, UNION, null, noder );
  }

  private static Noder getNoder(double tolerance) {
    SnappingNoder snapNoder = new SnappingNoder(tolerance);
    return new ValidatingNoder(snapNoder);
  }
  //=======================================
  
  private static Geometry[] createPolygons(int npts, boolean isUseSameBase) {
    RandomPolygonBuilder builder = new RandomPolygonBuilder(npts);
    Geometry poly1 = builder.createPolygon();
    
    RandomPolygonBuilder builder2 = builder;
    if (! isUseSameBase) {
      builder2 = new RandomPolygonBuilder(npts);
    }
    Geometry poly2 = builder2.createPolygon();
    poly2 = perturbByRotation(poly2);
    
    //System.out.println(poly1);
    //System.out.println(poly2);

    //checkValid(poly1);
    //checkValid(poly2);
    
    return new Geometry[] { poly1, poly2 };
  }

  private static Geometry perturbByRotation(Geometry geom) {
    AffineTransformation rot = AffineTransformation.rotationInstance(2 * Math.PI);
    Geometry geomRot = geom.copy();
    geomRot.apply(rot);
    return geomRot;
  }
  
  private void checkValid(Geometry poly) {
    if (poly.isValid()) return;
    System.out.println("INVALID!");
    System.out.println(poly);
  }
}
