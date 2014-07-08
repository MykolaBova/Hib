#!/bin/bash - 

SRC=$(pwd)"/../src"
find $SRC -name "*.class" -exec rm {} \;
export CLASSPATH=".:"$(pwd)"/../locallib/sqlite-jdbc-3.7.15-M1.jar:"$SRC
javac -Xlint:unchecked $1".java" && java $1
find $SRC -name "*.class" -exec rm {} \;
