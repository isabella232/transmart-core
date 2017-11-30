--
-- Type: TABLE; Owner: I2B2DEMODATA; Name: QT_PDO_QUERY_MASTER
--
 CREATE TABLE "I2B2DEMODATA"."QT_PDO_QUERY_MASTER" 
  (	"QUERY_MASTER_ID" NUMBER(5,0) NOT NULL ENABLE, 
"USER_ID" VARCHAR2(50 BYTE) NOT NULL ENABLE, 
"GROUP_ID" VARCHAR2(50 BYTE) NOT NULL ENABLE, 
"CREATE_DATE" DATE NOT NULL ENABLE, 
"REQUEST_XML" CLOB, 
"I2B2_REQUEST_XML" CLOB, 
 PRIMARY KEY ("QUERY_MASTER_ID")
 USING INDEX
 TABLESPACE "TRANSMART"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" 
LOB ("REQUEST_XML") STORE AS BASICFILE (
 TABLESPACE "TRANSMART" ENABLE STORAGE IN ROW CHUNK 8192 RETENTION 
 NOCACHE LOGGING ) 
LOB ("I2B2_REQUEST_XML") STORE AS BASICFILE (
 TABLESPACE "TRANSMART" ENABLE STORAGE IN ROW CHUNK 8192 RETENTION 
 NOCACHE LOGGING ) ;

--
-- Type: INDEX; Owner: I2B2DEMODATA; Name: QT_IDX_PQM_UGID
--
CREATE INDEX "I2B2DEMODATA"."QT_IDX_PQM_UGID" ON "I2B2DEMODATA"."QT_PDO_QUERY_MASTER" ("USER_ID", "GROUP_ID")
TABLESPACE "TRANSMART" ;

--
-- Type: SEQUENCE; Owner: I2B2DEMODATA; Name: QT_SQ_PQM_QMID
--
CREATE SEQUENCE  "I2B2DEMODATA"."QT_SQ_PQM_QMID"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

--
-- Type: TRIGGER; Owner: I2B2DEMODATA; Name: TRG_QT_PDO_QM_QM_ID
--
  CREATE OR REPLACE TRIGGER "I2B2DEMODATA"."TRG_QT_PDO_QM_QM_ID" 
   before insert on "I2B2DEMODATA"."QT_PDO_QUERY_MASTER" 
   for each row 
begin  
   if inserting then 
      if :NEW."QUERY_MASTER_ID" is null then 
         select QT_SQ_PQM_QMID.nextval into :NEW."QUERY_MASTER_ID" from dual; 
      end if; 
   end if; 
end;
/
ALTER TRIGGER "I2B2DEMODATA"."TRG_QT_PDO_QM_QM_ID" ENABLE;
 
