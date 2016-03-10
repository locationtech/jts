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
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Lineal;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.operation.BoundaryOp;
import org.locationtech.jts.operation.IsSimpleOp;
import org.locationtech.jts.operation.relate.RelateOp;

/**
 * An interface for rules which determine whether node points
 * which are in boundaries of {@link Lineal} geometry components
 * are in the boundary of the parent geometry collection.
 * The SFS specifies a single kind of boundary node rule,
 * the {@link Mod2BoundaryNodeRule} rule.
 * However, other kinds of Boundary Node Rules are appropriate
 * in specific situations (for instance, linear network topology
 * usually follows the {@link EndPointBoundaryNodeRule}.)
 * Some JTS operations 
 * (such as {@link RelateOp}, {@link BoundaryOp} and {@link IsSimpleOp})
 * allow the BoundaryNodeRule to be specified,
 * and respect the supplied rule when computing the results of the operation.
 * <p>
 * An example use case for a non-SFS-standard Boundary Node Rule is
 * that of checking that a set of {@link LineString}s have 
 * valid linear network topology, when turn-arounds are represented
 * as closed rings.  In this situation, the entry road to the
 * turn-around is only valid when it touches the turn-around ring
 * at the single (common) endpoint.  This is equivalent 
 * to requiring the set of <tt>LineString</tt>s to be 
 * <b>simple</b> under the {@link EndPointBoundaryNodeRule}.
 * The SFS-standard {@link Mod2BoundaryNodeRule} is not 
 * sufficient to perform this test, since it
 * states that closed rings have <b>no</b> boundary points.
 * <p>
 * This interface and its subclasses follow the <tt>Strategy</tt> design pattern.
 *
 * @author Martin Davis
 * @version 1.7
 *
 * @see RelateOp
 * @see BoundaryOp
 * @see IsSimpleOp
 * @see PointLocator
 */
public interface BoundaryNodeRule
{

	/**
	 * Tests whether a point that lies in <tt>boundaryCount</tt>
	 * geometry component boundaries is considered to form part of the boundary
	 * of the parent geometry.
	 * 
	 * @param boundaryCount the number of component boundaries that this point occurs in
	 * @return true if points in this number of boundaries lie in the parent boundary
	 */
  boolean isInBoundary(int boundaryCount);

  /**
   * The Mod-2 Boundary Node Rule (which is the rule specified in the OGC SFS).
   * @see Mod2BoundaryNodeRule
   */
  public static final BoundaryNodeRule MOD2_BOUNDARY_RULE = new Mod2BoundaryNodeRule();

  /**
   * The Endpoint Boundary Node Rule.
   * @see EndPointBoundaryNodeRule
   */
  public static final BoundaryNodeRule ENDPOINT_BOUNDARY_RULE = new EndPointBoundaryNodeRule();

  /**
   * The MultiValent Endpoint Boundary Node Rule.
   * @see MultiValentEndPointBoundaryNodeRule
   */
  public static final BoundaryNodeRule MULTIVALENT_ENDPOINT_BOUNDARY_RULE = new MultiValentEndPointBoundaryNodeRule();

  /**
   * The Monovalent Endpoint Boundary Node Rule.
   * @see MonoValentEndPointBoundaryNodeRule
   */
  public static final BoundaryNodeRule MONOVALENT_ENDPOINT_BOUNDARY_RULE = new MonoValentEndPointBoundaryNodeRule();

  /**
   * The Boundary Node Rule specified by the OGC Simple Features Specification,
   * which is the same as the Mod-2 rule.
   * @see Mod2BoundaryNodeRule
   */
  public static final BoundaryNodeRule OGC_SFS_BOUNDARY_RULE = MOD2_BOUNDARY_RULE;

  /**
   * A {@link BoundaryNodeRule} specifies that points are in the
   * boundary of a lineal geometry iff
   * the point lies on the boundary of an odd number
   * of components.
   * Under this rule {@link LinearRing}s and closed
   * {@link LineString}s have an empty boundary.
   * <p>
   * This is the rule specified by the <i>OGC SFS</i>,
   * and is the default rule used in JTS.
   *
   * @author Martin Davis
   * @version 1.7
   */
  public static class Mod2BoundaryNodeRule
      implements BoundaryNodeRule
  {
    public boolean isInBoundary(int boundaryCount)
    {
      // the "Mod-2 Rule"
      return boundaryCount % 2 == 1;
    }
  }

  /**
   * A {@link BoundaryNodeRule} which specifies that any points which are endpoints
   * of lineal components are in the boundary of the
   * parent geometry.
   * This corresponds to the "intuitive" topological definition
   * of boundary.
   * Under this rule {@link LinearRing}s have a non-empty boundary
   * (the common endpoint of the underlying LineString).
   * <p>
   * This rule is useful when dealing with linear networks.
   * For example, it can be used to check
   * whether linear networks are correctly noded.
   * The usual network topology constraint is that linear segments may touch only at endpoints.
   * In the case of a segment touching a closed segment (ring) at one point,
   * the Mod2 rule cannot distinguish between the permitted case of touching at the
   * node point and the invalid case of touching at some other interior (non-node) point.
   * The EndPoint rule does distinguish between these cases,
   * so is more appropriate for use.
   *
   * @author Martin Davis
   * @version 1.7
   */
  public static class EndPointBoundaryNodeRule
      implements BoundaryNodeRule
  {
    public boolean isInBoundary(int boundaryCount)
    {
      return boundaryCount > 0;
    }
  }

  /**
   * A {@link BoundaryNodeRule} which determines that only
   * endpoints with valency greater than 1 are on the boundary.
   * This corresponds to the boundary of a {@link MultiLineString}
   * being all the "attached" endpoints, but not
   * the "unattached" ones.
   *
   * @author Martin Davis
   * @version 1.7
   */
  public static class MultiValentEndPointBoundaryNodeRule
      implements BoundaryNodeRule
  {
    public boolean isInBoundary(int boundaryCount)
    {
      return boundaryCount > 1;
    }
  }

  /**
   * A {@link BoundaryNodeRule} which determines that only
   * endpoints with valency of exactly 1 are on the boundary.
   * This corresponds to the boundary of a {@link MultiLineString}
   * being all the "unattached" endpoints.
   *
   * @author Martin Davis
   * @version 1.7
   */
  public static class MonoValentEndPointBoundaryNodeRule
      implements BoundaryNodeRule
  {
    public boolean isInBoundary(int boundaryCount)
    {
      return boundaryCount == 1;
    }
  }


}