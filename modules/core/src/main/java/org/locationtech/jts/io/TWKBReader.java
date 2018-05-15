package org.locationtech.jts.io;

import com.google.protobuf.CodedInputStream;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;

import java.io.IOException;

public class TWKBReader {
    static final int twkbPoint = 1;
    static final int twkbLineString = 2;
    static final int twkbPolygon = 3;
    static final int twkbMultiPoint = 4;
    static final int twkbMultiLineString = 5;
    static final int twkbMultiPolygon = 6;
    static final int twkbGeometryCollection = 7;

    private CoordinateSequenceFactory csfactory = new PackedCoordinateSequenceFactory();
    private GeometryFactory factory = new GeometryFactory(csfactory);

    private static final String INVALID_GEOM_TYPE_MSG
            = "Invalid geometry type encountered in ";

    public TWKBReader() {
    }

    public Geometry read(byte[] bytes) throws ParseException {
        CodedInputStream is = CodedInputStream.newInstance(bytes);
        try {
            return readGeometry(is);
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected IOException caught: " + ex.getMessage());
        }
    }

    private Geometry readGeometry(CodedInputStream is) throws IOException, ParseException {

        TWKBMetadata metadata = readMetadata(is);
        switch (metadata.getType()) {
            case twkbPoint:
                return readPoint(is, metadata);
            case twkbLineString:
                return readLineString(is, metadata);
            case twkbPolygon:
                return readPolygon(is, metadata);
            case twkbMultiPoint:
                return readMultiPoint(is, metadata);
            case twkbMultiLineString:
                return readMultiLineString(is, metadata);
            case twkbMultiPolygon:
                return readMultiPolygon(is, metadata);
            case twkbGeometryCollection:
                return readGeometryCollection(is, metadata);
        }
        return null;
    }

    private Point readPoint(CodedInputStream is, TWKBMetadata metadata) throws IOException {
        if (!metadata.isEmpty()) {
            CoordinateSequence cs = readCoordinateSequence(is, 1, metadata);
            return factory.createPoint(cs);
        } else {
            return factory.createPoint();
        }
    }

    private LineString readLineString(CodedInputStream is, TWKBMetadata metadata) throws IOException {
        if (!metadata.isEmpty()) {
            int size = is.readInt32();
            CoordinateSequence cs = readCoordinateSequence(is, size, metadata);
            return factory.createLineString(cs);
        } else {
            return factory.createLineString();
        }
    }

    private Polygon readPolygon(CodedInputStream is, TWKBMetadata metadata) throws IOException {
        if (!metadata.isEmpty()) {
            int numRings = is.readInt32();
            LinearRing[] holes = null;
            if (numRings > 1)
                holes = new LinearRing[numRings - 1];

            LinearRing shell = readLinearRing(is, metadata);
            for (int i = 0; i < numRings - 1; i++) {
                holes[i] = readLinearRing(is, metadata);
            }
            return factory.createPolygon(shell, holes);
        } else {
            return factory.createPolygon();
        }
    }

    private MultiPoint readMultiPoint(CodedInputStream is, TWKBMetadata metadata) throws IOException {
        if (!metadata.isEmpty()) {
            int numGeom = is.readInt32();
            Point[] geoms = new Point[numGeom];
            for (int i = 0; i < numGeom; i++) {
                geoms[i] = readPoint(is, metadata);
            }
            return factory.createMultiPoint(geoms);
        } else {
            return factory.createMultiPoint();
        }
    }
    private MultiLineString readMultiLineString(CodedInputStream is, TWKBMetadata metadata) throws IOException, ParseException {
        if (!metadata.isEmpty()) {
            int numGeom = is.readInt32();
            LineString[] geoms = new LineString[numGeom];
            for (int i = 0; i < numGeom; i++) {
                geoms[i] = readLineString(is, metadata);
            }
            return factory.createMultiLineString(geoms);
        } else {
            return factory.createMultiLineString();
        }
    }
    private MultiPolygon readMultiPolygon(CodedInputStream is, TWKBMetadata metadata) throws IOException, ParseException {
        if (!metadata.isEmpty()) {
            int numGeom = is.readInt32();
            Polygon[] geoms = new Polygon[numGeom];
            for (int i = 0; i < numGeom; i++) {
                geoms[i] = readPolygon(is, metadata);
            }
            return factory.createMultiPolygon(geoms);
        } else {
            return factory.createMultiPolygon();
        }
    }
    private GeometryCollection readGeometryCollection(CodedInputStream is, TWKBMetadata metadata) throws IOException, ParseException {
        if (!metadata.isEmpty()) {
            int numGeom = is.readInt32();
            Geometry[] geoms = new Geometry[numGeom];
            for (int i = 0; i < numGeom; i++) {
                geoms[i] = readGeometry(is);
            }
            return factory.createGeometryCollection(geoms);
        } else {
            return factory.createGeometryCollection();
        }
    }


