set terminal term 
set output sprintf("%s/%s/latency_vs_mobility_cross_stderr%s",outputdir,timestr,termext)

set xlabel "Node speed (m/s)" font ", 20"
set ylabel sprintf("%s Latency (ms)",percentile) font ", 20" offset -3
#set yrange [0:100000]
#set yrange [0:5000]
set yrange [0:*]
set tics font ", 12"
#set xtics 0,5,15

set border linewidth 1.5
set style line 1 linewidth 2.5 linecolor rgb "red"
set style line 2 linewidth 2.5 linecolor rgb "blue"
set style line 3 linewidth 2.5 linecolor rgb "green"
set style line 4 linewidth 2.5 linecolor rgb "pink"
set boxwidth 0.1
set style fill empty 
set key top left spacing 1.75 font ", 18" at graph 0.1,0.9
#set key top left font ", 12"
set bmargin 4
set lmargin 15 

plot sprintf("%s/%s/rr/1k-lat.data",outputdir,timestr) using 2:10 title "k=1" w linespoints linestyle 1, \
	sprintf("%s/%s/rr/3k-lat.data",outputdir,timestr) using 2:10 title "RoundRobin k=3" w linespoints linestyle 2, \
	sprintf("%s/%s/bp/3k-lat.data",outputdir,timestr) using 2:10 title "Backpressure k=3" w linespoints linestyle 3
