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
sudo apt-get -y install bash bridge-utils ebtables iproute libev-dev python tcl tk tcl8.5 tk8.5 libtk-img

# Install EMANE prerequisites
sudo apt-get -y install libxml2 libprotobuf7 python-protobuf libpcap0.8 libpcre3 \
	libuuid1 libace-6.0.1 python-lxml python-setuptools

echo "Downloading quagga, CORE and EMANE."
#Then download:
#QUAGGA
export QUAGGA_URL=http://downloads.pf.itd.nrl.navy.mil/ospf-manet
wget $QUAGGA_URL/quagga-0.99.21mr2.2/quagga-mr_0.99.21mr2.2_amd64.deb

#Core
export CORE_URL=http://downloads.pf.itd.nrl.navy.mil/core/packages
wget "$CORE_URL/4.8/core-daemon_4.8-0ubuntu1_precise_amd64.deb"
wget "$CORE_URL/4.8/core-gui_4.8-0ubuntu1_precise_all.deb"

#Emane
export EMANE_URL=http://downloads.pf.itd.nrl.navy.mil/emane
export EMANE_PKG=emane-0.9.2-release-1.ubuntu-12_04.amd64.tar.gz
wget "$EMANE_URL/0.9.2-r1/$EMANE_PKG"
tar -xzvf $EMANE_PKG 

echo "Installing quagga, CORE and EMANE."
sudo dpkg -i quagga-mr_0.99.21mr2.2_amd64.deb

sudo dpkg -i core-daemon_4.8-0ubuntu1_precise_amd64.deb
sudo dpkg -i core-gui_4.8-0ubuntu1_precise_all.deb

pushd emane-0.9.2-release-1/debs/ubuntu-12_04/amd64
sudo dpkg -i emane*.deb
popd

#N.B. You need to install Oracle Java 1.7 manually first!
#TODO: Automate java install


echo "Downloading BonnMotion."
#BonnMotion
wget http://sys.cs.uos.de/bonnmotion/src/bonnmotion-2.1.3.zip
unzip bonnmotion-2.1.3.zip

# TODO: Automate bm install.
#pushd bonnmotion-2.1.3
#./install
# TODO: Requires manual intervention to confirm java location
#sudo ln -s `pwd`/bin/bm /usr/bin/bm
#popd

echo "Installing maven"
#Maven (you'll perhaps need to manually install soot-framework-2.5.0.jar into the local mvn repository
# libs/soot/soot-framework/2.5.0/soot-2.5.0.jar
sudo apt-get -y install maven

pushd ../../../../..
mvn install:install-file -DgroupId=soot -DartifactId=soot-framework -Dversion=2.5.0 -Dpackaging=jar -Dfile=libs/soot/soot-framework/2.5.0/soot-2.5.0.jar
./frontier-bld.sh core
popd

#Needed by javacv
sudo apt-get install libv4l-0

echo "Downloading NRL OLSR"
#Nrlolsr (optional)
wget http://downloads.pf.itd.nrl.navy.mil/olsr/nrlolsrdv7.8.1.tgz

tar -xzvf nrlolsrdv7.8.1.tgz
sudo apt-get -y install libpcap-dev
pushd nrlolsr/unix
make -f Makefile.linux
sudo ln -s `pwd`/nrlolsrd /usr/bin/nrlolsrd
popd

#olsrd 0.6.3 from ppa at https://launchpad.net/~guardianproject/+archive/ubuntu/commotion (Don't forget to update before installing!).
#echo "Downloading OLSRD"
#export OLSRD_PKG=olsrd-0.9.0.2.tar.gz
#wget "http://www.olsr.org/releases/0.9/$OLSRD_PKG"
#tar -xzvf $OLSRD_PKG  

#TODO: Install OLSRD
#(to download and install from source repo)
#git clone http://olsr.org/git/olsrd.git  
sudo apt-get -y install bison flex

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

#Update olsrd config.
sudo cp /etc/olsrd/olsrd.conf /etc/olsrd/olsrd.conf.orig
sudo cp ../vldb/config/olsrd.conf.default.full.txt /etc/olsrd/olsrd.conf
echo "Installing pip + python packages."

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

sudo apt-get -y install gnuplot

#For 14.04 can just install olsrd from apt-get directly.
sudo apt-get -y install python-pip
sudo apt-get -y install python-dev
sudo apt-get -y install libpng12-dev libfreetype6-dev pkg-config

#python pandas
sudo pip install pandas

#python matplotlib
#sudo pip install matplotlib
sudo apt-get -y install python-matplotlib

#python utm
sudo pip install utm

sudo pip install networkx

pushd ../../../..
sudo chown -R $USER:$USER .
popd
