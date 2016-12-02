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

package org.locationtech.jtstest;

import junit.framework.TestCase;
import org.locationtech.jts.util.Assert;
import org.locationtech.jtstest.testrunner.SimpleReportWriter;
import org.locationtech.jtstest.testrunner.TestEngine;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class CoreGeometryXMLTest extends TestCase {
    public CoreGeometryXMLTest(String name) {
        super(name);
    }

//    public void testExternal() {
//        testFiles("../core/src/test/resources/testxml/external");
//    }

//    public void testFailure() {
//        testFiles("../core/src/test/resources/testxml/failure");
//    }

    public void testGeneral() {
        testFiles("src/test/resources/testxml/general");
    }

//    public void testRobust() {
//        testFiles("../core/src/test/resources/testxml/robust");
//    }

//    public void testStmlf() {
//        testFiles("../core/src/test/resources/testxml/stmlf");
//    }

    public void testValidate() {
        testFiles("src/test/resources/testxml/validate");
    }

    private void testFiles(String directoryName) {
        TestEngine engine = new TestEngine();
        List testFiles = filenames(new File(directoryName));

        engine.setTestFiles(testFiles);
        engine.run();
        SimpleReportWriter reportWriter = new SimpleReportWriter(false);
        reportWriter.writeReport(engine);
        System.out.println(reportWriter.writeReport(engine));
        
        boolean failures = engine.getParseExceptionCount() + engine.getFailedCount() + engine.getExceptionCount() > 0;
        assertEquals(failures, false);
    }

    private static List<File> filenames(File directory) {
        Assert.isTrue(directory.isDirectory());
        File[] files = directory.listFiles();

        return Arrays.asList(files);
    }
}
