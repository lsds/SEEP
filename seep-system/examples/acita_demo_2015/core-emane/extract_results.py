#!/usr/bin/python
import re, pandas as pd, os.path, glob

def is_src_log(f):
    f_type = log_type(f) 
    return f_type in ['SOURCE', 'VIDEO_SOURCE', 'VIDEO_SOURCE2', 'LOCATION_SOURCE']

def is_sink_log(f):
    return log_type(f) == 'SINK'

def is_finished_sink_log(f):
    regex = re.compile(r'SNK: FINISHED with total tuples=(\d+),total bytes=(\d+),t=(\d+)')
    for line in f:
        match = re.search(regex, line)
        if match: return True 
    return False

def is_processor_log(f):
    #return log_type(f) == 'PROCESSOR'
    f_type = log_type(f) 
    return f_type in ['PROCESSOR', 'FACE_DETECTOR', 'FACE_DETECTOR_RECOGNIZER', 'FACE_RECOGNIZER', 'JOIN', 'HEATMAP_JOIN', 'FACE_RECOGNIZER_JOIN']

def log_type(f):
    regex = re.compile(r'Setting up (.*) operator with id=(.*)$')
    for line in f:
        match = re.search(regex, line)
        if match: return match.group(1)

    raise Exception("Unknown log type for file: "+str(f))

def src_tx_begin(f):
    """
    return t_begin
    """
    regex = re.compile(r'Source sending started at t=(\d+)')
    for line in f:
        match = re.search(regex, line)
	if match: return int(match.group(1))

    return None

def sub_tx_begin(f):
    """
    return t_sub_begin
    """
    failed_op = 10
    oq_sync_regex = re.compile(r't=(\d+), oq.sync')
    fail_begin_regex = re.compile(r'Failure ctrl watchdog completed hard cleanup for %d in'%(failed_op))
    last_tx = None
    for line in f:
        match = re.search(oq_sync_regex, line)
        if match:
            last_tx = int(match.group(1))
        else:
            match = re.search(fail_begin_regex, line)
            if match: return last_tx

    return None

def sink_rx_begin(f):
    """
    returns t_begin
    """
    regex = re.compile(r'SNK: Received initial tuple at t=(\d+)')
    for line in f:
        match = re.search(regex, line)
	if match: return int(match.group(1))

    return None

def sink_rx_end(f):
    """
    returns (total tuples, total bytes, t_end)
    """
    regex = re.compile(r'SNK: FINISHED with total tuples=(\d+),total bytes=(\d+),t=(\d+)')
    for line in f:
        match = re.search(regex, line)
        if match:
            return (int(match.group(1)), int(match.group(2)), int(match.group(3)))

    return None
   
def sink_rx_latencies(f):
    """
    returns a list of (tsrx, latency)
    """
    tuple_records = sink_rx_tuples(f)
    latencies = map(lambda tuple_record: (int(tuple_record[2]), int(tuple_record[5]), int(tuple_record[3]), int(tuple_record[4])), tuple_records)
    #return pd.Series(latencies)
    return latencies

def sink_rx_tuple_ids(f):
    tuple_records = sink_rx_tuples(f)
    return set(map(lambda record: int(record[2]), tuple_records))

def unfinished_sink_tuples(f, t_end, prev_tuples):
    tuple_records = sink_rx_tuples(f)
    print 'Found %d tuple records'%(len(tuple_records))
    filtered_tuple_records = filter(lambda (cnt, tid, ts, txts, rxts, latency, bytez, opLats, sockLats):
            int(rxts) <= int(t_end) and not int(ts) in prev_tuples, tuple_records)
    print 'Left with %d tuple records after filtering'%(len(filtered_tuple_records))
    #total_bytes = reduce(lambda total, (cnt, tid, ts, txts, rxts, latency, bytez): int(bytez)+total, filtered_tuple_records)
    total_bytes = reduce(lambda total, el: int(el[6])+total, filtered_tuple_records, 0)
    return (len(filtered_tuple_records), total_bytes)


