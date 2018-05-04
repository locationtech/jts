package org.locationtech.jts.io;

import org.locationtech.jts.geom.*;
import com.google.protobuf.CodedInputStream;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TWKBReader {
    final int twkbPoint = 1;
    final int twkbLineString = 2;
    final int twkbPolygon = 3;
    final int twkbMultiPoint = 4;
    final int twkbMultiLineString = 5;
    final int twkbMultiPolygon = 6;
    final int twkbGeometryCollection = 7;

    private CoordinateSequenceFactory csfactory = new PackedCoordinateSequenceFactory();
    private GeometryFactory factory = new GeometryFactory(csfactory);

    public TWKBReader() {
    }

    public Geometry read(byte[] bytes) throws ParseException {
        CodedInputStream is = CodedInputStream.newInstance(bytes);

        try {
            TWKBMetadata metadata = readMetadata(is);
            CoordinateSequence pts = readCoordinateSequence(is, metadata);

            switch (metadata.getType()) {
                case twkbPoint:
                    return factory.createPoint(pts);
                case twkbLineString:
                    return factory.createLineString(pts);
            }
            return null;
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected IOException caught: " + ex.getMessage());
        }
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

        // Read Optional bits first!

        // TODO: compute dimensions
        int dims = 2;
        if (metadata.hasExtendedDims()) {
            int dimensions = is.readRawByte();

            System.out.println("  reading dimension data :" + dimensions);
            System.out.println("          hasZ: " + (dimensions & 0x01));
            System.out.println("          hasM: " + (dimensions & 0x02));

            if ((dimensions & 0x03) > 0) {
                if ((dimensions & 0x03) < 2) {
                    dims = 3;
                }
                dims = 4;
            }
            System.out.println("  Geometry has " + dims + " dimensions");
        }
        metadata.setDims(dims);

        // TODO: Read optional size?
        if (metadata.hasSize()) {
            metadata.setSize(is.readSInt32());
            // Compute Size
        } else { // TODO: Deal with empty geometries properly
            metadata.setSize(1);
        }


        // TODO: Read Bounding Box relative to extra dimensions
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

    private CoordinateSequence readCoordinateSequence(CodedInputStream is, TWKBMetadata metadata) throws IOException {
        int dims = metadata.getDims();
        int size = metadata.getSize();

        // Create CoordinateSequence and read geometry
        CoordinateSequence seq = csfactory.create(size, dims);
        int targetDim = seq.getDimension();
        // JNH: Ask Martin about this!
        if (targetDim > dims)
            targetDim = dims;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < targetDim; j++) {
                // TODO:  Handle differences in precision between XY, Z, M
                double ordinate = readNextDouble(is, metadata.getPrecision());
                System.out.println(" Calling: " + i + " " + j + " " + ordinate);
                seq.setOrdinate(i, j, ordinate);
            }
        }

        return seq;
    }

    private double readNextDouble(CodedInputStream is, int precision) throws IOException {
        int value = is.readSInt32();
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

        public void setPrecision(int precision) {
            this.precision = precision;
        }

        int precision;
        byte header;
        int size;
        int dims;
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

