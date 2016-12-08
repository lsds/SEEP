set terminal term 
expdir=sprintf("%s/%s/%sk/%s%s/%ss",outputdir,timestr,k,var,varext,session)
set output sprintf("%s/link-tputs/link_cost_fixed_kvarsession_%s%s",expdir,timestr,termext)

set title sprintf("Link ETX costs \nk=%s, %s=%s, query=%s, duration=%s, session=%s",k,varname,var,query,duration,session)
set xlabel "Tx time (Seconds since epoch)"
set ylabel "Cost"
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
link_tput_files=system(sprintf("find %s/link-tputs -maxdepth 1 -name 'op_*_up_*_link_interval_tput.txt' | sed 's/.*op_\\([-]\\?[0-9]\\+\\)_up_\\([-]\\?[0-9]\\+\\)_link_interval_tput.txt/\\1_up_\\2/'", expdir))

plot for [i=1:words(link_tput_files)] sprintf("%s/link-tputs/op_%s_link_interval_tput.txt", expdir, word(link_tput_files,i)) using 1:4 title word(link_tput_files,i) w fsteps linestyle i
