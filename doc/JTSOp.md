# JtsOp User Guide

`JtsOp` is a CLI (command-line interface) to JTS and the TestBuilder.
It has the following features:

* read A and B geometries from the commandline or files in various formats (WKT, WKB, GeoJSON, GML, SHP)
* execute any spatial or scalar function available in the TestBuilder
* output the result in the formats WKT, WKB, GeoJSON, GML, SVG
* display information about the input geometries and function timing
* load other functions dynamically with `-geomfunc` parameter (as a Java class with static methods)

## Examples

 * Print usage instructions
      
       jtsop
       
 * Print usage instructions and list of available operations
 
       jtsop -help
       
 * Compute the area of a WKT geometry and output it
      
       jtsop -a some-geom.wkt -f txt area 
      
 * Compute the unary union of a WKT geometry and output as WKB
 
       jtsop -a some-geom.wkt -f wkb Overlay.unaryUnion 
 
 * Compute the union of two geometries in WKT and WKB and output as WKT
      
       jtsop -a some-geom.wkt -b some-other-geom.wkb -f wkt Overlay.Union
 
 * Compute the buffer of distance 10 of a WKT geometry and output as GeoJSON
    
       jtsop -a some-geom.wkt -f geojson Buffer.buffer 10
 
 * Compute the buffer of a literal geometry and output as WKT
 
       jtsop -a "POINT (10 10)" -f wkt Buffer.buffer 10
  
 * Output a literal geometry as GeoJSON
    
       jtsop -a "POINT (10 10)" -f geojson
       
 * Compute an operation on a geometry and output only geometry metrics and timing
 
       jtsop -v -a some-geom.wkt Buffer.buffer 10
