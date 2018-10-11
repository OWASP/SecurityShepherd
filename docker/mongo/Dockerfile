ARG MONGODB_VERSION
FROM mongo:${MONGODB_VERSION}

ENV RUN_USER mongodb
ENV RUN_GROUP mongodb

RUN chown -R ${RUN_USER}:${RUN_GROUP} "/etc/mongod.conf.orig"
RUN chown -R ${RUN_USER}:${RUN_GROUP} "/docker-entrypoint-initdb.d"

USER ${RUN_USER}

COPY target/moduleSchemas.js /docker-entrypoint-initdb.d/moduleSchemas.js

RUN sed -i 's/\/\/REMOVE/ /g' /docker-entrypoint-initdb.d/moduleSchemas.js

ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 27017
CMD ["mongod"]
