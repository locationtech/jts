/*
 * Copyright (c) 2019 Gabriel Roldan, 2022 Aurélien Mino
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.io.twkb;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;

class BoundsExtractor implements CoordinateSequenceFilter {

    private final int dimensions;

    double[] ordinates = new double[]{ //
        Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, // note, Double.MIN_VALUE is positive
        Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, //
        Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, //
        Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY//
    };

    BoundsExtractor(int dimensions) {
        this.dimensions = dimensions;
    }

    public @Override
    void filter(final CoordinateSequence seq, final int coordIndex) {
        for (int ordinateIndex = 0; ordinateIndex < dimensions; ordinateIndex++) {
            final double ordinate = seq.getOrdinate(coordIndex, ordinateIndex);
            final int minIndex = 2 * ordinateIndex;
            final int maxIndex = minIndex + 1;
            ordinates[minIndex] = Math.min(ordinates[minIndex], ordinate);
            ordinates[maxIndex] = Math.max(ordinates[maxIndex], ordinate);
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean isGeometryChanged() {
        return false;
    }
}
