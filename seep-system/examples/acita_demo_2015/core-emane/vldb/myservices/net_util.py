import os

from core.service import CoreService, addservice

class NetUtil(CoreService):
    ''' Record emane statistics.
    '''
    _name = "NetUtil"
    _group = "SEEP"
    _depends = ()
    _configs = ("net-util-run.sh", )
    _startindex = 81
    _startup = ("sh net-util-run.sh start",)
    _shutdown = ("sh net-util-run.sh stop",)
    _validate = ()
    _meta = "records net utilisation statistics"

    @classmethod
    def generateconfig(cls, node, filename, services):
        ''' Generate an net-util-run.sh logging script.
        '''
        cfg = """
#!/bin/sh

if [ "x$1" = "xstart" ]; then

"""
        repo_dir = "%s/../../../../../.."%os.path.dirname(os.path.realpath(__file__))
        seep_example_dir = "%s/seep-system/examples/acita_demo_2015"%repo_dir

        cfg += "cp %s/core-emane/vldb/config/net-util.sh .\n"%(seep_example_dir)
        cfg += "./net-util.sh &\n"
        cfg += """

elif [ "x$1" = "xstop" ]; then
    mkdir -p ${SESSION_DIR}/net-util
    mv *net-util.txt ${SESSION_DIR}/net-util
fi;
"""
        return cfg

addservice(NetUtil)
