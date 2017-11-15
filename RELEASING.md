# JTS Release Guide

This guide is intended for JTS project maintainers, 
to assist them in preparing releases of the project.

## Release Checklist

* Set the version number in the following artifacts:
  * Maven POMs
  * Java class `JTSVersion`
* Create a Release Milestone, and tag it to Issues and PRs wanted in the release
* Confirm Maven build executes with no errors
* Review scripts in `bin` to confirm correctness
* Update the Version History, to record significant changes

