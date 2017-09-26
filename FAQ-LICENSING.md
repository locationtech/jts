# JTS License FAQ

This FAQ clarifies the implications of the JTS software licensing.
JTS is dual-licensed under the EDL and EPL 
as described [here](LICENSES.md).

## Terminology

**Downstream projects** includes the following:

* Ports of JTS to other languages: GEOS, JSTS, etc..
* Use of JTS libraries in other codebases: GeoTools, GeoServer, etc ...

## Licensing Frequently Asked Questions

A1. **What is the difference between EDL and BSD-3?**

  BSD-3 is a license **family**.  EDL is the actual license and is called that because Eclipse is the distributing organization.

A2. **JTS no longer uses LGPL.  What does this mean for downstream projects?**

JTS is now released under a dual license: EDL and EPL.  
Dual-licensing gives downstream projects more options:

* EDL is a BSD-3-style license, which is more permissive than the LGPL.  
  This allows a wider variety of uses; for instance, it permits iOS use of JTS for mobile development.
  
* The EPL license provideds a similar permissiveness to LGPL with different wording around patents

A3. **But I thought LGPL and EPL do not mix? How does this effect the LGPL GEOS project?**

For use of JTS by an LGPL project like GEOS:

Use the BSD-3 License (the BSD-3 License is compatible with the LGPL license used by GEOS).

In the longer term the EPL license is being revised to work with GPL / LGPL, allowing GEOS the choice of either BSD-3 or EPL license.

A4. **How can an LGPL-licensed project (such as GEOS) contribute code to JTS?**

From the JTS perspective this works in the same way as any other contribution.  
The contributor must have an Eclipse Contributor License Agreement (CLA) in place (see here).

Code contributed to JTS must be releasable under the EDL & EPL licenses.
Therefore the contributor must also have permission from the downstream project (e.g. via OSGeo CLA or similar) 
to relicense the work in question. 
