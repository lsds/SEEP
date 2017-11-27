load sprintf("%s/colours.plt",tmpldir)
set terminal term 
set output sprintf("%s/%s/tput_vs_dimension_stddev%s",outputdir,timestr,termext)

set xlabel "Grid size (m)" font ", 20" 
set ylabel "Throughput (Kb/s)" font ", 20" offset -2
#set yrange [0:100]
set xrange[890:3010]
set xtics autofreq 300
set ytics autofreq 200
set tics font ", 16"
#set format x "%d"

set border linewidth 1.5
set style line 1 linewidth 2.5 linecolor rgb "red"
set style line 2 linewidth 2.5 linecolor rgb "blue"
set style line 3 linewidth 2.5 linecolor rgb "green"
set style line 4 linewidth 2.5 linecolor rgb "pink"

#set boxwidth 0.1
set style fill empty 
set key spacing 1.75 font ", 16"
#set bmargin 4
#set lmargin 13 

plot sprintf("%s/%s/1k-tput.data",outputdir,timestr) using 1:2:xtic(sprintf("%d",$1)) title "r=1" w lines linestyle 1, \
	sprintf("%s/%s/2k-tput.data",outputdir,timestr) using 1:2:xtic(sprintf("%d",$1)) title "r=2" w lines linestyle 2, \
	sprintf("%s/%s/3k-tput.data",outputdir,timestr) using 1:2:xtic(sprintf("%d",$1)) title "r=3" w lines linestyle 3, \
	sprintf("%s/%s/5k-tput.data",outputdir,timestr) using 1:2:xtic(sprintf("%d",$1)) title "r=5" w lines linestyle 4, \
	sprintf("%s/%s/1k-tput.data",outputdir,timestr) using 1:2:4 notitle linestyle 1 with yerrorb, \
	sprintf("%s/%s/2k-tput.data",outputdir,timestr) using ($1+0.05):2:4 notitle linestyle 2 with yerrorb, \
	sprintf("%s/%s/3k-tput.data",outputdir,timestr) using ($1+0.10):2:4 notitle linestyle 3 with yerrorb, \
	sprintf("%s/%s/5k-tput.data",outputdir,timestr) using ($1+0.15):2:4 notitle linestyle 4 with yerrorb

#plot sprintf("%s/%s/1k-tput.data",outputdir,timestr) using 1:2:xtic(sprintf("%.1f",$1)) title "r=1" w lines linestyle 1, \
#	sprintf("%s/%s/2k-tput.data",outputdir,timestr) using 1:2:xtic(sprintf("%.1f",$1)) title "r=2" w lines linestyle 2, \
#	sprintf("%s/%s/3k-tput.data",outputdir,timestr) using 1:2:xtic(sprintf("%.1f",$1)) title "r=3" w lines linestyle 3, \
#	sprintf("%s/%s/5k-tput.data",outputdir,timestr) using 1:2:xtic(sprintf("%.1f",$1)) title "r=5" w lines linestyle 4, \
#	sprintf("%s/%s/1k-tput.data",outputdir,timestr) using 1:2:4 notitle linestyle 1 with yerrorb, \
#	sprintf("%s/%s/2k-tput.data",outputdir,timestr) using ($1+0.05):2:4 notitle linestyle 2 with yerrorb, \
#	sprintf("%s/%s/3k-tput.data",outputdir,timestr) using ($1+0.10):2:4 notitle linestyle 3 with yerrorb, \
#	sprintf("%s/%s/5k-tput.data",outputdir,timestr) using ($1+0.15):2:4 notitle linestyle 4 with yerrorb
