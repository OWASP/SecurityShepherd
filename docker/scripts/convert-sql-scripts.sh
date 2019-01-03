#!/usr/bin/env bash

core="docker/mysql/coreSchema.sql"
module="docker/mysql/moduleSchemas.sql"

sed -i .bak 's/-- DELIMITER \$\$/DELIMITER \$\$/g' ${core}
sed -i .bak 's/-- DELIMITER \$\$/DELIMITER \$\$/g' ${module}

sed -i .bak 's/-- \$\$/\$\$/g' ${core}
sed -i .bak 's/-- \$\$/\$\$/g' ${module}

sed -i .bak 's/-- DELIMITER \;/DELIMITER \;/g' ${core}
sed -i .bak 's/-- DELIMITER \;/DELIMITER \;/g' ${module}
