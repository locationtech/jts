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

package org.locationtech.jtstest.testbuilder.io.shapefile;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.algorithm.RobustCGAlgorithms;
import org.locationtech.jts.geom.*;




/**
 * Wrapper for a Shapefile polygon.
 */
public class PolygonHandler implements ShapeHandler{
    protected static CGAlgorithms cga = new RobustCGAlgorithms();
    int myShapeType;
    
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
                    ((testPoint.z == p.z) || (!(testPoint.z == testPoint.z))  )  //nan test; x!=x iff x is nan
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
             return new MultiPolygon(null,new PrecisionModel(),0); //null shape
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
        ArrayList shells = new ArrayList();
        ArrayList holes = new ArrayList();
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
                coords[t].z = file.readDoubleLE();
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
            if(cga.isCCW(points)){
                holes.add(ring);
            }
            else{
                shells.add(ring);
            }
        }
        
        ArrayList holesForShells = assignHolesToShells(shells, holes);

        Polygon[] polygons = new Polygon[shells.size()];
        for (int i = 0; i < shells.size(); i++) {
          polygons[i] = geometryFactory.createPolygon((LinearRing) shells.get(i),
              (LinearRing[]) ((ArrayList) holesForShells.get(i))
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
        // System.out.println("geom isnt valid");
        return result;
      }

      private ArrayList assignHolesToShells(ArrayList shells, ArrayList holes)
      {
        // now we have a list of all shells and all holes
        ArrayList holesForShells = new ArrayList(shells.size());
        for (int i = 0; i < shells.size(); i++) {
          holesForShells.add(new ArrayList());
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
                || CGAlgorithms.isPointInRing(testHolePt, coordList) 
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
            ((ArrayList) holesForShells.get(findIndex(shells, minShell)))
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
        
           MultiPolygon multi;
        if(geometry instanceof MultiPolygon){
            multi = (MultiPolygon)geometry;
        }
        else{
            multi = new MultiPolygon(new Polygon[]{(Polygon)geometry},geometry.getPrecisionModel(),geometry.getSRID());
        }
        
         int nrings=0;
        
        for (int t=0;t<multi.getNumGeometries();t++)
        {
            Polygon p;
            p = (Polygon) multi.getGeometryN(t);
            nrings = nrings + 1 + p.getNumInteriorRing();
        }
         
         int npoints = multi.getNumPoints();
         
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
            z= cs[t].z ;
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
 * orginal
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
