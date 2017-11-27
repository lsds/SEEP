load sprintf("%s/colours.plt",tmpldir)
set terminal term
set output sprintf("sr_retransmissions%s",termext)
#set title sprintf("Retransmissions")
set ylabel "Total tx (batches)" font ", 20"
set xlabel "Bottleneck" font ", 20" offset 1
set xtics right offset 4,0
set tics font ", 16"
set xtics nomirror
set ytics nomirror
set auto x
set yrange [0:*]
set style data histogram
set style histogram cluster errorbars gap 2 lw 2
#set style fill solid border -1
set style fill solid border rgb BLACK 
set boxwidth 0.9
set key spacing 1.75 font ", 16"

plot sprintf("%s/%s/sr_retransmissions.txt",outputdir,timestr) using 2:4:xtic(1) title col, '' u 3:5 ti col
