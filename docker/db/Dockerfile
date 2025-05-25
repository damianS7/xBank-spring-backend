FROM postgres:17.4
COPY docker/db/sql/schema.sql /docker-entrypoint-initdb.d/01-schema.sql
COPY docker/db/sql/data.sql /docker-entrypoint-initdb.d/02-data.sql
EXPOSE  5432