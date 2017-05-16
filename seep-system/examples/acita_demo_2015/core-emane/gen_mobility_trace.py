#!/usr/bin/python
import subprocess,argparse,utm,os,glob,math
import pandas as pd
import matplotlib.pyplot as mpl

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

def gen_trace(session_dir, session_id, nodes, params):
    #params['trace_dir'] = '/home/dan/dev/seep-ita/seep-system/examples/'
    #params['trace_file'] = 'new_abboip.txt'
    
    if 'trace' in params:
        print 'Parsing existing trace: %s'%params['trace']
        #metafile = os.path.join(session_dir, 'trimmed_heatmap.metadata')
        metafile = os.path.join('../resources/mobility', 'trimmed_heatmap.metadata')
        metadata = pd.Series.from_csv(metafile, sep=' ') 

        print 'Read metadata:', metadata
        trace_file = parse_trace(session_dir, nodes, metadata, params)
        return trace_file
    else:
        # get trace file name
        trace_file = '%s/%s'%(session_dir, params.get('trace_name', trace_name))
       
        mobility_model = params.get('mobilityModel', default_model)

        if mobility_model ==  'RandomWaypoint':
            # get relevant params
            bm_raw_params = ['bm', '-f', trace_file, 'RandomWaypoint', 
                    '-n', str(nodes-1), 
                    '-R', str(session_id),
                    '-d', str(params.get('duration', default_duration)), 
                    '-x', str(params.get('x', default_x)), 
                    '-y', str(params.get('y', default_y)),
                    '-h', str(params.get('h', default_speed + 1.0)),
                    '-l', str(params.get('l', default_speed - 1.0)),
                    '-p', params.get('p', default_p)]

        elif mobility_model ==  'SteadyStateRandomWaypoint':
              
            bm_raw_params = ['bm', '-f', trace_file, 'SteadyStateRandomWaypoint', 
                    '-n', str(nodes-1), 
                    '-R', str(session_id),
                    '-d', str(params.get('duration', default_duration)), 
                    '-x', str(params.get('x', default_x)), 
                    '-y', str(params.get('y', default_y)),
                    '-o', str(params.get('o', default_speed)),
                    '-p', str(1.0 if params.get('o', default_speed) > 1.0 else params.get('o', default_speed) - 0.1),
                    #'-k', default_p,
                    #'-l', "0.5"]
                    '-k', "15.0",
                    '-l', "15.0"]

        print 'Generating mobility trace with cmd=%s'%str(bm_raw_params)
        # call bonn_motion to generate trace
        raw_proc = subprocess.Popen(bm_raw_params, cwd='.')
        raw_proc.wait()

        print 'Converting mobility trace to ns2 format with cmd=%s'%str(bm_raw_params)
        # call bonn_motion to convert raw trace to ns2
        bm_ns2_params = ['bm', 'NSFile', '-f', trace_file] 
        with open(os.devnull, 'w') as null: 
            ns2_proc = subprocess.Popen(bm_ns2_params, cwd='.', stdout=null, stderr=subprocess.STDOUT)
            ns2_proc.wait()
        return '%s.ns_movements'%trace_name

def parse_trace(session_dir, nodes, metadata, params):
    ns2_file = 'r_waypoints.ns_movements'
    convert_to_cartesian(session_dir, ns2_file,  metadata, trace_dirs[params['trace']], nodes)
    return ns2_file 

def convert_to_cartesian(session_dir, ns2_file, metadata, trace_dir, nodes):
    with open(os.path.join(session_dir, ns2_file), 'w') as utm_trace: 
        node = 0
        abs_trace_dir = os.path.abspath(trace_dir)
        print 'Trace dir=%s'%abs_trace_dir
        sftaxi_files = glob.glob(abs_trace_dir+ '/new_*.txt')
        for sftaxi_file in sftaxi_files[0:nodes]:
            parse_sftaxi_file(sftaxi_file, node, metadata, utm_trace)
            node += 1

def parse_sftaxi_file(trace_file, node, metadata, output_file):
    print 'Parsing trace file: %s'%trace_file
    output_file.write('\n')
    with open(trace_file, 'r') as sftaxi_trace:
        updates = list(sftaxi_trace)
        # Trace updates seem to be in reverse order.
        updates.reverse()
        utm_tuples = parse_utm_tuples(updates, metadata)

        dropped = len(updates)-len(utm_tuples)
        print 'Dropped %d of %d updates'%(dropped, len(updates))
        #updates.reverse()
        raw_movements = []

        #x,y,occupied,t = parse_sftaxi_line(updates[0])
        #utm_coords = utm.from_latlon(x,y)

        utm_coords,occupied,t = utm_tuples[0] 

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

        #for line in updates[1:]:
        for utm_coords,occupied,t in utm_tuples[1:]:
            #x,y,occupied,t = parse_sftaxi_line(line)
            #utm_coords = utm.from_latlon(x,y)
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
        output_file.write('#dropped=%d,orig=%d\n'%(dropped, len(updates)))
        print 'dims=%sx%s\n'%(str(dim_x),str(dim_y))

