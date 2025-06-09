# JTS User Guide

## Project Structure

JTS consists of several Java modules, each one corresponds to a separate JAR file.

The core modules are:

* `jts-core` - geometry model, operations, algorithms, and spatial data structures
* `jts-io-common` - I/O classes for open spatial formats

The following modules depend on proprietary libraries, and are not built by default:

* `jts-io-ora` - Oracle reader and writer
* `jts-io-sde` - SDE reader and writer

The following modules are applications and data for testing and working with JTS

* `jts-tests` - The JTS XML test suite and the Test Runner application
* `jts-app` -The TestBuilder GUI, for working with JTS geometry interactively

## Using JTS with Maven

To include JTS in a Maven project, add a dependency block like the following:

```xml
<properties>
    <jts.version>1.16.1</jts.version>
</properties>
<dependency>
    <groupId>org.locationtech.jts</groupId>
    <artifactId>jts-core</artifactId>
    <version>${jts.version}</version>
</dependency>
```

JTS artifacts are available on maven central.

### Using JTS Snapshots

Our [build server](https://ci.eclipse.org/jts/) publishes [snapshots](https://repo.eclipse.org/content/repositories/jts-snapshots/org/locationtech/jts) to the LocationTech Maven repository. 
To include JTS in a project, add the following repositories to the pom:

```xml
<repositories>
  <repository>
    <id>locationtech-releases</id>
    <url>https://repo.eclipse.org/content/repositories/jts</url>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
  </repository>
  <repository>
    <id>jts-snapshots</id>
    <url>https://repo.eclipse.org/content/repositories/jts-snapshots</url>
    <releases>
      <enabled>false</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
   </repository>
</repositories>
```

The latest snapshot builds are now avaialble:

```xml
<properties>
    <jts.version>1.20.0-SNAPSHOT</jts.version>
</properties>
<dependency>
    <groupId>org.locationtech.jts</groupId>
    <artifactId>jts-core</artifactId>
    <version>${jts.version}</version>
</dependency>
```

### Using prior versions of JTS with Maven

Older versions are available on Maven Central:

JTS 1.14.0:

```xml
<dependency>
    <groupId>com.vividsolutions</groupId>
    <artifactId>jts-core</artifactId>
    <version>1.14.0</version>
</dependency>
<dependency>
    <groupId>com.vividsolutions</groupId>
    <artifactId>jts-io</artifactId>
    <version>1.14.0</version>
</dependency>
```

JTS 1.13:

```xml
<dependency>
    <groupId>com.vividsolutions</groupId>
    <artifactId>jts</artifactId>
    <version>1.13</version>
</dependency>
```

Prior releases can also be downloaded from [Source Forge](https://sourceforge.net/projects/jts-topo-suite/files/jts/).

## Using JTS with Jigsaw Modules

JTS uses [#ModuleNameInManifest](http://openjdk.java.net/projects/jigsaw/spec/issues/#ModuleNameInManifest) to export a module name for each of the JARs published for use as a library. In this way, you can depend on the various JTS modules in your `module-info.java` in the following way:

```java
// module-info.java for project org.foo.baz

module org.foo.baz {
  requires org.locationtech.jts;            // jts-core
  requires org.locationtech.jts.io;         // jts-io-common
  requires org.locationtech.jts.io.oracle;  // jts-io-ora
  requires org.locationtech.jts.io.sde;     // jts-io-sde
}
```
## JTS System Properties

* `-Djts.overlay=ng` enables the use of OverlayNG in `Geometry` overlay methods. (*Note: in a future release this will become the default behaviour*) 
* `-Djts.relate=ng` enables the use of RelateNG in `Geometry` topological predicate methods. (*Note: in a future release this will become the default behaviour*) 

## JTS Tools

JTS includes various application tools, which are documented [here](doc/TOOLS.md).

