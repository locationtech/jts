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

    public byte[] write(Geometry geom)
    {
        CodedOutputStream cos = CodedOutputStream.newInstance(byteArrayOS);
        try {
            byteArrayOS.reset();
            write(geom, cos);
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
    public void write(Geometry geom, CodedOutputStream os) throws IOException
    {
        if (geom instanceof Point)
            writePoint((Point) geom, os);
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

    private void writePoint(Point pt, CodedOutputStream os) throws IOException
    {
        // Handle empty geometries first?
        writeHeader(pt, os);
        writeCoordinateSequence(pt.getCoordinateSequence(), false, os);
    }

    private void writeHeader(Geometry g, CodedOutputStream os) throws IOException {
        // Write Geometry Type and Precision Byte

        // Calculate type
        // TODO: Calculate type correctly
        byte geometryType = 1;

        // Calculate precision
        // TODO: Calculate precision correctly.
        byte precision = 0;

        byte typePrecision = (byte)((precision << 4) | geometryType);

        buf[0] = typePrecision;
        //os.write(buf, 1);

        // Write Metadata byte

        // TODO: Compute metadata byte correctly.
        buf[1] = 0x00;
        os.writeRawBytes(buf, 0, 2);


        // Optionally, write extended dimension data

        // Optionally, write size byte

        // Optionally, write bbox

        // Optionally, write id-array

    }

    private void writeCoordinateSequence(CoordinateSequence seq, boolean writeSize, CodedOutputStream os)
            throws IOException
    {
        // TODO: Wire through output dimensions
        byte[] valueArray = new byte[seq.getDimension()];
        double value;

        for (int i = 0; i < seq.size(); i++) {
            for (int j = 0; j < seq.getDimension(); j++) {
                value = seq.getOrdinate(i, j);
                System.out.println("writing value " + value);
                os.writeSInt32NoTag((int) value);
            }
        }

//        if (writeSize)
//            writeInt(seq.size(), os);
//
//        for (int i = 0; i < seq.size(); i++) {
//            writeCoordinate(seq, i, os);
    }
}