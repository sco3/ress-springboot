#!/bin/bash


stat -c %U:%G /opt/tnf > /tmp/opt-tnf-ownership

JAVA_HOME=/opt/tnf/java
JAVA="$JAVA_HOME/bin/java"

if [ ! -f $JAVA ] ; then
echo $JAVA not found - please install JRE at $JAVA_HOME
   exit 100
fi
