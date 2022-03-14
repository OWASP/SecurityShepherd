/*
 * This file is part of the Security Shepherd Project.
 *
 * The Security Shepherd project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.<br/>
 *
 * The Security Shepherd project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<br/>
 *
 * You should have received a copy of the GNU General Public License
 * along with the Security Shepherd project.  If not, see <http://www.gnu.org/licenses/>.
 */


-- Script used to create all of the schemas on the vulnerable database server

-- DELIMITER ;

-- ======================================================
-- SQL Lesson
-- ======================================================

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `SqlInjLesson`;
CREATE SCHEMA IF NOT EXISTS `SqlInjLesson` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `SqlInjLesson` ;

-- -----------------------------------------------------
-- Table `SqlInjLesson`.`tb_users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `SqlInjLesson`.`tb_users` (
  `usersId` INT NOT NULL AUTO_INCREMENT ,
  `username` VARCHAR(64) NOT NULL ,
  `comment` LONGTEXT NULL ,
  PRIMARY KEY (`usersId`) )
ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `SqlInjLesson`.`tb_users`
-- -----------------------------------------------------
START TRANSACTION;
USE `SqlInjLesson`;
INSERT INTO `SqlInjLesson`.`tb_users` (`usersId`, `username`, `comment`) VALUES (14232, 'Mark Denihan', 'This guy wrote this application');
INSERT INTO `SqlInjLesson`.`tb_users` (`usersId`, `username`, `comment`) VALUES (61523, 'Cloud', 'Has a Big Sword');
INSERT INTO `SqlInjLesson`.`tb_users` (`usersId`, `username`, `comment`) VALUES (12543, 'Fred Mtenzi', 'A lecturer in DIT Kevin Street');
INSERT INTO `SqlInjLesson`.`tb_users` (`usersId`, `username`, `comment`) VALUES (82642, 'qw!dshs@ab', 'Lesson Completed. The result key is 3c17f6bf34080979e0cebda5672e989c07ceec9fa4ee7b7c17c9e3ce26bc63e0');
INSERT INTO `SqlInjLesson`.`tb_users` (`usersId`, `username`, `comment`) VALUES (12345, 'user', 'Try Adding some SQL Code');
INSERT INTO `SqlInjLesson`.`tb_users` (`usersId`, `username`, `comment`) VALUES (12346, 'OR 1 = 1', 'Your Close, You need to escape the string with an apostraphe so that your code is interpreted');

COMMIT;

-- ======================================================
-- SQL Challenge Two (email)
-- ======================================================

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `SqlChalEmail` ;
CREATE SCHEMA IF NOT EXISTS `SqlChalEmail` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `SqlChalEmail` ;

-- -----------------------------------------------------
-- Table `SqlChalEmail`.`customers`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `SqlChalEmail`.`customers` (
  `customerId` VARCHAR(64) NOT NULL ,
  `customerName` VARCHAR(32) NOT NULL ,
  `customerAddress` VARCHAR(32) NOT NULL ,
  `comment` LONGTEXT NULL ,
  PRIMARY KEY (`customerId`) )
ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `SqlChalEmail`.`customers`
-- -----------------------------------------------------
START TRANSACTION;
USE `SqlChalEmail`;
INSERT INTO `SqlChalEmail`.`customers` (`customerId`, `customerName`, `customerAddress`, `comment`) VALUES ('019ce129ee8960a6b875b20095705d53f8c7b0ca', 'John Fits', 'crazycat@example.com', NULL);
INSERT INTO `SqlChalEmail`.`customers` (`customerId`, `customerName`, `customerAddress`, `comment`) VALUES ('44e2bdc1059903f464e5ba9a34b927614d7fee55', 'Rita Hanolan', 'thenightbefore@example.com', 'Well Done! The Result key is f62abebf5658a6a44c5c9babc7865110c62f5ecd9d0a7052db48c4dbee0200e3');
INSERT INTO `SqlChalEmail`.`customers` (`customerId`, `customerName`, `customerAddress`, `comment`) VALUES ('05159435826869ccfd76d77a2ed4ba7c2023f0cb', 'Rubix Man', 'manycolours@cube.com', NULL);
INSERT INTO `SqlChalEmail`.`customers` (`customerId`, `customerName`, `customerAddress`, `comment`) VALUES ('6c5c26a1deccf4a87059deb0a3fb463ff7d62fd5', 'Paul O Brien', 'sixshooter@deaf.com', NULL);

COMMIT;

-- ======================================================
-- SQL Challenge 1
-- ======================================================

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `SqlChalOne` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `SqlChalOne` ;

-- -----------------------------------------------------
-- Table `SqlChalOne`.`customers`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `SqlChalOne`.`customers` ;

CREATE  TABLE IF NOT EXISTS `SqlChalOne`.`customers` (
  `customerId` VARCHAR(64) NOT NULL ,
  `customerName` VARCHAR(32) NOT NULL ,
  `customerAddress` VARCHAR(32) NOT NULL ,
  `comment` LONGTEXT NULL ,
  PRIMARY KEY (`customerId`) )
ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `SqlChalOne`.`customers`
-- -----------------------------------------------------
START TRANSACTION;
USE `SqlChalOne`;
INSERT INTO `SqlChalOne`.`customers` (`customerId`, `customerName`, `customerAddress`, `comment`) VALUES ('019ce129ee8960a6b875b20095705d53f8c7b0ca', 'John Fits', 'crazycat@example.com', NULL);
INSERT INTO `SqlChalOne`.`customers` (`customerId`, `customerName`, `customerAddress`, `comment`) VALUES ('44e2bdc1059903f464e5ba9a34b927614d7fee55', 'Rita Hanolan', 'thenightbefore@example.com', NULL);
INSERT INTO `SqlChalOne`.`customers` (`customerId`, `customerName`, `customerAddress`, `comment`) VALUES ('05159435826869ccfd76d77a2ed4ba7c2023f0cb', 'Rubix Man', 'manycolours@cube.com', NULL);
INSERT INTO `SqlChalOne`.`customers` (`customerId`, `customerName`, `customerAddress`, `comment`) VALUES ('6c5c26a1deccf4a87059deb0a3fb463ff7d62fd5', 'Paul O Brien', 'sixshooter@deaf.com', 'Well Done! The reuslt Key is fd8e9a29dab791197115b58061b215594211e72c1680f1eacc50b0394133a09f');

COMMIT;

-- ======================================================
-- SQL Challenge 3
-- ======================================================

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `SqlChalThree` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `SqlChalThree` ;

-- -----------------------------------------------------
-- Table `SqlChalThree`.`customers`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `SqlChalThree`.`customers` ;

CREATE  TABLE IF NOT EXISTS `SqlChalThree`.`customers` (
  `customerId` VARCHAR(64) NOT NULL ,
  `customerName` VARCHAR(32) NOT NULL ,
  `creditCardNumber` VARCHAR(19) NOT NULL ,
  `creditCardExp` VARCHAR(5) NOT NULL ,
  `creditCardSecurityNumber` VARCHAR(3) NOT NULL ,
  PRIMARY KEY (`customerId`) )
ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `SqlChalThree`.`customers`
-- -----------------------------------------------------
START TRANSACTION;
USE `SqlChalThree`;
INSERT INTO `SqlChalThree`.`customers` (`customerId`, `customerName`, `creditCardNumber`, `creditCardExp`, `creditCardSecurityNumber`) VALUES ('ef322ce991de1a890470ad94001e2b83b9266334', 'John Doe', '8454 1244 4712 2144', '12/13', '452');
INSERT INTO `SqlChalThree`.`customers` (`customerId`, `customerName`, `creditCardNumber`, `creditCardExp`, `creditCardSecurityNumber`) VALUES ('92cb640f60e2c9ea11cf89ef2c87d442dc3fa345', 'Jason McCoy', '5468 1763 1854 1451', '12/13', '285');
INSERT INTO `SqlChalThree`.`customers` (`customerId`, `customerName`, `creditCardNumber`, `creditCardExp`, `creditCardSecurityNumber`) VALUES ('8d6588bbfe4ac5b52ebf452dfc5cefe934b788ae', 'Mark Denihan', '1245 2514 2315 2147', '09/20', '745');
INSERT INTO `SqlChalThree`.`customers` (`customerId`, `customerName`, `creditCardNumber`, `creditCardExp`, `creditCardSecurityNumber`) VALUES ('b8811379df47b10b59b717942b8d2aaafeb8f0f8', 'Mary Martin', '9815 1547 3214 7569', '11/14', '987');
INSERT INTO `SqlChalThree`.`customers` (`customerId`, `customerName`, `creditCardNumber`, `creditCardExp`, `creditCardSecurityNumber`) VALUES ('cef8433dc9f4e532999fd7767eaaf7ab620fd94d', 'Joseph McDonnell', '9175 1244 4758 8854', '12/13', '653');

COMMIT;

-- ======================================================
-- Broken Authentication and Session Management Challenge 2
-- ======================================================
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `BrokenAuthAndSessMangChalTwo` ;
CREATE SCHEMA IF NOT EXISTS `BrokenAuthAndSessMangChalTwo` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `BrokenAuthAndSessMangChalTwo` ;

-- -----------------------------------------------------
-- Table `BrokenAuthAndSessMangChalTwo`.`users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `BrokenAuthAndSessMangChalTwo`.`users` (
  `userId` INT NOT NULL ,
  `userName` VARCHAR(32) NOT NULL ,
  `userPassword` VARCHAR(128) NOT NULL ,
  `userAddress` VARCHAR(128) NOT NULL ,
  PRIMARY KEY (`userId`) )
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `BrokenAuthAndSessMangChalTwo`.`users`
-- -----------------------------------------------------
START TRANSACTION;
USE `BrokenAuthAndSessMangChalTwo`;
INSERT INTO `BrokenAuthAndSessMangChalTwo`.`users` (`userId`, `userName`, `userPassword`, `userAddress`) VALUES (12, 'admin', 'default', 'zoidberg22@shepherd.com');
INSERT INTO `BrokenAuthAndSessMangChalTwo`.`users` (`userId`, `userName`, `userPassword`, `userAddress`) VALUES (321, 'administrator', 'default', 'buzzthebald@shepherd.com');
INSERT INTO `BrokenAuthAndSessMangChalTwo`.`users` (`userId`, `userName`, `userPassword`, `userAddress`) VALUES (3212, 'root', 'default', 'elitehacker@shepherd.com');
INSERT INTO `BrokenAuthAndSessMangChalTwo`.`users` (`userId`, `userName`, `userPassword`, `userAddress`) VALUES (634, 'superuser', 'default', 'superman@security.com');
INSERT INTO `BrokenAuthAndSessMangChalTwo`.`users` (`userId`, `userName`, `userPassword`, `userAddress`) VALUES (4524, 'privileged', 'default', 'spoiltbrat@security.com');

