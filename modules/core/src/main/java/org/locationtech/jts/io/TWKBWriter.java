/*
 * Copyright (c) 2018 James Hughes
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
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
            write(geom, cos, xyprecision, zprecision, mprecision, includeSize, includeBbox, null);
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
    public double[] write(Geometry geom, CodedOutputStream os,
                      int xyprecision,
                      int zprecision,
                      int mprecision,
                      boolean includeSize,
                      boolean includeBbox,
                      double[] inputValueArray) throws IOException
    {
        writeHeader(geom, os, xyprecision, zprecision, mprecision, includeSize, includeBbox);
        if (geom instanceof Point)
            return writePoint((Point) geom, os, xyprecision, zprecision, mprecision, inputValueArray);
            // LinearRings will be written as LineStrings
        else if (geom instanceof LineString)
            return writeLineString((LineString) geom, os, xyprecision, zprecision, mprecision, inputValueArray);
        else if (geom instanceof Polygon)
            return writePolygon((Polygon) geom, os, xyprecision, zprecision, mprecision, inputValueArray);
        else if (geom instanceof MultiPoint)
            return writeMultiPoint((MultiPoint) geom, os, xyprecision, zprecision, mprecision, inputValueArray);
        else if (geom instanceof MultiLineString)
            return writeMultiLineString((MultiLineString) geom, os, xyprecision, zprecision, mprecision, inputValueArray);
        else if (geom instanceof MultiPolygon)
            return writeMultiPolygon((MultiPolygon) geom, os, xyprecision, zprecision, mprecision, inputValueArray);
        else if (geom instanceof GeometryCollection)
            return writeGeometryCollection((GeometryCollection) geom, os, xyprecision, zprecision, mprecision, includeSize, includeBbox, inputValueArray);
        else {
            Assert.shouldNeverReachHere("Unknown Geometry type");
            return null;
        }
    }

    private double[] writePoint(Point pt, CodedOutputStream os,
                            int xyprecision,
                            int zprecision,
                            int mprecision,
                            double[] inputValueArray) throws IOException
    {
        // Handle empty geometries first?

        return writeCoordinateSequence(pt.getCoordinateSequence(), false, os,
                xyprecision, zprecision, mprecision, inputValueArray);
    }

    private double[] writeLineString(LineString line, CodedOutputStream os,
                            int xyprecision,
                            int zprecision,
                            int mprecision,
                            double[] inputValueArray) throws IOException
    {
        // Handle empty geometries first?
        if (!line.isEmpty()) {
            os.writeInt32NoTag(line.getNumPoints());
            return writeCoordinateSequence(line.getCoordinateSequence(), false, os,
                    xyprecision, zprecision, mprecision, inputValueArray);
        }
        return null;
    }

    private double[] writePolygon(Polygon polygon, CodedOutputStream os,
                                 int xyprecision,
                                 int zprecision,
                                 int mprecision,
                                 double[] inputValueArray) throws IOException
    {
        // Handle empty geometries first?
        if (!polygon.isEmpty()) {
                        os.writeInt32NoTag(polygon.getNumInteriorRing() + 1);
            inputValueArray = writeLineString(polygon.getExteriorRing(), os,
                    xyprecision, zprecision, mprecision, inputValueArray);

            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                inputValueArray = writeLineString(polygon.getInteriorRingN(i), os,
                        xyprecision, zprecision, mprecision, inputValueArray);
            }
        }
        return inputValueArray;
    }

    private double[] writeMultiPoint(MultiPoint mpt, CodedOutputStream os,
                            int xyprecision,
                            int zprecision,
                            int mprecision,
                            double[] inputValueArray) throws IOException
    {
        // Handle empty geometries first?
        if (!mpt.isEmpty()) {
            os.writeInt32NoTag(mpt.getNumGeometries());
            for (int i = 0; i < mpt.getNumGeometries(); i++) {
                inputValueArray = writePoint((Point) mpt.getGeometryN(i), os, xyprecision, zprecision, mprecision, inputValueArray);
            }
        }
        return inputValueArray;
    }

    private double[] writeMultiLineString(MultiLineString multiLineString, CodedOutputStream os,
                                 int xyprecision,
                                 int zprecision,
                                 int mprecision,
                                 double[] inputValueArray) throws IOException
    {
        // Handle empty geometries first?
        if (!multiLineString.isEmpty()) {
            os.writeInt32NoTag(multiLineString.getNumGeometries());
            for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
                inputValueArray = writeLineString((LineString) multiLineString.getGeometryN(i), os, xyprecision, zprecision, mprecision, inputValueArray);
            }
        }
        return inputValueArray;
    }

    private double[] writeMultiPolygon(MultiPolygon multiPolygon, CodedOutputStream os,
                                      int xyprecision,
                                      int zprecision,
                                      int mprecision,
                                      double[] inputValueArray) throws IOException
    {
        if (!multiPolygon.isEmpty()) {
            os.writeInt32NoTag(multiPolygon.getNumGeometries());
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                inputValueArray = writePolygon((Polygon) multiPolygon.getGeometryN(i), os, xyprecision, zprecision, mprecision, inputValueArray);
            }
        }
        return inputValueArray;
    }

    private double[] writeGeometryCollection(GeometryCollection geometryCollection, CodedOutputStream os,
                                   int xyprecision,
                                   int zprecision,
                                   int mprecision,
                                   boolean includeSize,
                                   boolean includeBbox,
                                   double[] inputValueArray) throws IOException
    {
        if (!geometryCollection.isEmpty()) {
            os.writeInt32NoTag(geometryCollection.getNumGeometries());
            for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
                write(geometryCollection.getGeometryN(i), os, xyprecision, zprecision, mprecision, includeSize, includeBbox, null);
            }
        }
        return inputValueArray;
    }


    private int getDimension(Geometry g) {
        if (g.isEmpty()) {
            return 2;  // Why not?!
        }

        if (g instanceof Point) {
            return ((Point) g).getCoordinateSequence().getDimension();
        } else if (g instanceof LineString) {
            return (((LineString) g).getCoordinateSequence().getDimension());
        } else if (g instanceof Polygon) {
            return (((Polygon) g).getExteriorRing().getCoordinateSequence().getDimension());
        } else {
            return getDimension(g.getGeometryN(0));
        }

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
        byte geometryType = 0;
        dim = getDimension(g);
        // TODO: Clean up as Constants
        if (g instanceof Point) {
            geometryType = TWKBReader.twkbPoint;
            //dim = ((Point) g).getCoordinateSequence().getDimension();
        } else if (g instanceof LineString){
            geometryType = TWKBReader.twkbLineString;
            //dim = ((LineString) g).getCoordinateSequence().getDimension();
        } else if (g instanceof Polygon) {
            geometryType = TWKBReader.twkbPolygon;
            //dim = ((Polygon) g).getExteriorRing().getCoordinateSequence().getDimension();
        } else if (g instanceof MultiPoint) {
            geometryType = TWKBReader.twkbMultiPoint;
           // dim = ((Point)(g.getGeometryN(0))).getCoordinateSequence().getDimension();
        } else if (g instanceof MultiLineString) {
            geometryType = TWKBReader.twkbMultiLineString;
            //dim = ((LineString)(g.getGeometryN(0))).getCoordinateSequence().getDimension();
        } else if (g instanceof MultiPolygon) {
            geometryType = TWKBReader.twkbMultiPolygon;
            //dim = ((Polygon)(g.getGeometryN(0))).getExteriorRing().getCoordinateSequence().getDimension();
        } else if (g instanceof GeometryCollection) {
            geometryType = TWKBReader.twkbGeometryCollection;
            //dim = getDimension(g);
        }


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
        byte optionalDimesions = 0;
        if (dim > 2) {
            optionalDimesions |= 0x01; // has Z
            optionalDimesions |= (zprecision << 2);
            if (dim == 4) {
                optionalDimesions |= 0x02; // has M
                optionalDimesions |= (mprecision << 5);
            }
            os.writeRawByte(optionalDimesions);
        }



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


            if (dim > 2) {
                Coordinate[] coords = g.getCoordinates();
                double min[] = new double[dim - 2];
                double max[] = new double[dim - 2];

                for (int i = 0; i < coords.length; i++) {
                    prestorage[4] = Double.MAX_VALUE;
                    prestorage[5] = Double.MIN_VALUE;

                    for (int j = 2; j < dim; j++) {
                        // TODO: HANDLE ZM CASE
                        double value = coords[i].getOrdinate(j);
                        if (value < prestorage[4]) {
                            prestorage[4] = value;
                        }
                        if (value > prestorage[5]) {
                            prestorage[5] = value;
                        }
                    }
                }
                prestorage[5] -= prestorage[4];
            }

            for (int i = 0; i < dim * 2; i++){
                int precision = 0;
                if (i < 4) { precision = xyprecision; }
                if (i == 4 || i == 5) { precision = zprecision; }

                long longToWrite = Math.round((prestorage[i] * Math.pow(10, precision)));
                System.out.println(" Writing bbox " + i + " : " + longToWrite);
                os.writeSInt64NoTag(longToWrite);
            }
        }

        // Optionally, write id-array

    }

    private double[] writeCoordinateSequence(CoordinateSequence seq, boolean writeSize, CodedOutputStream os,
                                         int xyprecision,
                                         int zprecision,
                                         int mprecision,
                                         double[] inputValueArray)
            throws IOException
    {
        // TODO: Wire through output dimensions

        double[] valueArray = new double[seq.getDimension()];

        if (inputValueArray != null) {
            valueArray = inputValueArray;
        }

        double value;
        double valueToWrite;

        for (int i = 0; i < seq.size(); i++) {
            for (int j = 0; j < seq.getDimension(); j++) {
                int precision = xyprecision;
                // TODO: Fix this!
                if (j == 2) { precision = zprecision; }
                if (j == 3) { precision = mprecision; }

                value = seq.getOrdinate(i, j) * Math.pow(10, precision);

                if (i == 0 && inputValueArray == null) {
                    valueToWrite = value;
                } else {
                    valueToWrite = value - valueArray[j];
                }
                valueArray[j] = value;

                long longToWrite = Math.round(valueToWrite);

                System.out.println("writing value " + longToWrite);
                os.writeSInt64NoTag(longToWrite);
            }
        }
        return valueArray;
    }
}