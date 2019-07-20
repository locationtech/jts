/*
 * Copyright (c) 2016 Vivid Solutions.
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.twkb.TWKBReader;
import org.locationtech.jts.io.twkb.TWKBTestSupport.TWKBTestData;

/**
 * Tests for reading TWKB.
 * 
 * @author James Hughes
 * @author Gabriel Roldan
 */
public class TWKBReaderTest {

    public @Rule TWKBTestSupport testSupport = new TWKBTestSupport();

    private GeometryFactory geomFactory;

    private TWKBReader reader;

    public @Before void before() {
        reader = new TWKBReader();
    }

    public @Test void testZMPrecision() throws ParseException {

        testReadGeometry("POINT ZM (0 0 12345678 12345678 )", "01080300009c85e30b9c85e30b");

        testReadGeometry("POINT ZM (0 0 12345678.1 12345678.1)", "01082700009ab4de759ab4de75");

        testReadGeometry("POINT ZM (0 0 12345678.12 12345678.12)",
                "01084b0000888ab09909888ab09909");

        testReadGeometry("POINT ZM (0 0 12345678.123 12345678.123)",
                "01086f0000d6e4e0fd5bd6e4e0fd5b");

        testReadGeometry("POINT ZM (0 0 12345678.1235 12345678.1235)",
                "0108930000e6eec7e99707e6eec7e99707");

        testReadGeometry("POINT ZM (0 0 12345678.12346 12345678.12346)",
                "0108b70000f4d3ce9fee47f4d3ce9fee47");

        testReadGeometry("POINT ZM (0 0 12345678.123457 12345678.123457)",
                "0108db000082c792bccece0582c792bccece05");

        testReadGeometry("POINT ZM (0 0 12345678.1234568 12345678.1234568)",
                "0108ff000090c6b9d990923890c6b9d9909238");
    }

    @Ignore
    public @Test void testOptimizations() {
        fail("Implement me");
    }

    public @Test void testProvidedGeometryFactory() throws ParseException {
        this.geomFactory = new GeometryFactory();
        testReadAll(testSupport.getPoints());

        this.geomFactory = new GeometryFactory(PackedCoordinateSequenceFactory.DOUBLE_FACTORY);
        testReadAll(testSupport.getPoints());
    }

    public @Test void testPoints() throws ParseException {
        testReadAll(testSupport.getPoints());
    }

    public @Test void testMultiPoints() throws ParseException {
        testReadAll(testSupport.getMultiPoints());
    }

    public @Test void testLineStrings() throws ParseException {
        testReadAll(testSupport.getLineStrings());
    }

    public @Test void testMultiLineStrings() throws ParseException {
        testReadAll(testSupport.getMultiLineStrings());
    }

    public @Test void testPolygons() throws ParseException {
        testReadAll(testSupport.getPolygons());
    }

    public @Test void testMultiPolygons() throws ParseException {
        testReadAll(testSupport.getMultiPolygons());
    }

    public @Test void testGeometryCollections() throws ParseException {
        testReadAll(testSupport.getGeometryCollections());
    }

    private void testReadAll(List<TWKBTestData> testData) throws ParseException {
        for (TWKBTestData d : testData) {
            testRead(d);
        }
    }

    private void testReadGeometry(String expedctedWKT, String encodedHex) throws ParseException {
        Geometry expected = geom(expedctedWKT);
        byte[] twkb = WKBReader.hexToBytes(encodedHex);
        testRead(expected, twkb);
    }

    private void testRead(TWKBTestData d) throws ParseException {
        try {
            testRead(d.getExpectedGeometry(), d.getExpectedTWKB());
        } catch (AssertionError e) {
            log("precision[xy: %d, z: %d, m: %d], include size: %s, include bbox: %s",
                    d.getXyprecision(), d.getZprecision(), d.getMprecision(), d.isIncludeSize(),
                    d.isIncludeBbox());
            log("input     : %s", d.getInputWKT());
            log("input twkb: %s", d.getExpectedTWKBHex());
            log("expected  : %s", d.getExpectedGeometry());
            log("parsed    : %s", reader.read(d.getExpectedTWKB()));
            log("----------");
            throw e;
        }
    }

    private void testRead(Geometry expected, byte[] twkb) {
        reader.setGeometryFactory(geomFactory);
        Geometry parsed;
        try {
            parsed = reader.read(twkb);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        boolean equals = expected.equalsExact(parsed, 1e-8);
        assertTrue(String.format("Expected %s, got %s", expected, parsed), equals);
    }

    private void log(String fmt, Object... args) {
        System.err.printf(fmt + "\n", args);
    }

    private Geometry geom(String wkt) throws ParseException {
        WKTReader wktreader = this.geomFactory == null ? new WKTReader()
                : new WKTReader(geomFactory);
        wktreader.setIsOldJtsCoordinateSyntaxAllowed(false);
        return wktreader.read(wkt);
    }

}
