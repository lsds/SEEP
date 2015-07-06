#!/usr/bin/python
import subprocess,argparse,utm,os,glob,math

default_model = 'SteadyStateRandomWaypoint'
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
default_speed = 5.0
default_p = '2.0' 
trace_name = 'r_waypoints'

trace_dirs = {'sftaxi':'../resources/mobility/cabspottingdata'}

def gen_trace(session_dir, session_id, params):
    #params['trace_dir'] = '/home/dan/dev/seep-ita/seep-system/examples/'
    #params['trace_file'] = 'new_abboip.txt'
    
    if 'trace' in params:
        print 'Parsing existing trace: %s'%params['trace']
        trace_file = parse_trace(session_dir, params)
        return trace_file
    else:
        # get trace file name
        trace_file = '%s/%s'%(session_dir, params.get('trace_name', trace_name))
       
        mobility_model = params.get('mobilityModel', default_model)

        if mobility_model ==  'RandomWaypoint':
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

        elif mobility_model ==  'SteadyStateRandomWaypoint':
            bm_raw_params = ['bm', '-f', trace_file, 'SteadyStateRandomWaypoint', 
                    '-n', str(params['nodes']-1), 
                    '-R', str(session_id),
                    '-d', str(params.get('duration', default_duration)), 
                    '-x', str(params.get('x', default_x)), 
                    '-y', str(params.get('y', default_y)),
                    '-o', str(params.get('o', default_speed)),
                    '-p', "1.0",
                    '-k', default_p,
                    '-l', "0.5"]

        print 'Generating mobility trace with cmd=%s'%str(bm_raw_params)
        # call bonn_motion to generate trace
        raw_proc = subprocess.Popen(bm_raw_params, cwd='.')
        raw_proc.wait()

        # call bonn_motion to convert raw trace to ns2
        bm_ns2_params = ['bm', 'NSFile', '-f', trace_file] 
        ns2_proc = subprocess.Popen(bm_ns2_params, cwd='.')
        ns2_proc.wait()
        return '%s.ns_movements'%trace_name

def parse_trace(session_dir, params):
    ns2_file = 'r_waypoints.ns_movements'
    convert_to_cartesian(session_dir, ns2_file, trace_dirs[params['trace']])
    return ns2_file 

def convert_to_cartesian(session_dir, ns2_file, trace_dir):
    with open(os.path.join(session_dir, ns2_file), 'w') as utm_trace: 
        node = 0
        abs_trace_dir = os.path.abspath(trace_dir)
        print 'Trace dir=%s'%abs_trace_dir
        sftaxi_files = glob.glob(abs_trace_dir+ '/new_*.txt')
        for sftaxi_file in sftaxi_files[0:20]:
            parse_sftaxi_file(sftaxi_file, node, utm_trace)
            node += 1

