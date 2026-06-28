create keyspace if not exists cc with replication = {'class':'SimpleStrategy', 'replication_factor':'1'};
use cc;
CREATE TABLE cc (imsi text, msisdn text, age int, PRIMARY KEY(imsi));
INSERT INTO cc (imsi, msisdn , age ) values('1','2',1);

create table cci (timeid bigint, imsi bigint, msisdn bigint,simsi text, stimeid text, primary key (timeid,imsi));
insert into cci (timeid, imsi, msisdn, simsi, stimeid) values (1,2,3, 's1','s2'); 

create table bson (id bigint primary key, bson blob);
insert into bson json '{ "id":1, "bson":"0x13000000106100010000001062000200000000"}';



