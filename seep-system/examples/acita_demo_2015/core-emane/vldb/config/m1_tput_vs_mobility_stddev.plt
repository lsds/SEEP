#load "gp-style.plt"
load sprintf("%s/colours.plt",tmpldir)
#set output plotname
set terminal term 
set output sprintf("%s/%s/tput_vs_mobility_stddev%s",outputdir,timestr,termext)

set xlabel "Replication factor (k)" font ", 20"
set ylabel "Throughput (Kb/s)" font ", 20" offset -2
set yrange [0:*]
#set yrange [0:100]
set tics font ", 16"
#set xtics offset 1,1,3

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

#set style line 1 linewidth 2.5 linecolor rgb "red"
#set style line 2 linewidth 2.5 linecolor rgb "blue"
#set style line 3 linewidth 2.5 linecolor rgb "green"
#set style line 4 linewidth 2.5 linecolor rgb "pink"
#set boxwidth 0.1
#set style fill empty 
set key off 
set bmargin 4
set lmargin 13 

#plot "trackable-dupes-rel-exclude-groups.data" using 1:2 title "Safe to track" w lines linestyle 1, \
#	"trackable-distinct-covered.data" using 1:2 title "Length > Min. length" w lines linestyle 3
plot sprintf("%s/%s/all-k-tput.data",outputdir,timestr) using 2:4:xticlabels(3) ls 1 

#plot sprintf("%s/%s/1k-tput.data",outputdir,timestr) using 1:2 title "k=1" w lines linestyle 1, \
#	sprintf("%s/%s/2k-tput.data",outputdir,timestr) using 1:2 title "k=2" w lines linestyle 2, \
#	sprintf("%s/%s/3k-tput.data",outputdir,timestr) using 1:2 title "k=3" w lines linestyle 3, \
#	sprintf("%s/%s/5k-tput.data",outputdir,timestr) using 1:2 title "k=5" w lines linestyle 4, \
#	sprintf("%s/%s/1k-tput.data",outputdir,timestr) using 1:2:4 notitle linestyle 1 with yerrorb, \
#	sprintf("%s/%s/2k-tput.data",outputdir,timestr) using ($1+0.05):2:4 notitle linestyle 2 with yerrorb, \
#	sprintf("%s/%s/3k-tput.data",outputdir,timestr) using ($1+0.10):2:4 notitle linestyle 3 with yerrorb, \
#	sprintf("%s/%s/5k-tput.data",outputdir,timestr) using ($1+0.15):2:4 notitle linestyle 4 with yerrorb
