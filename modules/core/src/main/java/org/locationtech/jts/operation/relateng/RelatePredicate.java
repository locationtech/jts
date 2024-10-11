/*
 * Copyright (c) 2023 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.relateng;

import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Location;

/**
 * Creates predicate instances for evaluating OGC-standard named topological relationships.
 * Predicates can be evaluated for geometries using {@link RelateNG}.
 * 
 * @author Martin Davis
 *
 */
public interface RelatePredicate {

  /**
   * Creates a predicate to determine whether two geometries intersect.
   * <p>
   * The <code>intersects</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The two geometries have at least one point in common
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * at least one of the patterns
   *  <ul>
   *   <li><code>[T********]</code>
   *   <li><code>[*T*******]</code>
   *   <li><code>[***T*****]</code>
   *   <li><code>[****T****]</code>
   *  </ul>
   * <li><code>disjoint() = false</code>
   * <br>(<code>intersects</code> is the inverse of <code>disjoint</code>)
   * </ul>
   *
   *@return the predicate instance
   *
   * @see #disjoint()
   */
  public static TopologyPredicate intersects() {
    return new BasicPredicate() {
      
      public String name() { return "intersects"; }
  
      @Override
      public boolean requireSelfNoding() {
        //-- self-noding is not required to check for a simple interaction
        return false;
      }
      
      @Override
      public boolean requireExteriorCheck(boolean isSourceA) {
        //-- intersects only requires testing interaction
        return false;
      }
      
      @Override
      public void init(Envelope envA, Envelope envB) {
        require(envA.intersects(envB));
      }
      
      @Override
      public void updateDimension(int locA, int locB, int dimension) {
        setValueIf(true, isIntersection(locA, locB));
      }
  
      @Override
      public void finish() {
        //-- if no intersecting locations were found
        setValue(false);
      }
  
    };
  }

  /**
   * Creates a predicate to determine whether two geometries are disjoint.
   * <p>
   * The <code>disjoint</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The two geometries have no point in common
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * <code>[FF*FF****]</code>
   * <li><code>intersects() = false</code>
   * <br>(<code>disjoint</code> is the inverse of <code>intersects</code>)
   * </ul>
   *
   *@return the predicate instance
   *
   * @see #intersects()
   */
  public static TopologyPredicate disjoint() {
    return new BasicPredicate() {
       
      public String name() { return "disjoint"; }
      
      @Override
      public boolean requireSelfNoding() {
        //-- self-noding is not required to check for a simple interaction
        return false;
      }
      
      @Override
      public boolean requireInteraction() {
        //-- ensure entire matrix is computed
        return false;
      }
      
      @Override
      public boolean requireExteriorCheck(boolean isSourceA) {
        //-- disjoint only requires testing interaction
        return false;
      }

      @Override
      public void init(Envelope envA, Envelope envB) {
        setValueIf(true, envA.disjoint(envB));
      }

      @Override
      public void updateDimension(int locA, int locB, int dimension) {
        setValueIf(false, isIntersection(locA, locB));
      }
  
      @Override
      public void finish() {
        //-- if no intersecting locations were found
        setValue(true);
      }
  
    };
  }

  /**
   * Creates a predicate to determine whether a geometry contains another geometry.
   * <p>
   * The <code>contains</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>Every point of the other geometry is a point of this geometry,
   * and the interiors of the two geometries have at least one point in common.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * the pattern
   * <code>[T*****FF*]</code>
   * <li><code>within(B, A) = true</code>
   * <br>(<code>contains</code> is the converse of {@link #within} )
   * </ul>
   * An implication of the definition is that "Geometries do not
   * contain their boundary".  In other words, if a geometry A is a subset of
   * the points in the boundary of a geometry B, <code>B.contains(A) = false</code>.
   * (As a concrete example, take A to be a LineString which lies in the boundary of a Polygon B.)
   * For a predicate with similar behavior but avoiding
   * this subtle limitation, see {@link #covers}.
   *
   *@return the predicate instance
   *
   * @see #within()
   */
  public static TopologyPredicate contains() {
    return new IMPredicate() {
  
    public String name() { return "contains"; }
      
    @Override
    public boolean requireCovers(boolean isSourceA) {
      return isSourceA == RelateGeometry.GEOM_A;
    }
    
    @Override
    public boolean requireExteriorCheck(boolean isSourceA) {
      //-- only need to check B against Exterior of A
      return isSourceA == RelateGeometry.GEOM_B;
    }
    
    @Override
    public void init(int dimA, int dimB) {
      super.init(dimA, dimB);
      require( isDimsCompatibleWithCovers(dimA, dimB) );
    }
    
    @Override
    public void init(Envelope envA, Envelope envB) {
      requireCovers(envA, envB);
    }
  
    @Override
    public boolean isDetermined() {
      return intersectsExteriorOf(RelateGeometry.GEOM_A);
    }
  
    @Override
    public boolean valueIM() {
      return intMatrix.isContains();
    }
  };
  }

