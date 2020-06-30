/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.perf.operation.overlayng;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBHexFileReader;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.operation.overlayng.OverlayNG;

/**
 * A utility that reads a WKB file of geometries, 
 * and outputs an XML file of test cases
 * where pairs of polygons fail for OverlayNG union with a floating noder.
 * 
 * Usage:  OverlayCaseDumper infile [ outfile ]
 * 
 * @author mdavis
 *
 */
public class OverlayCaseDumper {
  
  private static final int MAX_CASES = 100;
  
  private static final int MAX_POINTS = 100;

  public static void main(String args[]) {
    OverlayCaseDumper opd = new OverlayCaseDumper();
    
    opd.parseArgs(args);
    try {
      opd.run();
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
  }

  private static GeometryFactory geomFact = new GeometryFactory();

  private String inFilename = null;
  private String outputFilename = null;
  
  
  private PrintStream outStream = System.out;
  
  private int caseCount;


  private Geometry prevGeom0;
  private Geometry prevGeom1;
  
  private void parseArgs(String[] args) {
    if (args.length < 1)
      throw new IllegalArgumentException("Input filename is required");
    inFilename = args[0];
    
    if (args.length >= 2) {
      outputFilename = args[1];
    }
      
  }
  
  private void run() throws ParseException, IOException {
    if (outputFilename  != null) {
      outStream = new PrintStream(new File(outputFilename));
    }
    
    List<Geometry> geomsIn = readWKBFile(inFilename);
    List<Geometry> geoms = flatten(geomsIn);
    System.out.println("Number of geoms read: " + geoms.size());
    
    List<Geometry> geomsFilt = filter(geoms);

    logHeader();
    doIntersections(geomsFilt);
    logFooter();
    
    System.out.println("Number of geoms filtered: " + geomsFilt.size());
    System.out.println("Number of cases output: " + caseCount);
    
    outStream.flush();
    outStream.close();
  }


  private List<Geometry> flatten(List<Geometry> geoms) {
    List<Geometry> flat = new ArrayList<Geometry>();
    for (Geometry geom : geoms) {
      if (geom.getNumGeometries() == 1) {
        flat.add(geom);
      }
      else {
        PolygonExtracter.getPolygons(geom, flat);
      }
    }
    return flat;
  }

  private void doIntersections(List<Geometry> geoms) {
    caseCount = 0;
    for (int i = 0; i < geoms.size(); i++) {
      Geometry geom = geoms.get(i);
      for (int j = i + 1; j < geoms.size(); j++) {
        Geometry geom1 = geoms.get(j);

        if (! geom.getEnvelopeInternal().intersects(geom1.getEnvelopeInternal()))
          continue;
        
        // skip duplicates to avoid repetition
        if (prevGeom0 != null && prevGeom0.equalsExact(geom))
          continue;
        if (prevGeom1 != null && prevGeom1.equalsExact(geom1)) 
          continue;
        
        prevGeom0 = geom;
        prevGeom1 = geom1;
        
        boolean isDumped = doIntersection(geom, geom1);
        if (isDumped)
          caseCount++;
        if (caseCount >= MAX_CASES) 
          return;
      }
    }
  }

  private boolean doIntersection(Geometry geom, Geometry geom1) {
    try {
      Geometry result = OverlayNG.overlay(geom, geom1, OverlayNG.INTERSECTION);
      return false;
    }
    catch (TopologyException ex) {
      log(geom, geom1);
      return true;
    }
  }


  private void logHeader() {
    outStream.println("<run>\n");
  }

  private void logFooter() {
    outStream.println("\n</run>");
  }
  
  private void log(Geometry geom0, Geometry geom1) {
    outStream.println("<case>\n<a>");
    outStream.println(geom0);
    outStream.println("</a>\n<b>");
    outStream.println(geom1);
    outStream.println("</b>\n" + 
        "<test><op name='union' arg1='A' arg2='B' >  </op></test>\n" + 
        "</case>\n" + 
        "\n");
  }

  private List<Geometry> filter(List<Geometry> geoms) {
    List<Geometry> filt = new ArrayList<Geometry>();
    for (Geometry geom : geoms) {
      if (geom.getNumPoints() > MAX_POINTS) continue;
      filt.add(geom);
    }
    return filt;
  }
  
  private static List<Geometry> readWKBFile(String filename) throws ParseException, IOException {
    File file = new File(filename);
    FileReader fileReader = new FileReader(file);
    WKBReader wkbrdr = new WKBReader(geomFact);
    WKBHexFileReader wkbhexReader = new WKBHexFileReader(fileReader, wkbrdr);
    return wkbhexReader.read();
  }


}
