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
