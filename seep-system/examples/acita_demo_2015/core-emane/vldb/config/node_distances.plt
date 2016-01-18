set terminal term 
set output sprintf("%s/%s/%sk/%sm/%ss/positions/%s_distances_fixed_kmobsession_%s%s",outputdir,timestr,k,mob,session,node,timestr,termext)

set title sprintf("Node %s distances to other nodes \nk=%s, mob=%s, query=%s, duration=%s, session=%s",node,k,mob,query,duration,session)
set xlabel "Tx time (Seconds since epoch)"
set ylabel "Distance"
set yrange [0:*]
set xtics font ",4"
#set xdata time
#set timefmt "%s"

set border linewidth 1.5
set style line 1 linewidth 2.5 linecolor rgb "red"
set style line 2 linewidth 2.5 linecolor rgb "blue"
set style line 3 linewidth 2.5 linecolor rgb "green"
set style line 4 linewidth 2.5 linecolor rgb "pink"
#set boxwidth 0.1
set style fill empty 

#plot for [i=3:1] sprintf("%s/%s/%sk/%sm/%ss/cpu-util.csv",outputdir,timestr,k,mob,session) using 193:(100-column(3+(i*6))) notitle w lines linestyle i+1
plot for [i=3:9] sprintf("%s/%s/%sk/%sm/%ss/positions/%sn%d.dist",outputdir,timestr,k,mob,session,node,i) using 1:4 title 'n'.i w lines linestyle i+1
#plot sprintf("%s/%s/%sk/%sm/%ss/n7n9.dist",outputdir,timestr,k,mob,session) using 1:4 notitle w lines linestyle 1

#plot sprintf("%s/%s/%sk/%sm/%ss/positions/%sn3.dist",outputdir,timestr,k,mob,session,node) using 1:4 title 'n3' w lines linestyle 1, \
#	sprintf("%s/%s/%sk/%sm/%ss/positions/%sn4.dist",outputdir,timestr,k,mob,session,node) using 1:4 title 'n4' w lines linestyle 2, \
#	sprintf("%s/%s/%sk/%sm/%ss/positions/%sn5.dist",outputdir,timestr,k,mob,session,node) using 1:4 title 'n5' w lines linestyle 3, \
#	sprintf("%s/%s/%sk/%sm/%ss/positions/%sn6.dist",outputdir,timestr,k,mob,session,node) using 1:4 title 'n6' w lines linestyle 4, \
#	sprintf("%s/%s/%sk/%sm/%ss/positions/%sn7.dist",outputdir,timestr,k,mob,session,node) using 1:4 title 'n7' w lines linestyle 5, \
#	sprintf("%s/%s/%sk/%sm/%ss/positions/%sn8.dist",outputdir,timestr,k,mob,session,node) using 1:4 title 'n8' w lines linestyle 6
#	sprintf("%s/%s/%sk/%sm/%ss/positions/%sn9.dist",outputdir,timestr,k,mob,session,node) using 1:4 title 'n9' w lines linestyle 7
