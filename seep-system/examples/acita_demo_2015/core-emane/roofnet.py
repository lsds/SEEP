from core.pycore import Session 
from emanesh.events import EventService, CommEffectEvent

def parse_roofnet_locations():
    loc_file = '../resources/roofnet/roofnet-sigcomm04/node_coordinates_06060624xx'
    locs = []
    with open(loc_file, 'r') as lf:
        for line in lf:
            [nodeid, lat, lon] = line.split(',')
            (x,y,zone_num,zone_letter) = utm.from_latlon(float(lat), float(lon))
            locs.append((int(nodeid), x, y)) 

    return locs

def parse_roofnet_packetloss():
    summary_file =  '../resources/roofnet/roofnet-sigcomm04/summaries_06060624xx' 
    exp_11M = 0606062403

    packet_losses = []
    with open(summary_file, 'r') as sf:
        for line in sf:
            [exp_id,link_test,test_phase,src,dst,delivery_ratio,signal,noise] = line.split(',')
            if int(exp_id) == exp_11M:
                packet_losses.append((int(src), int(dst), 1 / float(delivery_ratio)))
            

    return packet_losses

def publish_commeffects(session, packet_losses, roof_to_nem):
    for (src, dest, ploss) in packet_losses:
        publish_commeffect(session, roof_to_nem[src], roof_to_nem[dest], ploss)

def publish_commeffect(session, src, dest, packet_loss, latency=0.01, jitter=0.001, duplicate=0, unicast=11534336, broadcast=11534336, verbose=False):  
    ce = CommEffectEvent()

    if verbose: print 'Publishing commeffect event: src=%s,dest=%s,packet_loss=%s'%(str(src),str(dest),str(packet_loss))
    ce.append(nemid=src, latency=latency, jitter=jitter, loss=int(100*packet_loss), duplicate=duplicate, unicast=unicast, broadcast=broadcast)

    #session.emane.service.publish(0, ce)
    session.emane.service.publish(dest, ce)

