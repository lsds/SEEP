import os

from core.service import CoreService, addservice

class Dstat(CoreService):
    ''' Dstat service for logging packets.
    ''' 
    #DUMPOPTS="-c -C 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31 -T -g -r -d --top-io"
    _name = "dstat"
    _group = "SEEP"
    _depends = ()
    _configs = ("dstat-trace.sh", )
    _startindex = 81
    _startup = ("sh dstat-trace.sh start",)
    _shutdown = ("sh dstat-trace.sh stop",)
    #_validate = ("pidof dstat",)
    _validate = ()
    _meta = "records cpu utilization statistics"

    @classmethod
    def generateconfig(cls, node, filename, services):
        ''' Generate a startpcap.sh traffic logging script.
        '''
        cfg = """
#!/bin/sh
# set dstat options here (see 'man dstat' for help)
#DUMPOPTS="-c -C 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24 -T -g -r -d --top-io"
DUMPOPTS="-c -C 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31 -T -g -r -d --top-io"

if [ "x$1" = "xstart" ]; then

"""
        redir = "< /dev/null"
        dstat_processors = ",".join(map(str, range(3,64,4)))
        #cfg += "taskset -c 25-31 dstat ${DUMPOPTS} --output cpu-util.csv %s &\n"%(redir)
        #cfg += "taskset -c %s dstat ${DUMPOPTS} --output cpu-util.csv %s &\n"%(dstat_processors,redir)
        cfg += "dstat ${DUMPOPTS} --output cpu-util.csv %s &\n"%(redir)
        cfg += """

elif [ "x$1" = "xstop" ]; then
    mkdir -p ${SESSION_DIR}/dstat
    mv cpu-util.csv ${SESSION_DIR}/dstat
fi;
"""
        return cfg

addservice(Dstat)
