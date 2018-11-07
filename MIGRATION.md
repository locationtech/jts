JTS Upgrade Guide
=================

JTS 1.16
--------

Coordinate and CoordinateSequence now define methods for XYZM access, with this in mind we ask the field ``z`` is deprecated and we ask client code to use ``coord.getZ()``.

JTS 1.15
--------

The JTS Topology Suite has a long history, and in 2016/2017, [Vivid Solutions](http://www.vividsolutions.com/) brought the project to the LocationTech working group of the Eclipse Foundation.  

During that transition, the Java package names and Maven GAVs have changed.  For package names (typically used in imports), the change is reflected below:

|               | **JTS 1.14.0 and before** | **JTS 1.15.0 and later**    |
|---------------|:--------------------------|:----------------------------|
| Maven GroupId | com.vividsolutions        | org.locationtech.jts        |
| Package names | com.vividsolutions.jts.*  | org.locationtech.jts.*      |

To upgrade a Maven project (or another build tool using Maven dependency management), one can do a find and replace on ```pom.xml``` files (or similar build files).  In the source code, one could do a find and replace on the package names.  As a concrete example, one could use these two commands to handle most of the migration. 

```
git grep -l com.vividsolutions | grep pom.xml | xargs sed -i "s/com.vividsolutions/org.locationtech.jts/g"
git grep -l com.vividsolutions | xargs sed -i "s/com.vividsolutions/org.locationtech/g"
```

## Using JTS with Maven

To include JTS in a Maven project, add a dependency block like the following:

```xml
<dependency>
    <groupId>org.locationtech.jts</groupId>
    <artifactId>jts-core</artifactId>
    <version>${jts.version}</version>
</dependency>
```

Where ${jts.version} is the released version of JTS one chooses to use.  See the [releases](https://github.com/locationtech/jts/releases) page for options.
