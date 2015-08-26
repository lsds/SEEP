#!/bin/bash

su dokeeffe

mkdir install
cd install

echo "Installing prerequisites."

# First update and install prerequisite packages for CORE
sudo apt-get update
sudo apt-get install bash bridge-utils ebtables iproute libev-dev python tcl8.5 tk8.5 libtk-img

# Install EMANE prerequisites
sudo apt-get install libxml2 libprotobuf7 python-protobuf libpcap0.8 libpcre3 \
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

# TODO: Automate bm install.


echo "Installing maven"
#Maven (you'll perhaps need to manually install soot-framework-2.5.0.jar into the local mvn repository
# libs/soot/soot-framework/2.5.0/soot-2.5.0.jar
sudo apt-get install maven

#TODO: using the suggested mvn install ... when mvn clean compile assembly:single fails).

echo "Downloading NRL OLSR"
#Nrlolsr (optional)
wget http://downloads.pf.itd.nrl.navy.mil/olsr/nrlolsrdv7.8.1.tgz

#TODO: Install nrlolsr

#olsrd 0.6.3 from ppa at https://launchpad.net/~guardianproject/+archive/ubuntu/commotion (Don't forget to update before installing!).
echo "Downloading OLSRD"
export OLSRD_PKG=olsrd-0.9.0.2.tar.gz
wget "http://www.olsr.org/releases/0.9/$OLSRD_PKG"
tar -xzvf $OLSRD_PKG  

#TODO: Install OLSRD

echo "Installing pip + python packages."
#For 14.04 can just install olsrd from apt-get directly.
sudo apt-get install python-pip

#python pandas
sudo pip install pandas

#python matplotlib
sudo pip install matplotlib

#python utm
sudo pip install utm

#Then apply diff to core.
#Then apply diff to emane?
#Then update olsrd config?
#Then need to point core config to acita config dir.
#Then need to update /etc/hosts.
#Then need to update core config (e.g. for control net).
