---
--- creating immudb audit tables
---


---
--- auditing_dmp
---
 CREATE TABLE auditing_dmp(_id VARCHAR[128] NOT NULL,user_id VARCHAR[128] NOT NULL,api VARCHAR[128] NOT NULL,method VARCHAR[32] NOT NULL,info VARCHAR[1024] NOT NULL, epoch_time INTEGER NOT NULL, iso_time VARCHAR[64] NOT NULL, PRIMARY KEY _id);


---
--- auditing_consent
---
create table auditing_consent (_id varchar[128] not null,item_id varchar[256] not null,item_type varchar[128] not null,event varchar[256] not null,aiu_id varchar[128] not null,aip_id varchar[256] not null,dp_id varchar[128] not null,isotime varchar[128] not null,artifact varchar[128] not null,shalog varchar[2046] not null,primary key _id);
---
--- auditing_acl_apd
---

 CREATE TABLE auditing_acl_apd( id VARCHAR[128] NOT NULL, userid VARCHAR[128] NOT NULL, endpoint VARCHAR[128] NOT NULL, method VARCHAR[128] NOT NULL, body VARCHAR[2048] NOT NULL,size INTEGER NOT NULL,isotime VARCHAR[128] NOT NULL, epochtime INTEGER NOT NULL, PRIMARY KEY id);

---
--- rsaudit
---
 CREATE TABLE rsaudit (id VARCHAR[128] NOT NULL,api VARCHAR[128] NOT NULL,userid VARCHAR[128] NOT NULL,epochtime INTEGER NOT NULL,resourceid VARCHAR[256] NOT NULL,isotime VARCHAR[64] NOT NULL,providerid VARCHAR[128] NOT NULL,size INTEGER, PRIMARY KEY id);

---
--- auditingtable
---
 CREATE TABLE auditingtable (id VARCHAR[128] NOT NULL, userRole VARCHAR[64] NOT NULL,userID VARCHAR[128] NOT NULL,iid VARCHAR[250] NOT NULL,api VARCHAR[128] NOT NULL,method VARCHAR[32] NOT NULL,time INTEGER NOT NULL,iudxID VARCHAR[256] NOT NULL,PRIMARY KEY id);


---
--- auditing_ogc
---

CREATE TABLE auditing_ogc (id varchar[128] not null,userid varchar[128] not null,api varchar[512] not null,resourceid varchar[128] not null,providerid varchar[128] not null, resource_group varchar[128] not null,epochtime Integer not null,isotime varchar[128] not null,size Integer not null, PRIMARY KEY id);
---
--- creating index for the tables
---

---
--- auditing_dmp
---
 CREATE INDEX IF NOT EXISTS ON auditing_dmp(api, method, user_id, epoch_time);

---
--- auditing_acl_apd
---
 CREATE INDEX IF NOT EXISTS ON auditing_acl_apd(userid,endpoint,epochtime);

---
--- rsaudit
---
CREATE INDEX IF NOT EXISTS ON rsaudit(userid, epochtime, providerid);


---
--- auditingtable
---
CREATE INDEX IF NOT EXISTS ON auditingtable(userID, iudxID, time);

---
--- auditing_consent
---
CREATE INDEX IF NOT EXISTS ON auditing_consent(item_id, aiu_id, dp_id);

---
--- auditing_ogc
---
CREATE INDEX IF NOT EXISTS ON auditing_ogc (userid, providerid, resourceid, epochtime);