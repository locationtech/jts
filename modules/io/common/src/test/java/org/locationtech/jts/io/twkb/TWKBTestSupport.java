/*
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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.rules.ExternalResource;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;

public class TWKBTestSupport extends ExternalResource {

    public static final class TWKBTestData {
        private String inputWKT;

        private Geometry inputGeometry;

        private int xyprecision;

        private int zprecision;

        private int mprecision;

        private boolean includeSize;

        private boolean includeBbox;

        private String expectedTWKBHex;

        private byte[] expectedTWKB;

        private Geometry expectedGeometry;

        TWKBTestData() {
            // NO-OP
        }

        TWKBTestData(TWKBTestData other) {
            this.inputWKT = other.inputWKT;
            this.inputGeometry = other.inputGeometry;
            this.xyprecision = other.xyprecision;
            this.zprecision = other.zprecision;
            this.mprecision = other.mprecision;
            this.includeSize = other.includeSize;
            this.includeBbox = other.includeBbox;
            this.expectedTWKBHex = other.expectedTWKBHex;
            this.expectedTWKB = other.expectedTWKB;
            this.expectedGeometry = other.expectedGeometry;
        }

        public String getInputWKT() {
            return this.inputWKT;
        }

        public Geometry getInputGeometry() {
            return this.inputGeometry;
        }

        public int getXyprecision() {
            return this.xyprecision;
        }

        public int getZprecision() {
            return this.zprecision;
        }

        public int getMprecision() {
            return this.mprecision;
        }

        public boolean isIncludeSize() {
            return this.includeSize;
        }

        public boolean isIncludeBbox() {
            return this.includeBbox;
        }

        public String getExpectedTWKBHex() {
            return this.expectedTWKBHex;
        }

        public byte[] getExpectedTWKB() {
            return this.expectedTWKB;
        }

        public Geometry getExpectedGeometry() {
            return this.expectedGeometry;
        }

        public TWKBTestData setInputWKT(String inputWKT) {
            this.inputWKT = inputWKT;
            return this;
        }

        public TWKBTestData setInputGeometry(Geometry inputGeometry) {
            this.inputGeometry = inputGeometry;
            return this;
        }

        public TWKBTestData setXyprecision(int xyprecision) {
            this.xyprecision = xyprecision;
            return this;
        }

        public TWKBTestData setZprecision(int zprecision) {
            this.zprecision = zprecision;
            return this;
        }

        public TWKBTestData setMprecision(int mprecision) {
            this.mprecision = mprecision;
            return this;
        }

        public TWKBTestData setIncludeSize(boolean includeSize) {
            this.includeSize = includeSize;
            return this;
        }

        public TWKBTestData setIncludeBbox(boolean includeBbox) {
            this.includeBbox = includeBbox;
            return this;
        }

        public TWKBTestData setExpectedTWKBHex(String expectedTWKBHex) {
            this.expectedTWKBHex = expectedTWKBHex;
            return this;
        }

        public TWKBTestData setExpectedTWKB(byte[] expectedTWKB) {
            this.expectedTWKB = expectedTWKB;
            return this;
        }

        public TWKBTestData setExpectedGeometry(Geometry expectedGeometry) {
            this.expectedGeometry = expectedGeometry;
            return this;
        }
    }

    private final CSVFormat csvFormat;

    private final WKTReader wktReader;

    public TWKBTestSupport() {
        this.csvFormat = CSVFormat.DEFAULT//
                .withDelimiter('|')//
                .withCommentMarker('#')//
                .withIgnoreHeaderCase(true)//
                .withFirstRecordAsHeader()//
                .withTrim(true);
        this.wktReader = new WKTReader();
        // This disables the reader to parse XZM coordinates by default, creating coordinates with
        // only X/Y ordinates instead
        this.wktReader.setIsOldJtsCoordinateSyntaxAllowed(false);
    }

    public List<TWKBTestData> getPoints() {
        return load("/testdata/twkb/points.csv");
    }

    public List<TWKBTestData> getMultiPoints() {
        return load("/testdata/twkb/multipoints.csv");
    }

    public List<TWKBTestData> getLineStrings() {
        return load("/testdata/twkb/linestrings.csv");
    }

    public List<TWKBTestData> getMultiLineStrings() {
        return load("/testdata/twkb/multilinestrings.csv");
    }

    public List<TWKBTestData> getPolygons() {
        return load("/testdata/twkb/polygons.csv");
    }

    public List<TWKBTestData> getMultiPolygons() {
        return load("/testdata/twkb/multipolygons.csv");
    }

    public List<TWKBTestData> getGeometryCollections() {
        return load("/testdata/twkb/geometrycollections.csv");
    }

    private List<TWKBTestData> load(String resource) {
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            Objects.requireNonNull(in, resource + " does not exist");
            final CSVParser csvParser = CSVParser.parse(in, UTF_8, csvFormat);
            return csvParser.getRecords().stream().map(this::parseRecord)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private TWKBTestData parseRecord(CSVRecord record) {
        // input_wkt|xyprecision|zprecision|mprecision|withsize|withbbox|expected_wkt|expected_twkb
        String input = record.get("input_wkt");
        Geometry inputGeometry = parseWKT(input);
        int xyprecision = Integer.parseInt(record.get("xyprecision"));
        int zprecision = Integer.parseInt(record.get("zprecision"));
        int mprecision = Integer.parseInt(record.get("mprecision"));
        boolean includeSize = Boolean.parseBoolean(record.get("withsize"));
        boolean includeBbox = Boolean.parseBoolean(record.get("withbbox"));
        String expectedTWKBHex = record.get("expected_twkb");
        byte[] expectedTWKB = WKBReader.hexToBytes(expectedTWKBHex);
        Geometry expectedGeometry = parseWKT(record.get("expected_wkt"));

        return new TWKBTestData()//
                .setInputWKT(input)//
                .setInputGeometry(inputGeometry)//
                .setXyprecision(xyprecision)//
                .setZprecision(zprecision)//
                .setMprecision(mprecision)//
                .setIncludeSize(includeSize)//
                .setIncludeBbox(includeBbox)//
                .setExpectedTWKBHex(expectedTWKBHex)//
                .setExpectedTWKB(expectedTWKB)//
                .setExpectedGeometry(expectedGeometry);
    }

    public Geometry parseWKT(String wkt) {
        try {
            return wktReader.read(wkt);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
