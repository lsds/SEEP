set terminal term 
expdir=sprintf("%s/%s/%sk/%sm/%ss",outputdir,timestr,k,mob,session)
set output sprintf("%s/op_tput_fixed_kmobsession_%s%s",expdir,timestr,termext)

set title sprintf("Operator interval tputs \nk=%s, mob=%s, query=%s, duration=%s, session=%s",k,mob,query,duration,session)
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

op_tput_files=system(sprintf("find %s -name 'op_*_interval_tput.txt' | sed 's/.*op_\\([0-9]\\+\\)_interval_tput.txt/\\1/'", expdir))

#plot for [file in op_tput_files] file using 1:2 notitle w steps linestyle 1
plot for [i=1:words(op_tput_files)] sprintf("%s/op_%s_interval_tput.txt", expdir, word(op_tput_files,i)) using 1:2 title word(op_tput_files,i) w fsteps linestyle i

