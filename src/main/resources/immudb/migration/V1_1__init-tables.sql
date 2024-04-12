---
--- creating immudb audit tables
---


---
--- auditing_dmp
---
exec CREATE TABLE auditing_dmp(_id VARCHAR[128] NOT NULL,user_id VARCHAR[128] NOT NULL,api VARCHAR[128] NOT NULL,method VARCHAR[32] NOT NULL,info VARCHAR[1024] NOT NULL, epoch_time INTEGER NOT NULL, iso_time VARCHAR[64] NOT NULL, PRIMARY KEY _id);


---
--- auditing_consent
---
exec create table auditing_consent (_id varchar[128] not null,item_id varchar not null,item_type varchar not null,event varchar not null,aiu_id varchar not null,aip_id varchar not null,dp_id varchar not null,isotime varchar not null,artifact varchar not null,shalog varchar not null,primary key _id);

---
--- auditing_acl_apd
-- This is not the current schema used in Dev instance of immudb, to create
-- this schema, data from the old table could be migrated here
---
exec CREATE TABLE auditing_acl_apd(id VARCHAR[256] NOT NULL, userid VARCHAR[128] NOT NULL,endpoint VARCHAR[128] NOT NULL,method VARCHAR[128] NOT NULL,body VARCHAR[2048] NOT NULL,size INTEGER NOT NULL,isotime VARCHAR[128] NOT NULL,epochtime INTEGER NOT NULL,PRIMARY KEY id);

---
--- rsaudit
---
exec CREATE TABLE rsaudit (id VARCHAR(128) PRIMARY KEY, api VARCHAR(128) NOT NULL, userid VARCHAR(128) NOT NULL, epochtime INTEGER NOT NULL, resourceid VARCHAR(256) NOT NULL, isotime VARCHAR(64) NOT NULL, providerid VARCHAR(128) NOT NULL, size INTEGER);

---
--- auditingtable
---
exec CREATE TABLE auditingtable (id VARCHAR(128) PRIMARY KEY, userrole VARCHAR(64) NOT NULL, userid VARCHAR(128) NOT NULL, iid VARCHAR(250) NOT NULL, api VARCHAR(128) NOT NULL, method VARCHAR(32) NOT NULL, time INTEGER NOT NULL, iudxid VARCHAR(256) NOT NULL);


---
--- creating index for the tables
---

---
--- auditing_dmp
---
exec CREATE INDEX IF NOT EXISTS ON auditing_dmp(api, method, user_id, epoch_time);

---
--- auditing_acl_apd
---
exec CREATE INDEX IF NOT EXISTS ON auditing_acl(endpoint, method, userid, epochtime);

---
--- rsaudit
---
CREATE INDEX IF NOT EXISTS index_name ON rsaudit(userid, providerid);


---
--- auditingtable
---
CREATE INDEX IF NOT EXISTS index_name ON auditingtable(userid, api);

