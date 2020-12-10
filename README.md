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
$ ./gradlew run -Dport=8080 -Ddot=./path-to-dot/dot
```
OR
```sh
$ export GRAPHVIZ_DOT="./path-to-dot/dot"
$ ./gradlew run -Dport=8080
```

( using an IDE, execute `com.auzeill.regex.graph.Server` `main()` )

## Generate images
```sh
$ ./gradlew run -Dgenerate=regex-list.txt
```
The regex list file format is, one line per images:
<target_file_name>:<format>:<quoted_regex>

- target_file_name: end with ".png"
- type: L|P|T|A|TA(,left-margin,right-margin,top-margin,bottom-margin)?
    - For example A or TA,10,10,50,30
    - L: Create the legend
    - P: JVM Pattern
    - T: AST Tree
    - A: Automaton States
    - TA: AST Tree + Automaton States
    - ,left-margin,right-margin,top-margin,bottom-margin: crop a portion of the generated image
- quoted_regex: "..."

for example:
```
# this is a comment
a-start.png:A:"a*"
a-plus.png:TA,10,10,50,30:"a+"
```

## Test

```sh
$ ./gradlew test
```

Or to fix automatically the content of all `src/test/resources/**/*.dot` files:

```sh
$ ./gradlew test -DfixTest=true
```
