SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `core` ;
CREATE SCHEMA IF NOT EXISTS `core` DEFAULT CHARACTER SET latin1 ;
USE `core` ;

-- -----------------------------------------------------
-- Table `core`.`class`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `core`.`class` (
  `classId` VARCHAR(64) NOT NULL ,
  `className` VARCHAR(32) NOT NULL ,
  `classYear` VARCHAR(5) NOT NULL ,
  PRIMARY KEY (`classId`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `core`.`users` (
  `userId` VARCHAR(64) NOT NULL ,
  `classId` VARCHAR(64) NULL ,
  `userName` VARCHAR(32) NOT NULL ,
  `userPass` VARCHAR(512) NOT NULL ,
  `userRole` VARCHAR(32) NOT NULL ,
  `badLoginCount` INT NOT NULL DEFAULT 0 ,
  `suspendedUntil` DATETIME NOT NULL DEFAULT '1000-01-01 00:00:00' ,
  `userAddress` VARCHAR(128) NULL ,
  `tempPassword` TINYINT(1)  NULL DEFAULT FALSE ,
  `userScore` INT NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`userId`) ,
  INDEX `classId` (`classId` ASC) ,
  UNIQUE INDEX `userName_UNIQUE` (`userName` ASC) ,
  CONSTRAINT `classId`
    FOREIGN KEY (`classId` )
    REFERENCES `core`.`class` (`classId` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`modules`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `core`.`modules` (
  `moduleId` VARCHAR(64) NOT NULL ,
  `moduleName` VARCHAR(64) NOT NULL ,
  `moduleType` VARCHAR(16) NOT NULL ,
  `moduleCategory` VARCHAR(64) NULL ,
  `moduleResult` VARCHAR(256) NULL ,
  `moduleHash` VARCHAR(256) NULL ,
  `moduleStatus` VARCHAR(16) NULL DEFAULT 'open' ,
  `incrementalRank` INT NULL DEFAULT 200,
  `scoreValue` INT NOT NULL DEFAULT 50 ,
  `scoreBonus` INT NOT NULL DEFAULT 5 ,
  `hardcodedKey` TINYINT(1) NOT NULL DEFAULT TRUE,
  PRIMARY KEY (`moduleId`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`results`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `core`.`results` (
  `userId` VARCHAR(64) NOT NULL ,
  `moduleId` VARCHAR(64) NOT NULL ,
  `startTime` DATETIME NOT NULL ,
  `finishTime` DATETIME NULL ,
  `csrfCount` INT NULL DEFAULT 0 ,
  `resultSubmission` LONGTEXT NULL ,
  `knowledgeBefore` INT NULL ,
  `knowledgeAfter` INT NULL ,
  `difficulty` INT NULL ,
  PRIMARY KEY (`userId`, `moduleId`) ,
  INDEX `fk_Results_Modules1` (`moduleId` ASC) ,
  CONSTRAINT `fk_Results_users1`
    FOREIGN KEY (`userId` )
    REFERENCES `core`.`users` (`userId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Results_Modules1`
    FOREIGN KEY (`moduleId` )
    REFERENCES `core`.`modules` (`moduleId` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cheatSheet`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `core`.`cheatsheet` (
  `cheatSheetId` VARCHAR(64) NOT NULL ,
  `moduleId` VARCHAR(64) NOT NULL ,
  `createDate` DATETIME NOT NULL ,
  `solution` LONGTEXT NOT NULL ,
  PRIMARY KEY (`cheatSheetId`, `moduleId`) ,
  INDEX `fk_CheatSheet_Modules1` (`moduleId` ASC) ,
  CONSTRAINT `fk_CheatSheet_Modules1`
    FOREIGN KEY (`moduleId` )
    REFERENCES `core`.`modules` (`moduleId` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `core`.`sequence`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `core`.`sequence` (
  `tableName` VARCHAR(32) NOT NULL ,
  `currVal` BIGINT(20) NOT NULL DEFAULT 282475249 ,
  PRIMARY KEY (`tableName`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cheatsheet`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `core`.`cheatsheet` (
  `cheatSheetId` VARCHAR(64) NOT NULL ,
  `moduleId` VARCHAR(64) NOT NULL ,
  `createDate` DATETIME NOT NULL ,
  `solution` LONGTEXT NOT NULL ,
  PRIMARY KEY (`cheatSheetId`, `moduleId`) ,
  INDEX `fk_CheatSheet_Modules1` (`moduleId` ASC) ,
  CONSTRAINT `fk_CheatSheet_Modules1`
    FOREIGN KEY (`moduleId` )
    REFERENCES `core`.`modules` (`moduleId` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- procedure authUser
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`authUser` (IN theName VARCHAR(32), IN theHash VARCHAR(512))
BEGIN
DECLARE theDate DATETIME;
COMMIT;
SELECT NOW() FROM DUAL INTO theDate;
SELECT userId, userName, userRole, badLoginCount, tempPassword, classId FROM `users`
    WHERE userName = theName 
    AND userPass = SHA2(theHash, 512)
    AND suspendedUntil < theDate ;
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userLocked
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userLocked` (IN theName VARCHAR(32))
BEGIN
DECLARE theDate DATETIME;
COMMIT;
SELECT NOW() FROM DUAL INTO theDate;
SELECT userName FROM `users` 
    WHERE userName = theName
    AND theDate > suspendedUntil;
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userLock
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userLock` (theName VARCHAR(32))
BEGIN
DECLARE theDate DATETIME;
DECLARE untilDate DATETIME;
DECLARE theCount INT;

COMMIT;
SELECT NOW() FROM DUAL INTO theDate;
-- Get the badLoginCount from users if they are not suspended already or account has attempted a login within the last 10 mins
SELECT badLoginCount FROM `users` 
    WHERE userName = theName
    AND suspendedUntil < (theDate - '0000-00-00 00:10:00')
    INTO theCount;
    
SELECT suspendedUntil FROM `users` 
    WHERE userName = theName
    AND suspendedUntil < (theDate - '0000-00-00 00:10:00')
    INTO untilDate;
IF (untilDate < theDate) THEN
    IF (theCount >= 3) THEN
        -- Set suspended until 30 mins from now
        UPDATE `users` SET
            suspendedUntil = TIMESTAMPADD(MINUTE, 30, theDate),
            badLoginCount = 0
            WHERE userName = theName;
        COMMIT;
    -- ELSE the user is already suspended, or theCount < 3
    ELSE
        -- Get user where their last bad login was within 10 mins ago
        SELECT COUNT(userId) FROM users
            WHERE userName = theName
            AND suspendedUntil < (theDate - '0000-00-00 00:10:00')
            INTO theCount;
            
        -- IF a user was counted then they are not suspended, but have attemped a bad login within 10 mins of their last
        IF (theCount > 0) THEN 
            UPDATE `users` SET
                badLoginCount = (badLoginCount + 1),
                suspendedUntil = theDate
                WHERE userName = theName;
            COMMIT;
        -- ELSE this is the first time within 10 mins that this account has logged in bad
        ELSE
            UPDATE `users` SET
                badLoginCount = 1,
                suspendedUntil = theDate
                WHERE userName = theName;
            COMMIT;
        END IF;
    END IF;
END IF;
END











$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userFind
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userFind` (IN theName VARCHAR(32))
BEGIN
COMMIT;
SELECT userName, suspendedUntil FROM `users`
    WHERE userName = theName;
END




$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure playerCount
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`playerCount` ()
BEGIN
    COMMIT;
    SELECT count(userId) FROM users
        WHERE userRole = 'player';
END


$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userCreate
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userCreate` (IN theClassId VARCHAR(64), IN theUserName VARCHAR(32), IN theUserPass VARCHAR(512), IN theUserRole VARCHAR(32), IN theUserAddress VARCHAR(128), tempPass BOOLEAN)
BEGIN
    DECLARE theId VARCHAR(64);
    DECLARE theClassCount INT;    
    
    COMMIT;
    -- If (Valid User Type) AND (classId = null or (Valid Class Id)) Then create user
    IF (theUserRole = 'player' OR theUserRole = 'admin') THEN
        IF (theClassId != null) THEN
            SELECT count(classId) FROM class
                WHERE classId = theClassId
                INTO theClassCount;
            IF (theClassCount != 1) THEN
                SELECT null FROM DUAL INTO theClassId;
            END IF;
        END IF;
                
        -- Increment sequence for users table
        UPDATE sequence SET
            currVal = currVal + 1
            WHERE tableName = 'users';
        COMMIT;
        SELECT SHA(CONCAT(currVal, tableName)) FROM sequence
            WHERE tableName = 'users'
            INTO theId;
        
        -- Insert the values, badLoginCount and suspendedUntil Values will use the defaults defined by the table
        INSERT INTO users (
                userId,
                classId,
                userName,
                userPass,
                userRole,
                userAddress,
                tempPassword
            ) VALUES (
                theId,
                theClassId,
                theUserName,
                SHA2(theUserPass, 512),
                theUserRole,
                theUserAddress,
                tempPass
            );
        COMMIT;
        SELECT null FROM DUAL;
    ELSE
        SELECT 'Invalid Role Type Detected' FROM DUAL;
    END IF;
END
















$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userBadLoginReset
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userBadLoginReset` (IN theUserId VARCHAR(45))
BEGIN
    COMMIT;
    UPDATE users SET
        badLoginCount = 0
        WHERE userId = theUserId;
    COMMIT;
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userPasswordChange
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userPasswordChange` (IN theUserName VARCHAR(32), IN currentPassword VARCHAR(512), IN newPassword VARCHAR(512))
BEGIN
DECLARE theDate DATETIME;
COMMIT;
SELECT NOW() FROM DUAL INTO theDate;
UPDATE users SET
    userPass = SHA2(newPassword, 512),
    tempPassword = FALSE
    WHERE userPass = SHA2(currentPassword, 512)
    AND userName = theUserName
    AND suspendedUntil < theDate;
COMMIT;
END



$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure classCreate
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`classCreate` (IN theClassName VARCHAR(32), IN theClassYear VARCHAR(5))
BEGIN
    DECLARE theId VARCHAR(64);
    COMMIT;
    UPDATE sequence SET
        currVal = currVal + 1
        WHERE tableName = 'users';
    COMMIT;
    SELECT SHA(CONCAT(currVal, tableName)) FROM sequence
        WHERE tableName = 'users'
        INTO theId;
    
    INSERT INTO class VALUES (theId, theClassName, theClassYear);
END





$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure classCount
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`classCount` ()
BEGIN
    SELECT count(ClassId) FROM class;
END$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure classesGetData
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`classesGetData` ()
BEGIN
    SELECT classId, className, classYear FROM class;
END$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure classFind
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`classFind` (IN theClassId VARCHAR(64))
BEGIN
    SELECT className, classYear FROM class
        WHERE classId = theClassId;
END$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure playersByClass
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`playersByClass` (IN theClassId VARCHAR(64))
BEGIN
    COMMIT;
    SELECT userId, userName, userAddress FROM users
        WHERE classId = theClassId
        AND userRole = 'player'
        ORDER BY userName;
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure playerUpdateClass
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`playerUpdateClass` (IN theUserId VARCHAR(64), IN theClassId VARCHAR(64))
BEGIN
COMMIT;
UPDATE users SET
    classId = theClassId
    WHERE userId = theUserId
    AND userRole = 'player';
COMMIT;
SELECT userName FROM users
    WHERE userId = theUserId
    AND classId = theClassId
    AND userRole = 'player';
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure playerFindById
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`playerFindById` (IN playerId VARCHAR(64))
BEGIN
COMMIT;
SELECT userName FROM users
    WHERE userId = playerId
    AND userRole = 'player';
END$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure playersWithoutClass
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`playersWithoutClass` ()
BEGIN
    COMMIT;
    SELECT userId, userName, userAddress FROM users
        WHERE classId is NULL
        AND userRole = 'player'
        ORDER BY userName;
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure playerUpdateClassToNull
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`playerUpdateClassToNull` (IN theUserId VARCHAR(45))
BEGIN
COMMIT;
UPDATE users SET
    classId = NULL
    WHERE userId = theUserId
    AND userRole = 'player';
COMMIT;
SELECT userName FROM users
    WHERE userId = theUserId
    AND classId IS NULL
    AND userRole = 'player';
END
$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userUpdateRole
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userUpdateRole` (IN theUserId VARCHAR(64), IN theNewRole VARCHAR(32))
BEGIN
COMMIT;
UPDATE users SET
    userRole = theNewRole
    WHERE userId = theUserId;
COMMIT;
SELECT userName FROM users
    WHERE userId = theUserId;
END
$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleCreate
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleCreate` (IN theModuleName VARCHAR(64), theModuleType VARCHAR(16), theModuleCategory VARCHAR(64), isHardcodedKey BOOLEAN, theModuleSolution VARCHAR(256))
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
IF (isHardcodedKey IS NULL) THEN
    SELECT TRUE FROM DUAL
        INTO isHardcodedKey;
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
        moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, hardcodedKey
    )VALUES(
        theId, theModuleName, theModuleType, theModuleCategory, theModuleSolution, SHA2(CONCAT(theModuleName, theId), 256), isHardcodedKey
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
-- -----------------------------------------------------
-- procedure moduleAllInfo
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleAllInfo` (IN theType VARCHAR(64), IN theUserId VARCHAR(64))
BEGIN
(SELECT moduleName, moduleCategory, moduleId, finishTime
FROM modules LEFT JOIN results USING (moduleId) WHERE userId = theUserId AND moduleType = theType) UNION (SELECT moduleName, moduleCategory, moduleId, null FROM modules WHERE moduleId NOT IN (SELECT moduleId FROM modules JOIN results USING (moduleId) WHERE userId = theUserId AND moduleType = theType) AND moduleType = theType) ORDER BY moduleCategory, moduleName;
END











$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleGetResult
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleGetResult` (IN theModuleId VARCHAR(64))
BEGIN
COMMIT;
SELECT moduleName, moduleResult FROM modules
    WHERE moduleId = theModuleId
    AND moduleResult IS NOT NULL;
END



$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userUpdateResult
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userUpdateResult` (IN theModuleId VARCHAR(64), IN theUserId VARCHAR(64), IN theBefore INT, IN theAfter INT, IN theDifficulty INT, IN theAdditionalInfo LONGTEXT)
BEGIN
DECLARE theDate TIMESTAMP;
DECLARE theBonus INT;
DECLARE totalScore INT;
COMMIT;
SELECT NOW() FROM DUAL
    INTO theDate;
-- Get current bonus and decrement the bonus value
SELECT 0 FROM DUAL INTO totalScore;
SELECT scoreBonus FROM modules
    WHERE moduleId = theModuleId
    INTO theBonus;
IF (theBonus > 0) THEN
    SELECT (totalScore + theBonus) FROM DUAL
        INTO totalScore;
    UPDATE modules SET 
        scoreBonus = scoreBonus - 1
        WHERE moduleId = theModuleId;
    COMMIT;
END IF;

-- Get the Score value for the level
SELECT (totalScore + scoreValue) FROM modules
    WHERE moduleId = theModuleId
    INTO totalScore;
    
-- Update users score
UPDATE users SET
    userScore = userScore + totalScore
    WHERE userId = theUserId;
COMMIT;

-- Update result row
UPDATE results SET
    finishTime = theDate,
    `knowledgeBefore` = theBefore,
    `knowledgeAfter` = theAfter,
    `difficulty`  = theDifficulty,
    `resultSubmission` = theAdditionalInfo
    WHERE startTime IS NOT NULL
    AND finishTime IS NULL
    AND userId = theUserId
    AND moduleId = theModuleId;
COMMIT;
SELECT moduleName FROM modules
    JOIN results USING (moduleId)
    WHERE startTime IS NOT NULL
    AND finishTime IS NOT NULL
    AND userId = theUserId
    AND moduleId = theModuleId;
END $$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleGetHash
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleGetHash` (IN theModuleId VARCHAR(64), IN theUserId VARCHAR(64))
BEGIN
DECLARE theDate DATETIME;
DECLARE tempInt INT;
COMMIT;
SELECT NOW() FROM DUAL
    INTO theDate;
SELECT COUNT(*) FROM results
    WHERE userId = theUserId
    AND moduleId = theModuleId
    AND startTime IS NOT NULL
    INTO tempInt;
IF(tempInt = 0) THEN
    INSERT INTO results 
        (moduleId, userId, startTime)
        VALUES
        (theModuleId, theUserId, theDate);
    COMMIT;
END IF;
SELECT moduleHash, moduleCategory, moduleType FROM modules
    WHERE moduleId = theModuleId;
END





$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleGetResultFromHash
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleGetResultFromHash` (IN theHash VARCHAR(256))
BEGIN
COMMIT;
SELECT moduleResult FROM modules
    WHERE moduleHash = theHash;
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure resultMessageByClass
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`resultMessageByClass` (IN theClassId VARCHAR(64), IN theModuleId VARCHAR(64))
BEGIN
COMMIT;
SELECT userName, resultSubmission FROM results
    JOIN users USING (userId)
    JOIN class USING (classId)
    WHERE classId = theClassId
    AND moduleId = theModuleId;
END




$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure resultMessageSet
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`resultMessageSet` (IN theMessage VARCHAR(128), IN theUserId VARCHAR(64), IN theModuleId VARCHAR(64))
BEGIN
COMMIT;
UPDATE results SET
    resultSubmission = theMessage
    WHERE moduleId = theModuleId
    AND userId = theUserId;
COMMIT;
END




$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure resultMessagePlus
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`resultMessagePlus` (IN theModuleId VARCHAR(64), IN theUserId2 VARCHAR(64))
BEGIN
DECLARE temp INT;
COMMIT;
SELECT csrfCount FROM results
    WHERE userId = theUserId2
    AND moduleId = theModuleId
    INTO temp;
IF (temp = 0) THEN
    CALL moduleComplete(theModuleId, theUserId2);
END IF;
UPDATE results SET
    csrfCount = csrfCount + 1
    WHERE userId = theUserId2
    AND moduleId = theModuleId;
COMMIT;
END





$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleGetIdFromHash
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleGetIdFromHash` (IN theHash VARCHAR(256))
BEGIN
COMMIT;
SELECT moduleId FROM modules
    WHERE moduleHash = theHash;
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userGetNameById
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userGetNameById` (IN theUserId VARCHAR(64))
BEGIN
COMMIT;
SELECT userName FROM users
    WHERE userId = theUserId;
END
$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleComplete
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleComplete` (IN theModuleId VARCHAR(64), IN theUserId VARCHAR(64))
BEGIN
DECLARE theDate DATETIME;
COMMIT;
SELECT NOW() FROM DUAL
    INTO theDate;
UPDATE results SET
    finishTime = theDate
    WHERE startTime IS NOT NULL
    AND moduleId = theModuleId
    AND userId = theUserId;
COMMIT;
END




$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure cheatSheetCreate
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`cheatSheetCreate` (IN theModule VARCHAR(64), IN theSheet LONGTEXT)
BEGIN
DECLARE theDate DATETIME;
DECLARE theId VARCHAR(64);
    COMMIT;
    UPDATE sequence SET
        currVal = currVal + 1
        WHERE tableName = 'cheatSheet';
    COMMIT;
    SELECT SHA(CONCAT(currVal, tableName)) FROM sequence
        WHERE tableName = 'cheatSheet'
        INTO theId;
    SELECT NOW() FROM DUAL INTO theDate;
    INSERT INTO cheatSheet
        (cheatSheetId, moduleId, createDate, solution)
        VALUES
        (theId, theModule, theDate, theSheet);
    COMMIT;
END




$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleGetAll
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleGetAll` ()
BEGIN
COMMIT;
SELECT moduleId, moduleName, moduleType, moduleCategory FROM modules
    ORDER BY moduleType, moduleCategory, moduleName;
END
$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure cheatSheetGetSolution
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`cheatSheetGetSolution` (IN theModuleId VARCHAR(64))
BEGIN
COMMIT;
SELECT moduleName, solution FROM modules
    JOIN cheatsheet USING (moduleId)
    WHERE moduleId = theModuleID
    ORDER BY createDate DESC;
END
$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleGetHashById
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleGetHashById` (IN theModuleId VARCHAR(64))
BEGIN
COMMIT;
SELECT moduleHash FROM modules
    WHERE moduleId = theModuleId;
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userCheckResult
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userCheckResult` (IN theModuleId VARCHAR(64), IN theUserId VARCHAR(64))
BEGIN
COMMIT;
-- Returns a module Name if the user has not completed the module identified by moduleId
SELECT moduleName FROM results
    JOIN modules USING(moduleId)
    WHERE finishTime IS NULL
    AND startTime IS NOT NULL
    AND finishTime IS NULL
    AND userId = theUserId
    AND moduleId = theModuleId;
END


$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleIncrementalInfo
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleIncrementalInfo` (IN theUserId VARCHAR(64))
BEGIN
(SELECT moduleName, moduleCategory, moduleId, finishTime, incrementalRank FROM modules LEFT JOIN results USING (moduleId) WHERE userId = theUserId) UNION (SELECT moduleName, moduleCategory, moduleId, null, incrementalRank FROM modules WHERE moduleId NOT IN (SELECT moduleId FROM modules JOIN results USING (moduleId) WHERE userId = theUserId)) ORDER BY incrementalRank;
END




$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleFeedback
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleFeedback` (IN theModuleId VARCHAR(64))
BEGIN
SELECT userName, TIMESTAMPDIFF(MINUTE, finishTime, startTime)*(-1), difficulty, knowledgeBefore, knowledgeAfter, resultSubmission
	FROM modules 
	LEFT JOIN results USING (moduleId)
  LEFT JOIN users USING (userId)
  WHERE moduleId = theModuleId;
END
$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userProgress
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userProgress` (IN theClassId VARCHAR(64))
BEGIN
    COMMIT;
SELECT userName, count(finishTime), userScore FROM users JOIN results USING (userId) WHERE finishTime IS NOT NULL 
AND classId = theClassId
GROUP BY userName UNION SELECT userName, 0, userScore FROM users WHERE classId = theClassId AND userId NOT IN (SELECT userId FROM users JOIN results USING (userId) WHERE classId = theClassId AND finishTime IS NOT NULL GROUP BY userName) ORDER BY userScore DESC;
END





$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userStats
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userStats` (IN theUserName VARCHAR(32))
BEGIN
DECLARE temp INT;
SELECT COUNT(*) FROM modules INTO temp;
SELECT userName, sum(TIMESTAMPDIFF(MINUTE, finishTime, startTime)*(-1)) AS "Time", CONCAT(COUNT(*),"/", temp) AS "Progress"
    FROM modules
    LEFT JOIN results USING (moduleId)
    LEFT JOIN users USING (userId)
    WHERE userName = theUserName AND resultSubmission IS NOT NULL
    GROUP BY userName;
END


$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userStatsDetailed
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userStatsDetailed` (IN theUserName VARCHAR(32))
BEGIN
DECLARE temp INT;
SELECT COUNT(*) FROM modules INTO temp;
SELECT userName, moduleName, TIMESTAMPDIFF(MINUTE, finishTime, startTime)*(-1) AS "Time"
    FROM modules
    LEFT JOIN results USING (moduleId)
    LEFT JOIN users USING (userId)
    WHERE userName = theUserName AND resultSubmission IS NOT NULL
    ORDER BY incrementalRank;
END



$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleOpenInfo
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleOpenInfo` (IN theUserId VARCHAR(64))
BEGIN
(SELECT moduleName, moduleCategory, moduleId, finishTime FROM modules LEFT JOIN results USING (moduleId) 
WHERE userId = theUserId AND moduleStatus = 'open') UNION (SELECT moduleName, moduleCategory, moduleId, null FROM modules WHERE moduleId NOT IN (SELECT moduleId FROM modules JOIN results USING (moduleId) WHERE userId = theUserId AND moduleStatus = 'open') AND moduleStatus = 'open') ORDER BY moduleCategory, moduleName;
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleClosednfo
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleClosednfo` (IN theUserId VARCHAR(64))
BEGIN
(SELECT moduleName, moduleCategory, moduleId, finishTime
FROM modules LEFT JOIN results USING (moduleId) WHERE userId = theUserId AND moduleStatus = 'closed') UNION (SELECT moduleName, moduleCategory, moduleId, null FROM modules WHERE moduleId NOT IN (SELECT moduleId FROM modules JOIN results USING (moduleId) WHERE userId = theUserId AND moduleStatus = 'closed') AND moduleStatus = 'closed') ORDER BY moduleCategory, moduleName;
END
$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleSetStatus
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleSetStatus` (IN theModuleId VARCHAR(64), IN theStatus VARCHAR(16))
BEGIN
UPDATE modules SET
    moduleStatus = theStatus
    WHERE moduleId = theModuleId;
COMMIT;
END
$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleAllStatus
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleAllStatus` ()
BEGIN
SELECT moduleId, moduleName, moduleStatus
    FROM modules;
END
$$

DELIMITER ;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `core`.`modules`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `core`;
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('544aa22d3dd16a8232b093848a6523b0712b23da', 'SQL Injection 2', 'challenge', 'Injection', 'fd8e9a29dab791197115b58061b215594211e72c1680f1eacc50b0394133a09f', 'e1e109444bf5d7ae3d67b816538613e64f7d0f51c432a164efc8418513711b0a', 'open', '135', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('5dda8dc216bd6a46fccaa4ed45d49404cdc1c82e', 'SQL Injection 3', 'challenge', 'Injection', '9815 1547 3214 7569', 'b7327828a90da59df54b27499c0dc2e875344035e38608fcfb7c1ab8924923f6', 'open', '205', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('6319a2e38cc4b2dc9e6d840e1b81db11ee8e5342', 'Cross Site Scripting 3', 'challenge', 'XSS', '6abaf491c9122db375533c04df', 'ad2628bcc79bf10dd54ee62de148ab44b7bd028009a908ce3f1b4d019886d0e', 'open', '195', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('cd7f70faed73d2457219b951e714ebe5775515d8', 'Cross Site Scripting 1', 'challenge', 'XSS', '445d0db4a8fc5d4acb164d022b', 'd72ca2694422af2e6b3c5d90e4c11e7b4575a7bc12ee6d0a384ac2469449e8fa', 'open', '25', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('d4e2c37d8f1298fcaf4edcea7292cb76e9eab09b', 'Cross Site Scripting 2', 'challenge', 'XSS', '495ab8cc7fe9532c6a75d378de', 't227357536888e807ff0f0eff751d6034bafe48954575c3a6563cb47a85b1e888', 'open', '115', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('f771a10efb42a79a9dba262fd2be2e44bf40b66d', 'SQL Injection 1', 'challenge', 'Injection', 'f62abebf5658a6a44c5c9babc7865110c62f5ecd9d0a7052db48c4dbee0200e3', 'ffd39cb26727f34cbf9fce3e82b9d703404e99cdef54d2aa745f497abe070b', 'open', '125', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('408610f220b4f71f7261207a17055acbffb8a747', 'SQL Injection', 'lesson', 'Injection', '3c17f6bf34080979e0cebda5672e989c07ceec9fa4ee7b7c17c9e3ce26bc63e0', 'e881086d4d8eb2604d8093d93ae60986af8119c4f643894775433dbfb6faa594', 'open', '75', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('ca8233e0398ecfa76f9e05a49d49f4a7ba390d07', 'Cross Site Scripting', 'lesson', 'XSS', 'ea7b563b2935d8587539d747d', 'zf8ed52591579339e590e0726c7b24009f3ac54cdff1b81a65db1688d86efb3a', 'open', '15', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('453d22238401e0bf6f1ff5d45996407e98e45b07', 'Cross Site Request Forgery', 'lesson', 'CSRF', '666980771c29857b8a84c686751ce7edaae3d6ac0b00a55895926c748453ef71', 'ed4182af119d97728b2afca6da7cdbe270a9e9dd714065f0f775cd40dc296bc7', 'open', '55', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('20e755179a5840be5503d42bb3711716235005ea', 'CSRF 1', 'challenge', 'CSRF', '7639c952a191d569a0c741843b599604c37e33f9f5d8eb07abf0254635320b07', 's74a796e84e25b854906d88f622170c1c06817e72b526b3d1e9a6085f429cf52', 'open', '155', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('94cd2de560d89ef59fc450ecc647ff4d4a55c15d', 'CSRF 2', 'challenge', 'CSRF', '45309dbaf8eaf6d1a5f1ecb1bf1b6be368a6542d3da35b9bf0224b88408dc001', 'z311736498a13604705d608fb3171ebf49bc18753b0ec34b8dff5e4f9147eb5e', 'open', '215', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('5ca9115f3279b9b9f3308eb6a59a4fcd374846d6', 'CSRF 3', 'challenge', 'CSRF', '6bdbe1901cbe2e2749f347efb9ec2be820cc9396db236970e384604d2d55b62a', 'z6b2f5ebbe112dd09a6c430a167415820adc5633256a7b44a7d1e262db105e3c', 'open', '235', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('9533e21e285621a676bec58fc089065dec1f59f5', 'Broken Session Management', 'lesson', 'Session Management', '6594dec9ff7c4e60d9f8945ca0d4', 'b8c19efd1a7cc64301f239f9b9a7a32410a0808138bbefc98986030f9ea83806', 'open', '10', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('a4bf43f2ba5ced041b03d0b3f9fa3541c520d65d', 'Session Management Challenge 1', 'challenge', 'Session Management', 'db7b1da5d7a43c7100a6f01bb0c', 'dfd6bfba1033fa380e378299b6a998c759646bd8aea02511482b8ce5d707f93a', 'open', '105', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('b70a84f159876bb9885b6e0087d22f0a52abbfcf', 'Session Management Challenge 2', 'challenge', 'Session Management', '4ba31e5ffe29de092fe1950422a', 'd779e34a54172cbc245300d3bc22937090ebd3769466a501a5e7ac605b9f34b7', 'open', '145', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('0e9e650ffca2d1fe516c5d7b0ce5c32de9e53d1e', 'Session Management Challenge 3', 'challenge', 'Session Management', 'e62008dc47f5eb065229d48963', 't193c6634f049bcf65cdcac72269eeac25dbb2a6887bdb38873e57d0ef447bc3', 'open', '165', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('0dbea4cb5811fff0527184f99bd5034ca9286f11', 'Insecure Direct Object References', 'lesson', 'Insecure Direct Object References', '59e571b1e59441e76e0c85e5b49', 'fdb94122d0f032821019c7edf09dc62ea21e25ca619ed9107bcc50e4a8dbc100', 'open', '5', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('2dc909fd89c2b03059b1512e7b54ce5d1aaa4bb4', 'Insecure Direct Object Reference Challenge 1', 'challenge', 'Insecure Direct Object References', 'dd6301b38b5ad9c54b85d07c087aebec89df8b8c769d4da084a55663e6186742', 'o9a450a64cc2a196f55878e2bd9a27a72daea0f17017253f87e7ebd98c71c98c', 'open', '95', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('b6432a6b5022cb044e9946315c44ab262ab59e88', 'Unvalidated Redirects and Forwards', 'lesson', 'Unvalidated Redirects and Forwards', '658c43abcf81a61ca5234cfd7a2', 'f15f2766c971e16e68aa26043e6016a0a7f6879283c873d9476a8e7e94ea736f', 'open', '65', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('ac944b716c1ec5603f1d09c68e7a7e6e75b0d601', 'Insufficient Transport Layer Protection', 'lesson', 'Insufficient Transport Layer Protection', '15e83da388267da584954d4fe5a127be3dff117eaee7a97fcda40e61f3c2868b', 'ts906dc0c3dbc3eaaaf6da6ea5ddf17fd5bc46c83d26122952ea2f08a544dd32', 'open', '225', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('52c5394cdedfb2e95b3ad8b92d0d6c9d1370ea9a', 'Failure to Restrict URL Access', 'lesson', 'Failure to Restrict URL Access', 'f60d1337ac4d35cb67880a3adda79', 'oed23498d53ad1d965a589e257d8366d74eb52ef955e103c813b592dba0477e3', 'open', '12', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('201ae6f8c55ba3f3b5881806387fbf34b15c30c2', 'Insecure Cryptographic Storage', 'lesson', 'Insecure Cryptographic Storage', 'base64isNotEncryptionBase64isEncodingBase64HidesNothingFromYou', 'if38ebb58ea2d245fa792709370c00ca655fded295c90ef36f3a6c5146c29ef2', 'open', '35', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('0709410108f91314fb6f7721df9b891351eb2fcc', 'Insecure Cryptographic Storage Challenge 2', 'challenge', 'Insecure Cryptographic Storage', 'TheVigenereCipherIsAmethodOfEncryptingAlphabeticTextByUsingPoly', 'h8aa0fdc145fb8089661997214cc0e685e5f86a87f30c2ca641e1dde15b01177', 'open', '175', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('891a0208a95f1791287be721a4b851d4c584880a', 'Insecure Cryptographic Storage Challenge 1', 'challenge', 'Insecure Cryptographic Storage', 'mylovelyhorserunningthroughthefieldwhereareyougoingwithyourbiga', 'x9c408d23e75ec92495e0caf9a544edb2ee8f624249f3e920663edb733f15cd7', 'open', '85', '50', '5');
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('82e8e9e2941a06852b90c97087309b067aeb2c4c', 'Insecure Direct Object Reference Challenge 2', 'challenge', 'Insecure Direct Object References', '1f746b87a4e3628b90b1927de23f6077abdbbb64586d3ac9485625da21921a0f', 'vc9b78627df2c032ceaf7375df1d847e47ed7abac2a4ce4cb6086646e0f313a4', 'open', '185', '50', '5');
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('e0ba96bb4c8d4cd2e1ff0a10a0c82b5362edf998', 'SQL Injection 4', 'challenge', 'Injection', 'd316e80045d50bdf8ed49d48f130b4acf4a878c82faef34daff8eb1b98763b6f', '1feccf2205b4c5ddf743630b46aece3784d61adc56498f7603ccd7cb8ae92629', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('a84bbf8737a9ca749d81d5226fc87e0c828138ee', 'SQL Injection 5', 'challenge', 'Injection', '343f2e424d5d7a2eff7f9ee5a5a72fd97d5a19ef7bff3ef2953e033ea32dd7ee', '8edf0a8ed891e6fef1b650935a6c46b03379a0eebab36afcd1d9076f65d4ce62', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('ad332a32a6af1f005f9c8d1e98db264eb2ae5dfe', 'SQL Injection 6', 'challenge', 'Injection', '17f999a8b3fbfde54124d6e94b256a264652e5087b14622e1644c884f8a33f82', 'd0e12e91dafdba4825b261ad5221aae15d28c36c7981222eb59f7fc8d8f212a2', 'open', '200', '50', '5', 1);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`) VALUES ('53a53a66cb3bf3e4c665c442425ca90e29536edd', 'Insecure Data Storage', 'lesson', 'Mobile Insecure Data Storage', 'Battery777', 'ecfad0a5d41f59e6bed7325f56576e1dc140393185afca8975fbd6822ebf392f', 'open', '31', '50', '5');
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('307f78f18fd6a87e50ed6705231a9f24cd582574', 'Insecure Data Storage 2', 'challenge', 'Mobile Insecure Data Storage', 'WarshipsAndWrenches', '362f84cf26bf96aeae358d5d0bbee31e9291aaa5367594c29b3af542a7572c01', 'open', '200', '50', '5', 0);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('da3de2e556494a9c2fb7308a98454cf55f3a4911', 'Insecure Data Storage 3', 'challenge', 'Mobile Insecure Data Storage', 'starfish123', 'ec09515a304d2de1f552e961ab769967bdc75740ad2363803168b7907c794cd4', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('335440fef02d19259254ed88293b62f31cccdd41', 'Client Side Injection', 'lesson', 'Mobile Injection', 'VolcanicEruptionsAbruptInterruptions', 'f758a97011ec4452cc0707e546a7c0f68abc6ef2ab747ea87e0892767152eae1', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('a3f7ffd0f9c3d15564428d4df0b91bd927e4e5e4', 'Client Side Injection 2', 'challenge', 'Mobile Injection', 'SourHatsAndAngryCats', '8855c8bb9df4446a546414562eda550520e29f7a82400a317c579eb3a5a0a8ef', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('e635fce334aa61fdaa459c21c286d6332eddcdd3', 'Client Side Injection 3', 'challenge', 'Mobile Injection', 'BurpingChimneys', 'cfe68711def42bb0b201467b859322dd2750f633246842280dc68c858d208425', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('2ab09c0c18470ae5f87d219d019a1f603e66f944', 'Reverse Engineering', 'lesson', 'Mobile Reverse Engineer', 'NintendoMonster', '19753b944b63232812b7af1a0e0adb59928158da5994a39f584cb799f25a95b9', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('f16bf2ab1c1bf400d36330f91e9ac6045edcd003', 'Reverse Engineering 2', 'challenge', 'Mobile Reverse Engineer', 'FireStoneElectric', '5bc811f9e744a71393a277c51bfd8fbb5469a60209b44fa3485c18794df4d5b1', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('1506f22cd73d14d8a73e0ee32006f35d4f234799', 'Unintended Data Leakage', 'lesson', 'Mobile Data Leakage', 'SilentButSteadyRedLed', '392c20397c535845d93c32fd99b94f70afe9cca3f78c1e4766fee1cc08c035ec', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('8ba06bc21d5a9d0f4e6771a74e11ee7036893cd1', 'Unintended Data Leakage 2', 'challange', 'Mobile Data Leakage', 'UpsideDownPizzaDip', 'bf16081ed057b2d1bc97f4b9da897149819a159a8114d4867c7f8f327f5453a8', 'open', '200', '50', '5', 1);

COMMIT;

-- -----------------------------------------------------
-- Data for table `core`.`cheatSheet`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `core`;
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('1ed105033900e462b26ca0685b00d98f59efcd93', '0dbea4cb5811fff0527184f99bd5034ca9286f11', '2012-02-10 10:11:53', 'Stop the request with a proxy and change the &quot;username&quot; parameter to be equall to &quot;admin&quot;');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('286ac1acdd084193e940e6f56df5457ff05a9fe1', '453d22238401e0bf6f1ff5d45996407e98e45b07', '2012-02-10 10:11:53', 'To complete the lesson, the attack string is the following:<br/>&lt;img src=&quot;https://hostname:port/root/grantComplete/csrfLesson?userId=tempId&quot;/&gt;');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('44a6af94f6f7a16cc92d84a936cb5c7825967b47', 'cd7f70faed73d2457219b951e714ebe5775515d8', '2012-02-10 10:11:53', 'Input is been filtered. To complete this challenge, enter the following attack string;<br/>&lt;iframe src=&#39;#&#39; onload=&#39;alert(&quot;XSS&quot;)&#39;&gt;&lt;/iframe&gt;');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('5487f2bf98beeb3aea66941ae8257a5e0bec38bd', '2dc909fd89c2b03059b1512e7b54ce5d1aaa4bb4', '2012-02-10 10:11:53', 'The user Ids in this challenge follow a sequence. The Hidden Users ID is 11');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('5eccb1b8b1c033bba8ef928089808751cbe6e1f8', '94cd2de560d89ef59fc450ecc647ff4d4a55c15d', '2012-02-10 10:11:53', 'To complete this challenge, you must force another user to submit a post request. The easiest way to achieve this is to force the user to visit a custom webpage that submits the post request. This means the webpage needs to be accessable. It can be accessed via a HTTP server, a public Dropbox link, a shared file area. The following is an example webpage that would complete the challenge<br/><br/>&lt;html&gt;<br/>&lt;body&gt;<br/>&lt;form id=&quot;completeChallenge2&quot; action=&quot;https://hostname:port/user/csrfchallengetwo/plusplus&quot; method=&quot;POST&quot; &gt;<br/>&lt;input type=&quot;hidden&quot; name=&quot;userid&quot; value=&quot;exampleId&quot; /&gt;<br/>&lt;input type=&quot;submit&quot;/&gt;<br/>&lt;/form&gt;<br/>&lt;script&gt;<br/>document.forms[&quot;completeChallenge2&quot;].submit();<br/>&lt;/script&gt;<br/>&lt;/body&gt;<br/>&lt;/html&gt;<br/><br/>The class form function should be used to create an iframe that forces the user to visit this attack page.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('6924e936f811e174f206d5432cf7510a270a18fa', 'b70a84f159876bb9885b6e0087d22f0a52abbfcf', '2012-02-10 10:11:53', 'Use the login function with usernames like admin, administrator, root, etc to find administrator email accounts. Use the forgotten password functionality to change the password for the email address recovered. Inspect the response of the password reset request to see what the password was reset to. Use this password to login!');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('7382ff2f7ee416bf0d37961ec54de32c502351de', 'a4bf43f2ba5ced041b03d0b3f9fa3541c520d65d', '2012-02-10 10:11:53', 'Base 64 Decode the &quot;checksum&quot; cookie in the request to find it equals &quot;userRole=user&quot;. Change the value of userRole to be administrator instead. The cookies new value should be &quot;dXNlclJvbGU9YWRtaW5pc3RyYXRvcg==&quot; when you replace it.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('776ef847e16dde4b1d65a476918d2157f62f8c91', '5ca9115f3279b9b9f3308eb6a59a4fcd374846d6', '2012-02-10 10:11:53', 'To complete this challenge, you must force an admin to submit a post request. The easiest way to achieve this is to force the admin to visit a custom webpage that submits the post request. This means the webpage needs to be accessable. It can be accessed via a HTTP server, a public Dropbox link, a shared file area. The following is an example webpage that would complete the challenge<br/><br/>&lt;html&gt;<br/>&lt;body&gt;<br/>&lt;form id=&quot;completeChallenge3&quot; action=&quot;https://hostname:port/user/csrfchallengetwo/plusplus&quot; method=&quot;POST&quot; &gt;<br/>&lt;input type=&quot;hidden&quot; name=&quot;userid&quot; value=&quot;exampleId&quot; /&gt;<br/>&lt;input type=&quot;hidden&quot; name=&quot;csrfToken&quot; value=&quot;anythingExceptNull&quot; /&gt;<br/>&lt;input type=&quot;submit&quot;/&gt;<br/>&lt;/form&gt;<br/>&lt;script&gt;<br/>document.forms[&quot;completeChallenge3&quot;].submit();<br/>&lt;/script&gt;<br/>&lt;/body&gt;<br/>&lt;/html&gt;<br/><br/>The class form function should be used to create an iframe that forces the admin to visit this attack page.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('82c207a4e07cbfc54faec884be6db0524e74829e', '891a0208a95f1791287be721a4b851d4c584880a', '2012-02-10 10:11:53', 'To complete this challenge, move every character five places back to get the following plaintext;<br/>The result key for this lesson is the following string; myLovelyHorseRunningThroughTheFieldWhereAreYouGoingWithYourBigA');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('860e5ed692c956c2ae6c4ba20c95313d9f5b0383', 'b6432a6b5022cb044e9946315c44ab262ab59e88', '2012-02-10 10:11:53', 'To perform the CSRF correctly use the following attack string;<br/>https://hostname:port/user/redirect?to=https://hostname:port/root/grantComplete/unvalidatedredirectlesson?userid=tempId');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('945b7dcdef1a36ded2ab008422396f8ba51c0630', 'd4e2c37d8f1298fcaf4edcea7292cb76e9eab09b', '2012-02-10 10:11:53', 'Input is been filtered. To complete this challenge, enter the following attack string;<br/>&lt;input type=&quot;button&quot; onmouseup=&quot;alert(&#39;XSS&#39;)&quot;/&gt;');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('97f946ed0bbda4f85e472321a256eacf2293239d', '20e755179a5840be5503d42bb3711716235005ea', '2012-02-10 10:11:53', 'To complete this challenge, you can embed the CSRF request in an iframe very easily as follows;<br/>&lt;iframe src=&quot;https://hostname:port/user/csrfchallengeone/plusplus?userid=exampleId&quot;&gt;&lt;/iframe&gt;<br/>Then you need another user to be hit with the attack to mark it as completed.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('af5959a242047ee87f728b87570a4e9ed9417e5e', '544aa22d3dd16a8232b093848a6523b0712b23da', '2012-02-10 10:11:53', 'To complete this challenge, the following attack strings will return all rows from the table:<br/>&#39; || &#39;1&#39; = &#39;1<br/>&#39; OOORRR &#39;1&#39; = &#39;1<br/>The filter in this case does not filter alternative expressions of the boolean OR statement, and it also does not recursively filter OR, so if you enter enough nested OR statements, they will work as well.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('b8515347017439da4216c6f8d984326eb21652d0', '52c5394cdedfb2e95b3ad8b92d0d6c9d1370ea9a', '2012-02-10 10:11:53', 'The url of the result key is hidden in a div with an ID &quot;hiddenDiv&quot; that can be found in the source HTML of the lesson.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('b921c6b7dc82648f0a0d07513f3eecb39b3ed064', 'ca8233e0398ecfa76f9e05a49d49f4a7ba390d07', '2012-02-10 10:11:53', 'The following attack vector will work wonderfully;<br/>&lt;script&gt;alert(&#39;XSS&#39;)&lt;/script&gt;');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('ba4e0a2727561c41286aa850b89022c09e088b67', '0e9e650ffca2d1fe516c5d7b0ce5c32de9e53d1e', '2012-02-10 10:11:53', 'Use the password change function to send a functionality request. Stop this request with a proxy, and take the value of the &quot;current&quot; cookie. Base 64 Decode this two times. Modify the value to an administrator username such as &quot;admin&quot;. Encode this two times and change the value of the current cookie to reflect this change. Sign in as the username you set your current cookie\'\'s value to with the new password you set.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('bb94a8412d7bb95f84c73afa420ca57fbc917912', '9533e21e285621a676bec58fc089065dec1f59f5', '2012-02-10 10:11:53', 'Use a proxy to stop the request to complete the lesson. Change the value of the &quot;lessonComplete&quot; cookie to &quot;lessonComplete&quot; to complete the lesson.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('c0b869ff8a4cd1f388e5e6bdd6525d176175c296', '408610f220b4f71f7261207a17055acbffb8a747', '2012-02-10 10:11:53', "The lesson can be completed with the following attack string<br/>\' OR \'1\' = \'1");
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('c0ed3f81fc615f28a39ed2c23555cea074e513f0', '0709410108f91314fb6f7721df9b891351eb2fcc', '2012-02-10 10:11:53', 'To complete this challenge, inspect the javascript that executes when the &quot;check&quot; is performed. The encryption key is stored in the &quot;theKey&quot; parameter. The last IF statment in the script checks if the output is equal to the encrypted Result Key.<br/>So the key and ciphertext is stored in the script. You can use this informaiton to decypt the result key manually with the vigenere cipher. You can also modify the javascript to decode the key for you. To do this, make the following changes;<br/> 1) Change the line &quot;input\\_char\\_value += alphabet . indexOf (theKey . charAt (theKey\\_index));&quot; to: <br/>&quot;input\\_char\\_value -= alphabet . indexOf (theKey . charAt (theKey\\_index));&quot;<br/>This inverts the process to decrypt instead of decrypt<br/>2) Add the following line to the end of the script:<br/>$(&quot;#resultDiv&quot;).html(&quot;Decode Result: &quot; + output);');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('d0a0742494656c79767864b2898247df4f37b728', '6319a2e38cc4b2dc9e6d840e1b81db11ee8e5342', '2012-02-10 10:11:53', 'Input is been filtered. What is been filtered out is been completly removed. The filter does not act in a recurrive fashion so with enough nested javascript triggers, it can be defeated. To complete this challenge, enter the following attack string;<br/>&lt;input type=&quot;button&quot; oncliconcliconcliconcliconclickkkkk=&quot;alert(&#39;XSS&#39;)&quot;/&gt;');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('d51277769f9452b6508a3a22d9f52bea3b0ff84d', 'f771a10efb42a79a9dba262fd2be2e44bf40b66d', '2012-02-10 10:11:53', 'To complete this challenge, the following attack string will return all rows from the table:<br/>&#39; &#39; OR &#39;1&#39; = &#39;1<br/>The filter is implemented very poorly for this challenge, as it only removes the first apostraphy in a string, rather than a recursive funciton.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('e4cb1c92453cf0e6adb5fe0e66abd408cb5b76ea', 'ac944b716c1ec5603f1d09c68e7a7e6e75b0d601', '2012-02-10 10:11:53', 'A step by step guid is not yet available for this lesson. You will need a tool like <a>Wire Shark</a> and you will need to search for the packet with the result key! The packet is broadcasted with UDP.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('e7e44ba680b2ab1f6958b1344c9e43931b81164a', '5dda8dc216bd6a46fccaa4ed45d49404cdc1c82e', '2012-02-10 10:11:53', 'To complete this challenge, you must craft a second statment to return Mary Martin\'\'s credit card number as the current statement only returns the customerName attribute. The following string will perform this; </br> &#39; UNION ALL SELECT creditCardNumber FROM customers WHERE customerName = &#39;Mary Martin<br/> The filter in this challenge is difficult to get around. But the \'\'UNION\'\' operator is not been filtered. Using the UNION command you are able to return the results of custom statements.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('f392e5a69475b14fbe5ae17639e174f379c0870e', '201ae6f8c55ba3f3b5881806387fbf34b15c30c2', '2012-02-10 10:11:53', 'The lesson is encoded in Base64. Most proxy applicaitons include a decoder for this encoding.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('6afa50948e10466e9a94c7c2b270b3f958e412c6', '82e8e9e2941a06852b90c97087309b067aeb2c4c', '2012-02-10 10:11:53', "The user Id\'s inthis challenge are hashed using MD5. You can google the ID\'s to find their plain text if you have an internet connection to find their plain text. The sequence of ID\'\'s is as follows;<br/>2, 3, 5, 7, 9, 11<br/>The next number in the sequenceis 13. Modify the request with a proxy so that the id is the MD5 of 13 (c51ce410c124a10e0db5e4b97fc2af39)");

COMMIT;

-- -----------------------------------------------------
-- Data for table `core`.`sequence`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `core`;
INSERT INTO `core`.`sequence` (`tableName`, `currVal`) VALUES ('users', '282475249');
INSERT INTO `core`.`sequence` (`tableName`, `currVal`) VALUES ('cheatSheet', '282475299');
INSERT INTO `core`.`sequence` (`tableName`, `currVal`) VALUES ('class', '282475249');
INSERT INTO `core`.`sequence` (`tableName`, `currVal`) VALUES ('modules', '282475576');

COMMIT;



-- Default admin user

call userCreate(null, 'admin', 'password', 'admin', 'admin@securityShepherd.org', true);

-- Enable backup script

DROP DATABASE IF EXISTS backup;
CREATE DATABASE backup; 

SET GLOBAL event_scheduler = ON;
SET @@global.event_scheduler = ON;
SET GLOBAL event_scheduler = 1;
SET @@global.event_scheduler = 1;

USE core;
DELIMITER $$


drop event IF EXISTS update_status;

create event update_status
on schedule every 1 minute
do

BEGIN

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

drop table IF EXISTS `backup`.`users`;
drop table IF EXISTS `backup`.`class`;
drop table IF EXISTS `backup`.`modules`;
drop table IF EXISTS `backup`.`results`;
drop table IF EXISTS `backup`.`cheatsheet`;
drop table IF EXISTS `backup`.`sequence`;
-- -----------------------------------------------------
-- Table `core`.`class`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `backup`.`class` (
  `classId` VARCHAR(64) NOT NULL ,
  `className` VARCHAR(32) NOT NULL ,
  `classYear` VARCHAR(5) NOT NULL ,
  PRIMARY KEY (`classId`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `backup`.`users` (
  `userId` VARCHAR(64) NOT NULL ,
  `classId` VARCHAR(64) NULL ,
  `userName` VARCHAR(32) NOT NULL ,
  `userPass` VARCHAR(512) NOT NULL ,
  `userRole` VARCHAR(32) NOT NULL ,
  `badLoginCount` INT NOT NULL DEFAULT 0 ,
  `suspendedUntil` DATETIME NOT NULL DEFAULT '1000-01-01 00:00:00' ,
  `userAddress` VARCHAR(128) NULL ,
  `tempPassword` TINYINT(1)  NULL DEFAULT FALSE ,
  `userScore` INT NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`userId`) ,
  INDEX `classId` (`classId` ASC) ,
  UNIQUE INDEX `userName_UNIQUE` (`userName` ASC) ,
  CONSTRAINT `classId`
    FOREIGN KEY (`classId` )
    REFERENCES `backup`.`class` (`classId` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;



-- -----------------------------------------------------
-- Table `core`.`modules`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `backup`.`modules` (
  `moduleId` VARCHAR(64) NOT NULL ,
  `moduleName` VARCHAR(64) NOT NULL ,
  `moduleType` VARCHAR(16) NOT NULL ,
  `moduleCategory` VARCHAR(64) NULL ,
  `moduleResult` VARCHAR(256) NULL ,
  `moduleHash` VARCHAR(256) NULL ,
  `incrementalRank` INT NULL ,
  `scoreValue` INT NOT NULL DEFAULT 50 ,
  `scoreBonus` INT NOT NULL DEFAULT 5 ,
  PRIMARY KEY (`moduleId`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`results`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `backup`.`results` (
  `userId` VARCHAR(64) NOT NULL ,
  `moduleId` VARCHAR(64) NOT NULL ,
  `startTime` DATETIME NOT NULL ,
  `finishTime` DATETIME NULL ,
  `csrfCount` INT NULL DEFAULT 0 ,
  `resultSubmission` LONGTEXT NULL ,
  `knowledgeBefore` INT NULL ,
  `knowledgeAfter` INT NULL ,
  `difficulty` INT NULL ,
  PRIMARY KEY (`userId`, `moduleId`) ,
  INDEX `fk_Results_Modules1` (`moduleId` ASC) ,
  CONSTRAINT `fk_Results_users1`
    FOREIGN KEY (`userId` )
    REFERENCES `backup`.`users` (`userId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Results_Modules1`
    FOREIGN KEY (`moduleId` )
    REFERENCES `backup`.`modules` (`moduleId` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cheatSheet`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `backup`.`cheatSheet` (
  `cheatSheetId` VARCHAR(64) NOT NULL ,
  `moduleId` VARCHAR(64) NOT NULL ,
  `createDate` DATETIME NOT NULL ,
  `solution` LONGTEXT NOT NULL ,
  PRIMARY KEY (`cheatSheetId`, `moduleId`) ,
  INDEX `fk_CheatSheet_Modules1` (`moduleId` ASC) ,
  CONSTRAINT `fk_CheatSheet_Modules1`
    FOREIGN KEY (`moduleId` )
    REFERENCES `backup`.`modules` (`moduleId` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`sequence`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `backup`.`sequence` (
  `tableName` VARCHAR(32) NOT NULL ,
  `currVal` BIGINT(20) NOT NULL DEFAULT 282475249 ,
  PRIMARY KEY (`tableName`) )
ENGINE = InnoDB;



Insert into `backup`.`class` (Select * from `core`.`class`);
Insert into `backup`.`users` (Select * from `core`.`users`);
Insert into `backup`.`modules` (Select * from `core`.`modules`);
Insert into `backup`.`results` (Select * from `core`.`results`);
Insert into `backup`.`cheatSheet` (Select * from `core`.`cheatSheet`);
Insert into `backup`.`sequence` (Select * from `core`.`sequence`);

END $$

DELIMITER ;

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `core` ;
CREATE SCHEMA IF NOT EXISTS `core` DEFAULT CHARACTER SET latin1 ;
USE `core` ;

-- -----------------------------------------------------
-- Table `core`.`class`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `core`.`class` (
  `classId` VARCHAR(64) NOT NULL ,
  `className` VARCHAR(32) NOT NULL ,
  `classYear` VARCHAR(5) NOT NULL ,
  PRIMARY KEY (`classId`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `core`.`users` (
  `userId` VARCHAR(64) NOT NULL ,
  `classId` VARCHAR(64) NULL ,
  `userName` VARCHAR(32) NOT NULL ,
  `userPass` VARCHAR(512) NOT NULL ,
  `userRole` VARCHAR(32) NOT NULL ,
  `badLoginCount` INT NOT NULL DEFAULT 0 ,
  `suspendedUntil` DATETIME NOT NULL DEFAULT '1000-01-01 00:00:00' ,
  `userAddress` VARCHAR(128) NULL ,
  `tempPassword` TINYINT(1)  NULL DEFAULT FALSE ,
  `userScore` INT NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`userId`) ,
  INDEX `classId` (`classId` ASC) ,
  UNIQUE INDEX `userName_UNIQUE` (`userName` ASC) ,
  CONSTRAINT `classId`
    FOREIGN KEY (`classId` )
    REFERENCES `core`.`class` (`classId` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`modules`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `core`.`modules` (
  `moduleId` VARCHAR(64) NOT NULL ,
  `moduleName` VARCHAR(64) NOT NULL ,
  `moduleType` VARCHAR(16) NOT NULL ,
  `moduleCategory` VARCHAR(64) NULL ,
  `moduleResult` VARCHAR(256) NULL ,
  `moduleHash` VARCHAR(256) NULL ,
  `moduleStatus` VARCHAR(16) NULL DEFAULT 'open' ,
  `incrementalRank` INT NULL DEFAULT 200,
  `scoreValue` INT NOT NULL DEFAULT 50 ,
  `scoreBonus` INT NOT NULL DEFAULT 5 ,
  `hardcodedKey` TINYINT(1) NOT NULL DEFAULT TRUE,
  PRIMARY KEY (`moduleId`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`results`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `core`.`results` (
  `userId` VARCHAR(64) NOT NULL ,
  `moduleId` VARCHAR(64) NOT NULL ,
  `startTime` DATETIME NOT NULL ,
  `finishTime` DATETIME NULL ,
  `csrfCount` INT NULL DEFAULT 0 ,
  `resultSubmission` LONGTEXT NULL ,
  `knowledgeBefore` INT NULL ,
  `knowledgeAfter` INT NULL ,
  `difficulty` INT NULL ,
  PRIMARY KEY (`userId`, `moduleId`) ,
  INDEX `fk_Results_Modules1` (`moduleId` ASC) ,
  CONSTRAINT `fk_Results_users1`
    FOREIGN KEY (`userId` )
    REFERENCES `core`.`users` (`userId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Results_Modules1`
    FOREIGN KEY (`moduleId` )
    REFERENCES `core`.`modules` (`moduleId` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cheatSheet`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `core`.`cheatsheet` (
  `cheatSheetId` VARCHAR(64) NOT NULL ,
  `moduleId` VARCHAR(64) NOT NULL ,
  `createDate` DATETIME NOT NULL ,
  `solution` LONGTEXT NOT NULL ,
  PRIMARY KEY (`cheatSheetId`, `moduleId`) ,
  INDEX `fk_CheatSheet_Modules1` (`moduleId` ASC) ,
  CONSTRAINT `fk_CheatSheet_Modules1`
    FOREIGN KEY (`moduleId` )
    REFERENCES `core`.`modules` (`moduleId` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `core`.`sequence`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `core`.`sequence` (
  `tableName` VARCHAR(32) NOT NULL ,
  `currVal` BIGINT(20) NOT NULL DEFAULT 282475249 ,
  PRIMARY KEY (`tableName`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cheatsheet`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `core`.`cheatsheet` (
  `cheatSheetId` VARCHAR(64) NOT NULL ,
  `moduleId` VARCHAR(64) NOT NULL ,
  `createDate` DATETIME NOT NULL ,
  `solution` LONGTEXT NOT NULL ,
  PRIMARY KEY (`cheatSheetId`, `moduleId`) ,
  INDEX `fk_CheatSheet_Modules1` (`moduleId` ASC) ,
  CONSTRAINT `fk_CheatSheet_Modules1`
    FOREIGN KEY (`moduleId` )
    REFERENCES `core`.`modules` (`moduleId` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- procedure authUser
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`authUser` (IN theName VARCHAR(32), IN theHash VARCHAR(512))
BEGIN
DECLARE theDate DATETIME;
COMMIT;
SELECT NOW() FROM DUAL INTO theDate;
SELECT userId, userName, userRole, badLoginCount, tempPassword, classId FROM `users`
    WHERE userName = theName 
    AND userPass = SHA2(theHash, 512)
    AND suspendedUntil < theDate ;
END


















$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userLocked
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userLocked` (IN theName VARCHAR(32))
BEGIN
DECLARE theDate DATETIME;
COMMIT;
SELECT NOW() FROM DUAL INTO theDate;
SELECT userName FROM `users` 
    WHERE userName = theName
    AND theDate > suspendedUntil;
END













$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userLock
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userLock` (theName VARCHAR(32))
BEGIN
DECLARE theDate DATETIME;
DECLARE untilDate DATETIME;
DECLARE theCount INT;

COMMIT;
SELECT NOW() FROM DUAL INTO theDate;
-- Get the badLoginCount from users if they are not suspended already or account has attempted a login within the last 10 mins
SELECT badLoginCount FROM `users` 
    WHERE userName = theName
    AND suspendedUntil < (theDate - '0000-00-00 00:10:00')
    INTO theCount;
    
SELECT suspendedUntil FROM `users` 
    WHERE userName = theName
    AND suspendedUntil < (theDate - '0000-00-00 00:10:00')
    INTO untilDate;
IF (untilDate < theDate) THEN
    IF (theCount >= 3) THEN
        -- Set suspended until 30 mins from now
        UPDATE `users` SET
            suspendedUntil = TIMESTAMPADD(MINUTE, 30, theDate),
            badLoginCount = 0
            WHERE userName = theName;
        COMMIT;
    -- ELSE the user is already suspended, or theCount < 3
    ELSE
        -- Get user where their last bad login was within 10 mins ago
        SELECT COUNT(userId) FROM users
            WHERE userName = theName
            AND suspendedUntil < (theDate - '0000-00-00 00:10:00')
            INTO theCount;
            
        -- IF a user was counted then they are not suspended, but have attemped a bad login within 10 mins of their last
        IF (theCount > 0) THEN 
            UPDATE `users` SET
                badLoginCount = (badLoginCount + 1),
                suspendedUntil = theDate
                WHERE userName = theName;
            COMMIT;
        -- ELSE this is the first time within 10 mins that this account has logged in bad
        ELSE
            UPDATE `users` SET
                badLoginCount = 1,
                suspendedUntil = theDate
                WHERE userName = theName;
            COMMIT;
        END IF;
    END IF;
END IF;
END











$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userFind
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userFind` (IN theName VARCHAR(32))
BEGIN
COMMIT;
SELECT userName, suspendedUntil FROM `users`
    WHERE userName = theName;
END




$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure playerCount
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`playerCount` ()
BEGIN
    COMMIT;
    SELECT count(userId) FROM users
        WHERE userRole = 'player';
END


$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userCreate
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userCreate` (IN theClassId VARCHAR(64), IN theUserName VARCHAR(32), IN theUserPass VARCHAR(512), IN theUserRole VARCHAR(32), IN theUserAddress VARCHAR(128), tempPass BOOLEAN)
BEGIN
    DECLARE theId VARCHAR(64);
    DECLARE theClassCount INT;    
    
    COMMIT;
    -- If (Valid User Type) AND (classId = null or (Valid Class Id)) Then create user
    IF (theUserRole = 'player' OR theUserRole = 'admin') THEN
        IF (theClassId != null) THEN
            SELECT count(classId) FROM class
                WHERE classId = theClassId
                INTO theClassCount;
            IF (theClassCount != 1) THEN
                SELECT null FROM DUAL INTO theClassId;
            END IF;
        END IF;
                
        -- Increment sequence for users table
        UPDATE sequence SET
            currVal = currVal + 1
            WHERE tableName = 'users';
        COMMIT;
        SELECT SHA(CONCAT(currVal, tableName)) FROM sequence
            WHERE tableName = 'users'
            INTO theId;
        
        -- Insert the values, badLoginCount and suspendedUntil Values will use the defaults defined by the table
        INSERT INTO users (
                userId,
                classId,
                userName,
                userPass,
                userRole,
                userAddress,
                tempPassword
            ) VALUES (
                theId,
                theClassId,
                theUserName,
                SHA2(theUserPass, 512),
                theUserRole,
                theUserAddress,
                tempPass
            );
        COMMIT;
        SELECT null FROM DUAL;
    ELSE
        SELECT 'Invalid Role Type Detected' FROM DUAL;
    END IF;
END
















$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userBadLoginReset
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userBadLoginReset` (IN theUserId VARCHAR(45))
BEGIN
    COMMIT;
    UPDATE users SET
        badLoginCount = 0
        WHERE userId = theUserId;
    COMMIT;
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userPasswordChange
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userPasswordChange` (IN theUserName VARCHAR(32), IN currentPassword VARCHAR(512), IN newPassword VARCHAR(512))
BEGIN
DECLARE theDate DATETIME;
COMMIT;
SELECT NOW() FROM DUAL INTO theDate;
UPDATE users SET
    userPass = SHA2(newPassword, 512),
    tempPassword = FALSE
    WHERE userPass = SHA2(currentPassword, 512)
    AND userName = theUserName
    AND suspendedUntil < theDate;
COMMIT;
END



$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure classCreate
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`classCreate` (IN theClassName VARCHAR(32), IN theClassYear VARCHAR(5))
BEGIN
    DECLARE theId VARCHAR(64);
    COMMIT;
    UPDATE sequence SET
        currVal = currVal + 1
        WHERE tableName = 'users';
    COMMIT;
    SELECT SHA(CONCAT(currVal, tableName)) FROM sequence
        WHERE tableName = 'users'
        INTO theId;
    
    INSERT INTO class VALUES (theId, theClassName, theClassYear);
END





$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure classCount
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`classCount` ()
BEGIN
    SELECT count(ClassId) FROM class;
END$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure classesGetData
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`classesGetData` ()
BEGIN
    SELECT classId, className, classYear FROM class;
END$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure classFind
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`classFind` (IN theClassId VARCHAR(64))
BEGIN
    SELECT className, classYear FROM class
        WHERE classId = theClassId;
END$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure playersByClass
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`playersByClass` (IN theClassId VARCHAR(64))
BEGIN
    COMMIT;
    SELECT userId, userName, userAddress FROM users
        WHERE classId = theClassId
        AND userRole = 'player'
        ORDER BY userName;
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure playerUpdateClass
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`playerUpdateClass` (IN theUserId VARCHAR(64), IN theClassId VARCHAR(64))
BEGIN
COMMIT;
UPDATE users SET
    classId = theClassId
    WHERE userId = theUserId
    AND userRole = 'player';
COMMIT;
SELECT userName FROM users
    WHERE userId = theUserId
    AND classId = theClassId
    AND userRole = 'player';
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure playerFindById
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`playerFindById` (IN playerId VARCHAR(64))
BEGIN
COMMIT;
SELECT userName FROM users
    WHERE userId = playerId
    AND userRole = 'player';
END$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure playersWithoutClass
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`playersWithoutClass` ()
BEGIN
    COMMIT;
    SELECT userId, userName, userAddress FROM users
        WHERE classId is NULL
        AND userRole = 'player'
        ORDER BY userName;
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure playerUpdateClassToNull
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`playerUpdateClassToNull` (IN theUserId VARCHAR(45))
BEGIN
COMMIT;
UPDATE users SET
    classId = NULL
    WHERE userId = theUserId
    AND userRole = 'player';
COMMIT;
SELECT userName FROM users
    WHERE userId = theUserId
    AND classId IS NULL
    AND userRole = 'player';
END
$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userUpdateRole
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userUpdateRole` (IN theUserId VARCHAR(64), IN theNewRole VARCHAR(32))
BEGIN
COMMIT;
UPDATE users SET
    userRole = theNewRole
    WHERE userId = theUserId;
COMMIT;
SELECT userName FROM users
    WHERE userId = theUserId;
END
$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleCreate
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleCreate` (IN theModuleName VARCHAR(64), theModuleType VARCHAR(16), theModuleCategory VARCHAR(64), isHardcodedKey BOOLEAN, theModuleSolution VARCHAR(256))
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
IF (isHardcodedKey IS NULL) THEN
    SELECT TRUE FROM DUAL
        INTO isHardcodedKey;
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
        moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, hardcodedKey
    )VALUES(
        theId, theModuleName, theModuleType, theModuleCategory, theModuleSolution, SHA2(CONCAT(theModuleName, theId), 256), isHardcodedKey
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
-- -----------------------------------------------------
-- procedure moduleAllInfo
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleAllInfo` (IN theType VARCHAR(64), IN theUserId VARCHAR(64))
BEGIN
(SELECT moduleName, moduleCategory, moduleId, finishTime
FROM modules LEFT JOIN results USING (moduleId) WHERE userId = theUserId AND moduleType = theType) UNION (SELECT moduleName, moduleCategory, moduleId, null FROM modules WHERE moduleId NOT IN (SELECT moduleId FROM modules JOIN results USING (moduleId) WHERE userId = theUserId AND moduleType = theType) AND moduleType = theType) ORDER BY moduleCategory, moduleName;
END











$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleGetResult
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleGetResult` (IN theModuleId VARCHAR(64))
BEGIN
COMMIT;
SELECT moduleName, moduleResult FROM modules
    WHERE moduleId = theModuleId
    AND moduleResult IS NOT NULL;
END



$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userUpdateResult
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userUpdateResult` (IN theModuleId VARCHAR(64), IN theUserId VARCHAR(64), IN theBefore INT, IN theAfter INT, IN theDifficulty INT, IN theAdditionalInfo LONGTEXT)
BEGIN
DECLARE theDate TIMESTAMP;
DECLARE theBonus INT;
DECLARE totalScore INT;
COMMIT;
SELECT NOW() FROM DUAL
    INTO theDate;
-- Get current bonus and decrement the bonus value
SELECT 0 FROM DUAL INTO totalScore;
SELECT scoreBonus FROM modules
    WHERE moduleId = theModuleId
    INTO theBonus;
IF (theBonus > 0) THEN
    SELECT (totalScore + theBonus) FROM DUAL
        INTO totalScore;
    UPDATE modules SET 
        scoreBonus = scoreBonus - 1
        WHERE moduleId = theModuleId;
    COMMIT;
END IF;

-- Get the Score value for the level
SELECT (totalScore + scoreValue) FROM modules
    WHERE moduleId = theModuleId
    INTO totalScore;
    
-- Update users score
UPDATE users SET
    userScore = userScore + totalScore
    WHERE userId = theUserId;
COMMIT;

-- Update result row
UPDATE results SET
    finishTime = theDate,
    `knowledgeBefore` = theBefore,
    `knowledgeAfter` = theAfter,
    `difficulty`  = theDifficulty,
    `resultSubmission` = theAdditionalInfo
    WHERE startTime IS NOT NULL
    AND finishTime IS NULL
    AND userId = theUserId
    AND moduleId = theModuleId;
COMMIT;
SELECT moduleName FROM modules
    JOIN results USING (moduleId)
    WHERE startTime IS NOT NULL
    AND finishTime IS NOT NULL
    AND userId = theUserId
    AND moduleId = theModuleId;
END $$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleGetHash
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleGetHash` (IN theModuleId VARCHAR(64), IN theUserId VARCHAR(64))
BEGIN
DECLARE theDate DATETIME;
DECLARE tempInt INT;
COMMIT;
SELECT NOW() FROM DUAL
    INTO theDate;
SELECT COUNT(*) FROM results
    WHERE userId = theUserId
    AND moduleId = theModuleId
    AND startTime IS NOT NULL
    INTO tempInt;
IF(tempInt = 0) THEN
    INSERT INTO results 
        (moduleId, userId, startTime)
        VALUES
        (theModuleId, theUserId, theDate);
    COMMIT;
END IF;
SELECT moduleHash, moduleCategory, moduleType FROM modules
    WHERE moduleId = theModuleId;
END





$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleGetResultFromHash
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleGetResultFromHash` (IN theHash VARCHAR(256))
BEGIN
COMMIT;
SELECT moduleResult FROM modules
    WHERE moduleHash = theHash;
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure resultMessageByClass
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`resultMessageByClass` (IN theClassId VARCHAR(64), IN theModuleId VARCHAR(64))
BEGIN
COMMIT;
SELECT userName, resultSubmission FROM results
    JOIN users USING (userId)
    JOIN class USING (classId)
    WHERE classId = theClassId
    AND moduleId = theModuleId;
END




$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure resultMessageSet
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`resultMessageSet` (IN theMessage VARCHAR(128), IN theUserId VARCHAR(64), IN theModuleId VARCHAR(64))
BEGIN
COMMIT;
UPDATE results SET
    resultSubmission = theMessage
    WHERE moduleId = theModuleId
    AND userId = theUserId;
COMMIT;
END




$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure resultMessagePlus
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`resultMessagePlus` (IN theModuleId VARCHAR(64), IN theUserId2 VARCHAR(64))
BEGIN
DECLARE temp INT;
COMMIT;
SELECT csrfCount FROM results
    WHERE userId = theUserId2
    AND moduleId = theModuleId
    INTO temp;
IF (temp = 0) THEN
    CALL moduleComplete(theModuleId, theUserId2);
END IF;
UPDATE results SET
    csrfCount = csrfCount + 1
    WHERE userId = theUserId2
    AND moduleId = theModuleId;
COMMIT;
END





$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleGetIdFromHash
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleGetIdFromHash` (IN theHash VARCHAR(256))
BEGIN
COMMIT;
SELECT moduleId FROM modules
    WHERE moduleHash = theHash;
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userGetNameById
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userGetNameById` (IN theUserId VARCHAR(64))
BEGIN
COMMIT;
SELECT userName FROM users
    WHERE userId = theUserId;
END
$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleComplete
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleComplete` (IN theModuleId VARCHAR(64), IN theUserId VARCHAR(64))
BEGIN
DECLARE theDate DATETIME;
COMMIT;
SELECT NOW() FROM DUAL
    INTO theDate;
UPDATE results SET
    finishTime = theDate
    WHERE startTime IS NOT NULL
    AND moduleId = theModuleId
    AND userId = theUserId;
COMMIT;
END




$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure cheatSheetCreate
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`cheatSheetCreate` (IN theModule VARCHAR(64), IN theSheet LONGTEXT)
BEGIN
DECLARE theDate DATETIME;
DECLARE theId VARCHAR(64);
    COMMIT;
    UPDATE sequence SET
        currVal = currVal + 1
        WHERE tableName = 'cheatSheet';
    COMMIT;
    SELECT SHA(CONCAT(currVal, tableName)) FROM sequence
        WHERE tableName = 'cheatSheet'
        INTO theId;
    SELECT NOW() FROM DUAL INTO theDate;
    INSERT INTO cheatSheet
        (cheatSheetId, moduleId, createDate, solution)
        VALUES
        (theId, theModule, theDate, theSheet);
    COMMIT;
END




$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleGetAll
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleGetAll` ()
BEGIN
COMMIT;
SELECT moduleId, moduleName, moduleType, moduleCategory FROM modules
    ORDER BY moduleType, moduleCategory, moduleName;
END
$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure cheatSheetGetSolution
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`cheatSheetGetSolution` (IN theModuleId VARCHAR(64))
BEGIN
COMMIT;
SELECT moduleName, solution FROM modules
    JOIN cheatsheet USING (moduleId)
    WHERE moduleId = theModuleID
    ORDER BY createDate DESC;
END
$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleGetHashById
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleGetHashById` (IN theModuleId VARCHAR(64))
BEGIN
COMMIT;
SELECT moduleHash FROM modules
    WHERE moduleId = theModuleId;
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userCheckResult
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userCheckResult` (IN theModuleId VARCHAR(64), IN theUserId VARCHAR(64))
BEGIN
COMMIT;
-- Returns a module Name if the user has not completed the module identified by moduleId
SELECT moduleName FROM results
    JOIN modules USING(moduleId)
    WHERE finishTime IS NULL
    AND startTime IS NOT NULL
    AND finishTime IS NULL
    AND userId = theUserId
    AND moduleId = theModuleId;
END


$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleIncrementalInfo
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleIncrementalInfo` (IN theUserId VARCHAR(64))
BEGIN
(SELECT moduleName, moduleCategory, moduleId, finishTime, incrementalRank FROM modules LEFT JOIN results USING (moduleId) WHERE userId = theUserId) UNION (SELECT moduleName, moduleCategory, moduleId, null, incrementalRank FROM modules WHERE moduleId NOT IN (SELECT moduleId FROM modules JOIN results USING (moduleId) WHERE userId = theUserId)) ORDER BY incrementalRank;
END




$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleFeedback
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleFeedback` (IN theModuleId VARCHAR(64))
BEGIN
SELECT userName, TIMESTAMPDIFF(MINUTE, finishTime, startTime)*(-1), difficulty, knowledgeBefore, knowledgeAfter, resultSubmission
	FROM modules 
	LEFT JOIN results USING (moduleId)
  LEFT JOIN users USING (userId)
  WHERE moduleId = theModuleId;
END
$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userProgress
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userProgress` (IN theClassId VARCHAR(64))
BEGIN
    COMMIT;
SELECT userName, count(finishTime), userScore FROM users JOIN results USING (userId) WHERE finishTime IS NOT NULL 
AND classId = theClassId
GROUP BY userName UNION SELECT userName, 0, userScore FROM users WHERE classId = theClassId AND userId NOT IN (SELECT userId FROM users JOIN results USING (userId) WHERE classId = theClassId AND finishTime IS NOT NULL GROUP BY userName) ORDER BY userScore DESC;
END





$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userStats
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userStats` (IN theUserName VARCHAR(32))
BEGIN
DECLARE temp INT;
SELECT COUNT(*) FROM modules INTO temp;
SELECT userName, sum(TIMESTAMPDIFF(MINUTE, finishTime, startTime)*(-1)) AS "Time", CONCAT(COUNT(*),"/", temp) AS "Progress"
    FROM modules
    LEFT JOIN results USING (moduleId)
    LEFT JOIN users USING (userId)
    WHERE userName = theUserName AND resultSubmission IS NOT NULL
    GROUP BY userName;
END


$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userStatsDetailed
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userStatsDetailed` (IN theUserName VARCHAR(32))
BEGIN
DECLARE temp INT;
SELECT COUNT(*) FROM modules INTO temp;
SELECT userName, moduleName, TIMESTAMPDIFF(MINUTE, finishTime, startTime)*(-1) AS "Time"
    FROM modules
    LEFT JOIN results USING (moduleId)
    LEFT JOIN users USING (userId)
    WHERE userName = theUserName AND resultSubmission IS NOT NULL
    ORDER BY incrementalRank;
END



$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleOpenInfo
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleOpenInfo` (IN theUserId VARCHAR(64))
BEGIN
(SELECT moduleName, moduleCategory, moduleId, finishTime FROM modules LEFT JOIN results USING (moduleId) 
WHERE userId = theUserId AND moduleStatus = 'open') UNION (SELECT moduleName, moduleCategory, moduleId, null FROM modules WHERE moduleId NOT IN (SELECT moduleId FROM modules JOIN results USING (moduleId) WHERE userId = theUserId AND moduleStatus = 'open') AND moduleStatus = 'open') ORDER BY moduleCategory, moduleName;
END

$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleClosednfo
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleClosednfo` (IN theUserId VARCHAR(64))
BEGIN
(SELECT moduleName, moduleCategory, moduleId, finishTime
FROM modules LEFT JOIN results USING (moduleId) WHERE userId = theUserId AND moduleStatus = 'closed') UNION (SELECT moduleName, moduleCategory, moduleId, null FROM modules WHERE moduleId NOT IN (SELECT moduleId FROM modules JOIN results USING (moduleId) WHERE userId = theUserId AND moduleStatus = 'closed') AND moduleStatus = 'closed') ORDER BY moduleCategory, moduleName;
END
$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleSetStatus
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleSetStatus` (IN theModuleId VARCHAR(64), IN theStatus VARCHAR(16))
BEGIN
UPDATE modules SET
    moduleStatus = theStatus
    WHERE moduleId = theModuleId;
COMMIT;
END
$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure moduleAllStatus
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleAllStatus` ()
BEGIN
SELECT moduleId, moduleName, moduleStatus
    FROM modules;
END
$$

DELIMITER ;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `core`.`modules`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `core`;
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('544aa22d3dd16a8232b093848a6523b0712b23da', 'SQL Injection 2', 'challenge', 'Injection', 'fd8e9a29dab791197115b58061b215594211e72c1680f1eacc50b0394133a09f', 'e1e109444bf5d7ae3d67b816538613e64f7d0f51c432a164efc8418513711b0a', 'open', '135', '50', '5', 1);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('5dda8dc216bd6a46fccaa4ed45d49404cdc1c82e', 'SQL Injection 3', 'challenge', 'Injection', '9815 1547 3214 7569', 'b7327828a90da59df54b27499c0dc2e875344035e38608fcfb7c1ab8924923f6', 'open', '205', '50', '5', 1);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('6319a2e38cc4b2dc9e6d840e1b81db11ee8e5342', 'Cross Site Scripting 3', 'challenge', 'XSS', '6abaf491c9122db375533c04df', 'ad2628bcc79bf10dd54ee62de148ab44b7bd028009a908ce3f1b4d019886d0e', 'open', '195', '50', '5', 0);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('cd7f70faed73d2457219b951e714ebe5775515d8', 'Cross Site Scripting 1', 'challenge', 'XSS', '445d0db4a8fc5d4acb164d022b', 'd72ca2694422af2e6b3c5d90e4c11e7b4575a7bc12ee6d0a384ac2469449e8fa', 'open', '25', '50', '5', 0);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('d4e2c37d8f1298fcaf4edcea7292cb76e9eab09b', 'Cross Site Scripting 2', 'challenge', 'XSS', '495ab8cc7fe9532c6a75d378de', 't227357536888e807ff0f0eff751d6034bafe48954575c3a6563cb47a85b1e888', 'open', '115', '50', '5', 0);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('f771a10efb42a79a9dba262fd2be2e44bf40b66d', 'SQL Injection 1', 'challenge', 'Injection', 'f62abebf5658a6a44c5c9babc7865110c62f5ecd9d0a7052db48c4dbee0200e3', 'ffd39cb26727f34cbf9fce3e82b9d703404e99cdef54d2aa745f497abe070b', 'open', '125', '50', '5', 1);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('408610f220b4f71f7261207a17055acbffb8a747', 'SQL Injection', 'lesson', 'Injection', '3c17f6bf34080979e0cebda5672e989c07ceec9fa4ee7b7c17c9e3ce26bc63e0', 'e881086d4d8eb2604d8093d93ae60986af8119c4f643894775433dbfb6faa594', 'open', '75', '50', '5', 1);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('ca8233e0398ecfa76f9e05a49d49f4a7ba390d07', 'Cross Site Scripting', 'lesson', 'XSS', 'ea7b563b2935d8587539d747d', 'zf8ed52591579339e590e0726c7b24009f3ac54cdff1b81a65db1688d86efb3a', 'open', '15', '50', '5', 0);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('453d22238401e0bf6f1ff5d45996407e98e45b07', 'Cross Site Request Forgery', 'lesson', 'CSRF', '666980771c29857b8a84c686751ce7edaae3d6ac0b00a55895926c748453ef71', 'ed4182af119d97728b2afca6da7cdbe270a9e9dd714065f0f775cd40dc296bc7', 'open', '55', '50', '5', 0);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('20e755179a5840be5503d42bb3711716235005ea', 'CSRF 1', 'challenge', 'CSRF', '7639c952a191d569a0c741843b599604c37e33f9f5d8eb07abf0254635320b07', 's74a796e84e25b854906d88f622170c1c06817e72b526b3d1e9a6085f429cf52', 'open', '155', '50', '5', 1);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('94cd2de560d89ef59fc450ecc647ff4d4a55c15d', 'CSRF 2', 'challenge', 'CSRF', '45309dbaf8eaf6d1a5f1ecb1bf1b6be368a6542d3da35b9bf0224b88408dc001', 'z311736498a13604705d608fb3171ebf49bc18753b0ec34b8dff5e4f9147eb5e', 'open', '215', '50', '5', 1);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('5ca9115f3279b9b9f3308eb6a59a4fcd374846d6', 'CSRF 3', 'challenge', 'CSRF', '6bdbe1901cbe2e2749f347efb9ec2be820cc9396db236970e384604d2d55b62a', 'z6b2f5ebbe112dd09a6c430a167415820adc5633256a7b44a7d1e262db105e3c', 'open', '235', '50', '5', 1);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('9533e21e285621a676bec58fc089065dec1f59f5', 'Broken Session Management', 'lesson', 'Session Management', '6594dec9ff7c4e60d9f8945ca0d4', 'b8c19efd1a7cc64301f239f9b9a7a32410a0808138bbefc98986030f9ea83806', 'open', '10', '50', '5', 0);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('a4bf43f2ba5ced041b03d0b3f9fa3541c520d65d', 'Session Management Challenge 1', 'challenge', 'Session Management', 'db7b1da5d7a43c7100a6f01bb0c', 'dfd6bfba1033fa380e378299b6a998c759646bd8aea02511482b8ce5d707f93a', 'open', '105', '50', '5', 0);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('b70a84f159876bb9885b6e0087d22f0a52abbfcf', 'Session Management Challenge 2', 'challenge', 'Session Management', '4ba31e5ffe29de092fe1950422a', 'd779e34a54172cbc245300d3bc22937090ebd3769466a501a5e7ac605b9f34b7', 'open', '145', '50', '5', 0);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('0e9e650ffca2d1fe516c5d7b0ce5c32de9e53d1e', 'Session Management Challenge 3', 'challenge', 'Session Management', 'e62008dc47f5eb065229d48963', 't193c6634f049bcf65cdcac72269eeac25dbb2a6887bdb38873e57d0ef447bc3', 'open', '165', '50', '5', 0);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('0dbea4cb5811fff0527184f99bd5034ca9286f11', 'Insecure Direct Object References', 'lesson', 'Insecure Direct Object References', '59e571b1e59441e76e0c85e5b49', 'fdb94122d0f032821019c7edf09dc62ea21e25ca619ed9107bcc50e4a8dbc100', 'open', '5', '50', '5', 0);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('2dc909fd89c2b03059b1512e7b54ce5d1aaa4bb4', 'Insecure Direct Object Reference Challenge 1', 'challenge', 'Insecure Direct Object References', 'dd6301b38b5ad9c54b85d07c087aebec89df8b8c769d4da084a55663e6186742', 'o9a450a64cc2a196f55878e2bd9a27a72daea0f17017253f87e7ebd98c71c98c', 'open', '95', '50', '5', 1);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('b6432a6b5022cb044e9946315c44ab262ab59e88', 'Unvalidated Redirects and Forwards', 'lesson', 'Unvalidated Redirects and Forwards', '658c43abcf81a61ca5234cfd7a2', 'f15f2766c971e16e68aa26043e6016a0a7f6879283c873d9476a8e7e94ea736f', 'open', '65', '50', '5', 0);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('ac944b716c1ec5603f1d09c68e7a7e6e75b0d601', 'Insufficient Transport Layer Protection', 'lesson', 'Insufficient Transport Layer Protection', '15e83da388267da584954d4fe5a127be3dff117eaee7a97fcda40e61f3c2868b', 'ts906dc0c3dbc3eaaaf6da6ea5ddf17fd5bc46c83d26122952ea2f08a544dd32', 'open', '225', '50', '5', 1);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('52c5394cdedfb2e95b3ad8b92d0d6c9d1370ea9a', 'Failure to Restrict URL Access', 'lesson', 'Failure to Restrict URL Access', 'f60d1337ac4d35cb67880a3adda79', 'oed23498d53ad1d965a589e257d8366d74eb52ef955e103c813b592dba0477e3', 'open', '12', '50', '5', 0);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('201ae6f8c55ba3f3b5881806387fbf34b15c30c2', 'Insecure Cryptographic Storage', 'lesson', 'Insecure Cryptographic Storage', 'base64isNotEncryptionBase64isEncodingBase64HidesNothingFromYou', 'if38ebb58ea2d245fa792709370c00ca655fded295c90ef36f3a6c5146c29ef2', 'open', '35', '50', '5', 1);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('0709410108f91314fb6f7721df9b891351eb2fcc', 'Insecure Cryptographic Storage Challenge 2', 'challenge', 'Insecure Cryptographic Storage', 'TheVigenereCipherIsAmethodOfEncryptingAlphabeticTextByUsingPoly', 'h8aa0fdc145fb8089661997214cc0e685e5f86a87f30c2ca641e1dde15b01177', 'open', '175', '50', '5', 1);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('891a0208a95f1791287be721a4b851d4c584880a', 'Insecure Cryptographic Storage Challenge 1', 'challenge', 'Insecure Cryptographic Storage', 'mylovelyhorserunningthroughthefieldwhereareyougoingwithyourbiga', 'x9c408d23e75ec92495e0caf9a544edb2ee8f624249f3e920663edb733f15cd7', 'open', '85', '50', '5', 1);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('82e8e9e2941a06852b90c97087309b067aeb2c4c', 'Insecure Direct Object Reference Challenge 2', 'challenge', 'Insecure Direct Object References', '1f746b87a4e3628b90b1927de23f6077abdbbb64586d3ac9485625da21921a0f', 'vc9b78627df2c032ceaf7375df1d847e47ed7abac2a4ce4cb6086646e0f313a4', 'open', '185', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('e0ba96bb4c8d4cd2e1ff0a10a0c82b5362edf998', 'SQL Injection 4', 'challenge', 'Injection', 'd316e80045d50bdf8ed49d48f130b4acf4a878c82faef34daff8eb1b98763b6f', '1feccf2205b4c5ddf743630b46aece3784d61adc56498f7603ccd7cb8ae92629', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('a84bbf8737a9ca749d81d5226fc87e0c828138ee', 'SQL Injection 5', 'challenge', 'Injection', '343f2e424d5d7a2eff7f9ee5a5a72fd97d5a19ef7bff3ef2953e033ea32dd7ee', '8edf0a8ed891e6fef1b650935a6c46b03379a0eebab36afcd1d9076f65d4ce62', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('ad332a32a6af1f005f9c8d1e98db264eb2ae5dfe', 'SQL Injection 6', 'challenge', 'Injection', '17f999a8b3fbfde54124d6e94b256a264652e5087b14622e1644c884f8a33f82', 'd0e12e91dafdba4825b261ad5221aae15d28c36c7981222eb59f7fc8d8f212a2', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('182f519ef2add981c77a584380f41875edc65a56', 'Cross Site Scripting 4', 'challenge', 'XSS', '515e05137e023dd7828adc03f639c8b13752fbdffab2353ccec2beef2eec95e4', '06f81ca93f26236112f8e31f32939bd496ffe8c9f7b564bce32bd5e3a8c2f751', 'open', '200', '50', '5', 0);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('fccf8e4d5372ee5a73af5f862dc810545d19b176', 'Cross Site Scripting 5', 'challenge', 'XSS', '7d7cc278c30cca985ab027e9f9e09e2f759e5a3d1f63293fd1be975e13cd4744', 'f37d45f597832cdc6e91358dca3f53039d4489c94df2ee280d6203b389dd5671', 'open', '200', '50', '5', 0);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('0a37cb9296ff3763f7f3a45ff313bce47afa9384', 'CSRF 4', 'challenge', 'CSRF', '8f34078ef3e53f619618d9def1ede8a6a9117c77c2fad22f76bba633da83e6d4', '70b96195472adf3bf347cbc37c34489287969d5ba504ac2439915184d6e5dc49', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('04a5bd8656fdeceac26e21ef6b04b90eaafbd7d5', 'CSRF 5', 'challenge', 'CSRF', 'df611f54325786d42e6deae8bbd0b9d21cf2c9282ec6de4e04166abe2792ac00', '2fff41105149e507c75b5a54e558470469d7024929cf78d570cd16c03bee3569', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5', 'CSRF 6', 'challenge', 'CSRF', '849e1efbb0c1e870d17d32a3e1b18a8836514619146521fbec6623fce67b73e8', '7d79ea2b2a82543d480a63e55ebb8fef3209c5d648b54d1276813cd072815df3', 'open', '200', '50', '5', 1);
INSERT INTO `core`.`modules` (`moduleId`, `moduleName`, `moduleType`, `moduleCategory`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('53a53a66cb3bf3e4c665c442425ca90e29536edd', 'Insecure Data Storage', 'lesson', 'Mobile Insecure Data Storage', 'Battery777', 'ecfad0a5d41f59e6bed7325f56576e1dc140393185afca8975fbd6822ebf392f', 'open', '31', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('307f78f18fd6a87e50ed6705231a9f24cd582574', 'Insecure Data Storage 2', 'challenge', 'Mobile Insecure Data Storage', 'WarshipsAndWrenches', '362f84cf26bf96aeae358d5d0bbee31e9291aaa5367594c29b3af542a7572c01', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('da3de2e556494a9c2fb7308a98454cf55f3a4911', 'Insecure Data Storage 3', 'challenge', 'Mobile Insecure Data Storage', 'starfish123', 'ec09515a304d2de1f552e961ab769967bdc75740ad2363803168b7907c794cd4', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('335440fef02d19259254ed88293b62f31cccdd41', 'Client Side Injection', 'lesson', 'Mobile Injection', 'VolcanicEruptionsAbruptInterruptions', 'f758a97011ec4452cc0707e546a7c0f68abc6ef2ab747ea87e0892767152eae1', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('a3f7ffd0f9c3d15564428d4df0b91bd927e4e5e4', 'Client Side Injection 2', 'challenge', 'Mobile Injection', 'SourHatsAndAngryCats', '8855c8bb9df4446a546414562eda550520e29f7a82400a317c579eb3a5a0a8ef', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('e635fce334aa61fdaa459c21c286d6332eddcdd3', 'Client Side Injection 3', 'challenge', 'Mobile Injection', 'BurpingChimneys', 'cfe68711def42bb0b201467b859322dd2750f633246842280dc68c858d208425', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('2ab09c0c18470ae5f87d219d019a1f603e66f944', 'Reverse Engineering', 'lesson', 'Mobile Reverse Engineer', 'NintendoMonster', '19753b944b63232812b7af1a0e0adb59928158da5994a39f584cb799f25a95b9', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('f16bf2ab1c1bf400d36330f91e9ac6045edcd003', 'Reverse Engineering 2', 'challenge', 'Mobile Reverse Engineer', 'FireStoneElectric', '5bc811f9e744a71393a277c51bfd8fbb5469a60209b44fa3485c18794df4d5b1', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('1506f22cd73d14d8a73e0ee32006f35d4f234799', 'Unintended Data Leakage', 'lesson', 'Mobile Data Leakage', 'SilentButSteadyRedLed', '392c20397c535845d93c32fd99b94f70afe9cca3f78c1e4766fee1cc08c035ec', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('8ba06bc21d5a9d0f4e6771a74e11ee7036893cd1', 'Unintended Data Leakage 2', 'challange', 'Mobile Data Leakage', 'UpsideDownPizzaDip', 'bf16081ed057b2d1bc97f4b9da897149819a159a8114d4867c7f8f327f5453a8', 'open', '200', '50', '5', 1);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('3d5b46abc6865ba09aaff98a8278a5f5e339abff', 'Failure To Restrict URL Access Challenge 1', 'challenge', 'Failure to Restrict URL Access', 'c776572b6a9d5b5c6e4aa672a4771213', '4a1bc73dd68f64107db3bbc7ee74e3f1336d350c4e1e51d4eda5b52dddf86c99', 'open', '200', '50', '5', 0);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('c7ac1e05faa2d4b1016cfcc726e0689419662784', 'Failure To Restrict URL Access Challenge 2', 'challenge', 'Failure to Restrict URL Access', '40b675e3d404c52b36abe31d05842b283975ec62e8', '278fa30ee727b74b9a2522a5ca3bf993087de5a0ac72adff216002abf79146fa', 'open', '200', '50', '5', 0);
INSERT INTO modules (moduleId, moduleName, moduleType, moduleCategory, moduleResult, moduleHash, moduleStatus, incrementalRank, scoreValue, scoreBonus, hardcodedKey) VALUES ('b3cfd5890649e6815a1c7107cc41d17c82826cfa', 'Insecure Cryptographic Storage Challenge 3', 'challenge', 'Insecure Cryptographic Storage', 'THISISTHESECURITYSHEPHERDABCENCRYPTIONKEY', '2da053b4afb1530a500120a49a14d422ea56705a7e3fc405a77bc269948ccae1', 'open', '200', '50', '5', 1);

COMMIT;

-- -----------------------------------------------------
-- Data for table `core`.`cheatSheet`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `core`;
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('1ed105033900e462b26ca0685b00d98f59efcd93', '0dbea4cb5811fff0527184f99bd5034ca9286f11', '2012-02-10 10:11:53', 'Stop the request with a proxy and change the &quot;username&quot; parameter to be equall to &quot;admin&quot;');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('286ac1acdd084193e940e6f56df5457ff05a9fe1', '453d22238401e0bf6f1ff5d45996407e98e45b07', '2012-02-10 10:11:53', 'To complete the lesson, the attack string is the following:<br/>&lt;img src=&quot;https://hostname:port/root/grantComplete/csrfLesson?userId=tempId&quot;/&gt;');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('44a6af94f6f7a16cc92d84a936cb5c7825967b47', 'cd7f70faed73d2457219b951e714ebe5775515d8', '2012-02-10 10:11:53', 'Input is been filtered. To complete this challenge, enter the following attack string;<br/>&lt;iframe src=&#39;#&#39; onload=&#39;alert(&quot;XSS&quot;)&#39;&gt;&lt;/iframe&gt;');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('5487f2bf98beeb3aea66941ae8257a5e0bec38bd', '2dc909fd89c2b03059b1512e7b54ce5d1aaa4bb4', '2012-02-10 10:11:53', 'The user Ids in this challenge follow a sequence. The Hidden Users ID is 11');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('5eccb1b8b1c033bba8ef928089808751cbe6e1f8', '94cd2de560d89ef59fc450ecc647ff4d4a55c15d', '2012-02-10 10:11:53', 'To complete this challenge, you must force another user to submit a post request. The easiest way to achieve this is to force the user to visit a custom webpage that submits the post request. This means the webpage needs to be accessable. It can be accessed via a HTTP server, a public Dropbox link, a shared file area. The following is an example webpage that would complete the challenge<br/><br/>&lt;html&gt;<br/>&lt;body&gt;<br/>&lt;form id=&quot;completeChallenge2&quot; action=&quot;https://hostname:port/user/csrfchallengetwo/plusplus&quot; method=&quot;POST&quot; &gt;<br/>&lt;input type=&quot;hidden&quot; name=&quot;userid&quot; value=&quot;exampleId&quot; /&gt;<br/>&lt;input type=&quot;submit&quot;/&gt;<br/>&lt;/form&gt;<br/>&lt;script&gt;<br/>document.forms[&quot;completeChallenge2&quot;].submit();<br/>&lt;/script&gt;<br/>&lt;/body&gt;<br/>&lt;/html&gt;<br/><br/>The class form function should be used to create an iframe that forces the user to visit this attack page.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('6924e936f811e174f206d5432cf7510a270a18fa', 'b70a84f159876bb9885b6e0087d22f0a52abbfcf', '2012-02-10 10:11:53', 'Use the login function with usernames like admin, administrator, root, etc to find administrator email accounts. Use the forgotten password functionality to change the password for the email address recovered. Inspect the response of the password reset request to see what the password was reset to. Use this password to login!');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('7382ff2f7ee416bf0d37961ec54de32c502351de', 'a4bf43f2ba5ced041b03d0b3f9fa3541c520d65d', '2012-02-10 10:11:53', 'Base 64 Decode the &quot;checksum&quot; cookie in the request to find it equals &quot;userRole=user&quot;. Change the value of userRole to be administrator instead. The cookies new value should be &quot;dXNlclJvbGU9YWRtaW5pc3RyYXRvcg==&quot; when you replace it.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('776ef847e16dde4b1d65a476918d2157f62f8c91', '5ca9115f3279b9b9f3308eb6a59a4fcd374846d6', '2012-02-10 10:11:53', 'To complete this challenge, you must force an admin to submit a post request. The easiest way to achieve this is to force the admin to visit a custom webpage that submits the post request. This means the webpage needs to be accessable. It can be accessed via a HTTP server, a public Dropbox link, a shared file area. The following is an example webpage that would complete the challenge<br/><br/>&lt;html&gt;<br/>&lt;body&gt;<br/>&lt;form id=&quot;completeChallenge3&quot; action=&quot;https://hostname:port/user/csrfchallengetwo/plusplus&quot; method=&quot;POST&quot; &gt;<br/>&lt;input type=&quot;hidden&quot; name=&quot;userid&quot; value=&quot;exampleId&quot; /&gt;<br/>&lt;input type=&quot;hidden&quot; name=&quot;csrfToken&quot; value=&quot;anythingExceptNull&quot; /&gt;<br/>&lt;input type=&quot;submit&quot;/&gt;<br/>&lt;/form&gt;<br/>&lt;script&gt;<br/>document.forms[&quot;completeChallenge3&quot;].submit();<br/>&lt;/script&gt;<br/>&lt;/body&gt;<br/>&lt;/html&gt;<br/><br/>The class form function should be used to create an iframe that forces the admin to visit this attack page.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('82c207a4e07cbfc54faec884be6db0524e74829e', '891a0208a95f1791287be721a4b851d4c584880a', '2012-02-10 10:11:53', 'To complete this challenge, move every character five places back to get the following plaintext;<br/>The result key for this lesson is the following string; myLovelyHorseRunningThroughTheFieldWhereAreYouGoingWithYourBigA');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('860e5ed692c956c2ae6c4ba20c95313d9f5b0383', 'b6432a6b5022cb044e9946315c44ab262ab59e88', '2012-02-10 10:11:53', 'To perform the CSRF correctly use the following attack string;<br/>https://hostname:port/user/redirect?to=https://hostname:port/root/grantComplete/unvalidatedredirectlesson?userid=tempId');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('945b7dcdef1a36ded2ab008422396f8ba51c0630', 'd4e2c37d8f1298fcaf4edcea7292cb76e9eab09b', '2012-02-10 10:11:53', 'Input is been filtered. To complete this challenge, enter the following attack string;<br/>&lt;input type=&quot;button&quot; onmouseup=&quot;alert(&#39;XSS&#39;)&quot;/&gt;');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('97f946ed0bbda4f85e472321a256eacf2293239d', '20e755179a5840be5503d42bb3711716235005ea', '2012-02-10 10:11:53', 'To complete this challenge, you can embed the CSRF request in an iframe very easily as follows;<br/>&lt;iframe src=&quot;https://hostname:port/user/csrfchallengeone/plusplus?userid=exampleId&quot;&gt;&lt;/iframe&gt;<br/>Then you need another user to be hit with the attack to mark it as completed.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('af5959a242047ee87f728b87570a4e9ed9417e5e', '544aa22d3dd16a8232b093848a6523b0712b23da', '2012-02-10 10:11:53', 'To complete this challenge, the following attack strings will return all rows from the table:<br/>&#39; || &#39;1&#39; = &#39;1<br/>&#39; OOORRR &#39;1&#39; = &#39;1<br/>The filter in this case does not filter alternative expressions of the boolean OR statement, and it also does not recursively filter OR, so if you enter enough nested OR statements, they will work as well.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('b8515347017439da4216c6f8d984326eb21652d0', '52c5394cdedfb2e95b3ad8b92d0d6c9d1370ea9a', '2012-02-10 10:11:53', 'The url of the result key is hidden in a div with an ID &quot;hiddenDiv&quot; that can be found in the source HTML of the lesson.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('b921c6b7dc82648f0a0d07513f3eecb39b3ed064', 'ca8233e0398ecfa76f9e05a49d49f4a7ba390d07', '2012-02-10 10:11:53', 'The following attack vector will work wonderfully;<br/>&lt;script&gt;alert(&#39;XSS&#39;)&lt;/script&gt;');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('ba4e0a2727561c41286aa850b89022c09e088b67', '0e9e650ffca2d1fe516c5d7b0ce5c32de9e53d1e', '2012-02-10 10:11:53', 'Use the password change function to send a functionality request. Stop this request with a proxy, and take the value of the &quot;current&quot; cookie. Base 64 Decode this two times. Modify the value to an administrator username such as &quot;admin&quot;. Encode this two times and change the value of the current cookie to reflect this change. Sign in as the username you set your current cookie\'\'s value to with the new password you set.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('bb94a8412d7bb95f84c73afa420ca57fbc917912', '9533e21e285621a676bec58fc089065dec1f59f5', '2012-02-10 10:11:53', 'Use a proxy to stop the request to complete the lesson. Change the value of the &quot;lessonComplete&quot; cookie to &quot;lessonComplete&quot; to complete the lesson.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('c0b869ff8a4cd1f388e5e6bdd6525d176175c296', '408610f220b4f71f7261207a17055acbffb8a747', '2012-02-10 10:11:53', "The lesson can be completed with the following attack string<br/>\' OR \'1\' = \'1");
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('c0ed3f81fc615f28a39ed2c23555cea074e513f0', '0709410108f91314fb6f7721df9b891351eb2fcc', '2012-02-10 10:11:53', 'To complete this challenge, inspect the javascript that executes when the &quot;check&quot; is performed. The encryption key is stored in the &quot;theKey&quot; parameter. The last IF statment in the script checks if the output is equal to the encrypted Result Key.<br/>So the key and ciphertext is stored in the script. You can use this informaiton to decypt the result key manually with the vigenere cipher. You can also modify the javascript to decode the key for you. To do this, make the following changes;<br/> 1) Change the line &quot;input\\_char\\_value += alphabet . indexOf (theKey . charAt (theKey\\_index));&quot; to: <br/>&quot;input\\_char\\_value -= alphabet . indexOf (theKey . charAt (theKey\\_index));&quot;<br/>This inverts the process to decrypt instead of decrypt<br/>2) Add the following line to the end of the script:<br/>$(&quot;#resultDiv&quot;).html(&quot;Decode Result: &quot; + output);');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('d0a0742494656c79767864b2898247df4f37b728', '6319a2e38cc4b2dc9e6d840e1b81db11ee8e5342', '2012-02-10 10:11:53', 'Input is been filtered. What is been filtered out is been completly removed. The filter does not act in a recurrive fashion so with enough nested javascript triggers, it can be defeated. To complete this challenge, enter the following attack string;<br/>&lt;input type=&quot;button&quot; oncliconcliconcliconcliconclickkkkk=&quot;alert(&#39;XSS&#39;)&quot;/&gt;');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('d51277769f9452b6508a3a22d9f52bea3b0ff84d', 'f771a10efb42a79a9dba262fd2be2e44bf40b66d', '2012-02-10 10:11:53', 'To complete this challenge, the following attack string will return all rows from the table:<br/>&#39; &#39; OR &#39;1&#39; = &#39;1<br/>The filter is implemented very poorly for this challenge, as it only removes the first apostraphy in a string, rather than a recursive funciton.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('e4cb1c92453cf0e6adb5fe0e66abd408cb5b76ea', 'ac944b716c1ec5603f1d09c68e7a7e6e75b0d601', '2012-02-10 10:11:53', 'A step by step guid is not yet available for this lesson. You will need a tool like <a>Wire Shark</a> and you will need to search for the packet with the result key! The packet is broadcasted with UDP.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('e7e44ba680b2ab1f6958b1344c9e43931b81164a', '5dda8dc216bd6a46fccaa4ed45d49404cdc1c82e', '2012-02-10 10:11:53', 'To complete this challenge, you must craft a second statment to return Mary Martin\'\'s credit card number as the current statement only returns the customerName attribute. The following string will perform this; </br> &#39; UNION ALL SELECT creditCardNumber FROM customers WHERE customerName = &#39;Mary Martin<br/> The filter in this challenge is difficult to get around. But the \'\'UNION\'\' operator is not been filtered. Using the UNION command you are able to return the results of custom statements.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('f392e5a69475b14fbe5ae17639e174f379c0870e', '201ae6f8c55ba3f3b5881806387fbf34b15c30c2', '2012-02-10 10:11:53', 'The lesson is encoded in Base64. Most proxy applicaitons include a decoder for this encoding.');
INSERT INTO `core`.`cheatSheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('6afa50948e10466e9a94c7c2b270b3f958e412c6', '82e8e9e2941a06852b90c97087309b067aeb2c4c', '2012-02-10 10:11:53', "The user Id\'s inthis challenge are hashed using MD5. You can google the ID\'s to find their plain text if you have an internet connection to find their plain text. The sequence of ID\'\'s is as follows;<br/>2, 3, 5, 7, 9, 11<br/>The next number in the sequenceis 13. Modify the request with a proxy so that the id is the MD5 of 13 (c51ce410c124a10e0db5e4b97fc2af39)");

COMMIT;

-- -----------------------------------------------------
-- Data for table `core`.`sequence`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `core`;
INSERT INTO `core`.`sequence` (`tableName`, `currVal`) VALUES ('users', '282475249');
INSERT INTO `core`.`sequence` (`tableName`, `currVal`) VALUES ('cheatSheet', '282475299');
INSERT INTO `core`.`sequence` (`tableName`, `currVal`) VALUES ('class', '282475249');
INSERT INTO `core`.`sequence` (`tableName`, `currVal`) VALUES ('modules', '282475576');

COMMIT;



-- Default admin user

call userCreate(null, 'admin', 'password', 'admin', 'admin@securityShepherd.org', true);

-- Enable backup script

DROP DATABASE IF EXISTS backup;
CREATE DATABASE backup; 

SET GLOBAL event_scheduler = ON;
SET @@global.event_scheduler = ON;
SET GLOBAL event_scheduler = 1;
SET @@global.event_scheduler = 1;

USE core;
DELIMITER $$


drop event IF EXISTS update_status;

create event update_status
on schedule every 1 minute
do

BEGIN

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

drop table IF EXISTS `backup`.`users`;
drop table IF EXISTS `backup`.`class`;
drop table IF EXISTS `backup`.`modules`;
drop table IF EXISTS `backup`.`results`;
drop table IF EXISTS `backup`.`cheatsheet`;
drop table IF EXISTS `backup`.`sequence`;
-- -----------------------------------------------------
-- Table `core`.`class`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `backup`.`class` (
  `classId` VARCHAR(64) NOT NULL ,
  `className` VARCHAR(32) NOT NULL ,
  `classYear` VARCHAR(5) NOT NULL ,
  PRIMARY KEY (`classId`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `backup`.`users` (
  `userId` VARCHAR(64) NOT NULL ,
  `classId` VARCHAR(64) NULL ,
  `userName` VARCHAR(32) NOT NULL ,
  `userPass` VARCHAR(512) NOT NULL ,
  `userRole` VARCHAR(32) NOT NULL ,
  `badLoginCount` INT NOT NULL DEFAULT 0 ,
  `suspendedUntil` DATETIME NOT NULL DEFAULT '1000-01-01 00:00:00' ,
  `userAddress` VARCHAR(128) NULL ,
  `tempPassword` TINYINT(1)  NULL DEFAULT FALSE ,
  `userScore` INT NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`userId`) ,
  INDEX `classId` (`classId` ASC) ,
  UNIQUE INDEX `userName_UNIQUE` (`userName` ASC) ,
  CONSTRAINT `classId`
    FOREIGN KEY (`classId` )
    REFERENCES `backup`.`class` (`classId` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;



-- -----------------------------------------------------
-- Table `core`.`modules`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `backup`.`modules` (
  `moduleId` VARCHAR(64) NOT NULL ,
  `moduleName` VARCHAR(64) NOT NULL ,
  `moduleType` VARCHAR(16) NOT NULL ,
  `moduleCategory` VARCHAR(64) NULL ,
  `moduleResult` VARCHAR(256) NULL ,
  `moduleHash` VARCHAR(256) NULL ,
  `incrementalRank` INT NULL ,
  `scoreValue` INT NOT NULL DEFAULT 50 ,
  `scoreBonus` INT NOT NULL DEFAULT 5 ,
  PRIMARY KEY (`moduleId`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`results`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `backup`.`results` (
  `userId` VARCHAR(64) NOT NULL ,
  `moduleId` VARCHAR(64) NOT NULL ,
  `startTime` DATETIME NOT NULL ,
  `finishTime` DATETIME NULL ,
  `csrfCount` INT NULL DEFAULT 0 ,
  `resultSubmission` LONGTEXT NULL ,
  `knowledgeBefore` INT NULL ,
  `knowledgeAfter` INT NULL ,
  `difficulty` INT NULL ,
  PRIMARY KEY (`userId`, `moduleId`) ,
  INDEX `fk_Results_Modules1` (`moduleId` ASC) ,
  CONSTRAINT `fk_Results_users1`
    FOREIGN KEY (`userId` )
    REFERENCES `backup`.`users` (`userId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Results_Modules1`
    FOREIGN KEY (`moduleId` )
    REFERENCES `backup`.`modules` (`moduleId` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`cheatSheet`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `backup`.`cheatSheet` (
  `cheatSheetId` VARCHAR(64) NOT NULL ,
  `moduleId` VARCHAR(64) NOT NULL ,
  `createDate` DATETIME NOT NULL ,
  `solution` LONGTEXT NOT NULL ,
  PRIMARY KEY (`cheatSheetId`, `moduleId`) ,
  INDEX `fk_CheatSheet_Modules1` (`moduleId` ASC) ,
  CONSTRAINT `fk_CheatSheet_Modules1`
    FOREIGN KEY (`moduleId` )
    REFERENCES `backup`.`modules` (`moduleId` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `core`.`sequence`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `backup`.`sequence` (
  `tableName` VARCHAR(32) NOT NULL ,
  `currVal` BIGINT(20) NOT NULL DEFAULT 282475249 ,
  PRIMARY KEY (`tableName`) )
ENGINE = InnoDB;



Insert into `backup`.`class` (Select * from `core`.`class`);
Insert into `backup`.`users` (Select * from `core`.`users`);
Insert into `backup`.`modules` (Select * from `core`.`modules`);
Insert into `backup`.`results` (Select * from `core`.`results`);
Insert into `backup`.`cheatSheet` (Select * from `core`.`cheatSheet`);
Insert into `backup`.`sequence` (Select * from `core`.`sequence`);

END $$

DELIMITER ;
