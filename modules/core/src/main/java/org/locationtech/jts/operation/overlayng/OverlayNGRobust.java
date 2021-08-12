/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import java.util.Collection;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.noding.ValidatingNoder;
import org.locationtech.jts.noding.snap.SnappingNoder;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.operation.union.UnionStrategy;

/**
 * Performs an overlay operation using {@link OverlayNG}, 
 * providing full robustness by using a series of
 * increasingly robust (but slower) noding strategies.
 * <p>
 * The noding strategies used are:
 * <ol>
 * <li>A simple, fast noder using FLOATING precision.
 * <li>A {@link SnappingNoder} using an automatically-determined snap tolerance
 * <li>First snapping each geometry to itself, 
 * and then overlaying them using a <code>SnappingNoder</code>.
 * <li>The above two strategies are repeated with increasing snap tolerance, up to a limit.
 * <li>Finally a {@link org.locationtech.jts.noding.snapround.SnapRoundingNoder} is used with a automatically-determined scale factor
 *     intended to preserve input precision while still preventing robustness problems.
 * </ol>
 * If all of the above attempts fail to compute a valid overlay, 
 * the original {@link TopologyException} is thrown. 
 * In practice this is extremely unlikely to occur.
 * <p>
 * This algorithm relies on each overlay operation execution 
 * throwing a {@link TopologyException} if it is unable
 * to compute the overlay correctly.
 * Generally this occurs because the noding phase does 
 * not produce a valid noding.
 * This requires the use of a {@link ValidatingNoder}
 * in order to check the results of using a floating noder.
 * 
 * @author Martin Davis
 * 
 * @see OverlayNG
 */
public class OverlayNGRobust
{
  /**
   * Computes the unary union of a geometry using robust computation.
   * 
   * @param geom the geometry to union
   * @return the union result
   * 
   * @see UnaryUnionOp
   */
  public static Geometry union(Geometry geom) {
    UnaryUnionOp op = new UnaryUnionOp(geom);
    op.setUnionFunction(OVERLAY_UNION);
    return op.union();
  }
  
  /**
   * Computes the unary union of a collection of geometries using robust computation.
   * 
   * @param geoms the collection of geometries to union
   * @return the union result
   * 
   * @see UnaryUnionOp
   */
  public static Geometry union(Collection<Geometry> geoms) {
    UnaryUnionOp op = new UnaryUnionOp(geoms);
    op.setUnionFunction(OVERLAY_UNION);
    return op.union();
  }
  
  /**
   * Computes the unary union of a collection of geometries using robust computation.
   * 
   * @param geoms the collection of geometries to union
   * @param geomFact the geometry factory to use
   * @return the union of the geometries
   */
  public static Geometry union(Collection<Geometry> geoms, GeometryFactory geomFact) {
    UnaryUnionOp op = new UnaryUnionOp(geoms, geomFact);
    op.setUnionFunction(OVERLAY_UNION);
    return op.union();
  }
  
  private static UnionStrategy OVERLAY_UNION = new UnionStrategy() {

    public Geometry union(Geometry g0, Geometry g1) {
       return overlay(g0, g1, OverlayNG.UNION );
    }

    @Override
    public boolean isFloatingPrecision() {
      return true;
    }
  };
  
  /**
   * Overlay two geometries, using heuristics to ensure
   * computation completes correctly.
   * In practice the heuristics are observed to be fully correct.
   * 
   * @param geom0 a geometry
   * @param geom1 a geometry
   * @param opCode the overlay operation code (from {@link OverlayNG}
   * @return the overlay result geometry
   * 
   * @see OverlayNG
   */
  public static Geometry overlay(Geometry geom0, Geometry geom1, int opCode)
  {
    Geometry result;
    RuntimeException exOriginal;
    
    /**
     * First try overlay with a FLOAT noder, which is fast and causes least
     * change to geometry coordinates
     * By default the noder is validated, which is required in order
     * to detect certain invalid noding situations which otherwise
     * cause incorrect overlay output.
     */
    try {
      result = OverlayNG.overlay(geom0, geom1, opCode );       
      return result;
    }
    catch (RuntimeException ex) {
      /**
       * Capture original exception,
       * so it can be rethrown if the remaining strategies all fail.
       */
      exOriginal = ex;
    }
    
    /**
     * On failure retry using snapping noding with a "safe" tolerance.
     * if this throws an exception just let it go,
     * since it is something that is not a TopologyException
     */
    result = overlaySnapTries(geom0, geom1, opCode);
    if (result != null)
      return result;
    
    /**
     * On failure retry using snap-rounding with a heuristic scale factor (grid size).
     */
    result = overlaySR(geom0, geom1, opCode);
    if (result != null)
      return result;
    
    /**
     * Just can't get overlay to work, so throw original error.
     */
    throw exOriginal;
  }

  private static final int NUM_SNAP_TRIES = 5;

  /**
   * Attempt overlay using snapping with repeated tries with increasing snap tolerances.
   * 
   * @param geom0
   * @param geom1
   * @param opCode
   * @return the computed overlay result, or null if the overlay fails
   */
  private static Geometry overlaySnapTries(Geometry geom0, Geometry geom1, int opCode) {
    Geometry result;
    double snapTol = snapTolerance(geom0, geom1);
    
    for (int i = 0; i < NUM_SNAP_TRIES; i++) {
      
      result = overlaySnapping(geom0, geom1, opCode, snapTol);
      if (result != null) return result;
      
      /**
       * Now try snapping each input individually, 
       * and then doing the overlay.
       */
      result = overlaySnapBoth(geom0, geom1, opCode, snapTol);
      if (result != null) return result;
      
      // increase the snap tolerance and try again
      snapTol = snapTol * 10;
    }
    // failed to compute overlay
    return null;
  }

