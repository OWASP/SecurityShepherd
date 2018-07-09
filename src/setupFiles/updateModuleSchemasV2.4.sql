DELIMITER ;
-- securityMisconfigStealToken Schema
-- -----------------------------------------------------
-- -----------------------------------------------------
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
DROP SCHEMA IF EXISTS `securityMisconfigStealToken` ;
CREATE SCHEMA IF NOT EXISTS `securityMisconfigStealToken` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `securityMisconfigStealToken` ;
-- -----------------------------------------------------
-- Table `securityMisconfigStealToken`.`tokens`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `securityMisconfigStealToken`.`tokens` (
  `idtokens` INT NOT NULL AUTO_INCREMENT,
  `userId` VARCHAR(64) NULL,
  `token` VARCHAR(64) NULL,
  PRIMARY KEY (`idtokens`),
  UNIQUE INDEX `userId_UNIQUE` (`userId` ASC),
  UNIQUE INDEX `token_UNIQUE` (`token` ASC))
ENGINE = InnoDB;
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
-- getToken Procedure
DELIMITER $$
USE `securityMisconfigStealToken`$$
CREATE PROCEDURE `securityMisconfigStealToken`.`getToken` (IN theUserId VARCHAR(64))
BEGIN
DECLARE tokenExists INT;
COMMIT;
SELECT count(token) FROM `securityMisconfigStealToken`.`tokens` WHERE userId = theUserId INTO tokenExists;
IF (tokenExists < 1) THEN
	INSERT INTO tokens (userId, token) VALUES (theUserId, SHA2(CONCAT(RAND(), now()), 256));
	COMMIT;
END IF;
SELECT token FROM tokens WHERE userId = theUserId;
END
$$
DELIMITER ;
-- validToken Procedure
DELIMITER $$
USE `securityMisconfigStealToken`$$
CREATE PROCEDURE `securityMisconfigStealToken`.`validToken` (IN theUserId VARCHAR(64), theToken VARCHAR(64))
BEGIN
COMMIT;
SELECT count(token) FROM `securityMisconfigStealToken`.`tokens` WHERE userId != theUserId AND token = theToken;
END
$$
DELIMITER ;
COMMIT;
-- -----------------------------------------------------
-- -----------------------------------------------------
-- DirectObjectBank Schema
-- -----------------------------------------------------
-- -----------------------------------------------------
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
DROP SCHEMA IF EXISTS `directObjectBank` ;
CREATE SCHEMA IF NOT EXISTS `directObjectBank` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `directObjectBank` ;
-- -----------------------------------------------------
-- Table `directObjectBank`.`bankAccounts`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `directObjectBank`.`bankAccounts` (
  `account_number` INT NOT NULL AUTO_INCREMENT,
  `account_holder` VARCHAR(45) NOT NULL,
  `account_password` VARCHAR(256) NOT NULL,
  `account_balance` FLOAT NOT NULL DEFAULT 5,
  PRIMARY KEY (`account_number`), 
  UNIQUE INDEX `account_holder_UNIQUE` (`account_holder` ASC))
ENGINE = InnoDB;
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
-- -----------------------------------------------------
-- Data for table `directObjectBank`.`bankAccounts`
-- -----------------------------------------------------
START TRANSACTION;
USE `directObjectBank`;
INSERT INTO `directObjectBank`.`bankAccounts` (`account_number`, `account_holder`, `account_password`, `account_balance`) VALUES (0, 'Mr. Banks', 'SignInImpossibleBecauseNotHashed', 10000000);
COMMIT;
-- BankAuth Procedure
DELIMITER $$
USE `directObjectBank`$$
CREATE PROCEDURE `directObjectBank`.`bankAuth` (IN theUserId VARCHAR(45), thePass VARCHAR(256))
BEGIN
COMMIT;
SELECT account_number, account_holder FROM `directObjectBank`.`bankAccounts` WHERE account_holder = theUserId AND account_password = SHA2(thePass, 256);
END
$$
DELIMITER ;
-- CurrentFunds Procedure
DELIMITER $$
USE `directObjectBank`$$
CREATE PROCEDURE `directObjectBank`.`currentFunds` (IN theBankAccountNumber VARCHAR(45))
BEGIN
COMMIT;
SELECT account_balance FROM `directObjectBank`.`bankAccounts` WHERE account_number = theBankAccountNumber;
END
$$
DELIMITER ;
-- transferFunds Procedure
DELIMITER $$
USE `directObjectBank`$$
CREATE PROCEDURE `directObjectBank`.`transferFunds` (IN theGiverAccountNumber VARCHAR(45), IN theRecieverAccountNumber VARCHAR(45), IN theAmmount FLOAT)
BEGIN
COMMIT;
UPDATE `directObjectBank`.`bankAccounts` 
	SET account_balance = account_balance - theAmmount
	WHERE account_number = theGiverAccountNumber;
UPDATE `directObjectBank`.`bankAccounts` 
	SET account_balance = account_balance + theAmmount
	WHERE account_number = theRecieverAccountNumber;
