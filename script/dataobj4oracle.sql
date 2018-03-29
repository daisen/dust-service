-- dataobj
--
-- create by hst
-- ------------------------------------------------------
-- version 1.0



CREATE TABLE dataobj (
  id number(20)  NOT NULL ,
  gmt_create date DEFAULT sysdate,
  gmt_modified date DEFAULT sysdate,
  alias varchar2(50) NOT NULL,
  app varchar2(20) NOT NULL,
  module varchar2(20) NOT NULL,
  name varchar2(50) NOT NULL,
  table_name varchar2(50) NOT NULL,
  conditions varchar2(500) DEFAULT NULL,
  fix_condition varchar2(100) DEFAULT NULL,
  orders varchar2(100) DEFAULT NULL,
  start number(19)  DEFAULT 0 NOT NULL,
  page_size number(19)  DEFAULT 0 NOT NULL,
  fix_where_sql varchar2(500) DEFAULT NULL,
  order_by_sql varchar2(500) DEFAULT NULL,
  where_sql varchar2(500) DEFAULT NULL,
  constraint PK_dataobj PRIMARY KEY (id)
) ; 


CREATE TABLE dataobj_column (
  id number(20)  NOT NULL ,
  gmt_create date DEFAULT sysdate,
  gmt_modified date DEFAULT sysdate,
  master_id number(20)  DEFAULT NULL,
  name varchar2(50) NOT NULL,
  column_name varchar2(50) NOT NULL,
  column_label varchar2(50) NULL,
  relation_table_name varchar2(50) NULL,
  relation_column_name varchar2(50) NULL,
  id_column_label varchar2(50) NULL,
  data_type varchar2(20) NOT NULL,
  mirror_column_label varchar2(50) DEFAULT NULL,  
  table_name varchar2(50) NOT NULL,
  default_value varchar2(100) NULL,
  is_primary_key number(1)  DEFAULT 0 NOT NULL,
  is_ignore number(1)  DEFAULT 0 NOT NULL,
  width number(19)  DEFAULT 0 NOT NULL,
  decimal_digits number(19)  DEFAULT 0 NOT NULL,
  is_required number(1)  DEFAULT 0 NOT NULL,
  is_ number(1)  DEFAULT 0 NOT NULL,
  constraint PK_dataobj_column PRIMARY KEY (id)
) ; 


CREATE TABLE dataobj_table (
  id number(20)  NOT NULL ,
  gmt_create date DEFAULT sysdate,
  gmt_modified date DEFAULT sysdate,
  master_id number(20)  NOT NULL,
  table_name varchar2(50) NOT NULL,
  relation_type varchar2(20) DEFAULT 'LEFT',
  column_name varchar2(20) DEFAULT NULL,
  relation_column varchar2(20) DEFAULT NULL,
  relation_where varchar2(100) DEFAULT NULL,
  follow_delete number(1) DEFAULT 0,
  follow_insert number(1) DEFAULT 0,
  follow_update number(1) DEFAULT 0,
  constraint PK_dataobj_table PRIMARY KEY (id)  
) ; 


CREATE TABLE dataobj_big_text (
  id number(20)  NOT NULL ,
  gmt_create date DEFAULT sysdate,
  gmt_modified date DEFAULT sysdate,
  code varchar2(50) NOT NULL,
  content text DEFAULT NULL,
  constraint PK_dataobj_big_text PRIMARY KEY (id)  
) ; 


CREATE TABLE dataobj_app (
  id number(20)  NOT NULL ,
  gmt_create date DEFAULT sysdate,
  gmt_modified date DEFAULT sysdate,
  app varchar2(20) NOT NULL,
  description text DEFAULT NULL,
  constraint PK_dataobj_app PRIMARY KEY (id)  
) ; 


CREATE TABLE dataobj_module (
  id number(20)  NOT NULL ,
  gmt_create date DEFAULT sysdate,
  gmt_modified date DEFAULT sysdate,
  master_id number(20)  NOT NULL,
  module varchar2(20) NOT NULL,
  description clob DEFAULT NULL,
  constraint PK_dataobj_module PRIMARY KEY (id)
) ; 