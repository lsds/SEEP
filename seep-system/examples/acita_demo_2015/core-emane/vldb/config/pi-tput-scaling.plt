set terminal term
set output sprintf("pi_tput_scaling_%s%s",expname,termext)
#set title sprintf("Throughput for query=%s",expname)
set ylabel "Tput (Kb/s)"
set xlabel "Replication factor (r)"
#set xtics right offset 10,0
#set xtics nomirror
set xtics nomirror
set xtics 1,1,3
set ytics nomirror
set auto x
set yrange [0:*]
set style data histogram
#set style histogram cluster gap 2
#set style histogram cluster
#set style histogram
set style fill solid border -1
#set boxwidth 0.9
set boxwidth 0.5
set key off

set style line 1 linewidth 2.5 linecolor rgb "red"
set style line 2 linewidth 2.5 linecolor rgb "blue"
set style line 3 linewidth 2.5 linecolor rgb "green"
set style line 4 linewidth 2.5 linecolor rgb "pink"


plot sprintf("%s/%s/results.txt",outputdir,timestr) using 1:2:(0.2):1 with boxes lc variable