  /**
   * Attempt overlay using a {@link SnappingNoder}.
   * 
   * @param geom0
   * @param geom1
   * @param opCode
   * @param snapTol
   * @return the computed overlay result, or null if the overlay fails
   */
  private static Geometry overlaySnapping(Geometry geom0, Geometry geom1, int opCode, double snapTol) {
    try {
      return overlaySnapTol(geom0, geom1, opCode, snapTol);
    }
    catch (TopologyException ex) {
      //---- ignore exception, return null result to indicate failure
      
      //System.out.println("Snapping with " + snapTol + " - FAILED");
      //log("Snapping with " + snapTol + " - FAILED", geom0, geom1);
    }
    return null;
  }

  /**
   * Attempt overlay with first snapping each geometry individually.
   * 
   * @param geom0
   * @param geom1
   * @param opCode
   * @param snapTol
   * @return the computed overlay result, or null if the overlay fails
   */
  private static Geometry overlaySnapBoth(Geometry geom0, Geometry geom1, int opCode, double snapTol) {
    try {
      Geometry snap0 = snapSelf(geom0, snapTol);
      Geometry snap1 = snapSelf(geom1, snapTol); 
       //log("Snapping BOTH with " + snapTol, geom0, geom1);
      
      return overlaySnapTol(snap0, snap1, opCode, snapTol);
    }
    catch (TopologyException ex) {
      //---- ignore exception, return null result to indicate failure
    }
    return null;
  }
  
  /**
   * Self-snaps a geometry by running a union operation with it as the only input.
   * This helps to remove narrow spike/gore artifacts to simplify the geometry,
   * which improves robustness.
   * Collapsed artifacts are removed from the result to allow using
   * it in further overlay operations.
   * 
   * @param geom geometry to self-snap
   * @param snapTol snap tolerance
   * @return the snapped geometry (homogeneous)
   */
  private static Geometry snapSelf(Geometry geom, double snapTol) {
    OverlayNG ov = new OverlayNG(geom, null);
    SnappingNoder snapNoder = new SnappingNoder(snapTol);
    ov.setNoder(snapNoder);
    /**
     * Ensure the result is not mixed-dimension,
     * since it will be used in further overlay computation.
     * It may however be lower dimension, if it collapses completely due to snapping.
     */
    ov.setStrictMode(true);
    return ov.getResult();
  }
  
  private static Geometry overlaySnapTol(Geometry geom0, Geometry geom1, int opCode, double snapTol) {
    SnappingNoder snapNoder = new SnappingNoder(snapTol);
    return OverlayNG.overlay(geom0, geom1, opCode, snapNoder);
  }
  
  //============================================
  
  /**
   * A factor for a snapping tolerance distance which 
   * should allow noding to be computed robustly.
   */
  private static final double SNAP_TOL_FACTOR = 1e12;

  /**
   * Computes a heuristic snap tolerance distance
   * for overlaying a pair of geometries using a {@link SnappingNoder}.
   * 
   * @param geom0
   * @param geom1
   * @return the snap tolerance
   */
  private static double snapTolerance(Geometry geom0, Geometry geom1) {
    double tol0 = snapTolerance(geom0);
    double tol1 = snapTolerance(geom1);
    double snapTol = Math.max(tol0,  tol1);
    return snapTol;
  }
  
  private static double snapTolerance(Geometry geom) {
    double magnitude = ordinateMagnitude(geom);
    return magnitude / SNAP_TOL_FACTOR;
  }
  
  /**
   * Computes the largest magnitude of the ordinates of a geometry,
   * based on the geometry envelope.
   * 
   * @param geom a geometry
   * @return the magnitude of the largest ordinate
   */
  private static double ordinateMagnitude(Geometry geom) {
    if (geom == null || geom.isEmpty()) return 0;
    Envelope env = geom.getEnvelopeInternal();
    double magMax = Math.max(
        Math.abs(env.getMaxX()), Math.abs(env.getMaxY()));
    double magMin = Math.max(
        Math.abs(env.getMinX()), Math.abs(env.getMinY()));
    return Math.max(magMax, magMin);
  }
  
  //===============================================
  /*
  private static void log(String msg, Geometry geom0, Geometry geom1) {
    System.out.println(msg);
    System.out.println(geom0);
    System.out.println(geom1);
  }
  */
  
  /**
   * Attempt Overlay using Snap-Rounding with an automatically-determined
   * scale factor.
   * 
   * @param geom0
   * @param geom1
   * @param opCode
   * @return the computed overlay result, or null if the overlay fails
   */
  private static Geometry overlaySR(Geometry geom0, Geometry geom1, int opCode)
  {
    Geometry result;
    try {
      //System.out.println("OverlaySnapIfNeeded: trying snap-rounding");
      double scaleSafe = PrecisionUtil.safeScale(geom0, geom1);
      PrecisionModel pmSafe = new PrecisionModel(scaleSafe);
      result = OverlayNG.overlay(geom0, geom1, opCode, pmSafe);
      return result;
    }
    catch (TopologyException ex) {
      //---- ignore exception, return null result to indicate failure
    }
    return null;
  }

}
