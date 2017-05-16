set terminal term
set output sprintf("sr_retransmissions%s",termext)
set title sprintf("Retransmissions")
set ylabel "Total tx (batches)"
set xlabel "Bottleneck"
set xtics right offset 4,0
set xtics nomirror
set ytics nomirror
set auto x
set yrange [0:*]
set style data histogram
set style histogram cluster gap 2
set style fill solid border -1
set boxwidth 0.9

plot sprintf("%s/%s/sr_retransmissions.txt",outputdir,timestr) using 2:xtic(1) title col, '' u 3 ti col
