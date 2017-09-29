set terminal term 
set output sprintf("%s/%s/tput_vs_mobility_cross_stderr%s",outputdir,timestr,termext)

set xlabel "Node speed (m/s)" font ", 20" 
set ylabel "Throughput (Kb/s)" font ", 20" offset -2
#set yrange [0:100]
set tics font ", 12"

set border linewidth 1.5
set style line 1 linewidth 2.5 linecolor rgb "red"
set style line 2 linewidth 2.5 linecolor rgb "blue"
set style line 3 linewidth 2.5 linecolor rgb "green"
set style line 4 linewidth 2.5 linecolor rgb "pink"
set boxwidth 0.1
set style fill empty 
set key spacing 1.75 font ", 18"
set bmargin 4
set lmargin 13 

plot sprintf("%s/%s/rr/1k-tput.data",outputdir,timestr) using 1:2 title "k=1" w lines linestyle 1, \
	sprintf("%s/%s/rr/3k-tput.data",outputdir,timestr) using 1:2 title "RoundRobin k=3" w lines linestyle 2, \
	sprintf("%s/%s/bp/3k-tput.data",outputdir,timestr) using 1:2 title "Backpressure k=3" w lines linestyle 3, \
	sprintf("%s/%s/rr/1k-tput.data",outputdir,timestr) using 1:2:4 notitle linestyle 1 with yerrorb, \
	sprintf("%s/%s/rr/3k-tput.data",outputdir,timestr) using ($1+0.02):2:4 notitle linestyle 2 with yerrorb, \
	sprintf("%s/%s/bp/3k-tput.data",outputdir,timestr) using ($1+0.04):2:4 notitle linestyle 3 with yerrorb
