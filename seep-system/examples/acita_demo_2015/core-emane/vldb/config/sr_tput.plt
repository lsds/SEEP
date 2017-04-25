term="pdf"
termext=".pdf"
set terminal term 
set output sprintf("sr_tput_%s",termext)
set title sprintf("Recovery throughput")
set ylabel "Tput (Kb/s)"
set auto x
set yrange [0:*]
set style data histogram
set style histogram cluster gap 2
set style fill solid border -1
set boxwidth 0.9

plot sprintf("sr_tput.txt") using 2:xtic(1) title col, '' u 3 ti col
#plot sprintf("%s/sr_tput.txt", expdir) using 2:xtic(1) title col, '' u 3 ti col, '' u 4 ti col
#plot sprintf("%s/sr_tput.txt", expdir) using 2:xticlabels(1) notitle w boxes linestyle 1
#set boxwidth 1
#set xdata
#set xrange [*:*]
#plot sprintf("%s/sr_tput.txt", expdir) using 2:xticlabels(1) notitle w boxes linestyle 1

