package org.locationtech.jtstest.testbuilder.io.shapefile;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.locationtech.jts.geom.*;




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
public class Shapefile  
{


    static final int    SHAPEFILE_ID = 9994;
    static final int    VERSION = 1000;

    public static final int    NULL = 0;
    public static final int    POINT = 1;
      public static final int    POINTZ = 11;
        public static final int    POINTM = 21;
    public static final int    ARC = 3;
    public static final int    ARCM = 23;
    public static final int    ARCZ = 13;
    public static final int    POLYGON = 5;
    public static final int    POLYGONM = 25;
    public static final int    POLYGONZ = 15;
    public static final int    MULTIPOINT = 8;
    public static final int    MULTIPOINTM = 28;
    public static final int    MULTIPOINTZ = 18;
    public static final int    UNDEFINED = -1;
    //Types 2,4,6,7 and 9 were undefined at time or writeing

    public static final int XY = 2;
    public static final int XYM = 3;
    public static final int XYZM = 4;
    
    private URL baseURL;
    private InputStream myInputStream;
    private GeometryFactory geomFactory;

    /**
     * Creates and initialises a shapefile from a url
     * @param url The url of the shapefile
     */
    public Shapefile(URL url){
        baseURL=url;
        myInputStream= null;
        try {
         URLConnection uc = baseURL.openConnection();
         myInputStream = new BufferedInputStream(uc.getInputStream());
        }
        catch (Exception e)
        {
        }
    }

      public Shapefile(InputStream IS){
       myInputStream = IS;
    }


    private EndianDataInputStream getInputStream() throws IOException
    {
        if (myInputStream == null)
        {
            throw new IOException("Could not make a connection to the URL: " + baseURL);
        }
        EndianDataInputStream sfile = new EndianDataInputStream(myInputStream);
        return sfile;
    }


    /**
     * Initialises a shapefile from disk.
     * Use Shapefile(String) if you don't want to use LEDataInputStream directly (recomened)
     */
    public GeometryCollection read(GeometryFactory geometryFactory) throws IOException,ShapefileException,Exception{
       EndianDataInputStream file = getInputStream();
        if(file==null) throw new IOException("Failed connection or no content for "+baseURL);
        ShapefileHeader mainHeader = new ShapefileHeader(file);
        if(mainHeader.getVersion() < VERSION){System.err.println("Sf-->Warning, Shapefile format ("+mainHeader.getVersion()+") older that supported ("+VERSION+"), attempting to read anyway");}
        if(mainHeader.getVersion() > VERSION){System.err.println("Sf-->Warning, Shapefile format ("+mainHeader.getVersion()+") newer that supported ("+VERSION+"), attempting to read anyway");}

        Geometry body;
        ArrayList list = new ArrayList();
        int type=mainHeader.getShapeType();
        ShapeHandler handler = getShapeHandler(type);
        if(handler==null)throw new ShapeTypeNotSupportedException("Unsuported shape type:"+type);

        int recordNumber=0;
        int contentLength=0;
        try{
            while(true){
               // file.setLittleEndianMode(false);
                recordNumber=file.readIntBE();
                contentLength=file.readIntBE();
                try{
                    body = handler.read(file,geometryFactory,contentLength);
                    list.add(body);
                   // System.out.println("Done record: " + recordNumber);
                }catch(IllegalArgumentException r2d2){
                    //System.out.println("Record " +recordNumber+ " has is NULL Shape");
                    list.add(new GeometryCollection(null,null,-1));
                }catch(Exception c3p0){
                    System.out.println("Error processing record (a):" +recordNumber);
                    System.out.println(c3p0.getMessage());
                    c3p0.printStackTrace();
                    list.add(new GeometryCollection(null,null,-1));
                }
               // System.out.println("processing:" +recordNumber);
            }
        }catch(EOFException e){

        }
        return geometryFactory.createGeometryCollection((Geometry[])list.toArray(new Geometry[]{}));
    }

    private EndianDataInputStream file;
    private ShapefileHeader mainHeader;
    private ShapeHandler handler;
    private int recordNumber;
    
    /**
     * Initialises a shapefile from disk.
     * Use Shapefile(String) if you don't want to use LEDataInputStream directly (recomened)
     */
    public void readStream(GeometryFactory geometryFactory)
        throws IOException,ShapefileException,Exception
    {
      geomFactory = geometryFactory;
       file = getInputStream();
        if(file==null) throw new IOException("Failed connection or no content for "+baseURL);
        mainHeader = new ShapefileHeader(file);
        if(mainHeader.getVersion() < VERSION){System.err.println("Sf-->Warning, Shapefile format ("+mainHeader.getVersion()+") older that supported ("+VERSION+"), attempting to read anyway");}
        if(mainHeader.getVersion() > VERSION){System.err.println("Sf-->Warning, Shapefile format ("+mainHeader.getVersion()+") newer that supported ("+VERSION+"), attempting to read anyway");}

        //ArrayList list = new ArrayList();
        int type=mainHeader.getShapeType();
        handler = getShapeHandler(type);
        if(handler==null)throw new ShapeTypeNotSupportedException("Unsuported shape type:"+type);

        recordNumber=0;
    }

