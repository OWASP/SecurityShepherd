ARG MONGODB_VERSION

FROM docker AS builder
USER root
WORKDIR /workdir
COPY target/moduleSchemas.js moduleSchemas.js

FROM mongo:${MONGODB_VERSION}
COPY --from=builder /workdir/moduleSchemas.js /docker-entrypoint-initdb.d/

ENV RUN_USER mongodb
ENV RUN_GROUP mongodb

RUN chown -R ${RUN_USER}:${RUN_GROUP} "/etc/mongod.conf.orig"
RUN chown -R ${RUN_USER}:${RUN_GROUP} "/docker-entrypoint-initdb.d"

USER ${RUN_USER}
RUN sed -i 's/\/\/REMOVE/ /g' /docker-entrypoint-initdb.d/moduleSchemas.js

ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 27017
CMD ["mongod"]
