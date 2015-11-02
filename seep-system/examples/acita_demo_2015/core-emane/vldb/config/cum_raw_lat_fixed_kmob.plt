set terminal term 
set output sprintf("%s/%s/%sk/%sm/cum_raw_latency_fixed_kmob_%s%s",outputdir,timestr,k,mob,timestr,termext)

set title sprintf("Cumulative distribution of raw latency for %s runs\nk=%s, mob=%s, query=%s, duration=%s",runs,k,mob,query,duration)
set xlabel "Latency (ms)"
set ylabel "Percentage of runs with latency < x"
set yrange [0:100]

set border linewidth 1.5
set style line 1 linewidth 2.5 linecolor rgb "red"
set style line 2 linewidth 2.5 linecolor rgb "blue"
set style line 3 linewidth 2.5 linecolor rgb "green"
set style line 4 linewidth 2.5 linecolor rgb "pink"
#set boxwidth 0.1
set style fill empty 

plot sprintf("%s/%s/%sk/%sm/cum-raw-lat.data",outputdir,timestr,k,mob) using 2:1 notitle w lines linestyle 1