    /**
     * Returns the next geometry in the shapefile stream
     * @return null at EOF
     * @throws IOException
     */
    public Geometry next()
        throws IOException
    {
      Geometry geom = null;
      try{
        // file.setLittleEndianMode(false);
        recordNumber=file.readIntBE();
        int contentLength=file.readIntBE();
        try{
          geom = handler.read(file,geomFactory,contentLength);
          // System.out.println("Done record: " + recordNumber);
        }catch(IllegalArgumentException r2d2){
          //System.out.println("Record " +recordNumber+ " has is NULL Shape");
          geom = new GeometryCollection(null,null,-1);
        }catch(Exception c3p0){
          System.out.println("Error processing record (a):" +recordNumber);
          System.out.println(c3p0.getMessage());
          c3p0.printStackTrace();
          geom = new GeometryCollection(null,null,-1);
        }
        // System.out.println("processing:" +recordNumber);
      }
      catch(EOFException e){
        close();
      }
      return geom;
    }

    public void close()
        throws IOException
    {
      file.close();
    }

       //ShapeFileDimentions =>    2=x,y ; 3=x,y,m ; 4=x,y,z,m



    /**
     * Returns a string for the shape type of index.
     * @param index An int coresponding to the shape type to be described
     * @return A string descibing the shape type
     */
    public static String getShapeTypeDescription(int index){
        switch(index){
            case(NULL):return ("Null");
            case(POINT):return ("Points");
            case(POINTZ):return ("Points Z");
            case(POINTM):return ("Points M");
            case(ARC):return ("Arcs");
            case(ARCM):return ("ArcsM");
            case(ARCZ):return ("ArcsM");
            case(POLYGON):return ("Polygon");
            case(POLYGONM):return ("PolygonM");
            case(POLYGONZ):return ("PolygonZ");
            case(MULTIPOINT):return ("Multipoint");
            case(MULTIPOINTM):return ("MultipointM");
            case(MULTIPOINTZ):return ("MultipointZ");
            default:return ("Undefined");
        }
    }

    public static ShapeHandler getShapeHandler(Geometry geom, int ShapeFileDimentions ) throws Exception
    {
        return getShapeHandler(getShapeType(geom,ShapeFileDimentions));
    }

    public static ShapeHandler getShapeHandler(int type) throws Exception
    {
        switch(type){
            case Shapefile.POINT: return new PointHandler();
            case Shapefile.POINTZ: return new PointHandler(Shapefile.POINTZ);
            case Shapefile.POINTM: return new PointHandler(Shapefile.POINTM);
            case Shapefile.POLYGON: return new PolygonHandler();
            case Shapefile.POLYGONM: return new PolygonHandler(Shapefile.POLYGONM);
            case Shapefile.POLYGONZ: return new PolygonHandler(Shapefile.POLYGONZ);
            case Shapefile.ARC: return new MultiLineHandler();
            case Shapefile.ARCM: return new MultiLineHandler(Shapefile.ARCM);
            case Shapefile.ARCZ: return new MultiLineHandler(Shapefile.ARCZ);
            case Shapefile.MULTIPOINT: return new MultiPointHandler();
            case Shapefile.MULTIPOINTM: return new MultiPointHandler(Shapefile.MULTIPOINTM);
            case Shapefile.MULTIPOINTZ: return new MultiPointHandler(Shapefile.MULTIPOINTZ);
        }
        return null;
    }

     //ShapeFileDimentions =>    2=x,y ; 3=x,y,m ; 4=x,y,z,m
    public static int getShapeType(Geometry geom, int coordDimension ) throws ShapefileException
    {

        if ( (coordDimension !=2) && (coordDimension !=3) && (coordDimension !=4) )
        {
            throw new ShapefileException("invalid ShapeFileDimentions for getShapeType - expected 2,3,or 4 but got "+coordDimension+"  (2=x,y ; 3=x,y,m ; 4=x,y,z,m)");
            //ShapeFileDimentions = 2;
        }


        if(geom instanceof Point)
        {
            switch (coordDimension)
            {
                case 2: return Shapefile.POINT;
                case 3: return Shapefile.POINTM;
                case 4: return Shapefile.POINTZ;
            }
        }
        if(geom instanceof MultiPoint)
        {
            switch (coordDimension)
            {
                case 2: return Shapefile.MULTIPOINT;
                case 3: return Shapefile.MULTIPOINTM;
                case 4: return Shapefile.MULTIPOINTZ;
            }
        }
        if ( (geom instanceof Polygon) || (geom instanceof MultiPolygon) )
        {
            switch (coordDimension)
            {
                case 2: return Shapefile.POLYGON;
                case 3: return Shapefile.POLYGONM;
                case 4: return Shapefile.POLYGONZ;
            }
        }
        if ( (geom instanceof LineString) || (geom instanceof MultiLineString) )
        {
            switch (coordDimension)
            {
                case 2: return Shapefile.ARC;
                case 3: return Shapefile.ARCM;
                case 4: return Shapefile.ARCZ;
            }
        }
        return Shapefile.UNDEFINED;
    }

    public synchronized void readIndex(InputStream is) throws IOException {
        EndianDataInputStream file = null;
        try{
            BufferedInputStream in = new BufferedInputStream(is);
            file = new EndianDataInputStream(in);
        }catch(Exception e){System.err.println(e);}
        ShapefileHeader head = new ShapefileHeader(file);

        int pos=0,len=0;
        //file.setLittleEndianMode(false);
        file.close();
    }
}












