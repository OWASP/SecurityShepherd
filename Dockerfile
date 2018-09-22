FROM tomcat:alpine

ARG DB_DRIVER=org.gjt.mm.mysql.Driver
ARG DB_SCHEMA=core
ARG PROPS_DIR=/usr/local/tomcat/conf/database.properties

ARG MYSQL_USER
ARG MYSQL_PASS
ARG MYSQL_URI

ARG TLS_KEYSTORE_FILE
ARG TLS_KEYSTORE_PASS
ARG ALIAS
ARG HTTPS_PORT

RUN printf "databaseConnectionURL=$MYSQL_URI/\nDriverType=$DB_DRIVER\ndatabaseSchema=$DB_SCHEMA\ndatabaseUsername=$MYSQL_USER\ndatabasePassword=$MYSQL_PASS\n" >> $PROPS_DIR

RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY target/owaspSecurityShepherd.war /usr/local/tomcat/webapps/ROOT.war
COPY target/$TLS_KEYSTORE_FILE /usr/local/tomcat/conf/$TLS_KEYSTORE_FILE

COPY docker/serverxml.patch /usr/local/tomcat/conf/serverxml.patch
RUN sed -i 's/keystoreFile="conf\/TLS_KEYSTORE_FILE" keystorePass="TLS_KEYSTORE_PASS" keyAlias="ALIAS"\/>/keystoreFile="conf\/'"$TLS_KEYSTORE_FILE"'" keystorePass="'"$TLS_KEYSTORE_PASS"'" keyAlias="'"$ALIAS"'"\/>/g' /usr/local/tomcat/conf/serverxml.patch &&\
    sed -i 's/redirectPort="HTTPS_PORT" \/>/redirectPort="'"$HTTPS_PORT"'" \/>/g' /usr/local/tomcat/conf/serverxml.patch &&\
    patch /usr/local/tomcat/conf/server.xml /usr/local/tomcat/conf/serverxml.patch


COPY docker/webxml.patch /usr/local/tomcat/conf/webxml.patch
RUN patch /usr/local/tomcat/conf/web.xml /usr/local/tomcat/conf/webxml.patch

EXPOSE 8080 8443
CMD ["catalina.sh", "run"]