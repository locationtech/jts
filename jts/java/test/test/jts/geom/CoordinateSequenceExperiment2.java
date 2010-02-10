
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
package test.jts.geom;

import java.io.IOException;


import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;



/**
 * @version 1.7
 */
public class CoordinateSequenceExperiment2 {
    GeometryFactory fact = new GeometryFactory(new PrecisionModel(), 0);

    public static void main(String[] args) throws Exception {
        CoordinateSequenceExperiment2 test = new CoordinateSequenceExperiment2();
//        System.out.println("Press Enter to begin");
//        System.in.read();        
        test.run();
        System.exit(0);
    }

    public void run() throws IOException {
        int factor = 1;

        for (int i = 1; i < 15; i++) {
            int n = factor * 1000;

            if (n > 64000) { break; }
            factor *= 2;
            run2(n);
        }
    }

    public void run(int nPts) {
        double size = 100.0;
        double armLen = 50.0;
        int nArms = 10;
        long startTime = System.currentTimeMillis();        
        Polygon poly = GeometryTestFactory.createSineStar(fact, 0.0, 0.0, size,
                armLen, nArms, nPts);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        String totalTimeStr = totalTime < 10000 ? totalTime + " ms"
                                                : totalTime / 1000.0 + " s";
        System.out.println("Sine Star Creation Executed in " + totalTimeStr);        

        Polygon box = GeometryTestFactory.createBox(fact, 0, 0, 1, 100.0);

        startTime = System.currentTimeMillis();
        poly.intersects(box);

        endTime = System.currentTimeMillis();
        totalTime = endTime - startTime;
        totalTimeStr = totalTime < 10000 ? totalTime + " ms"
                                                : totalTime / 1000.0 + " s";
        System.out.println("n Pts: " + nPts + "   Executed in " + totalTimeStr);
    }

    public void run2(int nPts) throws IOException {
        double size = 100.0;
        double armLen = 50.0;
        int nArms = 10;
        long startTime = System.currentTimeMillis();
        Polygon poly = GeometryTestFactory.createSineStar(fact, 0.0, 0.0, size,
                armLen, nArms, nPts);
        Polygon box = GeometryTestFactory.createSineStar(fact, 0.0, size / 2,
                size, armLen, nArms, nPts);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        String totalTimeStr = totalTime < 10000 ? totalTime + " ms"
                                                : totalTime / 1000.0 + " s";
        System.out.println("Sine Star Creation Executed in " + totalTimeStr);        
                

        //RobustDeterminant.callCount = 0;
        System.out.println("n Pts: " + nPts);

        startTime = System.currentTimeMillis();
        poly.intersects(box);

        //poly.intersection(box);
        endTime = System.currentTimeMillis();
        totalTime = endTime - startTime;
        totalTimeStr = totalTime < 10000 ? totalTime + " ms"
                                                : (double) totalTime / 1000.0 +
            " s";

        //System.out.println("   signOfDet2x2 calls: " + RobustDeterminant.callCount);
        System.out.println("   Executed in " + totalTimeStr);
    }
}