def unfinished_sink_rx_latencies(f, t_end):
    tuple_records = sink_rx_tuples(f)
    filtered_tuple_records = filter(lambda (cnt, tid, ts, txts, rxts, latency, bytez, opLats, sockLats):
            int(rxts) <= int(t_end), tuple_records)
    latencies = map(lambda tuple_record: (int(tuple_record[2]), int(tuple_record[5]), int(tuple_record[3]), int(tuple_record[4])), filtered_tuple_records)
    #return pd.Series(latencies)
    return latencies

def dedup_latencies(latencies):
    deduped = {}
    for (ts, latency, txts, rxts) in latencies:
        if ts in deduped:
            print 'Found dupe latency %s'%(str(ts))
            if deduped[ts][0] > latency:
                deduped[ts] = (latency, txts, rxts)
        else:
            deduped[ts] = (latency, txts, rxts)
    #return pd.Series(deduped.values())
    return deduped

def sink_rx_tuples(f):
    results = []
    regex = re.compile(r'SNK: Received tuple with cnt=(\d+),id=(\d+),ts=(\d+),txts=(\d+),rxts=(\d+),latency=(\d+),bytes=(\d+),latencyBreakdown=(\d+);(\d+)$')
    #regex = re.compile(r'SNK: Received tuple with cnt=(\d+),id=(\d+),ts=(\d+),txts=(\d+),rxts=(\d+),latency=(\d+),bytes=(\d+)$')
    for line in f:
        match = re.search(regex, line)
        if match:
            results.append(match.groups())

    return results

def sub_rx_latencies(t_sub_begin, rx_latencies):
    return { ts : v for ts, v in rx_latencies.iteritems() if v[2] > t_sub_begin}

def processor_tput(f):
    regex = re.compile(r't=(\d+),id=(\d+),interval=(\d+),tput=(.*),cumTput=(.*)$')

    last_cum_tput = 0
    op_id = None
    for line in f:
        match = re.search(regex, line)
        if match:
            last_cum_tput = float(match.group(5))
            op_id = match.group(2)

    return (op_id, last_cum_tput)

def get_interval_tputs(f):
    regex = re.compile(r't=(\d+),id=([-]?\d+),interval=(\d+),tput=(.*),cumTput=(.*)$')
    op_id = None
    tputs = [] 
    for line in f:
        match = re.search(regex, line)
        if match:
            op_id = str(int(match.group(2)))
            tputs.append((int(match.group(1)), float(match.group(4)), float(match.group(5))))

    return (op_id, tputs)

def get_link_interval_tputs(f):
    # t=1470426279812,opid=111,upid=10,interval=1903,tput=63.0,cumTput=53.0, cost=7.408

    regex = re.compile(r't=(\d+),opid=([-+]?\d+),upid=([-+]?\d+),interval=(\d+),tput=(.*),cumTput=(.*), cost=(\d+\.\d+)$')
    op_id = None
    tputs = {} 
    for line in f:
        match = re.search(regex, line)
        if match:
            op_id = str(int(match.group(2)))
            up_id = str(int(match.group(3)))
            up_tputs = tputs.get(up_id, [])
            up_tputs.append((int(match.group(1)), float(match.group(5)), float(match.group(6)), float(match.group(7))))
            tputs[up_id] = up_tputs

    return (op_id, tputs)

