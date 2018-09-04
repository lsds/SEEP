#!/usr/bin/python

import sys,os,time,re,argparse,math,shutil,subprocess,socket,random


from core import pycore
from core.api import coreapi
from core.constants import *
from core.mobility import BasicRangeModel
from core.mobility import Ns2ScriptedMobility 
from core.emane.ieee80211abg import EmaneIeee80211abgModel
from core.emane.commeffect import EmaneCommEffectModel
from core.emane.emane import EmaneGlobalModel
#from core.misc.xmlutils import savesessionxml
from core.misc.xmlsession import savesessionxml
from core.misc import ipaddr

#from emanesh.events import EventService, LocationEvent

script_dir = os.path.dirname(os.path.realpath(__file__))
#script_dir = '%s/dev/seep-ita/seep-system/examples/acita_demo_2015/core-emane'%os.environ['HOME']

#print 'Appending script_dir to path'
#sys.path.append(script_dir)
from util import chmod_dir,pybool_to_javastr
from gen_mobility_trace import gen_trace
from gen_fixed_routes import create_static_routes
from emane_mobility import publish_loc, register_emane_ns2_model, EmaneNs2Session
from core_msg_util import *
from noise import create_noise_net
from iperf_util import *
from roofnet import *

#repo_dir = '%s/../../../..'
#svc_dir='/data/dev/seep-github/seep-system/examples/acita_demo_2015/core-emane/vldb/myservices'
svc_dir='%s/vldb/myservices'%script_dir
#conf_dir='/data/dev/seep-github/seep-system/examples/acita_demo_2015/core-emane/vldb/config'
conf_dir='%s/vldb/config'%script_dir
seep_jar = "seep-system-0.0.1-SNAPSHOT.jar"
mobility_params = [('file','%s/rwpt.ns_movements'%conf_dir),('refresh_ms',1000),
        ('loop',1),('autostart',1.0),('map',''),('script_start',''),('script_pause',''),('script_stop','')]


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
var=%.2f%s
session=%ds
#resultsDir=$scriptDir/log/$timeStr
resultsDir=$scriptDir/log/$timeStr/$k/$var/$session

expDir=$(pwd)

echo $expDir >> /tmp/datacollect.log
echo $scriptDir >> /tmp/datacollect.log
echo $timeStr >> /tmp/datacollect.log
echo $resultsDir >> /tmp/datacollect.log

mkdir -p $resultsDir

