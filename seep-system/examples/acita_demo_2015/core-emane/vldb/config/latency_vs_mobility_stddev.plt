set terminal pdf 
set output sprintf("%s/%s/latency_vs_mobility_stddev.pdf",outputdir,timestr)

set xlabel "Node speed (m/s)" font ", 16" offset 1
set ylabel "Latency (ms)" font ", 16" offset 1
#set yrange [0:100]
set tics font ", 8"

set border linewidth 1.5
set style line 1 linewidth 2.5 linecolor rgb "red"
set style line 2 linewidth 2.5 linecolor rgb "blue"
set style line 3 linewidth 2.5 linecolor rgb "green"
set style line 4 linewidth 2.5 linecolor rgb "pink"
set boxwidth 0.1
set style fill empty 

plot sprintf("%s/%s/1k-lat.data",outputdir,timestr) using 1:2 title "k=1" w lines linestyle 1, \
	sprintf("%s/%s/2k-lat.data",outputdir,timestr) using 1:2 title "k=2" w lines linestyle 2, \
	sprintf("%s/%s/3k-lat.data",outputdir,timestr) using 1:2 title "k=3" w lines linestyle 3, \
	sprintf("%s/%s/5k-lat.data",outputdir,timestr) using 1:2 title "k=5" w lines linestyle 4, \
	sprintf("%s/%s/1k-lat.data",outputdir,timestr) using 1:2:4 notitle linestyle 1 w yerrorb, \
	sprintf("%s/%s/2k-lat.data",outputdir,timestr) using ($1+0.05):2:4 notitle linestyle 2 w yerrorb, \
	sprintf("%s/%s/3k-lat.data",outputdir,timestr) using ($1+0.1):2:4 notitle linestyle 3 w yerrorb, \
	sprintf("%s/%s/5k-lat.data",outputdir,timestr) using ($1+0.15):2:4 notitle linestyle 4 w yerrorb
