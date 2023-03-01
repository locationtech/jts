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

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequences;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.twkb.TWKBHeader.GeometryType;

/**
 * Reads a {@link Geometry} encoded into TWKB (Tiny Well-known Binary).
 * <p>
 * The current TWKB specification is
 * <a href='https://github.com/TWKB/Specification/blob/master/twkb.md'>https://github.com/TWKB/Specification/blob/master/twkb.md</a>.
 * <p>
 */
public class TWKBReader {

    private static final GeometryFactory DEFAULT_FACTORY = new GeometryFactory(
        PackedCoordinateSequenceFactory.DOUBLE_FACTORY);

    private GeometryFactory geometryFactory;

    public TWKBReader() {
        this(DEFAULT_FACTORY);
    }

    public TWKBReader(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    public TWKBReader setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
        return this;
    }

    public Geometry read(byte[] bytes) throws ParseException {
        return read(new ByteArrayInputStream(bytes));
    }

    public Geometry read(InputStream in) throws ParseException {
        return read((DataInput) new DataInputStream(in));
    }

    public Geometry read(DataInput in) throws ParseException {
        try {
            return read(geometryFactory, in);
        } catch (IOException ex) {
            throw new ParseException("Unexpected IOException caught: " + ex.getMessage());
        }
    }

    private static Geometry read(GeometryFactory factory, DataInput in) throws IOException {
        Objects.requireNonNull(factory, "GeometryFactory is null");
        Objects.requireNonNull(in, "DataInput is null");

        TWKBHeader header = readHeader(in);
        return readGeometryBody(factory, header, in);
    }

    private static TWKBHeader readHeader(DataInput in) throws IOException {
        Objects.requireNonNull(in);
        final int typeAndPrecisionHeader = in.readByte() & 0xFF;
        final int geometryTypeCode = typeAndPrecisionHeader & 0b00001111;
        final GeometryType geometryType = GeometryType.valueOf(geometryTypeCode);
        final int precision = Varint.zigzagDecode((typeAndPrecisionHeader & 0b11110000) >> 4);
        final int metadata_header = in.readByte() & 0xFF;
        final boolean hasBBOX = (metadata_header & 0b00000001) > 0;
        final boolean hasSize = (metadata_header & 0b00000010) > 0;
        final boolean hasIdList = (metadata_header & 0b00000100) > 0;
        final boolean hasExtendedPrecision = (metadata_header & 0b00001000) > 0;
        final boolean isEmpty = (metadata_header & 0b00010000) > 0;

        boolean hasZ = false;
        boolean hasM = false;
        int zprecision = 0;
        int mprecision = 0;
        if (hasExtendedPrecision) {
            final int extendedDimsHeader = in.readByte() & 0xFF;
            hasZ = (extendedDimsHeader & 0b00000001) > 0;
            hasM = (extendedDimsHeader & 0b00000010) > 0;
            zprecision = (extendedDimsHeader & 0b00011100) >> 2;
            mprecision = (extendedDimsHeader & 0b11100000) >> 5;
        }

        int geometryBodySize = -1;
        if (hasSize) {
            geometryBodySize = Varint.readUnsignedVarInt(in);
        }
        return new TWKBHeader()
            .setGeometryType(geometryType)
            .setXyPrecision(precision)
            .setHasZ(hasZ)
            .setZPrecision(zprecision)
            .setHasM(hasM)
            .setMPrecision(mprecision)
            .setHasIdList(hasIdList)
            .setEmpty(isEmpty)
            .setHasSize(hasSize)
            .setHasBBOX(hasBBOX)
            .setGeometryBodySize(geometryBodySize);
    }

    private static Geometry readGeometryBody(GeometryFactory factory, TWKBHeader header,
        DataInput in) throws IOException {
        final GeometryType geometryType = header.geometryType();
        if (header.isEmpty()) {
            return geometryType.createEmpty(factory);
        }
        if (header.hasBBOX()) {
            skipBbox(header, in);
        }
        switch (geometryType) {
            case POINT:
                return readPoint(factory, in, header);
            case LINESTRING:
                return readLineString(factory, in, header, new long[header.getDimensions()]);
            case POLYGON:
                return readPolygon(factory, in, header, new long[header.getDimensions()]);
            case MULTIPOINT:
                return readMultiPoint(factory, in, header);
            case MULTILINESTRING:
                return readMultiLineString(factory, in, header);
            case MULTIPOLYGON:
                return readMultiPolygon(factory, in, header);
            case GEOMETRYCOLLECTION:
                return readGeometryCollection(factory, in, header);
            default:
                throw new IllegalStateException();
        }
    }

    private static Point readPoint(GeometryFactory factory, DataInput in, TWKBHeader header)
        throws IOException {
        CoordinateSequence seq = createCoordinateSequence(factory, 1, header);
        final int dimensions = header.getDimensions();
        for (int d = 0; d < dimensions; d++) {
            long preciseOrdinate = Varint.readSignedVarLong(in);
            int precision = header.getPrecision(d);
            double ordinate = preciseOrdinate / Math.pow(10, precision);
            seq.setOrdinate(0, d, ordinate);
        }
        return factory.createPoint(seq);
    }

    private static LineString readLineString(GeometryFactory factory, DataInput in,
        TWKBHeader header, long[] prev) throws IOException {
        CoordinateSequence coordinates = readCoordinateSequence(factory, in, header, prev);
        return factory.createLineString(coordinates);
    }

