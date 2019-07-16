FROM mongo:${MONGODB_VERSION}
COPY target/moduleSchemas.js /docker-entrypoint-initdb.d/moduleSchemas.js

RUN sed -i 's/\/\/REMOVE/ /g' /docker-entrypoint-initdb.d/moduleSchemas.js

ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 27017
CMD ["mongod"]
