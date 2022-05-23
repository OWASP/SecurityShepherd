ARG MONGODB_VERSION

FROM docker AS builder

ARG MONGO_BIND_ADDRESS

USER root
WORKDIR /workdir
COPY target/moduleSchemas.js moduleSchemas.js

RUN { \
    		echo 'storage:'; \
    		echo '  dbPath: /var/lib/mongodb'; \
    		echo '    journal:'; \
    		echo '    enabled: true'; \
            echo 'systemLog:'; \
            echo '  destination: file'; \
            echo '  logAppend: true'; \
            echo '  path: /var/log/mongodb/mongod.log'; \
            echo 'net:'; \
            echo '  port: 27017'; \
            echo '  bindIp: '${MONGO_BIND_ADDRESS}; \
            echo 'processManagement:'; \
            echo '  timeZoneInfo: /usr/share/zoneinfo'; \
    } > mongod.conf

FROM mongo:${MONGODB_VERSION}
COPY --from=builder /workdir/moduleSchemas.js /docker-entrypoint-initdb.d/

ENV RUN_USER mongodb
ENV RUN_GROUP mongodb

COPY --from=builder /workdir/mongod.conf /etc/mongod.conf

RUN chown -R ${RUN_USER}:${RUN_GROUP} "/etc/mongod.conf"
RUN chown -R ${RUN_USER}:${RUN_GROUP} "/docker-entrypoint-initdb.d"

USER ${RUN_USER}
RUN sed -i 's/\/\/REMOVE/ /g' /docker-entrypoint-initdb.d/moduleSchemas.js

ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 27017
CMD ["mongod"]
