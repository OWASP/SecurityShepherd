use core;
-- --------------------------------------
-- Class Table Update
-- --------------------------------------
SELECT "Updating Class Table Structure" FROM DUAL;
ALTER TABLE class ADD UNIQUE (className);

-- --------------------------------------
-- Module Table Stuff
-- --------------------------------------
SELECT "Updating Module Table Structure" FROM DUAL;
ALTER TABLE modules ADD moduleNameLangPointer VARCHAR(64);
ALTER TABLE modules ADD moduleCategoryLangPointer VARCHAR(64);
COMMIT;
UPDATE modules SET moduleNameLangPointer = REPLACE(LOWER(moduleName), " ", "."), moduleCategoryLangPointer = REPLACE(LOWER(moduleCategory), " ", ".");
COMMIT;
UPDATE modules SET moduleCategoryLangPointer = 'poor.data.validation', moduleCategory = 'Poor Data Validation' WHERE moduleCategoryLangPointer = 'bad.data.validation';
UPDATE modules SET moduleNameLangPointer = 'failure.to.restrict.url.access.1', moduleName = 'Failure to Restrict URL Access 1' WHERE moduleId = '3d5b46abc6865ba09aaff98a8278a5f5e339abff';
UPDATE modules SET moduleNameLangPointer = 'failure.to.restrict.url.access.2', moduleName = 'Failure to Restrict URL Access 2' WHERE moduleId = 'c7ac1e05faa2d4b1016cfcc726e0689419662784';
COMMIT;

