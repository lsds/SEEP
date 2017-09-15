set terminal term 
expdir=sprintf("%s/%s",outputdir,timestr)
set output sprintf("%s/pi_op_cum_util_fixed_kvarsession_%s%s",expdir,timestr,termext)

set title sprintf("Operator cumulative utilization \nk=5, query=fdr, duration=860 tuples")
set xlabel "Tx time (Seconds since epoch)"
set ylabel "Util"
set yrange [0:1]
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
set offsets graph 0.0, 0.0, 0.1, 0.1
op_util_files=system(sprintf("find %s -maxdepth 1 -name 'op_*_utils.txt' | sed 's/.*op_\\([-]\\?[0-9]\\+\\)_utils.txt/\\1/'", expdir))

#plot for [file in op_util_files] file using 1:2 notitle w steps linestyle 1
#plot for [i=1:words(op_util_files)] sprintf("%s/op_%s_interval_util.txt", expdir, word(op_util_files,i)) using 1:3 title word(op_util_files,i) w fsteps linestyle i
plot for [i=1:words(op_util_files)] sprintf("%s/op_%s_utils.txt", expdir, word(op_util_files,i)) using 1:3 title word(op_util_files,i) w lines linestyle i

