Steps to set up VM network interfaces POST VM REBOOT:
-------------------------------------

(i) You need to recreate the virtual interfaces used to connect each CORE node
to the VM's host only network interfaces. To do this, simply execute the veth-setup.sh script in DEMO_CONFIG_DIR=/home/acita14demo/dev/seep-ita/seep-system/examples/acita_demo_2015/core-emane/config):
	> sudo ./veth-setup.sh
To check it has worked, execute ifconfig -a and you should see 6 virtual ethernet interface pairs (veth<id>.host & veth<id>.core, for <id> in 1-6).

(ii) Next you need to create the policy routing tables and rules that ensure network traffic flows from each emulator into the corresponding CORE node, over the CORE MANET and back out to the destination emulator. The policy-routes-create.sh does this for you:
	> sudo ./policy-routes-create.sh
You can inspect the policy tables and routes created using standard linux commands (e.g. ip rules/ip route).

Starting CORE
-------------
(i) To start CORE, just type:
	> core-gui
(ii) Next, open the session configuration at $DEMON_CONFIG_DIR/scale-demo-numbered.imn. Start the session by pressing the play button.
(iii) Next, start the link cost server that the demo application workers will use to retrieve the current network state in order to make network-aware routing decisiions.
	> ./run_link_state_server.sh
Once the CORE nodes build a routing table, you should see the server periodically print the current link costs to stdout.

(iv) In addition, you'll need to start another server that receives notifications about operator deployment and application level routing decisions and updates the CORE GUI:
	> ./run_link_painter.sh

(Re-)Starting the Android emulators
-----------------------------------
(i) You need to start the Genymotion emulators through the genymotion player, and *NOT* directly through Virtualbox. To run the Genymotion player, execute:

	> genymotion
There should be 6 worker emulators. The master application is installed on Worker 1. Start each emulator by clicking on the play button beside it in the Genymotion player. The emulator startup is a bit flaky and will sometimes crash. If it crashes you'll need to keep retrying until it succeeds. 

(ii) After the emulator starts, *BEFORE* running the SEEP applications for the first time you need to first set up an appropriate routing gateway for the emulator to be able to communicate with the independent host only networks of the other emulators through CORE. To do this, you should open up the 'Terminal' application on the emulator desktop. Then for worker <i>, where <i> is in 1-6, type the following at the prompt:
	> su
	> cd /sdcard/Download
	> sh worker<i>-android-routes.sh

To check the routes are set up properly, you can execute "ip route". The output should show a default gateway to the virtual networks of each of the other 5 workers. You should also be able to ping the other emulators once they have been started.

Running the Demo
-----------------
(i) The master/worker application can be started by clicking on the SEEP icon on the emulator desktop. You should always start the master first, then start the appropriate number of worker applications for the deployment scenario (local=No other workers, remote=2 more, scaled=4 more).

(ii) Having chosen your deployment scenario on the master using the appropriate button, click the join button for each of the workers needed. Once they have joined, click deploy on the master. When deployment completes (as indicated by a popup on the master), click start.

(iii) When the demo completes, you'll need to completely kill and restart each of the master and worker applications in order to rerun it.