# Copy all log files to results dir
for d in n*.conf 
do
	cp $d/log1/*.log $resultsDir	
    if [ -e "$d/log2" ]
    then
        cp $d/log2/*.log $resultsDir	
    fi
	cp $d/mappingRecordOut.txt $resultsDir	
    mkdir $resultsDir/positions
	cp $d/*.xyz $resultsDir/positions	
	cp $d/mappingRecordOut.txt $scriptDir/log/$timeStr/session${session}MappingRecord.txt

    #Copy network links
    mkdir -p $resultsDir/links
    cp $d/links-*.txt $resultsDir/links

    #Copy any emane stats
    if [ -d emane-stats ];
    then
        mkdir -p $resultsDir/emane-tables
        cp $d/emane-tables*.txt $resultsDir/emane-tables
    fi

done

#Copy mobility params if they exist
cp r_waypoints.params $resultsDir

#Copy dstat trace if exists 
cp dstat/*.csv $resultsDir

#Copy any emane stats
if [ -d emane-stats ];
then
	mkdir -p $resultsDir/emane-stats
	cp emane-stats/*.txt $resultsDir/emane-stats
fi

#Copy net utilisation 
if [ -d net-util ];
then
    mkdir -p $resultsDir/net-util
    cp net-util/*net-util.txt $resultsDir/net-util
fi

#Copy cpu utilisation 
if [ -d cpu-util ];
then
    mkdir -p $resultsDir/cpu-util
    cp cpu-util/*cpu-util.txt $resultsDir/cpu-util
fi

cd $scriptDir
#./gen_core_results.py --expDir log/$timeStr 
./record_cpu_rates.py --expDir $resultsDir 
./record_net_rates.py --expDir $resultsDir 
./gen_core_results.py --expDir $resultsDir %s
#./move_analysis.py --nodes 10 --expDir $resultsDir/positions
chmod -R go+rw $resultsDir
cd $expDir
'''
def run_sessions(time_str, k, mob, nodes, var_suffix, sessions, params):
    for session in sessions:
        run_session(time_str, k, mob, nodes, var_suffix, session, params)

def run_session(time_str, k, mob, nodes, var_suffix, exp_session, params):

    print '*** Running session %d ***'%exp_session
    print 'params=',params
    tstart = time.time()

    distributed = bool(params['slave'])
    verbose = params['verbose'] 

    try:
        session_cfg = {'custom_services_dir':svc_dir, 'emane_log_level':'3',
                'verbose':"true" if verbose else "False", 
		'emane_event_monitor':"true" if params['emaneMobility'] else "false"} 
        if params['preserve']: session_cfg['preservedir'] = '1' 
        if distributed: 
            print 'slaves=',params['slave']
            slaves = params['slave'].split(',')
            #session_cfg['controlnetif1'] = "eth4"
            session_cfg['controlnetif1'] = "eth1"
            session_cfg['controlnet'] = "%s:172.16.1.0/24"%socket.gethostname()
            session_cfg['controlnet1'] = "%s:172.17.1.0/24"%socket.gethostname()

            slaveips = {}
            for i,slave in enumerate(slaves):
                slaveips[slave] = socket.gethostbyname(slave)
                session_cfg['controlnet'] += " %s:172.16.%d.0/24"%(slave, i+2)
                session_cfg['controlnet1'] += " %s:172.17.%d.0/24"%(slave, i+2)

            print 'Using controlnet: %s'%session_cfg['controlnet']
            print 'Using controlnet1: %s'%session_cfg['controlnet1']
        else: 
            session_cfg['controlnet'] = "172.16.1.0/24"

        if distributed and params['emaneMobility'] and mob > 0.0: session = EmaneNs2Session(cfg=session_cfg, persistent=True)
        else: session = pycore.Session(cfg=session_cfg, persistent=True)

        session.master=True
        session.location.setrefgeo(47.5791667,-122.132322,2.00000)
        session.location.refscale = 100.0
        session.metadata.additem("canvas c1", "{name {Canvas1}} {wallpaper-style {upperleft}} {wallpaper {/home/dan/dev/seep-ita/seep-system/examples/acita_demo_2015/core-emane/vldb/config/sample1-bg-large.png}} {size {3000 3000}}")


        if params['emaneMobility']: 
            register_emane_ns2_model(session)

        if distributed: 
            conf_msg = raw_emane_global_conf_msg(1)
            session.broker.handlerawmsg(conf_msg)
            session.confobj("emane", session, to_msg(conf_msg))

            for slave in slaves:
                remote_configure(session, slave, slaveips[slave])
                session.broker.handlerawmsg(location_conf_msg(session.location))

        if params['sinkDisplay']:
            sink_display = start_query_sink_display("sinkDisplay.log", session.sessiondir, params)
            print 'Started query sink display ', sink_display 

        params['repoDir'] = get_repo_dir()
        #This is so broken, should find a better way...
        write_replication_factor(k, session.sessiondir)
        write_chain_length(params['h'], session.sessiondir)
        write_query_type(params['query'], session.sessiondir)
        write_extra_params(params, session.sessiondir)
        write_session_params(params, session.sessiondir)
        write_extra_props_dir(session.sessiondir)
        write_extra_props(k, params['h'], params['query'], params, session.sessiondir)

        copy_seep_jar(session.sessiondir)
        trace_file = None
        if mob > 0.0:
            trace_params = dict(params)
            trace_params['h'] = mob + 1.0
            trace_params['l'] = mob - 1.0
            trace_params['o'] = mob
            trace_file = gen_trace(session.sessiondir, exp_session, nodes, trace_params)
            print 'Trace file=',trace_file


        model = params.get('model')
        #tx_range = 499 #TODO: How to apply to emane?
        #tx_range = 399 #TODO: How to apply to emane?
        #tx_range = 599 #TODO: How to apply to emane?
        #tx_range = 525 #TODO: How to apply to emane?
        #tx_range = 1250 #TODO: How to apply to emane?
        #tx_range = 250 #TODO: How to apply to emane?
        #tx_range = 300 #TODO: How to apply to emane?
        #tx_range = 450 #TODO: How to apply to emane?
        #tx_range = 750 #TODO: How to apply to emane?
        #tx_range = 410 #TODO: How to apply to emane?
        tx_range = 410 #TODO: How to apply to emane?
        #tx_range = 260 #TODO: How to apply to emane?
        print 'Model=', model
        if model == "Emane":
            # Gives ping range of ~915m with 1:1 pixels to m and default 802.11
            # settings (2ray).
            session.cfg['emane_models'] = "RfPipe, Ieee80211abg, Bypass, CommEffect"
            session.emane.loadmodels()

            #prefix = ipaddr.IPv4Prefix("10.0.0.0/32")
            #tmp.newnetif(net, ["%s/%s" % (prefix.addr(i), prefix.prefixlen)])
            # set increasing Z coordinates
            wlan1 = session.addobj(cls = pycore.nodes.EmaneNode, name = "wlan1", objid=1, verbose=verbose)
            wlan1.setposition(x=80,y=50)
            if distributed: session.broker.handlerawmsg(wlan1.tonodemsg(flags=coreapi.CORE_API_ADD_FLAG|coreapi.CORE_API_STR_FLAG))
            """
            TODO: Might need to add this back!
            for servername in session.broker.getserverlist():
                session.broker.addnodemap(servername, wlan1.objid)
            """
            if params['emaneModel'] == 'Ieee80211abg':
                create_80211_net(session, wlan1, distributed, params, verbose)
            elif params['emaneModel'] == 'CommEffect':
                create_commeffect_net(session, wlan1, distributed, params, verbose)
            else:
                raise Exception('Unknown emane model: %s'%str(params['emaneModel']))

        elif model == "Basic" and not distributed:
            wlan1 = session.addobj(cls = pycore.nodes.WlanNode, name="wlan1",objid=1, verbose=verbose)
            wlan1.setposition(x=80,y=50)
            print 'Basic Range Model default values: %s'%(str(BasicRangeModel.getdefaultvalues()))
            model_cfg = list(BasicRangeModel.getdefaultvalues())
            model_cfg[0] = str(tx_range) #Similar to default effective emane range.
            model_cfg[1] = '11000' #Similar to default emane bandwidth.
            print 'Basic Range configured values: %s'%(str(model_cfg))
            wlan1.setmodel(BasicRangeModel, tuple(model_cfg))
        else:
            raise Exception("Unknown model/distributed: %s/%s"%(model,str(distributed)))

        if not add_to_server(session, params): 
            if distributed: raise Exception("Couldn't attach to core daemon in distributed mode!")
            else: print 'Could not add to server'

        if params['iperf']:
            #run_basic_iperf_test(session, wlan1, mob, trace_file, tx_range, params)
            run_multi_source_iperf_test(session, nodes, wlan1, mob, trace_file, tx_range, params)

            return

        #Copy appropriate mapping constraints.
        exp_results_dir = '%s/log/%s'%(script_dir, time_str)
        session_constraints = '%s/session%dsMappingRecord.txt'%(exp_results_dir, exp_session)
        if os.path.exists(session_constraints):
            shutil.copy(session_constraints, '%s/mappingRecordIn.txt'%session.sessiondir)
        elif params['constraints']:
            session_constraints = '%s/static/%s'%(script_dir, params['constraints'])
            if not os.path.exists(session_constraints):
                raise Exception("Could not find sessions constraints: %s"%session_constraints)
            shutil.copy(session_constraints, '%s/mappingRecordIn.txt'%session.sessiondir)
        elif params['colocateSrcSink']:
            session_constraints = '%s/static/colo_src_sink_constraints.txt'%(script_dir)
            if not os.path.exists(session_constraints):
                raise Exception("Could not find colocated src and sink constraints: %s"%session_constraints)
            shutil.copy(session_constraints, '%s/mappingRecordIn.txt'%session.sessiondir)

        #TODO: Do I actually need the default route/multicast service?
        services_str = "IPForward|DefaultRoute|DefaultMulticastRoute|SSH"
        master_services = services_str
        if params['dstat']: master_services += "|dstat" 
 
        master = create_node(2, session, "%s|FrontierMaster"%master_services, wlan1,
                gen_grid_position(2+nodes, nodes - 1), addinf=False, verbose=verbose)

        services_str += "|%s"%params['net-routing']
        if params['quagga']: services_str += "|zebra|vtysh"
        if params['emanestats']: services_str += "|EmaneStats"
        #if params['netutil']: services_str += "|NetUtil"
        services_str += "|NetUtil"
        services_str += "|CpuUtil"

        workers = []
        num_workers = get_num_workers(k, nodes, params)
        print 'num_workers=', num_workers
        if params['roofnet']:
            roofnet_placements = parse_roofnet_locations()
            roof_node_ids = map(lambda (node, x, y) : node, roofnet_placements)


            numPinned = 0 # Get number of pinned nodes
            if params['pinnedSeed'] and params['constraints']:
                # Get number of pinned nodes
                session_constraints = '%s/static/%s'%(script_dir, params['constraints'])
                if not os.path.exists(session_constraints):
                    raise Exception("Could not find sessions constraints: %s"%session_constraints)
                with open(session_constraints, 'r') as f:
                    regex = re.compile('.*,.*:.*')
                    for line in f:
                        if re.search(regex, line): 
                            numPinned += 1
                if numPinned < 1 : raise Exception("Expecting pinned constraints with random seed.")
                random.seed(int(params['pinnedSeed']))
                pinned_shuffle = range(3, len(roofnet_placements)+3)
                random.shuffle(pinned_shuffle)
                unpinned_shuffle = pinned_shuffle[numPinned:]
                pinned_shuffle = pinned_shuffle[:numPinned]
                if len(pinned_shuffle) != numPinned: raise Exception("Logic error.")
                if params['pinnedSeed'] and not params['pinAll']:
                    random.seed(int(exp_session))
                    random.shuffle(unpinned_shuffle)

                print 'pinned shuffle=%s'%str(pinned_shuffle)
                print 'unpinned shuffle=%s'%str(unpinned_shuffle)
                shuffle = pinned_shuffle+unpinned_shuffle

                roof_to_nem = dict(zip(roof_node_ids, shuffle))

            else:
                # node id offset = wlan1 + master -> 2
                random.seed(int(exp_session))
                shuffle = range(3,len(roofnet_placements)+3)
                random.shuffle(shuffle)
                print 'shuffle=%s'%str(shuffle)
                roof_to_nem = dict(zip(roof_node_ids, shuffle))

                ##swaps = [(8,10), (7,9)]
                swaps = []
                nem_to_roof = dict(zip(shuffle, roof_node_ids))
                print 'Pre swaps roof_to_nem=%s'%str(roof_to_nem)
                for (m, n) in swaps:
                   roof_to_nem[nem_to_roof[m]] = n
                   roof_to_nem[nem_to_roof[n]] = m

                print 'Post swaps roof_to_nem=%s'%str(roof_to_nem)
                # Shuffle based on session id to give a different query placement for each session.
                # Need to make sure placements is sorted though. 
                # TODO: This initial placement is wrong! 
                #placements = map(lambda (node, x, y) : (roof_to_nem[node], x, y), roofnet_placements)
                #placements = map(lambda node: (roofnet_placements[node][1], roofnet_placements[node][2]), shuffle)

            placements = [(-1.0,-1.0),(-1.0,-1.0),(-1.0,-1.0)] + map(lambda node: (roofnet_placements[node-3][1], roofnet_placements[node-3][2]), shuffle)
            print 'roof2nem=%s'%str(roof_to_nem)
            print 'Roofnet placements=%s'%str(placements)

        else:
            placements = get_initial_placements(params['placement'], mob, params['xyScale'])

            print 'Initial placements=',placements
            if placements: 
                create_static_routes(placements, tx_range, session.sessiondir)
                if len(placements) != nodes-2: raise Exception("Expected placement for %d nodes, got %d"%(nodes-2,len(placements)))

        if params['injectFailures'] or params['injectProbFailures'] or params['failProb'] > 0.0: 
            print 'Creating failures injectors.'
            if params['injectFailures'] or params['injectProbFailures']:
                (src,dest) = (params['injectFailures'],'failure_cycles.txt') if params['injectFailures'] else (params['injectProbFailures'],'failure_probs.txt') 
                print 'Copying failure spec from %s to %s.'%(src,dest)
                shutil.copy("%s/static/%s"%(script_dir, src), '%s/%s'%(session.sessiondir,dest))
            else:
                print 'Generate a fixed failure probability schedule for all nodes'
                with open("%s/failure_probs.txt"%session.sessiondir, 'w') as fp:
                    fp.write('*,%.1f,%.1f,%.2f\n'%(params['failProbStart'], params['failProbSlot'], params['failProb'])) 

            with open("%s/start_failures.txt"%session.sessiondir, 'w') as sf:
                sf.write(str(time.time()) + "\n")

        print 'Creating workers.'
        for i in range(3,3+len(num_workers)):
            if placements:
                pos = placements[i]
            else:
                pos = gen_grid_position(i, nodes-1)
            worker_services = "|".join(["FrontierWorker%d"%lwid for lwid in range(1, num_workers[i-3]+1)])
            if params['pcap']: worker_services += "|PcapSrc"
            if params['injectFailures'] or params['injectProbFailures'] or params['failProb'] > 0.0: worker_services += "|FailureInjector"
            #if params['emanestats']: worker_services += "|EmaneStats"
            workers.append(create_node(i, session, "%s|%s"%(services_str, worker_services), wlan1, pos, verbose=verbose)) 
       
        routers = []
        print 'Creating routers.'
        # Create auxiliary 'router' nodes if any left
        for i in range(3+len(num_workers), 1+nodes):
            if placements:
                pos = placements[i]
            else:
                pos = gen_grid_position(i, nodes-1)

            router_services="|FailureInjector" if params['injectFailures'] or params['injectProbFailures']  or params['failProb'] > 0.0 else "" 
            if distributed:
                slave = slaves[i % len(slaves)]
                routers.append(create_remote_node(i, session, slave, "%s%s"%(services_str,router_services), wlan1, pos, verbose=verbose))
            else:
                routers.append(create_node(i, session, "%s%s"%(services_str,router_services), wlan1, pos, verbose=verbose))

        if params['noiseNodes'] > 0:
            #TODO: Is this the right way to compute noise_net_id?
            noise_net_id = nodes + 1
            wlan_noise = create_noise_net(session, noise_net_id, distributed, verbose)
            noise_nodes = []
            noise_services_str = "IPForward|DefaultRoute|DefaultMulticastRoute|SSH|IPerfSrc"

            for noise_node_id in range(nodes+2, nodes+2+params['noiseNodes']):
                print 'Creating noise source with node id %d'%noise_node_id
                #pos = placements[noise_node_id] if placements else gen_grid_position(noise_node_id, nodes + params['noiseNodes'])
                pos = gen_grid_position(noise_node_id, nodes + params['noiseNodes'])

                if distributed:
                    slave = slaves[i % len(slaves)]
                    noise_nodes.append(create_remote_node(noise_node_id, session, slave, "%s"%noise_services_str, wlan_noise, pos, verbose=verbose))
                else:
                    noise_nodes.append(create_node(noise_node_id, session, "%s"%noise_services_str, wlan_noise, pos, verbose=verbose))

        if trace_file:
            node_map = create_node_map(range(0,nodes-2), workers+routers)
            print 'Node map=%s'%node_map
            mobility_params[4] = ('map', node_map)
            mobility_params[0] = ('file','%s/%s'%(session.sessiondir, trace_file))
            refresh_ms = int(params.get('refresh_ms', 1000))
            mobility_params[1] = ('refresh_ms', refresh_ms)
            mobility_params[2] = ('loop', 0)
            mobility_script = 'emaneNs2script' if params['emaneMobility'] else 'ns2script'
            print 'Using %s mobility script'%mobility_script
            session.mobility.setconfig_keyvalues(wlan1.objid, mobility_script, mobility_params)

        var = mob
        if var_suffix=='n': var = nodes 
        if var_suffix=='d': var = params['x']
        if var_suffix=='c': var = params['defaultProcessingDelay']
        if var_suffix=='r': var = params['frameRate']
        if var_suffix=='rcd': var = params['routingCtrlDelay']
        if var_suffix=='retx': var = params['retransmitTimeout']
        if var_suffix=='bsz': var = params['maxTotalQueueSizeTuples']
        if var_suffix=='sl': var = params['skewLimit']
        if var_suffix=='fp': var = params['failProb']
 

        datacollect_hook = create_datacollect_hook(time_str, k, var, var_suffix, exp_session, params['sub']) 
        session.sethook("hook:5","datacollect.sh",None,datacollect_hook)
        session.node_count="%d"%(nodes)

        if params['saveconfig']:
            print 'Saving session config.'
            savesessionxml(session, '%s/session.xml'%session.sessiondir, '1.0')

        print 'Instantiating session ',exp_session, ' with CORE session id', session.sessionid
        if distributed and params['emaneMobility'] and mob > 0.0: session.instantiate(wlan1, routers)  
        else: session.instantiate()
	#remote_instantiate(session)

        chmod_dir(session.sessiondir)
        for n in range(2,3+len(num_workers)):
            node_dir = '%s/n%d.conf'%(session.sessiondir,n)
            chmod_dir('%s/var.run/sshd'%node_dir, 0655)
            chmod_dir('%s/var.run.sshd'%node_dir, 0655)
            while not os.path.exists('%s/etc.ssh/ssh_host_rsa_key'%node_dir):
                time.sleep(1)
            os.chmod('%s/etc.ssh/ssh_host_rsa_key'%node_dir, 0700)

        if params['emaneMobility'] and params['roofnet']:

            packet_losses = parse_roofnet_packetloss(roof_to_nem)
            #TODO Only publish for nodes that exist?

            if params['emaneModel'] == 'CommEffect':
                publish_commeffects(session, packet_losses, roof_to_nem, verbose=True)
                time.sleep(5)
                publish_commeffects(session, packet_losses, roof_to_nem, verbose=True)
                time.sleep(5)
                publish_commeffects(session, packet_losses, roof_to_nem, verbose=True)
                time.sleep(5)
            else:
                publish_pathlosses(session, packet_losses, roof_to_nem, txratemode=int(params['txratemode']), verbose=True)
                if distributed: publish_locations(session, roofnet_placements, roof_to_nem, verbose=True)
                time.sleep(5)
                publish_pathlosses(session, packet_losses, roof_to_nem, txratemode=int(params['txratemode']), verbose=True)
                if distributed: publish_locations(session, roofnet_placements, roof_to_nem, verbose=True)
                time.sleep(5)
                publish_pathlosses(session, packet_losses, roof_to_nem, txratemode=int(params['txratemode']), verbose=True)
                if distributed: publish_locations(session, roofnet_placements, roof_to_nem, verbose=True)
                time.sleep(5)

        elif params['emaneMobility'] and params['emaneModel'] == "CommEffect":

            #TODO Only publish for nodes that exist?
            loss = 0.0
            packet_losses = [(src, dest, loss) for src in range(3, nodes+1) for dest in range(3,nodes+1) if src != dest]
            roof_to_nem = dict((n,n) for n in range(3, nodes+1))

            publish_commeffects(session, packet_losses, roof_to_nem, verbose=True)
            time.sleep(5)
            publish_commeffects(session, packet_losses, roof_to_nem, verbose=True)
            time.sleep(5)
            publish_commeffects(session, packet_losses, roof_to_nem, verbose=True)
            time.sleep(5)

        print 'Waiting for a frontier worker/master to terminate'
        watch_frontier_services(session.sessiondir, map(lambda n: "n%d"%n,
            range(2,3 + sum(num_workers))))


        if model == "Emane" and params['emanestats']:
            record_emanesh_tables(session.sessiondir, range(3, nodes+1), params)

        #time.sleep(30)
        print 'Collecting data'
        session.datacollect()
	#remote_datacollect(session)
        time.sleep(5)
        print 'Shutting down'

    finally:
        print 'Shutting down session.'

        if session:
            if distributed: 
                remote_shutdown(session)
                time.sleep(300)
            session.shutdown()
            if not params['preserve']:
                core_cleanup()
            if 'server' in globals():
                print 'Removing session from core daemon server'
                server.delsession(session)

        if params['sinkDisplay'] and sink_display:
            print 'Shutting down query sink display ', sink_display
            sink_display.stdin.close()
            sink_display.terminate()
        print 'Session completed in %.1f seconds.'%(time.time() - tstart)

def remote_configure(session, slave, slaveip):
	session.broker.addserver(slave, slaveip, coreapi.CORE_API_PORT)
	session.broker.setupserver(slave)
	session.setstate(coreapi.CORE_EVENT_CONFIGURATION_STATE)
	tlvdata = coreapi.CoreEventTlv.pack(coreapi.CORE_TLV_EVENT_TYPE,
					coreapi.CORE_EVENT_CONFIGURATION_STATE)
	session.broker.handlerawmsg(coreapi.CoreEventMessage.pack(0, tlvdata))

def remote_instantiate(session):
	tlvdata = coreapi.CoreEventTlv.pack(coreapi.CORE_TLV_EVENT_TYPE,
					coreapi.CORE_EVENT_INSTANTIATION_STATE)
	msg = coreapi.CoreEventMessage.pack(0, tlvdata)
	session.broker.handlerawmsg(msg)

def remote_shutdown(session):
	tlvdata = coreapi.CoreEventTlv.pack(coreapi.CORE_TLV_EVENT_TYPE,
					coreapi.CORE_EVENT_SHUTDOWN_STATE)
	msg = coreapi.CoreEventMessage.pack(0, tlvdata)
	session.broker.handlerawmsg(msg)

def create_80211_net(session, wlan, distributed, params, verbose=False):
    names = EmaneIeee80211abgModel.getnames()
    values = list(EmaneIeee80211abgModel.getdefaultvalues())
    print 'Emane Model default names: %s'%(str(names))
    print 'Emane Model default values: %s'%(str(values))
    # TODO: change any of the EMANE 802.11 parameter values here
    values[ names.index('mode') ] = '3'
    #values[ names.index('rtsthreshold') ] = '1024'
    values[ names.index('retrylimit') ] = '0:7'
    #values[ names.index('retrylimit') ] = '0:3'
    #values[ names.index('cwmin') ] = '0:16'
    #values[ names.index('cwmax') ] = '0:2048'
    #prop_model = 'precomputed' if params['roofnet'] else '2ray'
    prop_model = 'precomputed' if params['roofnet'] else 'freespace'
    values[ names.index('propagationmodel') ] = prop_model 
    #values[ names.index('propagationmodel') ] = '2ray'
    #values[ names.index('propagationmodel') ] = 'freespace'
    #values[ names.index('pathlossmode') ] = '2ray'
    #values[ names.index('multicastrate') ] = '12'
    txratemode = params['txratemode']
    values[ names.index('multicastrate') ] = txratemode 
    #values[ names.index('multicastrate') ] = '4'
    #values[ names.index('multicastrate') ] = '1'
    #values[ names.index('unicastrate') ] = '12'
    #values[ names.index('distance') ] = '500'
    values[ names.index('unicastrate') ] = txratemode 
    #values[ names.index('unicastrate') ] = '4'
    values[ names.index('txpower') ] = '-10.0'
    #values[ names.index('txpower') ] = '-1.0'
    values[ names.index('flowcontrolenable') ] = 'on'
    #values[ names.index('flowcontrolenable') ] = 'off'
    values[ names.index('flowcontroltokens') ] = '10'
    print 'Emane Model overridden values: %s'%(str(list(values)))
    if distributed:
        typeflags = coreapi.CONF_TYPE_FLAGS_UPDATE
        msg = EmaneIeee80211abgModel.toconfmsg(flags=0, nodenum=wlan.objid,
     typeflags=typeflags, values=values)
        session.broker.handlerawmsg(msg)

    session.emane.setconfig(wlan.objid, EmaneIeee80211abgModel._name, values)

    if distributed:
        #No idea why I need to call this again here with a None node but seems I have to. 
        conf_msg = raw_emane_global_conf_msg(None)
        session.broker.handlerawmsg(conf_msg)
        session.confobj("emane", session, to_msg(conf_msg))

def create_commeffect_net(session, wlan, distributed, params, verbose=False):
    names = EmaneCommEffectModel.getnames()
    values = list(EmaneCommEffectModel.getdefaultvalues())
    print 'Emane Model default names: %s'%(str(names))
    print 'Emane Model default values: %s'%(str(values))
    #values[ names.index('flowcontrolenable') ] = 'on'
    #values[ names.index('flowcontroltokens') ] = '10'
    values[ names.index('defaultconnectivitymode') ] = 'off'
    #values[ names.index('defaultconnectivitymode') ] = 'on'
    values[ names.index('receivebufferperiod') ] = '1.0'
    #values[ names.index('defaultconnectivitymode') ] = 'on'
    values[ names.index('filterfile') ] = ''

    if distributed:
        typeflags = coreapi.CONF_TYPE_FLAGS_UPDATE
        msg = EmaneCommEffectModel.toconfmsg(flags=0, nodenum=wlan.objid,
     typeflags=typeflags, values=values)
        session.broker.handlerawmsg(msg)

    session.emane.setconfig(wlan.objid, EmaneCommEffectModel._name, values)

    if distributed:
        #No idea why I need to call this again here with a None node but seems I have to. 
        conf_msg = raw_emane_global_conf_msg(None)
        session.broker.handlerawmsg(conf_msg)
        session.confobj("emane", session, to_msg(conf_msg))

def get_num_workers(k, nodes, params):
    q = params['query']
    sink_scale_factor = get_sink_scale_factor(k, params)

    if q == 'chain' or q == 'fr' or q == 'fdr': 
        if q == 'fr' and params['h'] != 2: raise Exception("Fr query needs height of 2.");
        if q == 'fdr' and params['h'] != 1: raise Exception("Fdr query needs height of 1.");
        if params['colocateSrcSink']:
            if sink_scale_factor > 1: raise Exception("Can't colocate source with replicated sinks.")
            num_workers = [2] + [1] * (k * params['h'])
        else:
            num_workers = [1] * (1 + sink_scale_factor + (k * params['h']))
    elif q == 'join':
        if params['h'] != 1: raise Exception('Only support query of height 1 for join')
        sources = int(params['sources'])
        if sources <= 1: raise Exception('Need at least 2 sources for join')
        num_workers = [1] * (sources +  k + sink_scale_factor)
    elif q == 'leftJoin':
        if params['h'] < 2: raise Exception('Left join only needed for height > 2')
        sources =  int(params['sources'])
        height = sources - 1
        if height != params['h']: raise Exception('Height must equals # sources + 1 for left join')
        sinks = int(params['sinks'])
        if sinks != 1: raise Exception("Only 1 true (unreplicated) sink supported for left join")
        fan_in = int(params['fanin'])
        if fan_in != 2: raise Exception("TODO: Only support fan-in of 2 for left join currently.")
        join_ops = sources - 1
        num_workers = [1] * (sources + (k*join_ops)+ (sink_scale_factor * sinks))
    elif q == 'frJoin':
        if params['h'] != 2: raise Exception('Only support query of height 2 for frjoin')
        sources = int(params['sources'])
        if sources != 2: raise Exception('Only support 2 sources for frjoin')
        sinks = int(params['sinks'])
        if sinks != 1: raise Exception("Only 1 true (unreplicated) sink supported for frjoin")
        fan_in = int(params['fanin'])
        if fan_in != 2: raise Exception("TODO: Only support fan-in of 2 for frjoin currently.")
        join_ops = 1
        num_workers = [1] * (sources + (k * sources)+ (k * join_ops) + (sink_scale_factor * sinks))
    elif q == 'heatMap':
        sources =  int(params['sources'])
        sinks = int(params['sinks'])
        fan_in = int(params['fanin'])
        if fan_in != 2: raise Exception("TODO: Only support fan-in of 2 for heatmap currently.")
        height = int(math.ceil(math.log(sources, fan_in)))
        children = sources
        join_ops = 0 
        for i in range(0, height):
            parents = children / fan_in
            if children % fan_in > 0: parents += 1
            join_ops += parents
            children = parents
        print 'height=%d, join_ops=%d'%(height, join_ops)
        worker_nodes = nodes - 2
        if worker_nodes >= sources + k*(join_ops) + (sink_scale_factor * sinks):
            num_workers = [1] * (sources + k*(join_ops) + (sink_scale_factor * sinks))
        else:
            #Need to have multiple workers on some nodes.
            #N.B. Don't want to colocate replicas of the
            #same operator or the source.
            raise Exception("TODO")
    else: 
        raise Exception('Unknown query type: %s'%q)

    return num_workers

def get_sink_scale_factor(k, params):
	if not params['pyScaleOutSinks']: return 1
	elif params['sinkScaleFactor']: return int(params['sinkScaleFactor'])
	else: return k

def create_node(i, session, services_str, wlan, pos, ip_offset=-1, addinf=True, verbose=False):
#def create_node(i, session, services_str, wlan, pos, ip_offset=8):
    tstart = time.time() 
    n = session.addobj(cls = pycore.nodes.CoreNode, name="n%d"%i, objid=i)
    taddobj = time.time() - tstart
    #n.setposition(x=pos[0], y=pos[1], z=1.0) N.B. Might need an altitude of 3.0 for 2ray!
    print('N.B. Might need an altitude of 3.0 for 2ray (z=1.0)')
    n.setposition(x=pos[0], y=pos[1])
    session.services.addservicestonode(n, "", services_str, verbose=verbose)
    taddservices = time.time() - tstart
    if addinf:
        ip = i + ip_offset 
        #n.newnetif(net=wlan, addrlist=["10.0.0.%d/32"%(ip)], ifindex=0)
        n.newnetif(net=wlan, addrlist=["10.0.0.%d/24"%(ip)], ifindex=0)
        taddnetif = time.time() - tstart
        n.cmd([SYSCTL_BIN, "net.ipv4.icmp_echo_ignore_broadcasts=0"])
        tcmd = time.time() - tstart
        print 'taddobj=%.3f,taddserv=%.3f,taddnet=%.3f,tcmd=%.3f'%(taddobj,taddservices,taddnetif,tcmd)
        print 'Created node n%d (10.0.0.%d) with initial pos=(%.1f,%.1f)'%(i,ip,pos[0], pos[1])
    else:
        print 'Created node n%d (no inf) with initial pos=(%.1f,%.1f)'%(i,pos[0], pos[1])
    #n.cmd([SYSCTL_BIN, "net.ipv4.ip_forward=1"])
    #n.cmd([SYSCTL_BIN, "net.ipv4.conf.all.forwarding=1"])
    #n.cmd([SYSCTL_BIN, "net.ipv6.conf.all.forwarding=1"])
    #n.cmd([SYSCTL_BIN, "net.ipv4.conf.all.rp_filter=0"])
    #n.cmd([SYSCTL_BIN, "net.ipv4.conf.default.rp_filter=0"])

    return n

def create_remote_node(i, session, slave, services_str, wlan, pos, ip_offset=-1, addinf=True, verbose=False):
        n = pycore.nodes.CoreNode(session = session, objid = i,
                                    name = "n%d" % i, start=False)
        print('N.B. Might need an altitude of 3.0 for 2ray (z=1.0)')
        #n.setposition(x=pos[0], y=pos[1], z=1.0) N.B. Might need an altitude of 3.0 for 2ray!
        n.setposition(x=pos[0],y=pos[1])
        n.server = slave
        session.services.addservicestonode(n, "", services_str, verbose=verbose)
	# TODO: addinf
        session.broker.handlerawmsg(n.tonodemsg(flags=coreapi.CORE_API_ADD_FLAG | coreapi.CORE_API_STR_FLAG))

	#if addinf:
	#	ip = i + ip_offset 
	#	n.newnetif(net=wlan, addrlist=["10.0.0.%d/32"%(ip)], ifindex=0)
	prefix = ipaddr.IPv4Prefix("10.0.0.0/24")
	ip = i + ip_offset 
        tlvdata = coreapi.CoreLinkTlv.pack(coreapi.CORE_TLV_LINK_N1NUMBER,
                                           wlan.objid)
        tlvdata += coreapi.CoreLinkTlv.pack(coreapi.CORE_TLV_LINK_N2NUMBER, i)
        tlvdata += coreapi.CoreLinkTlv.pack(coreapi.CORE_TLV_LINK_TYPE,
                                            coreapi.CORE_LINK_WIRED)
        tlvdata += coreapi.CoreLinkTlv.pack(coreapi.CORE_TLV_LINK_IF2NUM, 0)
        tlvdata += coreapi.CoreLinkTlv.pack(coreapi.CORE_TLV_LINK_IF2IP4,
                                            prefix.addr(i+ip_offset))
                                            #"10.0.0.%d"%ip)
        tlvdata += coreapi.CoreLinkTlv.pack(coreapi.CORE_TLV_LINK_IF2IP4MASK,
                                            prefix.prefixlen)
        msg = coreapi.CoreLinkMessage.pack(coreapi.CORE_API_ADD_FLAG, tlvdata)
        session.broker.handlerawmsg(msg)
        print 'Created remote node n%d (no inf) with initial pos=(%.1f,%.1f)'%(i,pos[0], pos[1])
        return n

def create_node_map(ns_nums, nodes):
    print 'ns_nums=%s'%str(ns_nums)
    print 'nodes=%s'%str(nodes)
    if len(ns_nums) != len(nodes): 
        raise Exception("Invalid node mapping, %d != %d"%(len(ns_nums), len(nodes)))
    return ",".join(map(lambda (ns_num, node) : "%d:%d"%(ns_num,node.objid), zip(ns_nums, nodes)))

def get_initial_placements(placements, mobility, xyScale):
    if not placements or mobility > 0.0:
        return None
    else:
        result = {}
        placements_path = '%s/static/%s'%(script_dir, placements)
        xyScale = float(xyScale) if xyScale else 1.0
        print 'xyScale = %s'%str(xyScale)
        with open(placements_path, 'r') as pf:
            for line in pf:
                if not line.strip().startswith('#'):
                    els = map(int, line.split(','))
                    print els
                    result[els[0]] = (int(xyScale*els[1]), int(xyScale*els[2]))

        return result

def gen_linear_position(i):
    return (50 * i, 100)

def gen_grid_position(i, nodes, offset=3, spacing=400):
    if i < offset: raise Exception("Invalid offset for %d: %d"%(i,offset))
    dim = max(1.0, math.floor(math.sqrt(nodes)))
    num_x = (i-offset) % dim 
    num_y = math.floor((i-offset) / dim)
    print 'i=',i, 'nodes=',nodes,'offset=',offset,'dim=',dim,'num_x=',num_x,'num_y=',num_y
    return (int(spacing * num_x), int(spacing * num_y)) 

def add_to_server(session, params):
    global server
    try:
        server.addsession(session)
        return True
    except NameError:
        if params['daemon_server']: 
            params['daemon_server'].addsession(session)
            return True
        else: 
            print 'Name error'
            return False

def create_datacollect_hook(time_str, k, var, var_suffix, exp_session, sub):
    print 'Script dir = %s'%script_dir
    hook = datacollect_template%(script_dir, time_str, k, var, var_suffix, exp_session, '--sub' if sub else '')
    return hook

def watch_frontier_services(sessiondir, node_names):
    while True:
        for name in node_names:
            for process in ['worker1', 'worker2', 'master']:
                if os.path.exists("%s/%s.conf/%s.shutdown"%(sessiondir, name, process)):
		    print 'Shutdown file exists for node %s (%s) - exiting'%(name, process)
		    return

        time.sleep(0.5)

def write_replication_factor(k, session_dir):
    with open('%s/k.txt'%session_dir, 'w') as f:
        f.write(str(k))

def write_chain_length(h, session_dir):
    with open('%s/h.txt'%session_dir, 'w') as f:
        f.write(str(h))

def write_query_type(query, session_dir):
    with open('%s/query.txt'%session_dir, 'w') as f:
        f.write(str(query))

def write_extra_params(params, session_dir):
    with open('%s/extra_params.txt'%session_dir, 'w') as f:
        f.write('sources=%s\n'%str(params['sources']))
        f.write('sinks=%s\n'%str(params['sinks']))
        f.write('fanin=%s\n'%str(params['fanin']))

def write_session_params(params, session_dir):
    with open('%s/session_params.txt'%session_dir, 'w') as f:
        for p in params:
            f.write('%s=%s\n'%(p,str(params[p])))

def write_extra_props_dir(session_dir):
    with open('%s/extraPropsDir.txt'%session_dir, 'w') as f:
        f.write('%s/extraConfig.properties'%session_dir)

def write_extra_props(k, h, query, params, session_dir):
    with open('%s/extraConfig.properties'%session_dir, 'w') as f:
        for p in params:
            f.write('%s=%s\n'%(p,str(params[p])))
        f.write('k=%s\n'%(str(k)))
        f.write('chainLength=%s\n'%(str(h)))
        f.write('queryType=%s\n'%(str(query)))

def copy_seep_jar(session_dir):
    dest = '%s/lib'%session_dir
    os.mkdir(dest)
    shutil.copy('%s/../lib/%s'%(script_dir,seep_jar), dest)

#def exists_mobility_trace(time_str, session):
#    return os.path.isfile(

def regen_sessions(time_str):
    raise Exception("TODO")

def run_basic_iperf_test(session, wlan1, mob, nodes, trace_file, tx_range, params):
    services_str = "IPForward|SSH"
    services_str += "|%s"%params['net-routing']
    if params['quagga']: services_str += "|zebra|vtysh"
    if params['emanestats']: services_str += "|EmaneStats"
    if params['dstat']: services_str += "|dstat"

    placements = get_initial_placements(params['placement'], mob)
    if placements: create_static_routes(placements, tx_range, session.sessiondir)
    print placements
    pos = placements[2] if placements else gen_grid_position(2, nodes-1, 2, spacing=400)
    src_services = "%s|IPerfSrc"%(services_str)
    if params['duplex']: src_services += "|IPerfSink"
    src = create_node(2, session, src_services, wlan1, pos) 
    
    pos = placements[3] if placements else gen_grid_position(3, nodes-1, 2, spacing=400)
    sink_services = "%s|IPerfSink"%(services_str)
    if params['duplex']: sink_services += "|IPerfSrc"
    sink = create_node(3, session, sink_services, wlan1, pos) 
   
    routers = []
    print 'Creating routers.'
    # Create auxiliary 'router' nodes if any left
    for i in range(4, nodes+1):
        if placements:
            pos = placements[i]
        else:
            pos = gen_grid_position(i, nodes-1, 2, spacing=400)
        routers.append(create_node(i, session, "%s"%services_str, wlan1, pos))

    if trace_file:
        #node_map = create_node_map(range(0,6), workers)
        node_map = create_node_map(range(0,nodes-1), [src, sink]+routers)
        print 'Node map=%s'%node_map
        mobility_params[4] = ('map', node_map)
        mobility_params[0] = ('file','%s/%s'%(session.sessiondir, trace_file))
        refresh_ms = int(params.get('refresh_ms', 1000))
        mobility_params[1] = ('refresh_ms', refresh_ms)
        session.mobility.setconfig_keyvalues(wlan1.objid, 'ns2script', mobility_params)


    session.node_count="%d"%(nodes)
    session.instantiate()
    time.sleep(1000000)

def run_multi_source_iperf_test(session, nodes, wlan1, mob, trace_file, tx_range, params):
    services_str = "IPForward|SSH"
    services_str += "|%s"%params['net-routing']
    if params['quagga']: services_str += "|zebra|vtysh"
    if params['emanestats']: services_str += "|EmaneStats"
    if params['dstat']: services_str += "|dstat"
    if params['pcap']: services_str += "|PcapSrc"

    placements = get_initial_placements(params['placement'], mob, params['xyScale'])
    if placements: create_static_routes(placements, tx_range, session.sessiondir)

    iperf_cxns = read_iperf_cxns(script_dir, params)
    copy_iperf_cxns(session.sessiondir, script_dir, params)

    for i in range(2, nodes+1):
        i_services_str = services_str
        # Check if src or sink			
        if has_iperf_src(i, iperf_cxns):
            i_services_str += "|IPerfSrc"
        if has_iperf_dest(i, iperf_cxns):
            i_services_str += "|IPerfSink"

        if placements:
            pos = placements[i]
        else:
            raise Exception("TODO")

        create_node(i, session, "%s"%i_services_str, wlan1, pos)
		
    print placements

    session.node_count="%d"%(nodes)
    session.instantiate()

    if params['emaneMobility'] and params['emaneModel'] == "CommEffect":
        loss = 0.0
        packet_losses = [(src, dest, loss) for src in range(2, nodes+1) for dest in range(2,nodes+1) if src != dest]
        roof_to_nem = dict((n,n) for n in range(2, nodes+1))
        print 'packet_losses=%s'%str(packet_losses)
        print 'roof_to_nem=%s'%str(roof_to_nem)

        publish_commeffects(session, packet_losses, roof_to_nem, nem_offset=1, verbose=True)
        time.sleep(5)
        publish_commeffects(session, packet_losses, roof_to_nem, nem_offset=1, verbose=True)
        time.sleep(5)
        publish_commeffects(session, packet_losses, roof_to_nem, nem_offset=1, verbose=True)
        time.sleep(5)

    time.sleep(1000000)

def start_query_sink_display(logfile, logdir, params):

    if params['query'] == 'fr':   
        args = ['java', 'FaceRecognitionDemo']
        cwd = script_dir 
    elif params['query'] == 'heatMap':   
        args = ['java', 'AcitaDemo']
        cwd = script_dir + '/heatMap'
    else: return None

    #os.mkdir(logdir)
    with open(logdir + "/" + logfile, 'w') as log:
        p = subprocess.Popen(args, stdout=log, cwd=cwd, stderr=subprocess.STDOUT, env=os.environ.copy())

    return p

def get_repo_dir():
    regex = re.compile('(.*)/seep-system')
    return re.search(regex, script_dir).groups()[0]

def record_emanesh_tables(sessiondir, nodes, params):
    for node in nodes:
        args = ['/usr/sbin/vcmd', '-c', '%s/n%d'%(sessiondir, node), './record-emane-tables.sh', str(node)]
        with open('%s/n%d.conf/emane-tables-n%d.txt'%(sessiondir, node, node), 'w') as log:
            p = subprocess.Popen(args, stdout=log, cwd='%s/n%d.conf'%(sessiondir,node), stderr=subprocess.STDOUT, env=os.environ.copy())
            p.wait()

def core_cleanup():
    p = subprocess.Popen(['/usr/sbin/core-cleanup']) 
    p.wait()

if __name__ == "__main__" or __name__ == "__builtin__":
    print 'Hello world'
    parser = argparse.ArgumentParser(description='Run several frontier experiments on CORE')
    parser.add_argument('--k', dest='k', default='2', help='replication factors (2)')
    parser.add_argument('--h', dest='h', default='2', help='chain length (2)')
    parser.add_argument('--x', dest='x', default='1200', help='Grid x dimension (1200)')
    parser.add_argument('--y', dest='y', default='1200', help='Grid y dimension (1200)')
    parser.add_argument('--duration', dest='duration', default='100000', help='Mobility params duration')
    parser.add_argument('--query', dest='query', default='chain', help='query type: (chain), join')
    parser.add_argument('--sources', dest='sources', default='1', help='Sources')
    parser.add_argument('--sinks', dest='sinks', default='1', help='Sinks (non-replicated)')
    parser.add_argument('--fanin', dest='fanin', default='2', help='Join fan-in')
    parser.add_argument('--pausetime', dest='pt', default='5.0', help='pause time (5.0)')
    parser.add_argument('--sessions', dest='sessions', default='1', help='number of sessions to run')
    parser.add_argument('--specific', dest='specific', default=False, action='store_true', help='Run a specific session')
    parser.add_argument('--plotOnly', dest='plot_time_str', default=None, help='time_str of run to plot (hh-mm-DDDddmmyy)[None]')
    parser.add_argument('--nodes', dest='nodes', default='10', help='Total number of core nodes in network')
    parser.add_argument('--model', dest='model', default="Emane", help='Wireless model (Basic, Emane)')
    parser.add_argument('--routing', dest='routing', default='OLSRETX',
            help='Net layer routing alg (OLSR, OLSRETX, OSPFv3MDR)')
    parser.add_argument('--preserve', dest='preserve', default=False, action='store_true', help='Preserve session directories')
    parser.add_argument('--saveconfig', dest='saveconfig', default=False, action='store_true', help='Export the session configuration to an XML file')
    parser.add_argument('--constraints', dest='constraints', default='', help='Export the session configuration to an XML file')
    parser.add_argument('--placement', dest='placement', default='', help='Explicit static topology to use for all sessions')
    parser.add_argument('--iperf', dest='iperf', default=False, action='store_true', help='Do an iperf test')
    parser.add_argument('--iperfcxns', dest='iperfcxns', default=None, help='Do an iperf test')
    parser.add_argument('--scaleSinks', dest='scale_sinks', default=False, action='store_true', help='Replicate sinks k times')
    parser.add_argument('--quagga', dest='quagga', default=False, action='store_true', help='Start quagga services (zebra, vtysh)')
    parser.add_argument('--pcap', dest='pcap', default=False, action='store_true', help='Start pcap service for workers.')
    parser.add_argument('--emanestats', dest='emanestats', default=False, action='store_true', help='Start emanestats service on master')
    parser.add_argument('--dstat', dest='dstat', default=False, action='store_true', help='Start dstat service on master.')
    parser.add_argument('--duplex', dest='duplex', default=False, action='store_true', help='Send in both directions for iperf tests')
    parser.add_argument('--verbose', dest='verbose', action='store_true', default=False, help='Verbose core logging')
    parser.add_argument('--sinkDisplay', dest='sink_display', default=False, action='store_true', help='Start a sink display for query output')
    parser.add_argument('--gui', dest='gui', default=False, action='store_true', help='Show placements in core GUI')
    parser.add_argument('--slave', dest='slave', default=None, help='Hostname of slave')
    parser.add_argument('--emaneMobility', dest='emane_mobility', default=False, action='store_true', help='Use emane location events for mobility (instead of ns2)')
    parser.add_argument('--xyScale', dest='xy_scale', default=None, help='Scale factor for each (x,y) coordinate (static placement only)')
    parser.add_argument('--noiseNodes', dest='noise_nodes', default=0, help='Number of rf noise sources')
    parser.add_argument('--roofnet', dest='roofnet', default=False, action='store_true', help='Use roofnet placements and packet losses')
    parser.add_argument('--emaneModel', dest='emane_model', default='Ieee80211abg', help='Emane model to use (if using emane)')
    parser.add_argument('--txRateMode', dest='txratemode', default='4', help='Emane 802.11 transmission rate mode (4=11Mb/s, 12=54Mb/s)')
    args=parser.parse_args()

    k=int(args.k)
    pt=float(args.pt)
    params = {'nodes':int(args.nodes)}
    if args.model: params['model']=args.model
    params['net-routing']=args.routing
    params['specific']=args.specific
    params['preserve']=args.preserve
    params['h']=int(args.h)
    params['x']=int(args.x)
    params['y']=int(args.y)
    params['duration']=args.duration
    params['query']=args.query
    params['saveconfig']=args.saveconfig
    params['constraints']=args.constraints
    params['placement']=args.placement
    params['sources']=args.sources
    params['sinks']=args.sinks
    params['fanin']=args.fanin
    params['iperf']=args.iperf
    params['iperfcxns']=args.iperfcxns
    params['pyScaleOutSinks']=args.scale_sinks
    params['scaleOutSinks']=pybool_to_javastr(args.scale_sinks)
    params['quagga']=args.quagga
    params['pcap']=args.pcap
    params['emanestats']=args.emanestats
    params['dstat']=args.dstat
    params['duplex']=args.duplex
    params['sinkDisplay']=args.sink_display
    params['enableSinkDisplay']=pybool_to_javastr(args.sink_display)
    params['enableGUI']= "true" if args.gui else "false"
    params['slave']= args.slave 
    params['verbose']= args.verbose 
    params['emaneMobility']= args.emane_mobility
    params['xyScale'] = args.xy_scale
    params['noiseNodes'] = int(args.noise_nodes)
    params['roofnet'] = args.roofnet
    params['emaneModel'] = args.emane_model
    params['txratemode'] = args.txratemode
	
    #if args.verbose: params['verbose']='true'

    sessions = int(args.sessions)
    session_ids = [sessions] if args.specific else range(0,sessions)
    if args.plot_time_str:
        time_str = args.plot_time_str
        regen_sessions(time_str)
    else:
        time_str = time.strftime('%H-%M-%S-%a%d%m%y')
        run_sessions(time_str, k, pt, session_ids,params)