def get_weight_infos(f):
    """ 
    Example log line:
    t=1481214262870,op=11,ltqlen=726,iq=625,oq=101,ready=625,pending={0=0, 1=0},w=0.0,wi=[0.0, 0.0],wdqru(i=0 [u=-1:w=-359.244746835916,d=-625.0,q=100.0,r=0.5747915949374656]),(i=1 [u=-2:w=-78.81605920712812,d=-625.0,q=100.0,r=0.12610569473140498])
    """
    regex = re.compile(r't=(\d+),op=(.*),ltqlen=(\d+),iq=(\d+),oq=(\d+),ready=(\d+),pending=(.*),skew=(.*),w=(.*),wi=(.*),wdqru(.*)$')
    op_id = None
    weight_info = [] 
    for line in f:
        match = re.search(regex, line)
        if match:
            t = int(match.group(1))
            op_id = str(int(match.group(2)))
            ltqlen = int(match.group(3))
            iq = int(match.group(4))
            oq = int(match.group(5))
            ready = int(match.group(6))

            pending = []
            pending_match = re.search(re.compile(r'{(.*)}'), match.group(7)).group(1)

            if pending_match: 
                pending_dict = {}
                for split in pending_match.split(','):
                    i = int(split.split('=')[0])
                    pending_length_i = int(split.split('=')[1])
                    pending_dict[i] = pending_length_i

                for j in sorted(pending_dict): 
                    pending.append(pending_dict[j])
            
            skew = int(match.group(8))
            w = float(match.group(9))

            wi = []
            wi_match = re.search(re.compile(r'\[(.*)\]'), match.group(10)).group(1)

            if wi_match:
                wi = map(float, wi_match.split(','))
                    
            wdqru = match.group(11)
            weight_info.append((t, ltqlen, iq, oq, ready, pending, skew, w, wi, wdqru))

    return (op_id, weight_info)

def get_urc_qlens(f):
    regex = re.compile(r't=(\d+),op=(.*),local output qlen=(\d+)')
    op_id = None
    qlens = [] 
    for line in f:
        match = re.search(regex, line)
        if match:
            op_id = str(int(match.group(2)))
            qlens.append((int(match.group(1)), int(match.group(3))))

    return (op_id, qlens)

def get_utils(f):
    regex = re.compile(r't=(\d+),id=(\d+),interval=(\d+),util=(.*),cumUtil=(.*)$')
    op_id = None
    utils = [] 
    for line in f:
        match = re.search(regex, line)
        if match:
            op_id = str(int(match.group(2)))
            utils.append((int(match.group(1)), float(match.group(4)), float(match.group(5)), int(match.group(3))))

    return (op_id, utils)

def get_transmissions(f):
    regex = re.compile(r't=(\d+), oq.sync ([-]?\d+) sending ts=(\d+) for ([-]?\d+),')
    transmissions = []
    op_id = None
    for line in f:
        match = re.search(regex, line)
        if match:
            op_id = str(int(match.group(2)))
            transmissions.append((int(match.group(1)), int(match.group(3)), int(match.group(4))))

    return (op_id, transmissions)

def get_errors(f):
    err_regexes = re.compile(r'RuntimeException|Logic error|Logic Error|Abort')
    err_msgs = []
    for line in f:
        match = re.findall(err_regexes, line)
        if match: err_msgs += match 

    return err_msgs

def get_node_net_rates(exp_dir):
    all_node_rates = {}
    for fname in glob.glob('%s/net-util/*net-rates.txt'%exp_dir):
        node_rates = []
        with open(fname, 'r') as f:
            for line in f:
                if not line.startswith('#'):
                    splits = line.strip().split(' ')
                    # t1, t2, rx_bytes, tx_bytes
                    node_rates.append((int(splits[0]), int(splits[1]), int(splits[2]), int(splits[3])))

        host = os.path.basename(fname).rstrip('-net-rates.txt')
        all_node_rates[host] = node_rates
    return all_node_rates

def get_node_cpu_rates(exp_dir):
    all_node_rates = {}
    for fname in glob.glob('%s/cpu-util/*cpu-rates.txt'%exp_dir):
        node_rates = []
        with open(fname, 'r') as f:
            for line in f:
                if not line.startswith('#'):
                    splits = line.strip().split(' ')
                    # t1, t2, u_t1t2 
                    node_rates.append((int(splits[0]), int(splits[1]), float(splits[2])))

        host = os.path.basename(fname).rstrip('-cpu-rates.txt')
        all_node_rates[host] = node_rates

    return all_node_rates
