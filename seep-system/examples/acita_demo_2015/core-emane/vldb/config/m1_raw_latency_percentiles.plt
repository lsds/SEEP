load sprintf("%s/colours.plt",tmpldir)
set terminal term 
set output sprintf("%s/%s/raw_latency_percentiles%s",outputdir,timestr,termext)

set title sprintf("Latency percentiles (min/5th/95th/max) for different replication factors (k)\n mob=%s, query=%s, duration=%s, runs=%s",mob,query,duration,runs)
set xlabel "Replication factor (k)"
set ylabel "Latency (ms)"
#set yrange [0:100]
#set xrange [1:5]
# Only have xtic labels for incr's of 1
set xtics 1

set border linewidth 1.5
set style line 5 linewidth 2.5 linecolor rgb "black"
#set style line 1 linewidth 2.5 linecolor rgb "red"
#set style line 2 linewidth 2.5 linecolor rgb "blue"
#set style line 3 linewidth 2.5 linecolor rgb "green"
#set style line 4 linewidth 2.5 linecolor rgb "pink"

set style line 1 linewidth 2.5 linecolor rgb CRIMSON 
set style line 2 linewidth 2.5 linecolor rgb CADMIUMORANGE 
set style line 3 linewidth 2.5 linecolor rgb COBALT 
set style line 4 linewidth 2.5 linecolor rgb GREY 

set boxwidth 0.1
#Leave space at edges
set offset 0.1,0.1
set style fill empty 

plot sprintf("%s/%s/1k/%sm/1k-lat.data",outputdir,timestr,mob) using 1:6:6:6:6 notitle w candlesticks linestyle 5, \
	sprintf("%s/%s/2k/%sm/2k-lat.data",outputdir,timestr,mob) using 1:6:6:6:6 notitle w candlesticks linestyle 5, \
	sprintf("%s/%s/3k/%sm/3k-lat.data",outputdir,timestr,mob) using 1:6:6:6:6 notitle w candlesticks linestyle 5, \
	sprintf("%s/%s/5k/%sm/5k-lat.data",outputdir,timestr,mob) using 1:6:6:6:6 notitle w candlesticks linestyle 5, \
	sprintf("%s/%s/1k/%sm/1k-lat.data",outputdir,timestr,mob) using 1:4:3:11:9 notitle with candlesticks whiskerbars 0.5 linestyle 1, \
	sprintf("%s/%s/2k/%sm/2k-lat.data",outputdir,timestr,mob) using 1:4:3:11:9 notitle with candlesticks whiskerbars 0.5 linestyle 2, \
	sprintf("%s/%s/3k/%sm/3k-lat.data",outputdir,timestr,mob) using 1:4:3:11:9 notitle with candlesticks whiskerbars 0.5 linestyle 3, \
	sprintf("%s/%s/5k/%sm/5k-lat.data",outputdir,timestr,mob) using 1:4:3:11:9 notitle with candlesticks whiskerbars 0.5 linestyle 4 