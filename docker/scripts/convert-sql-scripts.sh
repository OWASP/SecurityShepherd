#!/usr/bin/env bash

core="docker/mysql/coreSchema.sql"
module="docker/mysql/moduleSchemas.sql"

sed -i'' -e 's/-- DELIMITER \$\$/DELIMITER \$\$/g' ${core}
sed -i'' -e 's/-- DELIMITER \$\$/DELIMITER \$\$/g' ${module}

sed -i'' -e 's/-- \$\$/\$\$/g' ${core}
sed -i'' -e 's/-- \$\$/\$\$/g' ${module}

sed -i'' -e 's/-- DELIMITER \;/DELIMITER \;/g' ${core}
sed -i'' -e 's/-- DELIMITER \;/DELIMITER \;/g' ${module}
