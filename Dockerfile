ARG TOMCAT_DOCKER_VERSION
FROM tomcat:${TOMCAT_DOCKER_VERSION}

ENV RUN_USER tomcat

RUN apt-get -qq update && apt-get install -y patch

RUN adduser --system --group ${RUN_USER} --home ${CATALINA_HOME}
RUN chown -R ${RUN_USER}:${RUN_GROUP} $CATALINA_HOME
USER ${RUN_USER}

ARG DB_DRIVER=org.gjt.mm.mysql.Driver
ARG DB_SCHEMA=core
ARG PROPS_MYSQL=/usr/local/tomcat/conf/database.properties
ARG PROPS_MONGO=/usr/local/tomcat/conf/mongo.properties

ARG MYSQL_USER
ARG MYSQL_PASS
ARG MYSQL_URI

ARG MONGO_HOST
ARG MONGO_PORT
ARG MONGO_CONN_TIMEOUT
ARG MONGO_SOCK_TIMEOUT
ARG MONGO_SVR_TIMEOUT

ARG TLS_KEYSTORE_FILE
ARG TLS_KEYSTORE_PASS
ARG ALIAS
ARG HTTPS_PORT

RUN printf "databaseConnectionURL=$MYSQL_URI/\nDriverType=$DB_DRIVER\ndatabaseSchema=$DB_SCHEMA\ndatabaseUsername=$MYSQL_USER\ndatabasePassword=$MYSQL_PASS\ndatabaseOptions=useUnicode=true&character_set_server=utf8mb4\n" >> $PROPS_MYSQL
RUN printf "connectionHost=$MONGO_HOST\nconnectionPort=$MONGO_PORT\ndatabaseName=shepherdGames\nconnectTimeout=$MONGO_CONN_TIMEOUT\nsocketTimeout=$MONGO_SOCK_TIMEOUT\nserverSelectionTimeout=$MONGO_SVR_TIMEOUT"  >> $PROPS_MONGO

RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY target/owaspSecurityShepherd.war /usr/local/tomcat/webapps/ROOT.war
COPY target/docker/tomcat/$TLS_KEYSTORE_FILE /usr/local/tomcat/conf/$TLS_KEYSTORE_FILE

COPY docker/tomcat/serverxml.patch /usr/local/tomcat/conf/serverxml.patch
RUN sed -i 's/keystoreFile="conf\/TLS_KEYSTORE_FILE" keystorePass="TLS_KEYSTORE_PASS" keyAlias="ALIAS">/keystoreFile="conf\/'"$TLS_KEYSTORE_FILE"'" keystorePass="'"$TLS_KEYSTORE_PASS"'" keyAlias="'"$ALIAS"'">/g' /usr/local/tomcat/conf/serverxml.patch &&\
    sed -i 's/redirectPort="HTTPS_PORT" \/>/redirectPort="'"$HTTPS_PORT"'" \/>/g' /usr/local/tomcat/conf/serverxml.patch &&\
    patch /usr/local/tomcat/conf/server.xml /usr/local/tomcat/conf/serverxml.patch

COPY docker/tomcat/webxml.patch /usr/local/tomcat/conf/webxml.patch
RUN patch /usr/local/tomcat/conf/web.xml /usr/local/tomcat/conf/webxml.patch

EXPOSE 8080 8443
CMD ["catalina.sh", "run"]
