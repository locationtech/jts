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
 * Contains classes and interfaces implementing linear referencing on linear geometries
 * <H3>Linear Referencing</H3>
 * Linear Referencing is a way of defining positions along linear geometries
 * (<code>LineStrings</code> and <code>MultiLineStrings</code>).
 * It is used extensively in linear network systems.
 * There are numerous possible <b>Linear Referencing Methods</b> which
 * can be used to define positions along linear geometry.
 * This package supports two:
 * <ul>
 * <li><b>Linear Location</b> - a linear location is a triple
 * <code>(component index, segment index, segment fraction)</code>
 * which precisely specifies a point on a linear geometry.
 * It allows for efficient mapping of the index value to actual coordinate values.</li>
 * <li><b>Length</b> - the natural concept of using the length along
 * the geometry to specify a position.</li>
 * </ul>
 *
 * <h2>Package Specification</h2>
 * <ul>
 * <li>Java Topology Suite Technical Specifications</li>
 * <li><A HREF="http://www.opengis.org/techno/specs.htm">
 * OpenGIS Simple Features Specification for SQL</A></li>
 * </ul>
 */
package org.locationtech.jts.linearref;