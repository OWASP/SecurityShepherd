-- -----------------------------------------------------
-- procedure moduleIncrementalInfo
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleIncrementalInfoTest` (IN theUserId VARCHAR(64))
BEGIN
(SELECT moduleName, moduleCategory, moduleId, finishTime, incrementalRank
	FROM modules 
	LEFT JOIN results USING (moduleId) 
	WHERE userId = theUserId)
UNION 
	(SELECT moduleName, moduleCategory, moduleId, null, incrementalRank
	FROM modules 
	WHERE moduleId NOT IN 
	(
		SELECT moduleId 
			FROM modules 
			JOIN results USING (moduleId) 
			WHERE userId = theUserId
			AND moduleStatus = 'open'
	))
	AND moduleStatus = 'open'
	ORDER BY incrementalRank;
END
DELIMITER $$