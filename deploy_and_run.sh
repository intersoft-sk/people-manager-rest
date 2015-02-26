#!/bin/sh

rm -rf tomcat/webapps/pmrest/
rm -rf tomcat/webapps/pmrest.war
cp -r target/pmrest.war tomcat/webapps

sh tomcat/bin/catalina.sh run
