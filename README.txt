     JTS Topology Suite
     ==================
     
Welcome to the repository for the JTS Topology Suite.

Repository Structure
--------------------

NOTE: The repo is evolving towards a Maven-compatible structure, but is not quite there yet.

* jts - Core JTS module
* jtsio - I/O drivers for proprietary formats
* jtsapp - Applications for working with JTS
* libjts - A wrapper for building JTS with GCJ (unmaintained)
* bin - Scripts for running JTS tools on various platforms
* doc - Documentation

* jts/testxml - Unit tests for use with JTS TestRunner app
* jts/testdata - test datasets for use in testing algorithms and functions

Building JTS
------------

* In the root directory execute

  ant
  
The project is built to the directory 'build'.

* Once JTS is built, unit tests can be executed using
 
  ant junit


Configuring JTS in Eclipse
--------------------------

JTS is only implicitly organized into modules, so a single Eclipse project is 
fine for working with all modules.

* Create a Java project (for example, 'JTS')

* Link the following source folders:
** src - jts/java/src
** test - jts/java/test
** src-app - jtsapp/src/java/main
** test-app - jtsapp/src/java/main
** src-io - jtsio/src
** test-io - jtsio/test

* Link to the libs in:
** jts/java/lib
** jtsio/lib

It is helpful to make Run Configurations for the following JTS tools:

* JTS TestBuilder - com.vividsolutions.jtstest.testbuilder.JTSTestBuilder
** VM args: -Xmx1000M

* JTS XML Tests - com.vividsolutions.jtstest.testrunner.TopologyTestApp
** Program arguments: -files <jts>/testxml/general <jts>/testxml/validate  
                           (where <jts> is the path of the jts directory)

