FROM mongo:4.1
COPY mongoSchema.js /docker-entrypoint-initdb.d/mongoSchema.js

ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 27017
CMD ["mongod"]
