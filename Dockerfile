ARG TOMCAT_DOCKER_VERSION


FROM docker AS builder
ARG TLS_KEYSTORE_FILE
ARG TLS_KEYSTORE_PASS
ARG ALIAS
ARG HTTPS_PORT
ARG DB_DRIVER=org.mariadb.jdbc.Driver
ARG DB_SCHEMA=core
ARG DB_USER
ARG DB_PASS
ARG MARIADB_URI
ARG MONGO_HOST
ARG MONGO_PORT
ARG MONGO_CONN_TIMEOUT
ARG MONGO_SOCK_TIMEOUT
ARG MONGO_SVR_TIMEOUT

USER root
WORKDIR /workdir

COPY target/owaspSecurityShepherd.war ROOT.war
COPY target/docker/tomcat/$TLS_KEYSTORE_FILE $TLS_KEYSTORE_FILE
COPY docker/tomcat/serverxml.patch serverxml.patch
COPY docker/tomcat/webxml.patch webxml.patch

RUN printf "databaseConnectionURL=$MARIADB_URI/\nDriverType=$DB_DRIVER\ndatabaseSchema=$DB_SCHEMA\ndatabaseUsername=$DB_USER\ndatabasePassword=$DB_PASS\ndatabaseOptions=useUnicode=true&character_set_server=utf8mb4\n" >> database.properties
RUN printf "connectionHost=$MONGO_HOST\nconnectionPort=$MONGO_PORT\ndatabaseName=shepherdGames\nconnectTimeout=$MONGO_CONN_TIMEOUT\nsocketTimeout=$MONGO_SOCK_TIMEOUT\nserverSelectionTimeout=$MONGO_SVR_TIMEOUT"  >> mongo.properties
RUN sed -i 's/keystoreFile="conf\/TLS_KEYSTORE_FILE" keystorePass="TLS_KEYSTORE_PASS" keyAlias="ALIAS">/keystoreFile="conf\/'"$TLS_KEYSTORE_FILE"'" keystorePass="'"$TLS_KEYSTORE_PASS"'" keyAlias="'"$ALIAS"'">/g' serverxml.patch &&\
    sed -i 's/redirectPort="HTTPS_PORT" \/>/redirectPort="'"$HTTPS_PORT"'" \/>/g' serverxml.patch

FROM tomcat:${TOMCAT_DOCKER_VERSION}
COPY --from=builder /workdir/ROOT.war /usr/local/tomcat/webapps/
COPY --from=builder /workdir/$TLS_KEYSTORE_FILE /usr/local/tomcat/conf/
COPY --from=builder /workdir/serverxml.patch /usr/local/tomcat/conf/
COPY --from=builder /workdir/webxml.patch /usr/local/tomcat/conf/
COPY --from=builder /workdir/database.properties /usr/local/tomcat/conf/
COPY --from=builder /workdir/mongo.properties /usr/local/tomcat/conf/

ENV RUN_USER tomcat
RUN apt-get -qq update && apt-get install -y patch libargon2-0
RUN adduser --system --group ${RUN_USER} --home ${CATALINA_HOME}
RUN chown -R ${RUN_USER}:${RUN_GROUP} $CATALINA_HOME
USER ${RUN_USER}

RUN rm -rf /usr/local/tomcat/webapps/ROOT
RUN patch /usr/local/tomcat/conf/server.xml /usr/local/tomcat/conf/serverxml.patch
RUN patch /usr/local/tomcat/conf/web.xml /usr/local/tomcat/conf/webxml.patch

EXPOSE 8080 8443
CMD ["catalina.sh", "run"]
