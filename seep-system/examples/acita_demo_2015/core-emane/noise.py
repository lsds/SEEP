from core import pycore
from core.api import coreapi
from core.emane.rfpipe import EmaneRfPipeModel

from core_msg_util import *

def create_noise_net(session, noise_net_id, distributed, verbose=False):
    wlan_noise = session.addobj(cls = pycore.nodes.EmaneNode, name = "wlan1", objid=noise_net_id, verbose=verbose)
    wlan_noise.setposition(x=50,y=80)
    if distributed: session.broker.handlerawmsg(wlan_noise.tonodemsg(flags=coreapi.CORE_API_ADD_FLAG|coreapi.CORE_API_STR_FLAG))
    names = EmaneRfPipeModel.getnames()
    values = list(EmaneRfPipeModel.getdefaultvalues())
    values[ names.index('datarate') ] = '1M'
    values[ names.index('flowcontrolenable') ] = 'on'
    values[ names.index('flowcontroltokens') ] = '10'
    #TODO: Tweak this, only seem to apply if EventService in globals()?
    values[ names.index('pcrcurveuri') ] = '/usr/share/emane/xml/models/mac/rfpipe/rfpipepcr.xml'
    values[ names.index('jitter') ] = '0.0'
    values[ names.index('delay') ] = '0.0'
    if distributed:
        typeflags = coreapi.CONF_TYPE_FLAGS_UPDATE
        msg = EmaneRfPipeModel.toconfmsg(flags=0, nodenum=wlan_noise.objid,
     typeflags=typeflags, values=values)
        session.broker.handlerawmsg(msg)

    session.emane.setconfig(wlan_noise.objid, EmaneRfPipeModel._name, values)

    if distributed:
        #No idea why I need to call this again here with a None node but seems I have to. 
        conf_msg = raw_emane_global_conf_msg(None)
        session.broker.handlerawmsg(conf_msg)
        session.confobj("emane", session, to_msg(conf_msg))

    return wlan_noise
