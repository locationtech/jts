


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
package org.locationtech.jts.operation.relate;

import org.locationtech.jts.algorithm.BoundaryNodeRule;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.operation.GeometryGraphOperation;

/**
 * Implements the SFS <tt>relate()</tt> generalized spatial predicate on two {@link Geometry}s.
 * <p>
 * The class supports specifying a custom {@link BoundaryNodeRule}
 * to be used during the relate computation.
 * <p>
 * If named spatial predicates are used on the result {@link IntersectionMatrix}
 * of the RelateOp, the result may or not be affected by the 
 * choice of <tt>BoundaryNodeRule</tt>, depending on the exact nature of the pattern.
 * For instance, {@link IntersectionMatrix#isIntersects()} is insensitive 
 * to the choice of <tt>BoundaryNodeRule</tt>, 
 * whereas {@link IntersectionMatrix#isTouches(int, int)} is affected by the rule chosen.
 * <p>
 * <b>Note:</b> custom Boundary Node Rules do not (currently)
 * affect the results of other {@link Geometry} methods (such
 * as {@link Geometry#getBoundary}.  The results of
 * these methods may not be consistent with the relationship computed by
 * a custom Boundary Node Rule.
 *
 * @version 1.7
 */
public class RelateOp
  extends GeometryGraphOperation
{
  /**
   * Computes the {@link IntersectionMatrix} for the spatial relationship
   * between two {@link Geometry}s, using the default (OGC SFS) Boundary Node Rule
   *
   * @param a a Geometry to test
   * @param b a Geometry to test
   * @return the IntersectonMatrix for the spatial relationship between the geometries
   */
  public static IntersectionMatrix relate(Geometry a, Geometry b)
  {
    RelateOp relOp = new RelateOp(a, b);
    IntersectionMatrix im = relOp.getIntersectionMatrix();
    return im;
  }

  /**
   * Computes the {@link IntersectionMatrix} for the spatial relationship
   * between two {@link Geometry}s using a specified Boundary Node Rule.
   *
   * @param a a Geometry to test
   * @param b a Geometry to test
   * @param boundaryNodeRule the Boundary Node Rule to use
   * @return the IntersectonMatrix for the spatial relationship between the input geometries
   */
  public static IntersectionMatrix relate(Geometry a, Geometry b, BoundaryNodeRule boundaryNodeRule)
  {
    RelateOp relOp = new RelateOp(a, b, boundaryNodeRule);
    IntersectionMatrix im = relOp.getIntersectionMatrix();
    return im;
  }

  private RelateComputer relate;

  /**
   * Creates a new Relate operation, using the default (OGC SFS) Boundary Node Rule.
   *
   * @param g0 a Geometry to relate
   * @param g1 another Geometry to relate
   */
  public RelateOp(Geometry g0, Geometry g1)
  {
    super(g0, g1);
    relate = new RelateComputer(arg);
  }

  /**
   * Creates a new Relate operation with a specified Boundary Node Rule.
   *
   * @param g0 a Geometry to relate
   * @param g1 another Geometry to relate
   * @param boundaryNodeRule the Boundary Node Rule to use
   */
  public RelateOp(Geometry g0, Geometry g1, BoundaryNodeRule boundaryNodeRule)
  {
    super(g0, g1, boundaryNodeRule);
    relate = new RelateComputer(arg);
  }

  /**
   * Gets the IntersectionMatrix for the spatial relationship
   * between the input geometries.
   *
   * @return the IntersectonMatrix for the spatial relationship between the input geometries
   */
  public IntersectionMatrix getIntersectionMatrix()
  {
    return relate.computeIM();
  }

}
