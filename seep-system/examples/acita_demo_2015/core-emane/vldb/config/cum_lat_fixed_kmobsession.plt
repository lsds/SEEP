set terminal term 
set output sprintf("%s/%s/%sk/%sm/%ss/cum_latency_fixed_kmobsession_%s%s",outputdir,timestr,k,mob,session,timestr,termext)

set title sprintf("Cumulative distribution of latency\nk=%s, mob=%s, query=%s, duration=%s, session=%s",k,mob,query,duration,session)
set xlabel "Latency (ms)"
set ylabel "Percentage of tuples with latency < x"
set yrange [0:100]

set border linewidth 1.5
set style line 1 linewidth 2.5 linecolor rgb "red"
set style line 2 linewidth 2.5 linecolor rgb "blue"
set style line 3 linewidth 2.5 linecolor rgb "green"
set style line 4 linewidth 2.5 linecolor rgb "pink"
#set boxwidth 0.1
set style fill empty 

plot sprintf("%s/%s/%sk/%sm/%ss/cum-lat.data",outputdir,timestr,k,mob,session) using 2:1 notitle w lines linestyle 1
