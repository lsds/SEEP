load sprintf("%s/colours.plt",tmpldir)
set terminal term size 5, 2  
#set terminal term
set output sprintf("pi_tput_scaling_all_%s%s",expname,termext)

set style line 1 linewidth 2.5 linecolor rgb "red"
set style line 2 linewidth 2.5 linecolor rgb "blue"
set style line 3 linewidth 2.5 linecolor rgb "green"
set style line 4 linewidth 2.5 linecolor rgb "pink"
set style line 5 linewidth 2.5 linecolor rgb "violet"

set style fill solid border rgb BLACK 

set ylabel "Tput (Kb/s)" font ", 20"
#set xlabel "Replication factor (r)" font ", 20"
set xlabel " "

set tics nomirror
#set auto x
set yrange [0:*]

set border linewidth 1.5

set style data histograms
#set style histogram cluster gap 4
set style histogram errorbars gap 1.5 lw 2

#set offset -0.5, -0.5, 0, 0
set offset -0.4,-0.4,0,0
set key top left
plot sprintf("%s/%s/results-combined.txt",outputdir,timestr) using 3:4:xtic(1) title col ls 1, \
									'' using 6:7:xtic(1) title col ls 2, \
									'' using 9:10:xtic(1) title col ls 3

#plot sprintf("%s/pi_results/pi-tput-scaling/results.txt",outputdir,timestr) using 2:3:xtic(1) title col ls 1, \
#	sprintf("%s/pi_results/pi-tput-scaling-dense/results.txt",outputdir,timestr) using 2:3:xtic(1) title col ls 1, \

#plot sprintf("%s/%s/combined-all-k-tput.data",outputdir,timestr) using 2:3:xtic(1) title col ls 1, \
#							'' using 4:5:xtic(1) title col ls 2, \
#							'' using 6:7:xtic(1) title col ls 3, \
#							'' using 8:9:xtic(1) title col ls 4, \
#							'' using 10:11:xtic(1) title col ls 5, \

#plot sprintf("%s/%s/results.txt",outputdir,timestr) using 1:2:(0.2):1 with boxes lc variable
#plot sprintf("%s/%s/results.txt",outputdir,timestr) using 1:2:(0.2):1 with boxes lc variable
