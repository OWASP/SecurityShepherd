use `core`;


CREATE TABLE IF NOT EXISTS `core`.`medals` (
  `medalId` INT NOT NULL AUTO_INCREMENT,
  `classId` VARCHAR(64) NULL,
  `moduleId` VARCHAR(64) NOT NULL,
  `scoreBonus` INT NOT NULL DEFAULT 5 ,
  `goldMedalAvailable` TINYINT(1) NOT NULL DEFAULT TRUE,
  `silverMedalAvailable` TINYINT(1) NOT NULL DEFAULT TRUE,
  `bronzeMedalAvailable` TINYINT(1) NOT NULL DEFAULT TRUE,
  PRIMARY KEY (`medalId`) ,
  INDEX `fk_Medals_Modules1` (`moduleId` ASC) ,
  CONSTRAINT `fk_Medals_Class1`
    FOREIGN KEY (`classId` )
    REFERENCES `core`.`class` (`classId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Medals_Modules1`
    FOREIGN KEY (`moduleId` )
    REFERENCES `core`.`modules` (`moduleId` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;



-- -----------------------------------------------------
-- procedure adminFindById
-- -----------------------------------------------------

USE `core`;
-- DELIMITER $$
CREATE PROCEDURE `core`.`adminFindById` (IN adminId VARCHAR(64))
BEGIN
COMMIT;
SELECT userName FROM users
    WHERE userId = adminId
    AND userRole = 'admin';
END
-- $$
-- DELIMITER ;
;

-- -----------------------------------------------------
-- procedure adminGetAll
-- -----------------------------------------------------
USE `core`;
-- DELIMITER $$
CREATE PROCEDURE `core`.`adminGetAll` ()
BEGIN
COMMIT;
SELECT userId, userName, userAddress FROM users
    WHERE userRole = 'admin'
    ORDER BY userName;
END
-- $$
-- DELIMITER ;
;

-- -----------------------------------------------------
-- procedure userUpdateResult
-- -----------------------------------------------------

USE `core`;
-- DELIMITER $$
DROP PROCEDURE `core`.`userUpdateResult`;
-- $$
-- DELIMITER ;

USE `core`;
-- DELIMITER $$

CREATE PROCEDURE `core`.`userUpdateResult` (IN theModuleId VARCHAR(64), IN theUserId VARCHAR(64), IN theBefore INT, IN theAfter INT, IN theDifficulty INT, IN theAdditionalInfo LONGTEXT)
BEGIN
DECLARE theDate TIMESTAMP;
DECLARE theClassId VARCHAR(64);
DECLARE theBonus INT;
DECLARE totalScore INT;
DECLARE medalInfo INT; -- Used to find out if there is a medal available
DECLARE goldMedalInfo INT;
DECLARE silverMedalInfo INT;
DECLARE bronzeMedalInfo INT;
DECLARE medalRow INT;
COMMIT;

-- Does this Module/class combo exist in the DB?
SELECT classId FROM users WHERE userid = theUserId INTO theClassId;
IF (theClassId IS NULL) THEN
  SELECT count(moduleId) FROM medals WHERE moduleId = theModuleId AND classId IS NULL INTO medalRow;
ELSE
  SELECT count(moduleId) FROM medals WHERE moduleId = theModuleId AND classId = theClassId INTO medalRow;
END IF;
IF (medalRow < 1) THEN
  INSERT INTO medals (classId, moduleId) VALUES (theClassId, theModuleId);
END IF;
COMMIT;
SELECT NOW() FROM DUAL
    INTO theDate;
-- Get current bonus and decrement the bonus value
SELECT 0 FROM DUAL INTO totalScore;




IF (theClassId IS NULL) THEN
  SELECT scoreBonus FROM medals WHERE moduleId = theModuleId AND classId IS NULL INTO theBonus;
ELSE
  SELECT scoreBonus FROM medals WHERE moduleId = theModuleId AND classId = theClassId INTO theBonus;
END IF;
IF (theBonus > 0) THEN
    SELECT (totalScore + theBonus) FROM DUAL
        INTO totalScore;
    IF (theClassId IS NULL) THEN
      UPDATE medals SET scoreBonus = scoreBonus - 1 WHERE moduleId = theModuleId AND classId IS NULL;
    ELSE
      UPDATE medals SET scoreBonus = scoreBonus - 1 WHERE moduleId = theModuleId AND classId = theClassId;




    END IF;
    COMMIT;
END IF;



IF (theClassId IS NULL) THEN
  SELECT count(moduleId) FROM medals WHERE moduleId = theModuleId AND (goldMedalAvailable = TRUE OR silverMedalAvailable = TRUE OR bronzeMedalAvailable = TRUE) AND classId IS NULL INTO medalInfo;
ELSE
  SELECT count(moduleId) FROM medals WHERE moduleId = theModuleId AND classId = theClassId AND (goldMedalAvailable = TRUE OR silverMedalAvailable = TRUE OR bronzeMedalAvailable = TRUE) INTO medalInfo;
END IF;
COMMIT;

IF (medalInfo > 0) THEN
  IF (theClassId IS NULL) THEN
    SELECT count(moduleId) FROM medals WHERE moduleId = theModuleId AND goldMedalAvailable = TRUE AND classId IS NULL INTO goldMedalInfo;
  ELSE
    SELECT count(moduleId) FROM medals WHERE moduleId = theModuleId AND classId = theClassId AND goldMedalAvailable = TRUE INTO goldMedalInfo;
  END IF;
  IF (goldMedalInfo > 0) THEN
    UPDATE users SET goldMedalCount = goldMedalCount + 1 WHERE userId = theUserId;
    IF (theClassId IS NULL) THEN
      UPDATE medals SET goldMedalAvailable = FALSE WHERE moduleId = theModuleId AND classId IS NULL;

    ELSE
      UPDATE medals SET goldMedalAvailable = FALSE WHERE moduleId = theModuleId AND classId = theClassId;
    END IF;
    COMMIT;
  ELSE
    IF (theClassId IS NULL) THEN
      SELECT count(moduleId) FROM medals WHERE moduleId = theModuleId AND silverMedalAvailable = TRUE AND classId IS NULL INTO silverMedalInfo;
    ELSE
      SELECT count(moduleId) FROM medals WHERE moduleId = theModuleId AND classId = theClassId AND silverMedalAvailable = TRUE INTO silverMedalInfo;
    END IF;
    IF (silverMedalInfo > 0) THEN
      UPDATE users SET silverMedalCount = silverMedalCount + 1 WHERE userId = theUserId;
      IF (theClassId IS NULL) THEN
        UPDATE medals SET silverMedalAvailable = FALSE WHERE moduleId = theModuleId AND classId IS NULL;

      ELSE
        UPDATE medals SET silverMedalAvailable = FALSE WHERE moduleId = theModuleId AND classId = theClassId;
      END IF;
      COMMIT;
    ELSE
      IF (theClassId IS NULL) THEN
        SELECT count(moduleId) FROM medals WHERE moduleId = theModuleId AND bronzeMedalAvailable = TRUE AND classId IS NULL INTO bronzeMedalInfo;
      ELSE
        SELECT count(moduleId) FROM medals WHERE moduleId = theModuleId AND classId = theClassId AND bronzeMedalAvailable = TRUE INTO bronzeMedalInfo;
      END IF;
      IF (bronzeMedalInfo > 0) THEN
        UPDATE users SET bronzeMedalCount = bronzeMedalCount + 1 WHERE userId = theUserId;
        IF (theClassId IS NULL) THEN
          UPDATE medals SET bronzeMedalAvailable = FALSE WHERE moduleId = theModuleId AND classId IS NULL;
        ELSE
          UPDATE medals SET bronzeMedalAvailable = FALSE WHERE moduleId = theModuleId AND classId = theClassId;
        END IF;
        COMMIT;
      END IF;
    END IF;
  END IF;
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
END

-- $$
-- DELIMITER ;
;

INSERT INTO modules (`moduleId`, `moduleName`, `moduleNameLangPointer`, `moduleType`, `moduleCategory`, `moduleCategoryLangPointer`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `hardcodedKey`) VALUES ('6f5db377c28da4179bca1a43ede8d6bcf7bd322e', 'Untrusted Input', 'untrusted.input', 'lesson', 'Mobile Security Decisions via Untrusted Input', 'mobile.security.decisions.via.untrusted.input', 'RetroMagicFuturePunch', '5e2b61c679d1f290d23308b3b66c3ec00cd069f1483b705d17f2795a4e77dcb6', 'open', '82', '50', 1);
INSERT INTO modules (`moduleId`, `moduleName`, `moduleNameLangPointer`, `moduleType`, `moduleCategory`, `moduleCategoryLangPointer`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `hardcodedKey`) VALUES ('064e28ea4b2f7708b8cb4495d9db1a5e05decdb8', 'Poor Authentication 2', 'poor.authentication.2','challenge', 'Mobile Poor Authentication', 'mobile.poor.authentication', 'MoreRobotsNotEnoughNuts', '808d8372ec7bc7e37e8e3b30d313cb47763926065a4623b27b24cc537fee72a7', 'open', '173', '70', 1);
INSERT INTO modules (`moduleId`, `moduleName`, `moduleNameLangPointer`, `moduleType`, `moduleCategory`, `moduleCategoryLangPointer`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `hardcodedKey`) VALUES ('6a411618a05e3cef8ccb6f3d7914412d27782a88', 'Content Provider Leakage 1', 'content.provider.leakage.1', 'challenge', 'Mobile Content Provider', 'mobile.content.provider', 'BlueCupNoPartySorry', '2a845ec1943a6342956a48cdc8ca60f40036b68a810109d0b9d2a35271377980', 'open', '178', '75', 1);
INSERT INTO modules (`moduleId`, `moduleName`, `moduleNameLangPointer`, `moduleType`, `moduleCategory`, `moduleCategoryLangPointer`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `hardcodedKey`) VALUES ('f02ce6bcd0a822d245433533997eaf44379065f4', 'Insecure Cryptographic Storage Home Made Key', 'insecure.cryptographic.storage.home.made.key', 'challenge', 'Insecure Cryptographic Storage', 'insecure.cryptographic.storage', '59A8D9A8020C61B3D76A600F94AJCECEABEDD44DF26874BD070BD07D', '9e5ed059b23632c8801d95621fa52071b2eb211d8c044dde6d2f4b89874a7bc4', 'open', '240', '140', 0);


CALL cheatSheetCreate('6f5db377c28da4179bca1a43ede8d6bcf7bd322e', '6f5db377c28da4179bca1a43ede8d6bcf7bd322e.solution');
CALL cheatSheetCreate('f02ce6bcd0a822d245433533997eaf44379065f4', 'f02ce6bcd0a822d245433533997eaf44379065f4.solution');