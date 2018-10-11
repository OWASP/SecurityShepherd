set -e

shepherdServerXmlLocation=https://raw.githubusercontent.com/owasp/SecurityShepherd/master/src/setupFiles/tomcatShepherdSampleServer.xml
shepherdWebXmlLocation=https://raw.githubusercontent.com/owasp/SecurityShepherd/master/src/setupFiles/tomcatShepherdSampleWeb.xml
shepherdManualPackLocation=https://github.com/OWASP/SecurityShepherd/releases/download/v3.1/owaspSecurityShepherd_V3.1.Manual.Pack.zip
if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 1>&2
   exit 1
else
  # Stop Ubuntu Bionic from complaining about no internet connection on boot
  systemctl disable systemd-networkd-wait-online.service
  systemctl mask systemd-networkd-wait-online.service
	# Install Pre-Requisite Stuff
	sudo add-apt-repository universe #Tomcat8 is here
	sudo add-apt-repository -y ppa:webupd8team/java #Java is here
	sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 9DA31620334BD75D9DCB49F368818C72E52529D4
	echo "deb [ arch=amd64 ] https://repo.mongodb.org/apt/ubuntu bionic/mongodb-org/4.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-4.0.list #mongodb is here
	sudo apt-get update -y
	sudo apt-get upgrade -y
	sudo apt-get install -y oracle-java8-installer tomcat8 tomcat8-admin mysql-server-5.7 mongodb-org unzip

	#Configuring Tomcat to Run the way we want (Oracle Java, HTTPs, Port 80 redirect to 443
	echo "Configuring Tomcat"
	sudo echo "JAVA_HOME=/usr/lib/jvm/java-8-oracle" >> /etc/default/tomcat8
	sudo echo "AUTHBIND=yes" >> /etc/default/tomcat8
  #Have to CHOWN conf / etc/tomcat8 so Tomcat can create DB Auth / DB Prop files there.
  sudo chown tomcat8 /etc/tomcat8
  sudo chown -R tomcat8 /var/lib/tomcat8/conf
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
	cat server.xml > /var/lib/tomcat8/conf/server.xml
	cat web.xml > /var/lib/tomcat8/conf/web.xml
	rm server.xml
	rm web.xml
	touch /etc/authbind/byport/80
	touch /etc/authbind/byport/443
	chmod 500 /etc/authbind/byport/80
	chmod 500 /etc/authbind/byport/443
	chown tomcat8 /etc/authbind/byport/80
	chown tomcat8 /etc/authbind/byport/443

  echo "Configuring MySQL (Blank Pass, just hit return after these two commands)"
	mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'CowSaysMoo';" --force -p
	mysql -u root -e "FLUSH PRIVILEGES;" --force -p

	echo "Configuring MongoDB"
  sudo service mongod start
  systemctl enable mongod.service
	mongo /home/*/manualPack/mongoSchema.js

	#Download and Deploy Shepherd
  echo "Setting Up Shepherd"
	sudo wget --quiet $shepherdManualPackLocation -O manualPack.zip
	mkdir manualPack
	unzip manualPack.zip -d manualPack
	cd /home/*
	sudo dos2unix manualPack/*.js
	sudo chmod 775 manualPack/*.war
	cd /var/lib/tomcat8/webapps/
	sudo rm -rf *
	sudo mv -v /home/*/manualPack/ROOT.war ./
	cd /home/*/manualPack/

	#Restart Tomcat
	sudo service tomcat8 restart
	echo "Shepherd is Ready to Rock!"
fi
