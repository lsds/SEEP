set terminal term 
set output sprintf("%s/%s/%sk/%s%s/%ss/tx_lat_fixed_kvarsession_%s%s",outputdir,timestr,k,var,varext,session,timestr,termext)

set title sprintf("End-to-end tuple latency \nk=%s, %s=%s, query=%s, duration=%s, session=%s",k,varname,var,query,duration,session)
set xlabel "Tx time (Seconds since epoch)"
set ylabel "Latency"
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

plot sprintf("%s/%s/%sk/%s%s/%ss/tx_latencies.txt",outputdir,timestr,k,var,varext,session) using 1:2 notitle w lines linestyle 1
