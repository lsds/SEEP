import numpy

base_power = 1.3286
mb = 1000000.0

def get_network_power_usage(node_net_rates, total_tuples, t_start, t_end):
    power_usage = {}
    node_net_rates = trim_net_intervals(node_net_rates, t_start, t_end)
    exp_interval_s = float(t_end - t_start) / 1000.0

    for node in node_net_rates:
        #power_usage[node] = base_power 
        energy = 0.0 
        for (t1, t2, rx_bytes, tx_bytes) in node_net_rates[node]:
            energy += rx_energy(t2 - t1, rx_bytes)
            energy += tx_energy(t2 - t1, tx_bytes)

        power_usage[node] = (energy / exp_interval_s)
    
    if len(node_net_rates) != len(power_usage): raise Exception("Logic error: %d != %d"%(len(node_net_rates), len(power_usage)))

    return get_total_power_usage(power_usage, total_tuples)

def get_cpu_power_usage(node_cpu_rates, total_tuples, t_start, t_end):
    power_usage = {}
    node_cpu_rates = trim_cpu_intervals(node_cpu_rates, t_start, t_end)
    exp_interval_s = float(t_end - t_start) / 1000.0

    for node in node_cpu_rates:
        energy = 0.0
        for (t1, t2, cpu_util) in node_cpu_rates[node]:
            energy += cpu_energy(t2 - t1, cpu_util)

        power_usage[node] = (energy / exp_interval_s)

    return get_total_power_usage(power_usage, total_tuples)

def get_ops_power_usage(op_utils, total_tuples, t_start, t_end):
    power_usage = {}
    op_utils = trim_op_intervals(op_utils, t_start, t_end)
    exp_interval_s = float(t_end - t_start) / 1000.0

    for op_id in op_utils:
        energy = 0.0
        for (t, interval_util, cum_util, interval) in op_utils[op_id]:
             energy += op_energy(interval, interval_util)

        power_usage[op_id] = (energy / exp_interval_s)

    # N.B. This won't include any base power for non-op nodes!
    return get_total_power_usage(power_usage, total_tuples)

def get_total_power_usage(power_usage, total_tuples):
    num_entries = len(power_usage)
    power_usage['total_excl_base'] = sum(power_usage.values())
    power_usage['total_normalized_excl_base'] = power_usage['total_excl_base'] / float(total_tuples)
    # N.B. Be careful not to compute over all entries now you've added total!
    power_usage['total'] = power_usage['total_excl_base'] + (num_entries * base_power) 
    power_usage['total_normalized'] = power_usage['total'] / float(total_tuples)
    return power_usage

def trim_net_intervals(node_net_rates, t_start, t_end):
    trimmed_node_net_rates = {}
    for node in node_net_rates:
        trimmed_node_net_rates[node] = []
        min_included_t1 = t_end + 1 # Will ignore if > t_end  
        max_included_t2 = t_start - 1 # Will ignore if < t_start
        for (t1, t2, rx_bytes, tx_bytes) in node_net_rates[node]:
            if t1 > t_start and t2 < t_end:
                trimmed_node_net_rates[node].append((t1,t2,rx_bytes,tx_bytes))
                min_included_t1 = min(t1, min_included_t1) 
                max_included_t2 = max(t2, max_included_t2)
                #print 'Including node %s interval (%d, %d) with t_start=%d, t_end=%d'%(node, t1,t2,t_start,t_end)
                #else:
                #print 'Ignoring node %s interval (%d, %d) with t_start=%d, t_end=%d'%(node, t1,t2,t_start,t_end)

        if min_included_t1 < t_end: 
            first_net_rate = (t_start, min_included_t1, 0, 0)
            trimmed_node_net_rates[node] = [first_net_rate] + trimmed_node_net_rates[node]

        if max_included_t2 > t_start:
            last_net_rate = (max_included_t2, t_end, 0, 0)
            trimmed_node_net_rates[node].append(last_net_rate)

    return trimmed_node_net_rates

