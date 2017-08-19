IMG  := jts/publish-jts-container
IMG_TAG  := "latest"

## REQUIRED ENV VARIABLES
#
# JTS_PUBLISH_TAG - git tag at https://github.com/locationtech/jts which to publish
# JTS_SONATYPE_SETTINGS - settings.xml file with sonatype credentials
# JTS_GPG_KEY_DIRECTORY - Directory with GPG keys
#

build: Dockerfile
	docker build \
		--no-cache \
		-t ${IMG}:${IMG_TAG} \
		--build-arg JTS_PUBLISH_TAG=${JTS_PUBLISH_TAG} \
		--build-arg JTS_SONATYPE_SETTINGS=${JTS_SONATYPE_SETTINGS} \
		--build-arg JTS_GPG_KEY_DIRECTORY=${JTS_GPG_KEY_DIRECTORY} \
		.
run:
	docker run -it ${IMG}:${IMG_TAG} /bin/bash

publish:
	docker run -it ${IMG}:${IMG_TAG} mvn clean deploy
