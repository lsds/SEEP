set terminal term 
set output sprintf("%s/%s/%sk/%s%s/%ss/emane-stats/%s_emane_stats_fixed_kvarsession_%s%s",outputdir,timestr,k,var,varext,session,stat,timestr,termext)

set title sprintf("%s \nk=%s, %s=%s, query=%s, duration=%s, session=%s",stat,k,varname,var,query,duration,session)
set xlabel "Seconds since epoch"
set ylabel sprintf("%s",stat)
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

delta_v(x) = ( vD = x - old_v, old_v = x, vD)
old_v = NaN

#plot for [i=3:27] sprintf("%s/%s/%sk/%s%s/%ss/emane-stats/n%d-%s-emane-stats.txt",outputdir,timestr,k,var,varext,session,i,stat) using 1:(delta_v($7)) title "n=".i w lines linestyle i
plot for [i=3:27] sprintf("%s/%s/%sk/%s%s/%ss/emane-stats/n%d-%s-emane-stats.txt",outputdir,timestr,k,var,varext,session,i,stat) using 1:7 title "n=".i w lines linestyle i
