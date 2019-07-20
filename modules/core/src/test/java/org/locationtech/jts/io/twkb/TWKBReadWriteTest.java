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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.twkb.TWKBReader;
import org.locationtech.jts.io.twkb.TWKBWriter;
import org.locationtech.jts.io.twkb.TWKBTestSupport.TWKBTestData;

public class TWKBReadWriteTest {

    public @Rule TWKBTestSupport testSupport = new TWKBTestSupport();

    private TWKBWriter writer;

    private TWKBReader reader;

    // a test case shall initialize if it want to the reader to use it, othewise reader used its
    // default factory
    private GeometryFactory readerFactory;

    public @Before void before() {
        writer = new TWKBWriter();
        reader = new TWKBReader();
    }

    public @Test void testXYPrecision() throws ParseException {
        final String encodeWKT = "POINT (12345678.12345678 0)";

        testXYPrecision(encodeWKT, "POINT (12345678 0)", 0);
        testXYPrecision(encodeWKT, "POINT (12345678.1 0)", 1);
        testXYPrecision(encodeWKT, "POINT (12345678.12 0)", 2);
        testXYPrecision(encodeWKT, "POINT (12345678.123 0)", 3);
        testXYPrecision(encodeWKT, "POINT (12345678.1235 0)", 4);
        testXYPrecision(encodeWKT, "POINT (12345678.12346 0)", 5);
        testXYPrecision(encodeWKT, "POINT (12345678.123457 0)", 6);
        testXYPrecision(encodeWKT, "POINT (12345678.1234568 0)", 7);

        testXYPrecision(encodeWKT, "POINT (12345680 0)", -1);
        testXYPrecision(encodeWKT, "POINT (12345700 0)", -2);
        testXYPrecision(encodeWKT, "POINT (12346000 0)", -3);
        testXYPrecision(encodeWKT, "POINT (12350000 0)", -4);
        testXYPrecision(encodeWKT, "POINT (12300000 0)", -5);
        testXYPrecision(encodeWKT, "POINT (12000000 0)", -6);
        testXYPrecision(encodeWKT, "POINT (10000000 0)", -7);
    }

    private void testXYPrecision(String encodeWKT, String expectedWKT, int xyprecision)
            throws ParseException {
        writer.setXYPrecision(xyprecision);
        check(encodeWKT, expectedWKT);
    }

    public @Test void testZMPrecision() throws ParseException {
        final String encodeWKT = "POINT ZM (0 0 12345678.12345678 12345678.12345678)";

        testZMPrecision(0, encodeWKT, "POINT ZM (0 0 12345678 12345678 )");
        testZMPrecision(1, encodeWKT, "POINT ZM (0 0 12345678.1 12345678.1)");
        testZMPrecision(2, encodeWKT, "POINT ZM (0 0 12345678.12 12345678.12)");
        testZMPrecision(3, encodeWKT, "POINT ZM (0 0 12345678.123 12345678.123)");
        testZMPrecision(4, encodeWKT, "POINT ZM (0 0 12345678.1235 12345678.1235)");
        testZMPrecision(5, encodeWKT, "POINT ZM (0 0 12345678.12346 12345678.12346)");
        testZMPrecision(6, encodeWKT, "POINT ZM (0 0 12345678.123457 12345678.123457)");
        testZMPrecision(7, encodeWKT, "POINT ZM (0 0 12345678.1234568 12345678.1234568)");
    }

    private void testZMPrecision(int zmprecision, String encodeWKT, String expectedWKT)
            throws ParseException {

        writer.setZPrecision(zmprecision);
        writer.setMPrecision(zmprecision);
        check(encodeWKT, expectedWKT);
    }

    public @Test void testPoints() throws ParseException {
        testWriteRead(testSupport.getPoints());
    }

    public @Test void testMultiPoints() throws ParseException {
        testWriteRead(testSupport.getMultiPoints());
    }

    public @Test void testLineStrings() throws ParseException {
        testWriteRead(testSupport.getLineStrings());
    }

    public @Test void testMultiLineStrings() throws ParseException {
        testWriteRead(testSupport.getMultiLineStrings());
    }

    public @Test void testPolygons() throws ParseException {
        testWriteRead(testSupport.getPolygons());
    }

    public @Test void testMultiPolygons() throws ParseException {
        testWriteRead(testSupport.getMultiPolygons());
    }

    public @Test void testGeometryCollections() throws ParseException {
        testWriteRead(testSupport.getGeometryCollections());
    }

    private void testWriteRead(List<TWKBTestData> pointsTestData) throws ParseException {
        for (TWKBTestData record : pointsTestData) {
            TWKBTestData withSize = record.withIncludeSize(true);
            boolean optimizedEncoding = true;
            boolean postgisCompatibleEncoding = false;

            check(record, postgisCompatibleEncoding);
            check(withSize, postgisCompatibleEncoding);

            check(record, optimizedEncoding);
            check(withSize, optimizedEncoding);
        }
    }

    private void check(TWKBTestData record, boolean optimizedEncoding) throws ParseException {

        writer.setOptimizedEncoding(optimizedEncoding);
        writer.setXYPrecision(record.getXyprecision());
        writer.setIncludeBbox(record.isIncludeBbox());
        writer.setIncludeSize(record.isIncludeSize());
        writer.setMPrecision(record.getMprecision());
        writer.setZPrecision(record.getZprecision());

        Geometry inputGeometry = record.getInputGeometry();
        Geometry expectedGeometry = record.getExpectedGeometry();

        byte[] encoded = writer.write(inputGeometry);
        Geometry parsed = reader.read(encoded);
        double coordComparisonTolerance = 1e-8;
        boolean equals = expectedGeometry.equalsExact(parsed, coordComparisonTolerance);
        if (!equals) {
            log("optimizedEncoding: %s, precision[xy: %d, z: %d, m: %d], include size: %s, include bbox: %s",
                    optimizedEncoding, record.getXyprecision(), record.getZprecision(),
                    record.getMprecision(), record.isIncludeSize(), record.isIncludeBbox());
            log("input   : %s", record.getInputGeometry());
            log("expected: %s", record.getExpectedTWKBHex());
            log("encoded : %s", testSupport.toHexString(encoded));
            log("expected: %s", record.getExpectedGeometry());
            log("parsed  : %s", parsed);
            log("----------");
            assertEquals(expectedGeometry, parsed);
        }
    }

    private void log(String fmt, Object... args) {
        System.err.printf(fmt + "\n", args);
    }

    private void check(String encodeWKT, String expectedWKT) throws ParseException {
        Geometry inputGeometry = geom(encodeWKT);
        Geometry expectedGeometry = geom(expectedWKT);
        check(inputGeometry, expectedGeometry);
    }

    private void check(Geometry inputGeometry, Geometry expectedGeometry) throws ParseException {
        byte[] encoded = writer.write(inputGeometry);
        Geometry parsed = reader.read(encoded);
        double coordComparisonTolerance = 1e-8;
        boolean equals = expectedGeometry.equalsExact(parsed, coordComparisonTolerance);
        assertTrue(String.format("Expected %s, got %s", expectedGeometry, parsed), equals);
    }

    private Geometry geom(String wkt) throws ParseException {
        WKTReader wktreader = this.readerFactory == null ? new WKTReader()
                : new WKTReader(readerFactory);

        wktreader.setIsOldJtsCoordinateSyntaxAllowed(false);
        return wktreader.read(wkt);
    }
}
