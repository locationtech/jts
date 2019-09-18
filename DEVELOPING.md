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

### Run Configurations

It is convenient to define the following Run Configurations:


* **JTS TestRunner** - for executing XML tests:

Field | Value
------|------
Type | Java Application
Project | `jts-tests`
Main class | `org.locationtech.jtstest.testrunner.JTSTestRunnerCmd`
Program arguments | `validate general`
Working directory | `${workspace_loc:jts-tests/src/test/resources/testxml}`

* **JTS TestBuilder** - for viewing and processing geometry with JTS

Field | Value
------|------
Type | Java Application
Project | `jts-app`
Main class | `org.locationtech.jtstest.testbuilder.JTSTestBuilder`
Program arguments (optional) | `-geomfunc <classname> ...`
VM args | `-Xmx1000M`
VM args (optional, for Mac) | `-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel`
Working directory | Default

## Testing

JTS aims for 100% code coverage for unit tests.

There are two kinds of unit tests:

### JUnit tests

Used for verifying API code, internal data structures, and ancillary algorithms.
These tests are written in Java.
This allows testing all parts of the codebase,
and can provide richer error detection and reporting.
However, the tests are not as readable or portable
as the XML tests.

* To run the unit tests in a module (`jts-core`):

        mvn test -pl modules/core

### XML Tests

JTS provides a code-independent, declarative XML-based format for expressing geometric functional tests.
This format has the following advantages:

* allows encoding large geometries
* provides geometric test cases in a reusable way
* easily consumed by tools such as the JTS TestBuilder or by other geometry libraries (e.g. GEOS)
* allows geometric tests to be used with other operation implementations, for testing or comparison purposes

This format should be used for tests which involve large geometries, or which
express fundamental geometric semantics of the JTS library.

The XML test format can be executed using the JTS TestRunner, or imported into the JTS TestBuilder.


