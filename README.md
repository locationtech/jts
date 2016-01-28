# JTS Topology Suite

Welcome to the repository for the JTS Topology Suite.
JTS consists of several Java modules,
each one corresponding to a separate JAR file.
Only the jts-core.jar is necessary to use the library in an application.
The others are external tools or optional extensions.

### Modules

- __core__ - Core JTS module
- __io__ - I/O drivers for proprietary formats
- __app__ - Applications & tools for working with JTS
- __example__ - Examples of working JTS code

### Other repository folders

- __bin__ - Scripts for running JTS tools on various platforms
- __doc__ - Documentation, Version History, license files

### Build JTS

The JTS library is intended to be Java 1.4 compatible
(to permit deployment on mobile platforms and some primitive databases).
The tools are not subject to this limitation, so target Java 1.6 or higher.

The following maven commands should be executed in the root of the directory.

#### Build JTS Topology Suite, test, and install in local repository

```console
> mvn install
```

#### Build JTS Topology Suite but skip the tests, and install in local repository

```console
> mvn clean install -DskipTests
```

The project is built to the directory 'build' (which is excluded from version control).

### Test JTS

There are XML files that describe test cases, which are in the `modules/core/src/test/resources/testxml`.
Test data (in WKT and WKB format) are found in `modules/core/src/test/resources/testdata`

Once JTS is built, Java unit tests can be executed using

```console
> mvn test
```

Along with the junit tests, the XML test files will also be run, using the TestRunner application.

### Deploy JTS

The main build artifacts are the following JARs

* jts-x.x.jar - main JAR
* jtsio-x.x.jar - IO drivers with external dependencies
* JTS_Test.jar - JTS apps

Only the main jta JAR is required to use the library.
The jtsio JAR is optional, and requires access to external JARs
The JTS_Test JAR is only needed when running the JTS tools.

### Configure JTS in Eclipse

JTS is only implicitly organized into modules, so a single Eclipse project is
fine for working with all modules.

#### Setup for eclipse development

```console
mvn eclipse:eclipse
```

#### Run Configurations

To allow convenient testing of JTS it is useful to configure the following Run Configurations, :

- JTS TestBuilder
  - Main class: _com.vividsolutions.jtstest.testbuilder.JTSTestBuilder_
  - Option Program Arguments: _-geomfunc <classname> ..._
  - VM args: _-Xmx1000M_
  - Optional VM args (on Mac): _-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel_

- JTS XML Tests - the Test Suite for JTS algorithms
  - Main class: _com.vividsolutions.jtstest.testrunner.TopologyTestApp_
  - Program arguments: _-files modules/core/src/test/resources/testxml/general modules/core/src/test/resources/testxml/validate_
  - Working Directory: _repo root_
