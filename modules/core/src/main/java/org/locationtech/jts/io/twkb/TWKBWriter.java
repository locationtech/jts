/*
 * Copyright (c) 2018 James Hughes
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.twkb.TWKBIO.TWKBOutputStream;

/**
 * <pre>
 * {@code
 * 
 * twkb                    := <header> <geometry_body>
 * header                  := <type_and_precision> <metadata_header> [extended_dimensions_header] [geometry_body_size]
 * type_and_precision      := byte := <type_mask OR precision_mask>)
 * type_mask               := <ubyte> (0b0000XXXX -> 1=point, 2=linestring, 3=polygon, 4=multipoint, 
 *                                     5=multilinestring, 6=multipolygon, 7=geometry collection)
 * precision_mask          := <signed byte> (zig-zag encoded 4-bit signed integer, 0bXXXX0000. Number of base-10 decimal places 
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
 * extended_dimensions_header  := byte := <Z_coordinates_presence_flag OR M_coordinates_presence_flag OR Z_precision OR M_precision>
 * Z_coordinates_presence_flag := 0b00000001 
 * M_coordinates_presence_flag := 0b00000010
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
 * # [xmin, deltax, ymin, deltay]                              iif Z_coordinates_presence_flag == 0 AND M_coordinates_presence_flag == 0
 * # [xmin, deltax, ymin, deltay, zmin, deltaz]                iif Z_coordinates_presence_flag == 1 AND M_coordinates_presence_flag == 0
 * # [xmin, deltax, ymin, deltay, zmin, deltaz, mmin, deltam]  iif Z_coordinates_presence_flag == 1 AND M_coordinates_presence_flag == 1
 * # [xmin, deltax, ymin, deltay, mmin, deltam]                iif Z_coordinates_presence_flag == 0 AND M_coordinates_presence_flag == 1
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
public class TWKBWriter {

    private TWKBHeader paramsHeader = TWKBHeader.builder().build();

    public TWKBWriter() {
    }

    /**
     * Number of base-10 decimal places stored.
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
        paramsHeader = paramsHeader.withXyPrecision(xyprecision);
        return this;
    }

    public TWKBWriter setEncodeZ(boolean includeZDimension) {
        paramsHeader = paramsHeader.withHasZ(includeZDimension);
        return this;
    }

    public TWKBWriter setEncodeM(boolean includeMDimension) {
        paramsHeader = paramsHeader.withHasM(includeMDimension);
        return this;
    }

    public TWKBWriter setZPrecision(int zprecision) {
        if (zprecision < 0 || zprecision > 7) {
            throw new IllegalArgumentException("Z precision cannot be negative or greater than 7");
        }
        paramsHeader = paramsHeader.withZPrecision(zprecision);
        return this;
    }

    public TWKBWriter setMPrecision(int mprecision) {
        if (mprecision < 0 || mprecision > 7) {
            throw new IllegalArgumentException("M precision cannot be negative or greater than 7");
        }
        paramsHeader = paramsHeader.withMPrecision(mprecision);
        return this;
    }

    public TWKBWriter setIncludeSize(boolean includeSize) {
        paramsHeader = paramsHeader.withHasSize(includeSize);
        return this;
    }

    public TWKBWriter setIncludeBbox(boolean includeBbox) {
        paramsHeader = paramsHeader.withHasBBOX(includeBbox);
        return this;
    }

    /**
     * Enables or disables the following optimizations at encoding time, defaults to {@code true}:
     * <ul>
     * <li>The {@code xy}, {@code m}, and {@code z} precision of an {@link Geometry#isEmpty() empty}
     * geometry are set to {@code 0}, despite the values of {@link #xyPrecision()},
     * {@link #zPrecision()}, and {@link #mPrecision()}
     * <li>BBOX is not encoded for {@link Point} geometries, despite {@link #hasBBOX()} being
     * {@code true}, and the {@code hasBBOX} flag is encoded as {@code false}
     * <li>{@link #hasZ()} is forcedly encoded as {@code false} if the input geometry has no Z
     * dimension (as per {@link CoordinateSequence#hasZ()}), even if {@code hasZ() == true}, and
     * consequently {@link #zPrecision()} is not set (defaulting to 0 if there's an M dimension).
     * This fixes a possible mismatch in the metadata where a dimension precision may be present in
     * the header where the geometry doesn't really have that dimension.
     * <li>{@link #hasM()} is forcedly encoded as {@code false} if the input geometry has no M
     * dimension (as per {@link CoordinateSequence#hasM()}), even if {@code hasM() == true}, and
     * consequently {@link #mPrecision()} is not set (defaulting to 0 if there's a Z dimension).
     * This fixes a possible mismatch in the metadata where a dimension precision may be present in
     * the header where the geometry doesn't really have that dimension.
     * <li>For a <strong>Polygon</strong> geometry, all its {@link LinearRing}s get their last
     * coordinate removed, as long as that wouldn't result in an invalid geometry. Following the
     * specification's suggestion, the last coordinate of a {@link LinearRing} is removed and
     * therefore it's encoded as a {@link LineString} with one less coordinate. When parsing, such
     * line strings are automatically closed to form a {@code LinearRing}
     * <li>Consecutive coordinates of a {@link CoordinateSequence} that, due to loss of precision,
     * collapse to a single point, are merged into a single coordinate, as long as that doesn't
     * result in an invalid geometry. For example,
     * {@code LINESTRING(0.1 0.1, 0.15 0.15, 1.2 1.2, 1.29 1.29)}, encoded with an {@code xy}
     * precision of {@code 1} decimal places, is collapsed to {@code LINESTRING(0.1 0.1, 1.2 1.2)}
     * instead of {@code LINESTRING(0.1 0.1, 0.1 0.1, 1.2 1.2, 1.2 1.2)}
     * </ul>
     * Disabling these optimizations make the encoding consistent with PostGIS TWKB encoding.
     */
    public TWKBWriter setOptimizedEncoding(boolean optimizedEncoding) {
        paramsHeader = paramsHeader.withOptimizedEncoding(optimizedEncoding);
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
        writeInternal(geom, out);
    }

    final /* @VisibleForTesting */ TWKBHeader writeInternal(Geometry geom, DataOutput out)
            throws IOException {
        Objects.requireNonNull(geom, "geometry is null");
        return TWKBIO.write(geom, TWKBOutputStream.of(out), paramsHeader);
    }
}