# JtsOp User Guide

`JtsOp` is a CLI (command-line interface) to JTS. It can be used to:

* Execute JTS operations on geometry to produce the results, in various spatial formats
* Chain together sequences of JTS operations to accomplish a spatial processing task
* Extract and transform geometry from data files
* Convert geometry from one format into another
* Generate geometry for testing or display purposes
* Summarize the contents of geometry datafiles
* Test the functionality or performance of JTS operations

## Functionality

`JtsOp` has the following functionality:

* Read A and B geometries from:
  * WKT or WKB literals on the command line
  * standard input (WKT or WKB)
  * files in various formats (WKT, WKB, GeoJSON, GML, SHP)
  * a single input containing two geometries can supply both A and B (option `-ab`)
* Execute any spatial or scalar function available in the JTS TestBuilder
* "spread" execution over each geometry component from one or both inputs
  * `-each [ a | b | ab | aa ]`
  * the `-each aa` parameter uses the A input for both arguments for binary operations
  * the `-index` parameter uses a spatial index for binary operations
* Run an operation multiple times using a sequence of different argument values
  * `-args v1,v2,v3 ...`
* Repeat operation execution multiple times, to provide better timing results
  * `-repeat n`
* Set the SRID of the output geometries
  * `-srid <SRID>`
* Output the result in the formats WKT, WKB, GeoJSON, GML, SVG
  * `-f wkt | wkb | geojson | svg`
* Explode output into a list of atomic geometries instead of a single geometry collection
  * `-explode`
* Display information about the input geometries and function timing
  * `-v`
* Load external spatial functions dynamically (as a Java class with static methods)
  * `-geomfunc classname` 
* chain operations together by writing/reading input from `stdin` and using OS piping

## Examples

 * Print usage instructions
      
       jtsop
       
 * Print usage instructions and list of available operations
 
       jtsop -help
       
 * Compute the area of a WKT geometry and output it as text
      
       jtsop -a some-geom.wkt -f txt area 
      
 * Compute the unary union of a WKT geometry and output as WKB
 
       jtsop -a some-geom.wkt -f wkb Overlay.unaryUnion 
 
 * Compute the union of two geometries in WKT and WKB and output as WKT
      
       jtsop -a some-geom.wkt -b some-other-geom.wkb -f wkt Overlay.Union
 
 * Compute the buffer of distance 10 of a WKT geometry and output as GeoJSON
    
       jtsop -a some-geom.wkt -f geojson Buffer.buffer 10
 
 * Compute the buffer of a WKT literal for multiple distances 
 
       jtsop -a "MULTIPOINT ( (0 0), (10 10) )" -f wkt -op Buffer.buffer 1,2,3,4
 
 * Compute the buffer of a WKT literal and output the individual polygons as a list of geometries in WKT 
 
       jtsop -a "MULTIPOINT ( (0 0), (10 10) )" -f wkt -explode -op Buffer.buffer 1
 
 * Compute the buffer of a WKB literal and output as WKT
 
       jtsop -a 000000000140240000000000004024000000000000 -f wkt Buffer.buffer 10
  
 * Compute the buffer of a WKT literal and output as WKB, with SRID set to 4326
 
       jtsop -a  "POINT (10 10)" -srid 4326 -f wkt Buffer.buffer 10
  
 * Output a literal geometry as GeoJSON
    
       jtsop -a "POINT (10 10)" -f geojson
       
 * Compute an operation on a geometry and output only geometry metrics and timing
 
       jtsop -v -a some-geom.wkt Buffer.buffer 10
       
 * Compute the intersection of all pairs of geometries from A and B, using a spatial index
 
       jtsop -v -a geomA.wkt --b geomB.wkt -each ab -index Overlay.intersection 
 
 * Chain operations using a pipe
 
       jtsop -f wkb CreateRandomShape.randomPoints 10 | jtsop -a stdin -f wkt Buffer.buffer 1
