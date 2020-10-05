#!/usr/bin/env bash

./gradlew build --stacktrace --warning-mode all &&
  java --add-opens java.base/java.util.regex=ALL-UNNAMED --illegal-access=warn -classpath build/libs/regex-graph-1.0-SNAPSHOT.jar com.auzeill.regex.graph.RegExGraph "$@" > out/graph.dot &&
  dot -Tsvg -oout/graph.svg out/graph.dot



