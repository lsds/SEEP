import numpy

def get_network_power_usage(node_net_rates, total_tuples):

    power_usage = {}
    for node in node_net_rates:
        #power_usage[node] = base_power() 
        power_usage[node] = 0 
        for (t1, t2, rx_bytes, tx_bytes) in node_net_rates[node]:
            power_usage[node] += avg_rx_power(t2 - t1, rx_bytes)
            power_usage[node] += avg_tx_power(t2 - t1, tx_bytes)

    num_nodes = len(node_net_rates)
    power_usage['total_excl_base'] = sum(power_usage.values())
    power_usage['total_normalized_excl_base'] = power_usage['total_excl_base'] / total_tuples
    # N.B. Be careful not to compute over all entries now you've added total!
    power_usage['total'] = power_usage['total_excl_base'] + base_power()  
    power_usage['total_normalized'] = power_usage['total'] / total_tuples
    return power_usage

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

def interpolated_power(data2watts, interval, bytez):
    interval_tput = (bytez * 8 * 1000) / interval

    x = map(lambda (a,b): a, data2watts)
    y = map(lambda (a,b): b, data2watts)

    return numpy.interp(interval_tput, x, y)

