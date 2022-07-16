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
 * Specifically, a polygonal coverage is valid if:
 * <ol>
 * <li>The interiors of all polygons are disjoint
 * This is the case if no polygon has a boundary which intersects the interior of another polygon.
 * <li>If the boundaries of polygons intersect the vertices
 * and line segments of the intersection match exactly.
 * </ol> 
 * 
 */
package org.locationtech.jts.coverage;