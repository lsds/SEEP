import shutil

def read_iperf_cxns(script_dir, params):
    cxns = []
    with open("%s/static/%s"%(script_dir, params['iperfcxns']), 'r') as cxns_file:
        for line in cxns_file:
            cxns.append(line.split(","))

    return cxns 

def copy_iperf_cxns(sessiondir, script_dir, params):
    shutil.copy("%s/static/%s"%(script_dir, params['iperfcxns']), '%s/iperf_connections.txt'%sessiondir)

def has_iperf_src(node, cxns):
    for [src, dest] in cxns:
        if int(src) == int(node):
            return True

def has_iperf_dest(node, cxns):
    for [src, dest] in cxns:
        if int(dest) == int(node):
            return True
