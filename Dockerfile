FROM tomcat:alpine

RUN printf "databaseConnectionURL=jdbc:mysql://secshep_mysql:3306/\nDriverType=org.gjt.mm.mysql.Driver\ndatabaseSchema=core\ndatabaseUsername=root\ndatabasePassword=CowSaysMoo\n" >> /usr/local/tomcat/conf/database.properties
