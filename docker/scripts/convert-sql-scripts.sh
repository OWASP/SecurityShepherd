#!/usr/bin/env bash

core="docker/mariadb/target/coreSchema.sql"
module="docker/mariadb/target/moduleSchemas.sql"

sed -i'' -e 's/-- DELIMITER \$\$/DELIMITER \$\$/g' ${core}
sed -i'' -e 's/-- DELIMITER \$\$/DELIMITER \$\$/g' ${module}

sed -i'' -e 's/-- \$\$/\$\$/g' ${core}
sed -i'' -e 's/-- \$\$/\$\$/g' ${module}

sed -i'' -e 's/-- DELIMITER \;/DELIMITER \;/g' ${core}
sed -i'' -e 's/-- DELIMITER \;/DELIMITER \;/g' ${module}
