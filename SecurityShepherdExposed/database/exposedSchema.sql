SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `sampleSchema` ;
CREATE SCHEMA IF NOT EXISTS `sampleSchema` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
USE `sampleSchema` ;

-- -----------------------------------------------------
-- Table `sampleSchema`.`example`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `sampleSchema`.`example` (
  `idexample` INT NOT NULL ,
  `comment` VARCHAR(45) NULL ,
  PRIMARY KEY (`idexample`) )
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `sampleSchema`.`example`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `sampleSchema`;
INSERT INTO `sampleSchema`.`example` (`idexample`, `comment`) VALUES ('77', 'You are in the wrong schema');

COMMIT;

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `productionSchema` ;
CREATE SCHEMA IF NOT EXISTS `productionSchema` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
USE `productionSchema` ;

-- -----------------------------------------------------
-- Table `productionSchema`.`users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `productionSchema`.`users` (
  `userId` VARCHAR(64) NOT NULL ,
  `userName` VARCHAR(64) NULL ,
  `userSecret` VARCHAR(256) NULL ,
  `userAddress` VARCHAR(64) NULL ,
  `userPhone` VARCHAR(64) NULL ,
  `userComment` VARCHAR(64) NULL ,
  PRIMARY KEY (`userId`) )
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `productionSchema`.`users`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `productionSchema`;
INSERT INTO `productionSchema`.`users` (`userId`, `userName`, `userSecret`, `userAddress`, `userPhone`, `userComment`) VALUES ('12345', 'admin', '4f979c8cf295c03d3c41bafe25faa21f1b1fa50535f906bef5af19357e8eee2d', 'Ireland', '0863612345', 'My password is the key');
INSERT INTO `productionSchema`.`users` (`userId`, `userName`, `userSecret`, `userAddress`, `userPhone`, `userComment`) VALUES ('1', 'Honeyn3t', 'wrongUser', 'Dublin', '0872384123', 'The Beginning of the End');
INSERT INTO `productionSchema`.`users` (`userId`, `userName`, `userSecret`, `userAddress`, `userPhone`, `userComment`) VALUES ('12', 'Trinity', 'wrongUser', 'TCD', '0834612345', 'Big College or big uni?');
INSERT INTO `productionSchema`.`users` (`userId`, `userName`, `userSecret`, `userAddress`, `userPhone`, `userComment`) VALUES ('21', 'Gold Fish', 'wrongUser', 'Tank', '0853451532', 'Forgetful');
INSERT INTO `productionSchema`.`users` (`userId`, `userName`, `userSecret`, `userAddress`, `userPhone`, `userComment`) VALUES ('22', 'Mario', 'wrongUser', 'Nintendo', '0866666333', 'Another Castle');
INSERT INTO `productionSchema`.`users` (`userId`, `userName`, `userSecret`, `userAddress`, `userPhone`, `userComment`) VALUES ('113', 'Luigi', 'wongUser', 'Nintendo', '0883882888', 'Taller Brother');
INSERT INTO `productionSchema`.`users` (`userId`, `userName`, `userSecret`, `userAddress`, `userPhone`, `userComment`) VALUES ('123', 'Peach', 'wrongUser', 'Nintendo', '0833322211', 'On a Pedistal');
INSERT INTO `productionSchema`.`users` (`userId`, `userName`, `userSecret`, `userAddress`, `userPhone`, `userComment`) VALUES ('133', 'One More Left', 'wrongUser', 'Unknown', '0833882283', 'This is the second last user');

COMMIT;

CREATE USER  'sourceCtfIsAweso'@'localhost' IDENTIFIED BY 'onefourthreeeightnineone';
GRANT SELECT ON `sampleSchema`.`users` TO 'sourceCtfIsAwesomeBallsRight'@'localhost';

CREATE USER  'brenHadBadTimeOn'@'localhost' IDENTIFIED BY 'twentyseveneighteighttwenty';
GRANT SELECT ON `sampleSchema`.`users` TO 'brenHadBadTimeOnHolidaySadFace'@'localhost';