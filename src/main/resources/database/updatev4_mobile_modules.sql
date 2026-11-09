-- Upgrade script for existing Security Shepherd installations: mobile module flags.
--
-- coreSchema.sql only runs on a fresh install, so an existing database keeps its old
-- rows. This script brings those rows in line with the mobile app and is safe to run
-- more than once. Run it against the `core` schema; see docs/upgrading.md.

USE `core`;

-- 1. Flags that the web /solutionSubmit endpoint must accept for the app's modules.
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
