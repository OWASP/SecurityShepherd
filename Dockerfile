FROM tomcat:alpine

ARG DB_DRIVER=org.gjt.mm.mysql.Driver
ARG DB_SCHEMA=core
ARG PROPS_DIR=/usr/local/tomcat/conf/database.properties

ARG MYSQL_USER
ARG MYSQL_PASS
ARG MYSQL_URI

RUN printf "databaseConnectionURL=$MYSQL_URI/\nDriverType=$DB_DRIVER\ndatabaseSchema=$DB_SCHEMA\ndatabaseUsername=$MYSQL_USER\ndatabasePassword=$MYSQL_PASS\n" >> $PROPS_DIR

RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY target/owaspSecurityShepherd.war /usr/local/tomcat/webapps/ROOT.war

RUN [ "keytool", "-genkey", "-alias", "tomcat", "-keyalg", "RSA", "-keystore", "$TLS_KEYSTORE_FILE", "-dname", "cn=OwaspShepherd, ou=Security Shepherd, o=OWASP, L=Baile Átha Cliath, ST=Laighin, C=IE", "-storepass", "$TLS_KEYSTORE_PASS", "-keypass", "$TLS_KEYSTORE_PASS", "-deststoretype", "pkcs12" ]