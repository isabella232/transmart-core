--
-- Type: TABLE; Owner: TM_WZ; Name: WT_VOCAB_NODES
--
 CREATE TABLE "TM_WZ"."WT_VOCAB_NODES" 
  (	"LEAF_NODE" VARCHAR2(1000 BYTE), 
"MODIFIER_CD" VARCHAR2(100 BYTE), 
"LABEL_NODE" VARCHAR2(1000 BYTE), 
"VALUE_INSTANCE" NUMBER(18,0), 
"LABEL_INSTANCE" NUMBER(18,0)
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;

