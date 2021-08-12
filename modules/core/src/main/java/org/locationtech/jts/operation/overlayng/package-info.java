/*
 * Copyright (c) 2016 Vivid Solutions.
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
 * <p>
 * Contains classes that perform vector overlay
 * to compute boolean set-theoretic spatial functions.
 * Overlay operations are used in spatial analysis for computing set-theoretic
 * operations (boolean combinations) of input {@link org.locationtech.jts.geom.Geometry}s.
 * </p>
 * <p>
 * The {@link org.locationtech.jts.operation.overlayng.OverlayNG} class provides the standard Simple Features
 * boolean set-theoretic overlay operations.
 * These are:
 * <ul>
 * <li><b>Intersection</b> - all points which lie in both geometries</li>
 * <li><b>Union</b> - all points which lie in at least one geometry</li>
 * <li><b>Difference</b> - all points which lie in the first geometry but not the second</li>
 * <li><b>Symmetric Difference</b> - all points which lie in one geometry but not both</li>
 * </ul>
 * These operations are supported for all combinations of the basic geometry types and their homogeneous collections.
 * <p>
 * Additional operations include:
 * <ul>
 * <li>{@link org.locationtech.jts.operation.overlayng.UnaryUnionNG} unions collections of geometries in an efficient way</li>
 * <li>{@link org.locationtech.jts.operation.overlayng.CoverageUnion} provides enhanced performance for unioning
 * valid polygonal and lineal coverages</li>
 * <li>{@link org.locationtech.jts.operation.overlayng.PrecisionReducer} allows reducing the precision of a geometry
 * in a topologically-valid way</li>
 * </ul>
 * <p>
 * <h2>Semantics</h2>
 * The requirements for overlay input are:
 * <ul>
 * <li>Input geometries may have different dimension.</li>
 * <li>Collections must be homogeneous
 * (all elements must have the same dimension).</li>
 * <li>In general, inputs must be valid geometries.</li>
 * <li>However, polygonal inputs may contain the following two kinds of "mild" invalid topology:
 * <ul>
 * <li>rings which self-touch at discrete points (sometimes called inverted shells and exverted holes).</li>
 * <li>rings which touch along line segments (i.e. topology collapse).</li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * The semantics of overlay output are:
 * <ul>
 * <li>Results are always valid geometries.
 * In particular, result <code>MultiPolygon</code>s are valid.</li>
 * <li>Repeated vertices are removed.</li>
 * <li>Linear results include all nodes (endpoints) present in the input.
 * In some cases more nodes will be present.
 * (If merged lines are required see {@link org.locationtech.jts.operation.linemerge.LineMerger}.)</li>
 * <li>Polygon edges which undergo topology collapse to lines
 * (due to rounding or snapping) are included in the result.
 * This means that all operations may produce a heterogeneous result.
 * Usually this only occurs when using a fixed-precision model,
 * but it can happen due to snapping performed to improve robustness.
 * </li>
 * <li>The <code>intersection</code> operation result includes
 * all components of the intersection
 * for geometries which intersect in components of the same and/or lower dimension.</li>
 * <li>The <code>difference</code> operation produces a homogeneous result
 * if no topology collapses are present.
 * In this case the result dimension is equal to that of the left-hand operand.</li>
 * <li>The <code>union</code> and <code>symmetric difference</code> operations
 * may produce a heterogeneous result if the inputs are of mixed dimension.</li>
 * <li>Homogeneous results are output as <code>Multi</code> geometries.</li>
 * <li>Heterogeneous results are output as a <code>GeometryCollection</code>
 * containing a set of atomic geometries.
 * (This provides backwards compatibility with the original overlay implementation.
 * However, it loses the information that the polygonal results
 * have valid <code>MultiPolygon</code> topology.)</li>
 * <li>Empty results are atomic <code>EMPTY</code> geometries
 * of dimension appropriate to the operation.</li>
 * <li>As far as possible, results preserve the order and direction of the inputs.
 * For instance, a MultiLineString intersection with a Polygon
 * will have resultants which are in the same order and have the same direction
 * as the input lines (assuming the input lines are disjoint).
 * If an input line is split into two or more parts,
 * they are ordered in the direction of occurence along their parent line.</li>
 * </ul>
 * <h2>Features</h2>
 * <h3>Functionality</h3>
 * <ul>
 * <li><b>Precision Model</b> - operations are performed using a defined precision model
 * (finite or floating)
 * <li><b>Robust Computation</b> - provides fully robust computation when an appropriate noder is used
 * <li><b>Performance optimizations</b> - including:
 * <ul>
 * <li>Short-circuiting for disjoint input envelopes
 * <li>Reduction of input segment count via clipping / limiting to overlap envelope
 * <li>Optimizations can be disabled if required (e.g. for testing or performance evaluation)
 * </ul>
 * <li><b>Pluggable Noding</b> - allows using different noders to change characteristics of performance and accuracy
 * <li><b>Precision Reduction</b> - in a topologically correct way.
 * Implemented by unioning a single input with an empty geometry
 * <li><b>Topology Correction / Conversion</b> - handles certain kinds
 * of polygonal inputs which are invalid
 * <li><b>Fast Coverage Union</b> - of valid polygonal and linear coverages
 * </ul>
 * <h3>Pluggable Noding</h3>
 * The noding phase of overlay uses a  {@link org.locationtech.jts.noding.Noder} subclass.
 * This is determine automatically based on the precision model of the input.
 * Or it can be provided explicity, which allows changing characteristics
 * of performance and robustness.
 * Examples of relevant noders include:
 * <ul>
 * <li>{@link org.locationtech.jts.noding.MCIndexNoder} - a fast full-precision noder, which however may not produce
 * a valid noding in some situations.
 * Should be combined with a {@link org.locationtech.jts.noding.ValidatingNoder} wrapper to detect
 * noding failures.</li>
 * <li>{@link org.locationtech.jts.noding.snap.SnappingNoder} - a robust full-precision noder</li>
 * <li>{@link org.locationtech.jts.noding.snapround.SnapRoundingNoder} - a noder which enforces a supplied fixed precision model
 * by snapping vertices and intersections to a grid</li>
 * <li>{@link org.locationtech.jts.noding.SegmentExtractingNoder} - a special-purpose noder that provides very fast noding
 * for valid polygonal coverages. Requires node-clean input to operate correctly. </li>
 * </ul>
 * <h3>Topology Correction / Conversion</h3>
 * As noted above, the overlay process
 * can handle polygonal inputs which are invalid according to the OGC topology model
 * in certain limited ways.
 * These invalid conditions are:
 * <ul>
 * <li>rings which self-touch at discrete points (sometimes called inverted shells and exverted holes).</li>
 * <li>rings which touch along line segments (i.e. topology collapse).</li>
 * </ul>
 * These invalidities are corrected during the overlay process.
 * <p>
 * Some of these invalidities are considered as valid in other geometry models.
 * By peforming a self-overlay these inputs can be converted
 * into the JTS OGC topological model.
 * <h3>Codebase</h3>
 * <ul>
 * <li>Defines a simple, full-featured topology model, with clear semantics.
 * The topology model incorporates handling topology collapse, which is
 * essential for snapping and fixed-precision noding.</li>
 * <li>Uses a simple topology graph data structure (based on the winged edge pattern).</li>
 * <li>Decouples noding and topology-build phases.
 * This makes the code clearer, and makes it possible
 * to allow supplying alternate implementations and semantics for each phase.</li>
 * <li>All optimizations are implemented internally,
 * so that clients do not have to add checks such as envelope overlap.</li>
 * </ul>
 * <h2>Algorithm</h2>
 * For non-point inputs the overlay algorithm is:
 * <p>
 * <ol>
 * <li>Check for empty input geometries, and return a result appropriate for the specified operation
 * <li>Extract linework and points from input geometries, with topology location information
 * <li>(If optimization enabled) Apply overlap envelope optimizations:
 * <ol>
 * <li>For Intersection, check if the input envelopes are disjoint
 * (using an envelope expansion adjustment to account for the precision grid).
 * <li>For Intersection and Difference, clip or limit the linework of the input geometries to the overlap envelope.
 * <li>If the optimized linework is empty, return an empty result of appropriate type.
 * </ol>
 * <li>Node the linework.  For full robustness snap-rounding noding is used.
 * Other kinds of noder can be used as well (for instance, the full-precision noding algorithm as the original overlay code).
 * <li>Merge noded edges.
 * Coincident edges from the two input geometries are merged, along with their topological labelling.
 * Topology collapses are detected in this step, and are flagged in the labelling so they can be handled appropriately duing result polygon extraction
 * <li>Build a fully-labelled topology graph.  This includes:
 * <ol>
 * <li>Create a graph structure on the noded, merged edges
 * <li>Propagate topology locations around nodes in the graph
 * <li>Label edges that have incomplete topology locations.
 * These occur when edges from an input geometry are isolated (disjoint from the edges of the other geometry in the graph).
 * </ol>
 * <li>If result is empty return an empty geometry of appropriate type
 * <li>Generate the result geometry from the labelled graph:
 * <ol>
 * <li>Build result polygons
 * <ol>
 * <li>Mark edges which should be included in the result areas
 * <li>Link maximal rings together
 * <li>Convert maximal rings to minimal (valid) rings
 * <li>Determine nesting of holes
 * <li>Construct result polygons
 * </ol>
 * <li>Build result linework
 * <ol>
 * <li>Mark edges to be included in the result lines
 * <li>Extract edges as lines
 * </ol>
 * <li>Build result points (certain intersection situations only)
 * <ol>
 * <li>Output points occur where the inputs touch at single points
 * </ol>
 * <li>Collect result elements into the result geometry
 * </ol>
 * </ol>
 * <h2>Package Specification</h2>
 * <ul>
 * <li><A HREF="http://www.opengis.org/techno/specs.htm">
 * OpenGIS Simple Features Specification for SQL</A>
 * </ul>
 */
package org.locationtech.jts.operation.overlayng;