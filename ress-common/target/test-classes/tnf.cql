CREATE KEYSPACE tnf WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};

CREATE TABLE tnf.cas_properties (
    name text PRIMARY KEY,
    value text
);

CREATE TABLE tnf.flyway_info (
    table_name text PRIMARY KEY,
    version text
);

use tnf;
insert into flyway_info (table_name, version) values ('cas_properties', '001.000');

insert into cas_properties (name, value) values ('hrcc.segments_by_imsi', '10');
insert into cas_properties (name, value) values ('metric.definition.all', '{"schema":"yes"}');
