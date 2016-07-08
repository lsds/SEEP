#!/usr/bin/python
import networkx as nx
import glob as glob
import argparse

def main(links_dir):
    adjacency_matrix = read_links(links_dir)
    analyse_graph(adjacency_matrix)

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

def analyse_graph(adjacency_matrix):
    # placements dict of the form nid -> (x,y)
    g = nx.Graph()
    g.add_nodes_from(adjacency_matrix.keys())

    for src in adjacency_matrix.keys():
        for dest in adjacency_matrix[src]:
            if src != dest: 
                g.add_edge(src, dest)


    print 'Graph degree = %s'%g.degree()
    return g


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Analyse net topology')
    parser.add_argument('--linksDir', dest='links_dir', help='relative path to links dir')
    args=parser.parse_args()

    main(args.links_dir)
