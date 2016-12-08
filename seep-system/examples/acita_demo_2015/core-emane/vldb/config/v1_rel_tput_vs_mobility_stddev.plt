load sprintf("%s/colours.plt",tmpldir)
set terminal term 
set output sprintf("%s/%s/rel_tput_vs_mobility_stddev%s",outputdir,timestr,termext)

set title "Relative throughput for different replication factors (k)"
set xlabel "Replication factor (k)"
set ylabel "Relative throughput"
#set yrange [0:100]
set yrange [0:*]
set tics font ", 10"

set border linewidth 1.5
set style line 1 lw 5 lt 1 linecolor rgb CRIMSON 
set style line 3 lw 5 lt 3 linecolor rgb BLUE 
set style line 2 lw 5 lt 2 linecolor rgb CADMIUMORANGE 
set style line 4 lw 5 lt 2 linecolor rgb GOLDENROD

set style data histograms
#set style histogram cluster gap 1
#set style histogram errorbars gap 2 lw 1
set style histogram errorbars gap 10 lw 5 
set style fill empty 
set boxwidth 2 absolute
#set border linewidth 1.5

set key off 
set bmargin 4
set lmargin 13 

plot sprintf("%s/%s/all-k-rel-tput.data",outputdir,timestr) using 2:4:xticlabels(3) ls 1 

#plot sprintf("%s/%s/1k-rel-tput.data",outputdir,timestr) using 1:2 title "k=1" w lines linestyle 1, \
#	sprintf("%s/%s/2k-rel-tput.data",outputdir,timestr) using 1:2 title "k=2" w lines linestyle 2, \
#	sprintf("%s/%s/3k-rel-tput.data",outputdir,timestr) using 1:2 title "k=3" w lines linestyle 3, \
#	sprintf("%s/%s/5k-rel-tput.data",outputdir,timestr) using 1:2 title "k=5" w lines linestyle 4, \
#	sprintf("%s/%s/1k-rel-tput.data",outputdir,timestr) using 1:2:4 notitle linestyle 1 with yerrorb, \
#	sprintf("%s/%s/2k-rel-tput.data",outputdir,timestr) using ($1+0.05):2:4 notitle linestyle 2 with yerrorb, \
#	sprintf("%s/%s/3k-rel-tput.data",outputdir,timestr) using ($1+0.10):2:4 notitle linestyle 3 with yerrorb, \
#	sprintf("%s/%s/5k-rel-tput.data",outputdir,timestr) using ($1+0.15):2:4 notitle linestyle 4 with yerrorb
