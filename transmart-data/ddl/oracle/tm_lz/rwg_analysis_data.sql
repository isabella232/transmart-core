--
-- Type: TABLE; Owner: TM_LZ; Name: RWG_ANALYSIS_DATA
--
 CREATE TABLE "TM_LZ"."RWG_ANALYSIS_DATA" 
  (	"STUDY_ID" VARCHAR2(200 BYTE), 
"PROBESET" VARCHAR2(200 BYTE), 
"FOLD_CHANGE" FLOAT(126), 
"PVALUE" FLOAT(126), 
"RAW_PVALUE" FLOAT(126), 
"MIN_LSMEAN" FLOAT(126), 
"MAX_LSMEAN" FLOAT(126), 
"ANALYSIS_CD" VARCHAR2(100 BYTE), 
"BIO_ASSAY_ANALYSIS_ID" NUMBER, 
"ADJUSTED_PVALUE" FLOAT(126)
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;

