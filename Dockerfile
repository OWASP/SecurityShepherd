FROM tomcat:alpine

ENV DB_DRIVER org.gjt.mm.mysql.Driver
ENV DB_SCHEMA core
ENV PROPS_DIR /usr/local/tomcat/conf/database.properties

ARG MYSQL_USER
ARG MYSQL_PASS
ARG MYSQL_URI

RUN printf "databaseConnectionURL=$MYSQL_URI/\nDriverType=$DB_DRIVER\ndatabaseSchema=$DB_SCHEMA\ndatabaseUsername=$MYSQL_USER\ndatabasePassword=$MYSQL_PASS\n" >> $PROPS_DIR