    private LinearRing readLinearRing(CodedInputStream is, TWKBMetadata metadata) throws IOException
    {
        int size = is.readInt32(); //.readInt();
        CoordinateSequence pts = readCoordinateSequenceRing(is, size, metadata);
        return factory.createLinearRing(pts);
    }

    private CoordinateSequence readCoordinateSequenceRing(CodedInputStream is, int size, TWKBMetadata metadata) throws IOException
    {
        CoordinateSequence seq = readCoordinateSequence(is, size, metadata);
        //if (isStrict) return seq;
        if (CoordinateSequences.isRing(seq)) return seq;
        return CoordinateSequences.ensureValidRing(csfactory, seq);
    }


    private int zigzagDecode(int input) {
        return (input >> 1) ^ (-(input & 1));
    }

    private int zigzagEncode(int input) {
        return (input << 1) ^ (input >> 31);
    }

    private TWKBMetadata readMetadata(CodedInputStream is) throws IOException {
        TWKBMetadata metadata = new TWKBMetadata();

        int geometryTypeAndPrecision = is.readRawByte();
        int geometryType = geometryTypeAndPrecision & 0x0F;
        int precision = zigzagDecode((geometryTypeAndPrecision & 0xF0) >> 4);

        System.out.println(" Geometry type and precision: " + geometryTypeAndPrecision);
        System.out.println("    type:      " + geometryType);
        System.out.println("    xy precision: " + precision);

        metadata.setType(geometryType);
        metadata.setPrecision(precision);

        byte header = readHeader(is);

        metadata.setHeader(header);

        int dims = 2;
        if (metadata.hasExtendedDims()) {
            int dimensions = is.readRawByte();


            if ((dimensions & 0x01) > 0) {
                dims += 1;
            }
            if ((dimensions & 0x02) > 0) {
                dims += 1;
            }

            System.out.println("  reading dimension data :" + dimensions);
            if ((dimensions & 0x01) > 0) {
                metadata.setHasZ(true);
                metadata.setZprecision((dimensions & 0x1C) >> 2);
                System.out.println("      hasZ with precision: " + metadata.getZprecision());
            }
            if ((dimensions & 0x02) > 0) {
                metadata.setHasM(true);
                metadata.setMprecision((dimensions & 0xE0) >> 5);
                System.out.println("      hasM with precision: " + metadata.getMprecision());
            }

            System.out.println("  Geometry has " + dims + " dimensions");

        }
        metadata.setDims(dims);

        // TODO: Read optional size?
        if (metadata.hasSize()) {
            metadata.setSize(is.readSInt32());
        } else { // TODO: Deal with empty geometries properly
            metadata.setSize(1);
        }

        if (metadata.hasBBOX()) {
            CoordinateSequence bbox = csfactory.create(2, dims);
            for (int i = 0; i < dims; i++) {
                double min = readNextDouble(is, precision);
                double delta = readNextDouble(is, precision);
                bbox.setOrdinate(0, i, min);
                bbox.setOrdinate(1, i, min + delta);
            }
            System.out.println("BBOX read " + bbox);
            metadata.setEnvelope(bbox);
        }

        // TODO: Read ID list

        return metadata;
    }

