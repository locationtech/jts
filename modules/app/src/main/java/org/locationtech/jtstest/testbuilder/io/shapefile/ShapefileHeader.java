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
 * Header.java
 *
 * Created on February 12, 2002, 3:29 PM
 */

package org.locationtech.jtstest.testbuilder.io.shapefile;

import java.io.IOException;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryCollection;


/**
 *
 * @author  jamesm
 */
public class ShapefileHeader{
    private final static boolean DEBUG=false;
    private int fileCode = -1;
    public int fileLength = -1;
    private int indexLength = -1;
    private int version = -1;
    private int shapeType = -1;
    //private double[] bounds = new double[4];
    private Envelope bounds;
    
    public ShapefileHeader(EndianDataInputStream file) throws IOException {
      //  file.setLittleEndianMode(false);
        fileCode = file.readIntBE();
       // if(DEBUG)System.out.println("Sfh->Filecode "+fileCode);
        if ( fileCode != Shapefile.SHAPEFILE_ID )
            System.err.println("Sfh->WARNING filecode "+fileCode+" not a match for documented shapefile code "+Shapefile.SHAPEFILE_ID);
        
        for(int i=0;i<5;i++){
            int tmp = file.readIntBE();
           // if(DEBUG)System.out.println("Sfh->blank "+tmp);
        }
        fileLength = file.readIntBE();
        
      //  file.setLittleEndianMode(true);
        version=file.readIntLE();
        shapeType=file.readIntLE();
       
        //read in and for now ignore the bounding box
        for(int i = 0;i<4;i++){
            file.readDoubleLE();
        }
        
        //skip remaining unused bytes
       // file.setLittleEndianMode(false);//well they may not be unused forever...
        file.skipBytes(32);
    }
    
    public ShapefileHeader(GeometryCollection geometries,int dims) throws Exception
    {
        ShapeHandler handle;
        if (geometries.getNumGeometries() == 0)
        {
            handle = new PointHandler(); //default
        }
        else
        {
               handle = Shapefile.getShapeHandler(geometries.getGeometryN(0),dims);
        }
        int numShapes = geometries.getNumGeometries();
        shapeType = handle.getShapeType();
        version = Shapefile.VERSION;
        fileCode = Shapefile.SHAPEFILE_ID;
        bounds = geometries.getEnvelopeInternal();
        fileLength = 0;
        for(int i=0;i<numShapes;i++){
            fileLength+=handle.getLength(geometries.getGeometryN(i));
            fileLength+=4;//for each header
        }
        fileLength+=50;//space used by this, the main header
        indexLength = 50+(4*numShapes);
    }
    
    public void setFileLength(int fileLength){
        this.fileLength = fileLength;
    }
        
    public int getShapeType(){
        return shapeType;
    }
    
    public int getVersion(){
        return version;
    }
    
    public Envelope getBounds(){
        return bounds;
    }
    
    public String toString()  {
        String res = new String("Sf-->type "+fileCode+" size "+fileLength+" version "+ version + " Shape Type "+shapeType);
        return res;
    }
}
