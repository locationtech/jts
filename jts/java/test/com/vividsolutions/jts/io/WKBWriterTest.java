/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jts.io;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import junit.framework.TestCase;

public class WKBWriterTest extends TestCase {

    public WKBWriterTest(String name) {
        super(name);
    }
    
    public void testSRID() throws Exception {
        GeometryFactory gf = new GeometryFactory();
        Point p1 = gf.createPoint(new Coordinate(1,2));
        p1.setSRID(1234);
        
        //first write out without srid set
        WKBWriter w = new WKBWriter();
        byte[] wkb = w.write(p1);
        
        //check the 3rd bit of the second byte, should be unset
        byte b = (byte) (wkb[1] & 0x20);
        assertEquals(0, b);
        
        //read geometry back in
        WKBReader r = new WKBReader(gf);
        Point p2 = (Point) r.read(wkb);
        
        assertTrue(p1.equalsExact(p2));
        assertEquals(0, p2.getSRID());
        
        //not write out with srid set
        w = new WKBWriter(2, true);
        wkb = w.write(p1);
        
        //check the 3rd bit of the second byte, should be set
        b = (byte) (wkb[1] & 0x20);
        assertEquals(0x20, b);
        
        int srid = ((int) (wkb[5] & 0xff) << 24) | ( (int) (wkb[6] & 0xff) << 16)
            | ( (int) (wkb[7] & 0xff) << 8) | (( int) (wkb[8] & 0xff) );
       
        assertEquals(1234, srid);
        
        r = new WKBReader(gf);
        p2 = (Point) r.read(wkb);
        
        //read the geometry back in
        assertTrue(p1.equalsExact(p2));
        assertEquals(1234, p2.getSRID());
    }
}