COMMIT;

-- ======================================================
-- BrokenAuthAndSessMangChalThree
-- ======================================================
DROP SCHEMA IF EXISTS `BrokenAuthAndSessMangChalThree` ;
CREATE SCHEMA IF NOT EXISTS `BrokenAuthAndSessMangChalThree` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `BrokenAuthAndSessMangChalThree`;

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';


-- -----------------------------------------------------
-- Table `BrokenAuthAndSessMangChalThree`.`users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `BrokenAuthAndSessMangChalThree`.`users` (
  `userId` INT NOT NULL ,
  `userName` VARCHAR(32) NOT NULL ,
  `userPassword` VARCHAR(128) NOT NULL ,
  `userAddress` VARCHAR(128) NOT NULL ,
  `userRole` VARCHAR(8) NOT NULL DEFAULT 'guest' ,
  PRIMARY KEY (`userId`) )
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `BrokenAuthAndSessMangChalThree`.`users`
-- -----------------------------------------------------
START TRANSACTION;
USE `BrokenAuthAndSessMangChalThree`;
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (12, 'admin', 'default', 'zoidberg22@shepherd.com', 'admin');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (321, 'administrator', 'default', 'buzzthebald@shepherd.com', 'admin');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (3212, 'root', 'default', 'elitehacker@shepherd.com', 'admin');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (634, 'superuser', 'default', 'superman@security.com', 'admin');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (4524, 'privileged', 'default', 'spoiltbrat@security.com', 'admin');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (40, 'guest1', 'default', 'guest1@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (41, 'guest2', 'default', 'guest2@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (42, 'guest3', 'default', 'guest3@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (43, 'guest4', 'default', 'guest4@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (44, 'guest5', 'default', 'guest5@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (45, 'guest6', 'default', 'guest6@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (46, 'guest7', 'default', 'guest7@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (47, 'guest8', 'default', 'guest8@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (48, 'guest9', 'guest', 'guest9@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (49, 'guest11', 'guest', 'guest11@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (50, 'guest12', 'guest', 'guest12@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (51, 'guest13', 'guest', 'guest13@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (52, 'guest14', 'guest', 'guest14@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (53, 'guest15', 'guest', 'guest15@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (54, 'guest16', 'guest', 'guest16@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (55, 'guest17', 'guest', 'guest17@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (56, 'guest18', 'guest', 'guest18@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (57, 'guest19', 'guest', 'guest19@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (58, 'guest20', 'guest', 'guest20@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (59, 'guest21', 'guest', 'guest21@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (60, 'guest22', 'guest', 'guest22@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (61, 'guest23', 'guest', 'guest23@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (62, 'guest10', 'guest', 'guest10@guest.com', 'guest');

COMMIT;

-- ======================================================
-- directObjectRefChalOne
-- ======================================================

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `directObjectRefChalOne` ;
CREATE SCHEMA IF NOT EXISTS `directObjectRefChalOne` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `directObjectRefChalOne` ;

-- -----------------------------------------------------
-- Table `directObjectRefChalOne`.`users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `directObjectRefChalOne`.`users` (
  `userId` VARCHAR(32) NOT NULL ,
  `userName` VARCHAR(32) NOT NULL ,
  `privateMessage` MEDIUMTEXT NOT NULL ,
  PRIMARY KEY (`userId`) )
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `directObjectRefChalOne`.`users`
-- -----------------------------------------------------
START TRANSACTION;
USE `directObjectRefChalOne`;
INSERT INTO `directObjectRefChalOne`.`users` (`userId`, `userName`, `privateMessage`) VALUES ('1', 'Paul Bourke', 'No Message Set');
INSERT INTO `directObjectRefChalOne`.`users` (`userId`, `userName`, `privateMessage`) VALUES ('3', 'Will Bailey', 'I love Go Karting');
INSERT INTO `directObjectRefChalOne`.`users` (`userId`, `userName`, `privateMessage`) VALUES ('5', 'Orla Cleary', 'As if!');
INSERT INTO `directObjectRefChalOne`.`users` (`userId`, `userName`, `privateMessage`) VALUES ('7', 'Ronan Fitzpatrick', 'I have retired');
INSERT INTO `directObjectRefChalOne`.`users` (`userId`, `userName`, `privateMessage`) VALUES ('9', 'Pat McKenana', 'I have a car!');
INSERT INTO `directObjectRefChalOne`.`users` (`userId`, `userName`, `privateMessage`) VALUES ('11', 'Hidden User', 'Result Key is <a>dd6301b38b5ad9c54b85d07c087aebec89df8b8c769d4da084a55663e6186742</a>');

COMMIT;

-- ======================================================
-- directObjectRefChalTwo
-- ======================================================

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `directObjectRefChalTwo` ;
CREATE SCHEMA IF NOT EXISTS `directObjectRefChalTwo` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `directObjectRefChalTwo` ;

