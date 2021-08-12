/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
/*
 * Copyright (c) 2003 Open Source Geospatial Foundation, All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms
 * of the OSGeo BSD License v1.0 available at:
 *
 * https://www.osgeo.org/sites/osgeo.org/files/Page/osgeo-bsd-license.txt
 */
package org.locationtech.jtstest.testbuilder.io.shapefile;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.PointLocation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;


/**
 * Wrapper for a Shapefile polygon.
 */
public class PolygonHandler implements ShapeHandler{
    int myShapeType;
    private PrecisionModel precisionModel = new PrecisionModel();
    private GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
    
    public PolygonHandler()
    {
        myShapeType = 5;
    }
    
      public PolygonHandler(int type) throws InvalidShapefileException
        {
            if  ( (type != 5) &&  (type != 15) &&  (type != 25) )
                throw new InvalidShapefileException("PolygonHandler constructor - expected type to be 5, 15, or 25.");
            
            myShapeType = type;
    }
    
    //returns true if testPoint is a point in the pointList list.
    boolean pointInList(Coordinate testPoint, Coordinate[] pointList)
    {
        int t, numpoints;
        Coordinate  p;
        
        numpoints = Array.getLength( pointList) ;
        for (t=0;t<numpoints; t++)
        {
            p = pointList[t];
            if ( (testPoint.x == p.x) && (testPoint.y == p.y) &&
                    ((testPoint.getZ() == p.getZ()) || (!(testPoint.getZ() == testPoint.getZ()))  )  //nan test; x!=x if x is nan
                    )
            {
                return true;
            }
        }
        return false;
    }
    
    public Geometry read( EndianDataInputStream file , GeometryFactory geometryFactory, int contentLength)
    throws IOException, InvalidShapefileException
    {
    
    	int actualReadWords = 0; //actual number of words read (word = 16bits)
        
       // file.setLittleEndianMode(true);
        int shapeType = file.readIntLE();	
		actualReadWords += 2;
        
         if (shapeType ==0)
        {
            return geometryFactory.createMultiPolygon(null); //null shape
        }
        
        if ( shapeType != myShapeType ) {
            throw new InvalidShapefileException
            ("PolygonHandler.read() - got shape type "+shapeType+" but was expecting "+myShapeType);
        }
        
        //bounds
        file.readDoubleLE();
        file.readDoubleLE();
        file.readDoubleLE();
        file.readDoubleLE();
        
		actualReadWords += 4*4;
 
        
        int partOffsets[];
        
        int numParts = file.readIntLE();
        int numPoints = file.readIntLE();       
		actualReadWords += 4;
        
        partOffsets = new int[numParts];
        
        for(int i = 0;i<numParts;i++){
            partOffsets[i]=file.readIntLE();
			actualReadWords += 2;
        }
        
        //LinearRing[] rings = new LinearRing[numParts];
        ArrayList<LinearRing> shells = new ArrayList<LinearRing>();
        ArrayList<LinearRing> holes = new ArrayList<LinearRing>();
        Coordinate[] coords = new Coordinate[numPoints];
        
        for(int t=0;t<numPoints;t++)
        {
            coords[t]= new Coordinate(file.readDoubleLE(),file.readDoubleLE());
			actualReadWords += 8;
        }
        
        if (myShapeType == 15)
        {
                //z
            file.readDoubleLE();  //zmin
            file.readDoubleLE();  //zmax
			actualReadWords += 8;
             for(int t=0;t<numPoints;t++)
            {
                coords[t].setZ(file.readDoubleLE());
				actualReadWords += 4;
            }
        }
      
        if (myShapeType >= 15)
        {
          //  int fullLength = 22 + (2*numParts) + (8*numPoints) + 8 + (4*numPoints)+ 8 + (4*numPoints);
          int fullLength;
          if (myShapeType == 15)
          {
          		//polyZ (with M)
			    fullLength = 22 + (2*numParts) + (8*numPoints) + 8 + (4*numPoints)+ 8 + (4*numPoints);         	
          }
          else
          {
          		//polyM (with M)
				fullLength = 22 + (2*numParts) + (8*numPoints) + 8+ (4*numPoints) ;
          }
            if (contentLength >= fullLength)
            {
                    file.readDoubleLE();  //mmin
                    file.readDoubleLE();  //mmax
					actualReadWords += 8;
                    for(int t=0;t<numPoints;t++)
                    {
                         file.readDoubleLE();
					     actualReadWords += 4;
                    }
            }            
        }
        
        
	//verify that we have read everything we need
	while (actualReadWords < contentLength)
	{
		  int junk = file.readShortBE();	
		 actualReadWords += 1;
	}
	
        
        int offset = 0;
        int start,finish,length;
        for(int part=0;part<numParts;part++){
            start = partOffsets[part];
            if(part == numParts-1){finish = numPoints;}
            else {
                finish=partOffsets[part+1];
            }
            length = finish-start;
            Coordinate points[] = new Coordinate[length];
            for(int i=0;i<length;i++){
                points[i]=coords[offset];
                offset++;
            }
            LinearRing ring = geometryFactory.createLinearRing(points);
            /**
             * Allow reading a 3-point ring, and treat it as a shell.
             */
            if(points.length >= 4 && Orientation.isCCW(points)){
                holes.add(ring);
            }
            else{
                shells.add(ring);
            }
        }
        
        ArrayList<ArrayList<LinearRing>> holesForShells = assignHolesToShells(shells, holes);

        Polygon[] polygons = new Polygon[shells.size()];
        for (int i = 0; i < shells.size(); i++) {
          polygons[i] = geometryFactory.createPolygon((LinearRing) shells.get(i),
              (LinearRing[]) ((ArrayList<LinearRing>) holesForShells.get(i))
                  .toArray(new LinearRing[0]));
        }

        if (polygons.length == 1) {
          return polygons[0];
        }

        holesForShells = null;
        shells = null;
        holes = null;
        // its a multi part

        Geometry result = geometryFactory.createMultiPolygon(polygons);
        // if (!(result.isValid() ))
        // System.out.println("geom isn't valid");
        return result;
      }

