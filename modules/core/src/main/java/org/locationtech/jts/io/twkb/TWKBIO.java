/*
 * Copyright (c) 2019 Gabriel Roldan
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.io.twkb;

import static org.locationtech.jts.io.twkb.TWKBHeader.GeometryType.POINT;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.CoordinateSequences;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.io.twkb.TWKBHeader.GeometryType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * <pre>
 * {@code
 * 
 * twkb                    := <header> <geometry_body>
 * header                  := <type_and_precision> <metadata_header> [extended_dimensions_header] [geometry_body_size]
 * type_and_precision      := byte := <type_mask OR precision>)
 * type_mask               := <ubyte> (0b0000XXXX -> 1=point, 2=linestring, 3=polygon, 4=multipoint, 
 *                                     5=multilinestring, 6=multipolygon, 7=geometry collection)
 * precision               := <signed byte> (zig-zag encoded 4-bit signed integer, 0bXXXX0000. Number of base-10 decimal places 
 *                                           stored. A positive retaining information to the right of the decimal place, negative 
 *                                           rounding up to the left of the decimal place)  
 * metadata_header := byte := <bbox_flag OR  size_flag OR idlist_flag OR extended_precision_flag OR empty_geometry_flag>
 * bbox_flag               := 0b00000001
 * size_flag               := 0b00000010
 * idlist_flag             := 0b00000100
 * extended_precision_flag := 0b00001000
 * empty_geometry_flag     := 0b00010000
 * 
 * # extended_dimensions_header present iif extended_precision_flag == 1
 * extended_dimensions_header  := byte := <Z_dimension_presence_flag OR M_dimension_presence_flag OR Z_precision OR M_precision>
 * Z_dimension_presence_flag   := 0b00000001 
 * M_dimension_presence_flag   := 0b00000010
 * Z_precision                 := 0b000XXX00 3-bit unsigned integer using bits 3-5 
 * M_precision                 := 0bXXX00000 3-bit unsigned integer using bits 6-8
 * 
 * # geometry_body_size present iif size_flag == 1 
 * geometry_body_size := uint32 # size in bytes of <geometry_body>
 * 
 * # geometry_body present iif empty_geometry_flag == 0
 * geometry_body := [bounds] [idlist] <geometry>
 * # bounds present iff bbox_flag == 1 
 * # 2 signed varints per dimension. i.e.:
 * # [xmin, deltax, ymin, deltay]                              iif Z_dimension_presence_flag == 0 AND M_dimension_presence_flag == 0
 * # [xmin, deltax, ymin, deltay, zmin, deltaz]                iif Z_dimension_presence_flag == 1 AND M_dimension_presence_flag == 0
 * # [xmin, deltax, ymin, deltay, zmin, deltaz, mmin, deltam]  iif Z_dimension_presence_flag == 1 AND M_dimension_presence_flag == 1
 * # [xmin, deltax, ymin, deltay, mmin, deltam]                iif Z_dimension_presence_flag == 0 AND M_dimension_presence_flag == 1
 * bounds          := sint32[4] | sint32[6] | sint32[8] 
 * geometry        := point | linestring | polygon | multipoint | multilinestring | multipolygon | geomcollection
 * point           := sint32[dimension]
 * linestring      := <npoints:uint32> [point[npoints]]
 * polygon         := <nrings:uint32> [linestring]
 * multipoint      := <nmembers:uint32> [idlist:<sint32[nmembers]>] [point[nmembers]]
 * multilinestring := <nmembers:uint32> [idlist:<sint32[nmembers]>] [linestring[nmembers]]
 * multipolygon    := <nmembers:uint32> [idlist:<sint32[nmembers]>] [polygon[nmembers]]
 * geomcollection  := <nmembers:uint32> [idlist:<sint32[nmembers]>] [twkb[nmembers]]
 * 
 * uint32 := <Unsigned variable-length encoded integer>
 * sint32 := <Signed variable-length, zig-zag encoded integer>
 * byte := <Single octect>
 * 
 * }
 * </pre>
 */
class TWKBIO {

    private static GeometryFactory DEFAULT_FACTORY = new GeometryFactory(
            PackedCoordinateSequenceFactory.DOUBLE_FACTORY);