SELECT "Adding New Modules" FROM DUAL;
INSERT INTO modules (`moduleId`, `moduleName`, `moduleNameLangPointer`, `moduleType`, `moduleCategory`, `moduleCategoryLangPointer`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('5b461ebe2e5e2797740cb3e9c7e3f93449a93e3a', 'Content Provider Leakage', 'content.provider.leakage', 'lesson', 'Mobile Content Providers', 'mobile.content.providers', 'LazerLizardsFlamingWizards', '4d41997b5b81c88f7eb761c1975481c4ce397b80291d99307cfad69662277d39', 'open', '79', '50', '5', 1);
INSERT INTO modules (`moduleId`, `moduleName`, `moduleNameLangPointer`, `moduleType`, `moduleCategory`, `moduleCategoryLangPointer`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('d7eaeaa1cc4f218abd86d14eefa183a0f8eb6298', 'NoSQL Injection One', 'nosql.injection.one', 'challenge', 'Injection', 'injection', 'c09f32d4c3dd5b75f04108e5ffc9226cd8840288a62bdaf9dc65828ab6eaf86a', 'd63c2fb5da9b81ca26237f1308afe54491d1bacf9fffa0b21a072b03c5bafe66', 'open', '89', '45', '5', 1);
INSERT INTO modules (`moduleId`, `moduleName`, `moduleNameLangPointer`, `moduleType`, `moduleCategory`, `moduleCategoryLangPointer`, `moduleResult`, `moduleHash`, `moduleStatus`, `incrementalRank`, `scoreValue`, `scoreBonus`, `hardcodedKey`) VALUES ('c685f8102ae0128c2ab342df64699bb8209a0839', 'SQL Injection Escaping', 'sql.injection.escaping', 'challenge', 'Injection', 'injection', '0dcf9078ba5d878f9e23809ac8f013d1a08fdc8f12c5036f1a4746dbe86c0aac', '8c3c35c30cdbbb73b7be3a4f8587aa9d88044dc43e248984a252c6e861f673d4', 'open', '99', '50', '5', 1);
UPDATE modules SET moduleName = 'SQL Injection New Challenge 2', moduleId = '544aa22d3dd16a8232b093848a6523b0712b23daNEW', moduleNameLangPointer = 'sql.injection.2.new', moduleHash = 'ffd39cb26727f34cbf9fce3e82b9d703404e99cdef54d2aa745f497abe070b', moduleResult = 'f62abebf5658a6a44c5c9babc7865110c62f5ecd9d0a7052db48c4dbee0200e3', incrementalRank = 85, scoreValue = 45 WHERE moduleId = 'f771a10efb42a79a9dba262fd2be2e44bf40b66d';
UPDATE modules SET moduleId = 'f771a10efb42a79a9dba262fd2be2e44bf40b66d', moduleName = 'SQL Injection 1', moduleNameLangPointer = 'sql.injection.1', moduleResult = 'fd8e9a29dab791197115b58061b215594211e72c1680f1eacc50b0394133a09f', moduleHash = 'e1e109444bf5d7ae3d67b816538613e64f7d0f51c432a164efc8418513711b0a', incrementalRank = 68, scoreValue = 35 WHERE moduleId = '544aa22d3dd16a8232b093848a6523b0712b23da';
COMMIT;
UPDATE modules SET moduleName = "SQL Injection 2", moduleNameLangPointer = 'sql.injection.2' WHERE moduleId = '544aa22d3dd16a8232b093848a6523b0712b23daNEW';
COMMIT;

SELECT "Updating Module Entries to Match v3.0" FROM DUAL;

UPDATE modules SET moduleName ='CSRF 6', moduleCategory = 'CSRF', moduleCategoryLangPointer = 'csrf', moduleResult = 'df611f54325786d42e6deae8bbd0b9d21cf2c9282ec6de4e04166abe2792ac00', incrementalRank = 176, scoreValue = 90, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '04a5bd8656fdeceac26e21ef6b04b90eaafbd7d5';
UPDATE modules SET moduleName ='Insecure Cryptographic Storage Challenge 2', moduleCategory = 'Insecure Cryptographic Storage', moduleCategoryLangPointer = 'insecure.cryptographic.storage', moduleResult = 'TheVigenereCipherIsAmethodOfEncryptingAlphabeticTextByUsingPoly', incrementalRank = 126, scoreValue = 65, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '0709410108f91314fb6f7721df9b891351eb2fcc';
UPDATE modules SET moduleName ='CSRF 5', moduleCategory = 'CSRF', moduleCategoryLangPointer = 'csrf', moduleResult = '8f34078ef3e53f619618d9def1ede8a6a9117c77c2fad22f76bba633da83e6d4', incrementalRank = 156, scoreValue = 80, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '0a37cb9296ff3763f7f3a45ff313bce47afa9384';
UPDATE modules SET moduleName ='Poor Authentication', moduleCategory = 'Mobile Poor Authentication', moduleCategoryLangPointer = 'mobile.poor.authentication', moduleResult = 'UpsideDownPizzaDip', incrementalRank = 90, scoreValue = 50, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '0cdd1549e7c74084d7059ce748b93ef657b44457';
UPDATE modules SET moduleName ='Insecure Direct Object References', moduleCategory = 'Insecure Direct Object References', moduleCategoryLangPointer = 'insecure.direct.object.references', moduleResult = '59e571b1e59441e76e0c85e5b49', incrementalRank = 5, scoreValue = 10, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '0dbea4cb5811fff0527184f99bd5034ca9286f11';
UPDATE modules SET moduleName ='Session Management Challenge 3', moduleCategory = 'Session Management', moduleCategoryLangPointer = 'session.management', moduleResult = 'e62008dc47f5eb065229d48963', incrementalRank = 115, scoreValue = 60, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '0e9e650ffca2d1fe516c5d7b0ce5c32de9e53d1e';
UPDATE modules SET moduleName ='Insecure Cryptographic Storage Challenge 4', moduleCategory = 'Insecure Cryptographic Storage', moduleCategoryLangPointer = 'insecure.cryptographic.storage', moduleResult = '50980917266ce6ec07471f49b1a046ca6a5034eb9261fb44c3ffc4b16931255c', incrementalRank = 177, scoreValue = 90, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '145111e80400e4fd48bd3aa5aca382e9c5640793';
UPDATE modules SET moduleName ='Unintended Data Leakage', moduleCategory = 'Mobile Data Leakage', moduleCategoryLangPointer = 'mobile.data.leakage', moduleResult = 'SilentButSteadyRedLed', incrementalRank = 77, scoreValue = 40, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '1506f22cd73d14d8a73e0ee32006f35d4f234799';
UPDATE modules SET moduleName ='Cross Site Scripting 4', moduleCategory = 'XSS', moduleCategoryLangPointer = 'xss', moduleResult = '515e05137e023dd7828adc03f639c8b13752fbdffab2353ccec', incrementalRank = 146, scoreValue = 75, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '182f519ef2add981c77a584380f41875edc65a56';
UPDATE modules SET moduleName ='Unintended Data Leakage 2', moduleCategory = 'Mobile Data Leakage', moduleCategoryLangPointer = 'mobile.data.leakage', moduleResult = '627884736748', incrementalRank = 140, scoreValue = 70, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '1e3c02ad49fa9a9e396a3b268d7da8f0b647d8f9';
UPDATE modules SET moduleName ='Insecure Cryptographic Storage', moduleCategory = 'Insecure Cryptographic Storage', moduleCategoryLangPointer = 'insecure.cryptographic.storage', moduleResult = 'base64isNotEncryptionBase64isEncodingBase64HidesNothingFromYou', incrementalRank = 46, scoreValue = 25, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '201ae6f8c55ba3f3b5881806387fbf34b15c30c2';
UPDATE modules SET moduleName ='CSRF 1', moduleCategory = 'CSRF', moduleCategoryLangPointer = 'csrf', moduleResult = '7639c952a191d569a0c741843b599604c37e33f9f5d8eb07abf0254635320b07', incrementalRank = 106, scoreValue = 55, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '20e755179a5840be5503d42bb3711716235005ea';
UPDATE modules SET moduleName ='Reverse Engineering', moduleCategory = 'Mobile Reverse Engineering', moduleCategoryLangPointer = 'mobile.reverse.engineering', moduleResult = 'DrumaDrumaDrumBoomBoom', incrementalRank = 75, scoreValue = 40, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '2ab09c0c18470ae5f87d219d019a1f603e66f944';
UPDATE modules SET moduleName ='Insecure Direct Object Reference Challenge 1', moduleCategory = 'Insecure Direct Object References', moduleCategoryLangPointer = 'insecure.direct.object.references', moduleResult = 'dd6301b38b5ad9c54b85d07c087aebec89df8b8c769d4da084a55663e6186742', incrementalRank = 66, scoreValue = 35, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '2dc909fd89c2b03059b1512e7b54ce5d1aaa4bb4';
UPDATE modules SET moduleName ='Insecure Data Storage 1', moduleCategory = 'Mobile Insecure Data Storage', moduleCategoryLangPointer = 'mobile.insecure.data.storage', moduleResult = 'WarshipsAndWrenches', incrementalRank = 116, scoreValue = 60, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '307f78f18fd6a87e50ed6705231a9f24cd582574';
UPDATE modules SET moduleName ='Client Side Injection', moduleCategory = 'Mobile Injection', moduleCategoryLangPointer = 'mobile.injection', moduleResult = 'VolcanicEruptionsAbruptInterruptions', incrementalRank = 87, scoreValue = 50, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '335440fef02d19259254ed88293b62f31cccdd41';
UPDATE modules SET moduleName ='Session Management Challenge 7', moduleCategory = 'Session Management', moduleCategoryLangPointer = 'session.management', moduleResult = '9042eeaa8455f71deea31a5a32ae51e71477b1581c3612972902206ac51bb621', incrementalRank = 209, scoreValue = 110, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '368491877a0318e9a774ba5d648c33cb0165ba1e';
UPDATE modules SET moduleName ='Poor Data Validation 2', moduleCategory = 'Poor Data Validation', moduleCategoryLangPointer = 'poor.data.validation', moduleResult = '05adf1e4afeb5550faf7edbec99170b40e79168ecb3a5da19943f05a3fe08c8e', incrementalRank = 157, scoreValue = 80, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '3b14ca3c8f9b90c9b2c8cd1fba9fa67add1272a3';
UPDATE modules SET moduleName ='Broken Crypto 3', moduleCategory = 'Mobile Broken Crypto', moduleCategoryLangPointer = 'mobile.broken.crypto', moduleResult = 'ShaveTheSkies', incrementalRank = 180, scoreValue = 180, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '3b1af0ad239325bf494c6e606585320b31612e72';
UPDATE modules SET moduleName ='Failure to Restrict URL Access 1', moduleCategory = 'Failure to Restrict URL Access', moduleCategoryLangPointer = 'failure.to.restrict.url.access', moduleResult = 'c776572b6a9d5b5c6e4aa672a4771213', incrementalRank = 76, scoreValue = 40, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '3d5b46abc6865ba09aaff98a8278a5f5e339abff';
UPDATE modules SET moduleName ='Broken Crypto 1', moduleCategory = 'Mobile Broken Crypto', moduleCategoryLangPointer = 'mobile.broken.crypto', moduleResult = 'd1f2df53084b970ab538457f5af34c8b', incrementalRank = 117, scoreValue = 60, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '3f010a976bcbd6a37fba4a10e4a057acc80bdc09';
UPDATE modules SET moduleName ='SQL Injection', moduleCategory = 'Injection', moduleCategoryLangPointer = 'injection', moduleResult = '3c17f6bf34080979e0cebda5672e989c07ceec9fa4ee7b7c17c9e3ce26bc63e0', incrementalRank = 55, scoreValue = 30, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '408610f220b4f71f7261207a17055acbffb8a747';
UPDATE modules SET moduleName ='Cross Site Request Forgery', moduleCategory = 'CSRF', moduleCategoryLangPointer = 'csrf', moduleResult = '666980771c29857b8a84c686751ce7edaae3d6ac1', incrementalRank = 78, scoreValue = 40, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '453d22238401e0bf6f1ff5d45996407e98e45b07';
UPDATE modules SET moduleName ='Reverse Engineering 1', moduleCategory = 'Mobile Reverse Engineering', moduleCategoryLangPointer = 'mobile.reverse.engineering', moduleResult = 'christopherjenkins', incrementalRank = 85, scoreValue = 50, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '52885a3db5b09adc24f38bc453fe348f850649b3';
UPDATE modules SET moduleName ='Failure to Restrict URL Access', moduleCategory = 'Failure to Restrict URL Access', moduleCategoryLangPointer = 'failure.to.restrict.url.access', moduleResult = 'f60d1337ac4d35cb67880a3adda79', incrementalRank = 25, scoreValue = 15, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '52c5394cdedfb2e95b3ad8b92d0d6c9d1370ea9a';
UPDATE modules SET moduleName ='Insecure Data Storage', moduleCategory = 'Mobile Insecure Data Storage', moduleCategoryLangPointer = 'mobile.insecure.data.storage', moduleResult = 'Battery777', incrementalRank = 45, scoreValue = 25, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '53a53a66cb3bf3e4c665c442425ca90e29536edd';
UPDATE modules SET moduleName ='SQL Injection 1', moduleCategory = 'Injection', moduleCategoryLangPointer = 'injection', moduleResult = 'fd8e9a29dab791197115b58061b215594211e72c1680f1eacc50b0394133a09f', incrementalRank = 68, scoreValue = 35, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '544aa22d3dd16a8232b093848a6523b0712b23da';
UPDATE modules SET moduleName ='Content Provider Leakage', moduleCategory = 'Mobile Content Providers', moduleCategoryLangPointer = 'mobile.content.providers', moduleResult = 'LazerLizardsFlamingWizards', incrementalRank = 79, scoreValue = 50, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '5b461ebe2e5e2797740cb3e9c7e3f93449a93e3a';
UPDATE modules SET moduleName ='CSRF 3', moduleCategory = 'CSRF', moduleCategoryLangPointer = 'csrf', moduleResult = '6bdbe1901cbe2e2749f347efb9ec2be820cc9396db236970e384604d2d55b62a', incrementalRank = 137, scoreValue = 70, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '5ca9115f3279b9b9f3308eb6a59a4fcd374846d6';
UPDATE modules SET moduleName ='SQL Injection 3', moduleCategory = 'Injection', moduleCategoryLangPointer = 'injection', moduleResult = '9815 1547 3214 7569', incrementalRank = 135, scoreValue = 70, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '5dda8dc216bd6a46fccaa4ed45d49404cdc1c82e';
UPDATE modules SET moduleName ='Security Misconfig Cookie Flag', moduleCategory = 'Security Misconfigurations', moduleCategoryLangPointer = 'security.misconfigurations', moduleResult = '92755de2ebb012e689caf8bfec629b1e237d23438427499b6bf0d7933f1b8215', incrementalRank = 208, scoreValue = 110, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '6158a695f20f9286d5f12ff3f4d42678f4a9740c';
UPDATE modules SET moduleName ='Cross Site Scripting 3', moduleCategory = 'XSS', moduleCategoryLangPointer = 'xss', moduleResult = '6abaf491c9122db375533c04df', incrementalRank = 128, scoreValue = 65, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '6319a2e38cc4b2dc9e6d840e1b81db11ee8e5342';
UPDATE modules SET moduleName ='Broken Crypto 2', moduleCategory = 'Mobile Broken Crypto', moduleCategoryLangPointer = 'mobile.broken.crypto', moduleResult = 'DancingRobotChilliSauce', incrementalRank = 149, scoreValue = 75, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '63bc4811a2e72a7c833962e5d47a41251cd90de3';
UPDATE modules SET moduleName ='SQL Injection 7', moduleCategory = 'Injection', moduleCategoryLangPointer = 'injection', moduleResult = '4637cae3d9b961fdff880d6d5ce4f69e91fe23db0aae7dcd4038e20ed8a287dc', incrementalRank = 210, scoreValue = 110, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '64070f5aec0593962a29a141110b9239d73cd7b3';
UPDATE modules SET moduleName ='Poor Data Validation 1', moduleCategory = 'Poor Data Validation', moduleCategoryLangPointer = 'poor.data.validation', moduleResult = 'd30475881612685092e5ec469317dcc5ccc1f548a97bfdb041236b5bba7627bf', incrementalRank = 67, scoreValue = 35, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '6be5de81223cc1b38b6e427cc44f8b6a28d2bc96';
UPDATE modules SET moduleName ='Session Management Challenge 8', moduleCategory = 'Session Management', moduleCategoryLangPointer = 'session.management', moduleResult = '11d84b0ad628bb6e99e0640ff1791a29a1938609829ef5bdccee92b2bccd2bcd', incrementalRank = 215, scoreValue = 115, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '7153290d128cfdef5f40742dbaeb129a36ac2340';
UPDATE modules SET moduleName ='Insecure Direct Object Reference Challenge 2', moduleCategory = 'Insecure Direct Object References', moduleCategoryLangPointer = 'insecure.direct.object.references', moduleResult = '1f746b87a4e3628b90b1927de23f6077abdbbb64586d3ac9485625da21921a0f', incrementalRank = 127, scoreValue = 65, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '82e8e9e2941a06852b90c97087309b067aeb2c4c';
UPDATE modules SET moduleName ='CSRF 7', moduleCategory = 'CSRF', moduleCategoryLangPointer = 'csrf', moduleResult = '849e1efbb0c1e870d17d32a3e1b18a8836514619146521fbec6623fce67b73e8', incrementalRank = 235, scoreValue = 120, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5';
UPDATE modules SET moduleName ='Insecure Cryptographic Storage Challenge 1', moduleCategory = 'Insecure Cryptographic Storage', moduleCategoryLangPointer = 'insecure.cryptographic.storage', moduleResult = 'mylovelyhorserunningthroughthefieldwhereareyougoingwithyourbiga', incrementalRank = 65, scoreValue = 35, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '891a0208a95f1791287be721a4b851d4c584880a';
UPDATE modules SET moduleName ='Session Management Challenge 6', moduleCategory = 'Session Management', moduleCategoryLangPointer = 'session.management', moduleResult = 'bb0eb566322d6b1f1dff388f5eee9929f6f1f9f5cac9eed266ef6e5053fe08e6', incrementalRank = 207, scoreValue = 110, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '9294ba32bdbd680e3260a0315cd98bf6ce8b69bd';
UPDATE modules SET moduleName ='CSRF 2', moduleCategory = 'CSRF', moduleCategoryLangPointer = 'csrf', moduleResult = '45309dbaf8eaf6d1a5f1ecb1bf1b6be368a6542d3da35b9bf0224b88408dc001', incrementalRank = 136, scoreValue = 70, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '94cd2de560d89ef59fc450ecc647ff4d4a55c15d';
UPDATE modules SET moduleName ='Broken Session Management', moduleCategory = 'Session Management', moduleCategoryLangPointer = 'session.management', moduleResult = '6594dec9ff7c4e60d9f8945ca0d4', incrementalRank = 16, scoreValue = 10, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = '9533e21e285621a676bec58fc089065dec1f59f5';
UPDATE modules SET moduleName ='Reverse Engineering 3', moduleCategory = 'Mobile Reverse Engineering', moduleCategoryLangPointer = 'mobile.reverse.engineering', moduleResult = 'C1babd72225f0e9934YZ8', incrementalRank = 120, scoreValue = 76, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = '9e46e3c8bde42dc16b9131c0547eedbf265e8f16';
UPDATE modules SET moduleName ='Client Side Injection 1', moduleCategory = 'Mobile Injection', moduleCategoryLangPointer = 'mobile.injection', moduleResult = 'SourHatsAndAngryCats', incrementalRank = 138, scoreValue = 70, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = 'a3f7ffd0f9c3d15564428d4df0b91bd927e4e5e4';
UPDATE modules SET moduleName ='Session Management Challenge 1', moduleCategory = 'Session Management', moduleCategoryLangPointer = 'session.management', moduleResult = 'db7b1da5d7a43c7100a6f01bb0c', incrementalRank = 75, scoreValue = 40, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = 'a4bf43f2ba5ced041b03d0b3f9fa3541c520d65d';
UPDATE modules SET moduleName ='SQL Injection 5', moduleCategory = 'Injection', moduleCategoryLangPointer = 'injection', moduleResult = '343f2e424d5d7a2eff7f9ee5a5a72fd97d5a19ef7bff3ef2953e033ea32dd7ee', incrementalRank = 175, scoreValue = 90, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = 'a84bbf8737a9ca749d81d5226fc87e0c828138ee';
UPDATE modules SET moduleName ='SQL Injection 6', moduleCategory = 'Injection', moduleCategoryLangPointer = 'injection', moduleResult = '17f999a8b3fbfde54124d6e94b256a264652e5087b14622e1644c884f8a33f82', incrementalRank = 186, scoreValue = 95, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = 'ad332a32a6af1f005f9c8d1e98db264eb2ae5dfe';
UPDATE modules SET moduleName ='Failure to Restrict URL Access 3', moduleCategory = 'Failure to Restrict URL Access', moduleCategoryLangPointer = 'failure.to.restrict.url.access', moduleResult = '8c1dbfdc7cad35a116535f76f21e448c6c7c0ebc395be2be80e5690e01adec18', incrementalRank = 206, scoreValue = 110, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = 'adc845f9624716eefabcc90d172bab4096fa2ac4';
UPDATE modules SET moduleName ='Insecure Cryptographic Storage Challenge 3', moduleCategory = 'Insecure Cryptographic Storage', moduleCategoryLangPointer = 'insecure.cryptographic.storage', moduleResult = 'THISISTHESECURITYSHEPHERDABCENCRYPTIONKEY', incrementalRank = 148, scoreValue = 75, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = 'b3cfd5890649e6815a1c7107cc41d17c82826cfa';
UPDATE modules SET moduleName ='Unvalidated Redirects and Forwards', moduleCategory = 'Unvalidated Redirects and Forwards', moduleCategoryLangPointer = 'unvalidated.redirects.and.forwards', moduleResult = '658c43abcf81a61ca5234cfd7a2', incrementalRank = 86, scoreValue = 45, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = 'b6432a6b5022cb044e9946315c44ab262ab59e88';
UPDATE modules SET moduleName ='Session Management Challenge 2', moduleCategory = 'Session Management', moduleCategoryLangPointer = 'session.management', moduleResult = '4ba31e5ffe29de092fe1950422a', incrementalRank = 105, scoreValue = 55, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = 'b70a84f159876bb9885b6e0087d22f0a52abbfcf';
UPDATE modules SET moduleName ='Poor Data Validation', moduleCategory = 'Poor Data Validation', moduleCategoryLangPointer = 'poor.data.validation', moduleResult = '6680b08b175c9f3d521764b41349fcbd3c0ad0a76655a10d42372ebccdfdb4bb', incrementalRank = 6, scoreValue = 10, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = 'b9d82aa7b46ddaddb6acfe470452a8362136a31e';
UPDATE modules SET moduleName ='Poor Authentication 1', moduleCategory = 'Mobile Poor Authentication', moduleCategoryLangPointer = 'mobile.poor.authentication', moduleResult = 'MegaKillerExtremeCheese', incrementalRank = 160, scoreValue = 60, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = 'ba6e65e4881c8499b5e53eb33b5be6b5d0f1fb2c';
UPDATE modules SET moduleName ='Security Misconfiguration', moduleCategory = 'Security Misconfigurations', moduleCategoryLangPointer = 'security.misconfigurations', moduleResult = '55b34717d014a5a355f6eced4386878fab0b2793e1d1dbfd23e6262cd510ea96', incrementalRank = 7, scoreValue = 10, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = 'bf847c4a8153d487d6ec36f4fca9b77749597c64';
UPDATE modules SET moduleName ='Cross Site Scripting 6', moduleCategory = 'XSS', moduleCategoryLangPointer = 'xss', moduleResult = 'c13e42171dbd41a7020852ffdd3399b63a87f5', incrementalRank = 185, scoreValue = 95, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = 'c6841bcc326c4bad3a23cd4fa6391eb9bdb146ed';
UPDATE modules SET moduleName ='SQL Injection Escaping', moduleCategory = 'Injection', moduleCategoryLangPointer = 'injection', moduleResult = '0dcf9078ba5d878f9e23809ac8f013d1a08fdc8f12c5036f1a4746dbe86c0aac', incrementalRank = 99, scoreValue = 50, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = 'c685f8102ae0128c2ab342df64699bb8209a0839';
UPDATE modules SET moduleName ='Failure to Restrict URL Access 2', moduleCategory = 'Failure to Restrict URL Access', moduleCategoryLangPointer = 'failure.to.restrict.url.access', moduleResult = '40b675e3d404c52b36abe31d05842b283975ec62e8', incrementalRank = 165, scoreValue = 85, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = 'c7ac1e05faa2d4b1016cfcc726e0689419662784';
UPDATE modules SET moduleName ='Cross Site Scripting', moduleCategory = 'XSS', moduleCategoryLangPointer = 'xss', moduleResult = 'ea7b563b2935d8587539d747d', incrementalRank = 26, scoreValue = 15, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = 'ca8233e0398ecfa76f9e05a49d49f4a7ba390d07';
UPDATE modules SET moduleName ='Insecure Data Storage 3', moduleCategory = 'Mobile Insecure Data Storage', moduleCategoryLangPointer = 'mobile.insecure.data.storage', moduleResult = 'c4ptainBrunch', incrementalRank = 130, scoreValue = 60, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = 'cb7d696bdf88899e8077063d911fc8da14176702';
UPDATE modules SET moduleName ='Cross Site Scripting 1', moduleCategory = 'XSS', moduleCategoryLangPointer = 'xss', moduleResult = '445d0db4a8fc5d4acb164d022b', incrementalRank = 35, scoreValue = 20, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = 'cd7f70faed73d2457219b951e714ebe5775515d8';
UPDATE modules SET moduleName ='Session Management Challenge 4', moduleCategory = 'Session Management', moduleCategoryLangPointer = 'session.management', moduleResult = '238a43b12dde07f39d14599a780ae90f87a23e', incrementalRank = 145, scoreValue = 75, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = 'ced925f8357a17cfe3225c6236df0f681b2447c4';
UPDATE modules SET moduleName ='CSRF 4', moduleCategory = 'CSRF', moduleCategoryLangPointer = 'csrf', moduleResult = 'bb78f73c7efefec25e518c3a91d50d789b689c4515b453b6140a2e4e1823d203', incrementalRank = 139, scoreValue = 70, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = 'cfbf7b915ee56508ad46ab79878f37fd9afe0d27';
UPDATE modules SET moduleName ='Cross Site Scripting 2', moduleCategory = 'XSS', moduleCategoryLangPointer = 'xss', moduleResult = '495ab8cc7fe9532c6a75d378de', incrementalRank = 119, scoreValue = 60, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = 'd4e2c37d8f1298fcaf4edcea7292cb76e9eab09b';
UPDATE modules SET moduleName ='NoSQL Injection One', moduleCategory = 'Injection', moduleCategoryLangPointer = 'injection', moduleResult = 'c09f32d4c3dd5b75f04108e5ffc9226cd8840288a62bdaf9dc65828ab6eaf86a', incrementalRank = 89, scoreValue = 45, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = 'd7eaeaa1cc4f218abd86d14eefa183a0f8eb6298';
UPDATE modules SET moduleName ='Insecure Data Storage 2', moduleCategory = 'Mobile Insecure Data Storage', moduleCategoryLangPointer = 'mobile.insecure.data.storage', moduleResult = 'starfish123', incrementalRank = 129, scoreValue = 65, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = 'da3de2e556494a9c2fb7308a98454cf55f3a4911';
UPDATE modules SET moduleName ='SQL Injection Stored Procedure', moduleCategory = 'Injection', moduleCategoryLangPointer = 'injection', moduleResult = 'd9c5757c1c086d02d491cbe46a941ecde5a65d523de36ac1bfed8dd4dd9994c8', incrementalRank = 177, scoreValue = 90, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = 'dc89383763c68cba0aaa1c6f3fd4c17e9d49a805';
UPDATE modules SET moduleName ='Insecure Direct Object Reference Bank', moduleCategory = 'Insecure Direct Object References', moduleCategoryLangPointer = 'insecure.direct.object.references', moduleResult = '4a1df02af317270f844b56edc0c29a09f3dd39faad3e2a23393606769b2dfa35', incrementalRank = 131, scoreValue = 60, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = 'de626470273c01388629e5a56ac6f17e2eef957b';
UPDATE modules SET moduleName ='SQL Injection 4', moduleCategory = 'Injection', moduleCategoryLangPointer = 'injection', moduleResult = 'd316e80045d50bdf8ed49d48f130b4acf4a878c82faef34daff8eb1b98763b6f', incrementalRank = 147, scoreValue = 75, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = 'e0ba96bb4c8d4cd2e1ff0a10a0c82b5362edf998';
UPDATE modules SET moduleName ='Client Side Injection 2', moduleCategory = 'Mobile Injection', moduleCategoryLangPointer = 'mobile.injection', moduleResult = 'BurpingChimneys', incrementalRank = 155, scoreValue = 80, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = 'e635fce334aa61fdaa459c21c286d6332eddcdd3';
UPDATE modules SET moduleName ='Session Management Challenge 5', moduleCategory = 'Session Management', moduleCategoryLangPointer = 'session.management', moduleResult = 'a15b8ea0b8a3374a1dedc326dfbe3dbae26', incrementalRank = 205, scoreValue = 110, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = 'ed732e695b85baca21d80966306a9ab5ec37477f';
UPDATE modules SET moduleName ='Broken Crypto', moduleCategory = 'Mobile Broken Crypto', moduleCategoryLangPointer = 'mobile.broken.crypto', moduleResult = '33edeb397d665ed7d1a580f3148d4b2f', incrementalRank = 97, scoreValue = 50, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = 'ef6496892b8e48ac2f349cdd7c8ecb889fc982af';
UPDATE modules SET moduleName ='Reverse Engineering 2', moduleCategory = 'Mobile Reverse Engineering', moduleCategoryLangPointer = 'mobile.reverse.engineering', moduleResult = 'FireStoneElectric', incrementalRank = 98, scoreValue = 50, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = 'f16bf2ab1c1bf400d36330f91e9ac6045edcd003';
UPDATE modules SET moduleName ='Unintended Data Leakage 1', moduleCategory = 'Mobile Data Leakage', moduleCategoryLangPointer = 'mobile.data.leakage', moduleResult = 'BagsofSalsa', incrementalRank = 132, scoreValue = 60, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = 'f40b0cd5d45327c9426675313f581cf70c7c7c28';
UPDATE modules SET moduleName ='SQL Injection 2', moduleCategory = 'Injection', moduleCategoryLangPointer = 'injection', moduleResult = 'f62abebf5658a6a44c5c9babc7865110c62f5ecd9d0a7052db48c4dbee0200e3', incrementalRank = 88, scoreValue = 45, scoreBonus = 5, hardcodedKey = 1 WHERE moduleId = 'f771a10efb42a79a9dba262fd2be2e44bf40b66d';
UPDATE modules SET moduleName ='CSRF JSON', moduleCategory = 'CSRF', moduleCategoryLangPointer = 'csrf', moduleResult = 'f57f1377bd847a370d42e1410bfe48c9a3484e78d50e83f851b634fe77d41a6e', incrementalRank = 141, scoreValue = 70, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = 'fcc2558e0a23b8420e173cf8029876cb887408d3';
UPDATE modules SET moduleName ='Cross Site Scripting 5', moduleCategory = 'XSS', moduleCategoryLangPointer = 'xss', moduleResult = '7d7cc278c30cca985ab027e9f9e09e2f759e5a3d1f63293', incrementalRank = 166, scoreValue = 85, scoreBonus = 5, hardcodedKey = 0 WHERE moduleId = 'fccf8e4d5372ee5a73af5f862dc810545d19b176';

COMMIT;

-- -----------------------------------------------------
-- Cheat Sheets
-- -----------------------------------------------------
SELECT "Updating Module Cheat Sheets to v3.0" FROM DUAL;
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
CALL cheatSheetCreate('53a53a66cb3bf3e4c665c442425ca90e29536edd', '53a53a66cb3bf3e4c665c442425ca90e29536edd.solution');
CALL cheatSheetCreate('307f78f18fd6a87e50ed6705231a9f24cd582574', '307f78f18fd6a87e50ed6705231a9f24cd582574.solution');
CALL cheatSheetCreate('da3de2e556494a9c2fb7308a98454cf55f3a4911', 'da3de2e556494a9c2fb7308a98454cf55f3a4911.solution');
CALL cheatSheetCreate('335440fef02d19259254ed88293b62f31cccdd41', '335440fef02d19259254ed88293b62f31cccdd41.solution');
CALL cheatSheetCreate('a3f7ffd0f9c3d15564428d4df0b91bd927e4e5e4', 'a3f7ffd0f9c3d15564428d4df0b91bd927e4e5e4.solution');
CALL cheatSheetCreate('e635fce334aa61fdaa459c21c286d6332eddcdd3', 'e635fce334aa61fdaa459c21c286d6332eddcdd3.solution');
CALL cheatSheetCreate('ef6496892b8e48ac2f349cdd7c8ecb889fc982af', 'ef6496892b8e48ac2f349cdd7c8ecb889fc982af.solution');
CALL cheatSheetCreate('3f010a976bcbd6a37fba4a10e4a057acc80bdc09', '3f010a976bcbd6a37fba4a10e4a057acc80bdc09.solution');
CALL cheatSheetCreate('63bc4811a2e72a7c833962e5d47a41251cd90de3', '63bc4811a2e72a7c833962e5d47a41251cd90de3.solution');
CALL cheatSheetCreate('2ab09c0c18470ae5f87d219d019a1f603e66f944', '2ab09c0c18470ae5f87d219d019a1f603e66f944.solution');
CALL cheatSheetCreate('f16bf2ab1c1bf400d36330f91e9ac6045edcd003', 'f16bf2ab1c1bf400d36330f91e9ac6045edcd003.solution');
CALL cheatSheetCreate('9e46e3c8bde42dc16b9131c0547eedbf265e8f16', '9e46e3c8bde42dc16b9131c0547eedbf265e8f16.solution');
CALL cheatSheetCreate('1506f22cd73d14d8a73e0ee32006f35d4f234799', '1506f22cd73d14d8a73e0ee32006f35d4f234799.solution');
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

-- -----------------------------------------------------
-- procedure moduleCreate
-- -----------------------------------------------------
SELECT "Updating Procedures to V3.0" FROM DUAL;
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
-- procedure moduleIncrementalInfo
-- -----------------------------------------------------
DROP PROCEDURE `core`.`moduleIncrementalInfo`;

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleIncrementalInfo` (IN theUserId VARCHAR(64))
BEGIN
(SELECT moduleNameLangPointer, moduleCategory, moduleId, finishTime, incrementalRank FROM modules LEFT JOIN results USING (moduleId) WHERE userId = theUserId AND moduleStatus = 'open') UNION (SELECT moduleNameLangPointer, moduleCategory, moduleId, null, incrementalRank FROM modules WHERE moduleStatus = 'open' AND moduleId NOT IN (SELECT moduleId FROM modules JOIN results USING (moduleId) WHERE userId = theUserId)) ORDER BY incrementalRank;
END

$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure userGetIdByName
-- -----------------------------------------------------
DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userGetIdByName` (IN theUserName VARCHAR(64))
BEGIN
COMMIT;
SELECT userId FROM users
    WHERE userName = theUserName;
END
$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure userClassId
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userClassId` (IN theUserName VARCHAR(64))
BEGIN
COMMIT;
SELECT classId FROM users
    WHERE userName = theUserName;
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
-- procedure userCreate
-- -----------------------------------------------------
DROP PROCEDURE `core`.`userCreate`;
DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userCreate` (IN theClassId VARCHAR(64), IN theUserName VARCHAR(32), IN theUserPass VARCHAR(512), IN theUserRole VARCHAR(32), IN theUserAddress VARCHAR(128), tempPass BOOLEAN)
BEGIN
    DECLARE theId VARCHAR(64);
    DECLARE theClassCount INT;    
    DECLARE theDate DATETIME;

    COMMIT;
    SELECT NOW() FROM DUAL INTO theDate;
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
        SELECT SHA(CONCAT(currVal, tableName, theDate)) FROM sequence
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
-- procedure userPasswordChangeAdmin
-- -----------------------------------------------------
DROP PROCEDURE `core`.`userPasswordChangeAdmin`;
DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userPasswordChangeAdmin` (IN theUserId VARCHAR(64), IN newPassword VARCHAR(512))
BEGIN
DECLARE theDate DATETIME;
COMMIT;
SELECT NOW() FROM DUAL INTO theDate;
UPDATE users SET
    userPass = SHA2(newPassword, 512),
    tempPassword = TRUE
    WHERE userId = theUserId;
COMMIT;
END

$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure moduleGetNameLocale
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`moduleGetNameLocale` (IN theModuleId VARCHAR(64))
BEGIN
COMMIT;
SELECT moduleNameLangPointer, moduleName FROM modules
    WHERE moduleId = theModuleId;
END

$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure userGetIdByName
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userGetIdByName` (IN theUserName VARCHAR(64))
BEGIN
COMMIT;
SELECT userId FROM users
    WHERE userName = theUserName;
END
$$

DELIMITER ;
-- -----------------------------------------------------
-- procedure userClassId
-- -----------------------------------------------------

DELIMITER $$
USE `core`$$
CREATE PROCEDURE `core`.`userClassId` (IN theUserName VARCHAR(64))
BEGIN
COMMIT;
SELECT classId FROM users
    WHERE userName = theUserName;
END
$$

DELIMITER ;