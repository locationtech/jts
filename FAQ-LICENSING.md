# JTS Licensing FAQ

JTS is dual-licensed under:

* the [Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-v20.html) ("EPL") 
* the [Eclipse Distribution License 1.0](http://www.eclipse.org/org/documents/edl-v10.php) (a Revised BSD-style license). 

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
  Revised BSD is also known as "Modified BSD", "BSD 2.0" or "3-clause BSD".  It is a less restrictive version of the original BSD license.
  
  EDL is the actual defined license.
  It is called that because Eclipse is the distributing organization.

**A2. JTS no longer uses LGPL.  What does this mean for downstream projects?**

  JTS is now released under a dual license: EDL **or** EPL.  
  
  Dual-licensing gives downstream projects their choice of either:

* The **EDL** is a Revised BSD-style license. This license is **more** permissive than the LGPL, allowing a wider variety of uses.
  
* The **EPL** provides a similar permissiveness to LGPL, with different wording around patents and requires a notice of where source code for JTS is available.

The choice of which license to use is up to you.

**A3. But I thought LGPL and EPL do not mix? How does this affect derivative projects like the LGPL GEOS project?**

  An LGPL project (like GEOS or GeoTools) can use JTS as a dependency, under the terms of the EDL license. 
  The three clauses in the EDL (common to all BSD-style licenses) allow GEOS to
  port the JTS work to C and distribute the resulting work under the LGPL. 
  Although the LGPL license contains additional restrictions 
  (notably a requirement to share modifications under the same license) 
  that exceed what is required by the EDL, these do not conflict with the EDL.
  
  For further information about the compatibility of Revised BSD-style licenses
  with the GPL see the [FSF License Comments](https://www.gnu.org/licenses/license-list.en.html).

**A4. How can an LGPL-licensed project (such as GEOS) contribute code to JTS?**

  From the JTS perspective this works in the same way as any other contribution.  
  The contributor must have an Eclipse *Contributor License Agreement* (CLA) in place.  
  For details see the [**Contributing Guide**](CONTRIBUTING.md).

  Code contributed to JTS **must** be releasable under the EDL & EPL licenses.
  Therefore the contributor must have permission from the contributing project (e.g. via OSGeo CLA or similar) 
  to relicense the work in question. 
