-- DATAOBJ 数据对象表
-- create by hst 2018-04-10
Declare
  vi_Num  integer;
  vi_ColNum integer;
Begin
  select count(1) into vi_Num from user_tables where table_name = upper('DATAOBJ'); 
  if vi_Num = 0 then
    execute immediate(' 
      CREATE TABLE DATAOBJ(                                                   
		  ID            NUMBER(20) NOT NULL,
		  GMT_CREATE    DATE DEFAULT SYSDATE,
		  GMT_MODIFIED  DATE DEFAULT SYSDATE,
		  ALIAS         VARCHAR2(50) NOT NULL,
		  APP           VARCHAR2(20) NOT NULL,
		  MODULE        VARCHAR2(20) NOT NULL,
		  NAME          VARCHAR2(50) NOT NULL,
		  TABLE_NAME    VARCHAR2(50) NOT NULL,
		  CONDITIONS    VARCHAR2(500),
		  FIX_CONDITION VARCHAR2(100),
		  ORDERS        VARCHAR2(100),
		  START_INDEX   NUMBER(19) DEFAULT 0 NOT NULL,
		  PAGE_SIZE     NUMBER(19) DEFAULT 0 NOT NULL,
		  FIX_WHERE_SQL VARCHAR2(500),
		  ORDER_BY_SQL  VARCHAR2(500),
		  WHERE_SQL     VARCHAR2(500)
		)
    ');
	execute immediate(' ALTER TABLE DATAOBJ ADD CONSTRAINT PK_DATAOBJ PRIMARY KEY (ID)');
	execute immediate(' CREATE UNIQUE INDEX IDX_DATAOBJ_ALIAS_APP ON DATAOBJ(APP, ALIAS) ');
	execute immediate(' ANALYZE TABLE DATAOBJ COMPUTE STATISTICS ');
  end if;  
End;
/

-- DATAOBJ_COLUMN 数据对象列信息表
-- create by hst 2018-04-10
Declare
  vi_Num  integer;
  vi_ColNum integer;
Begin
  select count(1) into vi_Num from user_tables where table_name = upper('DATAOBJ_COLUMN'); 
  if vi_Num = 0 then
    execute immediate(' 
      CREATE TABLE DATAOBJ_COLUMN(                                                   
		  ID                   NUMBER(20) NOT NULL,
		  GMT_CREATE           DATE DEFAULT SYSDATE,
		  GMT_MODIFIED         DATE DEFAULT SYSDATE,
		  MASTER_ID            NUMBER(20),
		  NAME                 VARCHAR2(50) NOT NULL,
		  COLUMN_NAME          VARCHAR2(50) NOT NULL,
		  COLUMN_LABEL         VARCHAR2(50),
		  RELATION_TABLE_NAME  VARCHAR2(50),
		  RELATION_COLUMN_NAME VARCHAR2(50),
		  ID_COLUMN_LABEL      VARCHAR2(50),
		  DATA_TYPE            VARCHAR2(20) NOT NULL,
		  MIRROR_COLUMN_LABEL  VARCHAR2(50),
		  TABLE_NAME           VARCHAR2(50) NOT NULL,
		  DEFAULT_VALUE        VARCHAR2(100),
		  IS_PRIMARY_KEY       NUMBER(1) DEFAULT 0 NOT NULL,
		  IS_IGNORE            NUMBER(1) DEFAULT 0 NOT NULL,
		  WIDTH                NUMBER(19) DEFAULT 0 NOT NULL,
		  DECIMAL_DIGITS       NUMBER(19) DEFAULT 0 NOT NULL,
		  IS_REQUIRED          NUMBER(1) DEFAULT 0 NOT NULL,
		  IS_AUTO_INCREMENT    NUMBER(1) DEFAULT 0 NOT NULL,
		  REMARK               VARCHAR2(1000)
		)
    ');
	execute immediate(' ALTER TABLE DATAOBJ_COLUMN ADD CONSTRAINT PK_DATAOBJ_COLUMN PRIMARY KEY (ID)');	
	execute immediate(' ANALYZE TABLE DATAOBJ_COLUMN COMPUTE STATISTICS ');
  end if;  
End;
/

-- DATAOBJ_TABLE 数据对象关联表信息表
-- create by hst 2018-04-10
Declare
  vi_Num  integer;
  vi_ColNum integer;
Begin
  select count(1) into vi_Num from user_tables where table_name = upper('DATAOBJ_TABLE'); 
  if vi_Num = 0 then
    execute immediate(' 
      CREATE TABLE DATAOBJ_TABLE(                                                   
		  ID              NUMBER(20) NOT NULL,
		  GMT_CREATE      DATE DEFAULT SYSDATE,
		  GMT_MODIFIED    DATE DEFAULT SYSDATE,
		  MASTER_ID       NUMBER(20) NOT NULL,
		  TABLE_NAME      VARCHAR2(50) NOT NULL,
		  RELATION_TYPE   VARCHAR2(20) DEFAULT ''LEFT'',
		  COLUMN_NAME     VARCHAR2(20),
		  RELATION_COLUMN VARCHAR2(20),
		  RELATION_WHERE  VARCHAR2(100),
		  FOLLOW_DELETE   NUMBER(1) DEFAULT 0,
		  FOLLOW_INSERT   NUMBER(1) DEFAULT 0,
		  FOLLOW_UPDATE   NUMBER(1) DEFAULT 0
		)
    ');
	execute immediate(' ALTER TABLE DATAOBJ_TABLE ADD CONSTRAINT PK_DATAOBJ_TABLE PRIMARY KEY (ID)');	
	execute immediate(' ANALYZE TABLE DATAOBJ_TABLE COMPUTE STATISTICS ');
  end if;  
End;
/

-- DATAOBJ_APP 应用表
-- create by hst 2018-04-10
Declare
  vi_Num  integer;
  vi_ColNum integer;
Begin
  select count(1) into vi_Num from user_tables where table_name = upper('DATAOBJ_APP'); 
  if vi_Num = 0 then
    execute immediate(' 
      CREATE TABLE DATAOBJ_APP(                                                   
		  ID           NUMBER(20) NOT NULL,
		  GMT_CREATE   DATE DEFAULT SYSDATE,
		  GMT_MODIFIED DATE DEFAULT SYSDATE,
		  APP          VARCHAR2(20) NOT NULL,
		  DESCRIPTION  VARCHAR2(200)
		)
    ');
	execute immediate(' ALTER TABLE DATAOBJ_APP ADD CONSTRAINT PK_DATAOBJ_APP PRIMARY KEY (ID)');	
	execute immediate(' ANALYZE TABLE DATAOBJ_APP COMPUTE STATISTICS ');
  end if;  
End;
/

-- DATAOBJ_MODULE 应用表
-- create by hst 2018-04-10
Declare
  vi_Num  integer;
  vi_ColNum integer;
Begin
  select count(1) into vi_Num from user_tables where table_name = upper('DATAOBJ_MODULE'); 
  if vi_Num = 0 then
    execute immediate(' 
      CREATE TABLE DATAOBJ_MODULE(                                                   
		  ID           NUMBER(20) NOT NULL,
		  GMT_CREATE   DATE DEFAULT SYSDATE,
		  GMT_MODIFIED DATE DEFAULT SYSDATE,
		  MASTER_ID    NUMBER(20) NOT NULL,
		  MODULE       VARCHAR2(20) NOT NULL,
		  DESCRIPTION  VARCHAR2(200)
		)
    ');
	execute immediate(' ALTER TABLE DATAOBJ_MODULE ADD CONSTRAINT PK_DATAOBJ_MODULE PRIMARY KEY (ID)');
		execute immediate(' CREATE UNIQUE INDEX IDX_DATAOBJ_MODULE_MASTER ON DATAOBJ_MODULE(MASTER_ID, MODULE) ');
	execute immediate(' ANALYZE TABLE DATAOBJ_MODULE COMPUTE STATISTICS ');
  end if;  
End;
/

-- DATAOBJ_BIG_TEXT 大文本表
-- create by hst 2018-04-10
Declare
  vi_Num  integer;
  vi_ColNum integer;
Begin
  select count(1) into vi_Num from user_tables where table_name = upper('DATAOBJ_BIG_TEXT'); 
  if vi_Num = 0 then
    execute immediate(' 
      CREATE TABLE DATAOBJ_BIG_TEXT(                                                   
		  ID           NUMBER(20) NOT NULL,
		  GMT_CREATE   DATE DEFAULT SYSDATE,
		  GMT_MODIFIED DATE DEFAULT SYSDATE,
		  CODE         VARCHAR2(50) NOT NULL,
		  CONTENT      CLOB
		)
    ');
	execute immediate(' ALTER TABLE DATAOBJ_BIG_TEXT ADD CONSTRAINT PK_DATAOBJ_BIG_TEXT PRIMARY KEY (ID)');	
	execute immediate(' ANALYZE TABLE DATAOBJ_BIG_TEXT COMPUTE STATISTICS ');
  end if;  
End;
/

-- SEQ_DATAOBJ 数据对象id自增序列
-- create by hst 2018-04-10
Declare
  vi_Num  integer;
Begin
  select count(1) into vi_Num from user_sequences where SEQUENCE_NAME = upper('SEQ_DATAOBJ');
  if vi_Num = 0 then
    execute immediate(' 
      create sequence SEQ_DATAOBJ
      minvalue 1
      maxvalue 9999999999999999999999
      start with 1001
      increment by 1
      cache 20');
  end if;
end;
/

-- SEQ_DATAOBJ_COLUMN 数据对象列id自增序列
-- create by hst 2018-04-10
Declare
  vi_Num  integer;
Begin
  select count(1) into vi_Num from user_sequences where SEQUENCE_NAME = upper('SEQ_DATAOBJ_COLUMN');
  if vi_Num = 0 then
    execute immediate(' 
      create sequence SEQ_DATAOBJ_COLUMN
      minvalue 1
      maxvalue 9999999999999999999999
      start with 1001
      increment by 1
      cache 20');
  end if;
end;
/

-- SEQ_DATAOBJ_TABLE 数据对象关联表id自增序列
-- create by hst 2018-04-10
Declare
  vi_Num  integer;
Begin
  select count(1) into vi_Num from user_sequences where SEQUENCE_NAME = upper('SEQ_DATAOBJ_TABLE');
  if vi_Num = 0 then
    execute immediate(' 
      create sequence SEQ_DATAOBJ_TABLE
      minvalue 1
      maxvalue 9999999999999999999999
      start with 1001
      increment by 1
      cache 20');
  end if;
end;
/

-- SEQ_DATAOBJ_APP 数据对象应用id自增序列
-- create by hst 2018-04-10
Declare
  vi_Num  integer;
Begin
  select count(1) into vi_Num from user_sequences where SEQUENCE_NAME = upper('SEQ_DATAOBJ_APP');
  if vi_Num = 0 then
    execute immediate(' 
      create sequence SEQ_DATAOBJ_APP
      minvalue 1
      maxvalue 9999999999999999999999
      start with 1001
      increment by 1
      cache 20');
  end if;
end;
/

-- SEQ_DATAOBJ_MODULE 数据对象模块id自增序列
-- create by hst 2018-04-10
Declare
  vi_Num  integer;
Begin
  select count(1) into vi_Num from user_sequences where SEQUENCE_NAME = upper('SEQ_DATAOBJ_MODULE');
  if vi_Num = 0 then
    execute immediate(' 
      create sequence SEQ_DATAOBJ_MODULE
      minvalue 1
      maxvalue 9999999999999999999999
      start with 1001
      increment by 1
      cache 20');
  end if;
end;
/