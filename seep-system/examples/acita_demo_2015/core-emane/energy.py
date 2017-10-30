def get_network_energy_usage(node_net_rates, total_tuples):
   
    energy_usage = {}
    for node in node_net_rates:
        energy_usage[node] = 0
        for (t1, t2, rx_bytes, tx_bytes) in node_net_rates[node]:
            energy_usage[node] += rx_energy(t2 - t1, rx_bytes)
            energy_usage[node] += tx_energy(t2 - t1, tx_bytes)

    energy_usage['total'] = sum(energy_usage.values())
    energy_usage['total_normalized'] = sum(energy_usage.values()) / total_tuples
    return energy_usage

def rx_energy(interval, bytez):
    # TODO come up with a power model
    return bytez

def tx_energy(interval, bytez):
    # TODO come up with a power model
    return bytez
