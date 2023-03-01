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
 * Classes that operate on polygonal coverages.
 * <p>
 * A polygonal coverage is a non-overlapping, fully-noded set of polygons.
 * Specifically, a set of polygons is a valid coverage if:
 * <ol>
 * <li>The interiors of all polygons are disjoint.
 * This is the case if no polygon has a boundary which intersects the interior of another polygon.
 * <li>Where polygons are adjacent (their boundaries intersect), the vertices
 * (and thus line segments) of the common boundary match exactly.
 * </ol> 
 * <p>
 * Coverage algorithms (such as {@link CoverageUnion}) 
 * generally require the input coverage to be valid to produce correct results.
 * Coverages can be validated using {@link CoverageValidator}.
 * 
 * 
 */
package org.locationtech.jts.coverage;