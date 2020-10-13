#!/usr/bin/env bash

find src/test/resources -type f -name "*.dot" -delete
./gradlew --stacktrace build -DfixTest=true
find src/test/resources -type f -not -name "*.txt" -not -name "*.dot"
