# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  
  config.vm.box = "ubuntu/xenial64"
  
  config.vm.network "public_network", ip: "192.168.1.10", host_ip: "127.0.0.1"
  
  config.vm.provision "shell", path: "provisioning/install.sh"
  
end
