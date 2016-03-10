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

/*
 * MultiPointHandler.java
 *
 * Created on July 17, 2002, 4:13 PM
 */

package org.locationtech.jtstest.testbuilder.io.shapefile;

import java.io.IOException;

import org.locationtech.jts.geom.*;


/**
 *
 * @author  dblasby
 */
public class MultiPointHandler  implements ShapeHandler  {
    int myShapeType= -1;
    
    /** Creates new MultiPointHandler */
    public MultiPointHandler() {
        myShapeType = 8;
    }
    
        public MultiPointHandler(int type) throws InvalidShapefileException
        {
            if  ( (type != 8) &&  (type != 18) &&  (type != 28) )
                throw new InvalidShapefileException("Multipointhandler constructor - expected type to be 8, 18, or 28");
            
        myShapeType = type;
    }
    
    public Geometry read(EndianDataInputStream file,GeometryFactory geometryFactory,int contentLength) throws IOException,InvalidShapefileException{
        //file.setLittleEndianMode(true);
	
		int actualReadWords = 0; //actual number of words read (word = 16bits)
	
        int shapeType = file.readIntLE();  
		actualReadWords += 2;
        
        if (shapeType ==0)
            return  new MultiPoint(null,new PrecisionModel(),0);
        if (shapeType != myShapeType)
        {
            throw new InvalidShapefileException("Multipointhandler.read() - expected type code "+myShapeType+" but got "+shapeType);
        }
        //read bbox
        file.readDoubleLE();
        file.readDoubleLE();
        file.readDoubleLE();
        file.readDoubleLE();
        
		actualReadWords += 4*4;
        
        int numpoints = file.readIntLE(); 
		actualReadWords += 2;
	 
        Coordinate[] coords = new Coordinate[numpoints];
        for (int t=0;t<numpoints;t++)
        {    
          
                double x = file.readDoubleLE();
                double y = file.readDoubleLE();
				actualReadWords += 8;
                coords[t] = new Coordinate(x,y);
        }
        if (myShapeType == 18)
        {
            file.readDoubleLE(); //z min/max
            file.readDoubleLE();
			actualReadWords += 8;
            for (int t=0;t<numpoints;t++)
            { 
                       double z =  file.readDoubleLE();//z
						actualReadWords += 4;
                       coords[t].z = z;
            }
        }
        
        
        if (myShapeType >= 18)
        {
           // int fullLength = numpoints * 8 + 20 +8 +4*numpoints + 8 +4*numpoints;
			int fullLength;
			if (myShapeType == 18)
			{
				//multipoint Z (with m)
				fullLength = 20 + (numpoints * 8)  +8 +4*numpoints + 8 +4*numpoints;
			}
			else
			{
				//multipoint M (with M)
				fullLength = 20 + (numpoints * 8)  +8 +4*numpoints;
			}
			
            if (contentLength >= fullLength)  //is the M portion actually there?
            {
                file.readDoubleLE(); //m min/max
                file.readDoubleLE();
				actualReadWords += 8;
                for (int t=0;t<numpoints;t++)
                { 
                            file.readDoubleLE();//m
							actualReadWords += 4;
                }
            }
        }
        
	//verify that we have read everything we need
	while (actualReadWords < contentLength)
	{
		  int junk2 = file.readShortBE();	
		  actualReadWords += 1;
	}
	
        return geometryFactory.createMultiPoint(coords);
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
    
    
    
    /**
     * Returns the shapefile shape type value for a point
     * @return int Shapefile.POINT
     */
    public  int getShapeType(){
        return myShapeType;
    }
    
    /**
     * Calcuates the record length of this object.
     * @return int The length of the record that this shapepoint will take up in a shapefile
     **/
    public int getLength(Geometry geometry){
        MultiPoint mp = (MultiPoint) geometry;
    
        if (myShapeType == 8)
            return mp.getNumGeometries() * 8 + 20;
        if (myShapeType == 28)
            return mp.getNumGeometries() * 8 + 20 +8 +4*mp.getNumGeometries();
        
        return mp.getNumGeometries() * 8 + 20 +8 +4*mp.getNumGeometries() + 8 +4*mp.getNumGeometries() ;
    }
}
