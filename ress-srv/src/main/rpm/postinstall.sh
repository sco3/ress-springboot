#!/bin/bash


chown -R $(cat /tmp/opt-tnf-ownership) /opt/tnf

if [ "x$TNF_USER" == "x" ] ; then
   owner="boss"
else
   owner=$TNF_USER
fi
echo owner: $owner

getent group $owner >/dev/null || groupadd $owner
id=$( getent passwd  $owner )
if [ "x$id" == "x" ] ; then
   echo make user $owner
   useradd -g $owner $owner
   echo $owner:$owner | chpasswd
fi

appdir=/opt/tnf/apps/cas-main
webappdir=/opt/tnf/apps/ress-srv

echo ===== Check Java
JAVA_HOME=/opt/tnf/java
JAVA="$JAVA_HOME/bin/java"

UTIL_COMMAND="$JAVA -cp $webappdir/lib/postinstall-util.jar"

echo ===== Postinstall actions

if [ ! -d $appdir-var/cfg ] ; then
   echo ===== Add $appdir-var/cfg
   mkdir -p $appdir-var/cfg
fi
cd $webappdir/cfg
for i_f in *.template ; do
   o_f=`echo $i_f | sed 's/.template//g'`
   if [ ! -f $appdir-var/cfg/$o_f ] ; then
      cp $i_f $appdir-var/cfg/$o_f
   fi
done

echo ===== SSL Keystore 

keystore=/opt/tnf/apps/cas-main-var/cfg/server-keystore.jks
if [ ! -f $keystore ] ; then
$JAVA_HOME/bin/keytool \
   -genkeypair         \
   -keyalg RSA         \
   -alias     server   \
   -keypass   server   \
   -storepass server   \
   -keystore $keystore \
   -dname CN=$(hostname -f)
fi

echo ===== Change owner to $owner for $webappdir

chown -R $owner:$owner $webappdir
chown -R $owner:$owner $appdir-var
