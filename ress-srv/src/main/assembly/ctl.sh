#!/bin/bash

# Avoid root execution
user=`whoami`
owner=`stat -c %U $0`
if [ "$user" != "$owner" ] ; then
   echo "This script should be run by $owner user."
   exit 1
fi

SCRIPT=$0
SCRIPT_PATH=$( dirname $(readlink -f $0) )
cd $SCRIPT_PATH

SCRIPT_NAME="ress-srv"
PIDFILE=$SCRIPT_PATH/$SCRIPT_NAME.pid
STATUS=""
ERROR=0
PID=""

CLASSPATH=$SCRIPT_PATH/cfg:$SCRIPT_PATH/../cas-main-var/cfg:$SCRIPT_PATH/../cas-main/cas-agent/cfg:\
$SCRIPT_PATH/lib/*

#VM_PARAMETERS=" -Xmx4G -Dlog4j.configuration=file:$SCRIPT_PATH/cfg/log4j.xml "
VM_PARAMETERS=" -Xmx4G "

JAVA_HOME=/opt/tnf/java
JAVA="$JAVA_HOME/bin/java"

MAIN_CLASS="com.tnf.cas.web.daemon.CasWebDaemon"


help() {
	echo "usage: $0 (start|stop|status)"
}

restart() {
   $SCRIPT stop && $SCRIPT start
}

start() {
   is_server_running
   RUNNING=$?
   if [ $RUNNING -eq 1 ]; then
      echo "$0 : $STATUS"
   else
      cleanpid
      
      touch $SCRIPT_PATH/RUNNING
      #VM_PARAMETERS="$VM_PARAMETERS -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8787"
      nohup $JAVA -classpath "$CLASSPATH" $VM_PARAMETERS $MAIN_CLASS 1>>/dev/null 2>&1 &
   
      if [ $? -eq 0 ];  then
         PID=$!
         echo $PID>$PIDFILE
         sleep 5
      fi
      is_server_running
      RUNNING=$?
      if [ $RUNNING -eq 1 ]; then
         echo "$0 : $SCRIPT_NAME started"
      else
         echo "$0 : $SCRIPT_NAME could not be started"
         cleanpid
         ERROR=1
      fi
   fi
}

stop() {
   is_server_running
   RUNNING=$?
   if [ $RUNNING -eq 0 ]; then
      echo "$0 : $STATUS"
   else
   
      rm -f $SCRIPT_PATH/RUNNING
   
      sleep 5
      is_server_running
      RUNNING=$?
      COUNTER=11
      while [ $RUNNING -ne 0 ] && [ $COUNTER -ne 0 ]; do
         COUNTER=`expr $COUNTER - 1`
         sleep 5
         is_server_running
         RUNNING=$?
      done
      if [ $RUNNING -eq 0 ]; then
         echo "$0 : $SCRIPT_NAME stopped"
      else
         echo "$0 : $SCRIPT_NAME could not be stopped"
         echo "kill $SCRIPT_NAME (pid $PID)"
         kill -9 ${PID}
         ERROR=2
      fi
   fi
   cleanpid
}

is_server_running() {
   PID=""
   if [ -f $PIDFILE ] ; then
       PID=`cat $PIDFILE`
   fi
   if [ "x$PID" != "x" ] && kill -0 $PID 2>/dev/null ; then
      RUNNING=1
   else
      RUNNING=0
   fi
   if [ $RUNNING -eq 0 ]; then
      STATUS="$SCRIPT_NAME not running"
   else
      STATUS="$SCRIPT_NAME (pid $PID) already running"
   fi
   return $RUNNING
}

cleanpid() {
   rm -f $PIDFILE
   rm -f $SCRIPT_PATH/RUNNING
}

if [ "x$1" = "xstart" ]; then
   start
elif [ "x$1" = "xstop" ]; then
   stop
elif [ "x$1" = "xstatus" ]; then
    is_server_running
    echo "$0 : $STATUS"
elif [ "x$1" = "xrestart" ]; then
   restart
else
	help
fi

exit $ERROR