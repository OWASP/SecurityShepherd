 
# OWASP Security Shepherd [![OWASP Flagship](https://img.shields.io/badge/owasp-flagship%20project-48A646.svg)](https://www.owasp.org/index.php/OWASP_Project_Inventory#tab=Flagship_Projects) 
The [OWASP Security Shepherd Project](http://bit.ly/owaspSecurityShepherd) is a web and mobile application security training platform. Security Shepherd has been designed to foster and improve security awareness among a varied skill-set demographic. The aim of this project is to take AppSec novices or experienced engineers and sharpen their penetration testing skill set to security expert status.

[![Build Status](https://travis-ci.com/OWASP/SecurityShepherd.svg?branch=dev)](https://travis-ci.com/OWASP/SecurityShepherd)
  
# Where can I download Security Shepherd?

### Virtual Machine or Manual Setup
You can download Security Shepherd VM's or Manual Installation Packs from [GitHub](https://github.com/OWASP/SecurityShepherd/releases)

### Docker (Ubuntu Linux Host)

#### Initial Setup
```console
# Install pre-reqs
sudo apt install git maven docker docker-compose default-jdk

# Clone the github repository
git clone https://github.com/OWASP/SecurityShepherd.git

# Change directory into the local copy of the repository
cd SecurityShepherd

# Adds current user to the docker group (don't have to run docker with sudo)
sudo gpasswd -a $USER docker

# Run maven to generate the WAR and HTTPS Cert.
mvn -Pdocker clean install -DskipTests

# Build the docker images, docker network and bring up the environment
docker-compose up
```

Open up an Internet Browser & type in the address bar;

* [localhost](http://localhost)

To login use the following credentials (you will be asked to update after login);

* username: ```admin```
* password: ```password```

Note: Environment variables can be configured in dotenv ```.env``` file in the root dir.

#### Full Guide
[Docker-Environment-Setup](https://github.com/OWASP/SecurityShepherd/wiki/Docker-Environment-Setup)

# How do I setup Security Shepherd?
We've got fully automated and step by step walkthroughs on our [wiki page](https://github.com/markdenihan/owaspSecurityShepherd/wiki) to help you get Security Shepherd up and running.
  
# What can Security Shepherd be used for?
Security Shepherd can be used as a;
* Teaching Tool for All Application Security
* Web Application Pen Testing Training Platform
* Mobile Application Pen Testing Training
* Safe Playground to Practise AppSec Techniques
* Platform to demonstrate real Security Risk examples
  
# Why choose Security Shepherd?
There are a lot of purposefully vulnerable applications available in the OWASP Project Inventory, and even more across the internet. Why should you use Security Shepherd? Here are a few reasons;  
* **Wide Topic Coverage**  
Shepherd includes over sixty levels across the entire spectrum of Web and Mobile application security under a single project.
* **Gentle Learning Curve**  
Shepherd is a perfect for users completely new to security with levels increases in difficulty at a pleasant pace.
* **Layman Write Ups**  
When each security concept is first presented in Shepherd, it is done so in layman terms so that anyone (even beginners) can absorb them.
* **Real World Examples**  
The security risks in Shepherd are real vulnerabilities that have had their exploit impact dampened to protect the application, users, and environment. There are no simulated security risks which require an expected, specific attack vector in order to pass a level. Attack vectors when used on Shepherd are how they would behave in the real world.
* **Scalability**  
Shepherd can be used locally by a single user or easily as a server for a high amount of users.
* **Highly Customisable**  
Shepherd enables admins to set what levels are available to their users and in what way they are presentended (Open, CTF and Tournament Layouts)
* **Perfect for Classrooms**  
Shepherd gives it's players user specific solution keys to prevent students from sharing keys, rather than going through the steps required to complete a level.
* **Scoreboard**  
Security Shepherd has a configurable scoreboard to encourage a competitive learning environment. Users that complete levels first, second and third get medals on their scoreboard entry and bonus points to keep things entertaining on the scoreboard.
* **User Management**  
Security Shepherd admins can create users, create admins, suspend, unsuspend, add bonus points, or take penalty points away from user's accounts with the admin user management controls. Admins can also segment their students into specific class groups. Admins can view the progress a class has made to identify struggling participants. An admin can even close public registration and manually create users if they wish for a private experience.
* **Robust Service**  
Shepherd has been used to run online CTFs such as the OWASP Global CTF and OWASP LATAM Tour CTF 2015, both surpassing 200 active users and running with no down time, bar planned maintenance periods.
* **Configurable Feedback**  
An administrator can enable a feedback process, which must be completed by users before a level is marked as complete. This is used both to facilitate project improvements based on feedback submitted and for system administrators to collect "Reports of Understanding" from their students.
* **Granular Logging**  
The logs reported by Security Shepherd are highly detailed and descriptive, but not screen blinding. If a user is misbehaving, you will know.