    public static Geometry read(InputStream in) throws IOException {
        return read((DataInput) new DataInputStream(in));
    }

    public static Geometry read(DataInput in) throws IOException {
        return read(DEFAULT_FACTORY, in);
    }

    public static Geometry read(GeometryFactory factory, DataInput in) throws IOException {
        Objects.requireNonNull(factory, "GeometryFactory is null");
        Objects.requireNonNull(in, "DataInput is null");

        TWKBHeader header = TWKBHeader.read(in);
        return readGeometryBody(factory, header, in);
    }

    public static void write(Geometry geom, TWKBOutputStream out) throws IOException {
        write(geom, out, TWKBHeader.builder().build());
    }

    public static TWKBHeader write(Geometry geometry, TWKBOutputStream out, TWKBHeader params)
            throws IOException {
        return write(geometry, out, params, false);
    }

    private static TWKBHeader write(Geometry geometry, TWKBOutputStream out, TWKBHeader params,
            boolean forcePreserveHeaderDimensions) throws IOException {
        Objects.requireNonNull(geometry, "Geometry is null");
        Objects.requireNonNull(out, "DataOutput is null");
        Objects.requireNonNull(params, "TWKBHeader is null");

        TWKBHeader header = prepareHeader(geometry, params, forcePreserveHeaderDimensions);

        if (header.hasSize()) {
            BufferedTKWBOutputStream bufferedBody = BufferedTKWBOutputStream.create();
            writeGeometryBody(geometry, bufferedBody, header);
            int bodySize = bufferedBody.size();
            header = header.withGeometryBodySize(bodySize);
            header.writeTo(out);
            bufferedBody.writeTo(out);
        } else {
            header.writeTo(out);
            writeGeometryBody(geometry, out, header);
        }
        return header;
    }

    private static TWKBHeader prepareHeader(Geometry geometry, TWKBHeader params,
            boolean forcePreserveHeaderDimensions) {

        final boolean isEmpty = geometry.isEmpty();
        final GeometryType geometryType = GeometryType.valueOf(geometry.getClass());
        TWKBHeader header = forcePreserveHeaderDimensions ? params
                : setDimensions(geometry, params);
        header = header.withEmpty(isEmpty).withGeometryType(geometryType);

        if (params.optimizedEncoding()) {
            if (isEmpty && header.hasExtendedPrecision()) {
                header = header.withHasZ(false).withHasM(false);
            }
            if ((isEmpty || geometryType == POINT) && header.hasBBOX()) {
                header = header.withHasBBOX(false);
            }
        } else {
            if (isEmpty && header.hasBBOX()) {
                header = header.withHasBBOX(false);
            }
        }
        return header;
    }

    static void writeGeometryBody(Geometry geom, DataOutput out, TWKBHeader header)
            throws IOException {
        writeGeometryBody(geom, TWKBOutputStream.of(out), header);
    }

    static void writeGeometryBody(Geometry geom, TWKBOutputStream out, TWKBHeader header)
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

