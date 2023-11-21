ALTER SESSION SET CONTAINER=XEPDB1;

GRANT UNLIMITED TABLESPACE TO aaa;
GRANT CREATE SESSION TO aaa;
GRANT CREATE ANY TABLE TO aaa;

CREATE TABLE aaa.sample_table(id raw(16) DEFAULT sys_guid() PRIMARY key, value1 varchar2(50 char), value2 varchar2(50 char));

CREATE OR REPLACE TYPE aaa.Custom_Record AS OBJECT (value1 VARCHAR2(50), value2 VARCHAR2(50));
/
CREATE OR REPLACE TYPE aaa.Custom_Record_List AS TABLE OF aaa.Custom_Record;
/
CREATE OR REPLACE PROCEDURE aaa.Process_Custom_Record(
    item IN aaa.Custom_Record
) AS
BEGIN
    INSERT INTO aaa.sample_table c (value1,value2) values(item.value1, (item.value2));
END;
/
CREATE OR REPLACE PROCEDURE aaa.Process_Custom_Record_List(
    p_list IN aaa.Custom_Record_List
) AS
BEGIN
    -- Iterate through the list and process each employee
    FOR i IN 1..p_list.COUNT LOOP
            INSERT INTO aaa.sample_table c (value1, value2) values(p_list(i).value1, p_list(i).value2);
        END LOOP;
END;
/




GRANT execute ON aaa.Custom_Record TO aaa;
GRANT execute ON aaa.Custom_Record_List TO aaa;
GRANT execute ON aaa.Process_Custom_Record TO aaa;
GRANT execute ON aaa.Process_Custom_Record_List TO aaa;
