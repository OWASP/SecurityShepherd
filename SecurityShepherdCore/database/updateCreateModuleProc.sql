-- -----------------------------------------------------
-- procedure moduleCreate
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
DROP PROCEDURE `core`.`moduleCreate`;$$
CREATE PROCEDURE `core`.`moduleCreate` (IN theModuleName VARCHAR(64), theModuleType VARCHAR(16), theModuleCategory VARCHAR(64), theModuleSolution VARCHAR(256))
BEGIN
DECLARE theId VARCHAR(64);
DECLARE theDate DATETIME;
COMMIT;
SELECT NOW() FROM DUAL
    INTO theDate;
IF (theModuleSolution IS NULL) THEN
    SELECT SHA2(theDate, 256) FROM DUAL
        INTO theModuleSolution;
END IF;
IF (theModuleType = 'lesson' OR theModuleType = 'challenge') THEN
    -- Increment sequence for users table
    UPDATE sequence SET
        currVal = currVal + 1
        WHERE tableName = 'modules';
    COMMIT;
    SELECT SHA(CONCAT(currVal, tableName, theDate, theModuleName)) FROM sequence
        WHERE tableName = 'modules'
        INTO theId;
    INSERT INTO modules (
        moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash
    )VALUES(
        theId, theModuleName, theModuleType, theModuleCategory, theModuleSolution, SHA2(CONCAT(theModuleName, theId), 256)
    );
    COMMIT;
    SELECT moduleId, moduleHash FROM modules
        WHERE moduleId = theId;
ELSE
    SELECT 'ERROR: Invalid module type submited' FROM DUAL;
END IF;

END

$$

DELIMITER ;
COMMIT;