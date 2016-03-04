from emanesh.events import EventService, LocationEvent

def publish_loc(n, x, y, z, session, verbose=False):
    loc = LocationEvent() 
    lat,lon,alt = session.location.getgeo(x, y, z)

    if verbose: print 'Publishing location event: lat=%s,lon=%s,alt=%s'%(str(lat),str(lon),str(alt))
    loc.append(n, latitude=lat,longitude=lon, altitude=alt)

    session.emane.service.publish(0, loc)
