set terminal term
set output sprintf("pi_tput_scaling_%s%s",expname,termext)
set title sprintf("Throughput for query=%s",expname)
set ylabel "Tput (Kb/s)"
set xlabel "Replication factor (r)"
#set xtics right offset 10,0
set xtics nomirror
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

plot sprintf("%s/%s/results.txt",outputdir,timestr) using 2:xtic(1) with boxes
