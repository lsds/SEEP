set terminal term 
expdir=sprintf("%s/%s/%sk/%s%s/%ss",outputdir,timestr,k,var,varext,session)
set output sprintf("%s/op_tput_fixed_kvarsession_%s%s",expdir,timestr,termext)

set title sprintf("Operator interval tputs \nk=%s, %s=%s, query=%s, duration=%s, session=%s",k,varname,var,query,duration,session)
set xlabel "Tx time (Seconds since epoch)"
set ylabel "Tput"
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
op_tput_files=system(sprintf("find %s -maxdepth 1 -name 'op_*_interval_tput.txt' | sed 's/.*op_\\([-]\\?[0-9]\\+\\)_interval_tput.txt/\\1/'", expdir))

#plot for [file in op_tput_files] file using 1:2 notitle w steps linestyle 1
plot for [i=1:words(op_tput_files)] sprintf("%s/op_%s_interval_tput.txt", expdir, word(op_tput_files,i)) using 1:2 title word(op_tput_files,i) w fsteps linestyle i

op_sink_tput_files=system(sprintf("find %s -maxdepth 1 -name 'op_-*_interval_tput.txt' | sed 's/.*op_\\([-]\\?[0-9]\\+\\)_interval_tput.txt/\\1/'", expdir))

set title sprintf("Sink operator interval tput \nk=%s, %s=%s, query=%s, duration=%s, session=%s",k,varname,var,query,duration,session)
plot for [i=1:words(op_sink_tput_files)] sprintf("%s/op_%s_interval_tput.txt", expdir, word(op_sink_tput_files,i)) using 1:2 title word(op_sink_tput_files,i) w fsteps linestyle i
