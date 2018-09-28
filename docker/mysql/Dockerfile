FROM mysql:5.5

ARG MYSQL_ROOT_PASSWORD=$MYSQL_PASS
ARG CONTAINER_TOMCAT
ARG DOCKER_NETWORK_NAME

COPY coreSchema.sql /docker-entrypoint-initdb.d/coreSchema.sql
COPY moduleSchemas.sql /docker-entrypoint-initdb.d/moduleSchemas.sql

RUN sed -i 's/@'\''localhost'\''/@'\'''"$CONTAINER_TOMCAT"'.'"$DOCKER_NETWORK_NAME"''\''/g' /docker-entrypoint-initdb.d/moduleSchemas.sql
# For 3.1 Closing NoSQL level
RUN sed -i 's/'\''nosql\.injection\.one'\'', '\''challenge'\'', '\''Injection'\'', '\''injection'\'', '\''c09f32d4c3dd5b75f04108e5ffc9226cd8840288a62bdaf9dc65828ab6eaf86a'\'', '\''d63c2fb5da9b81ca26237f1308afe54491d1bacf9fffa0b21a072b03c5bafe66'\'', '\''open/'\''nosql\.injection\.one'\'', '\''challenge'\'', '\''Injection'\'', '\''injection'\'', '\''c09f32d4c3dd5b75f04108e5ffc9226cd8840288a62bdaf9dc65828ab6eaf86a'\'', '\''d63c2fb5da9b81ca26237f1308afe54491d1bacf9fffa0b21a072b03c5bafe66'\'', '\''close/g' /docker-entrypoint-initdb.d/coreSchema.sql

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
