# JTS License FAQ

JTS is dual-licensed under the [Eclipse Public License 1.0](https://www.eclipse.org/legal/epl-v10.html) ("EPL") 
**or** the [Eclipse Distribution License 1.0](http://www.eclipse.org/org/documents/edl-v10.php) (a Revised BSD-style license). 
More detail is provided in the [LICENSES](LICENSES.md) document.

This FAQ clarifies the implications of the JTS software licensing.

## Terminology

* **Downstream projects** are codebases which link to JTS libraries
  * e.g. GeoTools, GeoServer, etc.
* **Derivative projects** are ports of JTS to other languages 
  * e.g. GEOS, JSTS, etc.


## Licensing Frequently Asked Questions

**A1. What is the difference between EDL and Revised BSD?**

  Revised BSD is a license *family* (see [Wikipedia entry](https://en.wikipedia.org/wiki/BSD_licenses#3-clause_license_.28.22BSD_License_2.0.22.2C_.22Revised_BSD_License.22.2C_.22New_BSD_License.22.2C_or_.22Modified_BSD_License.22.29)).
  Note that Revised BSD is also known as "BSD 2.0" or "3-clause BSD".  It is a less restrictive version of the original BSD license.
  
  EDL is the actual defined license.
  It is called that because Eclipse is the distributing organization.

**A2. JTS no longer uses LGPL.  What does this mean for downstream projects?**

JTS is now released under a dual license: EDL **or** EPL.  
Dual-licensing gives downstream projects more options:

* The **EDL** is a Revised BSD-style license.
  This is **more** permissive than the LGPL.  
  This allows a wider variety of uses.  For instance, it permits iOS use of JTS for mobile development.
  
* The **EPL** provides a similar permissiveness to LGPL with different wording around patents

**A3. But I thought LGPL and EPL do not mix? How does this effect derivative projects (like the LGPL GEOS project)?**

An LGPL project (like GEOS) should use JTS under the terms of the EDL license, which is compatible with the LGPL license.

In the longer term the EPL license is being revised to work with GPL / LGPL.  
This will allow derivative projects the choice of using JTS under the terms of either the EDL or EPL license.

**A4. How can an LGPL-licensed project (such as GEOS) contribute code to JTS?**

From the JTS perspective this works in the same way as any other contribution.  
The contributor must have an Eclipse *Contributor License Agreement* (CLA) in place.  
For details see the [**Contributing Guide**](CONTRIBUTING.md).

Code contributed to JTS **must** be releasable under the EDL & EPL licenses.
Therefore the contributor must also have permission from the contributing project (e.g. via OSGeo CLA or similar) 
to relicense the work in question. 
