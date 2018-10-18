FROM mysql:5.5

ARG MYSQL_ROOT_PASSWORD=$MYSQL_PASS
ARG CONTAINER_TOMCAT
ARG DOCKER_NETWORK_NAME

COPY coreSchema.sql /docker-entrypoint-initdb.d/coreSchema.sql
COPY moduleSchemas.sql /docker-entrypoint-initdb.d/moduleSchemas.sql

RUN sed -i 's/@'\''localhost'\''/@'\'''"$CONTAINER_TOMCAT"'.'"$DOCKER_NETWORK_NAME"''\''/g' /docker-entrypoint-initdb.d/moduleSchemas.sql

RUN mkdir -p /etc/mysql/conf.d \
	&& { \
		echo '[mysqld]'; \
		echo 'skip-host-cache'; \
		echo 'datadir = /var/lib/mysql'; \
		echo '!includedir /etc/mysql/conf.d/'; \
	} > /etc/mysql/my.cnf

ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 3306
CMD ["mysqld"]
