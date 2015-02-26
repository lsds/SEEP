#!/usr/bin/python

import sys,os,time,re,argparse
from core import pycore
from core.constants import *
from core.mobility import BasicRangeModel
from core.mobility import Ns2ScriptedMobility 
from core.emane.ieee80211abg import EmaneIeee80211abgModel


svc_dir='/data/dev/seep-github/seep-system/examples/acita_demo_2015/core-emane/vldb/myservices'
conf_dir='/data/dev/seep-github/seep-system/examples/acita_demo_2015/core-emane/vldb/config'
mobility_params = [('file','%s/rwpt.ns_movements'%conf_dir),('refresh_ms',500),
        ('loop',1),('autostart',5.0),('map',''),('script_start',''),('script_pause',''),('script_stop','')]

datacollect_template = '''#!/bin/bash
# session hook script; write commands here to execute on the host at the
# specified state


#echo "`hostname`:`pwd`" > /tmp/datacollect.log
#if [ -z "$SEEP_GITHUB_DIR" ]; then
#	echo "SEEP_GITHUB_DIR not set." >> /tmp/datacollect.log
#	SEEP_GITHUB_DIR=/data/dev/seep-github
#fi

#scriptDir=$SEEP_GITHUB_DIR/seep-system/examples/acita_demo_2015/core-emane
scriptDir=%s
timeStr=%s
k=%dk
mob=%.2fm
session=%ds
#resultsDir=$scriptDir/log/$timeStr
resultsDir=$scriptDir/log/$timeStr/$k/$mob/$session

expDir=$(pwd)

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
#./gen_core_results.py --expDir log/$timeStr 
./gen_core_results.py --expDir $resultsDir
cd $expDir
'''
def run_sessions(time_str, k, mob, sessions):
    for session in range(0, sessions):
        print '*** Running session %d ***'%session
        run_session(time_str, k, mob, session)

def run_session(time_str, k, mob, exp_session, model=None):
    try:
        session = pycore.Session(cfg={'custom_services_dir':svc_dir, 'preservedir':'1', 'controlnet': "172.16.0.0/24"}, persistent=True)
        #session = pycore.Session(cfg={'custom_services_dir':svc_dir}, persistent=True)
        write_replication_factor(k, session.sessiondir)
        """
        if not add_to_server(session): 
            print 'Could not add to server'
            return
        """
        #session.cfg['controlnet'] = "172.16.0.0/24"
        if not model:
            # Gives ping range of ~915m with 1:1 pixels to m and default 802.11
            # settings (2ray).
            session.master = True
            session.location.setrefgeo(47.57917,-122.13232,2.00000)
            session.location.refscale = 100.0
            session.cfg['emane_models'] = "RfPipe, Ieee80211abg, Bypass, AtdlOmni"
            session.emane.loadmodels()

        #prefix = ipaddr.IPv4Prefix("10.0.0.0/32")
        #tmp.newnetif(net, ["%s/%s" % (prefix.addr(i), prefix.prefixlen)])
        # set increasing Z coordinates
            wlan1 = session.addobj(cls = pycore.nodes.EmaneNode, name = "wlan1", objid=1, verbose=True)
        else:
            wlan1 = session.addobj(cls = pycore.nodes.WlanNode, name="wlan1",objid=1, verbose=True)

        wlan1.setposition(x=80,y=50)

        if not model:
            names = EmaneIeee80211abgModel.getnames()
            values = list(EmaneIeee80211abgModel.getdefaultvalues())
            print 'Emane Model default values: %s'%(str(list(EmaneIeee80211abgModel.getdefaultvalues())))
            # TODO: change any of the EMANE 802.11 parameter values here
            #values[ names.index('pathlossmode') ] = 'pathloss'
            session.emane.setconfig(wlan1.objid, EmaneIeee80211abgModel._name, values)

        else:
            print 'Basic Range Model default values: %s'%(str(BasicRangeModel.getdefaultvalues()))
            wlan1.setmodel(BasicRangeModel, BasicRangeModel.getdefaultvalues())

        services_str = "OLSR|IPForward"

        workers = []
        for i in range(2,8):
            pos = gen_linear_position(i)
            workers.append(create_node(i, session, "%s|MeanderWorker"%services_str, wlan1, pos)) 
        
        master = create_node(8, session, "%s|MeanderMaster"%services_str, wlan1,
                gen_linear_position(8))


        """
        node_map = create_node_map(range(0,6), workers)
        print 'Node map=%s'%node_map
        mobility_params[4] = ('map', node_map)
        session.mobility.setconfig_keyvalues(wlan1.objid, 'ns2script', mobility_params)
        """

        datacollect_hook = create_datacollect_hook(time_str, k, mob, exp_session) 
        session.sethook("hook:5","datacollect.sh",None,datacollect_hook)
        session.node_count=1+1+6
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
    n = session.addobj(cls = pycore.nodes.CoreNode, name="n%d"%i, objid=i)
    session.services.addservicestonode(n, "", services_str, verbose=False)
    n.newnetif(net=wlan, addrlist=["10.0.0.%d/32"%(i+ip_offset)], ifindex=0)
    n.cmd([SYSCTL_BIN, "net.ipv4.icmp_echo_ignore_broadcasts=0"])
    #n.cmd([SYSCTL_BIN, "net.ipv4.conf.forwarding.all=1"])
    n.setposition(x=pos[0], y=pos[1])
    return n

def create_node_map(ns_nums, nodes):
    if len(ns_nums) != len(nodes): 
        raise Exception("Invalid node mapping.")
    print 'ns_nums=%s'%str(ns_nums)
    print 'nodes=%s'%str(nodes)
    return ",".join(map(lambda (ns_num, node) : "%d:%d"%(ns_num,node.objid), zip(ns_nums, nodes)))

def gen_linear_position(i):
    return (50 * i, 100)

def add_to_server(session):
    global server
    try:
        server.addsession(session)
        return True
    except NameError:
        print 'Name error'
        return False

def create_datacollect_hook(time_str, k, mob, exp_session):
    script_dir = os.path.dirname(os.path.realpath(__file__))
    print 'Script dir = %s'%script_dir
    return datacollect_template%(script_dir, time_str, k, mob, exp_session)

def watch_meander_services(sessiondir, node_names):
    while True:
        for name in node_names:
            if os.path.exists("%s/%s.conf/worker.shutdown"%(sessiondir, name)) or os.path.exists("%s/%s.conf/master.shutdown"%(sessiondir, name)):
                print 'Shutdown file exists for node %s - exiting'%name
                return

        time.sleep(0.5)

def write_replication_factor(k, session_dir):
    with open('%s/k.txt'%session_dir, 'w') as f:
        f.write(str(k))

#def exists_mobility_trace(time_str, session):
#    return os.path.isfile(

if __name__ == "__main__" or __name__ == "__builtin__":
    parser = argparse.ArgumentParser(description='Run several meander experiments on CORE')
    parser.add_argument('--sessions', dest='sessions', default='1', help='number of sessions to run')
    parser.add_argument('--plotOnly', dest='plot_time_str', default=None, help='time_str of run to plot (hh-mm-DDDddmmyy)[None]')
    args=parser.parse_args()
    if args.plot_time_str:
        time_str = args.plot_time_str
    else:
        time_str = time.strftime('%H-%M-%S-%a%d%m%y')
    run_sessions(time_str, 2, 0.00, int(args.sessions))
