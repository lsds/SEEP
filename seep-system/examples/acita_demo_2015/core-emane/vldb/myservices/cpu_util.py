import os

from core.service import CoreService, addservice

class CpuUtil(CoreService):
    ''' Record emane statistics.
    '''
    _name = "CpuUtil"
    _group = "SEEP"
    _depends = ()
    _configs = ("cpu-util-run.sh", )
    _startindex = 81
    _startup = ("sh cpu-util-run.sh start",)
    _shutdown = ("sh cpu-util-run.sh stop",)
    _validate = ()
    _meta = "records cpu utilisation statistics"

    @classmethod
    def generateconfig(cls, node, filename, services):
        ''' Generate an cpu-util-run.sh logging script.
        '''
        cfg = """
#!/bin/sh

if [ "x$1" = "xstart" ]; then

"""
        repo_dir = "%s/../../../../../.."%os.path.dirname(os.path.realpath(__file__))
        seep_example_dir = "%s/seep-system/examples/acita_demo_2015"%repo_dir

        cfg += "cp %s/core-emane/vldb/config/cpu-util.sh .\n"%(seep_example_dir)
        cfg += "./cpu-util.sh &\n"
        cfg += """

elif [ "x$1" = "xstop" ]; then
    mkdir -p ${SESSION_DIR}/cpu-util
    mv *cpu-util.txt ${SESSION_DIR}/cpu-util
fi;
"""
        return cfg

addservice(CpuUtil)