  /**
   * Creates a predicate to determine whether a geometry is within another geometry.
   * <p>
   * The <code>within</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>Every point of this geometry is a point of the other geometry,
   * and the interiors of the two geometries have at least one point in common.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * <code>[T*F**F***]</code>
   * <li><code>contains(B, A) = true</code>
   * <br>(<code>within</code> is the converse of {@link #contains})
   * </ul>
   * An implication of the definition is that
   * "The boundary of a Geometry is not within the Geometry".
   * In other words, if a geometry A is a subset of
   * the points in the boundary of a geometry B, <code>within(B, A) = false</code>
   * (As a concrete example, take A to be a LineString which lies in the boundary of a Polygon B.)
   * For a predicate with similar behavior but avoiding
   * this subtle limitation, see {@link #coveredBy}.
   *
   *@return the predicate instance
   *
   * @see #contains()
   */
  public static TopologyPredicate within() {
    return new IMPredicate() {
  
      public String name() { return "within"; }
      
      @Override
      public boolean requireCovers(boolean isSourceA) {
        return isSourceA == RelateGeometry.GEOM_B;
      }
      
      @Override
      public boolean requireExteriorCheck(boolean isSourceA) {
        //-- only need to check A against Exterior of B
        return isSourceA == RelateGeometry.GEOM_A;
      }
       
      @Override
      public void init(int dimA, int dimB) {
        super.init(dimA, dimB);
        require( isDimsCompatibleWithCovers(dimB, dimA) );
      }
      
      @Override
      public void init(Envelope envA, Envelope envB) {
        requireCovers(envB, envA);
      }
    
      @Override
      public boolean isDetermined() {
        return intersectsExteriorOf(RelateGeometry.GEOM_B);
      }
      
      public boolean valueIM() {
          return intMatrix.isWithin();
      }
    };
  }

  /**
   * Creates a predicate to determine whether a geometry covers another geometry.
   * <p>
   * The <code>covers</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>Every point of the other geometry is a point of this geometry.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * at least one of the following patterns:
   *  <ul>
   *   <li><code>[T*****FF*]</code>
   *   <li><code>[*T****FF*]</code>
   *   <li><code>[***T**FF*]</code>
   *   <li><code>[****T*FF*]</code>
   *  </ul>
   * <li><code>coveredBy(b, a) = true</code>
   * <br>(<code>covers</code> is the converse of {@link #coveredBy})
   * </ul>
   * If either geometry is empty, the value of this predicate is <code>false</code>.
   * <p>
   * This predicate is similar to {@link #contains()},
   * but is more inclusive (i.e. returns <code>true</code> for more cases).
   * In particular, unlike <code>contains</code> it does not distinguish between
   * points in the boundary and in the interior of geometries.
   * For most cases, <code>covers</code> should be used in preference to <code>contains</code>.
   * As an added benefit, <code>covers</code> is more amenable to optimization,
   * and hence should be more performant.
   *
   *@return the predicate instance
   *
   * @see #coveredBy()
   */
  public static TopologyPredicate covers() {
    return new IMPredicate() {
  
      public String name() { return "covers"; }
      
      @Override
      public boolean requireCovers(boolean isSourceA) {
        return isSourceA == RelateGeometry.GEOM_A;
      }
      
      @Override
      public boolean requireExteriorCheck(boolean isSourceA) {
        //-- only need to check B against Exterior of A
        return isSourceA == RelateGeometry.GEOM_B;
      }
      
      @Override
      public void init(int dimA, int dimB) {
        super.init(dimA, dimB);
        require( isDimsCompatibleWithCovers(dimA, dimB) );
      }
      
      @Override
      public void init(Envelope envA, Envelope envB) {
        requireCovers(envA, envB);
      }
  
      @Override
      public boolean isDetermined() {
        return intersectsExteriorOf(RelateGeometry.GEOM_A);
      }
      
      @Override
      public boolean valueIM() {
        return intMatrix.isCovers();
      }
    };
  }
  
