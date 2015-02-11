#!/usr/bin/python

import sys,os,time,re
from core import pycore
from core.mobility import BasicRangeModel


svc_dir='/data/dev/seep-github/seep-system/examples/acita_demo_2015/core-emane/vldb/myservices'
def main():

    try:
        session = pycore.Session(cfg={'custom_services_dir':svc_dir}, persistent=True)
        """
        if not add_to_server(session): 
            print 'Could not add to server'
            return
        """
        for svc in session.services.get():
            print svc._name

        #prefix = ipaddr.IPv4Prefix("10.0.0.0/32")
        #tmp.newnetif(net, ["%s/%s" % (prefix.addr(i), prefix.prefixlen)])
        # set increasing Z coordinates
        wlan1 = session.addobj(cls = pycore.nodes.WlanNode, name="wlan1",objid=1,
                verbose=True)
        wlan1.setmodel(BasicRangeModel, BasicRangeModel.getdefaultvalues())
        wlan1.setposition(x=418.0,y=258.0)

        worker_services_str = "OLSR|IPForward|MeanderWorker"
        n2 = session.addobj(cls = pycore.nodes.CoreNode, name="n2")
        session.services.addservicestonode(n2, "", worker_services_str, verbose=False)
        n2.setposition(x=346.0,y=178.0)
        n3 = session.addobj(cls = pycore.nodes.CoreNode, name="n3")
        n3.setposition(x=515.0,y=160.0)
        n4 = session.addobj(cls = pycore.nodes.CoreNode, name="n4")
        n4.setposition(x=567.0,y=244.0)
        n5 = session.addobj(cls = pycore.nodes.CoreNode, name="n5")
        n5.setposition(x=558.0,y=317.0)
        n6 = session.addobj(cls = pycore.nodes.CoreNode, name="n6")
        n6.setposition(x=458.0,y=392.0)
        n7 = session.addobj(cls = pycore.nodes.CoreNode, name="n7")
        n7.setposition(x=349.0,y=359.0)
        n8 = session.addobj(cls = pycore.nodes.CoreNode, name="n8")
        n8.setposition(x=295.0,y=290.0)
        n2.newnetif(net=wlan1, addrlist=["10.0.0.10/32"], ifindex=0)
        n3.newnetif(net=wlan1, addrlist=["10.0.0.11/32"], ifindex=0)
        n4.newnetif(net=wlan1, addrlist=["10.0.0.12/32"], ifindex=0)
        n5.newnetif(net=wlan1, addrlist=["10.0.0.13/32"], ifindex=0)
        n6.newnetif(net=wlan1, addrlist=["10.0.0.14/32"], ifindex=0)
        n7.newnetif(net=wlan1, addrlist=["10.0.0.15/32"], ifindex=0)
        n8.newnetif(net=wlan1, addrlist=["10.0.0.16/32"], ifindex=0)


        print 'Instantiating session.'
        session.instantiate()
        time.sleep(30)

    finally:
        print 'Shutting down session.'
        if session:
            session.shutdown()

def add_to_server(session):
    global server
    try:
        server.addsession(session)
        return True
    except NameError:
        print 'Name error'
        return False

if __name__ == "__main__" or __name__ == "__builtin__":
    main()
