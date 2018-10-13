#!/usr/bin/env bash

core="docker/mysql/coreSchema.sql"
module="docker/mysql/moduleSchemas.sql"

sed -i 's/-- DELIMITER \$\$/DELIMITER \$\$/g' ${core}
sed -i 's/-- DELIMITER \$\$/DELIMITER \$\$/g' ${module}

sed -i 's/-- \$\$/\$\$/g' ${core}
sed -i 's/-- \$\$/\$\$/g' ${module}

sed -i 's/-- DELIMITER \;/DELIMITER \;/g' ${core}
sed -i 's/-- DELIMITER \;/DELIMITER \;/g' ${module}
