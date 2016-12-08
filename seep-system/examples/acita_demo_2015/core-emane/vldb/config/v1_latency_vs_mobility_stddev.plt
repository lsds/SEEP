load sprintf("%s/colours.plt",tmpldir)
set terminal term 
set output sprintf("%s/%s/latency_vs_mobility_stddev%s",outputdir,timestr,termext)

set xlabel "Replication factor (k)" font ", 20"
set ylabel sprintf("%s Latency (ms)",percentile) font ", 20" offset -3
#set yrange [0:5000]
set yrange [0:*]
set tics font ", 16"
#set xtics 0,5,15

#set border linewidth 1.5
#set style line 1 linewidth 2.5 linecolor rgb "red"
#set style line 2 linewidth 2.5 linecolor rgb "blue"
#set style line 3 linewidth 2.5 linecolor rgb "green"
#set style line 4 linewidth 2.5 linecolor rgb "pink"
#set boxwidth 0.1
#set style fill empty 
#set key bottom right spacing 1.75 font ", 12"
#set bmargin 4
#set lmargin 15 

set border linewidth 1.5
set style line 1 lw 5 lt 1 linecolor rgb CRIMSON 
set style line 3 lw 5 lt 3 linecolor rgb BLUE 
set style line 2 lw 5 lt 2 linecolor rgb CADMIUMORANGE 
set style line 4 lw 5 lt 2 linecolor rgb GOLDENROD

set style data histograms
set style histogram errorbars gap 10 lw 5 
set style fill empty 
set boxwidth 2 absolute

set key off 
set bmargin 4
set lmargin 13 

plot sprintf("%s/%s/all-k-lat.data",outputdir,timestr) using 2:4:xticlabels(3) ls 1 
#plot sprintf("%s/%s/1k-lat.data",outputdir,timestr) using 2:4 title "k=1" w lines linestyle 1, \
#	sprintf("%s/%s/2k-lat.data",outputdir,timestr) using 2:4 title "k=2" w lines linestyle 2, \
#	sprintf("%s/%s/3k-lat.data",outputdir,timestr) using 2:4 title "k=3" w lines linestyle 3, \
#	sprintf("%s/%s/5k-lat.data",outputdir,timestr) using 2:4 title "k=5" w lines linestyle 4
#	sprintf("%s/%s/5k-lat.data",outputdir,timestr) using 2:4 title "k=5" w lines linestyle 4, \
#	sprintf("%s/%s/1k-lat.data",outputdir,timestr) using 1:2:4 notitle linestyle 1 w yerrorb, \
#	sprintf("%s/%s/2k-lat.data",outputdir,timestr) using ($1+0.05):2:4 notitle linestyle 2 w yerrorb, \
#	sprintf("%s/%s/3k-lat.data",outputdir,timestr) using ($1+0.1):2:4 notitle linestyle 3 w yerrorb, \
#	sprintf("%s/%s/5k-lat.data",outputdir,timestr) using ($1+0.15):2:4 notitle linestyle 4 w yerrorb
