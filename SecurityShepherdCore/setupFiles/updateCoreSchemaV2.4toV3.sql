use core;
-- --------------------------------------
-- Module Table Stuff
-- --------------------------------------

ALTER TABLE modules ADD moduleNameLangPointer VARCHAR(64);
ALTER TABLE modules ADD moduleCategoryLangPointer VARCHAR(64);
UPDATE modules SET moduleNameLangPointer = REPLACE(LOWER(moduleName), ' ', '.');
UPDATE modules SET moduleCategoryLangPointer = REPLACE(LOWER(moduleCategory), ' ', '.');
UPDATE modules SET moduleCategoryLangPointer = 'poor.data.validation', moduleCategory = 'Poor Data Validation' WHERE moduleCategoryLangPointer = 'bad.data.validation';
INSERT INTO modules (`moduleId`, `moduleName`, `moduleNameLangPointer`, `moduleType`, `moduleCategory`, `moduleCategoryLangPointer`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('5b461ebe2e5e2797740cb3e9c7e3f93449a93e3a', 'Content Provider Leakage', 'content.provider.leakage', 'lesson', 'Mobile Content Providers', 'mobile.content.providers', 'LazerLizardsFlamingWizards', '4d41997b5b81c88f7eb761c1975481c4ce397b80291d99307cfad69662277d39', 'open', '79', '50', '5', 1);
INSERT INTO modules (`moduleId`, `moduleName`, `moduleNameLangPointer`, `moduleType`, `moduleCategory`, `moduleCategoryLangPointer`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('d7eaeaa1cc4f218abd86d14eefa183a0f8eb6298', 'NoSQL Injection One', 'nosql.injection.one', 'challenge', 'Injection', 'injection', 'c09f32d4c3dd5b75f04108e5ffc9226cd8840288a62bdaf9dc65828ab6eaf86a', 'd63c2fb5da9b81ca26237f1308afe54491d1bacf9fffa0b21a072b03c5bafe66', 'open', '89', '45', '5', 1);
INSERT INTO modules (`moduleId`, `moduleName`, `moduleNameLangPointer`, `moduleType`, `moduleCategory`, `moduleCategoryLangPointer`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('c685f8102ae0128c2ab342df64699bb8209a0839', 'SQL Injection Escaping', 'sql.injection.escaping', 'challenge', 'Injection', 'injection', '0dcf9078ba5d878f9e23809ac8f013d1a08fdc8f12c5036f1a4746dbe86c0aac', '8c3c35c30cdbbb73b7be3a4f8587aa9d88044dc43e248984a252c6e861f673d4', 'open', '99', '50', '5', 1);
UPDATE modules SET incrementalRank = '87' WHERE moduleId = '335440fef02d19259254ed88293b62f31cccdd41';
UPDATE modules SET moduleName = 'SQL Injection Old Challenge 1', moduleStatus = 'closed' WHERE moduleId = 'f771a10efb42a79a9dba262fd2be2e44bf40b66d';
UPDATE modules SET moduleId = 'f771a10efb42a79a9dba262fd2be2e44bf40b66d', moduleName = 'SQL Injection 2', moduleNameLangPointer = 'sql.injection.2', moduleResult = 'f62abebf5658a6a44c5c9babc7865110c62f5ecd9d0a7052db48c4dbee0200e3', moduleHash = 'ffd39cb26727f34cbf9fce3e82b9d703404e99cdef54d2aa745f497abe070b', incrementalRank = '88', scoreValue = '45' WHERE moduleName = 'SQL Injection 1';

COMMIT;


-- -----------------------------------------------------
-- procedure moduleCreate
-- -----------------------------------------------------

DROP PROCEDURE `core`.`moduleCreate`;

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleCreate` (IN theModuleName VARCHAR(64), theModuleType VARCHAR(16), theModuleCategory VARCHAR(64), isHardcodedKey BOOLEAN, theModuleSolution VARCHAR(256))
BEGIN
DECLARE theId VARCHAR(64);
DECLARE theDate DATETIME;
DECLARE theLangPointer VARCHAR(64);
DECLARE theCategoryLangPointer VARCHAR(64);
COMMIT;
SELECT NOW() FROM DUAL
    INTO theDate;
SELECT REPLACE(LOWER(theModuleName), ' ', '.') FROM DUAL
	INTO theLangPointer;
SELECT REPLACE(LOWER(theModuleCategory), ' ', '.') FROM DUAL
	INTO theCategoryLangPointer;
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
        moduleId, moduleName, moduleNameLangPointer, moduleType, moduleCategory, moduleCategoryLangPointer, moduleResult, moduleHash, hardcodedKey
    )VALUES(
        theId, theModuleName, theLangPointer, theModuleType, theModuleCategory, theCategoryLangPointer ,theModuleSolution, SHA2(CONCAT(theModuleName, theId), 256), isHardcodedKey
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

DROP PROCEDURE `core`.`moduleAllInfo`;

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleAllInfo` (IN theType VARCHAR(64), IN theUserId VARCHAR(64))
BEGIN
(SELECT moduleNameLangPointer, moduleCategoryLangPointer, moduleId, finishTime
FROM modules LEFT JOIN results USING (moduleId) WHERE userId = theUserId AND moduleType = theType AND moduleStatus = 'open') UNION (SELECT moduleNameLangPointer, moduleCategoryLangPointer, moduleId, null FROM modules WHERE moduleId NOT IN (SELECT moduleId FROM modules JOIN results USING (moduleId) WHERE userId = theUserId AND moduleType = theType AND moduleStatus = 'open') AND moduleType = theType  AND moduleStatus = 'open') ORDER BY moduleCategoryLangPointer, moduleNameLangPointer;
END

$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure lessonInfo
-- -----------------------------------------------------

DROP PROCEDURE `core`.`lessonInfo`;

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`lessonInfo` (IN theUserId VARCHAR(64))
BEGIN
(SELECT moduleNameLangPointer, moduleCategory, moduleId, finishTime
FROM modules LEFT JOIN results USING (moduleId) WHERE userId = theUserId AND moduleType = 'lesson' AND moduleStatus = 'open') UNION (SELECT moduleNameLangPointer, moduleCategory, moduleId, null FROM modules WHERE moduleId NOT IN (SELECT moduleId FROM modules JOIN results USING (moduleId) WHERE userId = theUserId AND moduleType = 'lesson' AND moduleStatus = 'open') AND moduleType = 'lesson'  AND moduleStatus = 'open') ORDER BY moduleNameLangPointer, moduleCategory, moduleNameLangPointer;
END

$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure moduleTournamentOpenInfo
-- -----------------------------------------------------

DROP PROCEDURE `core`.`moduleTournamentOpenInfo`;

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleTournamentOpenInfo` (IN theUserId VARCHAR(64))
BEGIN
(SELECT moduleNameLangPointer, moduleCategory, moduleId, finishTime, incrementalRank, scoreValue FROM modules LEFT JOIN results USING (moduleId) 
WHERE userId = theUserId AND moduleStatus = 'open') UNION (SELECT moduleNameLangPointer, moduleCategory, moduleId, null, incrementalRank, scoreValue FROM modules WHERE moduleId NOT IN (SELECT moduleId FROM modules JOIN results USING (moduleId) WHERE userId = theUserId AND moduleStatus = 'open') AND moduleStatus = 'open') ORDER BY incrementalRank, scoreValue, moduleNameLangPointer;
END

$$

DELIMITER ;

COMMIT;
-- -----------------------------------------------------
-- Cheat Sheets
-- -----------------------------------------------------

use core;
DELETE FROM cheatsheet;
COMMIT;
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('1ed105033900e462b26ca0685b00d98f59efcd93', '0dbea4cb5811fff0527184f99bd5034ca9286f11', '2012-02-10 10:11:53', '0dbea4cb5811fff0527184f99bd5034ca9286f11.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('286ac1acdd084193e940e6f56df5457ff05a9fe1', '453d22238401e0bf6f1ff5d45996407e98e45b07', '2012-02-10 10:11:53', '453d22238401e0bf6f1ff5d45996407e98e45b07.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('44a6af94f6f7a16cc92d84a936cb5c7825967b47', 'cd7f70faed73d2457219b951e714ebe5775515d8', '2012-02-10 10:11:53', 'cd7f70faed73d2457219b951e714ebe5775515d8.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('5487f2bf98beeb3aea66941ae8257a5e0bec38bd', '2dc909fd89c2b03059b1512e7b54ce5d1aaa4bb4', '2012-02-10 10:11:53', '2dc909fd89c2b03059b1512e7b54ce5d1aaa4bb4.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('5eccb1b8b1c033bba8ef928089808751cbe6e1f8', '94cd2de560d89ef59fc450ecc647ff4d4a55c15d', '2012-02-10 10:11:53', '94cd2de560d89ef59fc450ecc647ff4d4a55c15d.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('6924e936f811e174f206d5432cf7510a270a18fa', 'b70a84f159876bb9885b6e0087d22f0a52abbfcf', '2012-02-10 10:11:53', 'b70a84f159876bb9885b6e0087d22f0a52abbfcf.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('7382ff2f7ee416bf0d37961ec54de32c502351de', 'a4bf43f2ba5ced041b03d0b3f9fa3541c520d65d', '2012-02-10 10:11:53', 'a4bf43f2ba5ced041b03d0b3f9fa3541c520d65d.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('776ef847e16dde4b1d65a476918d2157f62f8c91', '5ca9115f3279b9b9f3308eb6a59a4fcd374846d6', '2012-02-10 10:11:53', '5ca9115f3279b9b9f3308eb6a59a4fcd374846d6.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('82c207a4e07cbfc54faec884be6db0524e74829e', '891a0208a95f1791287be721a4b851d4c584880a', '2012-02-10 10:11:53', '891a0208a95f1791287be721a4b851d4c584880a.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('860e5ed692c956c2ae6c4ba20c95313d9f5b0383', 'b6432a6b5022cb044e9946315c44ab262ab59e88', '2012-02-10 10:11:53', 'b6432a6b5022cb044e9946315c44ab262ab59e88.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('945b7dcdef1a36ded2ab008422396f8ba51c0630', 'd4e2c37d8f1298fcaf4edcea7292cb76e9eab09b', '2012-02-10 10:11:53', 'd4e2c37d8f1298fcaf4edcea7292cb76e9eab09b.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('97f946ed0bbda4f85e472321a256eacf2293239d', '20e755179a5840be5503d42bb3711716235005ea', '2012-02-10 10:11:53', '20e755179a5840be5503d42bb3711716235005ea.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('af5959a242047ee87f728b87570a4e9ed9417e5e', '544aa22d3dd16a8232b093848a6523b0712b23da', '2012-02-10 10:11:53', '544aa22d3dd16a8232b093848a6523b0712b23da.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('b8515347017439da4216c6f8d984326eb21652d0', '52c5394cdedfb2e95b3ad8b92d0d6c9d1370ea9a', '2012-02-10 10:11:53', '52c5394cdedfb2e95b3ad8b92d0d6c9d1370ea9a.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('b921c6b7dc82648f0a0d07513f3eecb39b3ed064', 'ca8233e0398ecfa76f9e05a49d49f4a7ba390d07', '2012-02-10 10:11:53', 'ca8233e0398ecfa76f9e05a49d49f4a7ba390d07.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('ba4e0a2727561c41286aa850b89022c09e088b67', '0e9e650ffca2d1fe516c5d7b0ce5c32de9e53d1e', '2012-02-10 10:11:53', '0e9e650ffca2d1fe516c5d7b0ce5c32de9e53d1e.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('bb94a8412d7bb95f84c73afa420ca57fbc917912', '9533e21e285621a676bec58fc089065dec1f59f5', '2012-02-10 10:11:53', '9533e21e285621a676bec58fc089065dec1f59f5.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('c0b869ff8a4cd1f388e5e6bdd6525d176175c296', '408610f220b4f71f7261207a17055acbffb8a747', '2012-02-10 10:11:53', '408610f220b4f71f7261207a17055acbffb8a747.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('c0ed3f81fc615f28a39ed2c23555cea074e513f0', '0709410108f91314fb6f7721df9b891351eb2fcc', '2012-02-10 10:11:53', '0709410108f91314fb6f7721df9b891351eb2fcc.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('d0a0742494656c79767864b2898247df4f37b728', '6319a2e38cc4b2dc9e6d840e1b81db11ee8e5342', '2012-02-10 10:11:53', '6319a2e38cc4b2dc9e6d840e1b81db11ee8e5342.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('d51277769f9452b6508a3a22d9f52bea3b0ff84d', 'f771a10efb42a79a9dba262fd2be2e44bf40b66d', '2012-02-10 10:11:53', 'f771a10efb42a79a9dba262fd2be2e44bf40b66d.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('e7e44ba680b2ab1f6958b1344c9e43931b81164a', '5dda8dc216bd6a46fccaa4ed45d49404cdc1c82e', '2012-02-10 10:11:53', '5dda8dc216bd6a46fccaa4ed45d49404cdc1c82e.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('f392e5a69475b14fbe5ae17639e174f379c0870e', '201ae6f8c55ba3f3b5881806387fbf34b15c30c2', '2012-02-10 10:11:53', '201ae6f8c55ba3f3b5881806387fbf34b15c30c2.solution');
INSERT INTO `core`.`cheatsheet` (`cheatSheetId`, `moduleId`, `createDate`, `solution`) VALUES ('6afa50948e10466e9a94c7c2b270b3f958e412c6', '82e8e9e2941a06852b90c97087309b067aeb2c4c', '2012-02-10 10:11:53', '82e8e9e2941a06852b90c97087309b067aeb2c4c.solution');
CALL cheatSheetCreate('a84bbf8737a9ca749d81d5226fc87e0c828138ee', 'a84bbf8737a9ca749d81d5226fc87e0c828138ee.solution');
CALL cheatSheetCreate('e0ba96bb4c8d4cd2e1ff0a10a0c82b5362edf998', 'e0ba96bb4c8d4cd2e1ff0a10a0c82b5362edf998.solution');
CALL cheatSheetCreate('ad332a32a6af1f005f9c8d1e98db264eb2ae5dfe', 'ad332a32a6af1f005f9c8d1e98db264eb2ae5dfe.solution');
CALL cheatSheetCreate('182f519ef2add981c77a584380f41875edc65a56', '182f519ef2add981c77a584380f41875edc65a56.solution'); 
CALL cheatSheetCreate('fccf8e4d5372ee5a73af5f862dc810545d19b176', 'fccf8e4d5372ee5a73af5f862dc810545d19b176.solution'); 
CALL cheatSheetCreate('0a37cb9296ff3763f7f3a45ff313bce47afa9384', '0a37cb9296ff3763f7f3a45ff313bce47afa9384.solution');
CALL cheatSheetCreate('04a5bd8656fdeceac26e21ef6b04b90eaafbd7d5', '04a5bd8656fdeceac26e21ef6b04b90eaafbd7d5.solution');
CALL cheatSheetCreate('853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5', '853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5.solution');
CALL cheatSheetCreate('3d5b46abc6865ba09aaff98a8278a5f5e339abff', '3d5b46abc6865ba09aaff98a8278a5f5e339abff.solution');
CALL cheatSheetCreate('c7ac1e05faa2d4b1016cfcc726e0689419662784', 'c7ac1e05faa2d4b1016cfcc726e0689419662784.solution');
CALL cheatSheetCreate('b3cfd5890649e6815a1c7107cc41d17c82826cfa', 'b3cfd5890649e6815a1c7107cc41d17c82826cfa.solution');
CALL cheatSheetCreate('ced925f8357a17cfe3225c6236df0f681b2447c4', 'ced925f8357a17cfe3225c6236df0f681b2447c4.solution');
CALL cheatSheetCreate('c6841bcc326c4bad3a23cd4fa6391eb9bdb146ed', 'c6841bcc326c4bad3a23cd4fa6391eb9bdb146ed.solution');
CALL cheatsheetCreate('53a53a66cb3bf3e4c665c442425ca90e29536edd', '53a53a66cb3bf3e4c665c442425ca90e29536edd.solution');
CALL cheatsheetCreate('307f78f18fd6a87e50ed6705231a9f24cd582574', '307f78f18fd6a87e50ed6705231a9f24cd582574.solution');
CALL cheatsheetCreate('da3de2e556494a9c2fb7308a98454cf55f3a4911', 'da3de2e556494a9c2fb7308a98454cf55f3a4911.solution');
CALL cheatsheetCreate('335440fef02d19259254ed88293b62f31cccdd41', '335440fef02d19259254ed88293b62f31cccdd41.solution');
CALL cheatsheetCreate('a3f7ffd0f9c3d15564428d4df0b91bd927e4e5e4', 'a3f7ffd0f9c3d15564428d4df0b91bd927e4e5e4.solution');
CALL cheatsheetCreate('e635fce334aa61fdaa459c21c286d6332eddcdd3', 'e635fce334aa61fdaa459c21c286d6332eddcdd3.solution');
CALL cheatsheetCreate('ef6496892b8e48ac2f349cdd7c8ecb889fc982af', 'ef6496892b8e48ac2f349cdd7c8ecb889fc982af.solution');
CALL cheatsheetCreate('3f010a976bcbd6a37fba4a10e4a057acc80bdc09', '3f010a976bcbd6a37fba4a10e4a057acc80bdc09.solution');
CALL cheatsheetCreate('63bc4811a2e72a7c833962e5d47a41251cd90de3', '63bc4811a2e72a7c833962e5d47a41251cd90de3.solution');
CALL cheatsheetCreate('2ab09c0c18470ae5f87d219d019a1f603e66f944', '2ab09c0c18470ae5f87d219d019a1f603e66f944.solution');
CALL cheatsheetCreate('f16bf2ab1c1bf400d36330f91e9ac6045edcd003', 'f16bf2ab1c1bf400d36330f91e9ac6045edcd003.solution');
CALL cheatsheetCreate('9e46e3c8bde42dc16b9131c0547eedbf265e8f16', '9e46e3c8bde42dc16b9131c0547eedbf265e8f16.solution');
CALL cheatsheetCreate('1506f22cd73d14d8a73e0ee32006f35d4f234799', '1506f22cd73d14d8a73e0ee32006f35d4f234799.solution');
CALL cheatSheetCreate('ed732e695b85baca21d80966306a9ab5ec37477f', 'ed732e695b85baca21d80966306a9ab5ec37477f.solution');
CALL cheatSheetCreate('cfbf7b915ee56508ad46ab79878f37fd9afe0d27', 'cfbf7b915ee56508ad46ab79878f37fd9afe0d27.solution');
CALL cheatSheetCreate('9294ba32bdbd680e3260a0315cd98bf6ce8b69bd', '9294ba32bdbd680e3260a0315cd98bf6ce8b69bd.solution');
CALL cheatSheetCreate('7153290d128cfdef5f40742dbaeb129a36ac2340', '7153290d128cfdef5f40742dbaeb129a36ac2340.solution');
CALL cheatSheetCreate('145111e80400e4fd48bd3aa5aca382e9c5640793', '145111e80400e4fd48bd3aa5aca382e9c5640793.solution');
CALL cheatSheetCreate('adc845f9624716eefabcc90d172bab4096fa2ac4', 'adc845f9624716eefabcc90d172bab4096fa2ac4.solution');
CALL cheatSheetCreate('64070f5aec0593962a29a141110b9239d73cd7b3', '64070f5aec0593962a29a141110b9239d73cd7b3.solution');
CALL cheatSheetCreate('1e3c02ad49fa9a9e396a3b268d7da8f0b647d8f9', '1e3c02ad49fa9a9e396a3b268d7da8f0b647d8f9.solution');
CALL cheatSheetCreate('f40b0cd5d45327c9426675313f581cf70c7c7c28', 'f40b0cd5d45327c9426675313f581cf70c7c7c28.solution');
CALL cheatSheetCreate('ba6e65e4881c8499b5e53eb33b5be6b5d0f1fb2c', 'ba6e65e4881c8499b5e53eb33b5be6b5d0f1fb2c.solution');
CALL cheatSheetCreate('52885a3db5b09adc24f38bc453fe348f850649b3', '52885a3db5b09adc24f38bc453fe348f850649b3.solution');
CALL cheatSheetCreate('3b1af0ad239325bf494c6e606585320b31612e72', '3b1af0ad239325bf494c6e606585320b31612e72.solution');
CALL cheatSheetCreate('0cdd1549e7c74084d7059ce748b93ef657b44457', '0cdd1549e7c74084d7059ce748b93ef657b44457.solution');
CALL cheatSheetCreate('368491877a0318e9a774ba5d648c33cb0165ba1e', '368491877a0318e9a774ba5d648c33cb0165ba1e.solution');
CALL cheatSheetCreate('6be5de81223cc1b38b6e427cc44f8b6a28d2bc96', '6be5de81223cc1b38b6e427cc44f8b6a28d2bc96.solution');
CALL cheatSheetCreate('3b14ca3c8f9b90c9b2c8cd1fba9fa67add1272a3', '3b14ca3c8f9b90c9b2c8cd1fba9fa67add1272a3.solution');
CALL cheatSheetCreate('b9d82aa7b46ddaddb6acfe470452a8362136a31e', 'b9d82aa7b46ddaddb6acfe470452a8362136a31e.solution');
CALL cheatSheetCreate('bf847c4a8153d487d6ec36f4fca9b77749597c64', 'bf847c4a8153d487d6ec36f4fca9b77749597c64.solution');
CALL cheatSheetCreate('fcc2558e0a23b8420e173cf8029876cb887408d3', 'fcc2558e0a23b8420e173cf8029876cb887408d3.solution');
CALL cheatSheetCreate('6158a695f20f9286d5f12ff3f4d42678f4a9740c', '6158a695f20f9286d5f12ff3f4d42678f4a9740c.solution');
CALL cheatSheetCreate('de626470273c01388629e5a56ac6f17e2eef957b', 'de626470273c01388629e5a56ac6f17e2eef957b.solution');
CALL cheatSheetCreate('dc89383763c68cba0aaa1c6f3fd4c17e9d49a805', 'dc89383763c68cba0aaa1c6f3fd4c17e9d49a805.solution');
CALL cheatSheetCreate('5b461ebe2e5e2797740cb3e9c7e3f93449a93e3a', '5b461ebe2e5e2797740cb3e9c7e3f93449a93e3a.solution');
CALL cheatSheetCreate('c685f8102ae0128c2ab342df64699bb8209a0839', 'c685f8102ae0128c2ab342df64699bb8209a0839.solution');
CALL cheatSheetCreate('d7eaeaa1cc4f218abd86d14eefa183a0f8eb6298', 'd7eaeaa1cc4f218abd86d14eefa183a0f8eb6298.solution');