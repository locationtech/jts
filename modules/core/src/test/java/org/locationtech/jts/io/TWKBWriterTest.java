package org.locationtech.jts.io;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.locationtech.jts.geom.CoordinateSequenceComparator;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;

import java.util.Arrays;

public class TWKBWriterTest extends TestCase {
    public static void main(String args[]) {
        TestRunner.run(TWKBWriterTest.class);
    }

//    private GeometryFactory geomFactory = new GeometryFactory();
//    private WKTReader rdr = new WKTReader(geomFactory);

//    GeometryFactory geomFactory = new GeometryFactory(
//            new PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.DOUBLE, 2));
//    WKTReader rdr = new WKTReader(geomFactory);


    public void testPointGeometries() throws ParseException {
       // checkTWKBGeometry("01000204", 2, "POINT(1 2)");
        checkTWKBGeometry("01080302040608", 4,"POINT(1 2 3 4)");

        // Written with precision = 5
        checkTWKBGeometry("a100c09a0c80b518", 2,"POINT(1 2)");
        checkTWKBGeometry("a10080a8d6b90780d0acf30e", 2,"POINT(10000 20000)");

        // With bounding boxes
        checkTWKBGeometry("0101020004000204", 2,"POINT(1 2)");
        checkTWKBGeometry("010903020004000600080002040608", 4, "POINT(1 2 3 4)");
    }

    public void testTWKB() throws ParseException {
        checkTWKBGeometry("a110", "POINT EMPTY");
        checkTWKBGeometry("a100c09a0c80b518", "POINT(1 2)");
        checkTWKBGeometry("a210", "LINESTRING EMPTY");
        checkTWKBGeometry("a20002c09a0c80b51880b51880b518", "LINESTRING(1 2,3 4)");
        checkTWKBGeometry("a310", "POLYGON EMPTY");
        checkTWKBGeometry("a3000104c09a0c80b51880b51880b51880b51880b518ffe930ffe930", "POLYGON((1 2,3 4,5 6,1 2))");
        checkTWKBGeometry("a3000204c09a0c80b51880b51880b51880b51880b518ffe930ffe9300480897a80897a80b51880b51880b51880b518ffe930ffe930", "POLYGON((1 2,3 4,5 6,1 2),(11 12,13 14,15 16,11 12))");
        checkTWKBGeometry("a3000304c09a0c80b51880b51880b51880b51880b518ffe930ffe9300480897a80897a80b51880b51880b51880b518ffe930ffe9300480897a80897a80b51880b51880b51880b518ffe930ffe930", "POLYGON((1 2,3 4,5 6,1 2),(11 12,13 14,15 16,11 12),(21 22,23 24,25 26,21 22))");
        checkTWKBGeometry("a410", "MULTIPOINT EMPTY");
        checkTWKBGeometry("a40001c09a0c80b518", "MULTIPOINT(1 2)");
        checkTWKBGeometry("a40002c09a0c80b51880b51880b518", "MULTIPOINT(1 2,3 4)");
        checkTWKBGeometry("a510", "MULTILINESTRING EMPTY");
        checkTWKBGeometry("a5000102c09a0c80b51880b51880b518", "MULTILINESTRING((1 2,3 4))");
        checkTWKBGeometry("a5000202c09a0c80b51880b51880b5180280b51880b51880b51880b518", "MULTILINESTRING((1 2,3 4),(5 6,7 8))");
        checkTWKBGeometry("a610", "MULTIPOLYGON EMPTY");
        checkTWKBGeometry("a600010104c09a0c80b51880b51880b51880b51880b518ffe930ffe930", "MULTIPOLYGON(((1 2,3 4,5 6,1 2)))");
        checkTWKBGeometry("a600020104c09a0c80b51880b51880b51880b51880b518ffe930ffe9300304000080b51880b51880b51880b518ffe930ffe9300480897a80897a80b51880b51880b51880b518ffe930ffe9300480897a80897a80b51880b51880b51880b518ffe930ffe930", "MULTIPOLYGON(((1 2,3 4,5 6,1 2)),((1 2,3 4,5 6,1 2),(11 12,13 14,15 16,11 12),(21 22,23 24,25 26,21 22)))");
        checkTWKBGeometry("a710", "GEOMETRYCOLLECTION EMPTY");
        checkTWKBGeometry("a70001a100c09a0c80b518", "GEOMETRYCOLLECTION(POINT(1 2))");
        checkTWKBGeometry("a70002a100c09a0c80b518a20002c09a0c80b51880b51880b518", "GEOMETRYCOLLECTION(POINT(1 2),LINESTRING(1 2,3 4))");
        checkTWKBGeometry("a70003a100c09a0c80b518a20002c09a0c80b51880b51880b518a3000304c09a0c80b51880b51880b51880b51880b518ffe930ffe9300480897a80897a80b51880b51880b51880b518ffe930ffe9300480897a80897a80b51880b51880b51880b518ffe930ffe930", "GEOMETRYCOLLECTION(POINT(1 2),LINESTRING(1 2,3 4),POLYGON((1 2,3 4,5 6,1 2),(11 12,13 14,15 16,11 12),(21 22,23 24,25 26,21 22)))");
    }

    private static CoordinateSequenceComparator comp2 = new CoordinateSequenceComparator(2);
    private static CoordinateSequenceComparator comp3 = new CoordinateSequenceComparator(3);
    private static CoordinateSequenceComparator comp4 = new CoordinateSequenceComparator(4);

    private void checkTWKBGeometry(String twkbHex, String expectedWKT) throws ParseException
    {
        checkTWKBGeometry(twkbHex, 2, expectedWKT);
        checkTWKBGeometry(twkbHex, 3, expectedWKT);
    }


    private void checkTWKBGeometry(String twkbHex, int dimension, String expectedWKT) throws ParseException
    {
        CoordinateSequenceFactory csf =
                new PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.DOUBLE ,dimension);
        GeometryFactory geomFactory = new GeometryFactory(csf);
        WKTReader rdr = new WKTReader(geomFactory);

        Geometry g = rdr.read(expectedWKT);

        TWKBWriter twkbWriter = new TWKBWriter();
        byte[] twkb = WKBReader.hexToBytes(twkbHex);
        byte[] written = twkbWriter.write(g);

        TWKBReader reader = new TWKBReader();
        Geometry g2 = reader.read(written);

        boolean isEqualHex = Arrays.equals(twkb, written);


        CoordinateSequenceComparator comp = null;
        switch (dimension) {
            case 2: comp = comp2; break;
            case 3: comp = comp3; break;
            case 4: comp = comp4; break;
            //default: throw new Exception("Never gonna get here!");
        }
        boolean isEqual = (g.compareTo(g2, comp) == 0);

        if (!isEqual || !isEqualHex) {
            System.out.println("isEqual: " + isEqual + " isEqualHex: " + isEqualHex);
            System.out.println("Input:      " + expectedWKT);
            System.out.println("Round-trip: " + g2);
            System.out.println("Expected " + twkbHex);
            System.out.println("Written  " + javax.xml.bind.DatatypeConverter.printHexBinary(written));
        }
        assertTrue(isEqualHex);
        assertTrue(isEqual);
    }

}
