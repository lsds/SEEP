term="pdf"
termext=".pdf"
set terminal term 
set output sprintf("sr_retransmissions%s",termext)
set title sprintf("Redundant retransmissions")
set ylabel "Total tx (batches)"
set auto x
set yrange [0:*]
set style data histogram
set style histogram cluster gap 2
set style fill solid border -1
set boxwidth 0.9

plot sprintf("%s/sr_retransmissions.txt") using 2:xtic(1) title col, '' u 3 ti col
#plot sprintf("%s/sr_retransmissions.txt", expdir) using 2:xticlabels(1) notitle w boxes linestyle 1
