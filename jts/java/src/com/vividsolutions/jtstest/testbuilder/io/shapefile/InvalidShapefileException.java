package com.vividsolutions.jtstest.testbuilder.io.shapefile;

/**
 * Thrown when an attempt is made to load a shapefile
 * which contains an error such as an invlaid shape
 */
public class InvalidShapefileException extends ShapefileException{
    public InvalidShapefileException(String s){
        super(s);
    }
}
