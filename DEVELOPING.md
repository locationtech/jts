# JTS Developing Guide

## Building

The JTS build chain uses Maven.  Build commands are executed at the project root directory (this one).

* Build JTS, with tests:

        mvn clean install
    
* Build JTS, no tests:

        mvn clean install -DskipTests

* Build `jts-io-ora`:

        mvn install -Poracle
        
* Build `jts-io-sde`:

        mvn install -Parcsde
        
* Build everything:

        mvn install -Pall

## Javadoc

* Build Javadoc for core modules

        mvn javadoc:aggregate

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
in a reusable way.  It is easily consumed by external tools such as the JTS TestBuilder.
It allows geometry function test cases to easily be to applied to other operation implementations,
for testing or comparison purposes.

This format should be used for tests which involve large geometries, or which 
express fundamental geometric semantics of the JTS library.

It is convenient to define the following Run Configurations:

* XML tests:
  * Main class: `org.locationtech.jtstest.testrunner.TopologyTestApp`
  * Program arguments: `-files validate general`
  * Working directory: `${jts-core}/src/test/resources/testxml`

