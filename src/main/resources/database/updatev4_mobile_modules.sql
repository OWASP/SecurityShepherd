-- Upgrade script for existing Security Shepherd installations: mobile module changes.
--
-- coreSchema.sql only runs on a fresh install, so an existing database keeps its old
-- rows. This script brings those rows in line with the mobile app and is safe to run
-- more than once. Run it against the `core` schema; see docs/upgrading.md.

USE `core`;

-- 1. New modules (Mobile Insecure Communication lesson and challenge).
INSERT IGNORE INTO modules (`moduleId`, `moduleName`, `moduleNameLangPointer`, `moduleType`, `moduleCategory`, `moduleCategoryLangPointer`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `hardcodedKey`, `isUnsafe`) VALUES ('a76d11ebd575aecfba5d69441cbd90c95e8abe31', 'Mobile Insecure Communication', 'mobile.insecure.communication', 'lesson', 'Mobile Insecure Communication', 'mobile.insecure.communication', 'Green_Socks_Float_Past_Reeds', '6a7a56237fbedee639dabbae9a3d6a0c069fdcf8dd5c89f45d8f0b0e2f44e7e2', 'open', '106', '35', 0, 0);
INSERT IGNORE INTO modules (`moduleId`, `moduleName`, `moduleNameLangPointer`, `moduleType`, `moduleCategory`, `moduleCategoryLangPointer`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `hardcodedKey`, `isUnsafe`) VALUES ('d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7', 'Mobile Insecure Communication Challenge', 'mobile.insecure.communication.challenge', 'challenge', 'Mobile Insecure Communication', 'mobile.insecure.communication', 'Empty_Wave_Breaks_On_Shore', 'd8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9', 'open', '107', '60', 0, 0);

-- 2. Cheat sheets for the new modules, added only if the module has none yet.
INSERT INTO cheatsheet (cheatSheetId, moduleId, createDate, solution)
SELECT SHA(CONCAT('mobile-upgrade-', 'a76d11ebd575aecfba5d69441cbd90c95e8abe31')), 'a76d11ebd575aecfba5d69441cbd90c95e8abe31', NOW(), 'a76d11ebd575aecfba5d69441cbd90c95e8abe31.solution'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM modules WHERE moduleId = 'a76d11ebd575aecfba5d69441cbd90c95e8abe31')
  AND NOT EXISTS (SELECT 1 FROM cheatsheet WHERE moduleId = 'a76d11ebd575aecfba5d69441cbd90c95e8abe31');
INSERT INTO cheatsheet (cheatSheetId, moduleId, createDate, solution)
SELECT SHA(CONCAT('mobile-upgrade-', 'd8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7')), 'd8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7', NOW(), 'd8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7.solution'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM modules WHERE moduleId = 'd8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7')
  AND NOT EXISTS (SELECT 1 FROM cheatsheet WHERE moduleId = 'd8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7');

-- 3. Flags that the web /solutionSubmit endpoint must accept for the app's modules.
--    hardcodedKey = 0: the module result is a base flag, and each player's flag is derived
--    from it (the same value the app's /mobileFlagGet endpoint issues).
--    hardcodedKey = 1: the module result is the key found offline in the app (Reverse Engineering).
UPDATE modules SET moduleResult = 'Copper_Bridge_Stands_In_Rain', hardcodedKey = 0 WHERE moduleId = '53a53a66cb3bf3e4c665c442425ca90e29536edd';
UPDATE modules SET moduleResult = 'Tangled_Rope_Skips_A_Stone', hardcodedKey = 0 WHERE moduleId = '307f78f18fd6a87e50ed6705231a9f24cd582574';
UPDATE modules SET moduleResult = 'Taco_Snores_On_A_Couch', hardcodedKey = 0 WHERE moduleId = '0cdd1549e7c74084d7059ce748b93ef657b44457';
UPDATE modules SET moduleResult = 'Cracked_Jar_Holds_Old_Honey', hardcodedKey = 0 WHERE moduleId = 'ba6e65e4881c8499b5e53eb33b5be6b5d0f1fb2c';
UPDATE modules SET moduleResult = 'Marble_Rolls_Past_The_Cat', hardcodedKey = 0 WHERE moduleId = '335440fef02d19259254ed88293b62f31cccdd41';
UPDATE modules SET moduleResult = 'Bitter_Snake_Finds_The_Lock', hardcodedKey = 0 WHERE moduleId = 'a3f7ffd0f9c3d15564428d4df0b91bd927e4e5e4';
UPDATE modules SET moduleResult = 'Hollow_Bridge_Holds_No_Rain', hardcodedKey = 0 WHERE moduleId = 'e635fce334aa61fdaa459c21c286d6332eddcdd3';
UPDATE modules SET moduleResult = 'Frozen_Clock_Melts_By_Noon', hardcodedKey = 1 WHERE moduleId = '2ab09c0c18470ae5f87d219d019a1f603e66f944';
UPDATE modules SET moduleResult = 'Shadow_Key_Hangs_By_Thread', hardcodedKey = 1 WHERE moduleId = '52885a3db5b09adc24f38bc453fe348f850649b3';