      private ArrayList<ArrayList<LinearRing>> assignHolesToShells(ArrayList<LinearRing> shells, ArrayList<LinearRing> holes)
      {
        // now we have a list of all shells and all holes
        ArrayList<ArrayList<LinearRing>> holesForShells = new ArrayList<ArrayList<LinearRing>>(shells.size());
        for (int i = 0; i < shells.size(); i++) {
          holesForShells.add(new ArrayList<LinearRing>());
        }

        // find homes
        for (int i = 0; i < holes.size(); i++) {
          LinearRing testHole = (LinearRing) holes.get(i);
          LinearRing minShell = null;
          Envelope minEnv = null;
          Envelope testHoleEnv = testHole.getEnvelopeInternal();
          Coordinate testHolePt = testHole.getCoordinateN(0);
          LinearRing tryShell;
          int nShells = shells.size();
          for (int j = 0; j < nShells; j++) {
            tryShell = (LinearRing) shells.get(j);
            Envelope tryShellEnv = tryShell.getEnvelopeInternal();
            if (! tryShellEnv.contains(testHoleEnv)) continue;
            
            boolean isContained = false;
            Coordinate[] coordList = tryShell.getCoordinates();

            if (nShells <= 1 
                || PointLocation.isInRing(testHolePt, coordList) 
                || pointInList(testHolePt, coordList))
              isContained = true;
            
            // check if new containing ring is smaller than the current minimum ring
            if (minShell != null)
              minEnv = minShell.getEnvelopeInternal();
            if (isContained) {
              if (minShell == null || minEnv.contains(tryShellEnv)) {
                minShell = tryShell;
              }
            }
          }

          if (minShell == null) {
            System.err.println("Found polygon with a hole not inside a shell");
          }
          else {
            // ((ArrayList)holesForShells.get(shells.indexOf(minShell))).add(testRing);
            ((ArrayList<LinearRing>) holesForShells.get(findIndex(shells, minShell)))
                .add(testHole);
          }
        }
        return holesForShells;
      }

