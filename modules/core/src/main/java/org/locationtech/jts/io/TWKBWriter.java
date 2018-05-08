package org.locationtech.jts.io;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TWKBWriter {

    private ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
    private OutStream byteArrayOutStream = new OutputStreamOutStream(byteArrayOS);

    public byte[] write(Geometry geom)
    {
        try {
            byteArrayOS.reset();
            write(geom, byteArrayOutStream);
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
    public void write(Geometry geom, OutStream os) throws IOException
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

    private void writePoint(Point pt, OutStream os) throws IOException
    {
        // Handle empty geometries first?

//        if (pt.getCoordinateSequence().size() == 0)
//            throw new IllegalArgumentException("Empty Points cannot be represented in WKB");
//        writeByteOrder(os);
//        writeGeometryType(WKBConstants.wkbPoint, pt, os);
        writeHeader(pt, os);
        writeCoordinateSequence(pt.getCoordinateSequence(), false, os);
    }

    private void writeHeader(Geometry g, OutStream os) {
    }

    private void writeCoordinateSequence(CoordinateSequence seq, boolean writeSize, OutStream os)
            throws IOException
    {
//        if (writeSize)
//            writeInt(seq.size(), os);
//
//        for (int i = 0; i < seq.size(); i++) {
//            writeCoordinate(seq, i, os);
    }
}