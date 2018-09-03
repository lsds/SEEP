#!/bin/bash
set -o errexit ; set -o nounset
if [[ $HOME == *"root"* ]]
then
	USER_HOME=$1
else
	USER_HOME=$HOME
fi

if [ ! -d "$USER_HOME" ]
then
	echo "Invalid home directory: $USER_HOME"
	echo "Please enter your home directory as the first argument to script"
	exit 1
fi

echo "Using USER_HOME directory of $USER_HOME"
echo "Making directory."
mkdir -p install
cd install

echo "Installing prerequisites."

# First update and install prerequisite packages for CORE
sudo apt-get update
sudo apt-get install git make
sudo apt-get -y install bash bridge-utils ebtables iproute libev-dev python tcl8.5 tk8.5 libtk-img unzip

echo "Installing Core from main repo."
#Install EMANE prerequisites
sudo apt-get -y install libxml2 libprotobuf8 python-protobuf libpcap0.8 libpcre3 \
libuuid1 libace-6.0.3 python-lxml python-setuptools

echo "Downloading quagga, CORE and EMANE."
#Then download:
#QUAGGA
export QUAGGA_URL=http://downloads.pf.itd.nrl.navy.mil/ospf-manet
wget $QUAGGA_URL/quagga-0.99.21mr2.2/quagga-mr_0.99.21mr2.2_amd64.deb

#Core
export CORE_URL=http://downloads.pf.itd.nrl.navy.mil/core/packages
wget "$CORE_URL/4.8/core-daemon_4.8-0ubuntu1_trusty_amd64.deb"
wget "$CORE_URL/4.8/core-gui_4.8-0ubuntu1_trusty_all.deb"

#Emane
export EMANE_URL=http://downloads.pf.itd.nrl.navy.mil/emane
export EMANE_PKG=emane-0.9.2-release-1.ubuntu-14_04.amd64.tar.gz
wget "$EMANE_URL/0.9.2-r1/$EMANE_PKG"
tar -xzvf $EMANE_PKG 

echo "Installing quagga, CORE and EMANE."
sudo dpkg -i quagga-mr_0.99.21mr2.2_amd64.deb

sudo dpkg -i core-daemon_4.8-0ubuntu1_trusty_amd64.deb
sudo dpkg -i core-gui_4.8-0ubuntu1_trusty_all.deb

pushd emane-0.9.2-release-1/debs/ubuntu-14_04/amd64
sudo dpkg -i emane*.deb
popd

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

if [ ! -f /usr/bin/bm ]; then
	echo "Downloading BonnMotion."
	#BonnMotion
	if [ ! -f bonnmotion-2.1.3.zip ]; then
		wget http://sys.cs.uos.de/bonnmotion/src/bonnmotion-2.1.3.zip
	fi
	unzip bonnmotion-2.1.3.zip

	# TODO: Automate bm install.
	pushd bonnmotion-2.1.3
	./install
	# TODO: Requires manual intervention to confirm java location
	sudo ln -s `pwd`/bin/bm /usr/bin/bm
	popd
fi

echo "Installing maven"
#Maven (you'll perhaps need to manually install soot-framework-2.5.0.jar into the local mvn repository
# libs/soot/soot-framework/2.5.0/soot-2.5.0.jar
sudo apt-get -y install maven
pushd ../../../../..
mvn install:install-file -DgroupId=soot -DartifactId=soot-framework -Dversion=2.5.0 -Dpackaging=jar -Dfile=libs/soot/soot-framework/2.5.0/soot-2.5.0.jar
./frontier-bld.sh core
popd

if [ ! -f /usr/bin/nrlolsrd ]; then
echo "Downloading NRL OLSR"
#Nrlolsr (optional)
wget http://downloads.pf.itd.nrl.navy.mil/olsr/nrlolsrdv7.8.1.tgz
tar -xzvf nrlolsrdv7.8.1.tgz
sudo apt-get -y install libpcap-dev
pushd nrlolsr/unix
make -f Makefile.linux
sudo ln -s `pwd`/nrlolsrd /usr/bin/nrlolsrd
popd
fi

