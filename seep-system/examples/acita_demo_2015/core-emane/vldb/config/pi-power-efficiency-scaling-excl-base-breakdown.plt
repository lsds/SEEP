set terminal term
set output sprintf("pi_power_efficiency_scaling_excl_base_breakdown_%s%s",expname,termext)
#set title sprintf("Power for query=%s excluding idle power",expname)
#set ylabel "Avg. Power Efficiency (J/Kb)" font ", 20"
set ylabel "Performance per Watt (Kb/W)" font ", 20"
set xlabel "Replication factor (r)" font ", 20"
#set xtics right offset 10,0
set xtics nomirror
set ytics nomirror
set tics font ", 16"
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
set key top left reverse Left
set key spacing 1.75 font ", 16"

#plot sprintf("%s/%s/results.txt",outputdir,timestr) using 2:xtic(1) with boxes
plot sprintf("%s/%s/results-net.txt",outputdir,timestr) using 2:xtic(1) t "Network", \
	sprintf("%s/%s/results-ops.txt",outputdir,timestr) using 2:xtic(1) t "Ops"
#	sprintf("%s/%s/results-cpu.txt",outputdir,timestr) using 2:xtic(1) t "CPU", \
