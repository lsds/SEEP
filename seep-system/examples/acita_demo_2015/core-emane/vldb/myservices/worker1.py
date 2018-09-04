#
# CORE
# Copyright (c)2010-2012 the Boeing Company.
# See the LICENSE file included in this distribution.
#
''' Sample user-defined service.
'''

import os

from core.service import CoreService, addservice
from core.misc.ipaddr import IPv4Prefix, IPv6Prefix
from worker_cfg_builder import build_cfg

class FrontierWorker1(CoreService):
    ''' This is a sample user-defined service. 
    '''
    # a unique name is required, without spaces
    _name = "FrontierWorker1"
    # you can create your own group here
    _group = "SEEP"
    # list of other services this service depends on
    _depends = ()
    # per-node directories
    _dirs = ()
    # generated files (without a full path this file goes in the node's dir,
    #  e.g. /tmp/pycore.12345/n1.conf/)
    _configs = ('worker1.sh',)
    # this controls the starting order vs other enabled services
    _startindex = 51 
    #_starttime="10"
    # list of startup commands, also may be generated during startup
    _startup = ('sh worker1.sh',)
    # list of shutdown commands
    _shutdown = ()


    @classmethod
    def generateconfig(cls, node, filename, services):
        ''' Return a string that will be written to filename, or sent to the
            GUI for user customization.
        '''
        
        return build_cfg(1, cls, node, filename, services)
            

# this line is required to add the above class to the list of available services
addservice(FrontierWorker1)