  /**
   * Creates a predicate to determine whether a geometry is covered by another geometry.
   * <p>
   * The <code>coveredBy</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>Every point of this geometry is a point of the other geometry.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * at least one of the following patterns:
   *  <ul>
   *   <li><code>[T*F**F***]</code>
   *   <li><code>[*TF**F***]</code>
   *   <li><code>[**FT*F***]</code>
   *   <li><code>[**F*TF***]</code>
   *  </ul>
   * <li><code>covers(B, A) = true</code>
   * <br>(<code>coveredBy</code> is the converse of {@link #covers})
   * </ul>
   * If either geometry is empty, the value of this predicate is <code>false</code>.
   * <p>
   * This predicate is similar to {@link #within},
   * but is more inclusive (i.e. returns <code>true</code> for more cases).
   *
   *@return the predicate instance
   *
   * @see #covers()
   */
  public static TopologyPredicate coveredBy() {
    return new IMPredicate() {
      public String name() { return "coveredBy"; }
      
      @Override
      public boolean requireCovers(boolean isSourceA) {
        return isSourceA == RelateGeometry.GEOM_B;
      }
      
      @Override
      public boolean requireExteriorCheck(boolean isSourceA) {
        //-- only need to check A against Exterior of B
        return isSourceA == RelateGeometry.GEOM_A;
      }
      
      @Override
      public void init(int dimA, int dimB) {
        super.init(dimA, dimB);
        require( isDimsCompatibleWithCovers(dimB, dimA) );
      }
      
      @Override
      public void init(Envelope envA, Envelope envB) {
        requireCovers(envB, envA);
      }
  
      @Override
      public boolean isDetermined() {
        return intersectsExteriorOf(RelateGeometry.GEOM_B);
      }

      @Override
      public boolean valueIM() {
        return intMatrix.isCoveredBy();
      }
    };
  }

  /**
   * Creates a predicate to determine whether a geometry crosses another geometry.
   * <p>
   * The <code>crosses</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The geometries have some but not all interior points in common.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * one of the following patterns:
   *   <ul>
   *    <li><code>[T*T******]</code> (for P/L, P/A, and L/A cases)
   *    <li><code>[T*****T**]</code> (for L/P, A/P, and A/L cases)
   *    <li><code>[0********]</code> (for L/L cases)
   *   </ul>
   * </ul>
   * For the A/A and P/P cases this predicate returns <code>false</code>.
   * <p>
   * The SFS defined this predicate only for P/L, P/A, L/L, and L/A cases.
   * To make the relation symmetric
   * JTS extends the definition to apply to L/P, A/P and A/L cases as well.
   *
   * @return the predicate instance
   */
  public static TopologyPredicate crosses() {
    return new IMPredicate() {
      public String name() { return "crosses"; }
      
      @Override
      public void init(int dimA, int dimB) {
        super.init(dimA, dimB);
        boolean isBothPointsOrAreas = (dimA == Dimension.P && dimB == Dimension.P)
            ||  (dimA == Dimension.A && dimB == Dimension.A);
        require(! isBothPointsOrAreas);
      }
  
      @Override
      public boolean isDetermined() {
        if (dimA == Dimension.L && dimB == Dimension.L) {
          //-- L/L interaction can only be dim = P
          if (getDimension(Location.INTERIOR, Location.INTERIOR) > Dimension.P)
            return true;
        }
        else if (dimA < dimB) {
          if (isIntersects(Location.INTERIOR, Location.INTERIOR)
              && isIntersects(Location.INTERIOR, Location.EXTERIOR)) {
            return true;
          }
        }
        else if (dimA > dimB) {
          if (isIntersects(Location.INTERIOR, Location.INTERIOR)
              && isIntersects(Location.EXTERIOR, Location.INTERIOR)) {
            return true;
          }
        }
        return false;
      }
  
      @Override
      public boolean valueIM() {
        return intMatrix.isCrosses(dimA, dimB);
      }
    };
  }