def parse_utm_tuples(lines, metadata):
    result = []
    min_x = float(metadata['min_x'])
    max_x = float(metadata['max_x'])
    min_y = float(metadata['min_y'])
    max_y = float(metadata['max_y'])

    for line in lines:
        x,y,occupied,t = parse_sftaxi_line(line)
        utm_coords = utm.from_latlon(x,y)
        if utm_coords[0] >= min_x and utm_coords[0] <= max_x and utm_coords[1] >= min_y and utm_coords[1] <= max_y:
            result.append((utm_coords,occupied,t))

    return result

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

def gen_heatmap(session_dir, params):
    # Get a list of locations as utm coords
    # 
    trace_dir = trace_dirs[params['trace']]
    abs_trace_dir = os.path.abspath(trace_dir)
    sftaxi_files = glob.glob(abs_trace_dir+ '/new_*.txt')

    #max_files = len(sftaxi_files) 
    #max_files = 100 
    max_files = 3 


    raw_cols = ['lat', 'long', 'occupied', 't']
    reuse_metadata = True

    if not reuse_metadata:
        min_x = -1.0
        min_y = -1.0
        max_x = 0.0
        max_y = 0.0
        dim_x = 0.0
        dim_y = 0.0

        
        for (node, sftaxi_file) in enumerate(sftaxi_files[:max_files]):
            raw_locs = pd.read_csv(sftaxi_file, sep=' ', names=raw_cols, usecols=range(2)) 
            utm_loc_series = raw_locs.apply(lambda latlon: utm.from_latlon(latlon[0],latlon[1]), axis=1)
            utm_locs = pd.DataFrame(utm_loc_series.tolist(), columns=['x','y','zone_num','zone_letter'], index= utm_loc_series.index)
            utm_coords = utm_locs[['x','y']]

            node_min_x = utm_locs['x'].min()
            node_min_y = utm_locs['y'].min()
            node_max_x = utm_locs['x'].max()
            node_max_y = utm_locs['y'].max()

            if min_x < 0 or node_min_x < min_x: min_x = node_min_x
            if min_y < 0 or node_min_y < min_y: min_y = node_min_y
            max_x = max(max_x, node_max_x)
            max_y = max(max_y, node_max_y)
            dim_x = max_x - min_x
            dim_y = max_y - min_y

            print utm_coords.dtypes
            print utm_coords.head()
            print ''
            print min_x,min_y,max_x,max_y
            print ''
            print dim_x,dim_y


        """
        x_tiles = 100.0
        tile_width = dim_x / x_tiles
        y_tiles = dim_y / tile_width
        tile_height = dim_y / y_tiles
        x_tiles = int(math.floor(x_tiles))+1
        y_tiles = int(math.floor(y_tiles))+1
        """
        tile_width = 1000.0
        tile_height = 1000.0
        x_tiles = int(math.floor(dim_x / tile_width))+1
        y_tiles = int(math.floor(dim_y / tile_height))+1
      
        metadata = pd.Series({'min_x':min_x,'min_y':min_y,
            'max_x':max_x,'max_y':max_y,
            'dim_x':dim_x,'dim_y':dim_y,
            'x_tiles':x_tiles,'y_tiles':y_tiles,
            'twidth':tile_width,'theight':tile_height})
        metafile = os.path.join(session_dir, 'heatmap.metadata')
        metadata.to_csv(metafile, sep=' ')

    else:
        metafile = os.path.join(session_dir, 'heatmap.metadata')
        metadata = pd.Series.from_csv(metafile, sep=' ') 

        print 'Read metadata:', metadata

        min_x = metadata['min_x'] 
        min_y = metadata['min_y'] 
        max_x = metadata['max_x']
        max_y = metadata['max_y']
        dim_x = metadata['dim_x']
        dim_y = metadata['dim_y']
        x_tiles = int(metadata['x_tiles'])
        y_tiles = int(metadata['y_tiles'])
        tile_width = metadata['twidth']
        tile_height = metadata['theight']

        #tile_width = tile_width / 5.0
        #tile_height = tile_height / 5.0 
        #x_tiles = 5 * x_tiles
        #y_tiles = 5 * y_tiles

    #xlims = [300, 350]
    #ylims = [300, 375]
    xlims = [310, 320]
    ylims = [340, 350]
    #xlims[:] = [5 * x for x in xlims]
    #ylims[:] = [5 * y for y in ylims]
    
    print 'x_tiles=%d, y_tiles=%d, tile_width=%.2f, tile_height=%.2f'%(x_tiles, y_tiles, tile_width, tile_height)
    heatmap = pd.DataFrame(index=range(0,x_tiles), columns=range(0,y_tiles)).fillna(0)

    for (node, sftaxi_file) in enumerate(sftaxi_files[:max_files]):
        raw_locs = pd.read_csv(sftaxi_file, sep=' ', names=raw_cols, usecols=range(2)) 
        utm_loc_series = raw_locs.apply(lambda latlon: utm.from_latlon(latlon[0],latlon[1]), axis=1)
        utm_locs = pd.DataFrame(utm_loc_series.tolist(), columns=['x','y','zone_num','zone_letter'], index= utm_loc_series.index)
        utm_coords = utm_locs[['x','y']]
        
        tile_series = utm_coords.apply(lambda xy: get_tile(xy[0], xy[1], tile_width, tile_height, min_x, min_y), axis=1)
        print node
        print ''
        print tile_series.head()

        for x,y in tile_series:
            heatmap.set_value(x, y, heatmap.iloc[x,y]+1)

        #tiles = pd.DataFrame(tile_series.tolist(), columns=['tx', 'ty'], index=tile_series.index)

        print '' 
        #tile_counts = tiles.groupby(lambda tile: tile).count()
        #print tile_counts.head()
        #print tile_counts.dtypes 
        #print heatmap.iloc[20:50, 50:80]
    outfile = os.path.join(session_dir, 'heatmap.csv')
    heatmap.to_csv(outfile, sep=' ')

    print 'Restricting to %s %s'%(str(xlims), str(ylims))
    trimmed_heatmap = heatmap.iloc[xlims[0]:xlims[1],ylims[0]:ylims[1]]

    trimmed_min_x = min_x+(xlims[0]*tile_width)
    trimmed_min_y = min_y+(ylims[0]*tile_height)
    trimmed_max_x = min_x+(xlims[1]*tile_width)
    trimmed_max_y = min_y+(ylims[1]*tile_height)
    trimmed_metadata = pd.Series({
        'min_x':trimmed_min_x,
        'min_y':trimmed_min_y,
        'max_x':trimmed_max_x,
        'max_y':trimmed_max_y,
        'dim_x':trimmed_max_x - trimmed_min_x,
        'dim_y':trimmed_max_y -trimmed_min_y,
        'x_tiles':xlims[1]-xlims[0],'y_tiles':ylims[1]-ylims[0],
        'twidth':tile_width,'theight':tile_height})
    metafile = os.path.join(session_dir, 'trimmed_heatmap.metadata')
    trimmed_metadata.to_csv(metafile, sep=' ')
    #trimmed_heatmap = trimmed_heatmap.iloc[10:20,40:50]
    plot_heatmap(trimmed_heatmap, min_x, min_y, tile_width, tile_height, session_dir)
    
