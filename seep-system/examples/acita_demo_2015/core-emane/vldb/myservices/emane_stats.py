import os

from core.service import CoreService, addservice

class EmaneStats(CoreService):
    ''' Record emane statistics.
    '''
    _name = "EmaneStats"
    _group = "SEEP"
    _depends = ()
    _configs = ("emane-stats.sh", )
    _startindex = 81
    _startup = ("sh emane-stats.sh start",)
    _shutdown = ("sh emane-stats.sh stop",)
    _validate = ()
    _meta = "records emanesh statistics"

    @classmethod
    def generateconfig(cls, node, filename, services):
        ''' Generate an emane-stats.sh logging script.
        '''
        cfg = """
#!/bin/sh

if [ "x$1" = "xstart" ]; then

"""
        repo_dir = "%s/../../../../../.."%os.path.dirname(os.path.realpath(__file__))
        seep_example_dir = "%s/seep-system/examples/acita_demo_2015"%repo_dir

        cfg += "cp %s/core-emane/vldb/config/watch-emane-stats.sh .\n"%(seep_example_dir)
        cfg += "cp %s/core-emane/vldb/config/emane-mac-stats.txt emane-required-stats.txt\n"%(seep_example_dir)
        cfg += "cp %s/core-emane/vldb/config/record-emane-tables.sh .\n"%(seep_example_dir)
        cfg += "cp %s/core-emane/vldb/config/olsrd-get-neighbours.sh get-neighbours.sh\n"%(seep_example_dir)
        cfg += "./get-neighbours.sh %s &\n"%(node.objid)
        cfg += 'echo "Starting emane stats watcher."\n'
        emanestat_processors = ",".join(map(str, range(3,64,4)))
        #cfg += "taskset -c 25-30 ./watch-emane-stats.sh %s < /dev/null &\n"%node.name
        #cfg += "taskset -c %s ./watch-emane-stats.sh %s < /dev/null &\n"%(emanestat_processors, node.name)
        cfg += "./watch-emane-stats.sh %s < /dev/null &\n"%node.name
        cfg += """

elif [ "x$1" = "xstop" ]; then
    mkdir -p ${SESSION_DIR}/emane-stats
    mv *emane-stats.txt ${SESSION_DIR}/emane-stats
    mkdir -p ${SESSION_DIR}/links
    cp links*.txt ${SESSION_DIR}/links
fi;
"""
        return cfg

addservice(EmaneStats)
