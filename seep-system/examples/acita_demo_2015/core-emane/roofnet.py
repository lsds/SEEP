import os,utm,math,numpy

from core.pycore import Session 
from emanesh.events import EventService, CommEffectEvent, PathlossEvent, LocationEvent

script_dir = os.path.dirname(os.path.realpath(__file__))

def parse_roofnet_locations():
    loc_file = '%s/../resources/roofnet/roofnet-sigcomm04/node_coordinates_06060624xx'%script_dir
    locs = []
    mins = None
    with open(loc_file, 'r') as lf:
        for line in lf:
            [nodeid, lat, lon] = line.split(',')
            (x,y,zone_num,zone_letter) = utm.from_latlon(float(lat), float(lon))
            #locs.append((int(nodeid), x/10000.0, y/10000.0)) 
            locs.append((int(nodeid), x, y)) 
            if not mins: mins = [x, y]
            else: 
                if x < mins[0]: mins[0] = x
                if y < mins[1]: mins[1] = y

    translated_locs = map(lambda(n, x, y): (n, x - mins[0] + 1, y - mins[1] + 1), locs)
    print 'Translated roofnet locs from: %s\nTo: %s'%(locs, translated_locs)
    return translated_locs 

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

def publish_pathlosses(session, packet_losses, roof_to_nem, nem_offset=2, verbose=False):

    for (src, dest, pkt_loss) in packet_losses: 
        pathloss = compute_pathloss(pkt_loss)
        publish_pathloss(session, roof_to_nem[src]-nem_offset, roof_to_nem[dest]-nem_offset, pathloss, verbose=verbose) 

def publish_pathloss(session, src_nem, dest_nem, pathloss, verbose=False):
    ce = PathlossEvent()

    ce.append(dest_nem, forward=pathloss)
    if verbose: print 'Publishing to nem %d with event nem %d, ce=%s'%(src_nem, dest_nem, str(ce))

    session.emane.service.publish(src_nem, ce)

def compute_pathloss(loss, txpower=-10.0):
    """
    /usr/share/emane/xml/models/mac/ieee80211abg/ieee80211pcr.xml
    128 byte packets
    <!-- 11Mpbs -->
    <datarate index="4">
      <row sinr="1.0"   por="0.0"/>
      <row sinr="2.0"   por="0.2"/>
      <row sinr="3.0"   por="8.9"/>
      <row sinr="4.0"   por="45.8"/>
      <row sinr="5.0"   por="82.5"/>
      <row sinr="6.0"   por="96.7"/>
      <row sinr="7.0"   por="99.6"/>
      <row sinr="8.0"   por="100.0"/>
    </datarate>

    Given a probability of reception (POR, i.e. 1-loss), first compute POR0

    POR = POR0^(S1/S0)
    where:
    S0 = 128
    S1 = exp_pkt_size

    Note that if por = 0.0 then just return pathloss = txpower - rxSensitivity +
    1, since packets dropped if rxPower < rxSensitivity and so set rxPower to
    rxSensitivity-1, and hence pathloss = txpower - rxpower = txpower -
    (rxSensitivity - 1) = txpower - rxSensitivity + 1

    Next, compute the corresponding SINR for POR0 given the pcr_curve.
  
    Given the SINR, and since SINR = rxPower - rxSensitivity, we can compute rxPower as:

        rxPower = SINR + rxSensitivity 

    where:

        rxSensitivity = -174 + noiseFigure + 10log(bandwidth)

    bandwidth defined by configuration parameter bandwidth (default=1000000), noiseFigure defined
    by systemnoisefigure (default=4.0). 
    
    So by default: 
        rxSensitivity = -174 + 4 + 100 = -100
        rxPower = SINR - 100

    But:

        rxPower = txPower + txAntennaGain + rxAntennaGain - pathloss

    And so assuming 0 gain, we can compute pathloss as:
    
        pathloss = txPower - rxPower 

    """

    noise_figure = 4
    bandwidth = 10000000
    rx_sensitivity = -174 + noise_figure + 10 * math.log10(bandwidth)
    exp_pkt_size=1500.0
    pcr_pkt_size=128.0

    assert loss >= 0.0 and loss <= 1

    por = 1 - loss
    if por <= 0.0:
        return txpower - rx_sensitivity + 1
    else:
        #por0 = por

        #Packet size adjustment (por0)
        # por = por0 ^ (exp_pkt_size / pcr_pkt_size)
        # -> por0 = por ^ (pcr_pkt_size / exp_pkt_size)
        por0 = math.pow(por, pcr_pkt_size / exp_pkt_size)
        
        sinr = get_sinr(por0)
        assert sinr >= 1.0 and sinr <= 8.0
        
        rxpower = sinr + rx_sensitivity
        pathloss = txpower - rxpower
        return pathloss

def get_sinr(por):
    reverse_pcr_curve = [(0.0, 1.0), (0.2, 2.0), (8.9,3.0), (45.8,4.0), (82.5,
        5.0), (96.7, 6.0), (99.6, 7.0), (100.0, 8.0)] 

    x = map(lambda (a,b): a, reverse_pcr_curve)
    y = map(lambda (a,b): b, reverse_pcr_curve)

    return numpy.interp(por, x, y)

    """
    #TODO interpolation
    for i, (p, sinr) in enumerate(reverse_pcr_curve):
        if i+1 >= len(reverse_pcr_curve):
            return sinr
        elif por < reverse_pcr_curve[i+1][0]:
            return sinr

    assert False
    """

    
def publish_locations(session, roofnet_placements, roof_to_nem, nem_offset=2, verbose=False):
    for (n, x, y) in roofnet_placements: 
        publish_loc(roof_to_nem[n]-nem_offset, x, y, 0, session, verbose=verbose) 

def publish_loc(n, x, y, z, session, verbose=False):
    loc = LocationEvent() 
    lat,lon,alt = session.location.getgeo(x, y, z)

    if verbose: print 'Publishing location event: lat=%s,lon=%s,alt=%s'%(str(lat),str(lon),str(alt))
    loc.append(n, latitude=lat,longitude=lon, altitude=alt)

    session.emane.service.publish(0, loc)



