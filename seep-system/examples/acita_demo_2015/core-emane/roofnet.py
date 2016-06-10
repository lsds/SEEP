import os,utm

from core.pycore import Session 
from emanesh.events import EventService, CommEffectEvent

script_dir = os.path.dirname(os.path.realpath(__file__))

def parse_roofnet_locations():
    loc_file = '%s/../resources/roofnet/roofnet-sigcomm04/node_coordinates_06060624xx'%script_dir
    locs = []
    with open(loc_file, 'r') as lf:
        for line in lf:
            [nodeid, lat, lon] = line.split(',')
            (x,y,zone_num,zone_letter) = utm.from_latlon(float(lat), float(lon))
            locs.append((int(nodeid), x, y)) 

    return locs

def parse_roofnet_packetloss(roof_to_nem=None):
    summary_file =  '%s/../resources/roofnet/roofnet-sigcomm04/summaries_06060624xx'%script_dir 
    exp_11M = 606062403

    unique_nodes = set([]) 
    packet_losses = []
    with open(summary_file, 'r') as sf:
        for line in sf:
            [exp_id,link_test,test_phase,src,dst,delivery_ratio,signal,noise] = line.split(',')

            if roof_to_nem and not int(src) in roof_to_nem or not int(dst) in roof_to_nem: continue
            unique_nodes.add(src)
            unique_nodes.add(dst)
            if int(exp_id) == exp_11M:
                packet_losses.append((int(src), int(dst), 1.0 - float(delivery_ratio)))

    print 'Unique nodes len=%d, ids=%s'%(len(unique_nodes), str(unique_nodes))
    return packet_losses

def publish_commeffects(session, packet_losses, roof_to_nem, nem_offset=2, verbose=False):
    if verbose: print 'Publishing %d roofnet packet loss events'%len(packet_losses)
    for (src, dest, ploss) in packet_losses:
        if verbose: print 'Publishing packet loss event %d %d %s'%(roof_to_nem[src], roof_to_nem[dest], str(ploss))
        publish_commeffect(session, roof_to_nem[src], roof_to_nem[dest], ploss, nem_offset=nem_offset, verbose=verbose)

def publish_commeffect(session, src, dest, packet_loss, latency=0.001, jitter=0.001, duplicate=0, unicast=11534336, broadcast=11534336, nem_offset=2, verbose=False):  
    ce = CommEffectEvent()

    event_nem = src-nem_offset
    ce.append(event_nem, latency=latency, jitter=jitter, loss=int(100*packet_loss), duplicate=duplicate, unicast=unicast, broadcast=broadcast)
    #ce.append(event_nem, latency=latency, jitter=jitter, loss=5, duplicate=duplicate, unicast=unicast, broadcast=broadcast)

    publish_nem = dest-nem_offset
    if verbose: print 'Publishing to nem %d with event nem %d, ce=%s'%(publish_nem, event_nem, str(ce))

    session.emane.service.publish(publish_nem, ce)



