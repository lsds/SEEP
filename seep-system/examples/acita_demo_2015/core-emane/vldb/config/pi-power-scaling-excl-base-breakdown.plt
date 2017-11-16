set terminal term
set output sprintf("pi_power_scaling_excl_base_%s%s",expname,termext)
set title sprintf("Power for query=%s excluding idle power",expname)
set ylabel "Avg. Power (W)"
set xlabel "Replication factor (r)"
#set xtics right offset 10,0
set xtics nomirror
set ytics nomirror
set auto x
set yrange [0:*]
set style data histogram
set style histogram rowstacked
#set style histogram cluster gap 2
#set style histogram cluster
#set style histogram
set style fill solid border -1
#set boxwidth 0.9
set boxwidth 0.5
#set key off
set key top left

#plot sprintf("%s/%s/results.txt",outputdir,timestr) using 2:xtic(1) with boxes
plot sprintf("%s/%s/results-net.txt",outputdir,timestr) using 2:xtic(1) t "Network", \
	sprintf("%s/%s/results-ops.txt",outputdir,timestr) using 2:xtic(1) t "Ops"
#	sprintf("%s/%s/results-cpu.txt",outputdir,timestr) using 2:xtic(1) t "CPU", \
