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

    translated_locs = map(lambda(n, x, y): (n, x - mins[0] + 10, y - mins[1] + 10), locs)
    print 'Translated roofnet locs from: %s\nTo: %s'%(str(locs), str(translated_locs))
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

def publish_pathlosses(session, packet_losses, roof_to_nem, nem_offset=2, txratemode=4, verbose=False):

    for (src, dest, pkt_loss) in packet_losses: 
        #pathloss = compute_pathloss(pkt_loss,txratemode=txratemode)
        #pathloss = compute_pathloss(pkt_loss,txpower=0.0,txratemode=txratemode)
        #pathloss = compute_pathloss(pkt_loss,txpower=-2.5,txratemode=txratemode)
        #pathloss = compute_pathloss(pkt_loss,txpower=-4.0,txratemode=txratemode)
        #pathloss = compute_pathloss(pkt_loss,txpower=-5.0,txratemode=txratemode)
        #pathloss = compute_pathloss(pkt_loss,txpower=-10.0,txratemode=txratemode)
        pathloss = compute_pathloss(pkt_loss,txpower=-10.0,txratemode=4)
        publish_pathloss(session, roof_to_nem[src]-nem_offset, roof_to_nem[dest]-nem_offset, pathloss, verbose=verbose) 

def publish_pathloss(session, src_nem, dest_nem, pathloss, verbose=False):
    ce = PathlossEvent()

    ce.append(dest_nem, forward=pathloss)
    if verbose: print 'Publishing to nem %d with event nem %d, ce=%s'%(src_nem, dest_nem, str(ce))

    session.emane.service.publish(src_nem, ce)

def compute_pathloss(loss, txpower=-10.0, txratemode=4):
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
    <!-- 54Mpbs -->
    <datarate index="12">
      <row sinr="17.0"  por="0.0"/>
      <row sinr="18.0"  por="0.2"/>
      <row sinr="19.0"  por="5.7"/>
      <row sinr="20.0"  por="32.4"/>
      <row sinr="21.0"  por="71.3"/>
      <row sinr="22.0"  por="92.4"/>
      <row sinr="23.0"  por="99.9"/>
      <row sinr="24.0"  por="100.0"/>
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


    However, just using the same loss ratio for each link with 54mb is a bit strange.
    Effectively you are saying the loss ratio is unchanged at higher datarates. 
    Also, you should really have the same level of interference, since according to EMANE's
    probabilistic model, that only really depends on the number of neighbours each node observes, 
    which in this case is completely dictated by the loss ratio, and is in fact the same as the
    11Mb case using the current approach.
    Alternatively, given a fixed pathloss, you could either reduce the txPower that's actually used
    at runtime in order to emulate a sparser network. This would however reduce the overall throughput.
   
    pathloss = txPower - SINR + 100
    SINR = txPower - pathloss + 100 
    So could just use a reduced txPower at runtime but keep pathloss fixed at a higher ratio.
    Similarly, to get everything to work for 54MB we might need to increase the runtime txpower, but 
    still use the 11MB pcr curves to compute the path loss? Presumably if the actual txpower used
    was higher, then the pathloss should have been higher? Right. The point is that you should have
    the same relative path loss for each link as you scale the txPower up and down.
 
    If you switch to 54MB, will the corresponding pcr curves be used for anything now though?
    
    
    """

    noise_figure = 4
    bandwidth = 10000000
    rx_sensitivity = -174 + noise_figure + 10 * math.log10(bandwidth)
    exp_pkt_size=1500.0
    pcr_pkt_size=128.0

    reverse_pcr_curve_11 = [(0.0, 1.0), (0.2, 2.0), (8.9,3.0), (45.8,4.0), (82.5,
        5.0), (96.7, 6.0), (99.6, 7.0), (100.0, 8.0)] 
	
    reverse_pcr_curve_54 = [(0.0, 17.0), (0.2, 18.0), (5.7,19.0), (32.4,20.0), (71.3,
        21.0), (92.4, 22.0), (99.9, 23.0), (100.0, 24.0)] 

    if txratemode == 12:
        reverse_pcr_curve = reverse_pcr_curve_54
    elif txratemode == 4:
        reverse_pcr_curve = reverse_pcr_curve_11
    else: raise Exception("Unknown txratemode: %s"%str(txratemode)) 

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
        
        sinr = get_sinr(por0, reverse_pcr_curve)
        assert sinr >= reverse_pcr_curve[0][1] and sinr <= reverse_pcr_curve[len(reverse_pcr_curve)-1][1]
        
        rxpower = sinr + rx_sensitivity
        pathloss = txpower - rxpower
        return pathloss

def get_sinr(por, reverse_pcr_curve):


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



