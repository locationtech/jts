/*
 * Copyright (c) 2024 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

/**
 * Provides classes to implement the RelateNG algorithm
 * computes topological relationships of {@link Geometry}s.
 * Topology is evaluated based on the  
 * <a href="https://en.wikipedia.org/wiki/DE-9IM">Dimensionally-Extended 9-Intersection Model</a> (DE-9IM).
 * The {@link RelateNG} class supports computing the value of boolean topological predicates.
 * Standard OGC named predicates are provided by the {@link RelatePredicate} functions.
 * Custom relationships can be specified via testing against DE-9IM matrix patterns
 * (see {@link IntersectionMatrixPattern} for examples).
 * The full DE-9IM {@link IntersectionMatrix} can also be computed.
 * <p>
 * The algorithm has the following capabilities:
 * <ol>
 * <li>Efficient short-circuited evaluation of topological predicates
 *     (including matching custom DE-9IM patterns)
 * <li>Optimized repeated evaluation of predicates against a single geometry 
 *     via cached spatial indexes (AKA "prepared mode")
 * <li>Robust computation (since only point-local topology is required,
 *     so that invalid geometry topology cannot cause failures)
 * <li>Support for mixed-type and overlapping {@link GeometryCollection} inputs
 *     (using <i>union semantics</i>)
 * <li>Support for {@link BoundaryNodeRule}
 * </ol>
 * 
 * RelateNG operates in 2D only; it ignores any Z ordinates.
 * 
 * <h3>Optimized Short-Circuited Evaluation</h3>
 * The RelateNG algorithm uses strategies to optimize the evaluation of
 * topological predicates, including matching DE-9IM matrix patterns.
 * These include fast tests of dimensions and envelopes, and short-circuited evaluation 
 * once the predicate value is known
 * (either satisfied or failed) based on the value of matrix entries.
 * Named predicates used explicit strategy code.
 * DE-9IM matrix pattern matching are short-circuited where possible 
 * based on analysis of the pattern matrix entries.
 * Spatial indexes are used to optimize topological computations
 * (such as locating points in geometry elements, 
 * and analyzing the topological relationship between geometry edges). 
 * 
 * <h3>Execution Modes</h3>
 * RelateNG provides two execution modes for evaluating predicates:
 * <ul>
 * <li><b>Single-shot</b> mode evaluates a predicate for a single case of two geometries.
 * It is provided by the {@link RelateNG} static functions which take two input geometries.
 * <li><b>Prepared</b> mode optimizes repeated evaluation of predicates 
 * against a fixed geometry.  It is used by creating an instance of {@link RelateNG} 
 * on the required geometry with the <tt>prepare</tt> functions, 
 * and then using the <tt>evaluate</tt> methods.
 * It provides much faster performance for repeated operations against a single geometry.
 * </ul>
 * 
 * <h3>Robustness</h3>
 * RelateNG provides robust evaluation of topological relationships,
 * up to the precision of double-precision computation.
 * It computes topological relationships in the locality of discrete points, 
 * without constructing a full topology graph of the inputs.
 * This means that invalid input geometries or numerical round-off do not cause exceptions
 * (although they may return incorrect answers).
 * However, it is necessary to node some inputs together (in particular, linear elements)
 * in order to provide consistent evaluation of the topological structure.
 * 
 * <h3>GeometryCollection Handling</h3>
 * {@link GeometryCollection}s may contain geometries of different dimensions, nested to any level.
 * The element geometries may overlap in any combination.
 * The OGC specification did not provide a definition for the topology
 * of GeometryCollections, or how they behave under the DE-9IM model.
 * RelateNG defines the topology for arbitrary collections of geometries
 * using "union semantics".
 * This is specified as:
 * <ul>
 * <li>GeometryCollections are evaluated as if they were replaced by the topological union
 * of their elements.
 * <li>The topological location at a point is equal to its location in the geometry of highest
 * dimension which contains it.  For example, a point located in the interior of a Polygon
 * and the boundary of a LineString has location <code>Interior</code>.
 * </ul>
 * 
 * <h3>Zero-length LineString Handling</h3>
 * Zero-length LineStrings are handled as topologically identical to a Point at the same coordinate. 
 *  
 * <h2>Package Specification</h2>
 * <ul>
 * <li><A HREF="http://www.opengis.org/techno/specs.htm">
 * OpenGIS Simple Features Specification for SQL</A>
 * </ul>
 */
package org.locationtech.jts.operation.relateng;
