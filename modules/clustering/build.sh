#!/bin/sh

## to be run from the modules/clustering dir...

## I have Emacs menu options for switching JAVA_HOME, MAVEN_HOME,
## ANT_HOME so these need to be added to my PATH lazily...

PATH=$MAVEN_HOME/bin:$PATH
PATH=$JAVA_HOME/bin:$PATH
PATH=$ANT_HOME/bin:$PATH

export PATH
export JAVA_HOME
export MAVEN_HOME
export ANT_HOME

## rebuild clustering service
## install result into dev tree
## run dev tree
time maven --emacs clean build jar:install javadoc && \
cp target/*.jar ../../target/geronimo-DEV/lib/ && \
cp src/deploy/*.xml ../../target/geronimo-DEV/deploy/ && \
cd ../../ &&
time maven --emacs run:main
