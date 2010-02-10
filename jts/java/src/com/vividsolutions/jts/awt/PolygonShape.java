/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jts.awt;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * A Shape which represents a polygon which may contain holes.
 * Provided because the standard AWT Polygon class does not support holes.
 * 
 * @author Martin Davis
 *
 */
public class PolygonShape implements Shape {
    private java.awt.Polygon shell;
    private ArrayList holes = new ArrayList();

    /**
     * Creates a new polygon shape.
     * 
     * @param shellVertices the vertices of the shell 
     * @param holeVerticesCollection a collection of Coordinate[] for each hole
     */
    public PolygonShape(Coordinate[] shellVertices,
        Collection holeVerticesCollection) {
        shell = toPolygon(shellVertices);

        for (Iterator i = holeVerticesCollection.iterator(); i.hasNext();) {
            Coordinate[] holeVertices = (Coordinate[]) i.next();
            holes.add(toPolygon(holeVertices));
        }
    }

    private java.awt.Polygon toPolygon(Coordinate[] coordinates) {
        java.awt.Polygon polygon = new java.awt.Polygon();

        for (int i = 0; i < coordinates.length; i++) {
            polygon.addPoint((int) coordinates[i].x, (int) coordinates[i].y);
        }

        return polygon;
    }

    public Rectangle getBounds() {
        /**@todo Implement this java.awt.Shape method*/
        throw new java.lang.UnsupportedOperationException(
            "Method getBounds() not yet implemented.");
    }

    public Rectangle2D getBounds2D() {
        return shell.getBounds2D();
    }

    public boolean contains(double x, double y) {
        /**@todo Implement this java.awt.Shape method*/
        throw new java.lang.UnsupportedOperationException(
            "Method contains() not yet implemented.");
    }

    public boolean contains(Point2D p) {
        /**@todo Implement this java.awt.Shape method*/
        throw new java.lang.UnsupportedOperationException(
            "Method contains() not yet implemented.");
    }

    public boolean intersects(double x, double y, double w, double h) {
        /**@todo Implement this java.awt.Shape method*/
        throw new java.lang.UnsupportedOperationException(
            "Method intersects() not yet implemented.");
    }

    public boolean intersects(Rectangle2D r) {
        /**@todo Implement this java.awt.Shape method*/
        throw new java.lang.UnsupportedOperationException(
            "Method intersects() not yet implemented.");
    }

    public boolean contains(double x, double y, double w, double h) {
        /**@todo Implement this java.awt.Shape method*/
        throw new java.lang.UnsupportedOperationException(
            "Method contains() not yet implemented.");
    }

    public boolean contains(Rectangle2D r) {
        /**@todo Implement this java.awt.Shape method*/
        throw new java.lang.UnsupportedOperationException(
            "Method contains() not yet implemented.");
    }

    public PathIterator getPathIterator(AffineTransform at) {
        ArrayList rings = new ArrayList();
        rings.add(shell);
        rings.addAll(holes);

        return new ShapeCollectionPathIterator(rings, at);
    }

    public PathIterator getPathIterator(AffineTransform at, double flatness) {
      // since Geomtery are linear, can simply delegate to the simple method
    	return getPathIterator(at);
    }
}