def parse_sftaxi_file(trace_file, node, output_file):
    print 'Parsing trace file: %s'%trace_file
    output_file.write('\n')
    with open(trace_file, 'r') as sftaxi_trace:
        updates = list(sftaxi_trace)
        # Trace updates seem to be in reverse order.
        updates.reverse()
        raw_movements = []

        x,y,occupied,t = parse_sftaxi_line(updates[0])
        utm_coords = utm.from_latlon(x,y)
        #write_ns2_initial_movement(node, utm_coords[0], utm_coords[1], output_file)
        src_coords = utm_coords
        src_time = t 

        raw_movements.append((src_time, t, utm_coords[0], utm_coords[1], 0))

        min_time = t
        max_time = t
        min_x = utm_coords[0]
        min_y = utm_coords[1]
        max_x = utm_coords[0]
        max_y = utm_coords[1]

        for line in updates[1:]:
            x,y,occupied,t = parse_sftaxi_line(line)
            utm_coords = utm.from_latlon(x,y)
            speed = compute_speed(src_coords, utm_coords, src_time, t)
            raw_movements.append((src_time, t, utm_coords[0], utm_coords[1], speed))
            #write_ns2_waypoint(src_time, node, utm_coords[0], utm_coords[1], speed, output_file)

            src_coords = utm_coords
            src_time = t 

            min_time = min(t, min_time)
            max_time = max(t, max_time)
            min_x = min(min_x, utm_coords[0])
            min_y = min(min_y, utm_coords[1])
            max_x = max(max_x, utm_coords[0])
            max_y = max(max_y, utm_coords[1])
            #output_file.write("%d,%s,%s,%s\n"%(node,str(t),str(utm_coords[0]),str(utm_coords[1])))

        dim_x = max_x - min_x
        dim_y = max_y - min_y

        print '#min_t=%d, max_t=%d, duration=%d\n'%(min_time,max_time, max_time - min_time)
        print '#min_x=%s, min_y=%s, max_x=%s, max_y=%s\n'%(str(min_x),str(min_y),str(max_x),str(max_y))
        print '#dims=%sx%s\n'%(str(dim_x),str(dim_y))

        # Normalize the times to start at 0, and also the waypoints to be wrt
        # 0,0 (and have x,y < 65535)
        found_initial = False
        for (src_time, t, x, y, speed) in raw_movements:
            if t < min_time: raise Exception('Logic error, t=%s < min_time=%s'%(str(t),str(min_time)))
            norm_x = x-min_x
            norm_y = y-min_y
            #Ignore positions to big for CORE to handle (annoying!).
            if norm_x < 65535 and norm_y < 65535:
                if found_initial:
                    norm_t = src_time - min_time
                    write_ns2_waypoint(norm_t, node, norm_x, norm_y, speed, output_file)
                else:
                    print 'Found initial'
                    write_ns2_initial_movement(node, norm_x, norm_y, output_file)
                    min_time = t
                    found_initial = True
            else:
                print 'Position outside area: (%d,%d), norm=(%d,%d)'%(x,y, norm_x,norm_y)
                norm_x = min(norm_x, 65534)
                norm_y = min(norm_y, 65534)
                if found_initial:
                    norm_t = src_time - min_time
                    write_ns2_waypoint(norm_t, node, norm_x, norm_y, speed, output_file)
                else:
                    print 'Found initial'
                    write_ns2_initial_movement(node, norm_x, norm_y, output_file)
                    min_time = t
                    found_initial = True

        
        output_file.write('#min_t=%d, max_t=%d, duration=%d\n'%(min_time,max_time, max_time - min_time))
        output_file.write('#min_x=%s, min_y=%s, max_x=%s, max_y=%s\n'%(str(min_x),str(min_y),str(max_x),str(max_y)))
        output_file.write('#dims=%sx%s\n'%(str(dim_x),str(dim_y)))
        print 'dims=%sx%s\n'%(str(dim_x),str(dim_y))

def write_ns2_initial_movement(node, x, y, f):
     f.write("$node_(%d) set X_ %s\n"%(node, str(x)))
     f.write("$node_(%d) set Y_ %s\n"%(node, str(y)))

def write_ns2_waypoint(t, node, x, y, speed, f):
    f.write('$ns_ at %s "$node_(%d) setdest %s %s %s"\n'%(t,node,x,y,speed))

def compute_speed(src, dest, tstart, tend):
    x_dist = (src[0] - dest[0])
    y_dist = (src[1] - dest[1])
    dist = math.sqrt(x_dist*x_dist + y_dist*y_dist)
    duration = tend - tstart
    if duration == 0: return 0.0
    elif duration < 0: raise Exception("Logic error negative duration.")
    else:
        return dist/duration

def parse_sftaxi_line(line):
    vals = line.split()
    return (float(vals[0]), float(vals[1]), int(vals[2]), int(vals[3]))

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Generate ns2 mobility trace using BonnMotion.')
    parser.add_argument('--sessionDir', dest='session_dir', default='/tmp', help='dir name (/tmp)')
    parser.add_argument('--nodes', dest='nodes', default='9', help = '# nodes (9)')
    parser.add_argument('--sessionid', dest='session_id', default='0', help = 'session id (random seed = 0)')
    args=parser.parse_args()

    gen_trace(args.session_dir, args.session_id, {'nodes':int(args.nodes)+1})
