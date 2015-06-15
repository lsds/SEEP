#!/usr/bin/python
import subprocess,argparse

default_duration = '100000'
default_x = '1200'
#default_x = '1500'
#default_x = '5000'
#default_x = '4000'
#default_x = '3000'
default_y = '1200' 
#default_y = '1500' 
#default_y = '5000' 
#default_y = '4000' 
#default_y = '3000' 
default_speed = 2.0
default_p = '2.0' 
trace_name = 'r_waypoints'

def gen_trace(session_dir, session_id, params):

    # get trace file name
    trace_file = '%s/%s'%(session_dir, trace_name)

    # get relevant params

    bm_raw_params = ['bm', '-f', trace_file, 'RandomWaypoint', 
            '-n', str(params['nodes']-1), 
            '-R', str(session_id),
            '-d', str(params.get('duration', default_duration)), 
            '-x', str(params.get('x', default_x)), 
            '-y', str(params.get('y', default_y)),
            '-h', str(params.get('h', default_speed + 1.0)),
            '-l', str(params.get('l', default_speed - 1.0)),
            '-p', params.get('p', default_p)]

    print 'Generating mobility trace with cmd=%s'%str(bm_raw_params)
    # call bonn_motion to generate trace
    raw_proc = subprocess.Popen(bm_raw_params, cwd='.')
    raw_proc.wait()

    # call bonn_motion to convert raw trace to ns2
    bm_ns2_params = ['bm', 'NSFile', '-f', trace_file] 
    ns2_proc = subprocess.Popen(bm_ns2_params, cwd='.')
    ns2_proc.wait()
    return '%s.ns_movements'%trace_name

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Generate ns2 mobility trace using BonnMotion.')
    parser.add_argument('--sessionDir', dest='session_dir', default='/tmp', help='dir name (/tmp)')
    parser.add_argument('--nodes', dest='nodes', default='9', help = '# nodes (9)')
    parser.add_argument('--sessionid', dest='session_id', default='0', help = 'session id (random seed = 0)')
    args=parser.parse_args()

    gen_trace(args.session_dir, args.session_id, {'nodes':int(args.nodes)+1})
