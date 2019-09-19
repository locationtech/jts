# JtsOp User Guide

`JtsOp` is a CLI (command-line interface) to JTS and the TestBuilder. It can be used to:

* Execute JTS operations on geometry to produce the results, in various spatial formats
* Chain together sequences of JTS operations to accomplish a spatial processing task
* Extract and transform geometry from data files
* Convert geometry from one format into another
* Generate geometry for testing or display purposes
* Summarize the contents of geometry datafiles
* Test the functionality or performance of JTS operations

It has the following features:

* read A and B geometries from:
  * WKT or WKB literals on the command line
  * standard input (WKT or WKB)
  * files in various formats (WKT, WKB, GeoJSON, GML, SHP)
  * a single input can supply both A and B (option `-ab`)
* execute any spatial or scalar function available in the TestBuilder
* "spread" execution over each geometry component from one or both inputs
  * `-each [ a | b | ab | aa ]`
  * the `-each aa` parameter uses the A input for both arguments for binary operations
  * the `-index` parameter uses a spatial index for binary operations
* run op multiple times using a list of op argument values
  * `-args v1 v2 v3 ...`
* repeat operation execution multiple times, to provide better timing results
* output the result in the formats WKT, WKB, GeoJSON, GML, SVG
* display information about the input geometries and function timing
* load other functions dynamically with `-geomfunc` parameter (as a Java class with static methods)
* chain operations together by writing/reading input from `stdin` and using OS piping

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
       
 * Compute the intersection of all pairs of geometries from A and B, using a spatial index
 
       jtsop -v -a geomA.wkt --b geomB.wkt -each ab -index Overlay.intersection 
 
 * Chain operations using a pipe
 
       jtsop -f wkb CreateRandomShape.randomPoints 10 | jtsop -a stdin -f wkt Buffer.buffer 1
