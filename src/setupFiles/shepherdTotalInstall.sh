set -e

shepherdServerXmlLocation=https://raw.githubusercontent.com/owasp/SecurityShepherd/master/SecurityShepherdCore/setupFiles/tomcatShepherdSampleServer.xml
shepherdWebXmlLocation=https://raw.githubusercontent.com/owasp/SecurityShepherd/master/SecurityShepherdCore/setupFiles/tomcatShepherdSampleWeb.xml
shepherdManualPackLocation=https://github.com/OWASP/SecurityShepherd/releases/download/v3.0/owaspSecurityShepherd_V3.0.Manual.Pack.zip
if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 1>&2
   exit 1
else
	# Install Pre-Requisite Stuff
	sudo apt-get update -y
	sudo apt-get install -y python-software-properties
	sudo add-apt-repository -y ppa:webupd8team/java
	sudo apt-get update -y
	sudo apt-get install -y oracle-java8-installer
	sudo apt-get install -y tomcat7 tomcat7-admin mysql-server-5.5
	sudo apt-get install -y unzip

	#Download and Deploy Shepherd to Tomcat and MySQL
	sudo wget --quiet $shepherdManualPackLocation -O manualPack.zip
	mkdir manualPack
	unzip manualPack.zip -d manualPack
	cd /home/*
	sudo apt-get install -y dos2unix
	sudo dos2unix manualPack/*.sql
	sudo dos2unix manualPack/*.js
	sudo chmod 775 manualPack/*.war
	cd /var/lib/tomcat7/webapps/
	sudo rm -rf *
	sudo mv -v /home/*/manualPack/ROOT.war ./
	cd /home/*/manualPack/
	echo "Configuring MySQL"
	echo "MySQL Password Please:"
	mysql -u root -e "source coreSchema.sql" --force -p
	echo "MySQL Password Please:"
	mysql -u root -e "source moduleSchemas.sql" --force -p

	#Install and Config MongoDB
	echo "Installing MongoDB"
	sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
	echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' | sudo tee /etc/apt/sources.list.d/mongodb.list
	sudo apt-get update
	sudo apt-get install -y mongodb-org=2.6.9 mongodb-org-server=2.6.9 mongodb-org-shell=2.6.9 mongodb-org-mongos=2.6.9 mongodb-org-tools=2.6.9
	sleep 10
	mongo /home/*/manualPack/mongoSchema.js

	#Configuring Tomcat to Run the way we want (Oracle Java, HTTPs, Port 80 redirect to 443
	echo "Configuring Tomcat"
	sudo echo "JAVA_HOME=/usr/lib/jvm/java-8-oracle" >> /etc/default/tomcat7
	sudo echo "AUTHBIND=yes" >> /etc/default/tomcat7
	cd /home/*
	homeDirectory="$(pwd)/"
	keyStoreFileName="shepherdKeystore.jks"
	echo "Please enter the password you would like to use for your Keystore (Used for HTTPs on Tomcat)"
	keytool -genkey -alias tomcat -keyalg RSA -destkeystore $keyStoreFileName -deststoretype JKS
	touch web.xml
	touch server.xml
	rm web.xml
	rm server.xml
	wget --quiet $shepherdWebXmlLocation -O web.xml
	wget --quiet $shepherdServerXmlLocation -O server.xml
	escapedFileName=$(echo "$homeDirectory$keyStoreFileName" | sed 's/\//\\\//g')
	echo $escapedFileName
	sed -i "s/____.*____/$escapedFileName/g" server.xml
	read -s -p "Please Enter the Keystore Password you used earlier and press [ENTER]" keystorePassword
	echo ""
	sed -i "s/___.*___/$keystorePassword/g" server.xml
	echo "Overwriting default tomcat Config with new config... (Do Not Ignore Any Errors From this point)"
	cat server.xml > /var/lib/tomcat7/conf/server.xml
	cat web.xml > /var/lib/tomcat7/conf/web.xml
	rm server.xml
	rm web.xml
	touch /etc/authbind/byport/80
	touch /etc/authbind/byport/443
	chmod 500 /etc/authbind/byport/80
	chmod 500 /etc/authbind/byport/443
	chown tomcat7 /etc/authbind/byport/80
	chown tomcat7 /etc/authbind/byport/443

	#Restart Tomcat
	sudo service tomcat7 restart
	echo "Shepherd is Ready to Rock!"
fi
