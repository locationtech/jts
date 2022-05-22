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

import java.util.Objects;
import java.util.function.Function;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * Represents the metadata header information of a geometry encoded with TWKB
 * @see <a href="https://github.com/TWKB/Specification/blob/master/twkb.md">TWKB specification</a>
 */
class TWKBHeader {

    public TWKBHeader() {

    }

    public TWKBHeader(TWKBHeader other) {
        this.geometryType = other.geometryType;
        this.xyPrecision = other.xyPrecision;
        this.hasBBOX = other.hasBBOX;
        this.hasSize = other.hasSize;
        this.hasIdList = other.hasIdList;
        this.isEmpty = other.isEmpty;
        this.hasZ = other.hasZ;
        this.hasM = other.hasM;
        this.zPrecision = other.zPrecision;
        this.mPrecision = other.mPrecision;
        this.geometryBodySize = other.geometryBodySize;
    }

    public GeometryType geometryType() {
        return this.geometryType;
    }

    public int xyPrecision() {
        return this.xyPrecision;
    }

    public boolean hasBBOX() {
        return this.hasBBOX;
    }

    public boolean hasSize() {
        return this.hasSize;
    }

    public boolean hasIdList() {
        return this.hasIdList;
    }

    public boolean isEmpty() {
        return this.isEmpty;
    }

    public boolean hasZ() {
        return this.hasZ;
    }

    public boolean hasM() {
        return this.hasM;
    }

    public int zPrecision() {
        return this.zPrecision;
    }

    public int mPrecision() {
        return this.mPrecision;
    }

    public TWKBHeader setGeometryType(GeometryType geometryType) {
        this.geometryType = geometryType;
        return this;
    }

    public TWKBHeader setXyPrecision(int xyPrecision) {
        this.xyPrecision = xyPrecision;
        return this;
    }

    public TWKBHeader setHasBBOX(boolean hasBBOX) {
        this.hasBBOX = hasBBOX;
        return this;
    }

    public TWKBHeader setHasSize(boolean hasSize) {
        this.hasSize = hasSize;
        return this;
    }

    public TWKBHeader setHasIdList(boolean hasIdList) {
        this.hasIdList = hasIdList;
        return this;
    }

    public TWKBHeader setEmpty(boolean empty) {
        isEmpty = empty;
        return this;
    }

    public TWKBHeader setHasZ(boolean hasZ) {
        this.hasZ = hasZ;
        return this;
    }

    public TWKBHeader setHasM(boolean hasM) {
        this.hasM = hasM;
        return this;
    }

    public TWKBHeader setZPrecision(int zPrecision) {
        this.zPrecision = zPrecision;
        return this;
    }

    public TWKBHeader setMPrecision(int mPrecision) {
        this.mPrecision = mPrecision;
        return this;
    }

    public TWKBHeader setGeometryBodySize(int geometryBodySize) {
        this.geometryBodySize = geometryBodySize;
        return this;
    }

    @Override
    public String toString() {
        return "TWKBHeader{" +
            "geometryType=" + geometryType +
            ", xyPrecision=" + xyPrecision +
            ", hasBBOX=" + hasBBOX +
            ", hasSize=" + hasSize +
            ", hasIdList=" + hasIdList +
            ", isEmpty=" + isEmpty +
            ", hasZ=" + hasZ +
            ", hasM=" + hasM +
            ", zPrecision=" + zPrecision +
            ", mPrecision=" + mPrecision +
            ", geometryBodySize=" + geometryBodySize +
            '}';
    }

    public int geometryBodySize() {
        return this.geometryBodySize;
    }

    enum GeometryType {
        POINT(1, GeometryFactory::createPoint), //
        LINESTRING(2, GeometryFactory::createLineString), //
        POLYGON(3, GeometryFactory::createPolygon), //
        MULTIPOINT(4, GeometryFactory::createMultiPoint), //
        MULTILINESTRING(5, GeometryFactory::createMultiLineString), //
        MULTIPOLYGON(6, GeometryFactory::createMultiPolygon), //
        GEOMETRYCOLLECTION(7, GeometryFactory::createGeometryCollection);

        private final int value;

        private final Function<GeometryFactory, Geometry> emptyBuilder;

        GeometryType(int value, Function<GeometryFactory, Geometry> emptyBuilder) {
            this.value = value;
            this.emptyBuilder = emptyBuilder;
        }

        public int getValue() {
            return value;
        }

        // held as a class variable cause calling values() on a tight loop has a non-depreciable
        // performance impact
        private static final GeometryType[] VALUES = GeometryType.values();

        public static GeometryType valueOf(int value) {
            return VALUES[value - 1];
        }

        public static GeometryType valueOf(Class<? extends Geometry> gclass) {
            Objects.requireNonNull(gclass);
            if (Point.class.isAssignableFrom(gclass))
                return POINT;
            if (LineString.class.isAssignableFrom(gclass))
                return LINESTRING;
            if (Polygon.class.isAssignableFrom(gclass))
                return POLYGON;
            if (MultiPoint.class.isAssignableFrom(gclass))
                return MULTIPOINT;
            if (MultiLineString.class.isAssignableFrom(gclass))
                return MULTILINESTRING;
            if (MultiPolygon.class.isAssignableFrom(gclass))
                return MULTIPOLYGON;
            if (GeometryCollection.class.isAssignableFrom(gclass))
                return GEOMETRYCOLLECTION;

            throw new IllegalArgumentException("Unrecognized geometry tpye: " + gclass);
        }

        public Geometry createEmpty(GeometryFactory factory) {
            return this.emptyBuilder.apply(factory);
        }
    }

    private GeometryType geometryType;

    private int xyPrecision = 0;

    private boolean hasBBOX = false;

    private boolean hasSize = false;

    private boolean hasIdList = false;

    public boolean hasExtendedPrecision() {
        return hasZ() || hasM();
    }

    private boolean isEmpty = false;

    private boolean hasZ = false;

    private boolean hasM = false;

    private int zPrecision = 0;

    private int mPrecision = 0;

    /**
     * Size of encoded geometry body, iif size_flag == 1, defaults to {@code -1} if {@link #hasSize}
     * ({@code == false}
     * <p>
     * {@code geometry_body_size := uint32 # size in bytes of <geometry_body>}
     */
    private int geometryBodySize;

    public int getDimensions() {
        return 2 + (hasZ ? 1 : 0) + (hasM ? 1 : 0);
    }

    public int getPrecision(final int dimensionIndex) {
        switch (dimensionIndex) {
        case 0:
        case 1:
            return xyPrecision;
        case 2:
            if (!(hasZ || hasM)) {
                throw new IllegalArgumentException("Geometry only has XY dimensions.");
            }
            return hasZ ? zPrecision : mPrecision;
        case 3:
            if (!(hasZ && hasM)) {
                throw new IllegalArgumentException("Geometry has no M dimension.");
            }
            return mPrecision;
        default:
            throw new IllegalArgumentException(
                    "Dimension index shall be between 0 and 3: " + dimensionIndex);
        }
    }

}