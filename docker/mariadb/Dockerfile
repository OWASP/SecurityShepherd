ARG DB_VERSION

FROM docker AS builder

ARG DB_BIND_ADDRESS

USER root
WORKDIR /workdir
COPY target/coreSchema.sql coreSchema.sql
COPY target/moduleSchemas.sql moduleSchemas.sql
RUN { \
    		echo '[mysqld]'; \
    		echo 'skip-host-cache'; \
    		echo 'datadir = /var/lib/mysql'; \
    		echo '!includedir /etc/mysql/conf.d/'; \
            echo 'ssl=0'; \
            echo 'bind-address='${DB_BIND_ADDRESS}; \
    	} > my.cnf


FROM mariadb:${DB_VERSION}
ARG MYSQL_ROOT_PASSWORD=$DB_PASS
ARG CONTAINER_TOMCAT
ARG DOCKER_NETWORK_NAME

COPY --from=builder /workdir/coreSchema.sql /docker-entrypoint-initdb.d/
COPY --from=builder /workdir/moduleSchemas.sql /docker-entrypoint-initdb.d/

ENV RUN_USER mysql
ENV RUN_GROUP mysql
ENV MYSQL_HOME "/etc/mysql"

RUN chown -R ${RUN_USER}:${RUN_GROUP} ${MYSQL_HOME}
RUN chown -R ${RUN_USER}:${RUN_GROUP} "/docker-entrypoint-initdb.d"

USER ${RUN_USER}
RUN sed -i 's/@'\''localhost'\''/@'\'''%''\''/g' /docker-entrypoint-initdb.d/moduleSchemas.sql
RUN mkdir -p /etc/mysql/conf.d
COPY --from=builder /workdir/my.cnf /etc/mysql/

ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 3306
CMD ["mysqld"]
