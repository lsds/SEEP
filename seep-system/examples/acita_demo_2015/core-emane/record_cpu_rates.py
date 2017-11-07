#!/usr/bin/python
import time,argparse,glob

def record_cpu_rates(sessiondir, t_start, t_end):
    """
    u(t) = (c_busy(t) - c_busy(t-1)) / (c_total(t) - c_total(t-1))
    """
    for raw_name in glob.glob("%s/cpu-util/*cpu-util.txt"%sessiondir):
        rates_out = raw_name.replace('cpu-util.txt', 'cpu-rates.txt')
        with open(raw_name, 'r') as rf:
            raw_lines = rf.readlines()
            (t, busy, total) = parse_line(raw_lines[0])
            with open(rates_out, 'w') as ro:
                ro.write('#t1 t2 u(t1t2)\n')
                for line in raw_lines[1:]:
                    (t2, busy2, total2) = parse_line(line)
                    if include_interval(t, t2, t_start, t_end):
                        u_t1t2 = float(busy2 - busy) / float(total2 - total)
                        ro.write('%d %d %.3f\n'%(t, t2, u_t1t2))
                    t = t2
                    busy = busy2
                    total = total2


def parse_line(line):
    """
    # t cpu user nice system idle iowait irq softirq steal guest guest_nice 
    # 1509981381591 cpu 512194268 902536 71123045 3104491679 30873931 1268 766257 0 0 0                                                                                                        
    c_busy(t) = c_user(t) + c_nice(t) + c_system(t)
    c_total(t) = c_busy(t) + c_idle(t)
    """
    splits = line.strip().split(' ')
    t = int(splits[0])
    user = int(splits[2])
    nice = int(splits[3])
    system = int(splits[4])
    idle = int(splits[5])
    busy = user+nice+system
    return (t, busy, busy+idle)
    #return (t, user, nice, system, idle)

def include_interval(t1, t2, t_start, t_end):
    return (not t_start or t_start <= t1) and (not t_end or t_end >= t2) 


if __name__ == "__main__" or __name__ == "__builtin__":
    parser = argparse.ArgumentParser(description='Analyse cpu usage of nodes')
    parser.add_argument('--expDir', dest='exp_dir', help='Session directory')
    parser.add_argument('--tStart', dest='t_start', default=None, help='Experiment start (ms since epoch)')
    parser.add_argument('--tEnd', dest='t_end', default=None, help='Experiment end (ms since epoch)')
    args=parser.parse_args()
   
    record_cpu_rates(args.exp_dir, int(args.t_start) if args.t_start else None, int(args.t_end) if args.t_end else None)
