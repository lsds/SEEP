#!/usr/bin/python
import networkx as nx
import glob as glob
import argparse
import matplotlib.pyplot as plt
import pprint as pp

def main(links_dir):
    adjacency_matrix = read_links(links_dir)
    g = build_graph(adjacency_matrix)
    record_degrees(g, links_dir)
    plot_degrees(g, links_dir)

def read_links(links_dir):
    # placements dict of the form nid -> (x,y)
    files = glob.glob(links_dir + "/links*.txt")
    adjacency_matrix = {}
    for f in files:
        with open(f, 'r') as links:
            for line in links:
                [src, dest] = map(lambda l : l.strip(), line.split(' '))
                if src in adjacency_matrix:
                    adjacency_matrix[src].add(dest)
                else: 
                    adjacency_matrix[src] = set([dest])

    return adjacency_matrix

def build_graph(adjacency_matrix):
    # placements dict of the form nid -> (x,y)
    g = nx.Graph()
    g.add_nodes_from(adjacency_matrix.keys())

    for src in adjacency_matrix.keys():
        for dest in adjacency_matrix[src]:
            if src != dest: 
                g.add_edge(src, dest)
    return g

def record_degrees(g, links_dir):
    with open(links_dir + "/degrees.txt", 'w') as f:
        print('Graph degree = %s'%g.degree())
        pp.pprint(g.degree(), f)

def plot_degrees(g, links_dir):
    degree_sequence=sorted(nx.degree(g).values(),reverse=True) # degree sequence
    #print "Degree sequence", degree_sequence
    dmax=max(degree_sequence)

    plt.loglog(degree_sequence,'b-',marker='o')
    plt.title("Degree rank plot")
    plt.ylabel("degree")
    plt.xlabel("rank")

    # draw graph in inset
    plt.axes([0.45,0.45,0.45,0.45])
    gcc=sorted(nx.connected_component_subgraphs(g), key = len, reverse=True)[0]
    pos=nx.spring_layout(gcc)
    plt.axis('off')
    nx.draw_networkx_nodes(gcc,pos,node_size=20)
    nx.draw_networkx_edges(gcc,pos,alpha=0.4)

    plt.savefig("%s/degree_histogram.png"%links_dir)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Analyse net topology')
    parser.add_argument('--linksDir', dest='links_dir', help='relative path to links dir')
    args=parser.parse_args()

    main(args.links_dir)
