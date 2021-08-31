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

        mvn install -Dall=true

* Limit build to release artifacts:

        mvn install -Drelease=true

## Quality Assurance

JTS build verify stage includes pmd, checkstyle and more:

        mvn verify

To skip QA checks:

        mvn verify -Dpmd.skip=true -Dcheckstyle.skip=true

To browse QA errors:
       
        mvn site:site
        open modules/core/target/site/index.html

### JUnit tests

JTS aims for 100% code coverage for unit tests.
Unit tests are written in Java using JUnit.
They are used for verifying API code, internal data structures, and ancillary algorithms.

This allows testing all parts of the codebase, and can provide richer error detection and reporting.
However, the tests are not as readable or portable as the XML tests.

* Run the unit tests in a module (e.g. `jts-core`):

        mvn test -pl modules/core

### XML Tests

JTS provides a simple language-independent, declarative XML-based format for expressing geometric functional tests.

This format has the following advantages:

* allows encoding large geometries more easily
* provides geometric test cases in a reusable, language-independent way
* easily consumed by tools such as the **JTS TestBuilder** 
* easily used by JTS ports (e.g. [GEOS](https://trac.osgeo.org/geos)) or other geometry libraries 
* allows geometric tests to be used with other operation implementations, for testing or comparison purposes

This format should be used for tests which
express fundamental geometric semantics of the JTS library.

The XML test format can be executed using the **JTS TestRunner**, or imported into the **JTS TestBuilder**.

### External QA tools

#### LGTM CodeQL analysis
[![Total alerts](https://img.shields.io/lgtm/alerts/g/locationtech/jts.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/locationtech/jts/alerts/) 
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/locationtech/jts.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/locationtech/jts/context:java)
* [Alerts report](https://lgtm.com/projects/g/locationtech/jts/alerts/?mode=tree)


#### ABI Laboratory Tracker
* [Binary compatibility report for JTS core](https://abi-laboratory.pro/?view=timeline&lang=java&l=jts-core)
* [Binary compatibility for JTS 1.15 and before](https://abi-laboratory.pro/index.php?view=timeline&lang=java&l=jts)

## Javadoc

* Build Javadoc for core modules

        mvn javadoc:aggregate

## Eclipse Configuration

Project:

1. Startup eclipse, creating a new `jts-workspace` location. This folder is used by eclipse to keep track of settings alongside your jts source code.
   
2. Use *File > Import*, and the wizard *Maven > Existing Maven Project*.
   
   Select top-level `jts` folder as the Root directory.

3. Once imported eclipse will build the project using built-in maven support.
   
4. During initial build warning is shown for maven lifecycle mapping for `checkstyle:check`. Use the quickfix to **ignore** this lifecycle mapping.
   
   Do not try the *maven-checkstyle-plugin* connector as it fails to install.

Plugins:

* Install *Eclipse-CS* from the market place.

  1. Select *jts-core* project properties and navigate to *Checkstyle* preference page.

  2. From the *Local check configuration* tab use *new* to create a check configuration.

  3. Setup a *Project Relative Configuration* named `jts`, selecting ``build-tools/src/main/resources/jts/checkstyle.xml``
     
  4. Press *Additional properties* to open the *Additional Checkstyle configuration file properties* dialog.
  
  5. Press *Find unresolved properties* to define:
     
     * property `checkstyle.header.file` value: Absolute path to `src/main/resources/jts/header.txt`
     * property `checkstyle.header.file`value: Absolute path to `src/main/resources/jts/suppressions.xml`

  6. From the *Main* tab:
     
     * Enable *Checkstyle active for this project*.
     * Select ``jts`` configuration
     
  7. You can *jts-core* as a blueprint to copy the Checkstyle configuration to other modules.
  
  Checkstyle is integrated into the build cycle updating warnings each time you save.

* Install *eclipse-pmd* following directions for [offline install](https://acanda.github.io/eclipse-pmd/getting-started.html) to download a [recent release](https://github.com/eclipse-pmd/eclipse-pmd/releases/).
  
  1. Select *jts-core* project properties and navigate to *PMD* preference page.
  2. Use *add* button to add a workspace ruleset using `build-tools/src/main/resources/jts/pmd-ruleset.xml`
  3. Name the ruleset `jts`
  
  PMD is integrated into the build cycle updating warnings each time you save, and providing some quickfixes.

* Alternative: Install *pmd-eclipse-plugin* from the market place.

  1. Select *jts-core* project properties and navigate to *PMD* preference page.
  2. Enable PMD
  3. Use a the ruleset configured in a project file, selecting `build-tools/src/main/resources/jts/pmd-ruleset.xml`
  
  You can use *PMD > Check code* to list errors and warnings. The results are shown in their own view, and quickfixes are not available.
  
Run Configurations:

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