#olsrd 0.6.3 from ppa at https://launchpad.net/~guardianproject/+archive/ubuntu/commotion (Don't forget to update before installing!).
echo "Downloading OLSRD"
#export OLSRD_PKG=olsrd-0.9.0.2.tar.gz
#wget "http://www.olsr.org/releases/0.9/$OLSRD_PKG"
#tar -xzvf $OLSRD_PKG  

#TODO: Install OLSRD
sudo apt-get -y install bison flex
#pushd olsrd-0.9.0.2
#make
#sudo make install
#popd

#git clone http://olsr.org/git/olsrd.git 
wget http://www.olsr.org/releases/0.9/olsrd-0.9.0.3.tar.gz
tar -xzvf olsrd-0.9.0.3.tar.gz 
pushd olsrd-0.9.0.3
make
sudo make install
pushd lib/txtinfo
make
sudo make install
popd
popd

#sudo apt-get install olsrd (installs 0.6.6 on trusty)

#Update olsrd config.
sudo cp /etc/olsrd/olsrd.conf /etc/olsrd/olsrd.conf.orig
sudo cp ../vldb/config/olsrd.conf.default.full.txt /etc/olsrd/olsrd.conf


pushd ..
#Then apply diff to core.
sudo cp vldb/config/core4.8_session.py /usr/lib/python2.7/dist-packages/core/session.py
sudo cp vldb/config/core4.8_mobility.py /usr/lib/python2.7/dist-packages/core/mobility.py

#Then apply diff to emane?
#TODO: Don't think I need to do anything for 9.2.

#Then need to update /etc/hosts.
sudo cp /etc/hosts /etc/hosts.orig
sudo bash -c "cat vldb/config/etc-hosts-additions >> /etc/hosts"

#Then need to update core config (e.g. for control net).
sudo cp /etc/core/core.conf /etc/core/core.conf.orig
sudo cp vldb/config/core4.8.conf.orig /etc/core/core.conf

#Then need to point core config to acita config dir.
ln -s `pwd`/vldb $USER_HOME/.core
mkdir -p $USER_HOME/tmp

# Install tun_flowctl
#popd
#git clone https://github.com/adjacentlink/tun_flowctl.git

#pushd tun_flowctl
#cp -r 3.13.0-35-generic `uname -r`
#make
#make install
#popd
#popd

#popd

echo "Installing pip + python packages."
#For 14.04 can just install olsrd from apt-get directly.
sudo apt-get -y install python-pip
sudo apt-get -y install python-dev
sudo apt-get -y install libpng12-dev libfreetype6-dev pkg-config

#python pandas
sudo pip install pandas

#python matplotlib
sudo pip install matplotlib

#python utm
sudo pip install utm

sudo pip install networkx

sudo apt-get -y install gnuplot

#Need to install a more recent libstdc++ for face recognition query
sudo apt-get -y install software-properties-common python-software-properties
sudo add-apt-repository ppa:ubuntu-toolchain-r/test
sudo apt-get update
sudo apt-get -y install gcc g++
#sudo apt-get install libstdc++6

#For facerec query.
sudo apt-get -y install libv4l-dev
pushd ../resources/training
mkdir -p chokepoint
cd chokepoint
#http://arma.sourceforge.net/chokepoint/ 
#wget http://filestore.nicta.com.au/Comms/OpenNICTA/data/crop/P2E.tar.xz
https://zenodo.org/record/815657/files/P2E.tar.xz
tar xf P2E.tar.xz
popd

#pushd /tmp
#wget http://filestore.nicta.com.au/Comms/OpenNICTA/data/orig/P2E_S1.tar.xz
#tar xf P2E_S1.tar.xz
#tar xf P2E_S1_C3.tar.xz
#tar xf P2E_S1_C3.1.tar.xz
#popd
#pushd ../resources/images
#cp /tmp/P2E_S1_C3.1/000021*.jpg .
#rm *.pgm
#popd

pushd ../../../..
sudo chown -R $USER:$USER .
popd
