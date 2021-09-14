/*
 * Copyright (c) 2021 Martin Davis
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
 * Classes for representing a planar triangulation as a set of linked triangles.
 * Triangles are represented by memory-efficient {@link Tri} objects.
 * A set of triangles can be linked into a triangulation using {@link TriangulationBuilder}.
 */
package org.locationtech.jts.triangulate.tri;