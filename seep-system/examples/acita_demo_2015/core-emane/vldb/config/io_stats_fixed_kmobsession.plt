set terminal term 
set output sprintf("%s/%s/%sk/%sm/%ss/io_stats_fixed_kmobsession_%s%s",outputdir,timestr,k,mob,session,timestr,termext)

set title sprintf("io reads/writes \nk=%s, mob=%s, query=%s, duration=%s, session=%s",k,mob,query,duration,session)
set xlabel "Seconds since epoch"
set ylabel "IO reads/writes"
set yrange [0:*]
set xtics font ",4"
set xdata time
set timefmt "%s"

set border linewidth 1.5
set style line 1 linewidth 2.5 linecolor rgb "red"
set style line 2 linewidth 2.5 linecolor rgb "blue"
set style line 3 linewidth 2.5 linecolor rgb "green"
set style line 4 linewidth 2.5 linecolor rgb "pink"
#set boxwidth 0.1
set style fill empty 
set datafile separator ","

plot sprintf("%s/%s/%sk/%sm/%ss/cpu-util.csv",outputdir,timestr,k,mob,session) using 193:196 notitle w lines linestyle 1, \
	sprintf("%s/%s/%sk/%sm/%ss/cpu-util.csv",outputdir,timestr,k,mob,session) using 193:197 notitle w lines linestyle 2
