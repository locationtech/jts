package org.locationtech.jts.io;

import com.google.protobuf.CodedOutputStream;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TWKBWriter {

    private ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
    //private OutStream byteArrayOutStream = new OutputStreamOutStream(byteArrayOS);

    private byte[] buf = new byte[8];
    private int outputDimension = 2;

    public TWKBWriter(int outputDimension) {
        this.outputDimension = outputDimension;
    }

    public TWKBWriter() {
    }

    public byte[] write(Geometry geom) {
        return write(geom, 0, 0, 0, false, false);
    }

    public byte[] write(Geometry geom,
                        int xyprecision,
                        int zprecision,
                        int mprecision,
                        boolean includeSize,
                        boolean includeBbox)
    {
        CodedOutputStream cos = CodedOutputStream.newInstance(byteArrayOS);
        try {
            byteArrayOS.reset();
            write(geom, cos, xyprecision, zprecision, mprecision, includeSize, includeBbox);
            cos.flush();
        }
        catch (IOException ex) {
            throw new RuntimeException("Unexpected IO exception: " + ex.getMessage());
        }
        return byteArrayOS.toByteArray();
    }

    /**
     * Writes a {@link Geometry} to an {@link OutStream}.
     *
     * @param geom the geometry to write
     * @param os the out stream to write to
     * @throws IOException if an I/O error occurs
     */
    public void write(Geometry geom, CodedOutputStream os,
                      int xyprecision,
                      int zprecision,
                      int mprecision,
                      boolean includeSize,
                      boolean includeBbox) throws IOException
    {
        writeHeader(geom, os, xyprecision, zprecision, mprecision, includeSize, includeBbox);
        if (geom instanceof Point)
            writePoint((Point) geom, os, xyprecision, zprecision, mprecision);
            // LinearRings will be written as LineStrings
//        else if (geom instanceof LineString)
//            writeLineString((LineString) geom, os);
//        else if (geom instanceof Polygon)
//            writePolygon((Polygon) geom, os);
//        else if (geom instanceof MultiPoint)
//            writeGeometryCollection(WKBConstants.wkbMultiPoint,
//                    (MultiPoint) geom, os);
//        else if (geom instanceof MultiLineString)
//            writeGeometryCollection(WKBConstants.wkbMultiLineString,
//                    (MultiLineString) geom, os);
//        else if (geom instanceof MultiPolygon)
//            writeGeometryCollection(WKBConstants.wkbMultiPolygon,
//                    (MultiPolygon) geom, os);
//        else if (geom instanceof GeometryCollection)
//            writeGeometryCollection(WKBConstants.wkbGeometryCollection,
//                    (GeometryCollection) geom, os);
        else {
            Assert.shouldNeverReachHere("Unknown Geometry type");
        }
    }

    private void writePoint(Point pt, CodedOutputStream os,
                            int xyprecision,
                            int zprecision,
                            int mprecision) throws IOException
    {
        // Handle empty geometries first?

        writeCoordinateSequence(pt.getCoordinateSequence(), false, os,
                xyprecision, zprecision, mprecision);
    }

    private void writeHeader(Geometry g, CodedOutputStream os,
                             int xyprecision,
                             int zprecision,
                             int mprecision,
                             boolean includeSize,
                             boolean includeBbox) throws IOException {
        int dim = 2;
        // Write Geometry Type and Precision Byte

        // Calculate type
        // TODO: Calculate type correctly
        byte geometryType = 1;

        byte typePrecision = (byte)((CodedOutputStream.encodeZigZag32(xyprecision ) << 4) | geometryType);

        buf[0] = typePrecision;
        //os.write(buf, 1);

        // Write Metadata byte

        // TODO: Compute metadata byte correctly.
        buf[1] = 0x00;

        if (includeBbox) { buf[1] |= 0x01; }
        if (includeSize) { buf[1] |= 0x02; }
        //if (There is an id list!   0x04; }
        if (dim > 2)     { buf[1] |= 0x08; }
        if (g.isEmpty()) { buf[1] |= 0x10; }

        os.writeRawBytes(buf, 0, 2);

        // Optionally, write extended dimension data

        // Optionally, write size byte
        // TODO:  This requires computing the size of the rest of the geometry!
//        if (includeSize) {
//            os.writeRawByte(CodedOutputStream.encodeZigZag32(g.getNumGeometries()));
//        }

        // Optionally, write bbox
        // TODO: Handle multiple dimensions
        if (includeBbox) {
            double[] prestorage = new double[dim * 2];

            Envelope env = g.getEnvelopeInternal();
            prestorage[0] = env.getMinX();
            prestorage[1] = env.getMaxX() - prestorage[0];

            prestorage[2] = env.getMinY();
            prestorage[3] = env.getMaxY() - prestorage[2];

            for (int i = 0; i <= 3; i++){
                long longToWrite = Math.round((prestorage[i] * Math.pow(10, xyprecision)));
                os.writeSInt64NoTag(longToWrite);
            }
        }

        // Optionally, write id-array

    }

    private void writeCoordinateSequence(CoordinateSequence seq, boolean writeSize, CodedOutputStream os,
                                         int xyprecision,
                                         int zprecision,
                                         int mprecision)
            throws IOException
    {
        // TODO: Wire through output dimensions
        byte[] valueArray = new byte[seq.getDimension()];
        double value;

        for (int i = 0; i < seq.size(); i++) {
            for (int j = 0; j < seq.getDimension(); j++) {
                value = seq.getOrdinate(i, j) * Math.pow(10, xyprecision);
                long longToWrite = Math.round(value);

                System.out.println("writing value " + longToWrite);
                os.writeSInt64NoTag(longToWrite);
            }
        }

//        if (writeSize)
//            writeInt(seq.size(), os);
//
//        for (int i = 0; i < seq.size(); i++) {
//            writeCoordinate(seq, i, os);
    }
}