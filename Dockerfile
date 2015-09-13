###################################################################
# Dockerfile to build Security Sherpherd
#
# Based on Ubuntu
# Version 0.8
###################################################################


FROM ubuntu:precise
ENV DEBIAN_FRONTEND noninteractive

MAINTAINER Paul <@ismisepaul>

#Change these Passwords
ENV keystorePwd=CowSaysMoo mysqlRootPwd=CowSaysMoo
 
#Other Environment Variables
ENV homeDirectory="/home/shepherd/" keyStoreFileName="shepherdKeystore.jks"

#Download locations
ENV shepherdServerXmlLocation="https://github.com/OWASP/SecurityShepherd/SecurityShepherdCore/setupFiles/tomcatShepherdSampleServer.xml" shepherdWebXmlLocation="https://github.com/OWASP/SecurityShepherd/SecurityShepherdCore/setupFiles/tomcatShepherdSampleWeb.xml" shepherdManualPackLocation="http://sourceforge.net/projects/owaspshepherd/files/owaspSecurityShepherd_V2.3%20Manual%20Pack.zip/download"

# Install Pre-Requisite Stuff
RUN apt-get update -y &&\
	apt-get install -y software-properties-common python-software-properties &&\
	add-apt-repository -y ppa:webupd8team/java &&\ 
	apt-get update -y &&\ 
	echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections &&\
	apt-get install -y oracle-java7-installer --force-yes &&\
	echo "mysql-server mysql-server/root_password password $mysqlRootPwd" | debconf-set-selections &&\
	echo "mysql-server mysql-server/root_password_again password $mysqlRootPwd" | debconf-set-selections &&\
	apt-get install -y tomcat7 tomcat7-common tomcat7-admin mysql-server-5.5 authbind unzip tofrodos wget less vim &&\
	mkdir $homeDirectory

#Download and Deploy Shepherd to Tomcat and MySQL
WORKDIR /home/shepherd
RUN wget --quiet $shepherdManualPackLocation -O manualPack.zip &&\
	mkdir manualPack &&\
	unzip manualPack.zip -d manualPack &&\
	fromdos manualPack/*.sql &&\
	chmod 775 manualPack/*.war &&\
	rm -rf /var/lib/tomcat7/webapps/* &&\
	mv manualPack/ROOT.war /var/lib/tomcat7/webapps/ &&\
	chown -R mysql /var/lib/mysql

#Configuring MySQL
WORKDIR /home/shepherd/manualPack
RUN /bin/bash -c "/usr/bin/mysqld_safe &" && \
	sleep 5 &&\
	mysql -u root -e "source coreSchema.sql" --force -p$mysqlRootPwd &&\
	mysql -u root -e "source moduleSchemas.sql" --force -p$mysqlRootPwd

#Configuring Tomcat
WORKDIR /home/shepherd
RUN echo "JAVA_HOME=/usr/lib/jvm/java-7-oracle" >> /etc/default/tomcat7 && \
	echo "AUTHBIND=yes" >> /etc/default/tomcat7 && \
	keytool -genkey -alias tomcat -keyalg RSA -keystore $keyStoreFileName -dname "cn=OwaspShepherd, ou=Security Shepherd, o=OWASP, L=Baile Átha Cliath, ST=Laighin, C=IE" -storepass $keystorePwd -keypass $keystorePwd -deststoretype JKS && \
	cd /var/lib/tomcat7/conf/ && \
	rm -f web.xml && \
	rm -f server.xml &&\
	wget --quiet $shepherdWebXmlLocation -O web.xml && \
	wget --quiet $shepherdServerXmlLocation  -O server.xml && \
	escapedFileName=$(echo "$homeDirectory$keyStoreFileName" | sed 's/\//\\\//g') && \
	sed -i "s/____.*____/$escapedFileName/g" server.xml && \
	sed -i "s/___.*___/$keystorePwd/g" server.xml && \
	touch /etc/authbind/byport/80 && \
	touch /etc/authbind/byport/443 && \
	chmod 500 /etc/authbind/byport/80 && \
	chmod 500 /etc/authbind/byport/443 && \
	chown tomcat7 /etc/authbind/byport/80 && \
	chown tomcat7 /etc/authbind/byport/443

EXPOSE 80 443 3306

#service mysql start not working...

CMD /usr/bin/mysqld_safe & && \
	service tomcat7 start && tail -f /var/lib/tomcat7/logs/catalina.out