  /**
   * Creates a predicate to determine whether two geometries are topologically equal.
   * <p>
   * The <code>equals</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The two geometries have at least one point in common,
   * and no point of either geometry lies in the exterior of the other geometry.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * the pattern <code>T*F**FFF*</code>
   * </ul>
   *
   * @return the predicate instance
   */
  public static TopologyPredicate equalsTopo() {
    return new IMPredicate() {
      public String name() { return "equals"; }
      
      @Override
      public void init(int dimA, int dimB) {
        super.init(dimA, dimB);
        //-- don't require equal dims, because EMPTY = EMPTY for all dims
      }
      
      @Override
      public boolean requireInteraction() {
        //-- allow EMPTY = EMPTY
        return false;
      };
    
      @Override
      public void init(Envelope envA, Envelope envB) {
        //-- handle EMPTY = EMPTY cases
        setValueIf(true, envA.isNull() && envB.isNull());
        
        require(envA.equals(envB));
      }   
      
      @Override
      public boolean isDetermined() {
        boolean isEitherExteriorIntersects = 
            isIntersects(Location.INTERIOR, Location.EXTERIOR)
        || isIntersects(Location.BOUNDARY, Location.EXTERIOR)
        || isIntersects(Location.EXTERIOR, Location.INTERIOR)
        || isIntersects(Location.EXTERIOR, Location.BOUNDARY);

        return isEitherExteriorIntersects;
      }
  
      @Override
      public boolean valueIM() {
        return intMatrix.isEquals(dimA, dimB);
      }
    };
  }

  /**
   * Creates a predicate to determine whether a geometry overlaps another geometry.
   * <p>
   * The <code>overlaps</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The geometries have at least one point each not shared by the other
   *     (or equivalently neither covers the other),
   *     they have the same dimension,
   *     and the intersection of the interiors of the two geometries has
   *     the same dimension as the geometries themselves.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   *     <code>[T*T***T**]</code> (for P/P and A/A cases)
   *     or <code>[1*T***T**]</code> (for L/L cases)
   * </ul>
   * If the geometries are of different dimension this predicate returns <code>false</code>.
   * This predicate is symmetric.
   *
   * @return the predicate instance
   */
  public static TopologyPredicate overlaps() {
    return new IMPredicate() {
      public String name() { return "overlaps"; }
      
      @Override
      public void init(int dimA, int dimB) {
        super.init(dimA, dimB);
        require(dimA == dimB);
      }
  
      @Override
      public boolean isDetermined() {
        if (dimA == Dimension.A || dimA == Dimension.P) {
          if (isIntersects(Location.INTERIOR, Location.INTERIOR)
              && isIntersects(Location.INTERIOR, Location.EXTERIOR)
              && isIntersects(Location.EXTERIOR, Location.INTERIOR))
            return true;          
        }
        if (dimA == Dimension.L) {
          if (isDimension(Location.INTERIOR, Location.INTERIOR, Dimension.L)
              && isIntersects(Location.INTERIOR, Location.EXTERIOR)
              && isIntersects(Location.EXTERIOR, Location.INTERIOR))
            return true;          
        }
        return false;
      }
  
      @Override
      public boolean valueIM() {
        return intMatrix.isOverlaps(dimA, dimB);
      }
    };
  }

  /**
   * Creates a predicate to determine whether a geometry touches another geometry.
   * <p>
   * The <code>touches</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The geometries have at least one point in common,
   * but their interiors do not intersect.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * at least one of the following patterns
   *  <ul>
   *   <li><code>[FT*******]</code>
   *   <li><code>[F**T*****]</code>
   *   <li><code>[F***T****]</code>
   *  </ul>
   * </ul>
   * If both geometries have dimension 0, the predicate returns <code>false</code>,
   * since points have only interiors.
   * This predicate is symmetric.
   *
   * @return the predicate instance
   */
  public static TopologyPredicate touches() {
    return new IMPredicate() {
      public String name() { return "touches"; }
      
      @Override
      public void init(int dimA, int dimB) {
        super.init(dimA, dimB);
        //-- Points have only interiors, so cannot touch
        boolean isBothPoints = dimA == 0 && dimB == 0;
        require(! isBothPoints);
      }
  
      @Override
      public boolean isDetermined() {
        //-- for touches interiors cannot intersect
        boolean isInteriorsIntersects = isIntersects(Location.INTERIOR, Location.INTERIOR);
        return isInteriorsIntersects;
      }
  
      @Override
      public boolean valueIM() {
        return intMatrix.isTouches(dimA, dimB);
      }
    };
  }

  /**
   * Creates a predicate that matches a DE-9IM matrix pattern.
   * 
   * @param imPattern the pattern to match
   * @return a predicate that matches the pattern
   * 
   * @see IntersectionMatrixPattern
   */
  public static TopologyPredicate matches(String imPattern) {
    return new IMPatternMatcher(imPattern);
  }
}
