--
-- Type: TABLE; Owner: BIOMART; Name: BIO_ASSAY_ANALYSIS_EQTL
--
 CREATE TABLE "BIOMART"."BIO_ASSAY_ANALYSIS_EQTL" 
  (	"BIO_ASY_ANALYSIS_EQTL_ID" NUMBER NOT NULL ENABLE, 
"BIO_ASSAY_ANALYSIS_ID" NUMBER NOT NULL ENABLE, 
"RS_ID" VARCHAR2(50 BYTE), 
"GENE" VARCHAR2(50 BYTE), 
"P_VALUE_CHAR" VARCHAR2(100 BYTE), 
"CIS_TRANS" VARCHAR2(10 BYTE), 
"DISTANCE_FROM_GENE" VARCHAR2(10 BYTE), 
"ETL_ID" NUMBER, 
"EXT_DATA" VARCHAR2(4000 BYTE), 
"P_VALUE" FLOAT(126), 
"LOG_P_VALUE" FLOAT(126)
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;


--
-- Type: TRIGGER; Owner: BIOMART; Name: TRG_BIO_ASY_ANALYSIS_EQTL_ID
--
  CREATE OR REPLACE TRIGGER "BIOMART"."TRG_BIO_ASY_ANALYSIS_EQTL_ID" before insert on BIOMART.BIO_ASSAY_ANALYSIS_EQTL for each row
begin
  if inserting then
    if :NEW."BIO_ASY_ANALYSIS_EQTL_ID" is null then
      select SEQ_BIO_DATA_ID.nextval into :NEW."BIO_ASY_ANALYSIS_EQTL_ID" from dual;
    end if;
  end if;
end;

/
ALTER TRIGGER "BIOMART"."TRG_BIO_ASY_ANALYSIS_EQTL_ID" ENABLE;
 
--
-- Type: INDEX; Owner: BIOMART; Name: BIO_ASSAY_ANALYSIS_EQTL_IDX1
--
CREATE INDEX "BIOMART"."BIO_ASSAY_ANALYSIS_EQTL_IDX1" ON "BIOMART"."BIO_ASSAY_ANALYSIS_EQTL" ("BIO_ASSAY_ANALYSIS_ID")
TABLESPACE "INDX" ;

