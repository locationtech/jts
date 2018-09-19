# JTS Release Guide

This guide is intended for JTS project maintainers, 
to assist them in preparing releases of the project.

## Release Checklist

* Create a Release Milestone, and tag it to Issues and PRs wanted in the release
* Confirm Maven build executes with no errors
* Update the Version History, to record significant changes
* Set the version number in the following artifacts:
  * Java class `JTSVersion`
  * Maven POMs (using the Maven release plugin)
* Review scripts in `bin` to confirm correctness
* Review and update the release notes
* Release to Maven central with the release property and profile ("mvn clean install -Drelease")

