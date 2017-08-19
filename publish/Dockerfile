FROM java:8

ARG JTS_PUBLISH_TAG
ARG JTS_SONATYPE_SETTINGS
ARG JTS_GPG_KEY_DIRECTORY

USER root

RUN apt-get install git

RUN useradd -ms /bin/bash jts

USER jts

RUN set -x \
    && cd /home/jts \
    && git clone https://github.com/locationtech/jts /home/jts/jts \
    && cd /home/jts/jts \
    && git checkout ${JTS_TAG}

WORKDIR /home/jts/jts

COPY ${JTS_SONATYPE_SETTINGS} /home/jts/jts/settings.xml
COPY ${JTS_GPG_KEY_DIRECTORY} /home/geotrellis/.gnupg/

USER root

RUN chown -R jts:jts /home/jts

USER jts
