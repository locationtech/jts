/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2016 Vivid Solutions
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 *
 */

package test.jts;

import java.io.File;

public class TestFiles {

    public static final String getResourceFilePath(String fileName) {
        ClassLoader classLoader = TestFiles.class.getClassLoader();
        return new File(classLoader.getResource("testdata/" + fileName).getFile()).getAbsolutePath();
    }
}
