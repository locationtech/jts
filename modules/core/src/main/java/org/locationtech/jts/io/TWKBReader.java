package org.locationtech.jts.io;

import org.locationtech.jts.geom.*;
import com.google.protobuf.CodedInputStream;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TWKBReader {
    final int wkbPoint = 1;
    int wkbLineString = 2;
    int wkbPolygon = 3;
    int wkbMultiPoint = 4;
    int wkbMultiLineString = 5;
    int wkbMultiPolygon = 6;
    int wkbGeometryCollection = 7;

    private CoordinateSequenceFactory csfactory = new PackedCoordinateSequenceFactory();
    private GeometryFactory factory = new GeometryFactory(csfactory);

    public TWKBReader() {
    }

    public Geometry read(byte[] bytes) throws ParseException {
        ByteBuffer bb = ByteBuffer.wrap(bytes);

        //byte typeByte = bb.get();
        int geometryTypeAndPrecision = bb.get();
        int geometryType = geometryTypeAndPrecision & 0x0F;
        int precision = zigzagDecode((geometryTypeAndPrecision & 0xF0) >> 4);

        System.out.println(" Geometry type and precision: " + geometryTypeAndPrecision);
        System.out.println("    type:      " + geometryType);
        System.out.println("    xy precision: " + precision);

        try {
            CoordinateSequence pts = readCoordinateSequence(bb, precision);

            switch (geometryType) {
                case wkbPoint:
                    return factory.createPoint(pts);
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

    private CoordinateSequence readCoordinateSequence(ByteBuffer bb, int precision) throws IOException {
        int header = readHeader(bb);

        // TODO: compute size
        int size = 1;

        // Read Optional bits first!

        // TODO: compute dimensions
        int dims = 2;
        if ((header & 0x08) > 0) {
            int dimensions = bb.get();

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

        CodedInputStream is = CodedInputStream.newInstance(bb);

        // TODO: Read optional size?

        // TODO: Read Boudning Box relative to extra dimensions
        CoordinateSequence bbox = csfactory.create(2, dims);
        for (int i = 0; i < dims; i++) {
            double min = readNextDouble(is, precision);
            double delta = readNextDouble(is, precision);
            bbox.setOrdinate(0, i, min);
            bbox.setOrdinate(1, i, min + delta);
        }
        System.out.println("BBOX read " + bbox);

        // TODO: Read ID list
        CoordinateSequence seq = csfactory.create(size, dims);
        int targetDim = seq.getDimension();
        // JNH: Ask Martin about this!
        if (targetDim > dims)
            targetDim = dims;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < targetDim; j++) {
                double ordinate = readNextDouble(is, precision);
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


    int readHeader(ByteBuffer bb) {
        int header = bb.get();
        System.out.println(" Header: " + header);
        System.out.println("   Has bbox:          " + (header & 0x01));
        System.out.println("   Has size:          " + (header & 0x02));
        System.out.println("   Has id list:       " + (header & 0x04));
        System.out.println("   Has extended dims: " + (header & 0x08));
        System.out.println("   Is empty geometry: " + (header & 0x10));

        return header;
    }
}
