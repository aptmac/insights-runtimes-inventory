####
# This Dockerfile is used in order to run tests for the Jenkinsfile pipeline PR checks
###

FROM registry.access.redhat.com/ubi8/openjdk-17:latest as base
USER root
COPY . /home/jboss
WORKDIR /home/jboss

FROM base as unit-test
RUN ./mvnw clean package -P coverage --no-transfer-progress
RUN mkdir -p artifacts
RUN cp core/target/surefire-reports/TEST-*.xml artifacts
RUN cp events/target/surefire-reports/TEST-*.xml artifacts
RUN cp coverage/target/site/jacoco-aggregate/jacoco.xml artifacts
