#!/bin/bash
set -o errexit ; set -o nounset
sudo apt-get update
sudo apt-get -y install maven

mvn install:install-file -DgroupId=soot -DartifactId=soot-framework -Dversion=2.5.0 -Dpackaging=jar -Dfile=libs/soot/soot-framework/2.5.0/soot-2.5.0.jar

#N.B. You need to install Oracle Java 1.7 manually first!
#TODO: Automate java install
#if [ ! -f jdk-7u79-linux-x64.tar.gz ]; then
#    echo "JDK not found"
#    tar -xzvf jdk-7u79-linux-x64.tar.gz 
#    sudo cp -r jdk1.7.0_79 /usr/lib/jvm
#    sudo update-alternatives --install "/usr/bin/java" "java" "/usr/lib/jvm/jdk1.7.0_79/bin/java" 1
#    sudo update-alternatives --install "/usr/bin/javac" "javac" "/usr/lib/jvm/jdk1.7.0_79/bin/javac" 1
#    sudo update-alternatives --install "/usr/bin/jar" "jar" "/usr/lib/jvm/jdk1.7.0_79/bin/jar" 1
## then manually do:
##    sudo update-alternatives --config java
##  and select the oracle jdk from the list.
#    echo "Installed JDK, manual configuration of alternatives required, exiting."
#    exit 1
#fi

#sudo apt-get -y install bison flex

mkdir -p install
cd install

#wget http://www.olsr.org/releases/0.9/olsrd-0.9.0.3.tar.gz
#tar -xzvf olsrd-0.9.0.3.tar.gz 
#pushd olsrd-0.9.0.3
#make
#sudo make install
#pushd lib/txtinfo
#make
#sudo make install
#popd
#popd
cd ..

# OR
#For 14.04 can just install olsrd from apt-get directly (installs 0.6.6 on trusty, 0.6.2 on pi)
sudo apt-get install olsrd 

#Update olsrd config.
sudo cp /etc/olsrd/olsrd.conf /etc/olsrd/olsrd.conf.orig
sudo cp ../vldb/config/olsrd.conf.default.full.txt /etc/olsrd/olsrd.conf

echo "Installing pip + python packages."
sudo apt-get -y install python-pip
sudo apt-get -y install python-dev
sudo apt-get -y install libpng12-dev libfreetype6-dev pkg-config

#python pandas
sudo pip install pandas

#python matplotlib
sudo pip install matplotlib

#python utm
#sudo pip install utm

#sudo pip install networkx

sudo apt-get -y install gnuplot

#Need to install a more recent libstdc++ for face recognition query
#sudo apt-get -y install software-properties-common python-software-properties
#sudo add-apt-repository ppa:ubuntu-toolchain-r/test
#sudo apt-get update
#sudo apt-get -y install gcc g++
##sudo apt-get install libstdc++6

#For facerec query.
sudo apt-get -y install libv4l-dev
pushd ../resources/training
mkdir -p chokepoint
cd chokepoint
wget http://filestore.nicta.com.au/Comms/OpenNICTA/data/crop/P2E.tar.xz
tar xf P2E.tar.xz
popd

pushd ../../../..
sudo chown -R $USER:$USER .
popd