      /**
       * Finds a object in a list. Should be much faster than indexof
       * 
       * @param list
       * @param o
       * @return
       */
      private static int findIndex(ArrayList list, Object o)
      {
        int n = list.size();
        for (int i = 0; i < n; i++) {
          if (list.get(i) == o)
            return i;
        }
        return -1;
      }
    
    
    public int getShapeType(){
        return myShapeType;
    }
    public int getLength(Geometry geometry){
        
         int nrings=0;
        
        for (int t=0;t<geometry.getNumGeometries();t++)
        {
            Polygon p;
            p = (Polygon) geometry.getGeometryN(t);
            nrings = nrings + 1 + p.getNumInteriorRing();
        }
         
         int npoints = geometry.getNumPoints();
         
         if (myShapeType == 15)
         {
             return 22+(2*nrings)+8*npoints + 4*npoints+8 +4*npoints+8;
         }
         if (myShapeType==25)
         {
             return 22+(2*nrings)+8*npoints + 4*npoints+8 ;
         }
         
         
         return 22+(2*nrings)+8*npoints;
    }
    
    
      double[] zMinMax(Geometry g)
    {
        double zmin,zmax;
        boolean validZFound = false;
        Coordinate[] cs = g.getCoordinates();
        double[] result = new double[2];
        
        zmin = Double.NaN;
        zmax = Double.NaN;
        double z;
        
        for (int t=0;t<cs.length; t++)
        {
            z= cs[t].getZ() ;
            if (!(Double.isNaN( z ) ))
            {
                if (validZFound)
                {
                    if (z < zmin)
                        zmin = z;
                    if (z > zmax)
                        zmax = z;
                }
                else
                {
                    validZFound = true;
                    zmin =  z ;
                    zmax =  z ;
                }
            }
           
        }
        
        result[0] = (zmin);
        result[1] = (zmax);
        return result;
        
    }
    
    
}

/*
 * $Log: PolygonHandler.java,v $
 * Revision 1.1  2009/10/14 04:21:22  mbdavis
 * added drag-n-drop for reading shp files
 *
 * Revision 1.5  2003/09/23 17:15:26  dblasby
 * *** empty log message ***
 *
 * Revision 1.4  2003/07/25 18:49:15  dblasby
 * Allow "extra" data after the content.  Fixes the ICI shapefile bug.
 *
 * Revision 1.3  2003/02/04 02:10:37  jaquino
 * Feature: EditWMSQuery dialog
 *
 * Revision 1.2  2003/01/22 18:31:05  jaquino
 * Enh: Make About Box configurable
 *
 * Revision 1.2  2002/09/09 20:46:22  dblasby
 * Removed LEDatastream refs and replaced with EndianData[in/out]putstream
 *
 * Revision 1.1  2002/08/27 21:04:58  dblasby
 * original
 *
 * Revision 1.3  2002/03/05 10:51:01  andyt
 * removed use of factory from write method
 *
 * Revision 1.2  2002/03/05 10:23:59  jmacgill
 * made sure geometries were created using the factory methods
 *
 * Revision 1.1  2002/02/28 00:38:50  jmacgill
 * Renamed files to more intuitve names
 *
 * Revision 1.4  2002/02/13 00:23:53  jmacgill
 * First semi working JTS version of Shapefile code
 *
 * Revision 1.3  2002/02/11 18:44:22  jmacgill
 * replaced geometry constructions with calls to geometryFactory.createX methods
 *
 * Revision 1.2  2002/02/11 18:28:41  jmacgill
 * rewrote to have static read and write methods
 *
 * Revision 1.1  2002/02/11 16:54:43  jmacgill
 * added shapefile code and directories
 *
 */
