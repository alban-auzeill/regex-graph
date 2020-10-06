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

( using an IDE, execute `com.auzeill.regex.graph.Server` `main()` )