-- -----------------------------------------------------
-- Table `directObjectRefChalTwo`.`users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `directObjectRefChalTwo`.`users` (
  `userId` VARCHAR(32) NOT NULL ,
  `userName` VARCHAR(32) NOT NULL ,
  `privateMessage` MEDIUMTEXT NOT NULL ,
  PRIMARY KEY (`userId`) )
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `directObjectRefChalTwo`.`users`
-- -----------------------------------------------------
START TRANSACTION;
USE `directObjectRefChalTwo`;
INSERT INTO `directObjectRefChalTwo`.`users` (`userId`, `userName`, `privateMessage`) VALUES ('c81e728d9d4c2f636f067f89cc14862c', 'Joe Sullivan', 'I was going to set a message, but then I decided not to.');
INSERT INTO `directObjectRefChalTwo`.`users` (`userId`, `userName`, `privateMessage`) VALUES ('eccbc87e4b5ce2fe28308fd9f2a7baf3', 'Will Bailey', 'I love Go Karting');
INSERT INTO `directObjectRefChalTwo`.`users` (`userId`, `userName`, `privateMessage`) VALUES ('e4da3b7fbbce2345d7772b0674a318d5', 'Orla Cleary', 'As if Im going to set a message. Who knows who could read it!');
INSERT INTO `directObjectRefChalTwo`.`users` (`userId`, `userName`, `privateMessage`) VALUES ('8f14e45fceea167a5a36dedd4bea2543', 'Ronan Fitzpatrick', 'I have retired');
INSERT INTO `directObjectRefChalTwo`.`users` (`userId`, `userName`, `privateMessage`) VALUES ('6512bd43d9caa6e02c990b0a82652dca', 'Pat McKenana', 'I have a car!');
INSERT INTO `directObjectRefChalTwo`.`users` (`userId`, `userName`, `privateMessage`) VALUES ('c51ce410c124a10e0db5e4b97fc2af39', 'Hidden User', 'Result Key is <a>1f746b87a4e3628b90b1927de23f6077abdbbb64586d3ac9485625da21921a0f</a>');

COMMIT;

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `SQLiC5Shop` ;
CREATE SCHEMA IF NOT EXISTS `SQLiC5Shop` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `SQLiC5Shop` ;

-- -----------------------------------------------------
-- Table `SQLiC5Shop`.`items`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `SQLiC5Shop`.`items` (
  `itemId` INT NOT NULL,
  `itemName` VARCHAR(45) NULL,
  `itemCost` INT NULL,
  PRIMARY KEY (`itemId`))
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `SQLiC5Shop`.`coupons`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `SQLiC5Shop`.`coupons` (
  `couponId` INT NOT NULL,
  `perCentOff` INT NULL,
  `couponCode` VARCHAR(128) NULL,
  `itemId` INT NOT NULL,
  PRIMARY KEY (`couponId`),
  INDEX `fk_coupons_items_idx` (`itemId` ASC),
  CONSTRAINT `fk_coupons_items`
    FOREIGN KEY (`itemId`)
    REFERENCES `SQLiC5Shop`.`items` (`itemId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `SQLiC5Shop`.`vipCoupons`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `SQLiC5Shop`.`vipCoupons` (
  `vipCouponId` INT NOT NULL,
  `perCentOff` INT NULL,
  `couponCode` VARCHAR(128) NULL,
  `itemId` INT NOT NULL,
  PRIMARY KEY (`vipCouponId`),
  INDEX `fk_vipCoupons_items1_idx` (`itemId` ASC),
  CONSTRAINT `fk_vipCoupons_items1`
    FOREIGN KEY (`itemId`)
    REFERENCES `SQLiC5Shop`.`items` (`itemId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `SQLiC5Shop`.`items`
-- -----------------------------------------------------
START TRANSACTION;
USE `SQLiC5Shop`;
INSERT INTO `SQLiC5Shop`.`items` (`itemId`, `itemName`, `itemCost`) VALUES (1, 'Pineapple', 30);
INSERT INTO `SQLiC5Shop`.`items` (`itemId`, `itemName`, `itemCost`) VALUES (2, 'Orange', 3000);
INSERT INTO `SQLiC5Shop`.`items` (`itemId`, `itemName`, `itemCost`) VALUES (3, 'Apple', 45);
INSERT INTO `SQLiC5Shop`.`items` (`itemId`, `itemName`, `itemCost`) VALUES (4, 'Banana', 15);

COMMIT;

-- -----------------------------------------------------
-- Data for table `SQLiC5Shop`.`coupons`
-- -----------------------------------------------------
START TRANSACTION;
USE `SQLiC5Shop`;
INSERT INTO `SQLiC5Shop`.`coupons` (`couponId`, `perCentOff`, `couponCode`, `itemId`) VALUES (1, 100, 'PleaseTakeAFruit', 3);
INSERT INTO `SQLiC5Shop`.`coupons` (`couponId`, `perCentOff`, `couponCode`, `itemId`) VALUES (2, 100, 'FruitForFree', 3);
INSERT INTO `SQLiC5Shop`.`coupons` (`couponId`, `perCentOff`, `couponCode`, `itemId`) VALUES (3, 10, 'PleaseTakeAnOrange', 2);
INSERT INTO `SQLiC5Shop`.`coupons` (`couponId`, `perCentOff`, `couponCode`, `itemId`) VALUES (4, 50, 'HalfOffOranges', 2);
INSERT INTO `SQLiC5Shop`.`coupons` (`couponId`, `perCentOff`, `couponCode`, `itemId`) VALUES (5, 10, 'PleaseTakeABanana', 4);
INSERT INTO `SQLiC5Shop`.`coupons` (`couponId`, `perCentOff`, `couponCode`, `itemId`) VALUES (6, 50, 'HalfOffBananas', 4);

COMMIT;

-- -----------------------------------------------------
-- Data for table `SQLiC5Shop`.`vipCoupons`
-- -----------------------------------------------------
START TRANSACTION;
USE `SQLiC5Shop`;
INSERT INTO `SQLiC5Shop`.`vipCoupons` (`vipCouponId`, `perCentOff`, `couponCode`, `itemId`) VALUES (861267, 100, 'spcil\/|Pse3cr3etCouponStu.f4rU176', 2);

COMMIT;

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `SqlChalFourSuperSecure` ;
CREATE SCHEMA IF NOT EXISTS `SqlChalFourSuperSecure` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `SqlChalFourSuperSecure` ;

-- ======================================================
-- SQL Injection Challenge 4
-- ======================================================

-- -----------------------------------------------------
-- Table `SqlChalFourSuperSecure`.`users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `SqlChalFourSuperSecure`.`users` (
  `idusers` INT NOT NULL AUTO_INCREMENT,
  `userName` VARCHAR(45) NOT NULL,
  `userPassword` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`idusers`),
  UNIQUE INDEX `userName_UNIQUE` (`userName` ASC))
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `SqlChalFourSuperSecure`.`users`
-- -----------------------------------------------------
START TRANSACTION;
USE `SqlChalFourSuperSecure`;
INSERT INTO `SqlChalFourSuperSecure`.`users` (`idusers`, `userName`, `userPassword`) VALUES (1, 'adam', '87i2ueeu2ndsedssda');
INSERT INTO `SqlChalFourSuperSecure`.`users` (`idusers`, `userName`, `userPassword`) VALUES (2, 'player', '87iueeundsedssda');
INSERT INTO `SqlChalFourSuperSecure`.`users` (`idusers`, `userName`, `userPassword`) VALUES (3, 'user', 'password');
INSERT INTO `SqlChalFourSuperSecure`.`users` (`idusers`, `userName`, `userPassword`) VALUES (4, 'mark', 'password');
INSERT INTO `SqlChalFourSuperSecure`.`users` (`idusers`, `userName`, `userPassword`) VALUES (5, 'sean', 'password');
INSERT INTO `SqlChalFourSuperSecure`.`users` (`idusers`, `userName`, `userPassword`) VALUES (6, 'denihan', 'password');
INSERT INTO `SqlChalFourSuperSecure`.`users` (`idusers`, `userName`, `userPassword`) VALUES (7, 'admin', "98y\'98hsadsoi!111,.,22ee");
INSERT INTO `SqlChalFourSuperSecure`.`users` (`idusers`, `userName`, `userPassword`) VALUES (8, 'duggan', 'password');

COMMIT;

-- -----------------------------------------------------
-- -----------------------------------------------------
-- SqlChalSix Schema
-- -----------------------------------------------------
-- -----------------------------------------------------

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `SqlChalSix` ;
CREATE SCHEMA IF NOT EXISTS `SqlChalSix` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `SqlChalSix` ;

-- -----------------------------------------------------
-- Table `SqlChalSix`.`users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `SqlChalSix`.`users` (
  `idusers` INT NOT NULL,
  `userName` VARCHAR(45) NOT NULL,
  `userPin` VARCHAR(16) NOT NULL,
  `userQuestion` VARCHAR(128) NOT NULL,
  `userAnswer` VARCHAR(191) NOT NULL,
  `userAge` VARCHAR(16) NOT NULL,
  PRIMARY KEY (`idusers`))
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `SqlChalSix`.`users`
-- -----------------------------------------------------
START TRANSACTION;
USE `SqlChalSix`;
INSERT INTO `SqlChalSix`.`users` (`idusers`, `userName`, `userPin`, `userQuestion`, `userAnswer`, `userAge`) VALUES (1, 'George', '8367', 'What is your favourite Flower', 'A Red Rose', '23');
INSERT INTO `SqlChalSix`.`users` (`idusers`, `userName`, `userPin`, `userQuestion`, `userAnswer`, `userAge`) VALUES (2, 'Brendan', '4685', 'What is the answer to this level?', '17f999a8b3fbfde54124d6e94b256a264652e5087b14622e1644c884f8a33f82', '98');
INSERT INTO `SqlChalSix`.`users` (`idusers`, `userName`, `userPin`, `userQuestion`, `userAnswer`, `userAge`) VALUES (3, 'Sean', '1254', 'Your favourite Viking', 'Thor', '25');
INSERT INTO `SqlChalSix`.`users` (`idusers`, `userName`, `userPin`, `userQuestion`, `userAnswer`, `userAge`) VALUES (4, 'Anthony', '7844', 'What game do I suck at?', 'All of the games', '84');
INSERT INTO `SqlChalSix`.`users` (`idusers`, `userName`, `userPin`, `userQuestion`, `userAnswer`, `userAge`) VALUES (5, 'Owen', '4648', 'Favourite Sandwhich Topping', 'Peanutbutter', '33');
INSERT INTO `SqlChalSix`.`users` (`idusers`, `userName`, `userPin`, `userQuestion`, `userAnswer`, `userAge`) VALUES (6, 'Eoin', '2653', 'Where did I holiday in the summer of 69?', 'The Dark Side of the Moon', '12');
INSERT INTO `SqlChalSix`.`users` (`idusers`, `userName`, `userPin`, `userQuestion`, `userAnswer`, `userAge`) VALUES (7, 'David', '3598', 'This is how we get ants', "Don\'t get me started", '6');

COMMIT;

-- -----------------------------------------------------
-- -----------------------------------------------------
-- csrfChallengeEnumTokens Schema
-- -----------------------------------------------------
-- -----------------------------------------------------

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `csrfChallengeEnumTokens` ;
CREATE SCHEMA IF NOT EXISTS `csrfChallengeEnumTokens` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `csrfChallengeEnumTokens` ;

-- -----------------------------------------------------
-- Table `csrfChallengeEnumTokens`.`csrfTokens`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `csrfChallengeEnumTokens`.`csrfTokens` (
  `userId` VARCHAR(64) NOT NULL,
  `csrfTokenscol` VARCHAR(191) NULL,
  PRIMARY KEY (`userId`))
ENGINE = InnoDB;

-- -----------------------------------------------------
-- -----------------------------------------------------
-- csrfChallengeFour Schema
-- -----------------------------------------------------
-- -----------------------------------------------------

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `csrfChallengeFour` ;
CREATE SCHEMA IF NOT EXISTS `csrfChallengeFour` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `csrfChallengeFour` ;

-- -----------------------------------------------------
-- Table `csrfChallengeFour`.`csrfTokens`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `csrfChallengeFour`.`csrfTokens` (
  `userId` VARCHAR(64) NOT NULL,
  `csrfTokenscol` VARCHAR(191) NULL,
  PRIMARY KEY (`userId`))
ENGINE = InnoDB;

-- -----------------------------------------------------
-- -----------------------------------------------------
-- BrokenAuthAndSessMangChalFive Schema
-- -----------------------------------------------------
-- -----------------------------------------------------

DROP SCHEMA IF EXISTS `BrokenAuthAndSessMangChalFive` ;
CREATE SCHEMA IF NOT EXISTS `BrokenAuthAndSessMangChalFive` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `BrokenAuthAndSessMangChalFive`;

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';


-- -----------------------------------------------------
-- Table `BrokenAuthAndSessMangChalFive`.`users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `BrokenAuthAndSessMangChalFive`.`users` (
  `userId` INT NOT NULL ,
  `userName` VARCHAR(32) NOT NULL ,
  `userPassword` VARCHAR(128) NOT NULL ,
  `userAddress` VARCHAR(128) NOT NULL ,
  `userRole` VARCHAR(8) NOT NULL DEFAULT 'guest' ,
  PRIMARY KEY (`userId`) )
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `BrokenAuthAndSessMangChalFive`.`users`
-- -----------------------------------------------------
START TRANSACTION;
USE `BrokenAuthAndSessMangChalFive`;
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (12, 'admin', 'default', 'zoidberg22@shepherd.com', 'admin');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (321, 'administrator', 'default', 'buzzthebald@shepherd.com', 'admin');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (3212, 'root', 'default', 'elitehacker@shepherd.com', 'admin');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (634, 'superuser', 'default', 'superman@security.com', 'admin');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (4524, 'privileged', 'default', 'spoiltbrat@security.com', 'admin');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (40, 'guest1', 'default', 'guest1@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (41, 'guest2', 'default', 'guest2@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (42, 'guest3', 'default', 'guest3@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (43, 'guest4', 'default', 'guest4@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (44, 'guest5', 'default', 'guest5@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (45, 'guest6', 'default', 'guest6@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (46, 'guest7', 'default', 'guest7@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (47, 'guest8', 'default', 'guest8@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (48, 'guest9', 'guest', 'guest9@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (49, 'guest11', 'guest', 'guest11@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (50, 'guest12', 'guest', 'guest12@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (51, 'guest13', 'guest', 'guest13@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (52, 'guest14', 'guest', 'guest14@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (53, 'guest15', 'guest', 'guest15@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (54, 'guest16', 'guest', 'guest16@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (55, 'guest17', 'guest', 'guest17@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (56, 'guest18', 'guest', 'guest18@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (57, 'guest19', 'guest', 'guest19@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (58, 'guest20', 'guest', 'guest20@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (59, 'guest21', 'guest', 'guest21@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (60, 'guest22', 'guest', 'guest22@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (61, 'guest23', 'guest', 'guest23@guest.com', 'guest');
INSERT INTO `BrokenAuthAndSessMangChalFive`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (62, 'guest10', 'guest', 'guest10@guest.com', 'guest');

COMMIT;

-- -----------------------------------------------------
-- -----------------------------------------------------
-- Session Management Challenge Six Schema
-- -----------------------------------------------------
-- -----------------------------------------------------
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `BrokenAuthAndSessMangChalSix` ;
CREATE SCHEMA IF NOT EXISTS `BrokenAuthAndSessMangChalSix` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `BrokenAuthAndSessMangChalSix` ;

-- -----------------------------------------------------
-- Table `BrokenAuthAndSessMangChalSix`.`users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `BrokenAuthAndSessMangChalSix`.`users` (
  `userId` INT NOT NULL ,
  `userName` VARCHAR(32) NOT NULL ,
  `userPassword` VARCHAR(128) NOT NULL ,
  `userAddress` VARCHAR(128) NOT NULL ,
  `secretQuestion` VARCHAR(191) NOT NULL ,
  `secretAnswer` VARCHAR(128) NOT NULL,
  PRIMARY KEY (`userId`) )
ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `BrokenAuthAndSessMangChalSix`.`users`
-- -----------------------------------------------------
START TRANSACTION;
USE `BrokenAuthAndSessMangChalSix`;
INSERT INTO `BrokenAuthAndSessMangChalSix`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (1224, 'manager', 		'!148972hsfalkjlkfi381', 'zoidberg23@shepherd.com',	'What is the first name of the person you first kissed?', 'Lena Andrysiak');
INSERT INTO `BrokenAuthAndSessMangChalSix`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (1225, 'sean',	 		'!148972hsfalkjlkfi381', 'zoidberg24@shepherd.com',	'What is the first name of the person you first kissed?', 'Ronit Tornincasa');
INSERT INTO `BrokenAuthAndSessMangChalSix`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (3214, 'administrator', 	'!148972hsfalkjlkfi381', 'buzzthebald@shepherd.com','What is the last name of the teacher who gave you your first failing grade?', 'Aran Keegan');
INSERT INTO `BrokenAuthAndSessMangChalSix`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (3212, 'root', 			'!148972hsfalkjlkfi381', 'elitehacker@shepherd.com','What is the name of the place your wedding reception was held?', 'Deerburn Hotel');
INSERT INTO `BrokenAuthAndSessMangChalSix`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (6344, 'superuser', 		'!148972hsfalkjlkfi381', 'superman@security.com',	'Who was the first person to beat you up when you were 8 years old?', 'Lileas Lockwood');
INSERT INTO `BrokenAuthAndSessMangChalSix`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (4524, 'privileged', 		'1489!72hsfalkjlkfi381', 'spoiltbrat@security.com',	'What was the name of the person who stole your TV the second time?', 'Olwen Sordi');
INSERT INTO `BrokenAuthAndSessMangChalSix`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (6343, 'mark', 			'148!972hsfalkjlkfi381', 'superman2@security.com',	'Who is your favourite Barista?', 'Buzz Fendall');
INSERT INTO `BrokenAuthAndSessMangChalSix`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (6342, 'markdenihan', 	'148!972hsfalkjlkfi381', 'superman3@security.com',	'Who is your most favourite person you have not met?', 'Etna Filippi');
INSERT INTO `BrokenAuthAndSessMangChalSix`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (6341, 'seanduggan', 		'148972!hsfalkjlkfi381', 'superman4@security.com',	'Who is your most favourite person you have not met?', 'Emily Fabian');
INSERT INTO `BrokenAuthAndSessMangChalSix`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (6551, 'rootuser', 		'14897!2hsfalkjlkfi381', 'superman6@security.com',	'Who is your most favourite person you have not met?', 'Leola Naggia');
INSERT INTO `BrokenAuthAndSessMangChalSix`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (6552, 'adminuser', 		'1489!72hsfalkjlkfi381', 'superman7@security.com',	'Who is your most favourite person you have not met?', 'Gladys Gabrielli');
INSERT INTO `BrokenAuthAndSessMangChalSix`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (6559, 'shepherd', 		'148972hsfalk!jlkfi381', 'superman8@security.com',	'Who is your most favourite person you have not met?', 'Morag Bristol');

COMMIT;

-- -----------------------------------------------------
-- -----------------------------------------------------
-- Session Management Challenge Seven Schema
-- -----------------------------------------------------
-- -----------------------------------------------------
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `BrokenAuthAndSessMangChalSeven` ;
CREATE SCHEMA IF NOT EXISTS `BrokenAuthAndSessMangChalSeven` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `BrokenAuthAndSessMangChalSeven` ;

-- -----------------------------------------------------
-- Table `BrokenAuthAndSessMangChalSeven`.`users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `BrokenAuthAndSessMangChalSeven`.`users` (
  `userId` INT NOT NULL ,
  `userName` VARCHAR(32) NOT NULL ,
  `userPassword` VARCHAR(128) NOT NULL ,
  `userAddress` VARCHAR(128) NOT NULL ,
  `secretQuestion` VARCHAR(191) NOT NULL ,
  `secretAnswer` VARCHAR(128) NOT NULL,
  PRIMARY KEY (`userId`) )
ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `BrokenAuthAndSessMangChalSeven`.`users`
-- -----------------------------------------------------
START TRANSACTION;
USE `BrokenAuthAndSessMangChalSeven`;
INSERT INTO `BrokenAuthAndSessMangChalSeven`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (1224, 'manager', 		'!148972hsfalkjlkfi381', 'zoidberg23@shepherd.com',		'What is your favourite flower?', 'Jade Vine');
INSERT INTO `BrokenAuthAndSessMangChalSeven`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (1225, 'sean',	 		'!148972hsfalkjlkfi381', 'zoidberg24@shepherd.com',		'What is your favourite flower?', 'Corpse Flower');
INSERT INTO `BrokenAuthAndSessMangChalSeven`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (3214, 'administrator', 	'!148972hsfalkjlkfi381', 'buzzthebald@shepherd.com','What is your favourite flower?', 'Gibraltar Campion');
INSERT INTO `BrokenAuthAndSessMangChalSeven`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (3212, 'root', 			'!148972hsfalkjlkfi381', 'elitehacker@shepherd.com',	'What is your favourite flower?', 'Franklin Tree');
INSERT INTO `BrokenAuthAndSessMangChalSeven`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (6344, 'superuser', 		'!148972hsfalkjlkfi381', 'superman@security.com',	'What is your favourite flower?', 'Jade Vine');
INSERT INTO `BrokenAuthAndSessMangChalSeven`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (4524, 'privileged', 		'1489!72hsfalkjlkfi381', 'spoiltbrat@security.com',	'What is your favourite flower?', 'Middlemist Red');
INSERT INTO `BrokenAuthAndSessMangChalSeven`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (6343, 'mark', 			'148!972hsfalkjlkfi381', 'superman2@security.com',		'What is your favourite flower?', 'Chocolate Cosmos');
INSERT INTO `BrokenAuthAndSessMangChalSeven`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (6342, 'markdenihan', 	'148!972hsfalkjlkfi381', 'superman3@security.com',		'What is your favourite flower?', 'Ghost Orchid');
INSERT INTO `BrokenAuthAndSessMangChalSeven`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (6341, 'seanduggan', 		'148972!hsfalkjlkfi381', 'superman4@security.com',	'What is your favourite flower?', 'Jade Vine');
INSERT INTO `BrokenAuthAndSessMangChalSeven`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (6551, 'rootuser', 		'14897!2hsfalkjlkfi381', 'superman6@security.com',		'What is your favourite flower?', 'Ghost Orchid');
INSERT INTO `BrokenAuthAndSessMangChalSeven`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (6552, 'adminuser', 		'1489!72hsfalkjlkfi381', 'superman7@security.com',	'What is your favourite flower?', 'Corpse Flower');
INSERT INTO `BrokenAuthAndSessMangChalSeven`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `secretQuestion`, `secretAnswer`) VALUES (6559, 'shepherd', 		'148972hsfalk!jlkfi381', 'superman8@security.com',		'What is your favourite flower?', 'Gibraltar Campion');

COMMIT;

-- -----------------------------------------------------
-- -----------------------------------------------------
-- CryptShop Schema
-- -----------------------------------------------------
-- -----------------------------------------------------

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `CryptShop` ;
CREATE SCHEMA IF NOT EXISTS `CryptShop` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `CryptShop` ;

-- -----------------------------------------------------
-- Table `CryptShop`.`items`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `CryptShop`.`items` (
  `itemId` INT NOT NULL,
  `itemName` VARCHAR(45) NULL,
  `itemCost` INT NULL,
  PRIMARY KEY (`itemId`))
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `CryptShop`.`coupons`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `CryptShop`.`coupons` (
  `couponId` INT NOT NULL,
  `perCentOff` INT NULL,
  `couponCode` VARCHAR(128) NULL,
  `itemId` INT NOT NULL,
  PRIMARY KEY (`couponId`),
  INDEX `fk_coupons_items_idx` (`itemId` ASC),
  CONSTRAINT `fk_coupons_items`
    FOREIGN KEY (`itemId`)
    REFERENCES `CryptShop`.`items` (`itemId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `CryptShop`.`items`
-- -----------------------------------------------------
START TRANSACTION;
USE `CryptShop`;
INSERT INTO `CryptShop`.`items` (`itemId`, `itemName`, `itemCost`) VALUES (1, 'Pineapple', 30);
INSERT INTO `CryptShop`.`items` (`itemId`, `itemName`, `itemCost`) VALUES (2, 'Orange', 3000);
INSERT INTO `CryptShop`.`items` (`itemId`, `itemName`, `itemCost`) VALUES (3, 'Apple', 45);
INSERT INTO `CryptShop`.`items` (`itemId`, `itemName`, `itemCost`) VALUES (4, 'Banana', 15);

COMMIT;

-- -----------------------------------------------------
-- Data for table `CryptShop`.`coupons`
-- -----------------------------------------------------
START TRANSACTION;
USE `CryptShop`;
INSERT INTO `CryptShop`.`coupons` (`couponId`, `perCentOff`, `couponCode`, `itemId`) VALUES (1, 100, 'PleaseTakeAFruit', 3);
INSERT INTO `CryptShop`.`coupons` (`couponId`, `perCentOff`, `couponCode`, `itemId`) VALUES (2, 100, 'FruitForFree', 3);
INSERT INTO `CryptShop`.`coupons` (`couponId`, `perCentOff`, `couponCode`, `itemId`) VALUES (3, 10, 'PleaseTakeAnOrange', 2);
INSERT INTO `CryptShop`.`coupons` (`couponId`, `perCentOff`, `couponCode`, `itemId`) VALUES (4, 50, 'HalfOffOranges', 2);
INSERT INTO `CryptShop`.`coupons` (`couponId`, `perCentOff`, `couponCode`, `itemId`) VALUES (5, 10, 'PleaseTakeABanana', 4);
INSERT INTO `CryptShop`.`coupons` (`couponId`, `perCentOff`, `couponCode`, `itemId`) VALUES (6, 50, 'HalfOffBananas', 4);
INSERT INTO `CryptShop`.`coupons` (`couponId`, `perCentOff`, `couponCode`, `itemId`) VALUES (432197, 100, 'e!c!3etZoumo@Stu4rU176', 2);
COMMIT;

-- -----------------------------------------------------
-- -----------------------------------------------------
-- Failure to Restrict URL Access Challenge Three Schema
-- -----------------------------------------------------
-- -----------------------------------------------------
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `UrlAccessThree` ;
CREATE SCHEMA IF NOT EXISTS `UrlAccessThree` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `UrlAccessThree` ;

-- -----------------------------------------------------
-- Table `UrlAccessThree`.`users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `UrlAccessThree`.`users` (
  `userId` INT NOT NULL ,
  `userName` VARCHAR(32) NOT NULL ,
  `userPassword` VARCHAR(128) NOT NULL ,
  `userAddress` VARCHAR(128) NOT NULL ,
  `userRole` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`userId`) )
ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `UrlAccessThree`.`users`
-- -----------------------------------------------------
START TRANSACTION;
USE `UrlAccessThree`;
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (1223, 'aGuest', 		'!14897-hs.alNj.kFim81', 'zoidberg23@shepherd.com', 'guest');
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (1224, 'manager', 		'!14897-hs.alNj.kFim81', 'zoidberg23@shepherd.com', 'admin');
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (1225, 'sean',	 		'!14897-hs.alNj.kFim81', 'zoidberg24@shepherd.com', 'admin');
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (3214, 'administrator', 	'!14897-hs.alNj.kFim81', 'buzzthebald@shepherd.com', 'admin');
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (3212, 'root', 			'!14897-hs.alNj.kFim81', 'elitehacker@shepherd.com', 'admin');
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (6344, 'superuser', 		'!14897-hs.alNj.kFim81', 'superman@security.com', 'admin');
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (6345, 'MrJohnReillyTheSecond', 		'!14897-hs.alNj.kFim81', 'MrJohnReillyTheSecond@security.com', 'superadmin');
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (6346, 'megauser', 		'!14897-hs.alNj.kFim81', 'megaman@security.com', 'admin');
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (6347, 'hyperuser', 		'!14897-hs.alNj.kFim81', 'hmegaman@security.com', 'admin');
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (6348, 'godzilla', 		'!14897-hs.alNj.kFim81', 'godzilla@security.com', 'admin');
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (6349, 'kinguser', 		'!14897-hs.alNj.kFim81', 'kinguser@security.com', 'admin');
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (4524, 'privileged', 		'1489!72hsfalkjlkfi381', 'spoiltbrat@security.com', 'admin');
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (6343, 'mark', 			'148!97-hs.alNj.kFim81', 'superman2@security.com', 'admin');
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (6342, 'markdenihan', 	'148!97-hs.alNj.kFim81', 'superman3@security.com', 'admin');
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (6341, 'seanduggan', 		'148972!hsfalkjlkfi381', 'superman4@security.com', 'admin');
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (6551, 'rootuser', 		'14897!2hsfalkjlkfi381', 'superman6@security.com', 'admin');
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (6552, 'adminuser', 		'1489!72hsfalkjlkfi381', 'superman7@security.com', 'admin');
INSERT INTO `UrlAccessThree`.`users` (`userId`, `userName`, `userPassword`, `userAddress`, `userRole`) VALUES (6559, 'shepherd', 		'148972hsfalk!jlkfi381', 'superman8@security.com', 'admin');

COMMIT;

-- -----------------------------------------------------
-- -----------------------------------------------------
-- sqlInjectSeven Schema
-- -----------------------------------------------------
-- -----------------------------------------------------

DROP SCHEMA IF EXISTS `sqlInjectSeven` ;
CREATE SCHEMA IF NOT EXISTS `sqlInjectSeven` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `sqlInjectSeven`;

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';


-- -----------------------------------------------------
-- Table `sqlInjectSeven`.`users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `sqlInjectSeven`.`users` (
  `userId` INT NOT NULL ,
  `userName` VARCHAR(32) NOT NULL ,
  `userPassword` VARCHAR(128) NOT NULL ,
  `userEmail` VARCHAR(128) NOT NULL ,
  `userRole` VARCHAR(8) NOT NULL DEFAULT 'default' ,
  PRIMARY KEY (`userId`) )
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `sqlInjectSeven`.`users`
-- -----------------------------------------------------
START TRANSACTION;
USE `sqlInjectSeven`;
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (40, 'User 1', 'de1027fnNys6687as!283619fj1237fault', 'UserJohn1@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (41, 'User 2', 'd128361027fnNys6687as!9fj1237efault', 'UserJim2@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (42, 'User 3', 'defa1283619f027fnNys6687as!j1237ult', 'UserJone3@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (43, 'User 4', 'd1283619fj12027fnNys6687as!37efault', 'UserBell4@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (44, 'User 5', 'defau1283619fj1237lt', 'UserConan5@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (45, 'User 6', 'de1283619fj1237fault', 'UserSmioth6@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (46, 'User 7', 'def1d88027fnNys6687as!sd&dsault', 'UserHat7@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (47, 'User 8', 'def1d027fnNys6687as!88sd&dsault', 'UserPage8@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (48, 'User 9', 'defaul027fnNys6687as!1d88sd&dst', 'UserCube9@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (49, 'User 11', 'd1d88027fnNys6687as!sd&dsefault', 'MrsJohn1@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (50, 'User 12', 'defau027fnNys6687as!l1d88sd&dst', 'MrsJim2@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (51, 'User 13', 'def_926diUUscnaosOault', 'MrsJone3@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (52, 'User 14', 'd_926diUUscnaosOefault', 'MrsBell4@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (53, 'User 15', 'defaul_926diUUscnaosOt', 'MrsConan5@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (54, 'User 16', 'de_926diUUscnaosOfault', 'MrsSmioth6@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (55, 'User 17', 'defaul_926diUUscnaosOt', 'MrsHat7@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (56, 'User 18', 'de_926diUUscnaosOfault', 'MrsPage8@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (57, 'User 19', 'defa_926diUUscnaosOult', 'MrsCube9@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (58, 'User 20', 'd_926diUUscnaosOefault', 'Mr20@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (59, 'User 21', 'defa1027fnNys6687as!ult', 'Mr2John1@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (60, 'User 22', 'de027fnNys6687as!fault', 'Mr2Jim2@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (61, 'User 23', 'defau027fnNys6687as!lt', 'Mr2Jone3@User.com', 'default');
INSERT INTO `sqlInjectSeven`.`users` (`userId`, `userName`, `userPassword`, `userEmail`, `userRole`) VALUES (62, 'User 10', 'def027fnNys6687as!ault', 'Mrs0@User.com', 'default');

COMMIT;

-- -----------------------------------------------------
-- -----------------------------------------------------
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
USE `securityMisconfigStealToken`;
-- DELIMITER $$
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
;
-- $$

-- DELIMITER ;

-- validToken Procedure
USE `securityMisconfigStealToken`;
-- DELIMITER $$
CREATE PROCEDURE `securityMisconfigStealToken`.`validToken` (IN theUserId VARCHAR(64), theToken VARCHAR(64))
BEGIN
COMMIT;
SELECT count(token) FROM `securityMisconfigStealToken`.`tokens` WHERE userId != theUserId AND token = theToken;
END
;
-- $$

-- DELIMITER ;

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
  `account_password` VARCHAR(191) NOT NULL,
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
INSERT INTO `directObjectBank`.`bankAccounts` (`account_number`, `account_holder`, `account_password`, `account_balance`) VALUES (0, 'Mr. Banks', 'SignInImpossibleBecauseNotHashed', 10000000000);

COMMIT;

-- BankAuth Procedure
USE `directObjectBank`;
-- DELIMITER $$
CREATE PROCEDURE `directObjectBank`.`bankAuth` (IN theUserId VARCHAR(45), thePass VARCHAR(191))
BEGIN
COMMIT;
SELECT account_number, account_holder FROM `directObjectBank`.`bankAccounts` WHERE account_holder = theUserId AND account_password = SHA2(thePass, 256);
END
;
-- $$

-- DELIMITER ;

-- CurrentFunds Procedure
USE `directObjectBank`;
-- DELIMITER $$
CREATE PROCEDURE `directObjectBank`.`currentFunds` (IN theBankAccountNumber VARCHAR(45))
BEGIN
COMMIT;
SELECT account_balance FROM `directObjectBank`.`bankAccounts` WHERE account_number = theBankAccountNumber;
END
;
-- $$

-- DELIMITER ;

-- transferFunds Procedure
USE `directObjectBank`;
-- DELIMITER $$
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
;
-- $$

-- DELIMITER ;

-- createAccount Procedure
USE `directObjectBank`;
-- DELIMITER $$
CREATE PROCEDURE `directObjectBank`.`createAccount` (IN accountHolder VARCHAR(45), IN accountPassword VARCHAR(191))
BEGIN
COMMIT;
INSERT INTO `directObjectBank`.`bankAccounts` (`account_holder`, `account_password`, `account_balance`) VALUES (accountHolder, SHA2(accountPassword, 256), 0);
COMMIT;
END
;
-- $$

-- DELIMITER ;

COMMIT;

-- ======================================================
-- SQL Stored Proecure Challenge
-- ======================================================

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `SqlChalStoredProc` ;
CREATE SCHEMA IF NOT EXISTS `SqlChalStoredProc` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
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
USE `SqlChalStoredProc`;
-- DELIMITER $$
CREATE PROCEDURE `SqlChalStoredProc`.`findUser` (IN theAddress VARCHAR(128))
BEGIN
COMMIT;
SELECT * FROM customers WHERE customerAddress = theAddress;
END
;
-- $$

-- DELIMITER ;

COMMIT;

-- -----------------------------------------------------
-- ======================================================
-- ======================================================
-- SQL Injection Poor Escaping (email)
-- ======================================================

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `SqlPoorEscape` ;
CREATE SCHEMA IF NOT EXISTS `SqlPoorEscape` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `SqlPoorEscape` ;

-- -----------------------------------------------------
-- Table `SqlPoorEscape`.`customers`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `SqlPoorEscape`.`customers` (
  `customerId` VARCHAR(64) NOT NULL ,
  `customerName` VARCHAR(32) NOT NULL ,
  `customerAddress` VARCHAR(35) NOT NULL ,
  `comment` LONGTEXT NULL ,
  PRIMARY KEY (`customerId`) )
ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `SqlPoorEscape`.`customers`
-- -----------------------------------------------------
START TRANSACTION;
USE `SqlPoorEscape`;
INSERT INTO `SqlPoorEscape`.`customers` (`customerId`, `customerName`, `customerAddress`, `comment`) VALUES ('019h53d60a6b875b20095705d53f8c7b0ca', 'John Fits', 'thislifecouldbethelast@example.com', NULL);
INSERT INTO `SqlPoorEscape`.`customers` (`customerId`, `customerName`, `customerAddress`, `comment`) VALUES ('44e2bdc105ifdua464e5ba9a34b927614d7fee55', 'Rita Hanolan', 'dontfoolyourself@example.com', 'Well Done! The Result key is 0dcf9078ba5d878f9e23809ac8f013d1a08fdc8f12c5036f1a4746dbe86c0aac');
INSERT INTO `SqlPoorEscape`.`customers` (`customerId`, `customerName`, `customerAddress`, `comment`) VALUES ('051594asdjd6869ccfd76d77a2ed4ba7c2023f0cb', 'Rubix Man', 'dontkidyourself@cube.com', NULL);
INSERT INTO `SqlPoorEscape`.`customers` (`customerId`, `customerName`, `customerAddress`, `comment`) VALUES ('6c5c26adjdccf4a87059deb0a3fb463ff7d62fd5', 'Paul O Brien', 'andweretooyoungtosee@deaf.com', NULL);

COMMIT;


-- -----------------------------------------------------
-- -----------------------------------------------------
-- Module Schema Users
-- -----------------------------------------------------
-- -----------------------------------------------------


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

commit;

GRANT USAGE ON *.* TO 'userLookUuuup'@'localhost' IDENTIFIED BY 'password';
DROP USER 'userLookUuuup'@'localhost';
CREATE USER 'userLookUuuup'@'localhost' IDENTIFIED BY 'youMomaSoTh1n';
GRANT SELECT ON `SqlChalSix`.`users`  TO 'userLookUuuup'@'localhost';

GRANT USAGE ON *.* TO 'DnTPubUser'@'localhost' IDENTIFIED BY 'password';
DROP USER 'DnTPubUser'@'localhost';
CREATE USER 'DnTPubUser'@'localhost' IDENTIFIED BY 'ch3fBrownSa4useIsS00000Go0d';
GRANT SELECT ON `SQLiC5Shop`.`items` TO 'DnTPubUser'@'localhost';
GRANT SELECT ON `SQLiC5Shop`.`coupons` TO 'DnTPubUser'@'localhost';

GRANT USAGE ON *.* TO 'DnTVipUser'@'localhost' IDENTIFIED BY 'password';
DROP USER 'DnTVipUser'@'localhost';
CREATE USER 'DnTVipUser'@'localhost' IDENTIFIED BY 'ch3fBrownSa4useIsS00000Go0d';
GRANT SELECT ON `SQLiC5Shop`.`items` TO 'DnTVipUser'@'localhost';
GRANT SELECT ON `SQLiC5Shop`.`vipCoupons` TO 'DnTVipUser'@'localhost';

GRANT USAGE ON *.* TO 'DnTPurUser'@'localhost' IDENTIFIED BY 'password';
DROP USER 'DnTPurUser'@'localhost';
CREATE USER 'DnTPurUser'@'localhost' IDENTIFIED BY 'ch3fBrownSa4useIsS00000Go0d';
GRANT SELECT ON `SQLiC5Shop`.`items` TO 'DnTPurUser'@'localhost';
GRANT SELECT ON `SQLiC5Shop`.`coupons` TO 'DnTPurUser'@'localhost';
GRANT SELECT ON `SQLiC5Shop`.`vipCoupons` TO 'DnTPurUser'@'localhost';

GRANT USAGE ON *.* TO 'firstBloodyMessL'@'localhost' IDENTIFIED BY 'password';
DROP USER 'firstBloodyMessL'@'localhost';
CREATE USER 'firstBloodyMessL'@'localhost' IDENTIFIED BY 'firstBlooded';
GRANT SELECT ON `SqlInjLesson`.`tb_users` TO 'firstBloodyMessL'@'localhost';

GRANT USAGE ON *.* TO 'CharlieSeabrook'@'localhost' IDENTIFIED BY 'password';
DROP USER 'CharlieSeabrook'@'localhost';
CREATE USER 'CharlieSeabrook'@'localhost' IDENTIFIED BY 'shallowHal';
GRANT SELECT ON `SqlChalEmail`.`customers` TO 'CharlieSeabrook'@'localhost';

GRANT USAGE ON *.* TO 'RightGuard3d'@'localhost' IDENTIFIED BY 'password';
DROP USER 'RightGuard3d'@'localhost';
CREATE USER 'RightGuard3d'@'localhost' IDENTIFIED BY 'logic3Speaker';
GRANT SELECT ON `SqlChalOne`.`customers` TO 'RightGuard3d'@'localhost';

GRANT USAGE ON *.* TO 'HdmiNoSignal'@'localhost' IDENTIFIED BY 'password';
DROP USER 'HdmiNoSignal'@'localhost';
CREATE USER 'HdmiNoSignal'@'localhost' IDENTIFIED BY '1nforma1ion';
GRANT SELECT ON `SqlChalThree`.`customers` TO 'HdmiNoSignal'@'localhost';

GRANT USAGE ON *.* TO 'randomUserName'@'localhost' IDENTIFIED BY 'password';
DROP USER  'randomUserName'@'localhost';
CREATE USER 'randomUserName'@'localhost' IDENTIFIED BY 'c4utionHoT';
GRANT SELECT ON `BrokenAuthAndSessMangChalTwo`.`users` TO 'randomUserName'@'localhost';
GRANT UPDATE ON `BrokenAuthAndSessMangChalTwo`.`users` TO 'randomUserName'@'localhost';

GRANT USAGE ON *.* TO 'doveM3nCare'@'localhost' IDENTIFIED BY 'password';
DROP USER  'doveM3nCare'@'localhost';
CREATE USER 'doveM3nCare'@'localhost' IDENTIFIED BY 'plusm0r3';
GRANT SELECT ON `BrokenAuthAndSessMangChalThree`.`users` TO 'doveM3nCare'@'localhost';
GRANT UPDATE ON `BrokenAuthAndSessMangChalThree`.`users` TO 'doveM3nCare'@'localhost';

GRANT USAGE ON *.* TO 'murr4yFe1ld'@'localhost' IDENTIFIED BY 'password';
DROP USER 'murr4yFe1ld'@'localhost';
CREATE USER  'murr4yFe1ld'@'localhost' IDENTIFIED BY '4gainst3ngl4nd';
GRANT SELECT ON `directObjectRefChalOne`.`users` TO 'murr4yFe1ld'@'localhost';

GRANT USAGE ON *.* TO 'd3nn1sM4nely'@'localhost' IDENTIFIED BY 'password';
DROP USER 'd3nn1sM4nely'@'localhost';
CREATE USER  'd3nn1sM4nely'@'localhost' IDENTIFIED BY 'Pr0ductM4rket1ngIssu3s';
GRANT SELECT ON `directObjectRefChalTwo`.`users` TO 'd3nn1sM4nely'@'localhost';

GRANT USAGE ON *.* TO 'secureDood'@'localhost' IDENTIFIED BY 'password';
DROP USER 'secureDood'@'localhost';
CREATE USER  'secureDood'@'localhost' IDENTIFIED BY 'iCantEv3n';
GRANT SELECT ON `SqlChalFourSuperSecure`.`users` TO 'secureDood'@'localhost';

GRANT USAGE ON *.* TO 'csrfChalEnuer'@'localhost' IDENTIFIED BY 'password';
DROP USER 'csrfChalEnuer'@'localhost';
CREATE USER  'csrfChalEnuer'@'localhost' IDENTIFIED BY 'c4n1bUplZ';
GRANT SELECT ON `csrfChallengeEnumTokens`.`csrfTokens` TO 'csrfChalEnuer'@'localhost';
GRANT INSERT ON `csrfChallengeEnumTokens`.`csrfTokens` TO 'csrfChalEnuer'@'localhost';
GRANT UPDATE ON `csrfChallengeEnumTokens`.`csrfTokens` TO 'csrfChalEnuer'@'localhost';

GRANT USAGE ON *.* TO 'l3tsg0cra'@'localhost' IDENTIFIED BY 'password';
DROP USER  'l3tsg0cra'@'localhost';
CREATE USER 'l3tsg0cra'@'localhost' IDENTIFIED BY '83ururMa';
GRANT SELECT ON `BrokenAuthAndSessMangChalFive`.`users` TO 'l3tsg0cra'@'localhost';
GRANT UPDATE ON `BrokenAuthAndSessMangChalFive`.`users` TO 'l3tsg0cra'@'localhost';

GRANT USAGE ON *.* TO 'csrfChalFour'@'localhost' IDENTIFIED BY 'password';
DROP USER 'csrfChalFour'@'localhost';
CREATE USER  'csrfChalFour'@'localhost' IDENTIFIED BY 'R1n13U2pv';
GRANT SELECT ON `csrfChallengeFour`.`csrfTokens` TO 'csrfChalFour'@'localhost';
GRANT INSERT ON `csrfChallengeFour`.`csrfTokens` TO 'csrfChalFour'@'localhost';
GRANT UPDATE ON `csrfChallengeFour`.`csrfTokens` TO 'csrfChalFour'@'localhost';

GRANT USAGE ON *.* TO 'randomMoFoName'@'localhost' IDENTIFIED BY 'password';
DROP USER  'randomMoFoName'@'localhost';
CREATE USER 'randomMoFoName'@'localhost' IDENTIFIED BY 'c2zXlq_ZoT';
GRANT SELECT ON `BrokenAuthAndSessMangChalSix`.`users` TO 'randomMoFoName'@'localhost';

GRANT USAGE ON *.* TO 'randomFlower'@'localhost' IDENTIFIED BY 'password';
DROP USER  'randomFlower'@'localhost';
CREATE USER 'randomFlower'@'localhost' IDENTIFIED BY 'c21-le_6oT';
GRANT SELECT ON `BrokenAuthAndSessMangChalSeven`.`users` TO 'randomFlower'@'localhost';

GRANT USAGE ON *.* TO 'tSwsfUSer'@'localhost' IDENTIFIED BY 'password';
DROP USER 'tSwsfUSer'@'localhost';
CREATE USER 'tSwsfUSer'@'localhost' IDENTIFIED BY '9s31iusd-n';
GRANT SELECT ON `CryptShop`.`items` TO 'tSwsfUSer'@'localhost';
GRANT SELECT ON `CryptShop`.`coupons` TO 'tSwsfUSer'@'localhost';

GRANT USAGE ON *.* TO 'yourOrEll'@'localhost' IDENTIFIED BY 'password';
DROP USER  'yourOrEll'@'localhost';
CREATE USER 'yourOrEll'@'localhost' IDENTIFIED BY '91dj3:766f';
GRANT SELECT ON `UrlAccessThree`.`users` TO 'yourOrEll'@'localhost';

GRANT USAGE ON *.* TO 'r1ndomFlower'@'localhost' IDENTIFIED BY 'password';
DROP USER  'r1ndomFlower'@'localhost';
CREATE USER 'r1ndomFlower'@'localhost' IDENTIFIED BY 'c41-l2_6oT';
GRANT SELECT ON `sqlInjectSeven`.`users` TO 'r1ndomFlower'@'localhost';

GRANT USAGE ON *.* TO 'al1th3Tokens'@'localhost' IDENTIFIED BY 'password';
DROP USER  'al1th3Tokens'@'localhost';
CREATE USER 'al1th3Tokens'@'localhost' IDENTIFIED BY '87SDO63yUN.';
GRANT SELECT ON `securityMisconfigStealToken`.`tokens` TO 'al1th3Tokens'@'localhost';
GRANT INSERT ON `securityMisconfigStealToken`.`tokens` TO 'al1th3Tokens'@'localhost';
GRANT EXECUTE ON PROCEDURE `securityMisconfigStealToken`.`getToken` TO 'al1th3Tokens'@'localhost';
GRANT EXECUTE ON PROCEDURE `securityMisconfigStealToken`.`validToken`  TO 'al1th3Tokens'@'localhost';

GRANT USAGE ON *.* TO 'theBankMan'@'localhost' IDENTIFIED BY 'password';
DROP USER 'theBankMan'@'localhost';
CREATE USER 'theBankMan'@'localhost' IDENTIFIED BY 'B4ndkm.M98n';
GRANT SELECT ON `directObjectBank`.`bankAccounts` TO 'theBankMan'@'localhost';
GRANT INSERT ON `directObjectBank`.`bankAccounts` TO 'theBankMan'@'localhost';
GRANT UPDATE ON `directObjectBank`.`bankAccounts` TO 'theBankMan'@'localhost';
GRANT EXECUTE ON PROCEDURE `directObjectBank`.`bankAuth` TO 'theBankMan'@'localhost';
GRANT EXECUTE ON PROCEDURE `directObjectBank`.`currentFunds` TO 'theBankMan'@'localhost';
GRANT EXECUTE ON PROCEDURE `directObjectBank`.`transferFunds` TO 'theBankMan'@'localhost';
GRANT EXECUTE ON PROCEDURE `directObjectBank`.`createAccount` TO 'theBankMan'@'localhost';

GRANT USAGE ON *.* TO 'procChalUser'@'localhost' IDENTIFIED BY 'password';
DROP USER 'procChalUser'@'localhost';
CREATE USER 'procChalUser'@'localhost' IDENTIFIED BY 'k61dSmsM*8n';
GRANT SELECT ON `SqlChalStoredProc`.`customers` TO 'procChalUser'@'localhost';
GRANT EXECUTE ON PROCEDURE `SqlChalStoredProc`.`findUser` TO 'procChalUser'@'localhost';

GRANT USAGE ON *.* TO 'imVideoingThis'@'localhost' IDENTIFIED BY 'password';
DROP USER 'imVideoingThis'@'localhost';
CREATE USER 'imVideoingThis'@'localhost' IDENTIFIED BY 'auoi@7723dj';
GRANT SELECT ON `SqlPoorEscape`.`customers` TO 'imVideoingThis'@'localhost';
