# JTS Developing Guide

## Eclipse Configuration

* Generate Eclipse configuration using `mvn eclipse:eclipse`
* Import the generated projects into an Eclipse workspace

## Testing

JTS aims for 100% code coverage for unit tests. 

There are two kinds of unit tests:

### JUnit tests

Used for verifying API code, internal data structures, and ancillary algorithms.

### XML Tests

JTS provides a code-independent, declarative XML-based format for expressing geometric functional tests.  
This format is convenient for encoding large geometries, and for providing geometric test cases 
in a reusable way.  This format should be used for tests which involve large geometries, or which 
express fundamental geometric properties of the JTS library.

 