def trim_cpu_intervals(node_cpu_rates, t_start, t_end):
    trimmed_node_cpu_rates = {}
    for node in node_cpu_rates:
        trimmed_node_cpu_rates[node] = []
        min_included_t1 = t_end + 1 # Will ignore if > t_end  
        max_included_t2 = t_start - 1 # Will ignore if < t_start
        for (t1, t2, cpu_util) in node_cpu_rates[node]:
            if t1 > t_start and t2 < t_end:
                trimmed_node_cpu_rates[node].append((t1,t2,cpu_util))
                min_included_t1 = min(t1, min_included_t1) 
                max_included_t2 = max(t2, max_included_t2)

        if min_included_t1 < t_end: 
            first_cpu_rate = (t_start, min_included_t1, 0.0)
            trimmed_node_cpu_rates[node] = [first_cpu_rate] + trimmed_node_cpu_rates[node]

        if max_included_t2 > t_start:
            last_cpu_rate = (max_included_t2, t_end, 0.0)
            trimmed_node_cpu_rates[node].append(last_cpu_rate)

    return trimmed_node_cpu_rates

def trim_op_intervals(op_utils, t_start, t_end):
    trimmed_op_utils = {}
    for node in op_utils:
        trimmed_op_utils[node] = []
        min_included_t1 = t_end + 1 # Will ignore if > t_end  
        max_included_t2 = t_start - 1 # Will ignore if < t_start
        for (t, interval_util, cum_util, interval) in op_utils[node]:
            t1 = t - interval
            t2 = t
            if t1 > t_start and t2 < t_end:
                trimmed_op_utils[node].append((t, interval_util, cum_util, interval))
                min_included_t1 = min(t1, min_included_t1) 
                max_included_t2 = max(t2, max_included_t2)

        if min_included_t1 < t_end: 
            first_op_util = (min_included_t1, 0.0, "Error", min_included_t1 - t_start)
            trimmed_op_utils[node] = [first_op_util] + trimmed_op_utils[node]

        if max_included_t2 > t_start:
            last_op_util = (t_end, 0.0, "Error", t_end - max_included_t2)
            trimmed_op_utils[node].append(last_op_util)

    return trimmed_op_utils


def rx_energy(interval, bytez):
    rx_watts_54 = [(0.0, 1.3286), (1*mb, 1.3797), (5*mb, 1.4308),
            (10*mb,1.4819), (15*mb, 1.533), (20*mb,1.533),
            (25*mb,1.5841), (30*mb, 1.5841), (35*mb,1.5841), (40*mb, 1.5841),
            (45*mb, 1.5841), (50*mb, 1.5841), (54*mb, 1.5841)]

    rx_watts_11 = [(0.0, 1.3286), (1*mb, 1.3797), (2*mb, 1.4308),
            (3*mb,1.4819), (4*mb, 1.533), (5*mb,1.533),
            (6*mb,1.5841), (7*mb, 1.5841), (8*mb,1.5841), (9*mb, 1.5841),
            (10*mb, 1.5841), (11*mb, 1.5841)]
    

    rx_watts = rx_watts_11
    interval_tput = (bytez * 8 * 1000) / interval
    return interpolated_energy(rx_watts, interval, interval_tput) 

def tx_energy(interval, bytez):
    tx_watts_54 = [(0.0, base_power), (1*mb, 1.3797), (5*mb, 1.533),
            (10*mb,1.6352), (15*mb, 1.6863), (20*mb,1.7885),
            (25*mb,1.8907), (30*mb, 1.9418), (35*mb,2.044), (40*mb, 2.0951),
            (45*mb, 2.0951), (50*mb, 2.0951), (54*mb, 2.0951)]

    tx_watts_11 = [(0.0, base_power), (1*mb, 1.3797), (2*mb, 1.533),
            (3*mb,1.6352), (4*mb, 1.6863), (5*mb,1.7885),
            (6*mb,1.8907), (7*mb, 1.9418), (8*mb,2.044), (9*mb, 2.0951),
            (10*mb, 2.0951), (11*mb, 2.0951)]


    tx_watts = tx_watts_11
    interval_tput = (bytez * 8 * 1000) / interval
    return interpolated_energy(tx_watts, interval, interval_tput) 

def cpu_energy(interval, cpu_util):
    # From stress --cpu 1?
    cpu_watts = [(0.0,base_power), (1.0, 1.5841)]

    return interpolated_energy(cpu_watts, interval, cpu_util)

def op_energy(interval, op_util):
    # From rpi processor busy waiting
    op_watts = [(0.0,base_power), (1.0, 1.5841)]
    
    return interpolated_energy(op_watts, interval, op_util)

def interpolated_energy(points, interval_ms, val):
    x = map(lambda (a,b): a, points)
    y = map(lambda (a,b): b, points)

    power = numpy.interp(val, x, y) - base_power
    joules = power * (float(interval_ms) / 1000.0)  
    return joules
