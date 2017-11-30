--
-- Type: TABLE; Owner: BIOMART; Name: BIO_AD_HOC_PROPERTY
--
 CREATE TABLE "BIOMART"."BIO_AD_HOC_PROPERTY" 
  (	"AD_HOC_PROPERTY_ID" NUMBER(22,0) NOT NULL ENABLE, 
"BIO_DATA_ID" NUMBER(22,0) NOT NULL ENABLE, 
"PROPERTY_KEY" NVARCHAR2(50), 
"PROPERTY_VALUE" NVARCHAR2(2000), 
 CONSTRAINT "BIO_AD_HOC_PROPERTY_PK" PRIMARY KEY ("AD_HOC_PROPERTY_ID")
 USING INDEX
 TABLESPACE "TRANSMART"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;

--
-- Type: TRIGGER; Owner: BIOMART; Name: TRG_BIO_AD_HOC_PROP_ID
--
  CREATE OR REPLACE TRIGGER "BIOMART"."TRG_BIO_AD_HOC_PROP_ID" before
  INSERT ON "BIOMART"."BIO_AD_HOC_PROPERTY" FOR EACH row BEGIN IF inserting THEN IF :NEW."AD_HOC_PROPERTY_ID" IS NULL THEN
  SELECT SEQ_BIO_DATA_ID.nextval INTO :NEW."AD_HOC_PROPERTY_ID" FROM dual;
END IF;
END IF;
END;
/
ALTER TRIGGER "BIOMART"."TRG_BIO_AD_HOC_PROP_ID" ENABLE;
 
