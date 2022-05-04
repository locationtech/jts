/*
 * Copyright (c) 2018 James Hughes, 2019 Gabriel Roldan, 2022 Aurélien Mino
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.twkb.TWKBHeader.GeometryType;

/**
 * Writes {@link Geometry}s in TWKB (Tiny Well-known Binary) format.
 * <p>
 * The current TWKB specification is
 * <a href='https://github.com/TWKB/Specification/blob/master/twkb.md'>https://github.com/TWKB/Specification/blob/master/twkb.md</a>.
 * <p>
 */
public class TWKBWriter {

    private TWKBHeader paramsHeader = new TWKBHeader()
        .setXyPrecision(7)
        .setZPrecision(0)
        .setMPrecision(0)
        .setHasBBOX(false)
        .setHasSize(false);

    /**
     * Number of base-10 decimal places stored for X and Y dimensions.
     * <p>
     * A positive retaining information to the right of the decimal place, negative rounding up to
     * the left of the decimal place).
     * <p>
     * Defaults to {@code 7}
     */
    public TWKBWriter setXYPrecision(int xyprecision) {
        if (xyprecision < -7 || xyprecision > 7) {
            throw new IllegalArgumentException(
                    "X/Z precision cannot be greater than 7 or less than -7");
        }
        paramsHeader = paramsHeader.setXyPrecision(xyprecision);
        return this;
    }

    public TWKBWriter setEncodeZ(boolean includeZDimension) {
        paramsHeader = paramsHeader.setHasZ(includeZDimension);
        return this;
    }

    public TWKBWriter setEncodeM(boolean includeMDimension) {
        paramsHeader = paramsHeader.setHasM(includeMDimension);
        return this;
    }

    /**
     * Number of base-10 decimal places stored for Z dimension.
     * <p>
     * A positive retaining information to the right of the decimal place, negative rounding up to
     * the left of the decimal place).
     * <p>
     * Defaults to {@code 0}
     */
    public TWKBWriter setZPrecision(int zprecision) {
        if (zprecision < 0 || zprecision > 7) {
            throw new IllegalArgumentException("Z precision cannot be negative or greater than 7");
        }
        paramsHeader = paramsHeader.setZPrecision(zprecision);
        return this;
    }

    /**
     * Number of base-10 decimal places stored for M dimension.
     * <p>
     * A positive retaining information to the right of the decimal place, negative rounding up to
     * the left of the decimal place).
     * <p>
     * Defaults to {@code 0}
     */
    public TWKBWriter setMPrecision(int mprecision) {
        if (mprecision < 0 || mprecision > 7) {
            throw new IllegalArgumentException("M precision cannot be negative or greater than 7");
        }
        paramsHeader = paramsHeader.setMPrecision(mprecision);
        return this;
    }

    /**
     * Whether the generated TWKB should include the size in bytes of the geometry.
     */
    public TWKBWriter setIncludeSize(boolean includeSize) {
        paramsHeader = paramsHeader.setHasSize(includeSize);
        return this;
    }

    /**
     * Whether the generated TWKB should include a Bounding Box for the geometry.
     */
    public TWKBWriter setIncludeBbox(boolean includeBbox) {
        paramsHeader = paramsHeader.setHasBBOX(includeBbox);
        return this;
    }