    private static LinearRing readLinearRing(GeometryFactory factory, DataInput in,
        TWKBHeader header, long[] prev) throws IOException {

        CoordinateSequence seq = readCoordinateSequence(factory, in, header, prev);
        if (!CoordinateSequences.isRing(seq)) {
            seq = CoordinateSequences.ensureValidRing(factory.getCoordinateSequenceFactory(), seq);
        }
        return factory.createLinearRing(seq);
    }

    private static Polygon readPolygon(GeometryFactory factory, DataInput in, TWKBHeader header,
        long[] prev) throws IOException {
        final int nrings = Varint.readUnsignedVarInt(in);
        if (nrings == 0) {
            return factory.createPolygon();// unlikely, empty check already performed?
        }
        LinearRing shell = readLinearRing(factory, in, header, prev);
        LinearRing[] holes = new LinearRing[nrings - 1];
        for (int h = 0; h < nrings - 1; h++) {
            holes[h] = readLinearRing(factory, in, header, prev);
        }
        return factory.createPolygon(shell, holes);
    }

    private static MultiPoint readMultiPoint(GeometryFactory factory, DataInput in,
        TWKBHeader header) throws IOException {
        final int nmembers = Varint.readUnsignedVarInt(in);
        if (header.hasIdList()) {
            skipIdList(nmembers, in);
        }
        CoordinateSequence coordinates = readCoordinateSequence(factory, in, nmembers, header,
            new long[header.getDimensions()]);
        return factory.createMultiPoint(coordinates);
    }

    private static MultiLineString readMultiLineString(GeometryFactory factory, DataInput in,
        TWKBHeader header) throws IOException {
        final int nmembers = Varint.readUnsignedVarInt(in);
        if (header.hasIdList()) {
            skipIdList(nmembers, in);
        }
        LineString[] lineStrings = new LineString[nmembers];
        long[] prev = new long[header.getDimensions()];
        for (int mN = 0; mN < nmembers; mN++) {
            lineStrings[mN] = readLineString(factory, in, header, prev);
        }
        return factory.createMultiLineString(lineStrings);
    }

    private static Geometry readMultiPolygon(GeometryFactory factory, DataInput in,
        TWKBHeader header) throws IOException {
        final int nmembers = Varint.readUnsignedVarInt(in);
        if (header.hasIdList()) {
            skipIdList(nmembers, in);
        }
        long[] prev = new long[header.getDimensions()];
        Polygon[] polygons = new Polygon[nmembers];
        for (int mN = 0; mN < nmembers; mN++) {
            polygons[mN] = readPolygon(factory, in, header, prev);
        }
        return factory.createMultiPolygon(polygons);
    }

    private static Geometry readGeometryCollection(GeometryFactory factory, DataInput in,
        TWKBHeader header) throws IOException {

        final int nmembers = Varint.readUnsignedVarInt(in);
        if (header.hasIdList()) {
            skipIdList(nmembers, in);
        }
        Geometry[] geometries = new Geometry[nmembers];
        for (int geomN = 0; geomN < nmembers; geomN++) {
            geometries[geomN] = read(factory, in);
        }
        return factory.createGeometryCollection(geometries);
    }

    private static void skipIdList(int nmembers, DataInput in) throws IOException {
        readIdList(nmembers, null, in);
    }

    private static void readIdList(int nmembers, /* Nullable */long[] target, DataInput in)
        throws IOException {
        for (int i = 0; i < nmembers; i++) {
            long id = Varint.readUnsignedVarLong(in);
            if (target != null) {
                target[i] = id;
            }
        }
    }

    private static void skipBbox(TWKBHeader header, DataInput in) throws IOException {
        final int dimensions = header.getDimensions();
        for (int coord = 0; coord < dimensions; coord++) {
            Varint.readSignedVarLong(in);
            Varint.readSignedVarLong(in);
        }
    }

    private static CoordinateSequence readCoordinateSequence(GeometryFactory factory, DataInput in,
        TWKBHeader header, long[] prev) throws IOException {
        final int size = Varint.readUnsignedVarInt(in);
        return readCoordinateSequence(factory, in, size, header, prev);
    }

    private static CoordinateSequence readCoordinateSequence(GeometryFactory factory, DataInput in,
        int size, TWKBHeader header, long[] prev) throws IOException {

        CoordinateSequence sequence = createCoordinateSequence(factory, size, header);
        final int dimensions = header.getDimensions();
        for (int coordIndex = 0; coordIndex < size; coordIndex++) {
            for (int ordinateIndex = 0; ordinateIndex < dimensions; ordinateIndex++) {
                int precision = header.getPrecision(ordinateIndex);
                long prevValue = prev[ordinateIndex];
                long delta = Varint.readSignedVarLong(in);
                long preciseOrdinate = delta + prevValue;
                double ordinate = preciseOrdinate / Math.pow(10, precision);
                prev[ordinateIndex] = preciseOrdinate;
                sequence.setOrdinate(coordIndex, ordinateIndex, ordinate);
            }
        }
        return sequence;
    }

    private static CoordinateSequence createCoordinateSequence(GeometryFactory factory, int size,
        final TWKBHeader header) {

        final int dim = header.getDimensions();
        final int measures = header.hasM() ? 1 : 0;
        CoordinateSequence sequence = factory.getCoordinateSequenceFactory().create(size, dim,
            measures);
        if (sequence.getDimension() != dim) {
            throw new IllegalStateException(
                "Provided CoordinateSequenceFactory does not support the required dimension. Requested "
                    + header + ", returned " + sequence.getDimension());
        }
        if (measures != sequence.getMeasures()) {
            throw new IllegalStateException("CoordinateSequenceFactory error: requested " + measures
                + " measures, returned " + sequence.getMeasures());
        }
        return sequence;
    }
}
