
/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.geom;

import java.io.IOException;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;





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
