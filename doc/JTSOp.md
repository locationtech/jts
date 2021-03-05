# JtsOp User Guide

`JtsOp` is a CLI (command-line interface) to JTS. It can be used to:

* Execute JTS operations on geometry to produce the results, in various spatial formats
* Chain together sequences of JTS operations to accomplish a spatial processing task
* Extract, subset, validate and transform geometry from data files
* Convert geometry from one format into another
* Generate geometry for testing or display purposes
* Summarize the contents of geometry datafiles
* Test the functionality or performance of JTS operations

## Functionality

`JtsOp` has the following functionality:

### Input
* Read A and B geometry lists from:
  * literals on the command line (WKT or WKB)
  * standard input (WKT or WKB)
  * files in various formats (WKT, WKB, GeoJSON, GML, SHP)
  * a single input can supply both A and B (option `-ab`)
* Apply a limit and/or offset when reading:
  * `-limit` specifies a limit
  * `-offseet` specified an offset
  * supported for WKT, WKB, SHP file formats
* Collect input A into a single GeometryCollection for use with "aggregate" functions (such as `Overlay.unaryUnion`)
  * `-collect`
* Explode A and/or B inputs into separate geometry components
  * `-eacha`, `-eachb`

### Operations
* Use a spatial index for binary operations
  * `-index`
* Execute any spatial or scalar function available in the JTS TestBuilder
* Run an operation multiple times using a set of different argument values
  * `-args v1,v2,v3 ...`
* Repeat operation execution multiple times, to provide better timing results
  * `-repeat n`
  
### Output
* Set the SRID of the output geometries
  * `-srid <SRID>`
* Explode output into a list of atomic geometries instead of a single geometry collection
  * `-explode`
* Output the result in the formats WKT, WKB, GeoJSON, GML, SVG
  * `-f wkt | wkb | geojson | svg`
* Display information about the input geometries and function timing
  * `-v`
* Display timing information
  * `-time`
* Load external spatial functions dynamically (as a Java class with static methods)
  * `-geomfunc classname` 
* List all available functions
  * `-help` 
* chain operations together by writing/reading input from `stdin` and using shell piping

## Examples

 * Print usage instructions
      
       jtsop
       
 * Print usage instructions and list of available operations
 
       jtsop -help
       
 * Compute the area of WKT geometries and output as text
      
       jtsop -a geoms.wkt -f txt area 
      
 * Compute the unary union of WKT geometries and output as WKB
 
       jtsop -a geoms.wkt -collect -f wkb Overlay.unaryUnion 
 
 * Compute the union of two geometries in WKT and WKB and output as WKT
      
       jtsop -a some-geom.wkt -b some-other-geom.wkb -f wkt Overlay.Union
 
 * Compute the buffer of distance 10 of a WKT geometries and output as GeoJSON
    
       jtsop -a some-geom.wkt -f geojson Buffer.buffer 10
 
 * Compute the buffer of a WKT literal for multiple distances 
 
       jtsop -a "MULTIPOINT ( (0 0), (10 10) )" -f wkt -op Buffer.buffer 1,2,3,4
 
 * Compute the buffer of a WKT literal and output the individual polygons as a list of geometries in WKT 
 
       jtsop -a "MULTIPOINT ( (0 0), (10 10) )" -f wkt -explode -op Buffer.buffer 1
 
 * Compute the buffer of a WKB literal and output as WKT
 
       jtsop -a 000000000140240000000000004024000000000000 -f wkt Buffer.buffer 10
  
 * Compute the buffer of a WKT literal and output as WKB, with SRID set to 4326
 
       jtsop -a  "POINT (10 10)" -srid 4326 -f wkb Buffer.buffer 10
  
 * Output a literal geometry as GeoJSON
    
       jtsop -a "POINT (10 10)" -f geojson
       
 * Validate geometries from a WKT file using limit and offset
      
       jtsop -a geoms.wkt -limit 100 -offset 40 -f txt Geometry.isValid 
       
 * Compute an operation on a file of geometries and output only geometry metrics and timing
 
       jtsop -v -a geosm.wkt Buffer.buffer 10
       
 * Compute the intersection of all pairs of geometries from A and B, using a spatial index
 
       jtsop -v -a geomsA.wkt --b geomsB.wkt -eacha -eachb -index Overlay.intersection 
 
 * Chain operations using a shell pipe
 
       jtsop -f wkb CreateRandomShape.randomPoints 10 | jtsop -a stdin -f wkt Buffer.buffer 1