    private static void writePoint(Point geom, TWKBOutputStream out, TWKBHeader header)
            throws IOException {
        assert !geom.isEmpty();
        CoordinateSequence seq = geom.getCoordinateSequence();
        int dimensions = header.getDimensions();
        for (int d = 0; d < dimensions; d++) {
            writeOrdinate(seq.getOrdinate(0, d), 0L, header.getPrecision(d), out);
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

    private static void writeCoordinateSequence(CoordinateSequence coordinateSequence,
            TWKBOutputStream out, TWKBHeader header, long[] prev) throws IOException {
        int size = coordinateSequence.size();
        out.writeUnsignedVarInt(size);
        writeCoordinateSequence(coordinateSequence, size, out, header, prev);
    }

    private static CoordinateSequence readCoordinateSequence(GeometryFactory factory, DataInput in,
            TWKBHeader header, long[] prev) throws IOException {
        final int size = Varint.readUnsignedVarInt(in);
        return readCoordinateSequence(factory, in, size, header, prev);
    }

    private static void writeCoordinateSequence(CoordinateSequence coordinateSequence, int size,
            TWKBOutputStream out, TWKBHeader header, long[] prev) throws IOException {

        final int dimensions = header.getDimensions();
        for (int coordIndex = 0; coordIndex < size; coordIndex++) {
            for (int ordinateIndex = 0; ordinateIndex < dimensions; ordinateIndex++) {
                long previousValue = prev[ordinateIndex];
                int precision = header.getPrecision(ordinateIndex);
                double ordinate = coordinateSequence.getOrdinate(coordIndex, ordinateIndex);
                long preciseOrdinate = writeOrdinate(ordinate, previousValue, precision, out);
                prev[ordinateIndex] = preciseOrdinate;
            }
        }
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

    private static long writeOrdinate(double ordinate, long previousOrdinateValue, int precision,
            TWKBOutputStream out) throws IOException {
        long preciseOrdinate = makePrecise(ordinate, precision);
        long delta = preciseOrdinate - previousOrdinateValue;
        out.writeSignedVarLong(delta);
        return preciseOrdinate;
    }

    private static long makePrecise(double value, int precision) {
        return Math.round(value * Math.pow(10, precision));
    }

    static void writeLineString(LineString geom, TWKBOutputStream out, TWKBHeader header,
            long[] prev) throws IOException {
        writeCoordinateSequence(geom.getCoordinateSequence(), out, header, prev);
    }

    private static void writePolygon(Polygon geom, TWKBOutputStream out, TWKBHeader header,
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

    private static void writeLinearRing(LinearRing geom, TWKBOutputStream out, TWKBHeader header,
            long[] prev) throws IOException {
        if (geom.isEmpty()) {
            out.writeUnsignedVarInt(0);
            return;
        }
        CoordinateSequence seq = geom.getCoordinateSequence();
        int size = seq.size();
        if (header.optimizedEncoding() && seq.size() > 2) {
            // With linear rings we can save one coordinate, they're automatically closed at parsing
            // time. But we can only do that if due to precision lost the two endpoints won't be
            // equal, otherwise the parser won't know it has to close the linear ring
            double x1 = seq.getOrdinate(0, 0);
            double y1 = seq.getOrdinate(0, 1);
            double x2 = seq.getOrdinate(size - 2, 0);
            double y2 = seq.getOrdinate(size - 2, 1);
            int precision = header.getPrecision(0);
            if (makePrecise(x1, precision) != makePrecise(x2, precision)
                    || makePrecise(y1, precision) != makePrecise(y2, precision)) {
                --size;
            }
        }
        out.writeUnsignedVarInt(size);
        writeCoordinateSequence(seq, size, out, header, prev);
    }

    private static void writeMultiPoint(MultiPoint geom, TWKBOutputStream out, TWKBHeader header)
            throws IOException {
        assert !geom.isEmpty();

        CoordinateSequence seq = geom.getFactory().getCoordinateSequenceFactory()
                .create(geom.getCoordinates());
        writeCoordinateSequence(seq, out, header, new long[header.getDimensions()]);
    }

    private static void writeMultiLineString(MultiLineString geom, TWKBOutputStream out,
            TWKBHeader header) throws IOException {
        final int size = writeNumGeometries(geom, out);
        long[] prev = new long[header.getDimensions()];
        for (int i = 0; i < size; i++) {
            writeLineString((LineString) geom.getGeometryN(i), out, header, prev);
        }
    }

    private static void writeMultiPolygon(MultiPolygon geom, TWKBOutputStream out,
            TWKBHeader header) throws IOException {
        final int size = writeNumGeometries(geom, out);
        long[] prev = new long[header.getDimensions()];
        for (int i = 0; i < size; i++) {
            writePolygon((Polygon) geom.getGeometryN(i), out, header, prev);
        }
    }

    private static void writeGeometryCollection(GeometryCollection geom, TWKBOutputStream out,
            TWKBHeader header) throws IOException {
        final int size = writeNumGeometries(geom, out);
        for (int i = 0; i < size; i++) {
            Geometry geometryN = geom.getGeometryN(i);
            boolean forcePreserveDimensions = geometryN.isEmpty();
            write(geometryN, out, header, forcePreserveDimensions);
        }
    }

    private static int writeNumGeometries(GeometryCollection geom, TWKBOutputStream out)
            throws IOException {
        int size = geom.getNumGeometries();
        out.writeUnsignedVarInt(size);
        return size;
    }

    private static void writeBbox(Geometry geom, TWKBOutputStream out, TWKBHeader header)
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

    private static void skipBbox(TWKBHeader header, DataInput in) throws IOException {
        final int dimensions = header.getDimensions();
        for (int coord = 0; coord < dimensions; coord++) {
            Varint.readSignedVarLong(in);
            Varint.readSignedVarLong(in);
        }
    }

    private static double[] computeEnvelope(Geometry geom, int dimensions) {
        BoundsExtractor extractor = new BoundsExtractor(dimensions);
        geom.apply(extractor);
        return extractor.ordinates;
    }

    private static TWKBHeader setDimensions(Geometry g, TWKBHeader header) {
        if (g.isEmpty()) {
            return header.withHasZ(false).withHasM(false);
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
        return header.withHasZ(hasZ).withHasM(hasM);
    }

    private static class BoundsExtractor implements CoordinateSequenceFilter {

        private final @Getter boolean done = false;

        private final @Getter boolean geometryChanged = false;

        private final int dimensions;

        double[] ordinates = new double[] { //
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, // note, Double.MIN_VALUE is
                                                                    // positive
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, //
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, //
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY//
        };

        BoundsExtractor(int dimensions) {
            this.dimensions = dimensions;
        }

        public @Override void filter(final CoordinateSequence seq, final int coordIndex) {
            for (int ordinateIndex = 0; ordinateIndex < dimensions; ordinateIndex++) {
                final double ordinate = seq.getOrdinate(coordIndex, ordinateIndex);
                final int minIndex = 2 * ordinateIndex;
                final int maxIndex = minIndex + 1;
                double minValue = ordinates[minIndex];
                double maxValue = ordinates[maxIndex];
                minValue = Math.min(minValue, ordinate);
                maxValue = ordinate > maxValue ? ordinate : maxValue;// Math.max(maxValue,
                                                                     // ordinate);
                ordinates[minIndex] = minValue;
                ordinates[maxIndex] = maxValue;
            }
        }
    }

    public static @RequiredArgsConstructor class TWKBOutputStream {

        private final DataOutput out;

        public static TWKBOutputStream of(DataOutput out) {
            return new TWKBOutputStream(out);
        }

        public void writeByte(int ubyte) throws IOException {
            out.writeByte(ubyte);
        }

        public void write(byte[] buff, int offset, int length) throws IOException {
            out.write(buff, offset, length);
        }

        public void writeUnsignedVarInt(int value) throws IOException {
            Varint.writeUnsignedVarInt(value, out);
        }

        public void writeSignedVarLong(long value) throws IOException {
            Varint.writeSignedVarLong(value, out);
        }
    }

    private static class BufferedTKWBOutputStream extends TWKBOutputStream {

        static BufferedTKWBOutputStream create() {
            BufferedDataOutput buff = BufferedDataOutput.create();
            return new BufferedTKWBOutputStream(buff);
        }

        public int size() {
            return ((BufferedDataOutput) super.out).writtenSize();
        }

        BufferedTKWBOutputStream(BufferedDataOutput buff) {
            super(buff);
        }

        public void writeTo(TWKBOutputStream out) throws IOException {
            BufferedDataOutput bufferedOut = ((BufferedDataOutput) super.out);
            int size = bufferedOut.writtenSize();
            byte[] buff = bufferedOut.buffer();
            out.write(buff, 0, size);
        }
    }

    private static class BufferedDataOutput extends DataOutputStream {

        static BufferedDataOutput create() {
            return new BufferedDataOutput(new InternalByteArrayOutputStream());
        }

        public int writtenSize() {
            return ((InternalByteArrayOutputStream) super.out).size();
        }

        public byte[] buffer() {
            return ((InternalByteArrayOutputStream) super.out).buffer();
        }

        BufferedDataOutput(InternalByteArrayOutputStream out) {
            super(out);
        }

        private static class InternalByteArrayOutputStream extends ByteArrayOutputStream {
            public byte[] buffer() {
                return super.buf;
            }
        }
    }
}
