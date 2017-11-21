JTS Upgrade Guide
=================

The JTS Topology Suite has a long history, and in 2016/2017, it moved from SourceForge to GitHub.  The Java package names and Maven GAVs have changed between version 1.14.0 and before.  

[Vivid Solutions](http://www.vividsolutions.com/) has donated the code to LocationTech.  For package names (typically used in imports), the change is reflected below:

|               | **JTS 1.14.0 and before** | ***JTS 1.15.0 and later**** |
|---------------|:--------------------------|:----------------------------|
| Package names | com.vividsolutions.jts.*  | org.locationtech.jts.*      |
| Maven GroupId | com.vividsolutions        | org.locationtech.jts        |

