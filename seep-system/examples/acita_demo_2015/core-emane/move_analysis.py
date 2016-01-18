#!/usr/bin/python
import time,argparse, math

def main(nodes, logdir):
    all_positions = {}
    for node in nodes:
        posfilepath = get_posfilepath(node, logdir)
        all_positions[node] = read_positions(posfilepath)
        print 'Read %d positions for node %d'%(len(all_positions[node]), node)

    all_distances = get_all_distances(all_positions)

    for node in nodes:
        record_node_distances(node, all_distances[node], logdir)
        #plot_node_distances(distfilepath)

def get_posfilepath(node, logdir):
    return '%s/n%d.xyz'%(logdir, node)

def get_distfilepath(node, other, logdir):
    return '%s/n%dn%d.dist'%(logdir, node, other)

def get_all_distances(all_positions):
    all_distances = {}
    for src in all_positions:
        src_distances = {}
        for other in all_positions:
            if other == src: continue
            merged_positions = merge_positions(src, other, all_positions[src], all_positions[other])
            sorted_positions = sorted(merged_positions)
            
            src_pos = None
            other_pos = None
            distances = []
            for (t, x, y, z, node) in sorted_positions:
                if node == src:
                    src_pos = (x,y,z)
                    if other_pos:
                        distances.append((t, distance(src_pos, other_pos)))
                elif node == other:
                    other_pos = (x,y,z)
                    if src_pos:
                        distances.append((t, distance(src_pos, other_pos)))
                else: raise Exception("Logic error")

            src_distances[other] = distances 

        all_distances[src] = src_distances

    return all_distances

def merge_positions(src, other, src_positions, other_positions):
    result = []
    for (t, x, y, z) in src_positions:
        result.append((t, x, y, z, src))
    for (t, x, y, z) in other_positions:
        result.append((t, x, y, z, other))
    return result

def distance(src_xyz, dest_xyz):
    #ignore z for now.
    src_x = src_xyz[0]
    src_y = src_xyz[1]
    dest_x = dest_xyz[0]
    dest_y = dest_xyz[1]

    dist = math.sqrt((dest_x - src_x)**2 + (dest_y - src_y)**2)
    #print 'dist from %s to %s is %s'%(src_xyz, dest_xyz, dist)
    return dist


def read_positions(posfilepath):
    positions = []
    with open(posfilepath, 'r') as posfile:
        for line in posfile:
            els = line.strip().split(' ')
            positions.append((float(els[0]), float(els[1]), float(els[2]), float(els[3])))
    return positions

def record_node_distances(node, node_distances, logdir):
    for other in node_distances:
        distfilepath = get_distfilepath(node, other, logdir)
        with open(distfilepath, 'w') as distfile:
            for (t, distance) in node_distances[other]:
                distfile.write('%s %s %s %.1f\n'%(str(t), str(node), str(other), distance))


if __name__ == "__main__" or __name__ == "__builtin__":
    parser = argparse.ArgumentParser(description='Plot distances between nodes over time')
    parser.add_argument('--nodes', dest='nodes', default='27', help='Nodes to analyse')
    parser.add_argument('--expDir', dest='exp_dir', default=None, help='Directory containing position traces')
    args=parser.parse_args()
   
    nodes = map(int, args.nodes.split(','))
    if len(nodes) == 1:
        nodes = range(3, nodes[0])

    print 'Analysing nodes: %s'%(str(nodes))
    main(nodes, args.exp_dir)


