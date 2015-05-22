cd /tmp

sudo apt-get install bash bridge-utils ebtables iproute libev-dev python tcl8.5 tk8.5 libtk-img

export URL=http://downloads.pf.itd.nrl.navy.mil/ospf-manet
wget $URL/quagga-0.99.21mr2.2/quagga-mr_0.99.21mr2.2_amd64.deb
sudo dpkg -i quagga-mr_0.99.21mr2.2_amd64.deb

wget http://downloads.pf.itd.nrl.navy.mil/core/packages/4.7/core-daemon_4.7-0ubuntu1_trusty_amd64.deb
wget http://downloads.pf.itd.nrl.navy.mil/core/packages/4.7/core-gui_4.7-0ubuntu1_trusty_all.deb
#sudo apt-get update
#sudo apt-get install core-network
dkpg -i core-daemon_4.7-0ubuntu1_trusty_amd64.deb
dkpg -i core-gui_4.7-0ubuntu1_trusty_all.deb

wget http://downloads.pf.itd.nrl.navy.mil/emane/0.9.1-r1/emane-0.9.1-release-1.src.tar.gz
tar -xzvf emane-0.9.1-release-1.src.tar.gz
cd emane-0.9.1-release-1/src
tar -xzvf emane-0.9.1.tar.gz
cd emane-0.9.1 

# From website
sudo apt-get install libxml2 libpcap pcre libuuid protobuf python-protobuf python-lxml ace

#Actual
sudo apt-get install libxml2 libpcap0.8 libpcre3 libuuid1 libprotobuf8 python-protobuf python-lxml libace-6.0.3

#Source install
sudo apt-get install libxml2 libxml2-dev libpcap0.8 libpcap-dev libpcre3 libpcre3-dev libuuid1 uuid-dev libprotobuf8 libprotobuf-dev python-protobuf python-lxml libace-6.0.3 libace-dev

sudo apt-get install autoconf automake libtool libxml-simple-perl

aclocal
autoconf
automake --add-missing

tar zxvf emane-X.Y.Z.tar.gz
cd emane-X.Y.Z
./configure && make install
./configure && make deb 


cd emane-X.Y.Z/src/emanesh
make
python setup.py install

sudo apt-get install olsrd

sudo apt-get install pip python-dev

sudo pip install numpy
sudo pip install pandas

sudo apt-get install gnuplot
