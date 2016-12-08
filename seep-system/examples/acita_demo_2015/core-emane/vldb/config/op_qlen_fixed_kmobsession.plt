set terminal term 
expdir=sprintf("%s/%s/%sk/%s%s/%ss",outputdir,timestr,k,var,varext,session)
set output sprintf("%s/op_qlen_fixed_kvarsession_%s%s",expdir,timestr,termext)

set title sprintf("Operator queue lengths \nk=%s, %s=%s, query=%s, duration=%s, session=%s",k,varname,var,query,duration,session)
set xlabel "Tx time (Seconds since epoch)"
set ylabel "Queue length (batches)"
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
set offsets graph 0.0, 0.0, 0.1, 0.1
op_qlen_files=system(sprintf("find %s -maxdepth 1 -name 'op_*_qlens.txt' | sed 's/.*op_\\([-]\\?[0-9]\\+\\)_qlens.txt/\\1/'", expdir))

#plot for [file in op_tput_files] file using 1:2 notitle w steps linestyle 1
plot for [i=1:words(op_qlen_files)] sprintf("%s/op_%s_qlens.txt", expdir, word(op_qlen_files,i)) using 1:2 title word(op_qlen_files,i) w fsteps linestyle i

