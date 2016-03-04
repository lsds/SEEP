from core.mobility import Ns2ScriptedMobility 
from emanesh.events import EventService, LocationEvent

def publish_loc(n, x, y, z, session, verbose=False):
    loc = LocationEvent() 
    lat,lon,alt = session.location.getgeo(x, y, z)

    if verbose: print 'Publishing location event: lat=%s,lon=%s,alt=%s'%(str(lat),str(lon),str(alt))
    loc.append(n, latitude=lat,longitude=lon, altitude=alt)

    session.emane.service.publish(0, loc)

class EmaneNs2ScriptedMobility(Ns2ScriptedMobility):
    ''' Handles the ns-2 script format, generated by scengen/setdest or
        BonnMotion, but converts all position updates into Emane location
        events.
    '''
    _name = "emaneNs2script"
    self.nem_offset = 2


    def __init__(self, session, objid, verbose = False, values = None):
        ''' 
        '''
        super(EmaneNs2ScriptedMobility, self).__init__(session = session, objid = objid,
                                                  verbose = verbose, values = values)

    def setnodeposition(self, node, x, y, z):
        ''' Helper to move a node, notify any GUI (connected session handlers),
            without invoking the interface poshook callback that may perform
            range calculation.
        '''
        # this would cause PyCoreNetIf.poshook() callback (range calculation)
        #node.setposition(x, y, z)

        #Slaves won't call this on their node, but might not matter if all
        #SEEP nodes are on the master?
        node.position.set(x, y, z)  
        #msg = node.tonodemsg(flags=0)
        self.writenodeposition(node, x, y, z)
        publish_loc(node.objid-self.nem_offset, x, y, z, session)
        #self.session.broadcastraw(None, msg)
        #self.session.sdt.updatenode(node.objid, flags=0, x=x, y=y, z=z)
