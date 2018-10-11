ARG MYSQL_VERSION
FROM mysql:${MYSQL_VERSION}

ENV RUN_USER mysql
ENV RUN_GROUP mysql
ENV MYSQL_HOME "/etc/mysql"

RUN chown -R ${RUN_USER}:${RUN_GROUP} ${MYSQL_HOME}
RUN chown -R ${RUN_USER}:${RUN_GROUP} "/docker-entrypoint-initdb.d"

USER ${RUN_USER}

ARG MYSQL_ROOT_PASSWORD=$MYSQL_PASS
ARG CONTAINER_TOMCAT
ARG DOCKER_NETWORK_NAME

COPY target/coreSchema.sql /docker-entrypoint-initdb.d/coreSchema.sql
COPY target/moduleSchemas.sql /docker-entrypoint-initdb.d/moduleSchemas.sql

RUN sed -i 's/@'\''localhost'\''/@'\'''%''\''/g' /docker-entrypoint-initdb.d/moduleSchemas.sql

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