def plot_heatmap(heatmap, min_x, min_y, tile_width, tile_height, session_dir):
    mpl.pcolor(heatmap.transpose())
    mpl.savefig(os.path.join(session_dir, 'heatmap.pdf'), bbox_inches='tight')
    mpl.show()

def get_tile(x, y, twidth, theight, min_x, min_y):
    xtile = int(math.floor((x - min_x) / twidth))
    ytile = int(math.floor((y - min_y) / theight))

    return (xtile, ytile)

"""
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Generate ns2 mobility trace using BonnMotion.')
    parser.add_argument('--sessionDir', dest='session_dir', default='/tmp', help='dir name (/tmp)')
    parser.add_argument('--nodes', dest='nodes', default='9', help = '# nodes (9)')
    parser.add_argument('--sessionid', dest='session_id', default='0', help = 'session id (random seed = 0)')
    parser.add_argument('--trace', dest='trace', default=None, help='Analyse trace')
    parser.add_argument('--heatmap', dest='heatmap', action='store_true', help='Generate a heatmap (requires trace)')
    args=parser.parse_args()

    if args.heatmap:
        gen_heatmap(args.session_dir, {'nodes':int(args.nodes)+1, 'trace':args.trace}) 
    else:
        params = {'nodes':int(args.nodes)+1}
        if args.trace: params['trace'] = args.trace
        gen_trace(args.session_dir, args.session_id, params)
"""
