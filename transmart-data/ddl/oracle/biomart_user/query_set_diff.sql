--
-- Type: TABLE; Owner: BIOMART_USER; Name: QUERY_SET_DIFF
-- DEPRECATED! user queries related functionality has been moved to a gb-backend application
--
CREATE TABLE "BIOMART_USER"."QUERY_SET_DIFF"
(
    "ID" NUMBER NOT NULL,
    "QUERY_SET_ID" NUMBER NOT NULL,
    "OBJECT_ID" NUMBER(38,0) NOT NULL,
    "CHANGE_FLAG" character varying(7) NOT NULL,
    PRIMARY KEY ("ID")
);

--
-- Type: SEQUENCE; Owner: BIOMART_USER; Name: QUERY_SET_DIFF_ID_SEQ
--
CREATE SEQUENCE "BIOMART_USER"."QUERY_SET_DIFF_ID_SEQ";

--
-- Type: TRIGGER; Owner: BIOMART_USER; Name: TRG_QUERY_SET_DIFF_ID
--
CREATE OR REPLACE TRIGGER "BIOMART_USER"."TRG_QUERY_SET_DIFF_ID"
before insert on "BIOMART_USER"."QUERY_SET_DIFF"
for each row begin
	if inserting then
		if :NEW."ID" is null then
			select QUERY_SET_DIFF_ID_SEQ.nextval into :NEW."ID" from dual;
		end if;
	end if;
end;

/
ALTER TRIGGER "BIOMART_USER"."TRG_QUERY_SET_DIFF_ID" ENABLE;

--
-- Table documentation
--
COMMENT ON TABLE biomart_user.query_set_diff IS 'Table stores information about specific objects, deleted or added to the subscribed user query related set. DEPRECATED! This table has been moved to a gb-backend application database.';

COMMENT ON COLUMN biomart_user.query_set_diff.query_set_id IS 'Foreign key to id in query_set table.';
COMMENT ON COLUMN biomart_user.query_set_diff.object_id IS 'The id of the object from the set that was updated, e.g. id of an i2b2demodata.patient_dimension instance.';
COMMENT ON COLUMN biomart_user.query_set_diff.change_flag IS 'The flag determining whether the object was added or removed from the related set';

