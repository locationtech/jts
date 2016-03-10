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

package org.locationtech.jtstest.testbuilder.io.shapefile;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;


public interface ShapeHandler {
    public int getShapeType();
    public Geometry read(EndianDataInputStream file,GeometryFactory geometryFactory,int contentLength) throws java.io.IOException,InvalidShapefileException;
    public int getLength(Geometry geometry); //length in 16bit words
}
