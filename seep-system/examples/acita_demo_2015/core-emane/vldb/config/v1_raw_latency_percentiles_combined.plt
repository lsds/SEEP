load sprintf("%s/colours.plt",tmpldir)
set terminal term size 5, 2
#set terminal pdf 
#set size 0.8,0.6
set output sprintf("%s/%s/combined_raw_latency_percentiles%s",outputdir,timestr,termext)

#set title sprintf("Latency (5th/LQ/median/UQ/95th) vs replication factor (r)\n") font ",10"
#set xlabel "Replication factor (k)" font ", 20"
#set ylabel "Latency\n (5th/LQ/median/UQ/95th ptile., ms)" font ", 20" offset -2
#set ylabel "Latency (ms)\n [5th/LQ/median/UQ/95th ptile.]"
set ylabel "Latency (ms)"
set xlabel " "

#set yrange [0:100]
#set xrange [1:5]
set xrange [0:4]
# Only have xtic labels for incr's of 1
#set xtics 1
#set tics font ", 16"
#set tics font ", 12"
set tics nomirror

set border linewidth 1.5
set style line 6 linewidth 2.5 linecolor rgb "black"
set style line 1 linewidth 2.5 linecolor rgb "red"
set style line 2 linewidth 2.5 linecolor rgb "blue"
set style line 3 linewidth 2.5 linecolor rgb "green"
set style line 4 linewidth 2.5 linecolor rgb "pink"
set style line 5 linewidth 2.5 linecolor rgb "violet"

#set style line 1 linewidth 2.5 linecolor rgb CRIMSON 
#set style line 2 linewidth 2.5 linecolor rgb CADMIUMORANGE 
#set style line 3 linewidth 2.5 linecolor rgb COBALT 
#set style line 4 linewidth 2.5 linecolor rgb GREY 

set boxwidth 0.1
#Leave space at edges
#set offset 0.1,0.1
#set style fill empty 
set style fill solid border rgb BLACK
set key top right 

#11-38-21-Mon051216
#15-10-41-Tue101017
#18-16-24-Tue201216

#plot sprintf("%s/%s/1k/%s%s/1k-lat.data",outputdir,timestr,var,varext) using 1:7:7:7:7 notitle w candlesticks linestyle 5, \
#	sprintf("%s/%s/2k/%s%s/2k-lat.data",outputdir,timestr,var,varext) using 1:7:7:7:7 notitle w candlesticks linestyle 5, \
#	sprintf("%s/%s/3k/%s%s/3k-lat.data",outputdir,timestr,var,varext) using 1:7:7:7:7 notitle w candlesticks linestyle 5, \
#	sprintf("%s/%s/5k/%s%s/5k-lat.data",outputdir,timestr,var,varext) using 1:7:7:7:7 notitle w candlesticks linestyle 5, \
#	sprintf("%s/%s/1k/%s%s/1k-lat.data",outputdir,timestr,var,varext) using 1:6:5:10:8 notitle with candlesticks whiskerbars 0.5 linestyle 1, \
#	sprintf("%s/%s/2k/%s%s/2k-lat.data",outputdir,timestr,var,varext) using 1:6:5:10:8 notitle with candlesticks whiskerbars 0.5 linestyle 2, \
#	sprintf("%s/%s/3k/%s%s/3k-lat.data",outputdir,timestr,var,varext) using 1:6:5:10:8 notitle with candlesticks whiskerbars 0.5 linestyle 3, \
#	sprintf("%s/%s/5k/%s%s/5k-lat.data",outputdir,timestr,var,varext) using 1:6:5:10:8 notitle with candlesticks whiskerbars 0.5 linestyle 4 

#plot sprintf("%s/%s/combined-all-k-lat.data",outputdir,timestr) using 2:4:xticlabels(1) linestyle -3, \
#	'' using ($2+($3 * 0.15)-0.3):8:7:12:10 notitle w candlesticks whiskerbars 0.5 linestyle 5, \
#	''  using ($2+($3 * 0.15)-0.3):9:9:9:9 notitle w candlesticks linestyle 5, \
#

