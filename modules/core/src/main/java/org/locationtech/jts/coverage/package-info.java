/*
 * Copyright (c) 2022 Martin Davis.
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
 * Classes that operate on <b>simple polygonal coverages</b>.
 * <p>
 * A polygonal coverage is a set of polygonal geometries which is <b>non-overlapping</b> 
 * and <b>edge-matched</b>. 
 * ({@link Polygon}s or {@link MultiPolygon}s).
 * A set of polygonal geometries is a valid coverage if:
 * <ol>
 * <li>Each geometry is valid
 * <li>The interiors of all polygons are disjoint (they are <b>non-overlapping</b>).
 * This is the case if no polygon has a boundary which intersects the interior of another polygon.
 * <li>Where polygons are adjacent (i.e. their boundaries intersect), 
 * they are <b>edge-matched</b>: the vertices
 * (and thus line segments) of the common boundary sections match exactly.
 * </ol> 
 * A valid polygonal coverage may contain gaps, holes and disjoint regions.
 * <p>
 * Coverage algorithms 
 * generally require the input coverage to be valid to produce correct results.
 * Coverages can be validated using {@link CoverageValidator}.
 * Invalid coverages containing topological errors such as gaps and overlaps 
 * can be repaired using {@link CoverageCleaner}.
 * <p>
 * Modeling a polygonal coverage as a set of discrete geometries with implicit topology
 * means that all JTS operations can be used on the coverage elements.
 * The coverage topology allows efficient
 * set-wise operations to be performed, including:
 * <ul>
 * <li>{@link CoverageUnion} merges the elements in a coverage. The coverage boundary is preserved, 
 * so this can be used on subsets of the coverage safely. 
 * <li>{@link CoverageSimplifier} simplifies the edges of a coverage while preserving the topology
 * </ul>
 */
package org.locationtech.jts.coverage;