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
time maven --emacs clean javadoc && \
cd ../../ && \
time maven --emacs -Dmodules=clustering build run:main
