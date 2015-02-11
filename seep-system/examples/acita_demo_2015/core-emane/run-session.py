#!/usr/bin/python

import sys,os,time,re
from core import pycore
from core.mobility import BasicRangeModel


svc_dir='/data/dev/seep-github/seep-system/examples/acita_demo_2015/core-emane/vldb/myservices'

#hook 5:datacollect_hook.sh {
datacollect_hookdata = '''#!/bin/bash
# session hook script; write commands here to execute on the host at the
# specified state

echo "`hostname`:`pwd`" > /tmp/datacollect.log
if [ -z "$SEEP_GITHUB_DIR" ]; then
	echo "SEEP_GITHUB_DIR not set." >> /tmp/datacollect.log
	SEEP_GITHUB_DIR=/data/dev/seep-github
fi

expDir=$(pwd)
scriptDir=$SEEP_GITHUB_DIR/seep-system/examples/acita_demo_2015/core-emane
timeStr=$(date +%H-%M-%S-%a%d%m%y)
resultsDir=$scriptDir/log/$timeStr

echo $expDir >> /tmp/datacollect.log
echo $scriptDir >> /tmp/datacollect.log
echo $timeStr >> /tmp/datacollect.log
echo $resultsDir >> /tmp/datacollect.log

mkdir -p $resultsDir

# Copy all log files to results dir
for d in n*.conf 
do
	cp $d/log/*.log $resultsDir	
done
	

cd $scriptDir
./gen_core_results.py --expTimeStr $timeStr 
cd $expDir
'''
def main():

    try:
        session = pycore.Session(cfg={'custom_services_dir':svc_dir,'preservedir':'1'}, persistent=True)
        """
        if not add_to_server(session): 
            print 'Could not add to server'
            return
        """
        #prefix = ipaddr.IPv4Prefix("10.0.0.0/32")
        #tmp.newnetif(net, ["%s/%s" % (prefix.addr(i), prefix.prefixlen)])
        # set increasing Z coordinates
        wlan1 = session.addobj(cls = pycore.nodes.WlanNode, name="wlan1",objid=1,
                verbose=True)
        wlan1.setmodel(BasicRangeModel, BasicRangeModel.getdefaultvalues())
        wlan1.setposition(x=418.0,y=258.0)

        services_str = "OLSR|IPForward"

        workers = []
        for i in range(2,8):
            pos = gen_position(i)
            workers.append(create_node(i, session, "%s|MeanderWorker"%services_str, wlan1, pos)) 
        
        master = create_node(8, session, "%s|MeanderMaster"%services_str, wlan1, gen_position(8))

        session.sethook("hook:5","datacollect.sh",None,datacollect_hookdata)
        print 'Instantiating session.'
        session.instantiate()

        print 'Waiting for a meander worker/master to terminate'
        watch_meander_services(session.sessiondir, map(lambda n: "n%d"%n, range(2,9)))
        #time.sleep(30)
        print 'Collecting data'
        session.datacollect()
        time.sleep(5)
        print 'Shutting down'

    finally:
        print 'Shutting down session.'
        if session:
            session.shutdown()

def create_node(i, session, services_str, wlan, pos, ip_offset=8):
    n = session.addobj(cls = pycore.nodes.CoreNode, name="n%d"%i)
    session.services.addservicestonode(n, "", services_str, verbose=False)
    n.setposition(x=pos[0], y=pos[1])
    n.newnetif(net=wlan, addrlist=["10.0.0.%d/32"%(i+ip_offset)], ifindex=0)

default_positions = {2 : (346.0,178.0),3:(515.0,160.0),4:(567.0,244.0),5:(558.0,317.0),6:(458.0,392.0),7:(349.0,359.0),8:(295.0,290.0)}
def gen_position(i):
    """TODO: Have different initial placement models etc."""
    return default_positions[i] 

def add_to_server(session):
    global server
    try:
        server.addsession(session)
        return True
    except NameError:
        print 'Name error'
        return False

def watch_meander_services(sessiondir, node_names):
    while True:
        for name in node_names:
            if os.path.exists("%s/%s.conf/worker.shutdown"%(sessiondir, name)) or os.path.exists("%s/%s.conf/master.shutdown"%(sessiondir, name)):
                print 'Shutdown file exists for node %s - exiting'%name
                return

        time.sleep(0.5)


if __name__ == "__main__" or __name__ == "__builtin__":
    main()
