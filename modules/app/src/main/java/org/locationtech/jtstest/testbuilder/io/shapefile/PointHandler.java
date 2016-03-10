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

import org.locationtech.jts.geom.*;


/**
 * Wrapper for a Shapefile point.
 */
public class PointHandler implements ShapeHandler{
    
    int Ncoords=2; //2 = x,y ;  3= x,y,m ; 4 = x,y,z,m
    int myShapeType = -1;
    
    public PointHandler(int type) throws InvalidShapefileException
    {
        if ( (type != 1) && (type != 11) && (type != 21))// 2d, 2d+m, 3d+m
            throw new InvalidShapefileException("PointHandler constructor: expected a type of 1, 11 or 21");
        myShapeType = type;
    }
    
    public PointHandler()
    {
        myShapeType = 1; //2d
    }
    
    public Geometry read(EndianDataInputStream file,GeometryFactory geometryFactory,int contentLength) throws IOException,InvalidShapefileException
    {
      //  file.setLittleEndianMode(true);
	int actualReadWords = 0; //actual number of words read (word = 16bits)
	
        int shapeType = file.readIntLE();
		actualReadWords += 2;
       
        if (shapeType != myShapeType)
            throw new InvalidShapefileException("pointhandler.read() - handler's shapetype doesnt match file's");
        
        double x = file.readDoubleLE();
        double y = file.readDoubleLE();
        double m , z = Double.NaN;
		actualReadWords += 8;
        
        if ( shapeType ==11 )
        {
            z= file.readDoubleLE();
			actualReadWords += 4;
        }
        if ( shapeType >=11 )
        {
            m = file.readDoubleLE();
			actualReadWords += 4;
        }
        
	//verify that we have read everything we need
	while (actualReadWords < contentLength)
	{
		  int junk2 = file.readShortBE();	
		  actualReadWords += 1;
	}
        
        return geometryFactory.createPoint(new Coordinate(x,y,z));
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
        if (myShapeType == Shapefile.POINT)
            return 10;
        if (myShapeType == Shapefile.POINTM)
            return 14;
        
        return 18;
    }
}
