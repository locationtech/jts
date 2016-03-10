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

package org.locationtech.jts.triangulate;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.WKTWriter;

/**
 * Indicates a failure during constraint enforcement.
 * 
 * @author Martin Davis
 * @version 1.0
 */
public class ConstraintEnforcementException extends RuntimeException {

    private static final long serialVersionUID = 386496846550080140L;

    private static String msgWithCoord(String msg, Coordinate pt) {
        if (pt != null)
            return msg + " [ " + WKTWriter.toPoint(pt) + " ]";
        return msg;
    }

    private Coordinate pt = null;

    /**
     * Creates a new instance with a given message.
     * 
     * @param msg a string
     */
    public ConstraintEnforcementException(String msg) {
        super(msg);
    }

    /**
     * Creates a new instance with a given message and approximate location.
     * 
     * @param msg a string
     * @param pt the location of the error
     */
    public ConstraintEnforcementException(String msg, Coordinate pt) {
        super(msgWithCoord(msg, pt));
        this.pt = new Coordinate(pt);
    }

    /**
     * Gets the approximate location of this error.
     * 
     * @return a location
     */
    public Coordinate getCoordinate() {
        return pt;
    }
}