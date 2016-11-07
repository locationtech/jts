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
