load sprintf("%s/colours.plt",tmpldir)
set terminal term 
set output sprintf("%s/%s/latency_vs_retx_timeout_stddev%s",outputdir,timestr,termext)

set xlabel "Retransmit timeout (s)" font ", 20"
set ylabel sprintf("Latency (ms)\n min/5th/med./95th/max") font ", 20" offset -3
#set yrange [0:100000]
#set yrange [0:5000]
set yrange [0.1:*]
#set logscale y
set tics font ", 16"
#set xtics 0,5,15

set border linewidth 1.5
set style line 1 linewidth 2.5 linecolor rgb CRIMSON 
set style line 2 linewidth 2.5 linecolor rgb CADMIUMORANGE 
set style line 3 linewidth 2.5 linecolor rgb COBALT 
set style line 4 linewidth 2.5 linecolor rgb GREY 
set style line 5 linewidth 2.5 linecolor rgb "black" 

set boxwidth 0.1
set style fill empty 
#set key top left spacing 1.75 font ", 12" at graph 0.1,0.9
#set key top left font ", 12"
#set bmargin 4
#set lmargin 15 

plot sprintf("%s/%s/1k-lat.data",outputdir,timestr) using ($2/1000):7:7:7:7 notitle w candlesticks linestyle 5, \
	sprintf("%s/%s/2k-lat.data",outputdir,timestr) using (($2/1000)+0.05):7:7:7:7 notitle w candlesticks linestyle 5, \
	sprintf("%s/%s/3k-lat.data",outputdir,timestr) using (($2/1000)+0.1):7:7:7:7 notitle w candlesticks linestyle 5, \
	sprintf("%s/%s/5k-lat.data",outputdir,timestr) using (($2/1000)+0.15):7:7:7:7 notitle w candlesticks linestyle 5, \
	sprintf("%s/%s/1k-lat.data",outputdir,timestr) using ($2/1000):5:4:12:10 notitle with candlesticks whiskerbars 0.5 linestyle 1, \
	sprintf("%s/%s/2k-lat.data",outputdir,timestr) using (($2/1000)+0.05):5:4:12:10 notitle with candlesticks whiskerbars 0.5 linestyle 2, \
	sprintf("%s/%s/3k-lat.data",outputdir,timestr) using (($2/1000)+0.1):5:4:12:10 notitle with candlesticks whiskerbars 0.5 linestyle 3, \
	sprintf("%s/%s/5k-lat.data",outputdir,timestr) using (($2/1000)+0.15):5:4:12:10 notitle with candlesticks whiskerbars 0.5 linestyle 4 

#plot sprintf("%s/%s/1k-lat.data",outputdir,timestr) using 1:2 title "k=1" w lines linestyle 1, \
#	sprintf("%s/%s/2k-lat.data",outputdir,timestr) using 1:2 title "k=2" w lines linestyle 2, \
#	sprintf("%s/%s/3k-lat.data",outputdir,timestr) using 1:2 title "k=3" w lines linestyle 3, \
#	sprintf("%s/%s/5k-lat.data",outputdir,timestr) using 1:2 title "k=5" w lines linestyle 4, \
#	sprintf("%s/%s/1k-lat.data",outputdir,timestr) using 1:2:4 notitle linestyle 1 w yerrorb, \
#	sprintf("%s/%s/2k-lat.data",outputdir,timestr) using ($1+0.05):2:4 notitle linestyle 2 w yerrorb, \
#	sprintf("%s/%s/3k-lat.data",outputdir,timestr) using ($1+0.1):2:4 notitle linestyle 3 w yerrorb, \
#	sprintf("%s/%s/5k-lat.data",outputdir,timestr) using ($1+0.15):2:4 notitle linestyle 4 w yerrorb


