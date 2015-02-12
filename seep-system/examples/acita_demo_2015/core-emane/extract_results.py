#!/usr/bin/python
import re

def is_src_log(lines):
    return log_type(lines) == 'SOURCE'

def is_sink_log(lines):
    return log_type(lines) == 'SINK'

def is_processor_log(lines):
    return log_type(lines) == 'PROCESSOR'

def log_type(lines):
    regex = re.compile(r'Setting up (\w+) operator with id=(.*)$')
    for line in lines:
        match = re.search(regex, line)
        if match: return match.group(1)

    raise Exception("Unknown log type!")

def src_tx_begin(lines):
    """
    return t_begin
    """
    regex = re.compile(r'Source sending started at t=(\d+)')
    for line in lines:
        match = re.search(regex, line)
	if match: return int(match.group(1))

    return None

def sink_rx_begin(lines):
    """
    returns t_begin
    """
    regex = re.compile(r'SNK: Received initial tuple at t=(\d+)')
    for line in lines:
        match = re.search(regex, line)
	if match: return int(match.group(1))

    return None

def sink_rx_end(lines):
    """
    returns (total tuples, total bytes, t_end)
    """
    regex = re.compile(r'SNK: FINISHED with total tuples=(\d+),total bytes=(\d+),t=(\d+)')
    for line in lines:
        match = re.search(regex, line)
        if match:
            return (int(match.group(1)), int(match.group(2)), int(match.group(3)))

    return None
   
def sink_rx_latencies(lines):
    """
    returns a list of (tsrx, latency)
    """
    tuple_records = sink_rx_tuples(lines)
    return map(lambda tuple_record: (tuple_record[3], tuple_record[4]), tuple_records)

def sink_rx_tuples(lines):
    results = []
    regex = re.compile(r'SNK: Received tuple with cnt=(\d+),id=(\d+),txts=(\d+),rxts=(\d+),latency=(\d+)$')
    for line in lines:
        match = re.search(regex, line)
	if match:
            return (int(match.group(1)), int(match.group(2)), int(match.group(3)))

    return None
