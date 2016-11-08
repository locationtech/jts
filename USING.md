# JTS User Guide

## Project Structure

JTS consists of several Java modules,
each one corresponding to a separate JAR file.
Only `jts-core` is needed to use the library in an application.
The other modules are tools or optional extensions.

* `jts-core` - The JTS geometry model and operations
* `jts-app` - Applications for working with JTS, including the TestBuilder GUI
* `jts-io` - I/O classes for open spatial formats
* `jts-ora` - Oracle reader and writer
* `jts-sde` - SDE reader and writer

## Using JTS with Maven

To include JTS in your Maven project, add a dependency block like the next one.

```xml
<dependency>
    <groupId>org.locationtech.jts</groupId>
    <artifactId>jts-core</artifactId>
    <version>${jts.version}</version>
</dependency>
```

JTS snapshot artifacts are published to the LocationTech Maven repository. To include JTS in your project, add the following repositories to your pom.

```xml
<repositories>
  <repository>
    <id>locationtech-releases</id>
    <url>https://repo.locationtech.org/content/groups/releases</url>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
  </repository>
  <repository>
    <id>jts-snapshots</id>
    <url>https://repo.locationtech.org/content/repositories/jts-snapshots</url>
    <releases>
      <enabled>false</enabled>
    </releases>
	<snapshots>
      <enabled>true</enabled>
    </snapshots>
   </repository>
</repositories>
```