plot sprintf("%s/%s/combined-1k-lat.data",outputdir,timestr) using 2:4:xticlabels(1) linestyle -3 notitle, \
	'' using ($2+($3 * 0.15)-0.3):8:7:12:10 w candlesticks whiskerbars 0.5 linestyle 1 title "r=1", \
	''  using ($2+($3 * 0.15)-0.3):9:9:9:9 notitle w candlesticks  linestyle 6, \
	sprintf("%s/%s/combined-2k-lat.data",outputdir,timestr) using 2:4:xticlabels(1) linestyle -3 notitle, \
	'' using ($2+($3 * 0.15)-0.3):8:7:12:10 w candlesticks whiskerbars 0.5 linestyle 2 title "r=2", \
	''  using ($2+($3 * 0.15)-0.3):9:9:9:9 notitle w candlesticks  linestyle 6, \
	sprintf("%s/%s/combined-3k-lat.data",outputdir,timestr) using 2:4:xticlabels(1) linestyle -3 notitle, \
	'' using ($2+($3 * 0.15)-0.3):8:7:12:10 w candlesticks whiskerbars 0.5 linestyle 3 title "r=3", \
	''  using ($2+($3 * 0.15)-0.3):9:9:9:9 notitle w candlesticks  linestyle 6, \
	sprintf("%s/%s/combined-4k-lat.data",outputdir,timestr) using 2:4:xticlabels(1) linestyle -3 notitle, \
	'' using ($2+($3 * 0.15)-0.3):8:7:12:10 w candlesticks whiskerbars 0.5 linestyle 4 title "r=4", \
	''  using ($2+($3 * 0.15)-0.3):9:9:9:9 notitle w candlesticks  linestyle 6, \
	sprintf("%s/%s/combined-5k-lat.data",outputdir,timestr) using 2:4:xticlabels(1) linestyle -3 notitle, \
	'' using ($2+($3 * 0.15)-0.3):8:7:12:10 w candlesticks whiskerbars 0.5  linestyle 5 title "r=5", \
	''  using ($2+($3 * 0.15)-0.3):9:9:9:9 notitle w candlesticks  linestyle 6, \

#	sprintf("%s/%s/combined-all-k-lat.data",outputdir,timestr) using $(2+3):7:8:12:10 notitle w candlesticks linestyle 5, \
#	sprintf("%s/%s/combined-all-k-lat.data",outputdir,timestr) using $(2+3):2:9:9:9:9 notitle w candlesticks linestyle 5, \
#	sprintf("%s/%s/2k/%s%s/2k-lat.data",outputdir,timestr,var,varext) using 1:7:7:7:7 notitle w candlesticks linestyle 5, \
#	sprintf("%s/%s/3k/%s%s/3k-lat.data",outputdir,timestr,var,varext) using 1:7:7:7:7 notitle w candlesticks linestyle 5, \
#	sprintf("%s/%s/5k/%s%s/5k-lat.data",outputdir,timestr,var,varext) using 1:7:7:7:7 notitle w candlesticks linestyle 5, \
#	sprintf("%s/%s/1k/%s%s/1k-lat.data",outputdir,timestr,var,varext) using 1:6:5:10:8 notitle with candlesticks whiskerbars 0.5 linestyle 1, \
#	sprintf("%s/%s/2k/%s%s/2k-lat.data",outputdir,timestr,var,varext) using 1:6:5:10:8 notitle with candlesticks whiskerbars 0.5 linestyle 2, \
#	sprintf("%s/%s/3k/%s%s/3k-lat.data",outputdir,timestr,var,varext) using 1:6:5:10:8 notitle with candlesticks whiskerbars 0.5 linestyle 3, \
#	sprintf("%s/%s/5k/%s%s/5k-lat.data",outputdir,timestr,var,varext) using 1:6:5:10:8 notitle with candlesticks whiskerbars 0.5 linestyle 4 


