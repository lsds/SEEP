#!/usr/bin/python
import time,argparse,glob

def record_net_rates(sessiondir, t_start, t_end):
    for raw_name in glob.glob("%s/net-util/*net-util.txt"%sessiondir):
        rates_out = raw_name.replace('net-util.txt', 'net-rates.txt')
        with open(raw_name, 'r') as rf:
            raw_lines = rf.readlines()
            (t, bytes_rx, bytes_tx) = parse_line(raw_lines[0])
            with open(rates_out, 'w') as ro:
                ro.write('#t1 t2 bytes_rx bytes_tx\n')
                for line in raw_lines[1:]:
                    (t2, bytes_rx2, bytes_tx2) = parse_line(line)
                    if include_interval(t, t2, t_start, t_end):
                        ro.write('%d %d %d %d\n'%(t, t2, bytes_rx2 - bytes_rx, bytes_tx2 - bytes_tx))
                    t = t2
                    bytes_rx = bytes_rx2
                    bytes_tx = bytes_tx2

def parse_line(line):
    splits = line.strip().split(' ')
    t = int(splits[0])
    rx_bytes = int(splits[2])
    tx_bytes = int(splits[10])
    return (t, rx_bytes, tx_bytes)

def include_interval(t1, t2, t_start, t_end):
    return (not t_start or t_start <= t1) and (not t_end or t_end >= t2) 


if __name__ == "__main__" or __name__ == "__builtin__":
    parser = argparse.ArgumentParser(description='Analyse network usage of nodes')
    parser.add_argument('--expDir', dest='exp_dir', help='Session directory')
    parser.add_argument('--tStart', dest='t_start', default=None, help='Experiment start (ms since epoch)')
    parser.add_argument('--tEnd', dest='t_end', default=None, help='Experiment end (ms since epoch)')
    args=parser.parse_args()
   
    record_net_rates(args.exp_dir, int(args.t_start) if args.t_start else None, int(args.t_end) if args.t_end else None)
