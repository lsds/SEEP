import numpy

def get_network_power_usage(node_net_rates, total_tuples, t_start, t_end):
    power_usage = {}
    node_net_rates = trim_net_intervals(node_net_rates, t_start, t_end)

    for node in node_net_rates:
        #power_usage[node] = base_power() 
        power_usage[node] = 0 
        for (t1, t2, rx_bytes, tx_bytes) in node_net_rates[node]:
            power_usage[node] += avg_rx_power(t2 - t1, rx_bytes)
            power_usage[node] += avg_tx_power(t2 - t1, tx_bytes)

    num_nodes = len(node_net_rates)
    return get_total_power_usage(power_usage, total_tuples)

def get_cpu_power_usage(node_cpu_rates, total_tuples, t_start, t_end):
    power_usage = {}
    node_cpu_rates = trim_cpu_intervals(node_cpu_rates, t_start, t_end)
    for node in node_cpu_rates:
        #power_usage[node] = base_power() 
        power_usage[node] = 0 
        for (t1, t2, cpu_util) in node_cpu_rates[node]:
            power_usage[node] += avg_cpu_power(t2 - t1, cpu_util)

    num_nodes = len(node_cpu_rates)
    return get_total_power_usage(power_usage, total_tuples)

def get_ops_power_usage(op_utils, total_tuples, t_start, t_end):
    power_usage = {}
    op_utils = trim_op_intervals(op_utils, t_start, t_end)

    for op_id in op_utils:
        #power_usage[node] = base_power() 
        power_usage[op_id] = 0 
        for (t, interval_util, cum_util, interval) in op_utils[op_id]:
            power_usage[op_id] += avg_op_power(interval, interval_util)

    num_nodes = len(op_utils)
    return get_total_power_usage(power_usage, total_tuples)

def get_total_power_usage(power_usage, total_tuples):
    power_usage['total_excl_base'] = sum(power_usage.values())
    power_usage['total_normalized_excl_base'] = power_usage['total_excl_base'] / total_tuples
    # N.B. Be careful not to compute over all entries now you've added total!
    power_usage['total'] = power_usage['total_excl_base'] + base_power()  
    power_usage['total_normalized'] = power_usage['total'] / total_tuples
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
            # cum_util distorted here
            first_op_util = (min_included_t1, 0.0, 0.0, min_included_t1 - t_start)
            trimmed_op_utils[node] = [first_op_util] + trimmed_op_utils[node]

        if max_included_t2 > t_start:
            # cum_util distorted here
            last_op_util = (t_end, 0.0, 0.0, t_end - max_included_t2)
            trimmed_op_utils[node].append(last_op_util)

    return trimmed_op_utils

def base_power():
    return 1.3286

def avg_rx_power(interval, bytez):
    mb = 1000000
    rx_watts_54 = [(0.0, 1.3286), (1*mb, 1.3797), (5*mb, 1.4308),
            (10*mb,1.4819), (15*mb, 1.533), (20*mb,1.533),
            (25*mb,1.5841), (30*mb, 1.5841), (35*mb,1.5841), (40*mb, 1.5841),
            (45*mb, 1.5841), (50*mb, 1.5841), (54*mb, 1.5841)]

    rx_watts_11 = [(0.0, 1.3286), (1*mb, 1.3797), (2*mb, 1.4308),
            (3*mb,1.4819), (4*mb, 1.533), (5*mb,1.533),
            (6*mb,1.5841), (7*mb, 1.5841), (8*mb,1.5841), (9*mb, 1.5841),
            (10*mb, 1.5841), (11*mb, 1.5841)]
    

    rx_watts = rx_watts_11
    
    #return interpolated_power(rx_watts, interval, bytez) - rx_watts[0][1]
    return interpolated_power(rx_watts, interval, bytez) - base_power() 

def avg_tx_power(interval, bytez):
    tx_watts_54 = [(0.0, 1.3286), (1*mb, 1.3797), (5*mb, 1.533),
            (10*mb,1.6352), (15*mb, 1.6863), (20*mb,1.7885),
            (25*mb,1.8907), (30*mb, 1.9418), (35*mb,2.044), (40*mb, 2.0951),
            (45*mb, 2.0951), (50*mb, 2.0951), (54*mb, 2.0951)]

    tx_watts_11 = [(0.0, 1.3286), (1*mb, 1.3797), (2*mb, 1.533),
            (3*mb,1.6352), (4*mb, 1.6863), (5*mb,1.7885),
            (6*mb,1.8907), (7*mb, 1.9418), (8*mb,2.044), (9*mb, 2.0951),
            (10*mb, 2.0951), (11*mb, 2.0951)]


    tx_watts = tx_watts_11

    #return interpolated_power(tx_watts, interval, bytez) - tx_watts[0][1]
    return interpolated_power(tx_watts, interval, bytez) - base_power() 

# TODO: Since might have different intervals, really need to multiply by
# the actual interval length here.
def avg_cpu_power(interval, cpu_util):
    # From stress --cpu 1?
    cpu_watts = [(0.0,1.3286), (1.0, 1.5841)]

    x = map(lambda (a,b): a, cpu_watts)
    y = map(lambda (a,b): b, cpu_watts)

    #TODO: Normalize to num cores?
    return numpy.interp(cpu_util, x, y)

# TODO: Since might have different intervals, really need to multiply by
# the actual interval length here.
def avg_op_power(interval, op_util):
    # From rpi processor busy waiting
    op_watts = [(0.0,1.3286), (1.0, 1.5841)]

    x = map(lambda (a,b): a, op_watts)
    y = map(lambda (a,b): b, op_watts)

    return numpy.interp(op_util, x, y)

def interpolated_power(data2watts, interval, bytez):
    interval_tput = (bytez * 8 * 1000) / interval

    x = map(lambda (a,b): a, data2watts)
    y = map(lambda (a,b): b, data2watts)

    return numpy.interp(interval_tput, x, y)

