#!/usr/bin/env bash

core="docker/mysql/target/coreSchema.sql"
module="docker/mysql/target/moduleSchemas.sql"

sed -i'' -e 's/-- DELIMITER \$\$/DELIMITER \$\$/g' ${core}
sed -i'' -e 's/-- DELIMITER \$\$/DELIMITER \$\$/g' ${module}

sed -i'' -e 's/-- \$\$/\$\$/g' ${core}
sed -i'' -e 's/-- \$\$/\$\$/g' ${module}

sed -i'' -e 's/-- DELIMITER \;/DELIMITER \;/g' ${core}
sed -i'' -e 's/-- DELIMITER \;/DELIMITER \;/g' ${module}
