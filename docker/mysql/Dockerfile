FROM mysql:5.5
ENV MYSQL_ROOT_PASSWORD=CowSaysMoo
COPY coreSchema.sql /docker-entrypoint-initdb.d/coreSchema.sql
COPY moduleSchemas.sql /docker-entrypoint-initdb.d/moduleSchemas.sql

ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 3306
CMD ["mysqld"]
