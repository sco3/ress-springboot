#!/bin/bash

webappdir=/opt/tnf/apps/ress-srv

if [ -d $webappdir ] ; then
   owner=`stat -c %U  $webappdir`
   echo ===== Shutdown Services
   if [ -f "$webappdir/ctl.sh" ] ; then
      sudo -u $owner bash $webappdir/ctl.sh stop
   fi
fi

exit 0