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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.twkb.TWKBHeader;
import org.locationtech.jts.io.twkb.TWKBWriter;
import org.locationtech.jts.io.twkb.TWKBTestSupport.TWKBTestData;

public class TWKBWriterTest {

    public @Rule TWKBTestSupport testSupport = new TWKBTestSupport();

    private TWKBWriter writer = new TWKBWriter();

    public @Test void testEmptyGeometries() throws ParseException {
        check("POINT EMPTY", -1, 0, 0, false, false, "1110");
        check("POINT EMPTY", 0, 0, 0, false, false, "0110");
        check("POINT EMPTY", 1, 0, 0, false, false, "2110");
        check("POINT EMPTY", 5, 0, 0, false, false, "a110");

        check("LINESTRING EMPTY", -1, 0, 0, false, false, "1210");
        check("LINESTRING EMPTY", 0, 0, 0, false, false, "0210");
        check("LINESTRING EMPTY", 1, 0, 0, false, false, "2210");
        check("LINESTRING EMPTY", 5, 0, 0, false, false, "a210");

        check("POLYGON EMPTY", -1, 0, 0, false, false, "1310");
        check("POLYGON EMPTY", 0, 0, 0, false, false, "0310");
        check("POLYGON EMPTY", 1, 0, 0, false, false, "2310");
        check("POLYGON EMPTY", 5, 0, 0, false, false, "a310");

        check("MULTIPOINT EMPTY", -1, 0, 0, false, false, "1410");
        check("MULTIPOINT EMPTY", 0, 0, 0, false, false, "0410");
        check("MULTIPOINT EMPTY", 1, 0, 0, false, false, "2410");
        check("MULTIPOINT EMPTY", 5, 0, 0, false, false, "a410");

        check("MULTILINESTRING EMPTY", -1, 0, 0, false, false, "1510");
        check("MULTILINESTRING EMPTY", 0, 0, 0, false, false, "0510");
        check("MULTILINESTRING EMPTY", 1, 0, 0, false, false, "2510");
        check("MULTILINESTRING EMPTY", 5, 0, 0, false, false, "a510");

        check("MULTIPOLYGON EMPTY", -1, 0, 0, false, false, "1610");
        check("MULTIPOLYGON EMPTY", 0, 0, 0, false, false, "0610");
        check("MULTIPOLYGON EMPTY", 1, 0, 0, false, false, "2610");
        check("MULTIPOLYGON EMPTY", 5, 0, 0, false, false, "a610");

        check("GEOMETRYCOLLECTION EMPTY", -1, 0, 0, false, false, "1710");
        check("GEOMETRYCOLLECTION EMPTY", 0, 0, 0, false, false, "0710");
        check("GEOMETRYCOLLECTION EMPTY", 1, 0, 0, false, false, "2710");
        check("GEOMETRYCOLLECTION EMPTY", 5, 0, 0, false, false, "a710");
    }

    public @Test void testXYPrecision() throws ParseException {
        final String encodeWKT = "POINT (12345678.12345678 0)";

        testXYPrecision(0, encodeWKT, "01009c85e30b00");
        testXYPrecision(1, encodeWKT, "21009ab4de7500");
        testXYPrecision(2, encodeWKT, "4100888ab0990900");
        testXYPrecision(3, encodeWKT, "6100d6e4e0fd5b00");
        testXYPrecision(4, encodeWKT, "8100e6eec7e9970700");
        testXYPrecision(5, encodeWKT, "a100f4d3ce9fee4700");
        testXYPrecision(6, encodeWKT, "c10082c792bccece0500");
        testXYPrecision(7, encodeWKT, "e10090c6b9d990923800");

        testXYPrecision(-1, encodeWKT, "110090da960100");
        testXYPrecision(-2, encodeWKT, "310082890f00");
        testXYPrecision(-3, encodeWKT, "5100f4c00100");
        testXYPrecision(-4, encodeWKT, "7100a61300");
        testXYPrecision(-5, encodeWKT, "9100f60100");
        testXYPrecision(-6, encodeWKT, "b1001800");
        testXYPrecision(-7, encodeWKT, "d1000200");
    }

    private void testXYPrecision(int xyprecision, String encodeWKT, String expectedHex)
            throws ParseException {
        boolean noSize = false;
        boolean noBbox = false;
        int zprecision = 0;
        int mprecision = 0;

        check(encodeWKT, xyprecision, zprecision, mprecision, noSize, noBbox, expectedHex);
    }

