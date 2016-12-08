set terminal term 
set output sprintf("%s/%s/%sk/%s%s/%ss/cpu_util_fixed_kvarsession_%s%s",outputdir,timestr,k,var,varext,session,timestr,termext)

set title sprintf("Per-core cpu utilization \nk=%s, %s=%s, query=%s, duration=%s, session=%s",k,varname,var,query,duration,session)
set xlabel "Seconds since epoch"
set ylabel "Utilization"
set yrange [0:100]
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

#plot for [i=0:24] sprintf("%s/%s/%sk/%s%s/%ss/cpu-util.csv",outputdir,timestr,k,var,varext,session) using 151:(100-column(3+(i*6))) notitle w lines linestyle i+1
plot for [i=0:31] sprintf("%s/%s/%sk/%s%s/%ss/cpu-util.csv",outputdir,timestr,k,var,varext,session) using 193:(100-column(3+(i*6))) notitle w lines linestyle i+1
#plot sprintf("%s/%s/%sk/%sm/%ss/cpu-util.csv",outputdir,timestr,k,mob,session) using 193:(100-$3) notitle w lines linestyle 1, \
#	sprintf("%s/%s/%sk/%sm/%ss/cpu-util.csv",outputdir,timestr,k,mob,session) using 193:9 notitle w lines linestyle 2, \
#	sprintf("%s/%s/%sk/%sm/%ss/cpu-util.csv",outputdir,timestr,k,mob,session) using 193:15 notitle w lines linestyle 3, \
#	sprintf("%s/%s/%sk/%sm/%ss/cpu-util.csv",outputdir,timestr,k,mob,session) using 193:21 notitle w lines linestyle 4, \
#	sprintf("%s/%s/%sk/%sm/%ss/cpu-util.csv",outputdir,timestr,k,mob,session) using 193:27 notitle w lines linestyle 5, \
#	sprintf("%s/%s/%sk/%sm/%ss/cpu-util.csv",outputdir,timestr,k,mob,session) using 193:33 notitle w lines linestyle 6, \
#	sprintf("%s/%s/%sk/%sm/%ss/cpu-util.csv",outputdir,timestr,k,mob,session) using 193:39 notitle w lines linestyle 7, \
#	sprintf("%s/%s/%sk/%sm/%ss/cpu-util.csv",outputdir,timestr,k,mob,session) using 193:45 notitle w lines linestyle 8, \
#	sprintf("%s/%s/%sk/%sm/%ss/cpu-util.csv",outputdir,timestr,k,mob,session) using 193:51 notitle w lines linestyle 9, \
#	sprintf("%s/%s/%sk/%sm/%ss/cpu-util.csv",outputdir,timestr,k,mob,session) using 193:57 notitle w lines linestyle 10, \
#	sprintf("%s/%s/%sk/%sm/%ss/cpu-util.csv",outputdir,timestr,k,mob,session) using 193:63 notitle w lines linestyle 11, \
#	sprintf("%s/%s/%sk/%sm/%ss/cpu-util.csv",outputdir,timestr,k,mob,session) using 193:69 notitle w lines linestyle 12
