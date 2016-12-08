load sprintf("%s/colours.plt",tmpldir)
set terminal term 
set output sprintf("%s/%s/latency_percentiles%s",outputdir,timestr,termext)

#set title sprintf("Latency (min/5th/95th/max) vs mobility for replication factor (k)\n query=%s, runs=%s",query,runs) font ",10"
set xlabel "Replication factor (k)" font ", 20"
set ylabel "Latency (ms)" font ", 20" offset -2

#set yrange [0:100]
#set xrange [1:5]
# Only have xtic labels for incr's of 1
set xtics 1
set tics font ", 16"

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

plot sprintf("%s/%s/1k-lat.data",outputdir,timestr) using 2:7:7:7:7 notitle w candlesticks linestyle 5, \
	sprintf("%s/%s/2k-lat.data",outputdir,timestr) using ($2+0.05):7:7:7:7 notitle w candlesticks linestyle 5, \
	sprintf("%s/%s/3k-lat.data",outputdir,timestr) using ($2+0.1):7:7:7:7 notitle w candlesticks linestyle 5, \
	sprintf("%s/%s/5k-lat.data",outputdir,timestr) using ($2+0.15):7:7:7:7 notitle w candlesticks linestyle 5, \
	sprintf("%s/%s/1k-lat.data",outputdir,timestr) using 2:5:4:12:10 notitle with candlesticks whiskerbars 0.5 linestyle 1, \
	sprintf("%s/%s/2k-lat.data",outputdir,timestr) using ($2+0.05):5:4:12:10 notitle with candlesticks whiskerbars 0.5 linestyle 2, \
	sprintf("%s/%s/3k-lat.data",outputdir,timestr) using ($2+0.1):5:4:12:10 notitle with candlesticks whiskerbars 0.5 linestyle 3, \
	sprintf("%s/%s/5k-lat.data",outputdir,timestr) using ($2+0.15):5:4:12:10 notitle with candlesticks whiskerbars 0.5 linestyle 4 
