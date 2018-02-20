service tomcat7 start &
#Fix for MySQL BUG in Overlay2
find /var/lib/mysql -type f -exec touch {} \; && service mysql start &
/usr/bin/mongod 