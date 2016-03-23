set terminal term 
set output sprintf("%s/%s/rel_tput_vs_dimension_stddev%s",outputdir,timestr,termext)

set title "Relative throughput vs Grid size for different replication factors (k)"
set xlabel "Grid size (m)"
set ylabel "Relative throughput"
#set yrange [0:100]

set border linewidth 1.5
set style line 1 linewidth 2.5 linecolor rgb "red"
set style line 2 linewidth 2.5 linecolor rgb "blue"
set style line 3 linewidth 2.5 linecolor rgb "green"
set style line 4 linewidth 2.5 linecolor rgb "pink"
set boxwidth 0.1
set style fill empty 

plot sprintf("%s/%s/1k-rel-tput.data",outputdir,timestr) using 1:2:xtic(sprintf("%.1f",$1)) title "k=1" w lines linestyle 1, \
	sprintf("%s/%s/2k-rel-tput.data",outputdir,timestr) using 1:2:xtic(sprintf("%.1f",$1)) title "k=2" w lines linestyle 2, \
	sprintf("%s/%s/3k-rel-tput.data",outputdir,timestr) using 1:2:xtic(sprintf("%.1f",$1)) title "k=3" w lines linestyle 3, \
	sprintf("%s/%s/5k-rel-tput.data",outputdir,timestr) using 1:2:xtic(sprintf("%.1f",$1)) title "k=5" w lines linestyle 4, \
	sprintf("%s/%s/1k-rel-tput.data",outputdir,timestr) using 1:2:4 notitle linestyle 1 with yerrorb, \
	sprintf("%s/%s/2k-rel-tput.data",outputdir,timestr) using ($1+0.05):2:4 notitle linestyle 2 with yerrorb, \
	sprintf("%s/%s/3k-rel-tput.data",outputdir,timestr) using ($1+0.10):2:4 notitle linestyle 3 with yerrorb, \
	sprintf("%s/%s/5k-rel-tput.data",outputdir,timestr) using ($1+0.15):2:4 notitle linestyle 4 with yerrorb