    public @Test void testZMPrecision() throws ParseException {
        final String encodeWKT = "POINT ZM (0 0 12345678.12345678 12345678.12345678)";

        testZMPrecision(0, encodeWKT, "01080300009c85e30b9c85e30b");
        testZMPrecision(1, encodeWKT, "01082700009ab4de759ab4de75");
        testZMPrecision(2, encodeWKT, "01084b0000888ab09909888ab09909");
        testZMPrecision(3, encodeWKT, "01086f0000d6e4e0fd5bd6e4e0fd5b");
        testZMPrecision(4, encodeWKT, "0108930000e6eec7e99707e6eec7e99707");
        testZMPrecision(5, encodeWKT, "0108b70000f4d3ce9fee47f4d3ce9fee47");
        testZMPrecision(6, encodeWKT, "0108db000082c792bccece0582c792bccece05");
        testZMPrecision(7, encodeWKT, "0108ff000090c6b9d990923890c6b9d9909238");
    }

    private void testZMPrecision(int zmprecision, String encodeWKT, String expectedHex)
            throws ParseException {
        boolean noSize = false;
        boolean noBbox = false;
        int xyprecision = 0;
        check(encodeWKT, xyprecision, zmprecision, zmprecision, noSize, noBbox, expectedHex);
    }

    public @Test void testIncludeSizeOnEmptyGeometries() throws ParseException {
        final boolean withSize = true;
        final boolean noBbox = false;
        check("POINT EMPTY", -1, 0, 0, withSize, noBbox, "111200");
        check("LINESTRING EMPTY", -1, 0, 0, withSize, noBbox, "121200");
        check("LINESTRING EMPTY", 5, 0, 0, withSize, noBbox, "a21200");
        check("POLYGON EMPTY", 5, 0, 0, withSize, noBbox, "a31200");
        check("MULTIPOINT EMPTY", 0, 0, 0, withSize, noBbox, "041200");
        check("MULTILINESTRING EMPTY", 1, 0, 0, withSize, noBbox, "251200");
        check("MULTIPOLYGON EMPTY", -1, 0, 0, withSize, noBbox, "161200");
        check("GEOMETRYCOLLECTION EMPTY", 0, 0, 0, withSize, noBbox, "071200");
    }

    public @Test void testEmptyGeometriesIncludeBboxIgnored() throws ParseException {
        final boolean withBbox = true;
        check("POINT EMPTY", -1, 0, 0, false, withBbox, "1110");
        check("POINT EMPTY", -1, 0, 0, true, withBbox, "111200");

        check("LINESTRING EMPTY", -1, 0, 0, false, withBbox, "1210");
        check("LINESTRING EMPTY", -1, 0, 0, true, withBbox, "121200");

        check("LINESTRING EMPTY", 5, 0, 0, false, withBbox, "a210");
        check("LINESTRING EMPTY", 5, 0, 0, true, withBbox, "a21200");

        check("POLYGON EMPTY", 5, 0, 0, false, withBbox, "a310");
        check("POLYGON EMPTY", 5, 0, 0, true, withBbox, "a31200");

        check("MULTIPOINT EMPTY", 0, 0, 0, false, withBbox, "0410");
        check("MULTIPOINT EMPTY", 0, 0, 0, true, withBbox, "041200");

        check("MULTILINESTRING EMPTY", 1, 0, 0, false, withBbox, "2510");
        check("MULTILINESTRING EMPTY", 1, 0, 0, true, withBbox, "251200");

        check("MULTIPOLYGON EMPTY", -1, 0, 0, false, withBbox, "1610");
        check("MULTIPOLYGON EMPTY", -1, 0, 0, true, withBbox, "161200");

        check("GEOMETRYCOLLECTION EMPTY", 0, 0, 0, false, withBbox, "0710");
        check("GEOMETRYCOLLECTION EMPTY", 0, 0, 0, true, withBbox, "071200");
    }

    @Ignore
    public @Test void testOptimizations() {
        fail("Implement me");
    }

    public @Test void testPoints() {
        // disable optimization, test data created with postgis doesn't have it
        writer.setOptimizedEncoding(false);
        testEncode(testSupport.getPoints());
    }

