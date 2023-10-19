#!/bin/bash

DOCKER_CONF="$PWD/.docker"

# run integration tests
docker --config="$DOCKER_CONF" build -t insights-runtimes-inventory/itest:latest -f test-scripts/Dockerfile --target base .
docker --config="$DOCKER_CONF" run -v /var/run/docker.sock:/var/run/docker.sock insights-runtimes-inventory/itest:latest \
    /home/jboss/mvnw clean verify -P coverage --no-transfer-progress; \
    mkdir -p artifacts; cp events/target/failsafe-reports/TEST-*.xml artifacts; \
    cp rest/target/failsafe-reports/TEST-*.xml artifacts; \
    cp coverage/target/site/jacoco-aggregate/jacoco.xml artifacts/it-jacoco.xml
result=$?

if [ $result -eq 0 ]; then
    # Retrieve the results of the unit tests from a container
    id=$(docker --config="$DOCKER_CONF" create insights-runtimes-inventory/itest)
    docker --config="$DOCKER_CONF" cp $id:/home/jboss/artifacts ./
    docker --config="$DOCKER_CONF" rm -v $id

    # If your unit tests store junit xml results, you should store them in a file matching format `artifacts/junit-*.xml`
    for report in $(ls artifacts); do
        if [[ $report == TEST* ]]; then
            mv artifacts/$report artifacts/junit-$report;
        fi
    done
else
    echo '============================================'
    echo '====  ✖ ERROR: INTEGRATION TEST FAILED  ===='
    echo '============================================'
    exit 1
fi