    public byte[] write(Geometry geom) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            write(geom, out);
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected IOException caught: " + ex.getMessage(), ex);
        }
        return out.toByteArray();
    }

    public void write(Geometry geom, OutputStream out) throws IOException {
        write(geom, (DataOutput) new DataOutputStream(out));
    }

    public void write(Geometry geom, DataOutput out) throws IOException {
        Objects.requireNonNull(geom, "geometry is null");
        write(geom, TWKBOutputStream.of(out), paramsHeader, false);
    }

    private TWKBHeader write(Geometry geometry, TWKBOutputStream out, TWKBHeader params,
        boolean forcePreserveHeaderDimensions) throws IOException {
        Objects.requireNonNull(geometry, "Geometry is null");
        Objects.requireNonNull(out, "DataOutput is null");
        Objects.requireNonNull(params, "TWKBHeader is null");

        TWKBHeader header = prepareHeader(geometry, new TWKBHeader(params), forcePreserveHeaderDimensions);

        if (header.hasSize()) {
            BufferedTKWBOutputStream bufferedBody = BufferedTKWBOutputStream.create();
            writeGeometryBody(geometry, bufferedBody, header);
            int bodySize = bufferedBody.size();
            header = header.setGeometryBodySize(bodySize);
            writeHeaderTo(header, out);
            bufferedBody.writeTo(out);
        } else {
            writeHeaderTo(header, out);
            writeGeometryBody(geometry, out, header);
        }
        return header;
    }

    private static void writeHeaderTo(TWKBHeader header, TWKBOutputStream out) throws IOException {
        Objects.requireNonNull(out);
        final int typeAndPrecisionHeader;
        final int metadataHeader;
        {
            final int geometryType = header.geometryType().getValue();
            final int precisionHeader = Varint.zigZagEncode(header.xyPrecision()) << 4;
            typeAndPrecisionHeader = precisionHeader | geometryType;

            metadataHeader = (header.hasBBOX() ? 0b00000001 : 0) //
                | (header.hasSize() ? 0b00000010 : 0)//
                | (header.hasIdList() ? 0b00000100 : 0)//
                | (header.hasExtendedPrecision() ? 0b00001000 : 0)//
                | (header.isEmpty() ? 0b00010000 : 0);
        }
        out.writeByte(typeAndPrecisionHeader);
        out.writeByte(metadataHeader);
        if (header.hasExtendedPrecision()) {
            int extendedDimsHeader = (header.hasZ() ? 0b00000001 : 0) | (header.hasM() ? 0b00000010 : 0);
            extendedDimsHeader |= header.zPrecision() << 2;
            extendedDimsHeader |= header.mPrecision() << 5;

            out.writeByte(extendedDimsHeader);
        }
        if (header.hasSize()) {
            out.writeUnsignedVarInt(header.geometryBodySize());
        }
    }

    private TWKBHeader prepareHeader(Geometry geometry, TWKBHeader params,
        boolean forcePreserveHeaderDimensions) {

        final boolean isEmpty = geometry.isEmpty();
        final GeometryType geometryType = GeometryType.valueOf(geometry.getClass());
        TWKBHeader header = forcePreserveHeaderDimensions ? params : setDimensions(geometry, params);
        header.setEmpty(isEmpty);
        header.setGeometryType(geometryType);

        if (isEmpty && header.hasBBOX()) {
            header = header.setHasBBOX(false);
        }
        return header;
    }

    private void writeGeometryBody(Geometry geom, TWKBOutputStream out, TWKBHeader header)
        throws IOException {
        if (header.isEmpty()) {
            return;
        }
        if (header.hasBBOX()) {
            writeBbox(geom, out, header);
        }
        final GeometryType geometryType = GeometryType.valueOf(geom.getClass());
        switch (geometryType) {
            case POINT:
                writePoint((Point) geom, out, header);
                return;
            case LINESTRING:
                writeLineString((LineString) geom, out, header, new long[header.getDimensions()]);
                return;
            case POLYGON:
                writePolygon((Polygon) geom, out, header, new long[header.getDimensions()]);
                return;
            case MULTIPOINT:
                writeMultiPoint((MultiPoint) geom, out, header);
                return;
            case MULTILINESTRING:
                writeMultiLineString((MultiLineString) geom, out, header);
                return;
            case MULTIPOLYGON:
                writeMultiPolygon((MultiPolygon) geom, out, header);
                return;
            case GEOMETRYCOLLECTION:
                writeGeometryCollection((GeometryCollection) geom, out, header);
                return;
            default:
                break;
        }
    }

    private void writePoint(Point geom, TWKBOutputStream out, TWKBHeader header)
        throws IOException {
        assert !geom.isEmpty();
        CoordinateSequence seq = geom.getCoordinateSequence();
        int dimensions = header.getDimensions();
        for (int d = 0; d < dimensions; d++) {
            writeOrdinate(seq.getOrdinate(0, d), 0L, header.getPrecision(d), out);
        }
    }

    private void writeCoordinateSequence(CoordinateSequence coordinateSequence,
        TWKBOutputStream out, TWKBHeader header, long[] prev, int minNPoints) throws IOException {

        final int dimensions = header.getDimensions();
        long[] delta = new long[dimensions];
        int nPoints = 0;
        int nPointsRemaining = coordinateSequence.size();
        // Real number of points can't be determined beforehand, since duplicated points may be removed, so buffering is required
        BufferedTKWBOutputStream bufferedOut = BufferedTKWBOutputStream.create();

        for (int coordIndex = 0; coordIndex < coordinateSequence.size(); coordIndex++) {
            long diff = 0;
            nPointsRemaining--;
            for (int ordinateIndex = 0; ordinateIndex < dimensions; ordinateIndex++) {
                int precision = header.getPrecision(ordinateIndex);
                double ordinate = coordinateSequence.getOrdinate(coordIndex, ordinateIndex);
                long preciseOrdinate = makePrecise(ordinate, precision);
                delta[ordinateIndex] = preciseOrdinate - prev[ordinateIndex];
                prev[ordinateIndex] = preciseOrdinate;
                diff += Math.abs(delta[ordinateIndex]);
            }
            if (coordIndex != 0 && diff == 0 && (nPoints + nPointsRemaining) > minNPoints) {
                // Skip this point
                continue;
            }

            for (int ordinateIndex = 0; ordinateIndex < header.getDimensions(); ordinateIndex++) {
                bufferedOut.writeSignedVarLong(delta[ordinateIndex]);
            }
            nPoints++;
        }

        out.writeUnsignedVarInt(nPoints);
        bufferedOut.writeTo(out);
    }

    private long writeOrdinate(double ordinate, long previousOrdinateValue, int precision,
        TWKBOutputStream out) throws IOException {
        long preciseOrdinate = makePrecise(ordinate, precision);
        long delta = preciseOrdinate - previousOrdinateValue;
        out.writeSignedVarLong(delta);
        return preciseOrdinate;
    }

    private long makePrecise(double value, int precision) {
        return Math.round(value * Math.pow(10, precision));
    }

    private void writeLineString(LineString geom, TWKBOutputStream out, TWKBHeader header,
        long[] prev) throws IOException {
        writeCoordinateSequence(geom.getCoordinateSequence(), out, header, prev, 3);
    }

    private void writePolygon(Polygon geom, TWKBOutputStream out, TWKBHeader header,
        long[] prev) throws IOException {
        if (geom.isEmpty()) {
            out.writeUnsignedVarInt(0);
            return;
        }
        final int numInteriorRing = geom.getNumInteriorRing();
        final int nrings = 1 + numInteriorRing;
        out.writeUnsignedVarInt(nrings);
        writeLinearRing(geom.getExteriorRing(), out, header, prev);
        for (int r = 0; r < numInteriorRing; r++) {
            writeLinearRing(geom.getInteriorRingN(r), out, header, prev);
        }
    }

    private void writeLinearRing(LinearRing geom, TWKBOutputStream out, TWKBHeader header,
        long[] prev) throws IOException {
        if (geom.isEmpty()) {
            out.writeUnsignedVarInt(0);
            return;
        }
        writeCoordinateSequence(geom.getCoordinateSequence(), out, header, prev, 3);
    }

    private void writeMultiPoint(MultiPoint geom, TWKBOutputStream out, TWKBHeader header)
        throws IOException {
        assert !geom.isEmpty();

        CoordinateSequence seq = geom.getFactory().getCoordinateSequenceFactory()
            .create(geom.getCoordinates());
        writeCoordinateSequence(seq, out, header, new long[header.getDimensions()], 2);
    }

    private void writeMultiLineString(MultiLineString geom, TWKBOutputStream out,
        TWKBHeader header) throws IOException {
        final int size = writeNumGeometries(geom, out);
        long[] prev = new long[header.getDimensions()];
        for (int i = 0; i < size; i++) {
            writeLineString((LineString) geom.getGeometryN(i), out, header, prev);
        }
    }

    private void writeMultiPolygon(MultiPolygon geom, TWKBOutputStream out,
        TWKBHeader header) throws IOException {
        final int size = writeNumGeometries(geom, out);
        long[] prev = new long[header.getDimensions()];
        for (int i = 0; i < size; i++) {
            writePolygon((Polygon) geom.getGeometryN(i), out, header, prev);
        }
    }

    private void writeGeometryCollection(GeometryCollection geom, TWKBOutputStream out,
        TWKBHeader header) throws IOException {
        final int size = writeNumGeometries(geom, out);
        for (int i = 0; i < size; i++) {
            Geometry geometryN = geom.getGeometryN(i);
            boolean forcePreserveDimensions = geometryN.isEmpty();
            write(geometryN, out, header, forcePreserveDimensions);
        }
    }

    private int writeNumGeometries(GeometryCollection geom, TWKBOutputStream out)
        throws IOException {
        int size = geom.getNumGeometries();
        out.writeUnsignedVarInt(size);
        return size;
    }

    private void writeBbox(Geometry geom, TWKBOutputStream out, TWKBHeader header)
        throws IOException {
        final int dimensions = header.getDimensions();
        final double[] boundsCoordinates = computeEnvelope(geom, dimensions);

        for (int d = 0; d < dimensions; d++) {
            final int precision = header.getPrecision(d);
            double min = boundsCoordinates[2 * d];
            double max = boundsCoordinates[2 * d + 1];
            long preciseMin = writeOrdinate(min, 0, precision, out);
            writeOrdinate(max, preciseMin, precision, out);
        }
    }

    private static double[] computeEnvelope(Geometry geom, int dimensions) {
        BoundsExtractor extractor = new BoundsExtractor(dimensions);
        geom.apply(extractor);
        return extractor.ordinates;
    }

    private static TWKBHeader setDimensions(Geometry g, TWKBHeader header) {
        if (g.isEmpty()) {
            return header.setHasZ(false).setHasM(false);
        }
        if (g instanceof Point) {
            return setDimensions(((Point) g).getCoordinateSequence(), header);
        }
        if (g instanceof LineString) {
            return setDimensions(((LineString) g).getCoordinateSequence(), header);
        }
        if (g instanceof Polygon) {
            return setDimensions(((Polygon) g).getExteriorRing().getCoordinateSequence(), header);
        }
        return setDimensions(g.getGeometryN(0), header);
    }

    private static TWKBHeader setDimensions(CoordinateSequence seq, TWKBHeader header) {
        boolean hasZ = seq.hasZ();
        boolean hasM = seq.hasM();
        return header.setHasZ(hasZ).setHasM(hasM);
    }

}