    public @Test void testPointsOptimized() throws ParseException {
        writer.setOptimizedEncoding(true);
        check("POINTZ(1 2 3)", 0, 0, 0, false, true, "010801020406");

        writer.setOptimizedEncoding(false);
        check("POINTZ(1 2 3)", 0, 0, 0, false, true, "010901020004000600020406");

        writer.setOptimizedEncoding(false);
        check("POINT(1 2)", 0, 0, 0, false, true, "0101020004000204");

        writer.setOptimizedEncoding(true);
        check("POINT(1 2)", 0, 0, 0, false, true, "01000204");

    }

    public @Test void testMultiPoints() {
        // disable optimization, test data created with postgis doesn't have it
        writer.setOptimizedEncoding(false);
        testEncode(testSupport.getMultiPoints());
    }

    public @Test void testLineStrings() {
        // disable optimization, test data created with postgis doesn't have it
        writer.setOptimizedEncoding(true);
        testEncode(testSupport.getLineStrings());
    }

    public @Test void testMultiLineStrings() {
        // disable optimization, test data created with postgis doesn't have it
        writer.setOptimizedEncoding(true);
        testEncode(testSupport.getMultiLineStrings());
    }

    public @Test void testPolyongs() {
        // disable optimization, test data created with postgis doesn't have it
        writer.setOptimizedEncoding(false);
        testEncode(testSupport.getPolygons());
    }

    public @Test void testMultiPolyongs() {
        // disable optimization, test data created with postgis doesn't have it
        writer.setOptimizedEncoding(false);
        testEncode(testSupport.getMultiPolygons());
    }

    public @Test void testGeometryCollections() {
        // disable optimization, test data created with postgis doesn't have it
        writer.setOptimizedEncoding(false);
        testEncode(testSupport.getGeometryCollections());
    }

    private void testEncode(List<TWKBTestData> testData) {
        testData.forEach(this::testEncode);
    }

    private void testEncode(TWKBTestData testData) {
        String input = testData.getInputWKT();
        int xyprecision = testData.getXyprecision();
        int zprecision = testData.getZprecision();
        int mprecision = testData.getMprecision();
        boolean includeSize = testData.isIncludeSize();
        boolean includeBbox = testData.isIncludeBbox();
        String expectedTWKB = testData.getExpectedTWKBHex();
        try {
            check(input, xyprecision, zprecision, mprecision, includeSize, includeBbox,
                    expectedTWKB);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void check(String inputWKT, int xyprecision, int zprecision, int mprecision,
            boolean includeSize, boolean includeBbox, String expectedTWKB) throws ParseException {

        Geometry geom = testSupport.parseWKT(inputWKT);
        byte[] twkb = WKBReader.hexToBytes(expectedTWKB);

        writer.setXYPrecision(xyprecision);
        writer.setZPrecision(zprecision);
        writer.setMPrecision(mprecision);
        writer.setIncludeSize(includeSize);
        writer.setIncludeBbox(includeBbox);
        byte[] written = writer.write(geom);

        boolean isEqualHex = Arrays.equals(twkb, written);

        String expected = expectedTWKB;
        String actual = testSupport.toHexString(written);

        if (!isEqualHex) {
            log("precision[xy: %d, z: %d, m: %d], include size: %s, include bbox: %s", xyprecision,
                    zprecision, mprecision, includeSize, includeBbox);
            log("input   : %s", inputWKT);
            log("expected: %s", expected);
            log("encoded : %s", actual);
            try {
                TWKBHeader resultHeader = writer.writeInternal(geom,
                        (DataOutput) new DataOutputStream(new ByteArrayOutputStream()));
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                resultHeader.writeTo(new DataOutputStream(out));
                log("header  : %s", testSupport.toHexString(out.toByteArray()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            log("----------");
            log("\\set g '%s'", inputWKT);
            log("SELECT :'g' AS input, %d AS xy, %d AS z, %d AS m, %s AS size, %s AS bbox,",
                    xyprecision, zprecision, mprecision, includeSize, includeBbox);
            log("\tST_AsText(ST_GeomFromTWKB( ST_AsTWKB(:'g'::Geometry, %d, %d, %d, %s, %s))) AS expected_wkt,",
                    xyprecision, zprecision, mprecision, includeSize, includeBbox);
            log("\tST_AsTWKB(:'g'::Geometry, %d, %d, %d, %s, %s) AS expected_twkb;", xyprecision,
                    zprecision, mprecision, includeSize, includeBbox);
            assertEquals(expected, actual);
        }
    }

    private void log(String fmt, Object... args) {
        System.err.printf(fmt + "\n", args);
    }
}
