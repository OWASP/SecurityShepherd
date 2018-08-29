FROM mysql:5.5

ARG MYSQL_ROOT_PASSWORD

COPY coreSchema.sql /docker-entrypoint-initdb.d/coreSchema.sql
COPY moduleSchemas.sql /docker-entrypoint-initdb.d/moduleSchemas.sql

RUN sed -i 's/-- DELIMITER \$\$/DELIMITER \$\$/g' /docker-entrypoint-initdb.d/coreSchema.sql &&\
        sed -i 's/-- \$\$/\$\$/g' /docker-entrypoint-initdb.d/coreSchema.sql &&\
        sed -i 's/-- DELIMITER \;/DELIMITER \;/g' /docker-entrypoint-initdb.d/coreSchema.sql

RUN sed -i 's/-- DELIMITER \$\$/DELIMITER \$\$/g' /docker-entrypoint-initdb.d/moduleSchemas.sql &&\
        sed -i 's/-- \$\$/\$\$/g' /docker-entrypoint-initdb.d/moduleSchemas.sql &&\
        sed -i 's/-- DELIMITER \;/DELIMITER \;/g' /docker-entrypoint-initdb.d/moduleSchemas.sql &&\
        sed -i 's/@'\''localhost'\''/@'\''secshep_tomcat.securityshepherd_default'\''/g' /docker-entrypoint-initdb.d/moduleSchemas.sql

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
