# JTS Release Guide

This guide is intended for JTS project maintainers, 
to assist them in preparing releases of the project.

## Release Checklist

* Update version numbers in artifacts, if not already done
  * Maven POMs
  * Java class `JTSVersion`
* Create a Release Milestone, and tag it to issues and PRs wanted in the release
* Confirm Maven build executes with no errors
* Review scripts in `bin` to confirm correctness

