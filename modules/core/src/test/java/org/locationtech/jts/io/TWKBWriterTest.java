package org.locationtech.jts.io;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.locationtech.jts.geom.CoordinateSequenceComparator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.Arrays;

public class TWKBWriterTest extends TestCase {
    public static void main(String args[]) {
        TestRunner.run(TWKBWriterTest.class);
    }

    private GeometryFactory geomFactory = new GeometryFactory();
    private WKTReader rdr = new WKTReader(geomFactory);

    public void testPointGeometries() throws ParseException {

        checkTWKBGeometry("01000204", "POINT(1 2)");
        checkTWKBGeometry("01080302040608", "POINT(1 2 3 4)");


        // Written with precision = 5
        checkTWKBGeometry("a100c09a0c80b518", "POINT(1 2)");
        checkTWKBGeometry("a10080a8d6b90780d0acf30e", "POINT(10000 20000)");

        // With bounding boxes
        checkTWKBGeometry("0101020004000204", "POINT(1 2)");
        checkTWKBGeometry("010903020004000600080002040608", "POINT(1 2 3 4)");
    }

    private static CoordinateSequenceComparator comp2 = new CoordinateSequenceComparator(2);

    private void checkTWKBGeometry(String twkbHex, String expectedWKT) throws ParseException
    {
        Geometry geometry = rdr.read(expectedWKT);

        TWKBWriter twkbWriter = new TWKBWriter();
        byte[] twkb = WKBReader.hexToBytes(twkbHex);
        byte[] written = twkbWriter.write(geometry);


        boolean isEqual = Arrays.equals(twkb, written);


        if (!isEqual) {
            System.out.println("Expected " + twkbHex);
            System.out.println("Written  " + javax.xml.bind.DatatypeConverter.printHexBinary(written));
        }
        assertTrue(isEqual);
    }

}
