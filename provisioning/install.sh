#!/bin/bash -x

# pre
sudo apt-get update -y
sudo apt-get install -y maven
sudo apt-get install -y openjdk-8-jdk

# install docker
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo apt-key fingerprint 0EBFCD88
sudo add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
   $(lsb_release -cs) \
   stable"
sudo apt-get -y update
sudo apt-get -y install docker-ce

# install docker-compose
sudo curl -L "https://github.com/docker/compose/releases/download/1.22.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
sudo gpasswd -a $USER docker

# Download Securityshephard 
cd /home/vagrant/
git clone https://github.com/OWASP/SecurityShepherd.git
cd SecurityShepherd
 
# Run maven and build docker images 
mvn -Pdocker clean install -DskipTests
sudo docker-compose up


