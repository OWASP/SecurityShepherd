-- Upgrades an existing Security Shepherd 3.1 database to the consolidated mobile module list.
-- Safe to run more than once: every statement is a DELETE or UPDATE keyed on a fixed module ID.
use core;

-- Mobile Insecure Data Storage 2 and 3, and Mobile Poor Authentication 2, no longer exist.
-- Their rows go from the dependent tables first, then the modules themselves.
DELETE FROM medals WHERE moduleId IN (
  'da3de2e556494a9c2fb7308a98454cf55f3a4911',
  'cb7d696bdf88899e8077063d911fc8da14176702',
  '064e28ea4b2f7708b8cb4495d9db1a5e05decdb8');
DELETE FROM results WHERE moduleId IN (
  'da3de2e556494a9c2fb7308a98454cf55f3a4911',
  'cb7d696bdf88899e8077063d911fc8da14176702',
  '064e28ea4b2f7708b8cb4495d9db1a5e05decdb8');
DELETE FROM cheatsheet WHERE moduleId IN (
  'da3de2e556494a9c2fb7308a98454cf55f3a4911',
  'cb7d696bdf88899e8077063d911fc8da14176702',
  '064e28ea4b2f7708b8cb4495d9db1a5e05decdb8');
DELETE FROM modules WHERE moduleId IN (
  'da3de2e556494a9c2fb7308a98454cf55f3a4911',
  'cb7d696bdf88899e8077063d911fc8da14176702',
  '064e28ea4b2f7708b8cb4495d9db1a5e05decdb8');

-- The remaining mobile modules gain a "Mobile" prefix.
UPDATE modules SET moduleName = 'Mobile Insecure Data Storage' WHERE moduleId = '53a53a66cb3bf3e4c665c442425ca90e29536edd';
UPDATE modules SET moduleName = 'Mobile Client Side Injection' WHERE moduleId = '335440fef02d19259254ed88293b62f31cccdd41';
UPDATE modules SET moduleName = 'Mobile Poor Authentication' WHERE moduleId = '0cdd1549e7c74084d7059ce748b93ef657b44457';
UPDATE modules SET moduleName = 'Mobile Insecure Data Storage 1' WHERE moduleId = '307f78f18fd6a87e50ed6705231a9f24cd582574';
UPDATE modules SET moduleName = 'Mobile Client Side Injection 1' WHERE moduleId = 'a3f7ffd0f9c3d15564428d4df0b91bd927e4e5e4';
UPDATE modules SET moduleName = 'Mobile Client Side Injection 2' WHERE moduleId = 'e635fce334aa61fdaa459c21c286d6332eddcdd3';
UPDATE modules SET moduleName = 'Mobile Poor Authentication 1' WHERE moduleId = 'ba6e65e4881c8499b5e53eb33b5be6b5d0f1fb2c';