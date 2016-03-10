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

/**
 * Thrown when an attempt is made to load a shapefile
 * which contains a shape type that is not supported
 * by the loader
 */
public class ShapeTypeNotSupportedException extends ShapefileException{
    public ShapeTypeNotSupportedException(String s){
        super(s);
    }
}