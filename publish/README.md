## Publishing JTS Releases

This directory contains the elements needed to publish JTS to Sonatype.

## Setup

You need to set up three environment variables:

- `JTS_PUBLISH_TAG`: git tag at https://github.com/locationtech/jts which to publish
- `JTS_SONATYPE_SETTINGS`: settings.xml file with sonatype credentials
- `JTS_GPG_KEY_DIRECTORY`: Directory with your GPG keys that you want to sign binaries with.

## Building the container

First build the container using `make build`. The image will always be rebuilt from
scratch, cloning jts and checking out the `JTS_PUBLISH_TAG` commit.


## Publishing

Run `make publish`. This will run the `maven clean deploy` inside of the
container, and push everything to the sonatype staging repository.

After that, you'll have to log in with the appropriate authentication to
https://oss.sonatype.org/, and from there follow the release instructions
at http://central.sonatype.org/pages/releasing-the-deployment.html.
