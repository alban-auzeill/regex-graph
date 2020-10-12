# Regex Graph

Convert a java regular expression pattern into a graph.

## Prerequisites
`/usr/bin/dot` should be available. It could be installed using:

```sh
$ sudo apt install graphviz
```

## Build
```sh
$ ./gradlew build
```

## Run
Using the command line:
```sh
$ ./gradlew run
```
Then open http://localhost:9000/ in your browser.

It's also possible to define customized server port and Graphviz dot path:
```sh
$ ./gradlew -Dport=8080 -Ddot=./path-to-dot/dot run
```
OR
```sh
$ export GRAPHVIZ_DOT="./path-to-dot/dot"
$ ./gradlew -Dport=8080 run
```

( using an IDE, execute `com.auzeill.regex.graph.Server` `main()` )

## Test

```sh
$ ./gradlew test
```

Or to fix automatically the content of all `src/test/resources/**/*.dot` files:

```sh
$ ./gradlew test -DfixTest=true
```