    private CoordinateSequence readCoordinateSequence(CodedInputStream is, int numPts, TWKBMetadata metadata) throws IOException {
        int dims = metadata.getDims();

        // Create CoordinateSequence and read geometry
        CoordinateSequence seq = csfactory.create(numPts, dims);
        // TODO: Make this exception 'better'.
        if (seq.getDimension() != dims) {
            throw new IOException("Dimension mismatch between CoordinateSequenceFactory and input.");
        }


        for (int i = 0; i < numPts; i++) {
            for (int j = 0; j < dims; j++) {
                // TODO:  Handle differences in precision between XY, Z, M
                double ordinateDelta = readNextDouble(is, metadata.getPrecision(j));
                metadata.valueArray[j] += ordinateDelta;

                System.out.println(" Calling: " + i + " " + j + " " + metadata.valueArray[j]);
                seq.setOrdinate(i, j, metadata.valueArray[j]);
            }
        }

        return seq;
    }

    private double readNextDouble(CodedInputStream is, int precision) throws IOException {
        long value = is.readSInt64();
        return value / Math.pow(10, precision);
    }

    byte readHeader(CodedInputStream is) throws IOException {
        byte header = is.readRawByte();
        System.out.println(" Header: " + header);
        System.out.println("   Has bbox:          " + (header & 0x01));
        System.out.println("   Has size:          " + (header & 0x02));
        System.out.println("   Has id list:       " + (header & 0x04));
        System.out.println("   Has extended dims: " + (header & 0x08));
        System.out.println("   Is empty geometry: " + (header & 0x10));

        return header;
    }

    public class TWKBMetadata {
        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getDims() {
            return dims;
        }

        public void setDims(int dims) {
            this.dims = dims;
            this.valueArray = new double[this.dims];
        }

        public byte getHeader() {
            return header;
        }

        public void setHeader(byte header) {
            this.header = header;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        int type;

        public int getPrecision() {
            return precision;
        }

        public int getPrecision(int i) {
            if (i >= 0 && i <= 1) {
                return precision;
            } else if (i == 2) {
                if (hasZ) {
                    return zprecision;
                } else if (hasM) {
                    return mprecision;
                } else {
                    throw new IllegalArgumentException("Geometry only has XY dimensions.");
                }
            } else if (i == 3 && hasZ && hasM) {
                return mprecision;
            } else {
                throw new IllegalArgumentException("Mismatch with the number of dimensions.");
            }
        }

        public void setPrecision(int precision) {
            this.precision = precision;
        }

        double[] valueArray;
        int precision;

        public int getZprecision() {
            return zprecision;
        }

        public void setZprecision(int zprecision) {
            this.zprecision = zprecision;
        }

        public int getMprecision() {
            return mprecision;
        }

        public void setMprecision(int mprecision) {
            this.mprecision = mprecision;
        }

        int zprecision;
        int mprecision;
        byte header;
        int size;
        int dims;

        boolean hasZ, hasM;

        public boolean isHasZ() {
            return hasZ;
        }

        public void setHasZ(boolean hasZ) {
            this.hasZ = hasZ;
        }

        public boolean isHasM() {
            return hasM;
        }

        public void setHasM(boolean hasM) {
            this.hasM = hasM;
        }

        CoordinateSequence envelope;

        public CoordinateSequence getEnvelope() {
            return envelope;
        }

        public void setEnvelope(CoordinateSequence envelope) {
            this.envelope = envelope;
        }

        public TWKBMetadata() {
        }

        boolean hasBBOX() {
            return (header & 0x01) > 0;
        }
        boolean hasSize() {
            return (header & 0x02) > 0;
        }
        boolean hasIdList() {
            return (header & 0x04) > 0;
        }
        boolean hasExtendedDims() {
            return (header & 0x08) > 0;
        }
        boolean isEmpty() {
            return (header & 0x10) > 0;
        }

    }
}

