import os,math
import networkx as nx

from util import chmod_dir

def create_static_routes(placements, tx_range, sessiondir):
    g = gen_topology(placements, tx_range)
    scripts = gen_scripts(placements.keys(), g)
    write_scripts(sessiondir, scripts)

def gen_topology(placements, tx_range):
    # placements dict of the form nid -> (x,y)
    g = nx.Graph()
    g.add_nodes_from(placements.keys())

    for src in placements.keys():
        for dest in placements.keys():
            if src != dest and dist(placements[src], placements[dest]) <= tx_range:
                g.add_edge(src, dest)

    return g

def dist(src_xy, dest_xy):
    return math.sqrt((src_xy[0] - dest_xy[0])**2 + (src_xy[1] - dest_xy[1])**2)

def gen_script(nid, g):
    paths = nx.shortest_path(g, nid)

    # First get gateways
    gws = [dest for dest in paths if len(paths[dest]) == 2]
    
    inf = "eth0"
    gw_cmds = ["ip route add %s dev %s metric 1"%(nid2ip(gw), inf) for gw in gws]

    # Next get multihop destinations
    multihop_dests = [(dest, paths[dest][1], len(paths[dest])-1) for dest in paths if len(paths[dest]) > 2]

    multihop_cmds = ["ip route add %s/32 via %s dev %s metric %d"%(nid2ip(dest),nid2ip(gw),inf, metric) for (dest, gw, metric) in multihop_dests]
    
    script = "#!/bin/sh\n"
    script += "# auto-generated script for fixed routes\n"
    script += "\n".join(gw_cmds) + "\n"
    script += "\n".join(multihop_cmds)
    return script

def gen_scripts(nids, g):
    result = {}
    for nid in nids: result[nid] = gen_script(nid, g)
    return result

def write_scripts(sessiondir, scripts):
    os.mkdir(sessiondir + "/fixed_routes")
    for nid in scripts:
        script_path = "%s/fixed_routes/fixed_routes_n%d.sh"%(sessiondir, nid)
        with open(script_path, 'w') as script:
            script.write(scripts[nid])
            os.chmod(script_path, 0777)

def nid2ip(nid):
    return "10.0.0.%d"%(nid-1)



