CREATE ROLE myuniversity PASSWORD 'myuniversity' NOSUPERUSER NOCREATEDB INHERIT LOGIN;

CREATE SCHEMA myuniversity_mymodule AUTHORIZATION myuniversity;

CREATE TABLE myuniversity_mymodule.item (_id SERIAL PRIMARY KEY, jsonb JSONB NOT NULL);

GRANT ALL ON myuniversity_mymodule.item TO myuniversity;
GRANT ALL ON myuniversity_mymodule.item__id_seq TO myuniversity;

CREATE TABLE myuniversity_mymodule.instance (_id SERIAL PRIMARY KEY, jsonb JSONB NOT NULL);

GRANT ALL ON myuniversity_mymodule.instance TO myuniversity;
GRANT ALL ON myuniversity_mymodule.instance__id_seq TO myuniversity;