COMMIT;
END
$$
DELIMITER ;
-- createAccount Procedure
DELIMITER $$
USE `directObjectBank`$$
CREATE PROCEDURE `directObjectBank`.`createAccount` (IN accountHolder VARCHAR(45), IN accountPassword VARCHAR(256))
BEGIN
COMMIT;
INSERT INTO `directObjectBank`.`bankAccounts` (`account_holder`, `account_password`, `account_balance`) VALUES (accountHolder, SHA2(accountPassword, 256), 0);
COMMIT;
END
$$
DELIMITER ;
COMMIT;
-- ======================================================
-- SQL Stored Proecure Challenge
-- ======================================================
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';
DROP SCHEMA IF EXISTS `SqlChalStoredProc` ;
CREATE SCHEMA IF NOT EXISTS `SqlChalStoredProc` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
USE `SqlChalStoredProc` ;
-- -----------------------------------------------------
-- Table `SqlChalStoredProc`.`customers`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `SqlChalStoredProc`.`customers` (
  `customerId` VARCHAR(64) NOT NULL ,
  `customerName` VARCHAR(32) NOT NULL ,
  `customerAddress` VARCHAR(128) NOT NULL ,
  `comment` LONGTEXT NULL ,
  PRIMARY KEY (`customerId`) )
ENGINE = InnoDB;
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
-- -----------------------------------------------------
-- Data for table `SqlChalStoredProc`.`customers`
-- -----------------------------------------------------
START TRANSACTION;
USE `SqlChalStoredProc`;
INSERT INTO `SqlChalStoredProc`.`customers` (`customerId`, `customerName`, `customerAddress`, `comment`) VALUES ('019ce129ee8960a6b875b20095705d53f8c7b0ca', 'John Fits', 'crazycat@example.com', NULL);
INSERT INTO `SqlChalStoredProc`.`customers` (`customerId`, `customerName`, `customerAddress`, `comment`) VALUES ('44e2bdc1059903f464e5ba9a34b927614d7fee55', 'Rita Hanolan', 'the1night2before3four@exampleEmails.com', 'Well Done! The Result key is d9c5757c1c086d02d491cbe46a941ecde5a65d523de36ac1bfed8dd4dd9994c8');
INSERT INTO `SqlChalStoredProc`.`customers` (`customerId`, `customerName`, `customerAddress`, `comment`) VALUES ('05159435826869ccfd76d77a2ed4ba7c2023f0cb', 'Rubix Man', 'manycolours@cube.com', NULL);
INSERT INTO `SqlChalStoredProc`.`customers` (`customerId`, `customerName`, `customerAddress`, `comment`) VALUES ('6c5c26a1deccf4a87059deb0a3fb463ff7d62fd5', 'Paul O Brien', 'sixshooter@deaf.com', NULL);
COMMIT;
-- findUser Procedure
DELIMITER $$
USE `SqlChalStoredProc`$$
CREATE PROCEDURE `SqlChalStoredProc`.`findUser` (IN theAddress VARCHAR(128))
BEGIN
COMMIT;
SELECT * FROM customers WHERE customerAddress = theAddress;
END
$$
DELIMITER ;
COMMIT;
-- -----------------------------------------------------
-- -----------------------------------------------------
DROP USER  'al1th3Tokens'@'localhost';
CREATE USER 'al1th3Tokens'@'localhost' IDENTIFIED BY '87SDO63yUN.';
GRANT SELECT ON `securityMisconfigStealToken`.`tokens` TO 'al1th3Tokens'@'localhost';
GRANT INSERT ON `securityMisconfigStealToken`.`tokens` TO 'al1th3Tokens'@'localhost';
GRANT EXECUTE ON PROCEDURE `securityMisconfigStealToken`.`getToken` TO 'al1th3Tokens'@'localhost';
GRANT EXECUTE ON PROCEDURE `securityMisconfigStealToken`.`validToken`  TO 'al1th3Tokens'@'localhost';
DROP USER 'theBankMan'@'localhost';
CREATE USER 'theBankMan'@'localhost' IDENTIFIED BY 'B4ndkm.M98n';
GRANT SELECT ON `directObjectBank`.`bankAccounts` TO 'theBankMan'@'localhost';
GRANT INSERT ON `directObjectBank`.`bankAccounts` TO 'theBankMan'@'localhost';
GRANT UPDATE ON `directObjectBank`.`bankAccounts` TO 'theBankMan'@'localhost';
GRANT EXECUTE ON PROCEDURE `directObjectBank`.`bankAuth` TO 'theBankMan'@'localhost';
GRANT EXECUTE ON PROCEDURE `directObjectBank`.`currentFunds` TO 'theBankMan'@'localhost';
GRANT EXECUTE ON PROCEDURE `directObjectBank`.`transferFunds` TO 'theBankMan'@'localhost';
GRANT EXECUTE ON PROCEDURE `directObjectBank`.`createAccount` TO 'theBankMan'@'localhost';
DROP USER 'procChalUser'@'localhost';
CREATE USER 'procChalUser'@'localhost' IDENTIFIED BY 'k61dSmsM*8n';
GRANT SELECT ON `SqlChalStoredProc`.`customers` TO 'procChalUser'@'localhost';
GRANT EXECUTE ON PROCEDURE `SqlChalStoredProc`.`findUser` TO 'procChalUser'@'localhost';