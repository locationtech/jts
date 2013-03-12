     JTS Topology Suite
     ==================
     
Welcome to the repository for the JTS Topology Suite.
JTS essentially consists of several Java modules,
each one corresponding to a separate JAR file.
Only the main jts.jar is necessary to use the library in an application.
The others are external tools or optional extensions.

Repository Structure
--------------------

NOTE: The repo is evolving towards a Maven-compatible structure, but is not quite there yet.

* jts - Core JTS module
* jtsio - I/O drivers for proprietary formats
* jtsapp - Applications & tools for working with JTS
* jts-sde-adapter - an older driver for ArcSDE (unmaintained)
* libjts - A wrapper for building JTS with GCJ (unmaintained)
* bin - Scripts for running JTS tools on various platforms
* doc - Documentation, Version History, license files

* jts/testxml - Unit tests for use with JTS TestRunner app
* jts/testdata - test datasets for use in testing algorithms and functions

Build JTS
---------

The JTS library is intended to be Java 1.4 compatible
(to permit deployment on mobile platforms and some primitive databases).
The tools are not subject to this limitation, so target Java 1.6 or higher.

* In the root directory execute

  ant
  
The project is built to the directory 'build' (which is excluded from version control).

Test JTS
--------

* Once JTS is built, Java unit tests can be executed using
 
  ant junit
  
* The XML test files can also be run, using the TestRunner application.
  This is invoked by the testrunner shell script, and may
  also be run from inside an IDE.
  At the JTS root dir run:
  
  testrunner -files jts/testxml/general jts/testxml/validate 

Deploy JTS
----------

The main build artifacts are the following JARs

* jts-x.x.jar - main JAR
* jtsio-x.x.jar - IO drivers with external dependencies
* JTS_Test.jar - JTS apps

Only the main jta JAR is required to use the library.
The jtsio JAR is optional, and requires access to external JARs
The JTS_Test JAR is only needed when running the JTS tools.

Configure JTS in Eclipse
------------------------

JTS is only implicitly organized into modules, so a single Eclipse project is 
fine for working with all modules.

* Create a Java project (for example, 'JTS')

* Link the following source folders:
** src - jts/java/src
** test - jts/java/test
** src-app - jtsapp/src/main/java
** test-app - jtsapp/src/test/java
** src-io - jtsio/src/main.java
** test-io - jtsio/src/test/java

* Link to the external archives in:
** jts/java/lib
** jtsio/lib

If not used it may be better to omit the jtsio module.

Alternatively, each ancillary module could be configured as a separate project,
with a dependency on the main JTS project.

Run Configurations 
^^^^^^^^^^^^^^^^^^

Useful JTS tools:

* JTS TestBuilder - com.vividsolutions.jtstest.testbuilder.JTSTestBuilder
** VM args: -Xmx1000M

* JTS XML Tests - com.vividsolutions.jtstest.testrunner.TopologyTestApp
** Program arguments: -files jts/testxml/general jts/testxml/validate  
** Working Directory: <repo root>

