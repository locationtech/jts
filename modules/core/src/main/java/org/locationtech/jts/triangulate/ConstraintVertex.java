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
import org.locationtech.jts.triangulate.quadedge.Vertex;

/**
 * A vertex in a Constrained Delaunay Triangulation.
 * The vertex may or may not lie on a constraint.
 * If it does it may carry extra information about the original constraint.
 * 
 * @author Martin Davis
 */
public class ConstraintVertex extends Vertex {
    private boolean isOnConstraint;
    private Object  constraint = null;

    /**
     * Creates a new constraint vertex
     * 
     * @param p the location of the vertex
     */
    public ConstraintVertex(Coordinate p) {
        super(p);
    }

    /**
     * Sets whether this vertex lies on a constraint.
     * 
     * @param isOnConstraint true if this vertex lies on a constraint
     */
    public void setOnConstraint(boolean isOnConstraint) {
        this.isOnConstraint = isOnConstraint;
    }

    /**
     * Tests whether this vertex lies on a constraint.
     * 
     * @return true if the vertex lies on a constraint
     */
    public boolean isOnConstraint() {
        return isOnConstraint;
    }

    /**
     * Sets the external constraint information
     * 
     * @param constraint an object which carries information about the constraint this vertex lies on
     */
    public void setConstraint(Object constraint) {
        isOnConstraint = true;
        this.constraint = constraint;
    }

    /**
     * Gets the external constraint object
     * 
     * @return the external constraint object
     */
    public Object getConstraint() {
        return constraint;
    }

    /**
     * Merges the constraint data in the vertex <tt>other</tt> into this vertex. 
     * This method is called when an inserted vertex is
     * very close to an existing vertex in the triangulation.
     * 
     * @param other the constraint vertex to merge
     */
    protected void merge(ConstraintVertex other) {
        if (other.isOnConstraint) {
            isOnConstraint = true;
            constraint = other.constraint;
        }
    }
}